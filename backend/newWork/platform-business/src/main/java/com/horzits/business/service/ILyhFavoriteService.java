package com.horzits.business.service;

import java.util.List;

public interface ILyhFavoriteService {
    boolean toggle(Long userId, String bizType, Long targetId);
    List<Long> listTargets(Long userId, String bizType);
    boolean isFavorited(Long userId, String bizType, Long targetId);
}
