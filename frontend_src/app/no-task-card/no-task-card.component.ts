import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-no-task-card',
  templateUrl: './no-task-card.component.html',
  styleUrls: ['./no-task-card.component.scss']
})
export class NoTaskCardComponent {
  @Input() title = "No task for today.";
  @Output() onAddTask = new EventEmitter<void>();
}
