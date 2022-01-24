import { Component, OnInit, Inject, Input, OnDestroy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { TasksService } from 'src/app/services/tasks/tasks.service';
import { Repetition } from 'src/app/common/intefaces/TaskResponse';
import { TeamSummary } from 'src/app/common/intefaces/TeamResponse';
import { Attachments, CreateTask } from 'src/app/common/intefaces/TaskBody';
import * as moment from 'moment';
import { TeamService } from 'src/app/services/team/team.service';
import { HttpClient } from '@angular/common/http';
import { AttachmentsService } from 'src/app/services/attachments/attachments.service';
import { DomSanitizer } from '@angular/platform-browser';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';

type RepetitionsDropdownTypes = "Once" | "Daily" | "WorkingDays";

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.scss']
})
export class ModalComponent implements OnInit, OnDestroy {
  taskId: number = -1;
  isAMeeting: boolean = true;
  taskDetails: Repetition | null = null;
  isEditPopUp: boolean = false;
  title: string = "";
  eventComp = true;
  event_btn_style = "event_btn_active";
  task_btn_style = "task_btn_inActive";
  private currDate = new Date();
  // taskEndDate = moment().format('L');
  form = new FormGroup({
    taskTitle: new FormControl('', [Validators.required,
    Validators.maxLength(50)]),
    // startTime: new FormControl({
    //   value: moment().format("HH:mm"),
    //   disabled: false
    // }, this.validStartTime.bind(this)),
    timeGroup: new FormGroup({
      startTime: new FormControl({ value: moment().format("HH:mm"), disabled: false }),
      endTime: new FormControl({ value: moment().format("HH:mm"), disabled: false })
    }, this.validTime.bind(this)),
    meetingLink: new FormControl(null),
    description: new FormControl()
  });
  selectedRepition: RepetitionsDropdownTypes = "Once";
  selectedPriority = "MEDIUM";
  repeats: RepetitionsDropdownTypes[] = [
    "Once",
    "Daily",
    "WorkingDays",
  ];
  priorities = ["LOW", "MEDIUM", "HIGH"];
  selectedTeamId: number = 0;
  teams: TeamSummary[] = [];
  attachments: Attachments[] = [];
  dateGroup = new FormGroup({
    startdate: new FormControl(''),
    endDateOfTask: new FormControl('')
  }, this.validDate.bind(this));

  //added by ravi
  uploadProgress = -1; //if uploadProgress is > -1, there's a upload going on, so show progress bar. Else hide it
  attahmentsFormData: FormData[] = []; //list of attachments form data to be sent viz API's after pressing submit
  taskCreatedSubscription: Subscription | null = null;
  constructor(
    public dialogRef: MatDialogRef<ModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private taskService: TasksService,
    private teamService: TeamService,
    private _snackBar: MatSnackBar,
    private attachmentService: AttachmentsService
  ) { }

  subscription: Subscription[] = [];
  ngOnInit(): void {
    this.taskId = this.data.taskId;
    // console.log("taskId: " + this.taskId);
    this.currDate.setHours(0, 0, 0, 0);
    this.dateGroup.get('startdate')?.setValue(this.currDate);
    this.dateGroup.get('endDateOfTask')?.setValue(this.currDate);
    this.subscription.push(this.teamService
      .onTeamUpdated
      .subscribe((teamsList) => { //get teams organized by logged in user
        this.teams = teamsList.slice();
        let none: TeamSummary = {
          teamId: 0,
          name: 'None',
          membersCount: 0
        };

        this.teams.splice(0, 0, none);
      }));
    this.teamService.getList();

    if (this.isAMeeting) {
      this.eventComp = true;
    }
    else {
      this.eventComp = false;
    }

    if (this.taskId === -1 || this.taskId === undefined) {
      this.isEditPopUp = false;
    }
    else {
      this.isEditPopUp = true;
      this.subscription.push(this.taskService
        .completeDescription(this.taskId)
        .subscribe((details: Repetition) => {
          this.taskDetails = details;
          this.title = details.task.title;
          if (details.task.type === "EVENT") {
            this.isAMeeting = true;
            this.eventClick();
          } else {
            this.isAMeeting = false;
            this.taskClick();
          }
          this.editTask();
        },
          (error) => {
            console.log("Error fetching the task");
          }));
    }

    if (this.data.teamId) {
      this.selectedTeamId = Number(this.data.teamId);
    }

  }

  get startDate() {
    return this.dateGroup.controls['startdate'] as FormControl;
  }

  get endDate() {
    return this.dateGroup.controls['endDateOfTask'] as FormControl;
  }

  get startTime(){
    return this.form?.get('timeGroup')?.get('startTime') as FormControl;
  }

