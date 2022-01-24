import { Component, Input, OnInit } from '@angular/core';
import * as moment from 'moment';
import Constants from 'src/app/common/Constants';
import { TaskDetail } from 'src/app/common/intefaces/TaskResponse';

@Component({
  selector: 'app-event-list-item',
  templateUrl: './event-list-item.component.html',
  styleUrls: ['./event-list-item.component.scss']
})
export class EventListItemComponent implements OnInit {
  @Input() task: TaskDetail | null = null;
  height: number = 0;
  top: number = 0;

  constructor() { }

  ngOnInit(): void {
    if (this.task == null) return;
    // const SOD = moment('2021-01-01T00:00:00');
    // console.log(SOD.toDate());

    // const EOD = moment('2021-01-01T23:59:59');
    // console.log(EOD.toDate());

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
    this.height = timeDuration * Constants.HEIGHT_PER_HOUR;
    this.top = startDuration * Constants.HEIGHT_PER_HOUR;
  }

}
