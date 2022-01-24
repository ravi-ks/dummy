package com.platform.pod;

import com.platform.pod.entities.Teams;
import com.platform.pod.entities.Users;
import com.platform.pod.exceptions.ApiException;
import com.platform.pod.repositories.TeamsRepository;
import com.platform.pod.repositories.UserRepo;
import com.platform.pod.services.TeamsService;
import com.platform.pod.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class TeamsServiceTest {
    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private TeamsService teamsService;

    @Mock
    private UserService userService;

    @Test
    public void testAddMemberToTeamWithNoException() {
        Teams expectedTeam = getExpectedTeam();
        Users expectedUser = getExpectedUser();
        int teamId = expectedTeam.getTeam_id();
        String organizerId = expectedTeam.getOrganizer().getEmail();
        String memberEmail = expectedUser.getEmail();
        when(userService.createUserFromEmail(memberEmail)).thenReturn(expectedUser);
        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));
        teamsService.addMemberToTeam(teamId, memberEmail, organizerId);
        verify(teamsRepository, times(1)).findById(teamId);
    }

    @Test
    public void testAddMemberToTeamWithException_NotOrganizer() {
        Teams expectedTeam = getExpectedTeam();
        Users expectedUser = getExpectedUser();
        int teamId = expectedTeam.getTeam_id();
        String organizerId = expectedUser.getEmail();//wrong organizer id to test not organizer exception
        String memberEmail = expectedUser.getEmail();
        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));
        assertThrows(ApiException.class,
                () -> teamsService.addMemberToTeam(teamId, memberEmail, organizerId));
        verify(teamsRepository, times(1)).findById(teamId);
    }


    @Test
    public void testRemoveMemberFromTeamWithNoException() {
        Teams expectedTeam = getExpectedTeam();
        Users expectedUser = expectedTeam.getOrganizer();
        int teamId = expectedTeam.getTeam_id();
        String organizerId = expectedTeam.getOrganizer().getEmail();
        String memberEmail = expectedUser.getEmail();
        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));
        when(userRepo.findByEmail(memberEmail)).thenReturn(expectedUser);
        teamsService.removeMemberFromTeam(teamId, memberEmail, organizerId);
        verify(teamsRepository, times(1)).findById(teamId);
        verify(userRepo, times(1)).findByEmail(memberEmail);
    }


    @Test
    public void testRemoveMemberFromTeamWithException_NotOrganizer() {
        Teams expectedTeam = getExpectedTeam();
        Users expectedUser = getExpectedUser();
        int teamId = expectedTeam.getTeam_id();
        String organizerId = expectedUser.getEmail();//wrong organizer id to test not organizer exception
        String memberEmail = expectedUser.getEmail();
        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));
        when(userRepo.findByEmail(memberEmail)).thenReturn(expectedUser);
        assertThrows(ApiException.class,
                () -> teamsService.removeMemberFromTeam(teamId, memberEmail, organizerId));
        verify(teamsRepository, times(1)).findById(teamId);
        verify(userRepo, times(1)).findByEmail(memberEmail);
    }

    @Test
    public void testUpdateTeamName() {
        Teams expectedTeam = getExpectedTeam();
        when(teamsRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(expectedTeam));
        teamsService.updateTeamName(1, "Dominators");
        verify(teamsRepository, times(1)).findById(Mockito.anyInt());
        verify(teamsRepository, times(1)).save(expectedTeam);
    }


    public Users getExpectedUser() {
        Users user = new Users(4, "hash@deloitte.com", null);
        return user;
    }

    public Users getOrganizer() {
        Users organizer = new Users(3, "ma@deloitte.com", null);
        return organizer;
    }

    public Teams getExpectedTeam() {
        Users organizer = getOrganizer();
        Users user2 = new Users(2, "sa@deloitt.com", null);
        List<Users> list = Arrays.asList(organizer, user2);
        Set<Users> set = new HashSet<>(list);
        Teams team = new Teams(1, "Superlatives", set, organizer);
        team.setOrganizer(organizer);
        return team;
    }
}
