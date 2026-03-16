package com.horzits.business.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.horzits.common.core.controller.BaseController;
import com.horzits.common.core.domain.AppRestResult;
import com.horzits.business.domain.LyhPracticeRecord;
import com.horzits.business.service.ILyhPracticeRecordService;

@RestController
@RequestMapping("/business/lyh/practice")
public class LyhPracticeRecordController extends BaseController {
    @Autowired
    private ILyhPracticeRecordService service;

    @PostMapping("/record")
    public AppRestResult record(@RequestBody Map<String, Object> body) {
        Long userId = getUserId();
        Long questionId = ((Number) body.getOrDefault("questionId", 0)).longValue();
        Integer durationSec = ((Number) body.getOrDefault("durationSec", 0)).intValue();
        Integer allowedTimeSec = ((Number) body.getOrDefault("allowedTimeSec", 60)).intValue();
        String answerText = (String) body.getOrDefault("answerText", "");
        String audioUrl = (String) body.getOrDefault("audioUrl", "");
        String remark = (String) body.getOrDefault("remark", "");

        int score = computeScore(durationSec, allowedTimeSec);

        LyhPracticeRecord record = new LyhPracticeRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setDurationSec(durationSec);
        record.setAnswerText(answerText);
        record.setScore(score);
        record.setRemark("audio=" + audioUrl + ";allowed=" + allowedTimeSec + ";" + remark);

        service.insert(record);

        Map<String, Object> ret = new HashMap<>();
        ret.put("score", score);
        ret.put("recordId", record.getRecordId());
        return AppRestResult.success(ret);
    }

    @GetMapping("/record/list")
    public AppRestResult list() {
        Long userId = getUserId();
        List<LyhPracticeRecord> list = service.listByUser(userId);
        return AppRestResult.success(list);
    }

    private int computeScore(int durationSec, int allowedSec) {
        if (allowedSec <= 0) allowedSec = 60;
        double r = Math.min(Math.max((double) durationSec / allowedSec, 0.0), 1.0);
        int fluency = (int) Math.round(40 * r);
        double target = allowedSec * 0.8;
        double lenRatio = 1.0 - Math.min(Math.abs(durationSec - target) / target, 1.0);
        int length = (int) Math.round(20 * lenRatio);
        int completeness = durationSec >= 6 ? 30 : 10;
        int expressiveness = 10;
        int total = completeness + fluency + length + expressiveness;
        if (total > 100) total = 100;
        if (total < 0) total = 0;
        return total;
    }
}
