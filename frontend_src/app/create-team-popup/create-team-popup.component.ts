import { Component, Input, OnDestroy, OnInit, Optional, ViewChild } from '@angular/core';
import { CopyTeamBody, CreateTeamBody, UpdateMemberBody, UpdateTeamNameBody } from '../common/intefaces/TeamBody';

import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSelectionList } from '@angular/material/list';
import { TeamDetails, TeamSummary } from '../common/intefaces/TeamResponse';
import { TeamService } from '../services/team/team.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogRef, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSelect } from '@angular/material/select';
import { Subscription } from 'rxjs';
import { ConfirmDeleteComponent } from '../confirm-delete/confirm-delete.component';

@Component({
  selector: 'app-create-team-popup',
  templateUrl: './create-team-popup.component.html',
  styleUrls: ['./create-team-popup.component.scss']
})


export class CreateTeamPopupComponent implements OnInit,OnDestroy {

  @Input() teamId: number | null = null;
  updatePopUp: boolean = false;
  members: string[] = [];
  memberEmail: string = "";
  teamName: string = "";
  invalidTeamName: boolean = true;
  form: FormGroup = new FormGroup({
    'team_name': new FormControl('', [Validators.required]),
    'member_email': new FormControl('', [Validators.email, this.existingMember.bind(this)])
  });

  teams: CopyTeamBody[] = [];
  //deleted members list - use only for updateTeam
  removedMembers: string[] = [];
  //added members list - use only for updateTeam
  addedMembers: string[] = [];

  previousTeamName: string = ''; //stores prev team name

  @ViewChild("teamMembers") teamMember: MatSelectionList | null = null;
  @ViewChild("selectCopyTeam") selectCopyTeam: MatSelect | null = null;

  constructor(
    private teamService: TeamService,
    private _snackBar: MatSnackBar,
    private confirmDialog: MatDialog,
    @Optional() private dialogRef: MatDialogRef<CreateTeamPopupComponent> | null,
    @Optional() private router: Router | null,
  ) { }

  subscription :Subscription[]=[];
  ngOnInit(): void {
    if (this.teamId !== null) {
      this.updatePopUp = true;
      let teamSubscription=this.teamService
        .getTeamDetails(this.teamId)
        .subscribe((team: TeamDetails) => {
          this.members = team.members.map(member => member.email);
          this.form.controls['team_name'].setValue(team.teamName);
          this.previousTeamName = team.teamName;
        })
        this.subscription.push(teamSubscription);
    }

    let teamSubscription=this.teamService.onTeamUpdated
      .subscribe((teamSummaryList: TeamSummary[]) => {
        for (let teamSummary of teamSummaryList) {
          this.teams.push({ teamId: teamSummary.teamId, teamName: teamSummary.name });
        }
      });
    this.subscription.push(teamSubscription);

    this.teamService.getTeamsList();
    if (this.router !== null) {
      this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    }
  }

  getInitials(user: String): String {
    return user.slice(0, 2).toUpperCase();
  }

  // Not needed as used [mat-dialog-close] on cancel button.
  // cancelTeamCreation(){
  //   this.dialogRef.close();
  // }

  createTeam() {
    let team: CreateTeamBody = {
      teamName: this.form.get('team_name')?.value,
      userEmails: this.members
    };
    let teamSubscription=this.teamService
      .createTeam(team)
      .subscribe(teamId => {
        this.dialogRef?.close();
        this._snackBar.open("Team created", "Done");
        if (this.router !== null) {
          this.router.navigate(['teams', teamId]);
        }
      },
        err => console.error(err));
    this.subscription.push(teamSubscription);

  }

  addMember() {
    if (this.form.get('member_email')?.valid && this.form.get('member_email')?.value !== "" && this.form.get('member_email')?.value !== null) {
      this.members.push(this.form.get('member_email')?.value);
      if (this.removedMembers.indexOf(this.form.get('member_email')?.value) > -1) {
        this.removedMembers.splice(this.removedMembers.indexOf(this.form.get('member_email')?.value), 1);
      }
      else {
        this.addedMembers.push(this.form.get('member_email')?.value);
      }
      this.form.controls['member_email'].setValue("");
    }
  }

