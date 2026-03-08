"use strict";
let request;
try {
  const reqModule = require("../../utils/request.js");
  request = reqModule.default || reqModule;
} catch (e) {
  console.error("Failed to require request module:", e);
  request = (opts) => Promise.reject(new Error("Request module missing"));
}

Page({
  data: {
    questions: [],
    loading: false
  },

  onShow() {
    this.fetchFavorites();
  },

  fetchFavorites() {
    this.setData({ loading: true });
    request({
      url: "/business/lyh/favorite/questions",
      method: "GET"
    }).then(res => {
      // Backend returns List<LyhQuestion>
      // AppRestResult.success(list) -> data: list
      const list = Array.isArray(res) ? res : [];
      this.setData({
        questions: list,
        loading: false
      });
    }).catch(err => {
      console.error("Fetch favorites failed", err);
      this.setData({ loading: false });
    });
  },

  handleQuestionTap(e) {
    const qId = e.currentTarget.dataset.id;
    const q = this.data.questions.find(item => item.questionId === qId);
    if (q) {
        // Navigate to question bank to practice
        // Ideally we should pass qId to highlight or filter it
        // For now, simple redirect
        wx.navigateTo({
            url: `/pages/practice/question-bank/index?searchValue=${encodeURIComponent(q.topic || '')}`
        });
    }
  },

  handleUnfavorite(e) {
    const qId = e.currentTarget.dataset.id;
    // Don't use 'that'
    const self = this;
    wx.showModal({
      title: '取消收藏',
      content: '确定要取消收藏该题目吗？',
      success(res) {
        if (res.confirm) {
          request({
            url: "/business/lyh/favorite/toggle",
            method: "POST",
            data: {
              bizType: "1",
              targetId: qId
            }
          }).then(() => {
            wx.showToast({ title: '已取消', icon: 'success' });
            const newQuestions = self.data.questions.filter(q => q.questionId !== qId);
            self.setData({ questions: newQuestions });
          });
        }
      }
    });
  },
  
  goPractice() {
    wx.switchTab({
      url: '/pages/practice/index',
    });
  }
});
