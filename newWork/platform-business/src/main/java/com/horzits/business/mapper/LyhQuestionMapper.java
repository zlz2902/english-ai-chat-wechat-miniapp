package com.horzits.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horzits.business.domain.LyhQuestion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LyhQuestionMapper extends BaseMapper<LyhQuestion> {
    public LyhQuestion selectLyhQuestionByQuestionId(Long questionId);
    public List<LyhQuestion> selectLyhQuestionList(LyhQuestion entity);
    public int insertLyhQuestion(LyhQuestion entity);
    public int updateLyhQuestion(LyhQuestion entity);
    public int deleteLyhQuestionByQuestionId(Long questionId);
    public int deleteLyhQuestionByQuestionIds(Long[] questionIds);

    /**
     * 查询用户收藏的题目
     * @param userId
     * @return
     */
    public List<LyhQuestion> selectFavoritedQuestions(Long userId);
}

