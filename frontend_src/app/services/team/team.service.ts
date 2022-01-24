import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, Subject } from 'rxjs';
import { CommonResponse } from 'src/app/common/intefaces/CommonResponse';
import { CopyTeamBody, CreateTeamBody, UpdateMemberBody, UpdateTeamNameBody } from 'src/app/common/intefaces/TeamBody';
import { TeamDetails, TeamSummary } from 'src/app/common/intefaces/TeamResponse';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  private readonly apiUrl = `${environment.apiURL}teams`;
  // TODO: Might Have to set Headers For POST APIs.
  private teams: TeamSummary[] = [];
  private onTeamListUpdated: Subject<TeamSummary[]> = new Subject<TeamSummary[]>();

  constructor(private http: HttpClient, private _snackbar: MatSnackBar) { }

  private triggerSideBarChange: Subject<boolean> = new Subject<boolean>();
  private deleteSubject: Subject<number> = new Subject<number>();

  isTeamNameUpdated: Subject<boolean> = new Subject<boolean>();//var to check if TeamName updated or not

  //trigger open and close of hamburger menu
  induceTriggerSideBarChange() {
    this.triggerSideBarChange.next(true);
  }

  onTriggerSideBarChange() {
    return this.triggerSideBarChange.asObservable();
  }

  getList() {
    this.onTeamListUpdated.next(this.teams);
  }

  /**
   * Creates a New Team.
   * @param team Team Information.
   */
  createTeam(team: CreateTeamBody): Observable<number> {
    const created: Subject<number> = new Subject<number>();
    this.http.post<TeamSummary>(`${this.apiUrl}/create`, team)
      .subscribe(team => {
        this.teams.unshift(team);
        this.onTeamListUpdated.next(this.teams);
        created.next(team.teamId);
      }, error => console.error(error));
    return created.asObservable();
  }

  /**
   * Create a New Team From Existing Team.
   * @param team Team Information.
   */
  copyTeam(team: CopyTeamBody) {
    this.http.post<TeamSummary>(`${this.apiUrl}/createFromAnotherTeam`, team)
      .subscribe(team => {
        this.teams.unshift(team);
        this.onTeamListUpdated.next(this.teams);
      }, error => console.error(error));
  }

  /**
   * Get List of Teams available for an Particular User.
   */
  getTeamsList() {
    this.http.get<TeamSummary[]>(`${this.apiUrl}/summary`)
      .subscribe(teams => {
        this.teams = teams;
        this.onTeamListUpdated.next(this.teams);
      }, error => console.error(error));
  }

  /**
   * Get the Information for a particular team.
   * @param teamID Team ID.
   */
  getTeamDetails(teamID: number): Observable<TeamDetails> {
    return this.http.get<TeamDetails>(`${this.apiUrl}/details/${teamID}`);
  }

  /**
   * Remove a member of Team.
   * @param member Team Member (user).
   */
  removeAmember(member: UpdateMemberBody) {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: member,
    };
    return this.http.delete<CommonResponse>(`${this.apiUrl}/removeMember`, options)
  }

  /**
   * Add a Member to The team.
   * @param member Team Member (user).
   */
  addAMember(member: UpdateMemberBody) {
    return this.http.put<CommonResponse>(`${this.apiUrl}/addMember`, member)
  }

  /**
   * Delete a team for the organizer.
   * @param teamID Team ID.
   */
  deleteATeam(teamID: number) {
    this.http.delete<CommonResponse>(`${this.apiUrl}/delete/${teamID}`)
      .subscribe(_ => {
        this.teams = this.teams.filter(team => team.teamId != teamID);
        this.onTeamListUpdated.next(this.teams);
        this.deleteSubject.next(teamID);
      });
  }

  /**
   * Update Team Name.
   * @param teamID Team ID.
   * @param teamName Team Name.
   */
  updateTeamName(updateTeamNameBody: UpdateTeamNameBody) {
    this.http.patch<CommonResponse>(`${this.apiUrl}/teamName`, updateTeamNameBody)
      .subscribe(_ => {
        this.teams = this.teams.map(team => {
          if (team.teamId == updateTeamNameBody.teamId) {
            team.name = updateTeamNameBody.teamName;
          }
          return team;
        });
        this.onTeamListUpdated.next(this.teams);
        this.isTeamNameUpdated.next(true);
      },
        error => {
          this.isTeamNameUpdated.next(false);
        }
      );
  }

  get onTeamUpdated(): Observable<TeamSummary[]> {
    return this.onTeamListUpdated.asObservable();
  }

  get onDeleteTeam(): Observable<number> {
    return this.deleteSubject.asObservable();
  }
}
