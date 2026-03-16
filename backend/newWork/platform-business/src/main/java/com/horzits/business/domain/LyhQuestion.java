package com.horzits.business.domain;

import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 口语题库题目 lyh_question
 */
@ApiModel(description = "口语题库题目")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhQuestion extends BaseEntity {
  private static final long serialVersionUID = 1L;

  private Long questionId;

  @ApiModelProperty("分类ID")
  @Excel(name = "分类ID")
  private Long categoryId;

  @ApiModelProperty("主题")
  @Excel(name = "主题")
  private String topic;

  @ApiModelProperty("难度(1入门 2进阶 3困难)")
  @Excel(name = "难度", readConverterExp = "1=入门,2=进阶,3=困难")
  private String difficulty;

  @ApiModelProperty("题目文本")
  @Excel(name = "题目文本")
  private String questionText;

  @ApiModelProperty("标签(逗号分隔)")
  @Excel(name = "标签")
  private String tags;

  @ApiModelProperty("音频URL")
  @Excel(name = "音频URL")
  private String audioUrl;

  @ApiModelProperty("状态(0正常 1停用)")
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;

  private String delFlag;
}

