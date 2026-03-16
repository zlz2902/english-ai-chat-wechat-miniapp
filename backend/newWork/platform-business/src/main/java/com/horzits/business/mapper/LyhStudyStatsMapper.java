package com.horzits.business.mapper;

import java.util.List;
import com.horzits.business.domain.LyhStudyStats;

/**
 * 学习统计Mapper接口
 */
public interface LyhStudyStatsMapper 
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
     * 删除学习统计
     * 
     * @param userId 学习统计主键
     * @return 结果
     */
    public int deleteLyhStudyStatsByUserId(Long userId);

    /**
     * 批量删除学习统计
     * 
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteLyhStudyStatsByUserIds(Long[] userIds);
}
