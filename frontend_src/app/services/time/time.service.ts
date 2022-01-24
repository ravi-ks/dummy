import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { CalendarService } from '../calendar/calendar.service';

@Injectable({
  providedIn: 'root'
})
export class TimeService {
  private readonly refreshRate = 1000 * 60 * 10;
  private interval: number | null = null;
  private updateTime: Subject<void> = new Subject<void>();

  constructor(
    private calendarService: CalendarService
  ) {
  }

  startTimer() {
    this.calendarService
      .OnDateChanged
      .subscribe(_ => {
        if (this.calendarService.isSameDate()) {
          if (this.interval === null) {
            this.startInterval();
          }
        } else {
          if (this.interval !== null) {
            window.clearInterval(this.interval);
            this.interval = null;
          }
        }
      });

  }

  startInterval() {
    if (this.interval !== null) {
      return;
    }

    this.interval = window.setInterval(() => {
      this.updateTime.next();
    }, this.refreshRate);
  }

  getTime() {
    this.updateTime.next();
  }

  public get onTimeUpdate() {
    return this.updateTime.asObservable();
  }
}
