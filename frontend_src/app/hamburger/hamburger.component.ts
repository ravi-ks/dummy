import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { TeamSummary } from '../common/intefaces/TeamResponse';
import { SidebarService } from '../services/sidebar/sidebar.service';
import { TeamService } from '../services/team/team.service';

@Component({
  selector: 'app-hamburger',
  templateUrl: './hamburger.component.html',
  styleUrls: ['./hamburger.component.scss'],
  styles: [`
    ::ng-deep .specific-class > .mat-expansion-indicator::after {
      color: white;
    }
  `]
})

export class HamburgerComponent implements OnInit, OnDestroy {
  public isMenuOpen: boolean = false;
  selectedValue: string = '';
  selected: number = 1; //initially home will be selected in the hamburger menu
  selectedTeamIndex: number = -1;

  teams: TeamSummary[] = [];

  constructor(
    private sidebarService: SidebarService,
    private router: Router,
    private teamService: TeamService
  ) { }

  subscription: Subscription[] = [];

  highlightBackground(url: string) {
    if (url === "/") {
      this.selected = 1;
    } else if (url.indexOf("/calendar") > -1) {
      this.selected = 2;
    } else if (url.indexOf("/calendar") > -1) {
      this.selected = 4;
    }
  }

  ngOnInit(): void {

    this.subscription.push(this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        this.highlightBackground(val.url);
      }
    }));

    this.subscription.push(this.sidebarService
      .OnStatusChanged
      .subscribe((isOpen: boolean) => {
        this.isMenuOpen = isOpen;
      }));

    this.subscription.push(this.teamService
      .onTeamUpdated
      .subscribe(teams => this.teams = teams));
    this.teamService.getTeamsList();

    // this.teamsService.getTeamsList().subscribe((teamsList) => this.teams = teamsList);

    //refresh teams list after a team is deleted.
    // this.teamsService.onTeamUpdated.subscribe((teamsSummary) => {
    //   console.log("Teams List Updated: " + teamsSummary);
    //   this.teams = teamsSummary;
    // });

    this.subscription.push(this.teamService.onTriggerSideBarChange().subscribe((triggerBoolean) => {
      this.selected = 1; //highlight home option of hamburger when opened
      if (this.isMenuOpen)
        this.triggerSideBarChange();
    }));

    //components can now refresh once the route has changed - specifying which team to display on click from hamburger's teams list
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;

    setTimeout(() => this.highlightBackground(this.router.url), 100);
  }

  public onSidenavClick(): void {
  }

  //nextStep to carry after an item is selected in hamburger menu.
  //highlighted index is used to identify the item to highlight.
  public nextStep(highlightIndex: number): void {
    this.selected = highlightIndex;
    this.selectedTeamIndex = -1;
  }

  public triggerSideBarChange() {
    this.sidebarService.changeStatus(!this.isMenuOpen);
  }

  public highlightTeam(index: number) {
    this.selectedTeamIndex = index;
    this.selected = 999; //as soon as a team is selected, to remove highlight over the "Teams" item AND to keep the expand-pane still open - this is used.
  }
  ngOnDestroy() {
    this.subscription.forEach((sub) => {
      sub.unsubscribe();
    })
  }
}
