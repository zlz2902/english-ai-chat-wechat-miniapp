package com.horzits.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 学习统计对象 lyh_study_stats
 */
@ApiModel(description = "学习统计")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhStudyStats extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 学习天数 */
    @Excel(name = "学习天数")
    @ApiModelProperty("学习天数")
    private Integer totalDays;

    /** 累计单词 */
    @Excel(name = "累计单词")
    @ApiModelProperty("累计单词")
    private Integer totalWords;

    /** 综合分 */
    @Excel(name = "综合分")
    @ApiModelProperty("综合分")
    private Integer score;

    /** 最近学习时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近学习时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最近学习时间")
    private Date lastStudyTime;
}
