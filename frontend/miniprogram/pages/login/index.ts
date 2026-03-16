import request from "../../utils/request";
import { TOKEN_KEY, USER_INFO_KEY } from "../../utils/constant";

Page({
  data: {
    username: "",
    password: "",
    code: "",
    uuid: "",
    captchaImg: "",
    loading: false,
  },

  onLoad() {
    // const token = wx.getStorageSync(TOKEN_KEY)
    // if (token) {
    //   wx.switchTab({
    //     url: '/pages/index/index'
    //   })
    //   return;
    // }
    this.getCaptcha();
  },

  getCaptcha() {
    request<{ uuid: string; img: string }>({
      url: "/captchaImage",
      method: "GET",
    })
      .then((res) => {
        if (res.img) {
          this.setData({
            uuid: res.uuid,
            captchaImg: `data:image/png;base64,${res.img}`,
          });
        }
      })
      .catch((err) => {
        console.error("获取验证码失败", err);
      });
  },

  onUsernameInput(e: any) {
    this.setData({
      username: e.detail.value,
    });
  },

  onPasswordInput(e: any) {
    this.setData({
      password: e.detail.value,
    });
  },

  onCodeInput(e: any) {
    this.setData({
      code: e.detail.value,
    });
  },

  handleLogin() {
    const { username, password, code, uuid } = this.data;

    if (!username || !password || !code) {
      wx.showToast({
        title: "请填写完整信息",
        icon: "none",
      });
      return;
    }

    this.setData({ loading: true });
    wx.showLoading({ title: "登录中..." });

    request<{ token: string }>({
      url: "/login",
      method: "POST",
      data: {
        username,
        password,
        code,
        uuid,
      },
    })
      .then((res) => {
        wx.setStorageSync(TOKEN_KEY, res.token);

        // Get User Info
        return request<any>({
          url: "/getInfo",
          method: "GET",
        });
      })
      .then((userInfo) => {
        wx.setStorageSync(USER_INFO_KEY, userInfo.user);

        wx.showToast({
          title: "登录成功",
          icon: "success",
        });

        setTimeout(() => {
          wx.switchTab({
            url: "/pages/index/index",
          });
        }, 1500);
      })
      .catch((err) => {
        console.error(err);
        this.getCaptcha(); // Refresh captcha on failure
      })
      .then(() => {
        // finally
        this.setData({ loading: false });
        wx.hideLoading();
      });
  },
});
