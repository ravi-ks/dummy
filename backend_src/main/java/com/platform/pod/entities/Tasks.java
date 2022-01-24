package com.platform.pod.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetTime;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Tasks{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long task_id;

    private OffsetTime startTime;

    private OffsetTime endTime;

    private String title;

    private String meetingLink; // Meeting Link

    private TaskPriority priority;

    private TaskType type;

    private String description;

    @OneToOne(cascade = CascadeType.PERSIST,fetch=FetchType.LAZY)
    private Users organizer;

    @OneToOne(cascade = CascadeType.PERSIST,fetch=FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Teams team_assigned_with_task;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "task_attachments", joinColumns = {@JoinColumn(name = "task_id")}, inverseJoinColumns = {@JoinColumn(name = "attachment_id")})
    public Set<Attachments> attachments = new HashSet<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "task_completion", joinColumns = {@JoinColumn(name = "task_id")}, inverseJoinColumns = {@JoinColumn(name = "user_id")})
    @JsonIgnore
    private Set<Users> usersWhoCompletedTask = new HashSet<>();

    @Override
    public String toString() {
        return "Tasks{" +
                "task_id=" + task_id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", meetingLink='" + meetingLink + '\'' +
                ", priority=" + priority +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", organizer=" + organizer +
                ", attachments=" + attachments +
                '}';
    }
}

