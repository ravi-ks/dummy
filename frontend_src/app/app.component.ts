import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from './auth.config';

import { Component, OnInit } from '@angular/core';
import { ViewEncapsulation } from '@angular/core';
import { TimeService } from './services/time/time.service';
import { UserService } from './services/user/user.service';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class AppComponent implements OnInit {
  login: boolean = false;

  constructor(
    private timeService: TimeService,
    private userService: UserService
  ) { }

  ngOnInit() {
    this.timeService.startTimer();
    this.userService
      .onLoggedIn
      .subscribe(_ => this.login = true);
    this.userService.login();
  }

}
