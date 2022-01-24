package com.platform.pod.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int attachment_id;
    private String name;

    public Attachments(String name) {
        this.name = name;
    }
}
