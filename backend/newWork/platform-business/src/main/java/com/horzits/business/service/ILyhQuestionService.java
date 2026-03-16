package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhQuestion;

public interface ILyhQuestionService {
    public LyhQuestion selectLyhQuestionByQuestionId(Long questionId);
    public List<LyhQuestion> selectLyhQuestionList(LyhQuestion entity);
    public int insertLyhQuestion(LyhQuestion entity);
    public int updateLyhQuestion(LyhQuestion entity);
    public int deleteLyhQuestionByQuestionIds(Long[] questionIds);
    public int deleteLyhQuestionByQuestionId(Long questionId);

    /**
     * 查询用户收藏的题目
     * @param userId
     * @return
     */
    public List<LyhQuestion> selectFavoritedQuestions(Long userId);
}

