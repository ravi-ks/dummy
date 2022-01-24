package com.platform.pod.dto;

public class UpdateTeamWrapper {
    int teamId;
    String email;

    public UpdateTeamWrapper(int teamId, String email) {
        this.teamId = teamId;
        this.email = email;
    }

    public UpdateTeamWrapper() {
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String userEmail) {
        email = userEmail;
    }
}
