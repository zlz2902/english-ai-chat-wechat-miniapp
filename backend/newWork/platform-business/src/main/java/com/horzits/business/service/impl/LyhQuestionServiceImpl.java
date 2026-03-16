package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.domain.LyhQuestion;
import com.horzits.business.mapper.LyhQuestionMapper;
import com.horzits.business.service.ILyhQuestionService;

@Service
public class LyhQuestionServiceImpl implements ILyhQuestionService {

    @Autowired
    private LyhQuestionMapper mapper;

    @Override
    public LyhQuestion selectLyhQuestionByQuestionId(Long questionId) {
        return mapper.selectLyhQuestionByQuestionId(questionId);
    }

    @Override
    public List<LyhQuestion> selectLyhQuestionList(LyhQuestion entity) {
        return mapper.selectLyhQuestionList(entity);
    }

    @Override
    public int insertLyhQuestion(LyhQuestion entity) {
        return mapper.insertLyhQuestion(entity);
    }

    @Override
    public int updateLyhQuestion(LyhQuestion entity) {
        return mapper.updateLyhQuestion(entity);
    }

    @Override
    public int deleteLyhQuestionByQuestionIds(Long[] questionIds) {
        return mapper.deleteLyhQuestionByQuestionIds(questionIds);
    }

    @Override
    public int deleteLyhQuestionByQuestionId(Long questionId) {
        return mapper.deleteLyhQuestionByQuestionId(questionId);
    }

    @Override
    public List<LyhQuestion> selectFavoritedQuestions(Long userId) {
        return mapper.selectFavoritedQuestions(userId);
    }
}

