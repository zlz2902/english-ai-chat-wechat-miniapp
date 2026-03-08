// pages/post/index.js
"use strict";

let request;
try {
  const reqModule = require("../../utils/request.js");
  request = reqModule.default || reqModule;
} catch (e) {
  console.error("Failed to require request module:", e);
  request = (opts) => Promise.reject(new Error("Request module missing"));
}

let constants;
let BASE_URL = "";
let TOKEN_KEY = "token";
try {
  constants = require("../../utils/constant.js");
  BASE_URL = constants.BASE_URL || "";
  TOKEN_KEY = constants.TOKEN_KEY || "token";
} catch (e) {
  console.warn("Failed to require constants module:", e);
}

Page({
  data: {
    content: "",
    imageUrl: "", // local preview path
    uploadedUrl: "", // server file path after upload
  },

  handleInput(e) {
    this.setData({
      content: e.detail.value,
    });
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: (res) => {
        if (res && res.tempFiles && res.tempFiles[0]) {
          const tempFilePath = res.tempFiles[0].tempFilePath;
          this.setData({
            imageUrl: tempFilePath,
          });
        }
      },
      fail: (err) => {
        console.error("chooseMedia failed", err);
        wx.showToast({ title: "选择图片失败", icon: "none" });
      },
    });
  },

  deleteImage() {
    this.setData({
      imageUrl: "",
      uploadedUrl: "",
    });
  },

  previewImage() {
    const src = this.data.imageUrl;
    if (src) {
      wx.previewImage({
        urls: [src],
      });
    }
  },

  uploadImage() {
    return new Promise((resolve, reject) => {
      const localPath = this.data.imageUrl;
      if (!localPath) {
        resolve("");
        return;
      }
      const token = wx.getStorageSync(TOKEN_KEY);
      wx.uploadFile({
        url: BASE_URL ? BASE_URL + "/common/upload" : "/common/upload",
        filePath: localPath,
        name: "file",
        header: {
          Authorization: token ? `Bearer ${token}` : "",
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data || "{}");
            if (data.code === 200) {
              resolve(data.fileName || data.url || "");
            } else {
              reject(new Error(data.msg || "Upload failed"));
            }
          } catch (e) {
            reject(new Error("Parse error"));
          }
        },
        fail: (err) => {
          reject(err);
        },
      });
    });
  },

  async submitPost() {
    if (!this.data.content && !this.data.imageUrl) {
      return;
    }

    wx.showLoading({ title: "Posting..." });
    try {
      let finalImageUrl = "";
      if (this.data.imageUrl) {
        finalImageUrl = await this.uploadImage();
      }
      await request({
        url: "/business/lyh/community/add",
        method: "POST",
        data: {
          content: this.data.content,
          images: finalImageUrl,
        },
      });
      wx.hideLoading();
      wx.showToast({ title: "Posted!", icon: "success" });
      setTimeout(() => {
        wx.navigateBack();
      }, 1200);
    } catch (err) {
      wx.hideLoading();
      console.error("Post failed", err);
      wx.showToast({
        title: (err && err.message) || "发布失败",
        icon: "none",
      });
    }
  },
});
