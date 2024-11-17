package com.igodating.questionary.service.impl;

import com.igodating.questionary.model.MatchingRule;
import com.igodating.questionary.model.Question;
import com.igodating.questionary.model.UserQuestionary;
import com.igodating.questionary.model.UserQuestionaryAnswer;
import com.igodating.questionary.model.constant.QuestionAnswerType;
import com.igodating.questionary.model.constant.RuleMatchingType;
import com.igodating.questionary.model.constant.UserQuestionaryStatus;
import com.igodating.questionary.repository.QuestionRepository;
import com.igodating.questionary.repository.UserQuestionaryAnswerRepository;
import com.igodating.questionary.repository.UserQuestionaryRepository;
import com.igodating.questionary.service.UserQuestionaryService;
import com.igodating.questionary.service.validation.UserQuestionaryValidationService;
import com.igodating.questionary.util.EntitiesListChange;
import com.igodating.questionary.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQuestionaryServiceImpl implements UserQuestionaryService {

    private final QuestionRepository questionRepository;

    private final UserQuestionaryRepository userQuestionaryRepository;

    private final UserQuestionaryAnswerRepository userQuestionaryAnswerRepository;

    private final UserQuestionaryValidationService userQuestionaryValidationService;

    @Override
    @Transactional(readOnly = true)
    public UserQuestionary getById(Long id) {
        return userQuestionaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Entity not found by id"));
    }

    @Override
    @Transactional
    public void createDraft(UserQuestionary userQuestionary, String userId) {
        userQuestionary.setUserId(userId);

        userQuestionaryValidationService.validateOnCreate(userQuestionary);

        userQuestionary.setQuestionaryStatus(UserQuestionaryStatus.DRAFT);

        userQuestionaryRepository.save(userQuestionary);

        for (UserQuestionaryAnswer userQuestionaryAnswer : userQuestionary.getAnswers()) {
            initTsVectorValueIfNeeded(userQuestionaryAnswer);
            userQuestionaryAnswer.setUserQuestionaryId(userQuestionary.getId());
        }

        userQuestionaryAnswerRepository.saveAll(userQuestionary.getAnswers());
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void update(UserQuestionary userQuestionary, String userId) {
        userQuestionaryValidationService.validateOnUpdate(userQuestionary, userId);

        UserQuestionary existedQuestionary = userQuestionaryRepository.getReferenceById(userQuestionary.getId());
        existedQuestionary.setQuestionaryStatus(userQuestionary.getQuestionaryStatus());
        existedQuestionary.setTitle(userQuestionary.getTitle());
        existedQuestionary.setDescription(userQuestionary.getDescription());

        EntitiesListChange<UserQuestionaryAnswer, Long> answersChange = ServiceUtils.changes(existedQuestionary.getAnswers(), userQuestionary.getAnswers(), (first, second) -> Objects.equals(first.getValue(), second.getValue()));

        boolean isDraft = UserQuestionaryStatus.DRAFT.equals(existedQuestionary.getQuestionaryStatus());
        boolean semanticRankingAnswerWasChanged = false;

        for (UserQuestionaryAnswer answerToDelete : answersChange.getToDelete()) {
            if (!isDraft && !semanticRankingAnswerWasChanged && questionIsSemantic(questionRepository.getReferenceById(answerToDelete.getQuestionId()))) {
                semanticRankingAnswerWasChanged = true;
            }
            userQuestionaryAnswerRepository.delete(answerToDelete);
        }

        for (Pair<UserQuestionaryAnswer, UserQuestionaryAnswer> answerPairToUpdate : answersChange.getOldNewPairToUpdate()) {
            UserQuestionaryAnswer oldAnswer = answerPairToUpdate.getFirst();
            UserQuestionaryAnswer newAnswer = answerPairToUpdate.getSecond();

            if (!isDraft && !semanticRankingAnswerWasChanged && questionIsSemantic(questionRepository.getReferenceById(oldAnswer.getQuestionId()))) {
                semanticRankingAnswerWasChanged = true;
            }

            oldAnswer.setValue(newAnswer.getValue());
            initTsVectorValueIfNeeded(oldAnswer);
            userQuestionaryAnswerRepository.save(oldAnswer);
        }

        for (UserQuestionaryAnswer answerToCreate : answersChange.getToCreate()) {
            if (!isDraft && !semanticRankingAnswerWasChanged && questionIsSemantic(questionRepository.getReferenceById(answerToCreate.getQuestionId()))) {
                semanticRankingAnswerWasChanged = true;
            }

            initTsVectorValueIfNeeded(answerToCreate);
            answerToCreate.setUserQuestionaryId(userQuestionary.getId());
            userQuestionaryAnswerRepository.save(answerToCreate);
        }

        if (!isDraft && semanticRankingAnswerWasChanged) {
            existedQuestionary.setQuestionaryStatus(UserQuestionaryStatus.ON_PROCESSING);
        }

        userQuestionaryRepository.save(existedQuestionary);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void setStatusToPublished(UserQuestionary userQuestionary) {
        userQuestionaryValidationService.validateOnSetStatusToPublished(userQuestionary);

        UserQuestionary existedQuestionary = userQuestionaryRepository.getReferenceById(userQuestionary.getId());

        existedQuestionary.setQuestionaryStatus(UserQuestionaryStatus.PUBLISHED);

        userQuestionaryRepository.save(existedQuestionary);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void moveFromDraft(UserQuestionary userQuestionary, String userId) {
        userQuestionaryValidationService.validateOnMoveFromDraft(userQuestionary, userId);

        UserQuestionary existedQuestionary = userQuestionaryRepository.getReferenceById(userQuestionary.getId());

        boolean answerWithSemanticRangingDoesExist = existedQuestionary.getAnswers()
                .stream()
                .anyMatch(answer -> questionIsSemantic(answer.getQuestion()));

        existedQuestionary.setQuestionaryStatus(answerWithSemanticRangingDoesExist ? UserQuestionaryStatus.ON_PROCESSING : UserQuestionaryStatus.PUBLISHED);
        userQuestionaryRepository.save(existedQuestionary);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void delete(UserQuestionary userQuestionary, String userId) {
        userQuestionaryValidationService.validateOnDelete(userQuestionary, userId);

        UserQuestionary existedQuestionary = userQuestionaryRepository.getReferenceById(userQuestionary.getId());
        existedQuestionary.setToDelete();

        userQuestionaryRepository.save(existedQuestionary);

        userQuestionaryAnswerRepository.deleteAllByUserQuestionaryId(existedQuestionary.getId());
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateEmbeddingAndSetProcessed(UserQuestionary userQuestionary) {
        userQuestionaryValidationService.validateOnUpdateEmbedding(userQuestionary);

        UserQuestionary existedQuestionary = userQuestionaryRepository.getReferenceById(userQuestionary.getId());
        existedQuestionary.setEmbedding(userQuestionary.getEmbedding());
        existedQuestionary.setQuestionaryStatus(UserQuestionaryStatus.PUBLISHED);

        userQuestionaryRepository.save(existedQuestionary);

        Map<Long, UserQuestionaryAnswer> existedAnswersIdMap = existedQuestionary.getAnswers().stream().collect(Collectors.toMap(UserQuestionaryAnswer::getId, Function.identity()));
        for (UserQuestionaryAnswer userQuestionaryAnswer : userQuestionary.getAnswers()) {
            UserQuestionaryAnswer existedAnswer = existedAnswersIdMap.get(userQuestionaryAnswer.getId());
            existedAnswer.setEmbedding(userQuestionaryAnswer.getEmbedding());
        }

        userQuestionaryAnswerRepository.saveAll(existedQuestionary.getAnswers());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserQuestionary> findUnprocessedWithLimit(int limit) {
        return userQuestionaryRepository.findAllByQuestionaryStatusAndDeletedAtIsNull(
                UserQuestionaryStatus.ON_PROCESSING,
                PageRequest.of(0, limit, Sort.by(Sort.Order.desc("id")))
        );
    }

    private boolean questionIsSemantic(Question question) {
        MatchingRule matchingRule = question.getMatchingRule();

        if (matchingRule == null) {
            return false;
        }

        return RuleMatchingType.SEMANTIC_RANGING.equals(matchingRule.getMatchingType());
    }

    private void initTsVectorValueIfNeeded(UserQuestionaryAnswer userQuestionaryAnswer) {
        Question question = questionRepository.getReferenceById(userQuestionaryAnswer.getQuestionId());

        if (question.getAnswerType().equals(QuestionAnswerType.FREE_FORM)) {
            userQuestionaryAnswer.setTsVectorValue(userQuestionaryAnswer.getValue());
        }
    }
}