  get endTime(){
    return this.form.get('timeGroup')?.get('endTime') as FormControl;
  }


  validDate(dateGroup: AbstractControl): ValidationErrors | null {
    if (dateGroup.get('startdate')?.value === undefined || dateGroup.get('endDateOfTask')?.value === undefined) {
      return null;
    }

    if (moment(dateGroup.get('startdate')?.value).isAfter(dateGroup.get('endDateOfTask')?.value)) {
      // Start Date is greater than end date.
      // console.log(dateGroup.get('startdate')?.value);

      return { startDateGreater: true };
    }

    if (
      moment()
        .set('hour', 0)
        .set('minute', 0)
        .set('second', 0)
        .isAfter(dateGroup.get('endDateOfTask')?.value, 'day')
    ) {
      return { endDateSmaller: true };
    }
    return null;
  }

  validTime(c: AbstractControl): ValidationErrors | null {
    const startTime: string | undefined = this.form?.get('timeGroup')?.get('startTime')?.value;
    const endTime: string | undefined = this.form?.get('timeGroup')?.get('endTime')?.value;
    if (startTime === undefined || endTime === undefined) {
      return null;
    }

    if (
      moment(this.parseTime(startTime))
        .isAfter(moment(this.parseTime(endTime)))) {
      return { invalidTime: true };
    }

    return null;
  }

  editTask() {
    // console.log(this.taskDetails);
    if (this.taskDetails !== null) {
      this.form.get('timeGroup')?.get('startTime')?.setValue(this.reverseFormatTime(this.parseTime(this.taskDetails.task.startTime)));
      this.form.get('timeGroup')?.get('endTime')?.setValue(this.reverseFormatTime(this.parseTime(this.taskDetails.task.endTime)));
      this.form.get('meetingLink')?.setValue(this.taskDetails.task.meetingLink);
      this.form.get('description')?.setValue(this.taskDetails.task.description);
      this.dateGroup.get('startdate')?.setValue(this.taskDetails.startsAt);
      this.dateGroup.get('endDateOfTask')?.setValue(this.taskDetails.endsOn);
      this.attachments = this.taskDetails.task.attachments;
      this.selectedPriority = this.taskDetails.task.priority;
      this.selectedTeamId = (this.taskDetails.task.team_assigned_with_task !== null) ? (this.taskDetails.task.team_assigned_with_task?.team_id) : 0;
      this.selectedRepition = this.getRepetitionType();
    }
  }

  getRepetitionType(): RepetitionsDropdownTypes {
    if (this.taskDetails !== null) {
      if (this.taskDetails.mon && this.taskDetails.tue && this.taskDetails.wed && this.taskDetails.thu && this.taskDetails.fri) {
        if (this.taskDetails.sun && this.taskDetails.sat) {
          // console.log("Day");
          return "Daily";
        }
        else {
          // console.log("Weekday");
          return "WorkingDays"
        }
      }
      // else if (this.taskDetails.sun && this.taskDetails.sat) {
      //   return "WeekEnd";
      // }
    }
    return "Once";
  }
  parseTime(t: string) {
    var d = new Date();
    t.slice(0, -2);
    const time = t.split(':');
    d.setHours(parseInt(time[0]));
    d.setMinutes(parseInt(time[1]));

    return d;
  }
  // noGreaterStartTime() {
  //   return (control: AbstractControl): ValidationErrors | null => {
  //     var sTime = (this.form?.get('startTime')?.value || '').toLowerCase().replace(' ', '');
  //     var eTime = (this.form?.get('endTime')?.value || '').toLowerCase().replace(' ', '');
  //     if (sTime.slice(-2) === 'pm' && eTime.slice(-2) === 'am') {
  //       return { noGreaterStartTime: true };
  //     }
  //     if (sTime.slice(-2) === 'am' && eTime.slice(-2) === 'pm') {
  //       return { noGreaterStartTime: false };
  //     }
  //     if (sTime.substring(0, 2) === '12' && eTime.substring(0, 2) != '12') {
  //       return { noGreaterStartTime: false };
  //     }
  //     let startDate = this.parseTime(sTime);
  //     let endDate = this.parseTime(eTime);

  //     if (startDate.getTime() > endDate.getTime()) {
  //       return { noGreaterStartTime: true };
  //     }
  //     return { noGreaterStartTime: false };
  //   };
  // }
  addAttachment(fileName: string) {
    this.attachments.unshift({
      attachment_url: "abc",
      name: fileName,
      imageSource: "../../../assets/icons/doc.svg"
    });
  }

