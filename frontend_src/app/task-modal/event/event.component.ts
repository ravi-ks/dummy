import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import * as moment from 'moment';
import { EventValidators } from './event.validators';
import { TeamService } from 'src/app/services/team/team.service';
import { Attachments } from 'src/app/common/intefaces/TaskBody';
import { HttpClient } from '@angular/common/http';
import { CreateTask } from '../../common/intefaces/TaskBody';
import { TeamSummary } from 'src/app/common/intefaces/TeamResponse';
import { TasksService } from 'src/app/services/tasks/tasks.service';
import { Repetition, TeamAssignedWithTask } from 'src/app/common/intefaces/TaskResponse';
import { Subscription } from 'rxjs';
@Component({
  selector: 'app-event',
  templateUrl: './event.component.html',
  styleUrls: ['./event.component.scss']
})
export class EventComponent implements OnInit, OnDestroy {
  @Input() isEvent = true;
  @Input() title = "";
  @Input() taskDetails: Repetition | null = null;
  @Input() taskId: number = -1;
  sTime = moment().format('LT');
  eTime = moment().format('LT');
  private currDate = new Date();
  // taskEndDate = moment().format('L');
  dateError: boolean = false;
  form = new FormGroup({
    taskTitle: new FormControl('', [Validators.required,
    Validators.maxLength(50)]),
    startTime: new FormControl({
      value: this.sTime,
      disabled: false
    }, [
      Validators.pattern(/^([1-9]|1[0-2]|0[1-9]):[0-5][0-9] ?([AaPp][Mm]) ?$/),
      this.noGreaterStartTime()
    ]),
    endTime: new FormControl({ value: this.eTime, disabled: false }, [Validators.pattern(/^([1-9]|1[0-2]|0[1-9]):[0-5][0-9] ?([AaPp][Mm]) ?$/),
    this.noGreaterStartTime()]),
    meetingLink: new FormControl(null, [Validators.required]),
    description: new FormControl()
  });;
  selectedRepition = "";
  selectedPriority = "";
  repeats = ["Daily", "WorkingDays"];
  priorities = ["LOW", "MEDIUM", "HIGH"];
  selectedTeamId: number = -1;
  teams: TeamSummary[] = [];
  attachments: Attachments[] = [];
  startdate = new FormControl();
  endDateOfTask = new FormControl();

  constructor(private teamService: TeamService, private http: HttpClient, private taskService: TasksService) {
  }

  subscription: Subscription[] = [];
  ngOnInit(): void {
    this.currDate.setHours(0, 0, 0, 0);
    this.startdate.setValue(this.currDate);
    this.endDateOfTask.setValue(this.currDate);
    this.subscription.push(this.teamService
      .onTeamUpdated
      .subscribe((teamsList) => this.teams = teamsList)); //get teams organized by logged in user

    this.teamService.getList();
    this.subscription.push(this.taskService.completeDescription(this.taskId).subscribe((details: Repetition) => {
      console.log("Call is successful");
      this.taskDetails = details;
      this.title = details.task.title;
      console.log(this.taskDetails);
      this.editTask();
    },
      (error) => {
        console.log("Error fetching the task");
      }));
  }

  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }

  editTask() {
    if (this.taskDetails !== null) {
      console.log("task details" + this.taskDetails);
      this.form.get('startTime')?.setValue(this.taskDetails.task.startTime);
      this.form.get('endTime')?.setValue(this.taskDetails.task.endTime);
      this.form.get('meetingLink')?.setValue(this.taskDetails.task.meetingLink);
      this.form.get('description')?.setValue(this.taskDetails.task.description);
      this.startdate.setValue(this.taskDetails.startsAt);
      this.endDateOfTask.setValue(this.taskDetails.endsOn);
      this.attachments = this.taskDetails.task.attachments;
      this.selectedPriority = this.taskDetails.task.priority;
      this.selectedTeamId = (this.taskDetails.task.team_assigned_with_task === null) ? 0 : this.taskDetails.task.team_assigned_with_task.team_id;
    }
  }
  get startTime() {
    return this.form.get('startTime') as FormControl;
  }

  get endTime() {
    return this.form.get('endTime') as FormControl;
  }

  get meetingLink() {
    return this.form.get('meetingLink') as FormControl;
  }

  parseTime(t: string) {
    var d = new Date();
    t.slice(0, -2);
    const time = t.split(':');
    d.setHours(parseInt(time[0]));
    d.setMinutes(parseInt(time[1]));

    return d;
  }
  noGreaterStartTime() {
    return (control: AbstractControl): ValidationErrors | null => {
      var sTime = (this.form?.get('startTime')?.value || '').toLowerCase().replace(' ', '');
      var eTime = (this.form?.get('endTime')?.value || '').toLowerCase().replace(' ', '');
      if (sTime.slice(-2) === 'pm' && eTime.slice(-2) === 'am') {
        return { noGreaterStartTime: true };
      }
      if (sTime.slice(-2) === 'am' && eTime.slice(-2) === 'pm') {
        return { noGreaterStartTime: false };
      }
      if (sTime.substring(0, 2) === '12' && eTime.substring(0, 2) != '12') {
        return { noGreaterStartTime: false };
      }
      let startDate = this.parseTime(sTime);
      let endDate = this.parseTime(eTime);

      if (startDate.getTime() > endDate.getTime()) {
        return { noGreaterStartTime: true };
      }
      return { noGreaterStartTime: false };
    };
  }

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
      this.addAttachment(file.name);
      const formData = new FormData();
      formData.append("thumbnail", file);
      const upload$ = this.http.post("http://localhost:4200", formData);
      upload$.subscribe();
    }
  }

  dateChange() {
    // console.log(this.endDateOfTask.value);
    // console.log(this.startdate.value);
    if (this.endDateOfTask.value < this.startdate.value) {
      this.dateError = true;
    }
    else {
      this.dateError = false;
    }
  }

  get taskTitle() {
    return this.form.get('taskTitle') as FormControl;
  }


  saveTask() {
    let task: CreateTask = {
      taskTitle: this.title,
      taskDescription: this.form.get('description')?.value, //spectate
      startTime: this.formatTime(this.form.get('startTime')?.value),
      endTime: this.formatTime(this.form.get('endTime')?.value),
      attachmentsURLList: [],
      type: this.isEvent ? "EVENT" : "TODO",
      priority: this.selectedPriority,
      meetingLink: this.form.get('meetingLink')?.value,
      repetitionType: Array.of(this.selectedRepition),
      startsAt: this.startdate.value,
      endsOn: this.endDateOfTask.value,
      assignedTeamId: this.selectedTeamId
    };
    console.log("saving task: " + task);
    this.taskService.createTask(task);
    this.taskService.OnTaskCreated.subscribe((_) => { });
  }

  formatTime(time: string) {
    let reqTime = this.parseTime(time);
    if (time.includes("PM")) {
      let newTime = this.parseTime(time).getTime() + 43200000;
      reqTime = new Date(newTime);
    }
    return moment(reqTime).format("HH:mm:ssZ");

  }
}
