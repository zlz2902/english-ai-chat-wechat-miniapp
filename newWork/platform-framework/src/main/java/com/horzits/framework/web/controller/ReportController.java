package com.horzits.framework.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/system/jeecg")
public class ReportController {

    @PreAuthorize("@ss.hasPermi('system:jeecg:index')")
    @GetMapping(value = "/getReport")
    public String getReport(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseUrl = scheme + "://" + serverName + ":" + serverPort;
        return baseUrl+"/jmreport/list";
    }

}