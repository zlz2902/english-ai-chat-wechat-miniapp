"use strict";
let request;
try {
  const reqModule = require("../../../utils/request.js");
  request = reqModule.default || reqModule;
} catch (e) {
  console.error("Failed to require request module:", e);
  request = (opts) => Promise.reject(new Error("Request module missing"));
}

Page({
  data: {
    scenarios: [],
  },

  onLoad() {
    this.getScenarios();
  },

  getScenarios() {
    request({
      url: "/business/lyh/scenario/open/list",
      method: "GET",
    })
      .then((res) => {
        const rows = res.rows || res || [];
        const scenarios = rows.map((item) => {
          const levelMap = {
            1: { label: "初级", value: "easy" },
            2: { label: "中级", value: "medium" },
            3: { label: "高级", value: "hard" },
          };
          const lv = levelMap[item.level] || levelMap["2"];
          return {
            id: item.scenarioId,
            title: item.title,
            image: item.imageUrl || "/assets/banner/banner01.jpg",
            level: lv.value,
            levelLabel: lv.label,
            description: item.description,
          };
        });
        this.setData({ scenarios });
      })
      .catch((err) => {
        console.error("Failed to load scenarios", err);
      });
  },

  enterScenario(e) {
    const id = Number(e.currentTarget.dataset.id);
    const title = e.currentTarget.dataset.title || "";
    const level = e.currentTarget.dataset.level || "medium";
    wx.navigateTo({
      url: `/pages/practice/scenario/session/index?id=${id}&title=${encodeURIComponent(
        title,
      )}&level=${level}`,
      fail: (err) => {
        console.error("navigateTo scenario session failed", err);
        wx.showToast({ title: "页面未注册或路径错误", icon: "none" });
      },
    });
  },
});
