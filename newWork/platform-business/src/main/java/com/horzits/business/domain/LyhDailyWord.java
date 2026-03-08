package com.horzits.business.domain;

import java.util.Date;
import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 每日一词 lyh_daily_word
 */
@ApiModel(description = "每日一词")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhDailyWord extends BaseEntity {
  private static final long serialVersionUID = 1L;

  private Long wordId;

  @ApiModelProperty("单词")
  @Excel(name = "单词")
  private String word;

  @ApiModelProperty("音标")
  @Excel(name = "音标")
  private String pronunciation;

  @ApiModelProperty("释义")
  @Excel(name = "释义")
  private String definition;

  @ApiModelProperty("日期")
  @Excel(name = "日期")
  private Date wordDate;

  @ApiModelProperty("状态(0启用 1停用)")
  @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
  private String status;
}

