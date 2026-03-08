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
 * 社区动态对象 lyh_post
 * 
 * @author horzits
 * @date 2024-07-02
 */
@ApiModel(description = "社区动态")
@Data
@EqualsAndHashCode(callSuper = true)
public class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          LyhCommunity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    private Long postId;

    /** 用户ID */
    @Excel(name = "用户ID")
    @ApiModelProperty("用户ID")
    private Long userId;

    /** 内容 */
    @Excel(name = "内容")
    @ApiModelProperty("内容")
    private String content;

    /** 图片URL */
    @Excel(name = "图片URL")
    @ApiModelProperty("图片URL")
    private String images;

    /** 点赞数 */
    @Excel(name = "点赞数")
    @ApiModelProperty("点赞数")
    private Integer likesCount;

    /** 评论数 */
    @Excel(name = "评论数")
    @ApiModelProperty("评论数")
    private Integer commentsCount;

    /** 可见性(0公开 1仅自己) */
    @Excel(name = "可见性", readConverterExp = "0=公开,1=仅自己")
    @ApiModelProperty("可见性")
    private String visibility;

    /** 状态(0正常 1屏蔽) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=屏蔽")
    @ApiModelProperty("状态")
    private String status;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 备注 */
    @Excel(name = "备注")
    @ApiModelProperty("备注")
    private String remark;

    // --- 非数据库字段 (Business Logic Fields) ---

    /** 发布人昵称 */
    @ApiModelProperty(hidden = true)
    private String nickName;

    /** 发布人头像 */
    @ApiModelProperty(hidden = true)
    private String avatarUrl;

    /** 是否已点赞 */
    @ApiModelProperty(hidden = true)
    private boolean hasLiked;

    /** 格式化的时间字符串 (用于前端显示) */
    @ApiModelProperty(hidden = true)
    private String timeStr;
    
    /** 当前登录用户ID (用于判断是否点赞) */
    @ApiModelProperty(hidden = true)
    private Long currentUserId;

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }
    
    // Compatibility getters for frontend
    public Long getId() {
        return postId;
    }

    public void setId(Long id) {
        this.postId = id;
    }

    public String getName() {
        return nickName;
    }

    public void setName(String name) {
        this.nickName = name;
    }

    public String getAvatar() {
        return avatarUrl;
    }

    public void setAvatar(String avatar) {
        this.avatarUrl = avatar;
    }

    public String getTime() {
        return timeStr;
    }

    public void setTime(String time) {
        this.timeStr = time;
    }

    public Integer getLikes() {
        return likesCount;
    }

    public void setLikes(Integer likes) {
        this.likesCount = likes;
    }

    public Integer getComments() {
        return commentsCount;
    }

    public void setComments(Integer comments) {
        this.commentsCount = comments;
    }
    
    // Map single image to images for compatibility
    public String getImage() {
        return images;
    }

    public void setImage(String image) {
        this.images = image;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("postId", getPostId())
            .append("userId", getUserId())
            .append("content", getContent())
            .append("images", getImages())
            .append("likesCount", getLikesCount())
            .append("commentsCount", getCommentsCount())
            .append("createTime", getCreateTime())
            .toString();
    }
}
