import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import * as moment from 'moment';
import { Subscription } from 'rxjs';
import { TaskDetail } from '../common/intefaces/TaskResponse';
import { PopupContentComponent } from '../popup-content/popup-content.component';
import { CalendarService } from '../services/calendar/calendar.service';
import { TasksService } from '../services/tasks/tasks.service';
import { ModalComponent } from '../task-modal/modal/modal.component';

@Component({
  selector: 'app-teams-dashboard',
  templateUrl: './teams-dashboard.component.html',
  styleUrls: ['./teams-dashboard.component.scss']
})
export class TeamsDashboardComponent implements OnInit, OnDestroy {
  todaysTasks: TaskDetail[] = [];
  todoTasks: TaskDetail[] = [];

  @Input() teamId = 0;

  constructor(
    private calendarService: CalendarService,
    private taskService: TasksService,
    private _route: ActivatedRoute,
    private _snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  subscription: Subscription[] = [];
  ngOnInit() {
    this.subscription.push(this._route.params.subscribe(params => {
      this.teamId = params['teamId'];
      this.fetchTasks();
    }));
    this.subscription.push(this.taskService.onTriggerToReloadTasks()
      .subscribe(_ => this.loadTasks()));
  }


  loadTasks() {
    this.subscription.push(this.taskService
      .getDailyTaskForTeam(this.teamId, this.calendarService.getCurrentDate())
      .subscribe(tasks => {
        this.todaysTasks = tasks;
      }));

    this.subscription.push(this.taskService
      .getTaskInRangeForTeam(
        this.teamId,
        moment(this.calendarService.getCurrentDate()).add(1, 'days').toDate(),
        moment(this.calendarService.getCurrentDate()).add(8, 'days').toDate()
      )
      .subscribe(tasks => {
        this.todoTasks = tasks;
      }));
  }

  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }

  fetchTasks() {
    this.loadTasks();

    this.subscription.push(this.taskService
      .onTriggerRemoveTaskFromView
      .subscribe((taskId) => {
        //remove task from todaysTasks and todoTasks
        this.todaysTasks = this.todaysTasks.filter((task) => task.taskId !== taskId);
        this.todoTasks = this.todoTasks.filter((task) => task.taskId !== taskId);
      }));

    this.subscription.push(this.taskService
      .OnTodoTaskCreated
      .subscribe(task => {
        if (task.team === null || task.team?.team_id != this.teamId) {
          return;
        }
        console.log("todo created1");
        console.log(task);

        const date = moment(this.calendarService.getCurrentDate());
        if (
          date.isSameOrAfter(task.startDate) &&
          date.isSameOrBefore(task.endDate)
        ) {
          console.log("pushing new task");
          console.log(task);
          // Today's Task.
          this.todaysTasks.push(task);
        }

        const startOfWeek = date.clone().add(1, 'day').startOf('day');
        const endOfWeek = date.clone().add(8, 'day').endOf('day');

        if (
          endOfWeek.isSameOrAfter(task.startDate) &&
          startOfWeek.isSameOrBefore(task.endDate)
        ) {
          this.todoTasks.push(task);
        }
      }));
  }

  addNewTask() {
    this.dialog.open(ModalComponent, {
      data: {
        teamId: this.teamId
      }
    });

  }

  openSnackBar(message: string, action: string) {
    this._snackBar.open(message, action);
  }

  taskDescription(task: TaskDetail) {
    const dialogRef = this.dialog.open(PopupContentComponent, {
      data: {
        task,
        date: moment()
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      // console.log(`Dialog result: ${result}`);
    });
  }
}
