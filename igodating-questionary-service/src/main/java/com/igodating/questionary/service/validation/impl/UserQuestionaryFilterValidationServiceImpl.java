package com.igodating.questionary.service.validation.impl;

import com.igodating.questionary.dto.filter.UserQuestionaryRecommendationRequest;
import com.igodating.questionary.dto.filter.UserQuestionaryFilterItem;
import com.igodating.questionary.exception.ValidationException;
import com.igodating.questionary.model.MatchingRule;
import com.igodating.questionary.model.Question;
import com.igodating.questionary.model.QuestionaryTemplate;
import com.igodating.questionary.model.UserQuestionary;
import com.igodating.questionary.model.constant.RuleAccessType;
import com.igodating.questionary.repository.UserQuestionaryRepository;
import com.igodating.questionary.service.cache.QuestionaryTemplateCacheService;
import com.igodating.questionary.service.validation.UserQuestionaryFilterValidationService;
import com.igodating.questionary.service.validation.AnswerValueFormatValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQuestionaryFilterValidationServiceImpl implements UserQuestionaryFilterValidationService {

    private final UserQuestionaryRepository userQuestionaryRepository;

    private final QuestionaryTemplateCacheService questionaryTemplateCacheService;

    private final AnswerValueFormatValidationService answerValueFormatValidationService;

    @Override
    @Transactional(readOnly = true)
    public void validateUserQuestionaryFilter(UserQuestionaryRecommendationRequest filter, String userId) {
        UserQuestionary forQuestionary = userQuestionaryRepository.findById(filter.forUserQuestionaryId()).orElseThrow(() -> new ValidationException("Entity not found by id"));

        if (!Objects.equals(forQuestionary.getUserId(), userId)) {
            throw new ValidationException("Wrong user id");
        }

        QuestionaryTemplate questionaryTemplate = questionaryTemplateCacheService.getById(forQuestionary.getQuestionaryTemplateId());

        if (questionaryTemplate == null) {
            throw new ValidationException("Template is not in cache");
        }

        if (questionaryTemplate.isDeleted()) {
            throw new ValidationException("Attempt to filter by deleted template");
        }

        Map<Long, Question> questionFromTemplate = questionaryTemplate.getQuestions().stream().collect(Collectors.toMap(Question::getId, v -> v));

        if (!CollectionUtils.isEmpty(filter.userFilters())) {
            for (UserQuestionaryFilterItem userQuestionaryFilterItem : filter.userFilters()) {
                if (userQuestionaryFilterItem.isEmpty()) {
                    throw new ValidationException("Filter item is empty");
                }

                Question matchedQuestionFromTemplate = questionFromTemplate.get(userQuestionaryFilterItem.questionId());

                if (matchedQuestionFromTemplate == null) {
                    throw new ValidationException("Question not in template");
                }

                MatchingRule matchingRule = matchedQuestionFromTemplate.getMatchingRule();

                if (matchingRule == null) {
                    throw new ValidationException("Matching rule doesn't exist");
                }

                if (RuleAccessType.PRIVATE.equals(matchingRule.getAccessType())) {
                    throw new ValidationException("Private access");
                }

                String value = userQuestionaryFilterItem.filterValue();

                answerValueFormatValidationService.validateValueWithQuestion(value, matchedQuestionFromTemplate);
            }
        }
    }
}
