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
    currentTab: 0,
    categories: [],
    questionsRaw: [],
    questions: [],
    searchKeyword: "",
    difficulties: [
      { key: "all", label: "全部" },
      { key: "easy", label: "入门" },
      { key: "medium", label: "进阶" },
      { key: "hard", label: "高级" },
    ],
    selectedDifficulty: "all",
    playingId: null,
    favorites: {},
  },

  onLoad() {
    this.fetchFavoritesFromServer().finally(() => {
      this.getCategories();
    });
  },

  fetchFavoritesFromServer() {
    return request({
      url: "/business/lyh/favorite/list",
      method: "GET",
      data: { bizType: "1" },
    })
      .then((ids) => {
        const map = {};
        (ids || []).forEach((id) => {
          map[String(id)] = true;
        });
        this.setData({ favorites: map }, () => this.applyFavToQuestions());
      })
      .catch(() => {
        // 忽略错误：未登录或网络问题时不阻塞页面
      });
  },

  getCategories() {
    request({
      url: "/business/lyh/practiceCategory/open/list",
      method: "GET",
    })
      .then((res) => {
        const rows = res.rows || res || [];
        const categories = [
          { id: 0, name: "全部" },
          ...rows.map((item) => ({
            id: item.categoryId,
            name: item.categoryName,
          })),
        ];
        this.setData({ categories });
        this.getQuestions();
      })
      .catch((err) => {
        console.error("Failed to load categories", err);
      });
  },

  getQuestions(categoryId) {
    const params = {};
    if (categoryId && categoryId !== 0) {
      params.categoryId = categoryId;
    }
    request({
      url: "/business/lyh/question/open/list",
      method: "GET",
      data: params,
    })
      .then((res) => {
        const rows = res.rows || res || [];
        const fav = this.data.favorites || {};
        const questions = rows.map((item) => {
          const diff = this.mapDifficulty(item.difficulty);
          return {
            id: item.questionId,
            text: item.questionText,
            topic: item.topic,
            difficulty: diff.value,
            difficultyLabel: diff.label,
            audio: item.audioUrl || item.audio_url || "",
            isFav: !!fav[String(item.questionId)],
          };
        });
        this.setData(
          {
            questionsRaw: questions,
            selectedDifficulty: "all",
          },
          () => this.applyFilterAndLimit(),
        );
      })
      .catch((err) => {
        console.error("Failed to load questions", err);
      });
  },

  applyFavToQuestions() {
    const fav = this.data.favorites || {};
    const src = this.data.questionsRaw || [];
    if (!src.length) return;
    const questionsRaw = src.map((q) =>
      Object.assign({}, q, { isFav: !!fav[String(q.id)] }),
    );
    this.setData({ questionsRaw }, () => this.applyFilterAndLimit());
  },

  mapDifficulty(val) {
    const map = {
      "1": { label: "入门", value: "easy" },
      "2": { label: "进阶", value: "medium" },
      "3": { label: "高级", value: "hard" },
    };
    return map[val] || { label: "未知", value: "medium" };
  },

  onTabClick(e) {
    const index = e.currentTarget.dataset.index;
    const category = this.data.categories[index];
    this.setData({ currentTab: index });
    this.getQuestions(category.id);
  },

  onPractice(e) {
    const id = Number(e.currentTarget.dataset.id);
    const text = e.currentTarget.dataset.text || "";
    const diff = e.currentTarget.dataset.difficulty || "medium";
    wx.navigateTo({
      url: `/pages/practice/session/index?id=${id}&text=${encodeURIComponent(text)}&diff=${diff}`,
      fail: (err) => {
        console.error("navigateTo session failed", err);
        wx.showToast({ title: "无法进入练习，请重新编译项目后重试", icon: "none" });
      },
    });
  },

  onFilterDifficulty(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({ selectedDifficulty: key }, () => this.applyFilterAndLimit());
  },

  applyFilterAndLimit() {
    const questionsRaw = this.data.questionsRaw || [];
    const selectedDifficulty = this.data.selectedDifficulty || "all";
    const keyword = (this.data.searchKeyword || "").trim().toLowerCase();
    let list = questionsRaw;
    if (selectedDifficulty !== "all") {
      list = list.filter((q) => q.difficulty === selectedDifficulty);
    }
    if (keyword) {
      list = list.filter((q) => {
        return (
          (q.text && q.text.toLowerCase().includes(keyword)) ||
          (q.topic && q.topic.toLowerCase().includes(keyword))
        );
      });
    }
    this.setData({ questions: list });
  },

  onSearchInput(e) {
    const v = e.detail.value || "";
    this.setData({ searchKeyword: v }, () => this.applyFilterAndLimit());
  },

  onSearchConfirm() {
    this.applyFilterAndLimit();
  },

  clearSearch() {
    this.setData({ searchKeyword: "" }, () => this.applyFilterAndLimit());
  },

  toggleFavorite(e) {
    const id = String(e.currentTarget.dataset.id);
    // 乐观更新
    const srcList = this.data.questionsRaw || [];
    const current = srcList.find((q) => String(q.id) === id);
    const optimistic = !(current && current.isFav);
    const questionsRawOptimistic = srcList.map((q) =>
      String(q.id) === id ? Object.assign({}, q, { isFav: optimistic }) : q,
    );
    this.setData({ questionsRaw: questionsRawOptimistic }, () => this.applyFilterAndLimit());

    request({
      url: "/business/lyh/favorite/toggle",
      method: "POST",
      data: { bizType: "1", targetId: Number(id) },
    })
      .then((res) => {
        const favorited = !!(res && (res.favorited === true || res.favorited === "true"));
        const favMap = Object.assign({}, this.data.favorites || {});
        if (favorited) {
          favMap[id] = true;
        } else {
          delete favMap[id];
        }
        const src = this.data.questionsRaw || [];
        const questionsRaw = src.map((q) =>
          String(q.id) === id ? Object.assign({}, q, { isFav: favorited }) : q,
        );
        this.setData({ favorites: favMap, questionsRaw }, () => this.applyFilterAndLimit());
      })
      .catch(() => {
        // 回滚
        const src = this.data.questionsRaw || [];
        const questionsRaw = src.map((q) =>
          String(q.id) === id ? Object.assign({}, q, { isFav: !optimistic }) : q,
        );
        this.setData({ questionsRaw }, () => this.applyFilterAndLimit());
      });
  },

  playAudio(e) {
    const id = e.currentTarget.dataset.id;
    const src = e.currentTarget.dataset.audio;
    if (!src) return;
    const ctx = wx.createInnerAudioContext();
    ctx.autoplay = true;
    ctx.src = src;
    this.setData({ playingId: id });
    ctx.onEnded(() => {
      this.setData({ playingId: null });
      ctx.destroy();
    });
    ctx.onStop(() => {
      this.setData({ playingId: null });
      ctx.destroy();
    });
    ctx.onError(() => {
      this.setData({ playingId: null });
      ctx.destroy();
    });
  },
});
