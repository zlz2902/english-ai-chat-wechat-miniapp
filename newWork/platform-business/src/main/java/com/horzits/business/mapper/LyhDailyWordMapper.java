package com.horzits.business.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.horzits.business.domain.LyhDailyWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LyhDailyWordMapper extends BaseMapper<LyhDailyWord> {
    public LyhDailyWord selectLyhDailyWordByWordId(Long wordId);
    public List<LyhDailyWord> selectLyhDailyWordList(LyhDailyWord entity);
    public int insertLyhDailyWord(LyhDailyWord entity);
    public int updateLyhDailyWord(LyhDailyWord entity);
    public int deleteLyhDailyWordByWordId(Long wordId);
    public int deleteLyhDailyWordByWordIds(Long[] wordIds);

    @Select("select word_id, word, pronunciation, definition, word_date, status, create_by, create_time, update_by, update_time from lyh_daily_word order by coalesce(word_date, create_time) desc limit 1")
    public LyhDailyWord selectLatest();
}

