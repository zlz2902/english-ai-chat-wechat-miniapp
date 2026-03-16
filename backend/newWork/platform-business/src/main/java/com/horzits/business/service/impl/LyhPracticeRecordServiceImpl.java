package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.domain.LyhPracticeRecord;
import com.horzits.business.mapper.LyhPracticeRecordMapper;
import com.horzits.business.service.ILyhPracticeRecordService;

@Service
public class LyhPracticeRecordServiceImpl implements ILyhPracticeRecordService {
    @Autowired
    private LyhPracticeRecordMapper mapper;

    @Override
    public int insert(LyhPracticeRecord record) {
        return mapper.insert(record);
    }

    @Override
    public List<LyhPracticeRecord> listByUser(Long userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    public List<LyhPracticeRecord> listByUserAndQuestion(Long userId, Long questionId) {
        return mapper.selectByUserAndQuestion(userId, questionId);
    }
}
