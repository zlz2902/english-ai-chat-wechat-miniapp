package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.domain.LyhDailyWord;
import com.horzits.business.mapper.LyhDailyWordMapper;
import com.horzits.business.service.ILyhDailyWordService;

@Service
public class LyhDailyWordServiceImpl implements ILyhDailyWordService {

    @Autowired
    private LyhDailyWordMapper mapper;

    @Override
    public LyhDailyWord selectLyhDailyWordByWordId(Long wordId) {
        return mapper.selectLyhDailyWordByWordId(wordId);
    }

    @Override
    public List<LyhDailyWord> selectLyhDailyWordList(LyhDailyWord entity) {
        return mapper.selectLyhDailyWordList(entity);
    }

    @Override
    public int insertLyhDailyWord(LyhDailyWord entity) {
        return mapper.insertLyhDailyWord(entity);
    }

    @Override
    public int updateLyhDailyWord(LyhDailyWord entity) {
        return mapper.updateLyhDailyWord(entity);
    }

    @Override
    public int deleteLyhDailyWordByWordIds(Long[] wordIds) {
        return mapper.deleteLyhDailyWordByWordIds(wordIds);
    }

    @Override
    public int deleteLyhDailyWordByWordId(Long wordId) {
        return mapper.deleteLyhDailyWordByWordId(wordId);
    }

    @Override
    public LyhDailyWord selectLatest() {
        return mapper.selectLatest();
    }
}

