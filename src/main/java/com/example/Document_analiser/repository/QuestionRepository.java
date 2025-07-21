package com.example.Document_analiser.repository;

import com.example.Document_analiser.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByUserUsername(String username);
    List<Question> findByTextContainingIgnoreCaseOrAnswer_TextContainingIgnoreCase(String q1, String q2);
    List<Question> findByTopicIgnoreCase(String topic);
} 