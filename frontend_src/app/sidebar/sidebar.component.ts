import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ModalComponent } from '../task-modal/modal/modal.component';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
  }

  addTask() {
    let dialogRef = this.dialog.open(ModalComponent, {
      data: { taskId: -1}
    });
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog Result : ${result}`);
    })
  }
}
