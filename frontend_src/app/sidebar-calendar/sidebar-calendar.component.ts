import { Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatCalendar } from '@angular/material/datepicker';
import { Subscription } from 'rxjs';
import { CalendarService } from '../services/calendar/calendar.service';

@Component({
  selector: 'app-sidebar-calendar',
  templateUrl: './sidebar-calendar.component.html',
  styleUrls: ['./sidebar-calendar.component.scss']
})
export class SidebarCalendarComponent implements OnInit,OnDestroy {
  selected: Date | null = null;
  month: String | null = null;
  year: number | null = null;
  @Input("monthSize") monthSize: "long" | "short" = "long";
  @ViewChild("calendar") calendar: MatCalendar<Date> | null = null;

  constructor(private calendarService: CalendarService) {
  }

  subscription : Subscription[]=[];
  ngOnInit(): void {
    const calendarSubscription=this.calendarService.OnDateChanged.subscribe((date: Date) => {
      this.calendar?._goToDateInView(date, "month");
      this.selected = date;
      this.month = date.toLocaleString("en-us", { month: this.monthSize });
      this.year = date.getFullYear();
    });
    this.subscription.push(calendarSubscription);
    this.calendarService.getDate();
  }

  selectedDateChanged(date: Date) {
    this.calendarService.changeDate(date);
  }

  goToNextMonth() {
    this.calendarService.nextMonth();
  }

  goToPreviousMonth() {
    this.calendarService.previousMonth();
  }

  ngOnDestroy(){
    this.subscription.forEach((sub)=>{
      sub.unsubscribe();
    })
  }
}
