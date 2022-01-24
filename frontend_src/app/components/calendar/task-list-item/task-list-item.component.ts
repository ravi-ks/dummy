import { Component, Input, OnInit } from '@angular/core';
import * as moment from 'moment';
import { TaskDetail } from 'src/app/common/intefaces/TaskResponse';

@Component({
  selector: 'app-task-list-item',
  templateUrl: './task-list-item.component.html',
  styleUrls: ['./task-list-item.component.scss']
})
export class TaskListItemComponent implements OnInit {
  @Input() task: TaskDetail | null = null;
  constructor() { }

  ngOnInit(): void {
  }

  get startingTime(): string {
    if (this.task) {
      return moment('2021-08-01T' + this.task.startTime)
        .format('h:mma')
        .replace(':00', '');
    }
    return '';
  }

}
