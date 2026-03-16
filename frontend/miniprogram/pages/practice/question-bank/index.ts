import request from "../../../utils/request";

Page({
  data: {
    currentTab: 0,
    categories: [] as any[],
    questionsRaw: [] as any[],
    questions: [] as any[],
    difficulties: [
      { key: "all", label: "全部" },
      { key: "easy", label: "入门" },
      { key: "medium", label: "进阶" },
      { key: "hard", label: "困难" },
    ],
    selectedDifficulty: "all",
    quickLimit: 0,
    playingId: null as number | null,
  },

  onLoad() {
    this.getCategories();
  },

  getCategories() {
    request<{ rows: any[] }>({
      url: "/business/lyh/practiceCategory/open/list",
      method: "GET",
    })
      .then((res) => {
        const categories = [
          { id: 0, name: "全部" },
          ...res.rows.map((item: any) => ({
            id: item.categoryId,
            name: item.categoryName,
          })),
        ];

        this.setData({
          categories: categories,
        });

        this.getQuestions();
      })
      .catch((err) => {
        console.error("Failed to load categories", err);
      });
  },

  getQuestions(categoryId?: number) {
    const params: any = {};
    if (categoryId && categoryId !== 0) {
      params.categoryId = categoryId;
    }

    request<{ rows: any[] }>({
      url: "/business/lyh/question/open/list",
      method: "GET",
      data: params,
    })
      .then((res) => {
        const questions = res.rows.map((item: any) => ({
          id: item.questionId,
          text: item.questionText,
          topic: item.topic,
          difficulty: this.mapDifficulty(item.difficulty).value,
          difficultyLabel: this.mapDifficulty(item.difficulty).label,
          audio: item.audioUrl || item.audio_url || "",
        }));

        this.setData(
          {
            questionsRaw: questions,
            selectedDifficulty: "all",
            quickLimit: 0,
          },
          () => this.applyFilterAndLimit(),
        );
      })
      .catch((err) => {
        console.error("Failed to load questions", err);
      });
  },

  mapDifficulty(val: string) {
    const map: any = {
      "1": { label: "入门", value: "easy" },
      "2": { label: "进阶", value: "medium" },
      "3": { label: "困难", value: "hard" },
    };
    return map[val] || { label: "未知", value: "medium" };
  },

  onTabClick(e: any) {
    const index = e.currentTarget.dataset.index;
    const category = this.data.categories[index];
    this.setData({
      currentTab: index,
    });
    this.getQuestions(category.id);
  },

  onPractice(e: any) {
    const id = Number(e.currentTarget.dataset.id);
    const text = e.currentTarget.dataset.text || "";
    const diff = e.currentTarget.dataset.difficulty || "medium";
    wx.navigateTo({
      url: `/pages/practice/session/index?id=${id}&text=${encodeURIComponent(text)}&diff=${diff}`,
      fail: (err) => {
        console.error("navigateTo session failed", err);
        wx.showToast({
          title: "无法进入练习，请重新编译项目后重试",
          icon: "none",
        });
      },
      success: () => {
        // no-op
      },
    });
  },

  onFilterDifficulty(e: any) {
    const key = e.currentTarget.dataset.key;
    this.setData({ selectedDifficulty: key, quickLimit: 0 }, () =>
      this.applyFilterAndLimit(),
    );
  },

  startQuickPractice(e: any) {
    const count = Number(e.currentTarget.dataset.count) || 0;
    this.setData({ quickLimit: count }, () => this.applyFilterAndLimit());
  },

  applyFilterAndLimit() {
    const { questionsRaw, selectedDifficulty, quickLimit } = this.data as any;
    let list = questionsRaw;
    if (selectedDifficulty !== "all") {
      list = list.filter((q: any) => q.difficulty === selectedDifficulty);
    }
    if (quickLimit && quickLimit > 0) {
      list = list.slice(0, quickLimit);
    }
    this.setData({ questions: list });
  },

  playAudio(e: any) {
    const id = e.currentTarget.dataset.id;
    const src = e.currentTarget.dataset.audio;
    if (!src) {
      return;
    }
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
