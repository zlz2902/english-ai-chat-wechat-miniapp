package com.horzits.business.domain;

import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "练习记录")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhPracticeRecord extends BaseEntity {
  private static final long serialVersionUID = 1L;

  private Long recordId;

  @ApiModelProperty("用户ID")
  @Excel(name = "用户ID")
  private Long userId;

  @ApiModelProperty("题目ID")
  @Excel(name = "题目ID")
  private Long questionId;

  @ApiModelProperty("作答文本/摘要")
  @Excel(name = "作答文本")
  private String answerText;

  @ApiModelProperty("评分(0-100)")
  @Excel(name = "评分")
  private Integer score;

  @ApiModelProperty("练习时长(秒)")
  @Excel(name = "练习时长(秒)")
  private Integer durationSec;

  @ApiModelProperty("备注")
  @Excel(name = "备注")
  private String remark;
}
