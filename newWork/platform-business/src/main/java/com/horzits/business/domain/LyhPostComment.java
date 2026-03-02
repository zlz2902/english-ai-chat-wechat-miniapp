package com.horzits.business.domain;

import com.horzits.common.annotation.Excel;
import com.horzits.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子评论对象 lyh_post_comment
 */
@ApiModel(description = "帖子评论")
@Data
@EqualsAndHashCode(callSuper = true)
public class LyhPostComment extends BaseEntity {
  private static final long serialVersionUID = 1L;

  /** 评论ID */
  private Long commentId;

  /** 帖子ID */
  @Excel(name = "帖子ID")
  @ApiModelProperty("帖子ID")
  private Long postId;

  /** 用户ID */
  @Excel(name = "用户ID")
  @ApiModelProperty("用户ID")
  private Long userId;

  /** 父评论ID */
  @Excel(name = "父评论ID")
  @ApiModelProperty("父评论ID")
  private Long parentId;

  /** 评论内容 */
  @Excel(name = "评论内容")
  @ApiModelProperty("评论内容")
  private String content;

  /** 状态(0正常 1屏蔽) */
  @Excel(name = "状态", readConverterExp = "0=正常,1=屏蔽")
  @ApiModelProperty("状态")
  private String status;

  /** 删除标志(0存在 2删除) */
  private String delFlag;

  // --- 非数据库字段 ---
  
  /** 评论人昵称 */
  @ApiModelProperty(hidden = true)
  private String nickName;
  
  /** 评论人头像 */
  @ApiModelProperty(hidden = true)
  private String avatarUrl;
}
