package com.igodating.questionary.scheduler.task.impl;

import com.igodating.questionary.dto.textembedding.TextEmbeddingRequest;
import com.igodating.questionary.dto.textembedding.TextEmbeddingRequestItem;
import com.igodating.questionary.dto.textembedding.TextEmbeddingResponse;
import com.igodating.questionary.feign.TextEmbeddingService;
import com.igodating.questionary.model.MatchingRule;
import com.igodating.questionary.model.Question;
import com.igodating.questionary.model.UserQuestionary;
import com.igodating.questionary.model.UserQuestionaryAnswer;
import com.igodating.questionary.model.constant.RuleMatchingType;
import com.igodating.questionary.scheduler.task.UserQuestionaryEmbeddingCalculationTask;
import com.igodating.questionary.service.UserQuestionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserQuestionaryEmbeddingCalculationTaskImpl implements UserQuestionaryEmbeddingCalculationTask {

    private final UserQuestionaryService userQuestionaryService;

    private final TextEmbeddingService textEmbeddingService;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void executeEmbeddingCalculation(int batchSize) {
        //todo пока синхронная обработка

        List<UserQuestionary> questionaries = userQuestionaryService.findUnprocessedWithLimit(batchSize);

        for (UserQuestionary userQuestionary : questionaries) {
            handleQuestionary(userQuestionary);
        }
    }

    private void handleQuestionary(UserQuestionary questionary) {
        List<UserQuestionaryAnswer> answersInFreeFormAndSemanticMatchingRule = questionary.getAnswers()
                .stream()
                .filter(answer -> {
                    Question question = answer.getQuestion();
                    MatchingRule matchingRule = question.getMatchingRule();
                    return matchingRule != null && RuleMatchingType.SEMANTIC_RANGING.equals(matchingRule.getMatchingType());
                })
                .toList();

        if (answersInFreeFormAndSemanticMatchingRule.isEmpty()) {
            userQuestionaryService.setStatusToPublished(questionary);
            return;
        }

        List<TextEmbeddingRequestItem> requestItems = answersInFreeFormAndSemanticMatchingRule
                .stream()
                .map(answer -> new TextEmbeddingRequestItem(answer.getId().toString(), answer.getValue()))
                .toList();

        TextEmbeddingResponse response = textEmbeddingService.getEmbeddings(new TextEmbeddingRequest(requestItems));

        questionary.setEmbedding(response.globalEmbedding());

        userQuestionaryService.updateEmbeddingAndSetProcessed(questionary);
    }
}
