package com.platform.pod.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.platform.pod.entities.Repetitions;
import com.platform.pod.entities.TaskPriority;
import com.platform.pod.entities.Teams;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class Task {
    private long taskId;
    private String title;
    private TaskPriority priority;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata")
    private Date endDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata")
    private OffsetTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata")
    private OffsetTime endTime;

    private Repeat repeat;

    private boolean isChecked = false;

    private boolean canBeChecked = false;

    public Task(long taskId, String title, TaskPriority priority, Date startDate, Date endDate, OffsetTime startTime, OffsetTime endTime, boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;

        repeat = new Repeat(sunday, monday, tuesday, wednesday, thursday, friday, saturday);
    }

    public Task(
            long taskId,
            String title,
            TaskPriority priority,
            Date startDate,
            Date endDate,
            OffsetTime startTime,
            OffsetTime endTime,
            boolean sunday,
            boolean monday,
            boolean tuesday,
            boolean wednesday,
            boolean thursday,
            boolean friday,
            boolean saturday,
            boolean isChecked
    ) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isChecked = isChecked;

        repeat = new Repeat(sunday, monday, tuesday, wednesday, thursday, friday, saturday);
    }

    public Task(
            long taskId,
            String title,
            TaskPriority priority,
            Date startDate,
            Date endDate,
            OffsetTime startTime,
            OffsetTime endTime,
            boolean sunday,
            boolean monday,
            boolean tuesday,
            boolean wednesday,
            boolean thursday,
            boolean friday,
            boolean saturday,
            boolean isChecked,
            Integer teamId,
            boolean isOrganizer
    ) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isChecked = isChecked;
        this.canBeChecked = teamId == null || !isOrganizer;

        repeat = new Repeat(sunday, monday, tuesday, wednesday, thursday, friday, saturday);
    }


    public Task(Repetitions r) {
        this.taskId = r.getTask().getTask_id();
        this.title = r.getTask().getTitle();
        this.priority = r.getTask().getPriority();
        this.startDate = r.getStartsAt();
        this.endDate = r.getEndsOn();
        this.startTime = r.getTask().getStartTime();
        this.endTime = r.getTask().getEndTime();
        this.repeat = r.toRepeat();
    }
}
