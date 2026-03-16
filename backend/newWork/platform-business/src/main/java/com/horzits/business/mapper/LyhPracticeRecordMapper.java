package com.horzits.business.mapper;

import java.util.List;
import com.horzits.business.domain.LyhPracticeRecord;

public interface LyhPracticeRecordMapper {
    int insert(LyhPracticeRecord record);
    List<LyhPracticeRecord> selectByUserId(Long userId);
    List<LyhPracticeRecord> selectByUserAndQuestion(Long userId, Long questionId);
}
