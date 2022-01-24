package com.platform.pod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamSummary {
    private int teamId;
    private String name;
    private int membersCount;
}
