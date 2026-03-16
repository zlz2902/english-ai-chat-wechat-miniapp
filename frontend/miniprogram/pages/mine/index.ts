Page({
  data: {
    userInfo: {} as any,
    isLogin: false,
  },

  onShow() {
    const userInfo = wx.getStorageSync("userInfo");
    const token = wx.getStorageSync("token");

    if (token && userInfo) {
      this.setData({
        userInfo: userInfo,
        isLogin: true,
      });
    } else {
      this.setData({
        userInfo: { nickName: "点击登录" },
        isLogin: false,
      });
    }
  },

  handleEditProfile() {
    if (!this.data.isLogin) {
      wx.navigateTo({ url: "/pages/login/index" });
      return;
    }
    wx.showActionSheet({
      itemList: ["更换头像", "修改昵称"],
      success: (res) => {
        if (res.tapIndex === 0) {
          this.handleEditAvatar();
        } else if (res.tapIndex === 1) {
          wx.showModal({
            title: "修改昵称",
            editable: true,
            placeholderText: "输入新的昵称",
            success: (res) => {
              if (res.confirm && res.content) {
                const updatedUser = {
                  ...this.data.userInfo,
                  nickName: res.content,
                };
                this.setData({ userInfo: updatedUser });
                wx.setStorageSync("userInfo", updatedUser);
                wx.showToast({ title: "已更新", icon: "success" });
              }
            },
          });
        }
      },
    });
  },

  handleEditAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        const updatedUser = { ...this.data.userInfo, avatarUrl: tempFilePath };
        this.setData({ userInfo: updatedUser });
        wx.setStorageSync("userInfo", updatedUser);
        wx.showToast({ title: "头像已更新", icon: "success" });
      },
    });
  },

  handleAbout() {
    wx.showModal({
      title: "关于我们",
      content:
        "由李雨寰开发的智聊英语App，致力于为广大使用者提供一个学习的平台。",
      showCancel: false,
      confirmText: "知道了",
    });
  },

  handleChangePassword() {
    if (!this.data.isLogin) {
      wx.navigateTo({ url: "/pages/login/index" });
      return;
    }
    wx.showModal({
      title: "请输入旧密码",
      editable: true,
      placeholderText: "输入旧密码",
      confirmText: "下一步",
      success: (resOld) => {
        if (resOld.confirm && resOld.content) {
          const oldPwd = resOld.content;
          wx.showModal({
            title: "请输入新密码",
            editable: true,
            placeholderText: "输入新密码",
            confirmText: "提交",
            success: (resNew) => {
              if (resNew.confirm && resNew.content) {
                const newPwd = resNew.content;
                const url = `/system/user/profile/updatePwd?oldPassword=${encodeURIComponent(oldPwd)}&newPassword=${encodeURIComponent(newPwd)}`;
                // 使用通用请求：PUT + 查询参数
                const req = require("../../utils/request.js");
                const request = req.default || req;
                request({
                  url,
                  method: "PUT",
                })
                  .then(() => {
                    wx.showToast({ title: "修改成功", icon: "success" });
                  })
                  .catch((err: any) => {
                    console.error("updatePwd failed", err);
                  });
              }
            },
          });
        }
      },
    });
  },

  handleLogout() {
    wx.showModal({
      title: "提示",
      content: "确定要退出登录吗？",
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync("token");
          wx.removeStorageSync("userInfo");
          this.setData({
            userInfo: { nickName: "点击登录" },
            isLogin: false,
          });
          wx.reLaunch({
            url: "/pages/login/index",
          });
        }
      },
    });
  },

  navigateTo(e: any) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.navigateTo({ url });
    }
  },
});
