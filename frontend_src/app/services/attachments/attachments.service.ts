import { HttpClient, HttpEventType, HttpHeaders} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CommonResponse } from 'src/app/common/intefaces/CommonResponse';
import { environment } from 'src/environments/environment';
import {RequestOptions, Request, RequestMethod, ResponseContentType} from '@angular/http';

@Injectable({
  providedIn: 'root'
})
export class AttachmentsService {
  private readonly apiUrl = `${environment.apiURL}task`;


  constructor(private http: HttpClient) { }

  uploadFile(formData: FormData, taskId: number) {
    return this.http.post<string>(`${this.apiUrl}/${taskId}/uploadAttachment`, formData);
  }

  downloadFile(fileName: string, taskId: number) {
    let headers = new HttpHeaders({'Content-Type': 'application/octet-stream',
    'Accept': 'application/octet-stream'});

    return this.http.get(`${this.apiUrl}/${taskId}/downloadAttachment/${fileName}`,
    {headers: headers, responseType: 'blob'});
  }

  deleteFile() {
  }

  getAllAttachments(taskId: number) {
  }
}
