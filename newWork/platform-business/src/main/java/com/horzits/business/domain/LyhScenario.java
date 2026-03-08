package com.horzits.business.domain;

import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 场景 lyh_scenario
 */
@ApiModel(description = "口语练习场景")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhScenario extends BaseEntity {
  private static final long serialVersionUID = 1L;

  private Long scenarioId;

  @ApiModelProperty("标题")
  @Excel(name = "标题")
  private String title;

  @ApiModelProperty("封面图URL")
  @Excel(name = "封面图URL")
  private String imageUrl;

  @ApiModelProperty("难度(1初级 2中级 3高级)")
  @Excel(name = "难度", readConverterExp = "1=初级,2=中级,3=高级")
  private String level;

  @ApiModelProperty("描述")
  @Excel(name = "描述")
  private String description;

  @ApiModelProperty("角色名称")
  @Excel(name = "角色名称")
  private String personaName;

  @ApiModelProperty("角色提示词")
  @Excel(name = "角色提示词")
  private String personaPrompt;

  @ApiModelProperty("脚本JSON")
  @Excel(name = "脚本JSON")
  private String scriptJson;

  @ApiModelProperty("排序")
  @Excel(name = "排序")
  private Integer sortOrder;

  @ApiModelProperty("状态(0正常 1下架)")
  @Excel(name = "状态", readConverterExp = "0=正常,1=下架")
  private String status;

  private String delFlag;
}
