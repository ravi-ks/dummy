package com.platform.pod.controllers;

import com.platform.pod.common.Constants;
import com.platform.pod.common.ResponseMessage;
import com.platform.pod.dto.*;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.TeamsRepository;
import com.platform.pod.services.TeamsService;
import com.platform.pod.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/teams")
public class TeamsController {
    @Autowired
    TeamsService teamsService;

    @Autowired
    TeamsRepository teamsRepository;

    @Autowired
    UserService userService;

    /**
     * This API creates a team, by further invocating TeamsService's createTeam method
     * @author Ravikumar Shantharaju
     * @param email Email of logged-in user under scope
     * @param createTeamWrapper Wrapper for incoming JSON request body (deserialized data)
     * @return TeamSummary of the created team
     */

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public TeamSummary createTeam(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @RequestBody CreateTeamWrapper createTeamWrapper
    ) {
        int team_id = teamsService.createTeam(createTeamWrapper.getUserEmails(), createTeamWrapper.getTeamName(), email);
        return new TeamSummary(
                team_id,
                createTeamWrapper.getTeamName(),
                createTeamWrapper.getUserEmails().size()
        );
    }

// As Design got updated we have to remove this API.
//    @RequestMapping(value = "/createFromAnotherTeam", method = RequestMethod.POST)
//    public TeamSummary createTeamFromAnotherTeam(
//            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
//            @RequestBody CreateFromAnotherTeamWrapper createFromAnotherTeamWrapper
//    ) {
//        int organizerId = 1;
//        int createdTeamId = teamsService.createTeamFromAnotherTeam(
//                createFromAnotherTeamWrapper.getTeamId(),
//                createFromAnotherTeamWrapper.getTeamName(),
//                organizerId
//        );
//        return new TeamSummary(
//                createdTeamId,
//                teamsService.getTeam(createdTeamId).getTeam_name(),
//                teamsRepository.getMemberCountOfTeam(createdTeamId)
//        );
//    }

    /**
     * This API deletes a team, by further invocating TeamsService's deleteTeam method
     * @author Ravikumar Shantharaju
     * @param email Email of logged-in user under scope
     * @param teamId Id of the team to be deleted
     * @return "Successfully Deleted Team!" if no exception is thrown
     */
    @RequestMapping(value = "/delete/{teamId}", method = RequestMethod.DELETE)
    public ResponseMessage deleteTeam(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("teamId") int teamId
    ) {
        return new ResponseMessage(
                teamsService.deleteTeam(teamId, email)
        );
    }

    /**
     * API to get all the teams managed/created by logged-in user, by further invocating TeamsService's getTeamsManagedBy method
     * @param email Email of logged-in user under scope
     * @return List of Teams, summarized.
     */

    @GetMapping(value = "/summary")
    public List<TeamSummary> getTeams(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email
    ) {
        // If the user is not an organiser, this will return an empty list and if that's the case handle the scenario on the front-end by displaying an appropriate message.
        return teamsService.getTeamsManagedBy(email);
    }

    /**
     * API to get a particular teams details (of type TeamDetails), by passing teamId as a param.
     * This is done by further invocating TeamsService's getTeamDetails method
     * @param email Email of logged-in user under scope
     * @param teamId Id of the team, whose details has to be fetched
     * @return TeamDetails of the team requested
     */

    @GetMapping(value = "/details/{team_id}")
    public TeamDetails getTeamDetails(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @PathVariable("team_id") int teamId
    ) {
        return teamsService.getTeamDetails(email, teamId);
    }

    /**
     * API to add a new member to the team, by further invocating TeamsService's addMemberToTeam method
     * @param email Email of logged-in user under scope
     * @param updateTeamWrapper Wrapper data type to store passed JSON request body.
     * @return "Member Added To Team" if no exception is incurred
     */
    @RequestMapping(value = "/addMember", method = RequestMethod.PUT)
    public ResponseMessage addMemberToTeam(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @RequestBody UpdateTeamWrapper updateTeamWrapper
    ) {
        int teamId = updateTeamWrapper.getTeamId();
        String memberEmail = updateTeamWrapper.getEmail();
        String result = teamsService.addMemberToTeam(teamId, memberEmail, email);
        return new ResponseMessage(result);
    }

    /**
     * API to remove a member from the team, by further invocating TeamsService's removeMemberFromTeam method
     * @param email Email of logged-in user under scope
     * @param updateTeamWrapper Wrapper data type to store passed JSON request body.
     * @return "Member Removed From Team" if no exception is incurred
     */
    @RequestMapping(value = "/removeMember", method = RequestMethod.DELETE)
    public ResponseMessage removeMemberFromTeam(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @RequestBody UpdateTeamWrapper updateTeamWrapper
    ) {
        int teamId = updateTeamWrapper.getTeamId();
        String memberEmail = updateTeamWrapper.getEmail();
        String result = teamsService.removeMemberFromTeam(teamId, memberEmail, email);
        return new ResponseMessage(result);
    }

    /**
     * API to update team name, by further invocating TeamsService's updateTeamName method
     * @param email Email of logged-in user under scope
     * @param updateTeamName Wrapper data type to store passed JSON request body.
     * @throws ApiException when specified new team name already exists (i.e duplicate team name
     * within the teams created by logged-in user )
     * @return "Team Updated" is no error incurred
     */
    @RequestMapping(value = "/teamName", method = RequestMethod.PATCH)
    public ResponseMessage updateTeamName(
            @AuthenticationPrincipal(expression = Constants.EMAIL_EXPR) String email,
            @RequestBody UpdateTeamName updateTeamName
    ) {
        //check for duplicate team name per organizer
        for(var teamSummary : this.teamsService.getTeamsManagedBy(email)) {
            if(teamSummary.getName().equals(updateTeamName.getTeamName())){
                throw new ApiException(HttpStatus.BAD_REQUEST, "Team name already taken");
            }
        }
        int teamId = updateTeamName.getTeamId();
        String newTaskName = updateTeamName.getTeamName();
        String result = teamsService.updateTeamName(teamId, newTaskName);
        return new ResponseMessage(result);
    }
}
