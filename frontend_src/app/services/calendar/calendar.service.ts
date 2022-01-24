import { Injectable } from '@angular/core';
import * as moment from 'moment';
import { Observable, Subject } from 'rxjs';
import { ViewTypes } from 'src/app/common/intefaces/view-types';

@Injectable({
  providedIn: 'root'
})
export class CalendarService {
  /**
   * Current Date.
   */
  private readonly currentDate: Date = new Date();
  /**
   * Selected Date.
   */
  private selectedDate: Date = this.currentDate;
  /**
   * Selected View Type.
   */
  private selectedType: ViewTypes = ViewTypes.day;

  /**
   * Observable For Selected Date Changed.
   */
  private onSelectedDateChanged: Subject<Date> = new Subject<Date>();

  /**
   * Observable for View Type changes.
   */
  private onSelectedTypeChanged: Subject<ViewTypes> = new Subject<ViewTypes>();

  constructor() { }

  /**
   * Reset Date back to Current Date.
   */
  resetDate() {
    this.selectedDate = this.currentDate;
    this.onSelectedDateChanged.next(this.selectedDate);
  }

  /**
   * Get The previous date based on the View type.
   */
  prevDate(): void {
    switch (this.selectedType) {
      case ViewTypes.day:
        this.selectedDate = moment(this.selectedDate).subtract(1, 'days').startOf('day').toDate();
        break;
      case ViewTypes.week:
        this.selectedDate = moment(this.selectedDate).subtract(1, 'weeks').startOf('week').toDate();
        break;
      case ViewTypes.month:
        // this.selectedDate = moment(this.selectedDate).subtract(1, 'months').startOf('month').toDate();
        this.previousMonth();
        break;
    }
    this.signalDateChange();
  }

  /**
   * Get The next date based on the View type.
   */
  nextDate(): void {
    switch (this.selectedType) {
      case ViewTypes.day:
        this.selectedDate = moment(this.selectedDate).add(1, 'days').toDate();
        break;
      case ViewTypes.week:
        this.selectedDate = moment(this.selectedDate).add(1, 'weeks').startOf('week').toDate();
        break;
      case ViewTypes.month:
        // this.selectedDate = moment(this.selectedDate).add(1, 'months').startOf('month').toDate();
        this.nextMonth();
        break;
    }
    this.signalDateChange();
  }

  /**
   * Get the next month
   */
  nextMonth(): void {
    this.selectedDate = moment(this.selectedDate).add(1, 'months').startOf('month').toDate();
    this.signalDateChange();
  }

  /**
   * Get the previous month
   */
  previousMonth(): void {
    this.selectedDate = moment(this.selectedDate).subtract(1, 'months').startOf('month').toDate();
    this.signalDateChange();
  }

  signalDateChange(): void {
    this.onSelectedDateChanged.next(this.selectedDate);
  }

  /**
   * Get the Current date.
   */
  getCurrentDate(): Date {
    return this.currentDate
  }

  /**
   * Get the Selected date Using Observable.
   */
  getDate(): void {
    this.onSelectedDateChanged.next(this.selectedDate);
  }

  /**
   * Get the Selected date.
   */
  getSelectedDate(): Date {
    return this.selectedDate;
  }

  /**
   * Call the Selected View Type Observable.
   * @returns View Type.
   */
  callViewType(): void {
    this.onSelectedTypeChanged.next(this.selectedType);
  }

  /**
   * Get the Selected View Type.
   * @returns View Type.
   */
  getViewType(): ViewTypes {
    return this.selectedType;
  }

  /**
   * Change the Calendar View Type to (Daily/Weekly/Monthly).
   * @param type View Type in which we have to Change.
   */
  changeViewType(type: ViewTypes): void {
    this.selectedType = type;
    this.onSelectedTypeChanged.next(type);
  }

  /**
   * Change the current Date.
   * @param toDate The date to which we have to change.
   */
  changeDate(toDate: Date): void {
    this.selectedDate = toDate;
    this.onSelectedDateChanged.next(toDate);
  }

  /**
   * Check if the selected date and current date is same without compating time.
   * @returns true if date is same elese false.
   */
  isSameDate(): boolean {
    const format = 'yyyy-mm-dd';
    return moment(this.currentDate).format(format) === moment(this.selectedDate).format(format);
  }

  /**
   * Observable When Date Changes.
   */
  get OnDateChanged(): Observable<Date> {
    return this.onSelectedDateChanged.asObservable();
  }

  /**
   * Observable When View Type (Daily/Weekly/Monthly) Changes.
   */
  get onViewTypeChanged(): Observable<ViewTypes> {
    return this.onSelectedTypeChanged.asObservable();
  }
}
