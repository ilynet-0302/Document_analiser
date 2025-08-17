package com.example.Document_analiser.service;

import com.example.Document_analiser.entity.Answer;
import com.example.Document_analiser.repository.AnswerRepository;
import org.springframework.stereotype.Service;

/**
 * Service layer for managing {@link Answer} entities.
 */
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;

    public AnswerService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    /**
     * Persists the given answer entity.
     *
     * @param answer the answer to save
     * @return the stored answer instance
     */
    public Answer save(Answer answer) {
        return answerRepository.save(answer);
    }
}