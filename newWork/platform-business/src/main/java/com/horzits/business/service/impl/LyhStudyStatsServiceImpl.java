package com.horzits.business.service.impl;

import java.util.List;
import com.horzits.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horzits.business.mapper.LyhStudyStatsMapper;
import com.horzits.business.domain.LyhStudyStats;
import com.horzits.business.service.ILyhStudyStatsService;

/**
 * 学习统计Service业务层处理
 */
@Service
public class LyhStudyStatsServiceImpl implements ILyhStudyStatsService 
{
    @Autowired
    private LyhStudyStatsMapper lyhStudyStatsMapper;

    /**
     * 查询学习统计
     * 
     * @param userId 学习统计主键
     * @return 学习统计
     */
    @Override
    public LyhStudyStats selectLyhStudyStatsByUserId(Long userId)
    {
        return lyhStudyStatsMapper.selectLyhStudyStatsByUserId(userId);
    }

    /**
     * 查询学习统计列表
     * 
     * @param lyhStudyStats 学习统计
     * @return 学习统计
     */
    @Override
    public List<LyhStudyStats> selectLyhStudyStatsList(LyhStudyStats lyhStudyStats)
    {
        return lyhStudyStatsMapper.selectLyhStudyStatsList(lyhStudyStats);
    }

    /**
     * 新增学习统计
     * 
     * @param lyhStudyStats 学习统计
     * @return 结果
     */
    @Override
    public int insertLyhStudyStats(LyhStudyStats lyhStudyStats)
    {
        lyhStudyStats.setUpdateTime(DateUtils.getNowDate());
        return lyhStudyStatsMapper.insertLyhStudyStats(lyhStudyStats);
    }

    /**
     * 修改学习统计
     * 
     * @param lyhStudyStats 学习统计
     * @return 结果
     */
    @Override
    public int updateLyhStudyStats(LyhStudyStats lyhStudyStats)
    {
        lyhStudyStats.setUpdateTime(DateUtils.getNowDate());
        return lyhStudyStatsMapper.updateLyhStudyStats(lyhStudyStats);
    }

    /**
     * 批量删除学习统计
     * 
     * @param userIds 需要删除的学习统计主键
     * @return 结果
     */
    @Override
    public int deleteLyhStudyStatsByUserIds(Long[] userIds)
    {
        return lyhStudyStatsMapper.deleteLyhStudyStatsByUserIds(userIds);
    }

    /**
     * 删除学习统计信息
     * 
     * @param userId 学习统计主键
     * @return 结果
     */
    @Override
    public int deleteLyhStudyStatsByUserId(Long userId)
    {
        return lyhStudyStatsMapper.deleteLyhStudyStatsByUserId(userId);
    }

    @Autowired
    private com.horzits.business.mapper.LyhCheckinMapper lyhCheckinMapper;

    @Override
    public void syncStatsFromCheckin(Long userId) {
        int checkinCount = lyhCheckinMapper.selectCountByUserId(userId);
        LyhStudyStats stats = lyhStudyStatsMapper.selectLyhStudyStatsByUserId(userId);
        if (stats == null) {
            stats = new LyhStudyStats();
            stats.setUserId(userId);
            stats.setTotalDays(checkinCount);
            stats.setScore(checkinCount * 10);
            stats.setTotalWords(0);
            this.insertLyhStudyStats(stats);
        } else {
            stats.setTotalDays(checkinCount);
            stats.setScore(checkinCount * 10);
            this.updateLyhStudyStats(stats);
        }
    }
}
