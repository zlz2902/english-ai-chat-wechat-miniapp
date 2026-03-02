package com.horzits.business.domain;

import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(description = "收藏记录")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhFavorite extends BaseEntity {
  private static final long serialVersionUID = 1L;

  private Long favoriteId;

  @ApiModelProperty("用户ID")
  @Excel(name = "用户ID")
  private Long userId;

  @ApiModelProperty("业务类型(1题目 2场景 3帖子)")
  @Excel(name = "业务类型")
  private String bizType;

  @ApiModelProperty("目标ID")
  @Excel(name = "目标ID")
  private Long targetId;
}
