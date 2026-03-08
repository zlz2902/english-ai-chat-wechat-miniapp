"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var constant_1 = require("./constant");
var request = function (options) {
    return new Promise(function (resolve, reject) {
        var token = wx.getStorageSync(constant_1.TOKEN_KEY);
        wx.request({
            url: "".concat(constant_1.BASE_URL).concat(options.url),
            method: options.method || "GET",
            data: options.data,
            header: Object.assign({ "Content-Type": "application/json", Authorization: token ? "Bearer ".concat(token) : "" }, (options.header || {})),
            success: function (res) {
                if (res.statusCode === 200) {
                    var _a = res.data, code = _a.code, msg = _a.msg, rest = __rest(_a, ["code", "msg"]);
                    if (code === 200) {
                        // 优先返回 data 字段，如果 data 不存在，则返回剩余字段（兼容 AjaxResult 和 AppRestResult）
                        if (res.data.data !== undefined && res.data.data !== null) {
                            resolve(res.data.data);
                        }
                        else {
                            resolve(rest);
                        }
                    }
                    else if (code === 401) {
                        wx.removeStorageSync(constant_1.TOKEN_KEY);
                        wx.navigateTo({ url: "/pages/login/index" });
                        reject(new Error("Unauthorized"));
                    }
                    else {
                        wx.showToast({
                            title: msg || "Error",
                            icon: "none",
                        });
                        reject(new Error(msg || "Error"));
                    }
                }
                else {
                    wx.showToast({
                        title: "HTTP Error: ".concat(res.statusCode),
                        icon: "none",
                    });
                    reject(new Error("HTTP Error: ".concat(res.statusCode)));
                }
            },
            fail: function (err) {
                wx.showToast({
                    title: "Network Error",
                    icon: "none",
                });
                reject(err);
            },
        });
    });
};
// Helper for object rest/spread
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
exports.default = request;
