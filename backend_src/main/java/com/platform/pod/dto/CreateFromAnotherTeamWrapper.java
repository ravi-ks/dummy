package com.platform.pod.dto;

import com.platform.pod.entities.Teams;

public class CreateFromAnotherTeamWrapper {
    int teamId;
    String teamName; //new team name

    public CreateFromAnotherTeamWrapper(int teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public CreateFromAnotherTeamWrapper() {
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
