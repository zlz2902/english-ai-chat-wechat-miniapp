package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhDailyWord;

public interface ILyhDailyWordService {
    public LyhDailyWord selectLyhDailyWordByWordId(Long wordId);
    public List<LyhDailyWord> selectLyhDailyWordList(LyhDailyWord entity);
    public int insertLyhDailyWord(LyhDailyWord entity);
    public int updateLyhDailyWord(LyhDailyWord entity);
    public int deleteLyhDailyWordByWordIds(Long[] wordIds);
    public int deleteLyhDailyWordByWordId(Long wordId);
    public LyhDailyWord selectLatest();
}

