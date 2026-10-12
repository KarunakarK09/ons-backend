
package com.anurag.ons.repository;

import com.anurag.ons.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("SELECT n FROM Note n WHERE LOWER(n.subject) LIKE LOWER(CONCAT('%', :subject, '%'))")
    List<Note> searchBySubject(@Param("subject") String subject);
}
