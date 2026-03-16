package com.horzits.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.horzits.business.domain.LyhFavorite;

public interface LyhFavoriteMapper {
    int insert(LyhFavorite entity);
    int deleteByUserBizTarget(@Param("userId") Long userId, @Param("bizType") String bizType, @Param("targetId") Long targetId);
    Integer existsByUserBizTarget(@Param("userId") Long userId, @Param("bizType") String bizType, @Param("targetId") Long targetId);
    List<Long> selectTargetIdsByUserAndBiz(@Param("userId") Long userId, @Param("bizType") String bizType);
}
