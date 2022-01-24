import { Component, Input, OnInit, Output } from '@angular/core';
import * as moment from 'moment';
import { EventEmitter } from '@angular/core';
import { TaskDetail } from '../common/intefaces/TaskResponse';


interface ClassType {
  [key: string]: boolean;
}

@Component({
  selector: 'app-task-button',
  templateUrl: './task-button.component.html',
  styleUrls: ['./task-button.component.scss']
})
export class TaskButtonComponent implements OnInit {
  readonly PIXELS_PER_HOUR = 50;

  @Input() task: TaskDetail | null = null;
  @Input() isWeekly = false;
  @Output() onClick = new EventEmitter<void>();
  startTime: string = "";
  endTime: string = "";
  height: number = 0;
  top: number = 0;

  constructor() { }

  ngOnInit(): void {
    if (this.task == null) return;
    const timeDuration = moment
      .duration(
        moment('2021-01-01T' + this.task.endTime)
          .diff(moment('2021-01-01T' + this.task.startTime))
      )
      .asHours();
    const startDuration = moment
      .duration(
        moment('2021-01-01T' + this.task.startTime)
          .diff(moment('2021-01-01T00:00:00'))
      )
      .asHours();
    this.height = timeDuration * this.PIXELS_PER_HOUR;
    this.top = startDuration * this.PIXELS_PER_HOUR;
    this.startTime = moment('2021-08-01T' + this.task.startTime).format('h:mma');
    this.endTime = moment('2021-08-01T' + this.task.endTime).format('h:mma');
  }

  get classes(): ClassType {
    const toReturn: ClassType = {
      row: this.height <= 40,
      week: this.isWeekly
    };
    if (this.task !== null) {
      toReturn[this.task.priority] = true;
    }
    return toReturn;
  }
}
