package com.horzits.business.domain.vo;

import com.horzits.common.annotation.Excel;



public class ksscexcel {
    @Excel(name = "账号")
    private String useracc;

    public String getUseracc() {
        return useracc;
    }

    public void setUseracc(String useracc) {
        this.useracc = useracc;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Excel(name = "密码")
    private String passwd;
    @Excel(name = "结果")
    private String result;
}
