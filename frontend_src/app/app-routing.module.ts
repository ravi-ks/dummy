import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CalendarComponent } from './components/calendar/calendar.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { EventComponent } from './task-modal/event/event.component';
import { TodoComponent } from './task-modal/todo/todo.component';
import { TeamsDashboardComponent } from './teams-dashboard/teams-dashboard.component';

const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'todo' ,component : TodoComponent},
  { path: 'event' ,component : EventComponent},
  { path: 'calendar', component: CalendarComponent },
  { path: 'teams/:teamId', component: TeamsDashboardComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
