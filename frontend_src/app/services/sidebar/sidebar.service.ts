import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {

  constructor() { }

  /**
   * Observable for status of sidebar.
   * true, if the sideBar is open else false.
   */
  onStatusChange: Subject<boolean> = new Subject<boolean>();

  /**
   * Function to signal status change.
   */
  changeStatus(status: boolean): void{
    this.onStatusChange.next(status);
  }


  get OnStatusChanged(): Observable<boolean> {
    return this.onStatusChange.asObservable();
  }
}
