package com.example.Document_analiser.repository;

import com.example.Document_analiser.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByUserUsername(String username);
    List<Question> findByTextContainingIgnoreCaseOrAnswer_TextContainingIgnoreCase(String q1, String q2);
    List<Question> findByTopicIgnoreCase(String topic);

    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.answer " +
           "LEFT JOIN FETCH q.document " +
           "WHERE q.user.username = :username AND (:documentId IS NULL OR q.document.id = :documentId)")
    Page<Question> findHistory(@Param("username") String username,
                               @Param("documentId") Long documentId,
                               Pageable pageable);
}