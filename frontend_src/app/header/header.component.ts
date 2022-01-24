import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { NavigationEnd, Router } from '@angular/router';
import { MatSidenav } from '@angular/material/sidenav';
import { ViewTypes } from '../common/intefaces/view-types';
import { CalendarService } from '../services/calendar/calendar.service';
import { SidebarService } from '../services/sidebar/sidebar.service';
import { MatDialog } from '@angular/material/dialog';
import { CreateTeamPopupComponent } from '../create-team-popup/create-team-popup.component';
import { OAuthService } from 'angular-oauth2-oidc';
import { UserService } from '../services/user/user.service';
import Utility from '../common/Utility';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  //ravi added
  sidebarOpen = false;

  @ViewChild("menu") trigger: MatMenuTrigger | null = null;

  viewTypeOptions: ViewTypes[] = [
    ViewTypes.day,
    ViewTypes.week,
    ViewTypes.month
  ];
  selectedOption: ViewTypes = ViewTypes.day;
  homeScreen: boolean = true;

  initials = "";
  day: number | null = null;
  month: String | null = null;
  year: number | null = null;
  showFullDate: Boolean = true;

  constructor(
    private calendarService: CalendarService,
    private router: Router,
    private sidebarService: SidebarService,
    private dialog: MatDialog,
    private authService: OAuthService,
    private userService: UserService
  ) {
  }

  subscription: Subscription[] = [];
  ngOnInit(): void {
    const userSubscription = this.userService
      .onLoggedIn
      .subscribe(user => this.initials = Utility.getUserInitials(user.name));

    const routerSubscription = this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        this.homeScreen = (val.url === "/");
      }
    });
    this.subscription.push(userSubscription);
    this.subscription.push(routerSubscription);

    const sidebarSubscription = this.sidebarService.OnStatusChanged.subscribe((isOpen: boolean) => {
      this.sidebarOpen = isOpen;
    })
    this.subscription.push(sidebarSubscription);
    /*ravi added
    this.hamburgerService.OnHamburgerChanged.subscribe((isOpen: boolean) => {
      this.isMenuOpen = isOpen;
    })
    */

    const calendarSubscription = this.calendarService.OnDateChanged.subscribe((date: Date) => {
      this.day = date.getDate();
      this.month = date.toLocaleString("en-us", { month: "long" });
      this.year = date.getFullYear();
    });
    this.subscription.push(calendarSubscription);
    this.calendarService.getDate();
    this.userService.getUser();

    setTimeout(() => this.homeScreen = (this.router.url === "/"), 100);
  }

  selectionChanged(newSelection: ViewTypes) {
    this.selectedOption = newSelection;
    this.calendarService.changeViewType(this.selectedOption);

    if (this.selectedOption === ViewTypes.day) {
      this.showFullDate = true;
    }
    else {
      this.showFullDate = false;
    }
  }

  onClickingAvatar() {
    this.trigger?.openMenu;
  }

  onClickingTeams() {
    const dialogRef = this.dialog.open(CreateTeamPopupComponent);
  }

  goToPrevious() {
    this.calendarService.prevDate();
  }

  goToNext() {
    this.calendarService.nextDate();
  }

  logOut() {
    this.authService.logOut();
  }

  openNavigation() {
    this.sidebarOpen = !this.sidebarOpen;
    this.sidebarService.changeStatus(this.sidebarOpen);
  }
  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }
}
