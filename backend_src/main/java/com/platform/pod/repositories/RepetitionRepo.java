package com.platform.pod.repositories;

import com.platform.pod.entities.Repetitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepetitionRepo extends JpaRepository<Repetitions, Integer>, JpaSpecificationExecutor<Repetitions> {
    @Query("SELECT r FROM Repetitions r LEFT JOIN r.task t JOIN t.organizer org WHERE t.task_id = :id AND org.id = :org")
    Repetitions findRepetitionsByTaskId(@Param("id") long taskID, @Param("org") int organizerId);
}