  onFileSelected(fileList: FileList) {
    const file: File | null = fileList.item(0);
    if (file) {
      if (file.size > 10485760) {
        this._snackBar.open("File is too big, max limit is 10MB", "OK");
        return;
      }
      const formData = new FormData();
      formData.append("files", file);
      this.attahmentsFormData.push(formData); //list with all attachments details, to be uploaded after pressing submit
      this.addAttachment(file.name);
    }
  }


  dateChange() { }
  // get startTime() {
  //   return this.form.get('startTime') as FormControl;
  // }

  // get endTime() {
  //   return this.form.get('endTime') as FormControl;
  // }

  get meetingLink() {
    return this.form.get('meetingLink') as FormControl;
  }

  updateTask() {
    // console.log("The task will be updated");
    let task = this.getTaskDetails();
    this.taskService.updateTask(this.taskId, task).subscribe(() => {
      // console.log("Task updated successfully");
      this.uploadAllAttachments(this.taskId);
      this.dialogRef.close();
      this.taskService.triggerToReloadTasks();
      this._snackBar.open("Task Updated Successfully", "Done");
    },
      (error) => {
        this._snackBar.open("Task Updation Failed", "Done");
      })
  }


  saveTask() {
    // console.log("TimeError: " + this.timeError);
    // console.log("TitleError: " + !this.form.get('taskTitle')?.valid);
    // console.log("DateError: " + this.dateError);

    if (
      !this.form.get('taskTitle')?.valid ||
      !this.dateGroup?.valid ||
      !this.form?.get('timeGroup')?.valid
      // || !this.form?.get('endTime')?.valid
    ) {
      return;
    }

    if (this.isEditPopUp) {
      this.updateTask();
      return;
    }


    let task = this.getTaskDetails();
    this.taskService.createTask(task);
    this.subscription.push(this.taskService
      .OnTaskCreated
      .subscribe((_taskCreated) => {
        this.uploadAllAttachments(_taskCreated.taskId);
        this.dialogRef.close();
        this._snackBar.open("Task Created Successfully", "Done")
      },
        (error) => {
          this._snackBar.open("Task Creation Failed", "Done");
          // console.log(error);
          // console.log("Task Creation Failed");
        }));
  }

  uploadAllAttachments(taskId: number) {
    // console.log("Uploading following attachments: ");
    // console.log(this.attahmentsFormData);
    for (let i = 0; i < this.attahmentsFormData.length; i++) {
      let formData = this.attahmentsFormData[i];
      let fileName = '';
      this.attachmentService.uploadFile(formData, taskId)
        //todo: comfigure max size, ensure extension safety
        .subscribe(
          (response) => {
            // console.log(response);
            fileName = response;
          },
          error => {
          this._snackBar.open("File upload failed. Try again later", "OK");
          }
        );
    }
  }

  formatTime(time: string) {
    let reqTime = this.parseTime(time);
    // if (time.includes("PM")) {
    //   console.log("Executed");
    //   let newTime = this.parseTime(time).getTime() + 43200000;
    //   reqTime = new Date(newTime);
    // }
    return moment(reqTime).format("HH:mm:ssZ");
  }
  reverseFormatTime(time: Date) {
    return moment(time).format("HH:mm");
  }
  onNoClick() {
    this.dialogRef.close();
  }
  get taskTitle() {
    return this.form.get('taskTitle') as FormControl;
  }

  eventClick() {
    this.event_btn_style = "event_btn_active";
    this.task_btn_style = "task_btn_inActive";
    this.eventComp = true;
  }

  taskClick() {
    this.task_btn_style = "task_btn_active";
    this.event_btn_style = "event_btn_inActive";
    this.eventComp = false;
  }

  getTaskDetails(): CreateTask {
    const startsAt = moment(this.startDate.value).startOf('day').utcOffset('+0530');
    const endsOn = moment(this.endDate.value).endOf('day').utcOffset('+0530');
    const repetitionType: string[] = [];
    if (this.selectedRepition === "Once") {
      repetitionType.push(
        startsAt.format('ddd').toLowerCase()
      );
    } else {
      repetitionType.push(this.selectedRepition);
    }
    return {
      taskTitle: this.title,
      taskDescription: this.form.get('description')?.value, //spectate
      startTime: this.formatTime(this.form.get('timeGroup')?.get('startTime')?.value),
      endTime: this.formatTime(this.form.get('timeGroup')?.get('endTime')?.value),
      attachmentsURLList: [],
      type: this.eventComp ? "EVENT" : "TODO",
      priority: this.selectedPriority,
      meetingLink: this.form.get('meetingLink')?.value,
      repetitionType,
      startsAt: startsAt.format(),
      endsOn: endsOn.format(),
      assignedTeamId: this.selectedTeamId
    };
  }

  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }
}
