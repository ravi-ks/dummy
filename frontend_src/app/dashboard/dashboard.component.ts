import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import * as moment from 'moment';
import { Subscription } from 'rxjs';
import { TaskDetail } from '../common/intefaces/TaskResponse';
import { PopupContentComponent } from '../popup-content/popup-content.component';
import { CalendarService } from '../services/calendar/calendar.service';
import { TasksService } from '../services/tasks/tasks.service';
import { TeamService } from '../services/team/team.service';
import { ModalComponent } from '../task-modal/modal/modal.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  todaysTasks: TaskDetail[] = [];
  todoTasks: TaskDetail[] = [];

  constructor(
    private calendarService: CalendarService,
    private taskService: TasksService,
    private dialog: MatDialog,
    private teamService: TeamService
  ) { }

  subscription: Subscription[] = [];
  //reloads tasks (todo's and events - can be used after editing task)
  reloadTasks() {
    this.subscription.push(this.taskService
      // .getTaskInRangeForUser(new Date('2021-08-29T00:00:00'), new Date('2021-08-31T00:00:00'))
      .getTodosByDay(this.calendarService.getCurrentDate())
      .subscribe(tasks => {
        this.todaysTasks = tasks;
      }));

    const tommorow = moment(this.calendarService.getCurrentDate()).add(1, 'days').toDate()
    this.subscription.push(this.taskService
      .getTodosFromDay(tommorow)
      .subscribe(tasks => this.todoTasks = tasks));
  }

  ngOnInit(): void {
    //when reloadTask is triggered, reload tasks
    this.subscription.push(
      this.taskService
        .onTriggerToReloadTasks()
        .subscribe(_ => this.reloadTasks())
    );

    this.subscription.push(
      this.teamService
        .onDeleteTeam
        .subscribe(_ => this.reloadTasks())
    );

    this.subscription.push(this.taskService
      .getTodosByDay(this.calendarService.getCurrentDate())
      .subscribe(tasks => {
        this.todaysTasks = tasks;
      }));


    this.subscription.push(this.taskService
      .onTriggerRemoveTaskFromView
      .subscribe((taskId) => {
        //remove task from todaysTasks and todoTasks
        this.todaysTasks = this.todaysTasks.filter((task) => task.taskId !== taskId);
        this.todoTasks = this.todoTasks.filter((task) => task.taskId !== taskId);
      }));

    const tommorow = moment(this.calendarService.getCurrentDate()).add(1, 'days').toDate()
    this.subscription.push(this.taskService
      .getTodosFromDay(tommorow)
      .subscribe(tasks => this.todoTasks = tasks));


    this.subscription.push(this.taskService
      .OnTodoTaskCreated
      .subscribe(task => {
        const date = moment(this.calendarService.getCurrentDate());
        if (
          date.isSameOrAfter(task.startDate) &&
          date.isSameOrBefore(task.endDate)
        ) {
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


    this.subscription.push(
      this.taskService
        .OnCheckTask
        .subscribe(taskId => {
          this.todaysTasks = this.todaysTasks.map((task) => {
            if (task.taskId === taskId) {
              task.checked = true;
            }
            return task;
          });
          this.todoTasks = this.todoTasks.map((task) => {
            if (task.taskId === taskId) {
              task.checked = true;
            }
            return task;
          });
        })
    );
  }

  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }
  openTask(task: TaskDetail) {
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
  openUpcomingTask(task: TaskDetail){
    let date = moment();
    if(moment(task.startDate).isAfter(date)){
      date = moment(task.startDate);
    }
    else if(moment(task.startDate).isBefore(date)){
      if(moment(task.endDate).isSameOrAfter(moment().add(1, 'days'))){
        date = moment().add(1, 'days');
      }
    }
    const dialogRef = this.dialog.open(PopupContentComponent, {
      data: {
        task,
        date: date
      }
    });
  }
  addNewTask() {
    let dialogRef = this.dialog.open(ModalComponent, {
      data: {}
    });
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog Result : ${result}`);
    });
  }
}
