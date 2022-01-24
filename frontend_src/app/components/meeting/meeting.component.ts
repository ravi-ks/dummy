import { AfterViewChecked, AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import * as moment from 'moment';
import { Observable, Subscription } from 'rxjs';
import Constants from 'src/app/common/Constants';
import { TaskDetail } from 'src/app/common/intefaces/TaskResponse';
import { PopupContentComponent } from 'src/app/popup-content/popup-content.component';
import { CalendarService } from 'src/app/services/calendar/calendar.service';
import { TasksService } from 'src/app/services/tasks/tasks.service';
import { TimeService } from 'src/app/services/time/time.service';

@Component({
  selector: 'app-meeting',
  templateUrl: './meeting.component.html',
  styleUrls: ['./meeting.component.scss']
})
export class MeetingComponent implements OnInit, AfterViewInit, OnDestroy {
  times: String[] = new Array<String>(24);
  meetings: TaskDetail[] = [];
  @Input() height = 400;
  @ViewChild('parent') parent: ElementRef | null = null;
  constructor(
    private taskService: TasksService,
    private calendarService: CalendarService,
    private timeService: TimeService,
    private dialog: MatDialog
  ) { }

  subscription: Subscription[] = [];
  ngOnInit(): void {
    let start = 12, meridian = true;
    for (let i = 0; i < 24; ++i) {
      this.times[i] = `${start} ${meridian ? 'PM' : 'AM'}`;
      if (start % 12 === 0) meridian = !meridian;
      start = start % 12 + 1;
    }

    //trigger dynamic layout update on event update
    this.subscription.push(this.taskService
    .onTriggerToReloadTasks()
    .subscribe(_ => this.calendarService.signalDateChange()));

    this.subscription.push(this.taskService
      .OnEventTaskCreated
      .subscribe(task => {
        const date = moment(this.calendarService.getCurrentDate());
        if (
          date.isSameOrAfter(task.startDate) &&
          date.isSameOrBefore(task.endDate)
        ) {
          // Today's Task.
          this.meetings.push(task);
        }

      }));
    const calendarSubscription = this.calendarService
      .OnDateChanged
      .subscribe(date => {
        const taskSubscription = this.taskService
          .getEventsByDay(date)
          .subscribe(meetings => {
            this.meetings = meetings;
          });
        this.subscription.push(taskSubscription);
      });
    this.subscription.push(calendarSubscription);

    this.calendarService.getDate();
    const timeSubscription = this.timeService.onTimeUpdate.subscribe(() => this.scrollView());
    this.subscription.push(timeSubscription);
    this.calendarService.getDate();

    this.timeService
      .onTimeUpdate
      .subscribe(() => this.scrollView());


    this.subscription.push(
      this.taskService
        .onTriggerRemoveTaskFromView
        .subscribe(taskId => this.meetings = this.meetings.filter(task => task.taskId !== taskId))
    );
  }

  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }

  scrollView() {
    if (this.parent) {
      const position = moment().get('hour') * Constants.HEIGHT_PER_HOUR;
      (this.parent.nativeElement as HTMLDivElement).scrollTo(0, position);
    }
  }

  ngAfterViewInit() {
    this.scrollView();
    this.timeService.getTime();
  }

  openDescription(meet: TaskDetail) {
    this.dialog.open(PopupContentComponent, {
      data: {
        task: meet,
        date: moment(this.calendarService.getSelectedDate())
      }
    });
  }

}
