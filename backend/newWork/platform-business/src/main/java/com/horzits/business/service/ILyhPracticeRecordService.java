package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhPracticeRecord;

public interface ILyhPracticeRecordService {
    int insert(LyhPracticeRecord record);
    List<LyhPracticeRecord> listByUser(Long userId);
    List<LyhPracticeRecord> listByUserAndQuestion(Long userId, Long questionId);
}
