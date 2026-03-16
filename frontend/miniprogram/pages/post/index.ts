import request from "../../utils/request";
import { BASE_URL, TOKEN_KEY } from "../../utils/constant";

Page({
  data: {
    content: "",
    imageUrl: "", // Local path for preview
    uploadedUrl: "", // Server URL after upload
  },

  handleInput(e: any) {
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
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.setData({
          imageUrl: tempFilePath,
        });
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
    if (this.data.imageUrl) {
      wx.previewImage({
        urls: [this.data.imageUrl],
      });
    }
  },

  uploadImage(): Promise<string> {
    return new Promise((resolve, reject) => {
      if (!this.data.imageUrl) {
        resolve("");
        return;
      }

      const token = wx.getStorageSync(TOKEN_KEY);
      
      wx.uploadFile({
        url: `${BASE_URL}/common/upload`,
        filePath: this.data.imageUrl,
        name: "file",
        header: {
          Authorization: token ? `Bearer ${token}` : "",
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.fileName); // Return the relative path or full URL depending on backend
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
        // Upload image first
        finalImageUrl = await this.uploadImage();
      }

      // Submit post
      await request({
        url: "/business/lyh/community/add",
        method: "POST",
        data: {
          content: this.data.content,
          images: finalImageUrl, // Assuming backend accepts single image string or need to adjust
          // Backend LyhCommunity.java has "images" field as String
        },
      });

      wx.hideLoading();
      wx.showToast({
        title: "Posted!",
        icon: "success",
      });

      // Navigate back after delay
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);

    } catch (err: any) {
      wx.hideLoading();
      console.error("Post failed", err);
      wx.showToast({
        title: err.message || "Failed to post",
        icon: "none",
      });
    }
  },
});