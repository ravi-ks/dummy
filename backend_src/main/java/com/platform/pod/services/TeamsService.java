package com.platform.pod.services;

import com.platform.pod.entities.Teams;
import com.platform.pod.entities.Users;
import com.platform.pod.interfaces.TeamsInterface;
import com.platform.pod.repositories.TeamsRepository;
import com.platform.pod.repositories.UserRepo;

import java.util.ArrayList;
import java.util.List;

import com.platform.pod.dto.TeamDetails;
import com.platform.pod.dto.TeamSummary;
import com.platform.pod.dto.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.platform.pod.exceptions.ApiException;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class TeamsService implements TeamsInterface {
    @Autowired
    TeamsRepository teamsRepository;
    @Autowired
    UserService userService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    TasksService tasksService;

    /**
     * Service utility to create a team
     * @author Ravikumar Shantharaju
     * @param userEmails email id's of users in this team
     * @param teamName name of the team
     * @param organizerEmail email id of the user who created/organized this team
     * @throws ApiException if an attempt to create team with no members is made
     * @return team id of the newly created team
     */
    public int createTeam(List<String> userEmails, String teamName, String organizerEmail) {

        if (userEmails.size() == 0) {
            throw new ApiException("Team Without Members is Not Possible.");
        }

        Users organizer = userService.getUserByEmail(organizerEmail);

        //check for unique team name for a given organizer
        List<Teams> organisedTeams = teamsRepository.findByOrganizer(organizer);
        for (Teams team : organisedTeams) {
            if (team.getTeam_name().equals(teamName)) {
                throw new ApiException("Team name already exists, team creation aborted");
            }
        }

        //create team
        Teams team = new Teams();
        team.setTeam_name(teamName);
        team.setOrganizer(organizer);
        //assign users to team
        for (String email : userEmails) {
            if (email.equals(organizerEmail)) {
                throw new ApiException("Organizer Can't be a Team Member");
            }
            team.users_and_teams.add(
                    userService.createUserFromEmail(email)
            );
        }
        return teamsRepository.save(team).getTeam_id();
        //persist team and return the created team's id
    }

//    public int createTeamFromAnotherTeam(int teamId, String teamName, int organizerid) {
//        try {
//            Teams team = teamsRepository.getById(teamId);
//            List<String> userEmails = new ArrayList<>();
//            for (Users user : team.getUsers_and_teams()) {
//                userEmails.add(user.getEmail());
//            }
//            return createTeam(userEmails, teamName, organizerid);
//        } catch (EntityNotFoundException e) {
//            throw new ApiException(HttpStatus.NOT_FOUND, "Invalid team id, no such team exist.");
//        }
//    }

    /**
     * Service utility to delete a team
     * @author Ravikumar Shantharaju
     * @param teamId Id of the team to be deleted
     * @param email email of the user deleting this team
     * @throws ApiException if team with given id is not present (NOT_FOUND), and if the user deleting the team
     * is not the one who created/organized it (BAD_REQUEST)
     * @return "Successfully Deleted Team!" if no exception is thrown
     */
    public String deleteTeam(int teamId, String email) {
        Optional<Teams> teamsOptional = teamsRepository.findById(teamId);
        if (teamsOptional.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Team Not Found");
        }

        if (!teamsOptional.get().organizer.getEmail().equals(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only team organizer can delete the team");
        }

        //delete tasks(and repetitions) assigned to teams first, else causes deletion anomaly
        tasksService.deleteTaskByTeamId(teamId);
        teamsRepository.deleteById(teamId);
        return "Successfully Deleted Team!";
    }

    /**
     * Service utility to get the list of summary of teams managed by a user.
     * @param email email id of the user whose managed teams are to be fetched
     * @return list of teams, summarized (i.e TeamSummary); organized/created/managed by passed user email
     */
    public List<TeamSummary> getTeamsManagedBy(String email) {
        List<TeamSummary> teamSummary = new ArrayList<>();

        // TODO: Fetch from Single Query.
        List<Teams> teamsManagedByUser = getTeamsOf(email);
        for (var team : teamsManagedByUser) {
            TeamSummary teamSum = new TeamSummary(
                    team.getTeam_id(),
                    team.getTeam_name(),
                    teamsRepository.getMemberCountOfTeam(team.getTeam_id()
                    )
            );
            teamSummary.add(teamSum);
        }

        return teamSummary;
    }

    /**
     * Service utility to  get the list of teams managed by a user.
     * Differs from getTeamsManagedBy function, by type of data returned.
     * @param email email id of the user whose managed teams are to be fetched
     * @return list of teams (i.e of type Teams) organized/created/managed by passed user email
     */
    private List<Teams> getTeamsOf(String email) {
        return teamsRepository.findByOrganizer(userService.getUserByEmail(email));
    }


    /**
     * Service utility to  get the details of a team managed by a user.
     * @param email email id of the user whose managed team, identified by teamId param is to be fetched
     * @param teamId id of the team, whose details are to be fetched
     * @throws ApiException if team with requested teamId is not found or if the requesting user isn't the organizer of the team
     * @return details of the team requested
     */
    //    Function to get the team details of the team managed by the user.
    public TeamDetails getTeamDetails(String email, int teamId) {
        Optional<Teams> teamsOptional = teamsRepository.findById(teamId);
        if (teamsOptional.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Invalid Team");
        }
        if (!teamsRepository.validateOrganizer(teamId, email)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "You are not the organizer of the team.");
        }
        List<UserDetails> members = teamsRepository.getDetailsOfMembersOf(teamId);
        return new TeamDetails(
                teamId,
                teamsOptional.get().getTeam_name(),
                members
        );
    }

    /**
     * Service utility to add a member to the team.
     * @param teamId id of the team, where member has to be added
     * @param memberEmail email of the member being added to the team
     * @param email team organizer email id, i.e email id of the user who's adding member to the team.
     * @return "Member Added To Team" if no exception is incurred
     */
    public String addMemberToTeam(int teamId, String memberEmail, String email) {
        Optional<Teams> optionalTeam = teamsRepository.findById(teamId);
        if (optionalTeam.isEmpty()) {
            throw new ApiException("Team Not Found");
        }
        Teams team = optionalTeam.get();
        Users member = userService.createUserFromEmail(memberEmail);
        if (!team.getOrganizer().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Member Not Registered as Organizer");
        }

        if (team.getUsers_and_teams().contains(member)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Member  Already in Team");
        }
        team.users_and_teams.add(member);
        teamsRepository.save(team);
        return "Member Added To Team";
    }


    /**
     * Service utility to remove a member from the team.
     * @param teamId id of the team, where member is being removed from.
     * @param memberEmail email of the member being removed from the team
     * @param email team organizer email id, i.e email id of the user who's removing member from the team.
     * @throws ApiException if team with given teamId is not found, or member being removed isn't part of the
     * team at all, or if the user trying to remove member from the team is not a organizer
     * @return "Member Removed From Team" if no exception is incurred
     */
    public String removeMemberFromTeam(int teamId, String memberEmail, String email) {
        Optional<Teams> optionalTeam = teamsRepository.findById(teamId);
        if (optionalTeam.isEmpty()) {
            throw new ApiException("Team Not Found");
        }
        Teams team = optionalTeam.get();
        Users member = userRepo.findByEmail(memberEmail);
        if (member == null) {
            throw new ApiException("Member Not Found");
        }
        if (!team.getOrganizer().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Member Not Registered as Organizer");
        }
        if (!team.getUsers_and_teams().contains(member)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Member Not present in Team");
        }
        team.users_and_teams.remove(member);
        teamsRepository.save(team);
        return "Member Removed From Team";
    }

    /**
     * Accessory utility to get team by teamId
     * @param teamId id of the team to be fetched
     * @return team (of type Teams) with requested teamId
     */
    //accessory methods
    public Teams getTeam(int teamId) {
        return teamsRepository.getById(teamId);
    }

    /**
     * Service utility to update team name
     * @param teamId id of the team whose name is to be updated
     * @param newTeamName new name of the team
     * @throws ApiException if team with given id is not found
     * @return "Team Updated" is no error incurred
     */
    public String updateTeamName(int teamId, String newTeamName) {
        Optional<Teams> optionalTeam = teamsRepository.findById(teamId);
        if (optionalTeam.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Team Not Found");
        }
        Teams team = optionalTeam.get();
        team.setTeam_name(newTeamName);
        teamsRepository.save(team);
        return "Team Updated";
    }
}
