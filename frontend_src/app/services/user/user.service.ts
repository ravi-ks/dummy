import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { of, Subject } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { authConfig } from 'src/app/auth.config';
import { environment } from 'src/environments/environment';
import { Member } from '../../common/intefaces/TeamResponse';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = `${environment.apiURL}users`;
  private loggedIn: Subject<Member> = new Subject<Member>();
  private user: Member | null = null;

  constructor(
    private http: HttpClient,
    private oauthService: OAuthService
  ) { }

  getUserInfo() {
    this.http
      .get<Member>(`${this.apiUrl}/login`)
      // .pipe(timeout(9001), catchError(e => of(null)))
      .subscribe(user => {
        if (user === null) {
          return;
        }
        this.user = user;
        this.loggedIn.next(user);
      });
  }

  login() {
    this.oauthService.configure(authConfig);
    this.oauthService
      .loadDiscoveryDocumentAndLogin()
      .then(login => {
        this.getUserInfo();
      });
    this.oauthService.setupAutomaticSilentRefresh();
  }

  getUser() {
    if (this.user !== null) {
      this.loggedIn.next(this.user);
    }
  }

  get onLoggedIn() {
    return this.loggedIn.asObservable();
  }
}
