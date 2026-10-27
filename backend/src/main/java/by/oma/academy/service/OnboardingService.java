package by.oma.academy.service;

import by.oma.academy.dto.OnboardingDto;
import by.oma.academy.dto.OnboardingRequest;
import by.oma.academy.model.OnboardingAnswers;
import by.oma.academy.repository.OnboardingAnswersRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class OnboardingService {

    private final OnboardingAnswersRepository onboardingAnswersRepository;
    private final ObjectMapper objectMapper;

    public OnboardingService(OnboardingAnswersRepository onboardingAnswersRepository, ObjectMapper objectMapper) {
        this.onboardingAnswersRepository = onboardingAnswersRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public OnboardingDto getAnswers(Long userId) {
        return onboardingAnswersRepository.findByUserId(userId)
                .map(this::toDto)
                .orElse(new OnboardingDto(Collections.emptyList(), null, Collections.emptyList(), false));
    }

    @Transactional
    public OnboardingDto saveAnswers(Long userId, OnboardingRequest request) {
        OnboardingAnswers answers = onboardingAnswersRepository.findByUserId(userId)
                .orElseGet(() -> {
                    OnboardingAnswers oa = new OnboardingAnswers();
                    oa.setUserId(userId);
                    return oa;
                });

        try {
            answers.setInterests(objectMapper.writeValueAsString(request.interests() != null ? request.interests() : List.of()));
            answers.setExperience(request.experience());
            answers.setGoals(objectMapper.writeValueAsString(request.goals() != null ? request.goals() : List.of()));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения опроса онбординга", e);
        }

        answers = onboardingAnswersRepository.save(answers);
        return toDto(answers);
    }

    private OnboardingDto toDto(OnboardingAnswers a) {
        List<String> interests = parseList(a.getInterests());
        List<String> goals = parseList(a.getGoals());
        return new OnboardingDto(interests, a.getExperience(), goals, true);
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
