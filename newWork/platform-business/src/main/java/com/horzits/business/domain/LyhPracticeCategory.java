package com.horzits.business.domain;

import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 练习分类对象 lyh_practice_category
 */
@ApiModel(description = "练习分类对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhPracticeCategory extends BaseEntity {
  private static final long serialVersionUID = 1L;

  /** 分类ID */
  private Long categoryId;

  /** 分类名称 */
  @ApiModelProperty(value = "分类名称")
  @Excel(name = "分类名称")
  private String categoryName;

  /** 分类类型(ielts/toefl/daily/work) */
  @ApiModelProperty(value = "分类类型")
  @Excel(name = "分类类型")
  private String categoryType;

  /** 排序 */
  @ApiModelProperty(value = "排序")
  @Excel(name = "排序")
  private Integer sortOrder;

  /** 状态（0正常 1停用） */
  @ApiModelProperty(value = "状态")
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  private String delFlag;
}
