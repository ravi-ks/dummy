package com.platform.pod.dto;

import com.platform.pod.entities.Attachments;
import com.platform.pod.entities.TaskPriority;
import com.platform.pod.entities.TaskType;

import java.util.HashSet;
import java.util.Set;

import com.platform.pod.entities.Users;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDescription {
    private String title;
    private long id;
    private Set<Attachments> attachments = new HashSet<>();
    private String description;
    private int totalChecks;
    private int totalMemberInTeam;
    private TaskPriority priority;
    private TaskType taskType;
    private String meetingLink;
    private Users organizer;
    private boolean isOrganizer;

    public void setIsOrganizer(boolean value) {
        this.isOrganizer = value;
    }

    public boolean getIsOrganizer() {
        return this.isOrganizer;
    }
}
