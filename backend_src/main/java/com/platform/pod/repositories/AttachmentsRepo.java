package com.platform.pod.repositories;

import com.platform.pod.entities.Attachments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentsRepo extends JpaRepository<Attachments, Integer> {
    public Attachments getByName(String name);
    public int deleteByName(String name);
}
