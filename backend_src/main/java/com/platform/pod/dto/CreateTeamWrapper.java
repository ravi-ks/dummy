package com.platform.pod.dto;

import com.platform.pod.entities.Users;

import java.util.List;

public class CreateTeamWrapper {
    List<String> userEmails;
    String teamName;

    public CreateTeamWrapper(List<String> userEmails, String teamName) {
        this.userEmails = userEmails;
        this.teamName = teamName;
    }

    public CreateTeamWrapper() {
    }

    public List<String> getUserEmails() {
        return userEmails;
    }

    public void setUserEmails(List<String> userEmails) {
        this.userEmails = userEmails;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
