package com.privacydoccontrol.repository;

import com.privacydoccontrol.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findByToken(String token);

    @Query("SELECT d FROM Document d WHERE UPPER(TRIM(d.token)) = UPPER(TRIM(:token))")
    Document findByTokenIgnoreCase(@Param("token") String token);
}
