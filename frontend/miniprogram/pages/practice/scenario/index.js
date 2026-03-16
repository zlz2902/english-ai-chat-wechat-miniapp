"use strict";
let request;
let BASE_URL = "";
try {
  const reqModule = require("../../../utils/request.js");
  request = reqModule.default || reqModule;
} catch (e) {
  console.error("Failed to require request module:", e);
  request = (opts) => Promise.reject(new Error("Request module missing"));
}
try {
  const constants = require("../../../utils/constant.js");
  BASE_URL = (constants && (constants.BASE_URL || constants.default?.BASE_URL)) || "";
} catch (e) {
  console.warn("Failed to load BASE_URL, images without absolute URL may fail.", e);
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
          const rawImg = item.imageUrl || "";
          let image = "/assets/banner/banner01.jpg";
          if (rawImg) {
            image = rawImg.startsWith("http")
              ? rawImg
              : (BASE_URL ? `${BASE_URL}${encodeURI(rawImg)}` : rawImg);
          }
          return {
            id: item.scenarioId,
            title: item.title,
            image,
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
