import { Component, Input, OnDestroy } from '@angular/core';
import * as moment from 'moment';
import { Subscription } from 'rxjs';
import { TaskDetail } from 'src/app/common/intefaces/TaskResponse';
import { TasksService } from 'src/app/services/tasks/tasks.service';

@Component({
  selector: 'app-task',
  templateUrl: './task.component.html',
  styleUrls: ['./task.component.scss']
})
export class TaskComponent implements OnDestroy {
  @Input() check = true;
  @Input() task: TaskDetail | null = null;

  constructor(private taskService: TasksService) {
  }

  checkTask(event: MouseEvent) {
    event.stopPropagation();
    if (this.task === null) {
      return;
    }
    this.taskSubscription = this.taskService
      .checkTask(this.task.taskId)
      .subscribe(_ => {
        if (this.task !== null) {
          this.task.checked = !this.task.checked;
        }
      });
  }

  taskSubscription: Subscription | null = null;
  ngOnDestroy() {
    if (this.taskSubscription !== null) {
      this.taskSubscription.unsubscribe();
    }
  }
  get startTime(): string {
    if (this.task) {
      return moment('2021-08-01T' + this.task.startTime).format('hh:mm');
    }
    return '';
  }

  get endTime(): string {
    if (this.task) {
      return moment('2021-08-01T' + this.task.endTime).format('hh:mm');
    }
    return '';
  }

}
