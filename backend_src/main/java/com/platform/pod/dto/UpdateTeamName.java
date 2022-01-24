package com.platform.pod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class UpdateTeamName {
    int teamId;
    String teamName;

    public UpdateTeamName() {
    }

    public UpdateTeamName(int teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
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
