package com.trisha.midia.repository;

import com.trisha.midia.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFile, String> {
}
