import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from '../environments/environment';
import { CalendarComponent } from './components/calendar/calendar.component';
import { OAuthModule } from 'angular-oauth2-oidc';
import { SidebarCalendarComponent } from './sidebar-calendar/sidebar-calendar.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { HeaderComponent } from './header/header.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { TaskComponent } from './components/task/task.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { SidebarComponent } from './sidebar/sidebar.component';
import { HamburgerComponent } from './hamburger/hamburger.component';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';

import { PopupContentComponent } from './popup-content/popup-content.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MeetingComponent } from './components/meeting/meeting.component';
import { EventListItemComponent } from './components/meeting/event-list-item/event-list-item.component';
import { DashboardComponent } from './dashboard/dashboard.component';

import { IvyCarouselModule } from 'angular-responsive-carousel';
import { ModalComponent } from './task-modal/modal/modal.component';
import { EventComponent } from './task-modal/event/event.component';
import { TodoComponent } from './task-modal/todo/todo.component';
import { TaskListItemComponent } from './components/calendar/task-list-item/task-list-item.component';
import { TeamsDashboardComponent } from './teams-dashboard/teams-dashboard.component';

import { CreateTeamPopupComponent } from './create-team-popup/create-team-popup.component';
import { MatButtonModule } from '@angular/material/button';;
import { TaskButtonComponent } from './task-button/task-button.component';
import { NoTaskCardComponent } from './no-task-card/no-task-card.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { DragDropModule } from '@angular/cdk/drag-drop';
import { ErrorInterceptor } from './error-interceptor';
import { ErrorComponent } from './error/error.component';
import { ConfirmDeleteComponent } from './confirm-delete/confirm-delete.component';
import { DEFAULT_TIMEOUT, TimeoutInterceptor } from './common/TimeoutInterceptor';
@NgModule({
  declarations: [
    AppComponent,
    CalendarComponent,
    HeaderComponent,
    TaskComponent,
    SidebarCalendarComponent,
    HeaderComponent,
    SidebarComponent,
    HamburgerComponent,
    MeetingComponent,
    EventListItemComponent,
    DashboardComponent,
    ModalComponent,
    EventComponent,
    TodoComponent,
    TeamsDashboardComponent,
    PopupContentComponent,
    CreateTeamPopupComponent,
    PopupContentComponent,
    MeetingComponent,
    EventListItemComponent,
    DashboardComponent,
    TaskListItemComponent,
    TaskButtonComponent,
    NoTaskCardComponent,
    ErrorComponent,
    ConfirmDeleteComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      // Register the ServiceWorker as soon as the app is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    }),
    HttpClientModule,
    OAuthModule.forRoot({
      resourceServer: {
        allowedUrls: [environment.apiURL],
        sendAccessToken: true
      }
    }),
    MatFormFieldModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    FormsModule,
    MatSelectModule,
    MatMenuModule,
    MatCardModule,
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatExpansionModule,
    HttpClientModule,
    IvyCarouselModule,
    MatDialogModule,
    ReactiveFormsModule,
    DragDropModule,
    MatTooltipModule,
    HttpClientModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatSnackBarModule
  ],
  providers: [
    [
      {
        provide: HTTP_INTERCEPTORS,
        useClass: TimeoutInterceptor,
        multi: true
      }
    ],
    [
      {
        provide: DEFAULT_TIMEOUT,
        useValue: 1000000
      }
    ],
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
