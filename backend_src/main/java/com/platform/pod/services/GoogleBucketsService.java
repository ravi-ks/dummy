package com.platform.pod.services;

import com.platform.pod.entities.Attachments;
import com.platform.pod.entities.Tasks;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.AttachmentsRepo;
import com.platform.pod.repositories.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class GoogleBucketsService {

    @Autowired
    TasksRepository tasksRepository;

    @Autowired
    AttachmentsRepo attachmentsRepo;

    /**
     * Service utility to convert MultipartFile to File
     * @author Ravikumar Shantharaju
     * @param file MultipartFile to be converted
     * @return converted File
     * @throws IOException
     */
    public File convertMultiPartToFile(MultipartFile file ) throws IOException
    {
        File convFile = new File( file.getOriginalFilename() );
        FileOutputStream fos = new FileOutputStream( convFile );
        fos.write( file.getBytes() );
        fos.close();
        return convFile;
    }

    /**
     * Service utility to insert meta-data of uploading file to DB
     * @author Ravikumar Shantharaju
     * @param fileName name of the file/attachment being uploaded
     * @param taskId id of the task, where attachment belongs to
     */
    public void insertDataToDB(String fileName, int taskId){
        Tasks task = tasksRepository.getById((long) taskId);

        Attachments attachment = new Attachments(0, fileName);
        Set<Attachments> taskAttachments = task.getAttachments();
        taskAttachments.add(attachment);
        task.setAttachments(taskAttachments);

        tasksRepository.save(task);
    }

    /**
     * THIS METHOD IS NEVER USED AS THE API CALLING THIS METHOD IS NEVER USED IN THE APPLICATION
     * Service utility to delete meta-data of deleting file from DB.
     * @author Ravikumar Shantharaju
     * @param fileName name of file/attachment to be deleted
     * @param taskId id of the task, where attachment belongs to
     * @throws ApiException if user who is deleting the attachment isn't an organizer
     * @return 1 for successful deletion and -1 otherwise
     */
    @Transactional
    public int deleteFile(String fileName, int taskId){
        Tasks task = tasksRepository.getById((long) taskId);

        //check if the one deleting the file/attachment is the one who organized this task
        //dummy organizer id, until authentication done
        int organizerId = 1; //env var
        if(task.getOrganizer().getUser_id() != organizerId){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Only task organizer can delete attachments, delete operation aborted.");
        }

        if(task != null) {
            Attachments attachment = attachmentsRepo.getByName(fileName);
            if(attachment != null) {
                Set<Attachments> attachments = task.getAttachments();
                attachments.remove(attachment);
                task.setAttachments(attachments);
                tasksRepository.save(task);
                attachmentsRepo.deleteByName(fileName);
                return 1;
            }
            System.out.println("Log Message: Attachment with specified name doesn't exist in DB" );
            return -1;
        }
        System.out.println("Log Message: Task with specified id doesn't exist in DB" );
        return -1;
    }

    /**
     * Service utility to get meta-data of all attachments belonging to a task from DB.
     * @author Ravikumar Shantharaju
     * @param taskId id of the task whose attachments are to be fetched
     * @return set of attachments for a task
     */
    public Set<Attachments> getAllAttachments(int taskId){
        return tasksRepository.getById((long) taskId).getAttachments();
    }

}
