package com.horzits.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.horzits.business.domain.LyhFavorite;
import com.horzits.business.mapper.LyhFavoriteMapper;
import com.horzits.business.service.ILyhFavoriteService;

@Service
public class LyhFavoriteServiceImpl implements ILyhFavoriteService {

    @Autowired
    private LyhFavoriteMapper mapper;

    @Override
    public List<Long> listTargets(Long userId, String bizType) {
        return mapper.selectTargetIdsByUserAndBiz(userId, bizType);
    }

    @Override
    public boolean isFavorited(Long userId, String bizType, Long targetId) {
        Integer cnt = mapper.existsByUserBizTarget(userId, bizType, targetId);
        return cnt != null && cnt > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long userId, String bizType, Long targetId) {
        boolean exists = isFavorited(userId, bizType, targetId);
        if (exists) {
            mapper.deleteByUserBizTarget(userId, bizType, targetId);
            return false;
        } else {
            LyhFavorite f = new LyhFavorite();
            f.setUserId(userId);
            f.setBizType(bizType);
            f.setTargetId(targetId);
            mapper.insert(f);
            return true;
        }
    }
}
