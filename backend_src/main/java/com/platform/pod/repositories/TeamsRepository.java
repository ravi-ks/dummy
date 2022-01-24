package com.platform.pod.repositories;

import com.platform.pod.dto.UserDetails;
import com.platform.pod.entities.Teams;
import com.platform.pod.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TeamsRepository extends JpaRepository<Teams, Integer> {
    // Function to Check if User is the organizer
    List<Teams> findByOrganizer(Users organizer);

    //    Function to get the number of members in a team.
    @Query(value = "SELECT COUNT(uat.user_id) FROM Teams t INNER JOIN t.users_and_teams uat WHERE t.team_id =:team")
    int getMemberCountOfTeam(@Param("team") int teamId);

    //    Function to get the members of a team.
    @Query(value = "SELECT new com.platform.pod.dto.UserDetails(uat.user_id as userId, uat.email, uat.name) FROM Teams t INNER JOIN t.users_and_teams uat WHERE t.team_id=:team")
    List<UserDetails> getDetailsOfMembersOf(@Param("team") int teamId);

    @Query(value = "SELECT COUNT(t) > 0 FROM Teams t JOIN t.organizer o WHERE t.team_id = :team AND o.email = :user")
    Boolean validateOrganizer(@Param("team") int teamId, @Param("user") String email);
}
