package com.platform.pod;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.pod.common.ResponseMessage;
import com.platform.pod.controllers.TeamsController;
import com.platform.pod.dto.*;
import com.platform.pod.services.TeamsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

@SpringBootTest
public class TeamsControllerTesting {

    //    To call the rest end points.
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    TeamsService teamsService;

    @InjectMocks
    TeamsController teamsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamsController).build();
    }

    @Test
    public void testingInstance() {
        Assertions.assertNotNull(teamsController);
    }

    @Test
    public void testingGetTeams() throws Exception {
//        Mocking
        List<TeamSummary> teamSummaryList = new ArrayList<>();
        teamSummaryList.add(new TeamSummary(1, "Team 1", 100));
        teamSummaryList.add(new TeamSummary(2, "Team 2", 100));
        teamSummaryList.add(new TeamSummary(3, "Team 3", 100));

//        Actual Testing
        mockMvc.perform(MockMvcRequestBuilders.get("/teams/summary"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(teamSummaryList)));
    }

    @Test
    public void testingGetTeamDetails() throws Exception {
//        Mocking
        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(new UserDetails(1, "one@email.com", "one"));
        userDetailsList.add(new UserDetails(2, "two@email.com", "two"));
        userDetailsList.add(new UserDetails(3, "three@email.com", "three"));

        TeamDetails teamDetails = new TeamDetails(1, "team name", userDetailsList);

        Mockito.when(teamsService.getTeamDetails(Mockito.anyString(), Mockito.anyInt())).thenReturn(teamDetails);

//        Actual Testing
        mockMvc.perform(MockMvcRequestBuilders.get("/teams/details/1/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(teamDetails)));
    }

    @Test
    public void testAddMemberToTeam() throws Exception {
        UpdateTeamWrapper updateTeamWrapper = new UpdateTeamWrapper();
        updateTeamWrapper.setTeamId(1);
        updateTeamWrapper.setEmail("prafful@gmail.com");
        String memberEmail = updateTeamWrapper.getEmail();
        int teamId = updateTeamWrapper.getTeamId();
        String expected = "Member : " + memberEmail + " Added To Team : 1";
        ResponseMessage responseMessage = new ResponseMessage(expected);
        Mockito.when(teamsService.addMemberToTeam(teamId, memberEmail, "abc@test.com")).thenReturn(expected);
        mockMvc.perform(MockMvcRequestBuilders.put("/teams/addMember")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(updateTeamWrapper))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(responseMessage)));
    }

    @Test
    public void testRemoveMemberFromTeam() throws Exception {
        UpdateTeamWrapper updateTeamWrapper = new UpdateTeamWrapper();
        updateTeamWrapper.setTeamId(1);
        updateTeamWrapper.setEmail("prafful@gmail.com");
        String memberEmail = updateTeamWrapper.getEmail();
        int teamId = updateTeamWrapper.getTeamId();
        String expected = "Member : " + memberEmail + " Added To Team : 1";
        ResponseMessage responseMessage = new ResponseMessage(expected);
        Mockito.when(teamsService.removeMemberFromTeam(teamId, memberEmail, "abc@test.com")).thenReturn(expected);
        mockMvc.perform(MockMvcRequestBuilders.delete("/teams/removeMember")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(updateTeamWrapper))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(responseMessage)));
    }

    @Test
    public void testUpdateTeamName() throws Exception {
        UpdateTeamName updateTeamName = new UpdateTeamName();
        updateTeamName.setTeamName("Dominators");
        updateTeamName.setTeamId(1);
        int teamId = updateTeamName.getTeamId();
        String newTeamName = updateTeamName.getTeamName();
        String expected = "Team Name Superlatives Updated with " + newTeamName;
        ResponseMessage responseMessage = new ResponseMessage(expected);
        Mockito.when(teamsService.updateTeamName(teamId, newTeamName)).thenReturn(expected);

        mockMvc.perform(MockMvcRequestBuilders.patch("/teams/teamName")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTeamName))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(responseMessage)));
    }
}
