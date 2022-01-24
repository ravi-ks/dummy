import { Subscription } from 'rxjs';
import { AfterViewInit, Component, ElementRef, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import * as moment from 'moment';
import Constants from 'src/app/common/Constants';
import { TaskDetail } from 'src/app/common/intefaces/TaskResponse';
import { ViewTypes } from 'src/app/common/intefaces/view-types';
import { PopupContentComponent } from 'src/app/popup-content/popup-content.component';
import { CalendarService } from 'src/app/services/calendar/calendar.service';
import { TasksService } from 'src/app/services/tasks/tasks.service';
import { TimeService } from 'src/app/services/time/time.service';

interface CalendarCell {
  show: string;
  date: moment.Moment;
  current?: boolean;
}

interface TaskByWeek {
  [key: string]: TaskDetail[];
}

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss']
})
export class CalendarComponent implements OnInit, AfterViewInit, OnDestroy {
  weeknames: string[] = moment.weekdays().map(e => e.substring(0, 3).toUpperCase());
  @ViewChild('parent') parent: ElementRef | null = null;

  cells: CalendarCell[] = new Array(24);
  times: string[] = [];
  weekDates: moment.Moment[] = [
    moment(),
    moment(),
    moment(),
    moment(),
    moment(),
    moment(),
    moment()
  ];

  currenDateBoolean: boolean[] = [
    false,
    false,
    false,
    false,
    false,
    false,
    false,
  ];

  viewType: ViewTypes = ViewTypes.day;

  tasksByWeek: TaskByWeek = {};
  tasksByDay: TaskDetail[] = [];

  constructor(
    private calendarService: CalendarService,
    private taskService: TasksService,
    public dialog: MatDialog,
    private timeService: TimeService
  ) {
    let start = 12, meridian = true;
    for (let i = 0; i < 24; ++i) {
      this.times.push(`${start} ${meridian ? 'PM' : 'AM'}`);
      if (start % 12 === 0) meridian = !meridian;
      start = start % 12 + 1;
    }
    this.times[0] = "";

    this.weeknames = [
      moment(calendarService.getCurrentDate()).format('ddd')
    ];
    this.currenDateBoolean[0] = true;
    this.resetTaskByWeek();
  }

  subscription: Subscription[] = [];
  ngOnInit(): void {
    //when task reload is triggered, we basically send a deceptive signalDateChange, hence reloading the tasks in calendar
    this.subscription.push(this.taskService
      .onTriggerToReloadTasks()
      .subscribe(_ => this.calendarService.signalDateChange()));

    //if task is deleted, mirror it in calendar view
    this.subscription.push(this.taskService
      .onTriggerRemoveTaskFromView
      .subscribe((taskId) => {
        if (this.viewType === ViewTypes.day) {
          this.tasksByDay = this.tasksByDay.filter((task) => task.taskId !== taskId);
        } else {
          Object.keys(this.tasksByWeek).forEach(key => {
            this.tasksByWeek[key] = this.tasksByWeek[key].filter((task) => task.taskId !== taskId);
          });
        }
      }));
    this.subscription.push(this.calendarService
      .OnDateChanged
      .subscribe(date => {
        if (this.viewType === ViewTypes.week) {
          this.forWeek(date);
        } else if (this.viewType === ViewTypes.day) {
          this.updateDayHeader(date);
        } else if (this.viewType === ViewTypes.month) {
          this.forMonth(date);
        }
      }));

    this.subscription.push(this.calendarService
      .onViewTypeChanged
      .subscribe(type => {
        this.viewType = type;
        if (type === ViewTypes.day) {
          this.cells = new Array(24);
        } else if (type === ViewTypes.week || type === ViewTypes.month) {
          this.cells = new Array(24 * 7);
          this.weeknames = moment.weekdays().map(e => e.substr(0, 3).toUpperCase());
        }
        this.calendarService.getDate();
      }));


    this.subscription.push(this.taskService
      .OnTaskCreated
      .subscribe(task => {
        if (this.viewType === ViewTypes.day) {
          const date = moment(this.calendarService.getSelectedDate());
          if (
            date.isSameOrAfter(task.startDate) &&
            date.isSameOrBefore(task.endDate)
          ) {
            // Day's Task.
            this.tasksByDay.push(task);
          }
        } else {
          this.extractIndividualTask(task);
        }
      }));

    this.subscription.push(this.timeService
      .onTimeUpdate
      .subscribe(() => this.scrollView()));

    // this.viewType = this.calendarService.getViewType();
    // this.calendarService.getDate();
    this.calendarService.callViewType();
  }

