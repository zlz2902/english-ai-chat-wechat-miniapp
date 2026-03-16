package com.horzits.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 每日打卡对象 lyh_checkin
 * 
 * @author horzits
 * @date 2024-07-02
 */
@ApiModel(description = "每日打卡")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhCheckin extends BaseEntity {
  private static final long serialVersionUID = 1L;

  /** 打卡ID */
  private Long checkinId;

  /** 用户ID */
  @Excel(name = "用户ID")
  @ApiModelProperty("用户ID")
  private Long userId;

  /** 打卡日期 */
  @JsonFormat(pattern = "yyyy-MM-dd")
  @Excel(name = "打卡日期", width = 30, dateFormat = "yyyy-MM-dd")
  @ApiModelProperty("打卡日期")
  private Date checkinDate;

  /** 连续天数(打卡后) */
  @Excel(name = "连续天数(打卡后)")
  @ApiModelProperty("连续天数(打卡后)")
  private Integer streakAfter;

  /** 用户昵称 */
  @Excel(name = "用户昵称")
  @ApiModelProperty("用户昵称")
  private String userName;

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("checkinId", getCheckinId())
        .append("userId", getUserId())
        .append("checkinDate", getCheckinDate())
        .append("streakAfter", getStreakAfter())
        .append("createTime", getCreateTime())
        .toString();
  }
}
