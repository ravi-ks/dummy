package com.platform.pod.controllers;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.DateTime;
import com.google.api.services.storage.model.StorageObject;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.*;
import com.platform.pod.entities.Attachments;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.services.GoogleBucketsService;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;


@RequestMapping("/task")
@RestController
public class GoogleBucketsController {

    @Autowired
    Storage storage;

    @Autowired
    GoogleBucketsService googleBucketsService;

    /**
     * API to upload attachment for a task. It's stored in google cloud's buckets.
     * Also add the meta-data into database.
     * All filenames per task are deemed to be unique as it's appended with timestamp
     * @author Ravikumar Shantharaju
     * @param multipartFile file to be uploaded
     * @param taskId id of the task to which the attachment belongs to
     * @return fileName of the uploaded attachment
     * @throws IOException
     */
    @RequestMapping(value = "/{taskId}/uploadAttachment", method = RequestMethod.POST)
    public String uploadAttachment(@RequestParam("files") MultipartFile multipartFile, @PathVariable int taskId) throws IOException {
        try {
            //appending filename with timestamp (maintain uniqueness)
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            String fileName = multipartFile.getOriginalFilename();
            String basename = FilenameUtils.getBaseName(fileName);
            String extension = FilenameUtils.getExtension(fileName);
            fileName = basename + "-" + timeStamp + "." + extension;

            //convert multipart to file
            File file = googleBucketsService.convertMultiPartToFile(multipartFile);
            //add attachment to the task's folder
            BlobId blobId = BlobId.of("project-pod_com", "task-id-" + taskId + "/" + fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, Files.readAllBytes(file.toPath()));
            //delete file stored in server's src folder
            file.delete();

            //insert data to db
            googleBucketsService.insertDataToDB(fileName, taskId);


            String string = String.format("\"%s\"", fileName);
            return string;
        }
        catch(SizeLimitExceededException sizeLimitExceededException){
            throw new ApiException(HttpStatus.BAD_REQUEST, "File limit exceeded, max size is ceiled at 10MB per file");
        }
    }


    /**
     * API to download attachment for a task. It's fetched from Google cloud's buckets.
     * The filename is first stripped of timestamp, and then sent
     * @author Ravikumar Shantharaju
     * @param fileName name of the file/attachment to download
     * @param taskId if of the task, that the attachment belongs to
     * @return Requested file, as a downloadable.
     * @throws IOException
     */

    @RequestMapping(value = "/{taskId}/downloadAttachment/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadAttachment(@PathVariable("fileName") String fileName, @PathVariable("taskId") int taskId) throws IOException {
        File file = null;
        try {
            Blob blob = storage.get("project-pod_com", "task-id-" + taskId + "/" + fileName);
            ReadChannel readChannel = blob.reader();
            file = new File(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.getChannel().transferFrom(readChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();

            String basename = FilenameUtils.getBaseName(fileName);
            String extension = FilenameUtils.getExtension(fileName);
            basename = basename.substring(0, basename.length() - 20);
            fileName = basename + "." + extension;
            //System.out.println("\n\n\nFile Name after stripping timestamp: " + basename + "." + extension);


            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
        catch(NullPointerException e){
            throw new ApiException(HttpStatus.NOT_FOUND, "Possibly, file not found. More Info: " + e.getMessage());
        }
        finally {
            //delete the file afterwords
            if(file != null)
                file.delete();
        }
    }

    /**
     * THIS API IS NEVER USED IN THE APPLICATION (YET)
     * API to delete attachment of a task, by invocating deleteFile method of GoogleBucketsService.
     * Deletion here is just marking the file deleted in the DB,
     * as actual deletion from GCloud's buckets is forbidden due to lack of permissions
     * @author Ravikumar Shantharaju
     * @param fileName name of the file/attachment to delete
     * @param taskId id of the task, that the attachment belongs to
     * @return 1 for successful deletion and -1 otherwise
     * @throws IOException
     */
    @RequestMapping(value = "/{taskId}/deleteAttachment/{fileName}", method = RequestMethod.DELETE)
    public int deleteAttachment(@PathVariable("fileName") String fileName, @PathVariable("taskId") int taskId) throws IOException {
        //only deletes record from database, as delete previlage isn't given on google cloud
        return googleBucketsService.deleteFile(fileName, taskId);
    }

    /**
     * API to get all attachment for a task, by invocating getAllAttachments method of GoogleBucketsService.
     * @author Ravikumar Shantharaju
     * @param taskId id of the task whose attachments are to be fetched
     * @return set of attachments for given taskId
     */
    @RequestMapping(value = "/{taskId}/getAllAttachments", method = RequestMethod.GET)
    public Set<Attachments> getAllAttachments(@PathVariable("taskId") int taskId){
        return googleBucketsService.getAllAttachments(taskId);
    }


}