  updateDayHeader(date: Date) {
    this.subscription.push(this.taskService
      .getDailyTaskForUser(date)
      .subscribe(tasks => {
        this.tasksByDay = tasks;
      }));
    this.weeknames = [
      moment(date).format('ddd')
    ];
    this.weekDates[0] = moment(date);
    this.currenDateBoolean[0] = moment(date).isSame(
      moment(this.calendarService.getCurrentDate()),
      'day'
    );
  }

  forWeek(date: Date) {
    const startOfWeek = moment(date).startOf('week');
    const endOfWeek = moment(date).endOf('week');
    this.subscription.push(this.taskService
      .getTaskInRangeForUser(startOfWeek.toDate(), endOfWeek.toDate())
      .subscribe(tasks => {
        this.extractTaskByWeek(tasks);
      }));
    let i = 0;
    do {
      this.weekDates[i] = startOfWeek.clone();
      this.currenDateBoolean[i] = startOfWeek.isSame(
        moment(this.calendarService.getCurrentDate()),
        'day'
      );
      startOfWeek.add(1, 'day')
      ++i;
    } while (i < 7);
  }

  resetTaskByWeek() {
    this.tasksByWeek = {
      SUN: [],
      MON: [],
      TUE: [],
      WED: [],
      THU: [],
      FRI: [],
      SAT: [],
    };
  }

  extractIndividualTask(task: TaskDetail) {
    for (const week of Object.entries(task.repeat)) {
      if (week[1]) {
        this.tasksByWeek[week[0].toUpperCase()].push(task);
      }
    }
  }

  extractTaskByWeek(tasks: TaskDetail[]) {
    this.resetTaskByWeek();
    for (const task of tasks) {
      this.extractIndividualTask(task);
    }
  }

  forMonth(date: Date) {
    const startOfMonth = moment(date).startOf('month');
    const endOfMonth = moment(date).endOf('month');
    const startOFWeek = moment(startOfMonth).startOf('week');
    const endOfWeek = endOfMonth.clone().endOf('week');
    const startDiff = startOfMonth.diff(startOFWeek, 'days');
    const endDiff = endOfWeek.diff(endOfMonth, 'days');
    const dates: CalendarCell[] = [];
    const currentDate = this.calendarService.getCurrentDate();
    const isCurrentMonthYear = (date.getMonth() === currentDate.getMonth()) && (date.getFullYear() === currentDate.getFullYear());

    this.subscription.push(this.taskService
      .getTaskInRangeForUser(startOFWeek.toDate(), endOfWeek.toDate())
      .subscribe(tasks => this.extractTaskByWeek(tasks)));



    for (let i = 0; i < startDiff; ++i) {
      if (i < 1) {
        dates.push({
          show: startOFWeek.format('MMM DD'),
          date: startOFWeek.clone()
        });
      }
      else {
        dates.push({
          show: startOFWeek.format('DD'),
          date: startOFWeek.clone()
        });
      }
      startOFWeek.add(1, 'day');
    }

    const monthEndDate = endOfMonth.get('date');
    for (let i = startOfMonth.get('date'); i <= monthEndDate; ++i) {
      dates.push({
        show: `${i}`,
        date: startOfMonth.clone(),
        current: isCurrentMonthYear && (i === currentDate.getDate())
      });
      startOfMonth.add(1, 'day');
    }

    for (let i = 0; i < endDiff; ++i) {
      endOfMonth.add(1, 'day');
      if (i < 1) {
        dates.push({
          show: endOfMonth.format('MMM DD'),
          date: endOfMonth.clone()
        });
      }
      else {
        dates.push({
          show: endOfMonth.format('DD'),
          date: endOfMonth.clone()
        });
      }
    }
    this.cells = dates;
  }

  getTask(index: number, date: moment.Moment, task: TaskDetail): TaskDetail | null {
    if (
      date.isSameOrAfter(task.startDate) &&
      date.isSameOrBefore(task.endDate)
    )
      return task;

    return null;
  }

  public get allViewTypes(): typeof ViewTypes {
    return ViewTypes;
  }

  openTask(task: TaskDetail, date: moment.Moment): void {
    const dialogRef = this.dialog.open(PopupContentComponent, {
      data: {
        task,
        date
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      // console.log(`Dialog result: ${result}`);
    });

  }


  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }

  scrollView() {
    if (this.parent && this.calendarService.isSameDate()) {
      const position = moment().get('hour') * Constants.HEIGHT_PER_HOUR;
      (this.parent.nativeElement as HTMLDivElement).scrollTo(0, position);
    }
  }

  ngAfterViewInit() {
    this.scrollView();
    this.timeService.getTime();
  }

}
