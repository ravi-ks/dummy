package com.platform.pod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.platform.pod.dto.TeamDetails;
import com.platform.pod.dto.TeamSummary;
import com.platform.pod.dto.UserDetails;
import com.platform.pod.entities.Teams;
import com.platform.pod.entities.Users;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.TeamsRepository;
import com.platform.pod.services.TeamsService;
import com.platform.pod.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TeamsServiceTests {

    @Mock
    TeamsRepository teamsRepository;

    @Mock
    UserService userService;

    @InjectMocks
    TeamsService teamsService;

    @Test
    public void testingInstances() {
        Assertions.assertNotNull(teamsService);
    }

    @Test
    public void testingGetTeamsManagedBy() {

//        Mocking
        List<Teams> teamsList = new ArrayList<>();
        teamsList.add(new Teams(1, "Team 1", new HashSet<>(), null));
        teamsList.add(new Teams(2, "Team 2", new HashSet<>(), null));
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(new Users());
        Mockito.when(teamsRepository.findByOrganizer(Mockito.any(Users.class))).thenReturn(teamsList);
        Mockito.when(teamsRepository.getMemberCountOfTeam(Mockito.anyInt())).thenReturn(100);

//        Actual Testing
        List<TeamSummary> teamSummaries = teamsService.getTeamsManagedBy("abc@test.com");

//        Checking size
        Assertions.assertEquals(2, teamSummaries.size());
//        Checking the memberCount
        Assertions.assertEquals(100, teamSummaries.get(0).getMembersCount());
//        Checking team Names
        Assertions.assertEquals("Team 1", teamSummaries.get(0).getName());
        Assertions.assertEquals("Team 2", teamSummaries.get(1).getName());
//        Checking team id's
        Assertions.assertEquals(1, teamSummaries.get(0).getTeamId());
        Assertions.assertEquals(2, teamSummaries.get(1).getTeamId());
    }

    @Test
    public void testingGetTeamDetails() {
//        Mocking
        List<Teams> teamsList = new ArrayList<>();
        teamsList.add(new Teams(1, "Team 1", new HashSet<>(), null));
        teamsList.add(new Teams(2, "Team 2", new HashSet<>(), null));
        Mockito.when(userService.getUserById(Mockito.anyInt())).thenReturn(new Users());
        Mockito.when(teamsRepository.findByOrganizer(Mockito.any(Users.class))).thenReturn(teamsList);

        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(new UserDetails(1, "one@email.com", "one"));
        userDetailsList.add(new UserDetails(2, "two@email.com", "two"));
        userDetailsList.add(new UserDetails(3, "three@email.com", "three"));
        Mockito.when(teamsRepository.getDetailsOfMembersOf(Mockito.anyInt())).thenReturn(userDetailsList);

//        Actual Testing
        TeamDetails teamDetails = teamsService.getTeamDetails("abc@test.com", 1);

//        Checking memberCount in Team
        Assertions.assertEquals(3, teamDetails.getMembers().size());

//        Checking userId's
        Assertions.assertEquals(1, teamDetails.getMembers().get(0).getUserId());
        Assertions.assertEquals(2, teamDetails.getMembers().get(1).getUserId());
        Assertions.assertEquals(3, teamDetails.getMembers().get(2).getUserId());

//        Checking emails
        Assertions.assertEquals("one@email.com", teamDetails.getMembers().get(0).getEmail());
        Assertions.assertEquals("two@email.com", teamDetails.getMembers().get(1).getEmail());
        Assertions.assertEquals("three@email.com", teamDetails.getMembers().get(2).getEmail());

//        TestingException - thrown when the team id specified is not managed by the user
        Assertions.assertThrows(ApiException.class, () -> {
            TeamDetails teamDetails1 = teamsService.getTeamDetails("abc@test.com", 3);
        });
    }
}
