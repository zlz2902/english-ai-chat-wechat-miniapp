"use strict";
const BASE_URL = "http://localhost:8089";
Page({
  data: {
    username: "",
    password: "",
    code: "",
    uuid: "",
    captchaImg: "",
    loading: false,
    captchaEnabled: true,
  },
  onLoad() {
    const token = wx.getStorageSync("token");
    if (token) {
      wx.switchTab({
        url: "/pages/index/index",
      });
      return;
    }
    this.getCaptcha();
  },
  getCaptcha() {
    console.log("正在请求验证码...");
    wx.request({
      url: BASE_URL + "/captchaImage",
      method: "GET",
      success: (res) => {
        console.log("验证码接口返回:", res);
        if (res.statusCode === 200 && res.data) {
          // 检查验证码开关
          if (
            res.data.captchaEnabled === false ||
            res.data.captchaEnabled === "false"
          ) {
            console.log("后端验证码已关闭");
            this.setData({ captchaEnabled: false });
            return;
          }
          if (res.data.code === 200 && res.data.img) {
            this.setData({
              uuid: res.data.uuid,
              captchaImg: "data:image/jpeg;base64," + res.data.img,
              captchaEnabled: true,
            });
          } else if (res.data.code !== 200) {
            console.error("验证码获取业务失败:", res.data);
            wx.showToast({
              title: res.data.msg || "验证码获取失败",
              icon: "none",
            });
          }
        } else {
          console.error("验证码接口异常:", res);
          wx.showToast({
            title: "服务器响应异常",
            icon: "none",
          });
        }
      },
      fail: (err) => {
        console.error("验证码请求失败:", err);
        // 如果验证码请求失败，但登录接口能通，可能是后端不需要验证码
        // 这里暂时不自动关闭，而是提示网络错误
        wx.showToast({
          title: "网络连接失败",
          icon: "none",
        });
      },
    });
  },
  onUsernameInput(e) {
    this.setData({
      username: e.detail.value,
    });
  },
  onPasswordInput(e) {
    this.setData({
      password: e.detail.value,
    });
  },
  onCodeInput(e) {
    this.setData({
      code: e.detail.value,
    });
  },
  handleLogin() {
    const { username, password, code, uuid, captchaEnabled } = this.data;
    if (!username || !password || (captchaEnabled && !code)) {
      wx.showToast({
        title: "请填写完整信息",
        icon: "none",
      });
      return;
    }
    this.setData({ loading: true });
    wx.showLoading({
      title: "登录中...",
    });
    wx.request({
      url: BASE_URL + "/login",
      method: "POST",
      header: {
        "Content-Type": "application/json",
      },
      data: {
        username,
        password,
        code,
        uuid,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data) {
          if (res.data.code === 200 && res.data.token) {
            const token = res.data.token;
            wx.setStorageSync("token", token);
            wx.request({
              url: BASE_URL + "/getInfo",
              method: "GET",
              header: {
                Authorization: "Bearer " + token,
              },
              success: (infoRes) => {
                if (
                  infoRes.statusCode === 200 &&
                  infoRes.data &&
                  infoRes.data.code === 200
                ) {
                  wx.setStorageSync("userInfo", infoRes.data.user);
                  wx.setStorageSync("roles", infoRes.data.roles || []);
                  wx.setStorageSync(
                    "permissions",
                    infoRes.data.permissions || [],
                  );
                }
                const user =
                  (infoRes && infoRes.data && infoRes.data.user) || {};
                if (user.status === "1" || user.status === 1) {
                  wx.removeStorageSync("token");
                  wx.showModal({
                    title: "提示",
                    content: "你的账号已被冻结，请联系管理员",
                    showCancel: false,
                  });
                  return;
                }
                wx.showToast({
                  title: "登录成功",
                  icon: "success",
                });
                const roles = infoRes.data.roles || [];
                const isAdmin = roles.includes("admin");
                setTimeout(() => {
                  if (isAdmin) {
                    wx.reLaunch({
                      url: "/pages/admin/index",
                    });
                  } else {
                    wx.switchTab({
                      url: "/pages/index/index",
                    });
                  }
                }, 1500);
              },
              fail: (e) => {
                console.error(e);
              },
            });
          } else {
            wx.showToast({
              title: res.data.msg || "登录失败",
              icon: "none",
            });
            this.getCaptcha();
          }
        } else {
          wx.showToast({
            title: "网络错误",
            icon: "none",
          });
        }
      },
      fail: (err) => {
        wx.showToast({
          title: "请求失败",
          icon: "none",
        });
        console.error(err);
      },
      complete: () => {
        this.setData({ loading: false });
        wx.hideLoading();
      },
    });
  },
});
