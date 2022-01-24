package com.platform.pod.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class Teams{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int team_id;
    String team_name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_and_teams", joinColumns = {@JoinColumn(name = "team_id")}
            , inverseJoinColumns = {@JoinColumn(name = "user_id")})
    @JsonIgnore
    public Set<Users> users_and_teams = new HashSet<>();

    @OneToOne(cascade = CascadeType.PERSIST,fetch=FetchType.LAZY)
    @JsonIgnore
    public Users organizer;

    public Users getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Users organizer) {
        this.organizer = organizer;
    }

    public Teams(int team_id, String team_name, Set<Users> users_and_teams, Users organizer) {
        this.team_id = team_id;
        this.organizer = organizer;
        this.team_name = team_name;
        this.users_and_teams = users_and_teams;
    }

    public Teams(int team_id, String team_name) {
        this.team_id = team_id;
        this.team_name = team_name;
    }

    public Teams() {
    }

    public int getTeam_id() {
        return team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public Set<Users> getUsers_and_teams() {
        return users_and_teams;
    }

    public void setUsers_and_teams(Set<Users> users_and_teams) {
        this.users_and_teams = users_and_teams;
    }

    @Override
    public String toString() {
        return "Teams{" +
                "team_id=" + team_id +
                ", team_name='" + team_name + '\'' +
                '}';
    }
}
