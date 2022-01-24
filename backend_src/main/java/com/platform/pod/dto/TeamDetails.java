package com.platform.pod.dto;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetails {
    private int teamId;
    private String teamName;
    private List<UserDetails> members;
}
