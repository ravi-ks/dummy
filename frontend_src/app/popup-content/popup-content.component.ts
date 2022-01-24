import { Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import * as moment from 'moment';
import { Subscription } from 'rxjs';
import { CommonResponse } from '../common/intefaces/CommonResponse';
import { TaskDescription, Repetition, TaskDetail } from '../common/intefaces/TaskResponse';
import Utility from '../common/Utility';
import { AttachmentsService } from '../services/attachments/attachments.service';
import { TasksService } from '../services/tasks/tasks.service';
import { ModalComponent } from '../task-modal/modal/modal.component';
import * as downloadjs from 'downloadjs';
import { ConfirmDeleteComponent } from '../confirm-delete/confirm-delete.component';


interface PopupContentData {
  date: moment.Moment;
  task: TaskDetail;
}

@Component({
  selector: 'app-popup-content',
  templateUrl: './popup-content.component.html',
  styleUrls: ['./popup-content.component.scss']
})
export class PopupContentComponent implements OnInit, OnDestroy {

  taskDescription: TaskDescription | null = null;
  date: String | null = null;
  startTime: String | null = null;
  endTime: String | null = null;
  isAMeeting = true;
  teamTask: boolean = false;
  completed: boolean = false;
  organizerInitials: String = "";
  organizerName: string = "";
  hasAttachments: boolean = false;
  @ViewChild("taskCompletion") taskCompletion: ElementRef | null = null;

  constructor(
    private dialogRef: MatDialogRef<PopupContentComponent>,
    @Inject(MAT_DIALOG_DATA) private data: PopupContentData,
    private attachmentService: AttachmentsService,
    private tasksService: TasksService, private dialog: MatDialog,
    private confirmDialog: MatDialog
  ) {
    this.date = this.data.date?.format('dddd, MMMM DD');
    this.startTime = moment('2021-08-01T' + this.data.task?.startTime)?.format('h:mma');
    this.endTime = moment('2021-08-01T' + this.data.task?.endTime)?.format('h:mma');
    // this.endTime = this.data.endTime;
  }

  subscription: Subscription[] = [];
  ngOnInit(): void {
    this.subscription.push(this.tasksService
      .getTaskDescription(this.data.task.taskId)
      .subscribe((apiData: TaskDescription) => {
        this.taskDescription = apiData;
        this.teamTask = (this.taskDescription.totalMemberInTeam !== 0);
        // assumption: For a non-organizer user, the value of totalChecks will be 1 if the user completed the task else 0
        this.completed = (this.taskDescription.totalChecks === 1);

        this.isAMeeting = this.taskDescription.taskType === "EVENT";
        this.hasAttachments = this.taskDescription.attachments.length !== 0;
        this.organizerName = this.taskDescription.organizer.name;
        this.organizerInitials = Utility.getUserInitials(this.organizerName);
      },
        (error) => {
          console.log("Error Occured.");
        }))
  }



  editTask() {
    // console.log("We will edit the task");
    let dialogRef = this.dialog.open(ModalComponent, {
      data: { taskId: this.data.task.taskId }
    });
    this.dialogRef.close();
  }

  confirmDelete() {
    let confirmTaskDialogRef = this.confirmDialog.open(ConfirmDeleteComponent, { data: { message: 'Task' } });
    confirmTaskDialogRef.afterClosed().subscribe(result => {
      if (result === "true") {
        this.deleteTask();
      }
    })
  }

  deleteTask() {
    if (this.taskDescription != null) {
      this.subscription.push(
        this.tasksService
          .deleteTask(this.taskDescription.id)
          .subscribe(
            () => {
              this.dialogRef.close();
            },
            (error) => {
              console.log("Task Deletion failed");
            }
          )
      );
    }

  }

  checkUncheck() {
    if (this.taskDescription != null) {
      this.subscription.push(
        this.tasksService
          .checkTask(this.taskDescription.id)
          .subscribe((_) => {
            this.completed = true;
          })
      );
    }
  }
  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }

  downloadAttachment(fileName: string) {
    if (this.taskDescription)
      this.attachmentService.downloadFile(fileName, this.taskDescription?.id)
        .subscribe(res => {
          const file = new Blob([res], { type: 'octet-stream' });
          downloadjs(file, fileName);
        });
  }
}
