package com.horzits.common.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("api通用返回")
public class AppRestResult<T> {

    //uuid,用作唯一标识符，供序列化和反序列化时检测是否一致
    private static final long serialVersionUID = 7498483649536881777L;

    /**
     * 状态码
     */
    @ApiModelProperty("响应码")
    public static final Integer SUCCESS = 200;

    /**
     * 返回内容
     */
    @ApiModelProperty("响应描述")
    public static final String SUCCESS_MSG = "操作成功";

    //标识代码，0表示成功，非0表示出错
    @ApiModelProperty(value = "标识代码,200表示成功，其他表示出错", example = "200")
    private Integer code;

    //提示信息，通常供报错时使用
    @ApiModelProperty(value = "提示信息", example = "操作成功")
    private String msg;

    // //RSA密钥签名
    // @ApiModelProperty(value = "RSA密钥签名,WMS不需要验证")
    // private String signature;
    //
    // //时间戳
    // @ApiModelProperty(value = "时间戳")
    // private Long timestamp;

    //正常返回时返回的数据
    @ApiModelProperty("返回的数据")
    private T data;

    // //constructor
    // public AppRestResult() {
    //     this.timestamp=System.currentTimeMillis();
    // }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public AppRestResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
        // this.timestamp=System.currentTimeMillis();
    }

    //constructor
    public AppRestResult(Integer status, String msg, T data) {
        this.code = status;
        this.msg = msg;
        this.data = data;
        // this.timestamp=System.currentTimeMillis();
    }

    //返回成功数据
    public static AppRestResult success() {
        return new AppRestResult(AppRestResult.SUCCESS, AppRestResult.SUCCESS_MSG);
    }

    //返回成功数据
    public static<T> AppRestResult success(T data) {
        return new AppRestResult(AppRestResult.SUCCESS, AppRestResult.SUCCESS_MSG, data);
    }

    public AppRestResult success(String msg) {
        return new AppRestResult(AppRestResult.SUCCESS, msg, null);
    }

    public AppRestResult success(String msg, T data) {
        return new AppRestResult(AppRestResult.SUCCESS, msg, data);
    }

    //返回出错数据
    public static AppRestResult error(Integer code, String msg) {
        return new AppRestResult(code, msg, null);
    }
    public static AppRestResult error(String msg) {
        return new AppRestResult(500, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // public String getSignature() {
    //     return signature;
    // }
    // 0
    // public void setSignature(String signature) {
    //     this.signature = signature;
    // }
    //
    // public Long getTimestamp() {
    //     return timestamp;
    // }
    //
    // public void setTimestamp(Long timestamp) {
    //     this.timestamp = timestamp;
    // }

    @Override
    public String toString() {
        return "AppRestResult{" +
                   "code=" + code +
                   ", msg='" + msg + '\'' +
                   // ", signature='" + signature + '\'' +
                   // ", timestamp=" + timestamp +
                   ", data=" + data +
                   '}';
    }
}
