package com.igodating.questionary.util.val.impl;

import com.igodating.questionary.model.MatchingRuleDefaultValues;
import com.igodating.questionary.model.MatchingRuleDefaultValuesCase;
import com.igodating.questionary.model.Question;
import com.igodating.questionary.util.val.DefaultValueExtractor;
import com.igodating.questionary.util.val.ValuesEqualityChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultValueExtractorImpl implements DefaultValueExtractor {

    private final ValuesEqualityChecker valuesEqualityChecker;

    @Override
    public String extractDefaultValueForMatchingByAnswer(String answer, Question question) {
        if (question.getMatchingRule() == null || question.getMatchingRule().getDefaultValues() == null) {
            return null;
        }

        return findInCaseBlock(answer, question).orElse(question.getMatchingRule().getDefaultValues().getDefaultValue());
    }

    private Optional<String> findInCaseBlock(String answer, Question question) {
        MatchingRuleDefaultValues defaultValues = question.getMatchingRule().getDefaultValues();
        return defaultValues.getCases().stream()
                .filter(defaultValCase -> valuesEqualityChecker.equals(defaultValCase.getWhen(), answer, question))
                .map(MatchingRuleDefaultValuesCase::getThen)
                .findFirst();
    }
}
