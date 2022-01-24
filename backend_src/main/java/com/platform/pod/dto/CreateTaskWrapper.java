package com.platform.pod.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.platform.pod.entities.Attachments;
import com.platform.pod.entities.RepetitionType;
import com.platform.pod.entities.TaskPriority;
import com.platform.pod.entities.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskWrapper {
    private String taskTitle;

    private String taskDescription;
    private OffsetTime startTime;
    private OffsetTime endTime;
    List<Attachments> attachmentsURLList;
    private TaskType type;
    private TaskPriority priority = TaskPriority.LOW;
    private String meetingLink; // Meeting Link
    private int assignedTeamId;
    private List<RepetitionType> repetitionType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private Date endsOn;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private Date startsAt;


}
