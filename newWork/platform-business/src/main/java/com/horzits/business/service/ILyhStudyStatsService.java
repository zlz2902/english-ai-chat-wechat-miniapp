package com.horzits.business.service;

import java.util.List;
import com.horzits.business.domain.LyhStudyStats;

/**
 * 学习统计Service接口
 */
public interface ILyhStudyStatsService 
{
    /**
     * 查询学习统计
     * 
     * @param userId 学习统计主键
     * @return 学习统计
     */
    public LyhStudyStats selectLyhStudyStatsByUserId(Long userId);

    /**
     * 查询学习统计列表
     * 
     * @param lyhStudyStats 学习统计
     * @return 学习统计集合
     */
    public List<LyhStudyStats> selectLyhStudyStatsList(LyhStudyStats lyhStudyStats);

    /**
     * 新增学习统计
     * 
     * @param lyhStudyStats 学习统计
     * @return 结果
     */
    public int insertLyhStudyStats(LyhStudyStats lyhStudyStats);

    /**
     * 修改学习统计
     * 
     * @param lyhStudyStats 学习统计
     * @return 结果
     */
    public int updateLyhStudyStats(LyhStudyStats lyhStudyStats);

    /**
     * 批量删除学习统计
     * 
     * @param userIds 需要删除的学习统计主键集合
     * @return 结果
     */
    public int deleteLyhStudyStatsByUserIds(Long[] userIds);

    /**
     * 删除学习统计信息
     * 
     * @param userId 学习统计主键
     * @return 结果
     */
    public int deleteLyhStudyStatsByUserId(Long userId);

    /**
     * 根据打卡记录同步统计数据
     * @param userId
     */
    public void syncStatsFromCheckin(Long userId);
}