  existingMember(control: FormControl): { [s: string]: boolean } | null {
    if (this.members.includes(control.value)) {
      return { 'existingMember': true };
    }

    return null;
  }

  removeMember(userEmail: string) {
    //if newly added member is deleted, just remove it from added-members and members list
    if (this.addedMembers.indexOf(userEmail) > -1) {
      const foundIndex = this.addedMembers.indexOf(userEmail, 0);
      if (foundIndex > -1) {
        this.addedMembers.splice(foundIndex, 1);
      }
    }
    //if previously persisted member is deleted, remove it from members list and add it to removedMembers list
    else {
      this.removedMembers.push(userEmail);
    }

    //removing member from members list - unconditionally
    const index = this.members.indexOf(userEmail, 0);
    this.members.splice(index, 1);
  }

  // Code below is for update popup
  updateTeam() {
    var isSnackBarShown = false; //if isSnackBarShown is true, dont show snack bar again.
    this.teamName = this.form.get('team_name')?.value,
    console.log("ff");
    console.log(this.removedMembers);

    //calling removeAMember service
    this.removedMembers.forEach(async (removedMember) => {
      let newRemovedMember: UpdateMemberBody = {
        email: removedMember,
        teamId: this.teamId as any
      };
      let teamSubscription=this.teamService.removeAmember(newRemovedMember).subscribe((response) => {
      if(!isSnackBarShown)
        isSnackBarShown = true;
        this._snackBar.open("Team Updated", "Done");
      }
      );
      this.subscription.push(teamSubscription);
    });

    //console.log("name");

    //update team name
    let updatedTeamName: UpdateTeamNameBody = {
      teamId: this.teamId as any,
      teamName: this.teamName
    };
    if(this.teamName !== this.previousTeamName )
      this.teamService.updateTeamName(updatedTeamName);

    this.teamService.isTeamNameUpdated.subscribe(_ => {
      if(_ && !isSnackBarShown){
        this._snackBar.open("Team Updated", "Done");
        isSnackBarShown = true;
      }});

    //console.log("add");

    //calling addAMember service
    this.addedMembers.forEach((addedMember) => {
      let newAddedMember: UpdateMemberBody = {
        email: addedMember,
        teamId: this.teamId as any
      };
      let teamSubscription=this.teamService.addAMember(newAddedMember).subscribe((response) => {
        if(!isSnackBarShown)
        isSnackBarShown = true;
        this._snackBar.open("Team Updated", "Done");
      });
      this.subscription.push(teamSubscription);
    });

    this.removedMembers = [];
    this.addedMembers = [];
  }

  confirmDelete(){
    let confirmDialogRef = this.confirmDialog.open(ConfirmDeleteComponent,{data : {message : 'Team'}});
    confirmDialogRef.afterClosed().subscribe(result => {
      if(result==="true"){
        this.deleteTeam();
      }
    })
  }

  deleteTeam() {
    this._snackBar.open("Team deleted", "Done");
    if (this.teamId != null) {
      this.teamService.deleteATeam(this.teamId);
      if (this.router !== null) {
        this.router.navigateByUrl("/");
      }
      //trigger hamburger to close, this'll also refresh teamslist
      this.teamService.induceTriggerSideBarChange();
    }
  }

  copyTeam() {
    if (this.selectCopyTeam?.value !== undefined) {
      let teamSubscription=this.teamService.getTeamDetails(this.selectCopyTeam?.value).subscribe((teamDetails: TeamDetails) => {
        for (let member of teamDetails.members) {
          if (!this.members.includes(member.email)) {
            this.members.push(member.email);
          }
        }
      },
        (error) => {
          console.log("Unable to fetch team information.");
        })
      this.subscription.push(teamSubscription);
    }
  }
  ngOnDestroy(){
    this.subscription.forEach((sub)=>{
      sub.unsubscribe();
    })
  }
}
