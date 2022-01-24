package com.platform.pod.repositories;

import java.util.Date;
import java.util.List;

import com.platform.pod.dto.Task;
import com.platform.pod.entities.TaskType;
import com.platform.pod.entities.Tasks;
import com.platform.pod.entities.Teams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TasksRepository extends JpaRepository<Tasks, Long> {

    //    Function to get the tasks of multiple days of a particular team.
    @Query("SELECT new com.platform.pod.dto.Task(t.id, t.title, t.priority, r.startsAt, r.endsOn, t.startTime, t.endTime, r.sun, r.mon, r.tue, r.wed, r.thu, r.fri, r.sat) FROM Repetitions r JOIN r.task t JOIN t.team_assigned_with_task tat WHERE r.startsAt <=:endDate AND r.endsOn >=:startDate AND tat.team_id =:team_id AND t.type = :type ORDER BY t.startTime")
    List<Task> getTasks(@Param("team_id") int teamId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("type") TaskType type);

    //    Function to get all the task of a particular user.
    @Query("SELECT DISTINCT new com.platform.pod.dto.Task(t.id, t.title, t.priority, r.startsAt, r.endsOn, t.startTime, t.endTime, r.sun, r.mon, r.tue, r.wed, r.thu, r.fri, r.sat) FROM Repetitions r JOIN r.task t LEFT JOIN t.team_assigned_with_task tat LEFT JOIN t.organizer o LEFT JOIN tat.users_and_teams u WHERE r.startsAt <=:endDate AND r.endsOn >=:startDate AND (u.user_id =:user_id OR o.user_id =:user_id) ORDER BY t.startTime")
    List<Task> getUserTasks(@Param("user_id") int userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // Function to get all the task of a particular user from a given start date, and no end date .
    @Query("SELECT DISTINCT new com.platform.pod.dto.Task(t.id, t.title, t.priority, r.startsAt, r.endsOn, t.startTime, t.endTime, r.sun, r.mon, r.tue, r.wed, r.thu, r.fri, r.sat) FROM Repetitions r JOIN r.task t LEFT JOIN t.team_assigned_with_task tat LEFT JOIN t.organizer o LEFT JOIN tat.users_and_teams u WHERE r.endsOn >=:startDate AND (u.user_id =:user_id OR o.user_id =:user_id)")
    List<Task> getUserTasksFromStartDate(@Param("user_id") int userId, @Param("startDate") Date startDate);

    // Function to get all the task of a particular user from a given start date, and no end date .
    @Query("SELECT DISTINCT new com.platform.pod.dto.Task(t.id, t.title, t.priority, r.startsAt, r.endsOn, t.startTime, t.endTime, r.sun, r.mon, r.tue, r.wed, r.thu, r.fri, r.sat) FROM Repetitions r JOIN r.task t LEFT JOIN t.team_assigned_with_task tat LEFT JOIN t.organizer o LEFT JOIN tat.users_and_teams u WHERE r.endsOn >=:startDate AND (u.user_id =:user_id OR o.user_id =:user_id) AND t.type =:type")
    List<Task> getTaskFromStartDateAndType(@Param("user_id") int userId, @Param("startDate") Date startDate, @Param("type") TaskType type);

    //    Function to get all the task of a particular user.
    @Query(
            "SELECT DISTINCT new " +
                    "com.platform.pod.dto.Task(t.id, t.title, t.priority, r.startsAt, r.endsOn, t.startTime, t.endTime, r.sun, r.mon, r.tue, r.wed, r.thu, r.fri, r.sat,CASE WHEN (checked_user.user_id = :user_id) THEN true ELSE false END,tat.team_id,CASE WHEN (o.user_id = :user_id) THEN true ELSE false END) " +
                    "FROM Repetitions r " +
                    "JOIN r.task t " +
                    "LEFT JOIN t.team_assigned_with_task tat " +
                    "LEFT JOIN t.organizer o " +
                    "LEFT JOIN t.usersWhoCompletedTask checked_user " +
                    "LEFT JOIN tat.users_and_teams u " +
                    "WHERE r.startsAt <=:endDate AND r.endsOn >=:startDate AND (u.user_id =:user_id OR o.user_id =:user_id) AND t.type = :type ORDER BY t.startTime")
    List<Task> getUserTasksByType(@Param("user_id") int userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("type") TaskType type);

    @Transactional
    @Modifying
    @Query("delete from Repetitions r where r.task.task_id in (select task_id from Tasks t where t.team_assigned_with_task.team_id = :teamId)")
    void deleteRepetitionsByTeamId(@Param("teamId") int teamId);

    @Transactional
    @Modifying
    @Query("delete from Tasks t where t.team_assigned_with_task.team_id = :teamId")
    void deleteTasksByTeamId(@Param("teamId") int teamId);

}
