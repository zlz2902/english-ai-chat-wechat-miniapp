import request from "../../utils/request";
import { TOKEN_KEY, BASE_URL } from "../../utils/constant";

type FeedItem = {
  id: number | string;
  name: string;
  avatar: string;
  time: string;
  content: string;
  image?: string | null;
  likes: number;
  comments: number;
  hasLiked: boolean;
};

Page({
  data: {
    currentDate: "",
    isCheckedIn: false,
    dailyWord: {
      word: "Loading...",
      pronunciation: "",
      definition: "正在获取数据...",
    },
    features: [
      {
        id: 1,
        name: "场景模拟\nScenario",
        icon: "/assets/tabbar/work.png",
        url: "/pages/practice/index",
      },
      {
        id: 2,
        name: "AI 陪练\nAI Coach",
        icon: "/assets/tabbar/mine.png",
        url: "/pages/ai-coach/index",
      },
    ],
    feedList: [] as FeedItem[],
  },

  onLoad() {
    this.updateDate();
    this.getDailyWord();
    this.checkCheckinStatus();
    this.getFeedList();
  },

  onShow() {
    const token = wx.getStorageSync(TOKEN_KEY);
    if (!token) {
      wx.reLaunch({
        url: "/pages/login/index",
      });
      return;
    }
    // 每次显示页面时刷新社区动态，确保看到最新的头像和昵称
    this.getFeedList();
    this.checkCheckinStatus();
  },

  updateDate() {
    const now = new Date();
    const options: Intl.DateTimeFormatOptions = {
      month: "long",
      day: "numeric",
      weekday: "long",
    };
    const dateStr = now.toLocaleDateString("en-US", options);
    const cnDateStr = now.toLocaleDateString("zh-CN", {
      month: "long",
      day: "numeric",
      weekday: "long",
    });
    this.setData({
      currentDate: `${cnDateStr} / ${dateStr}`,
    });
  },

  getDailyWord() {
    // 本地单词库（7个单词，轮播显示）
    const localWords = [
      {
        word: "Perseverance",
        pronunciation: "/ˌpɜːrsəˈvɪrəns/",
        definition:
          "Persistence in doing something despite difficulty or delay in achieving success.",
      },
      {
        word: "Innovation",
        pronunciation: "/ˌɪnəˈveɪʃn/",
        definition:
          "The action or process of innovating; a new method, idea, product, etc.",
      },
      {
        word: "Integrity",
        pronunciation: "/ɪnˈtɛɡrəti/",
        definition:
          "The quality of being honest and having strong moral principles.",
      },
      {
        word: "Resilience",
        pronunciation: "/rɪˈzɪliəns/",
        definition:
          "The capacity to recover quickly from difficulties; toughness.",
      },
      {
        word: "Ambition",
        pronunciation: "/æmˈbɪʃn/",
        definition:
          "A strong desire to do or to achieve something, typically requiring determination and hard work.",
      },
      {
        word: "Gratitude",
        pronunciation: "/ˈɡrætɪtuːd/",
        definition:
          "The quality of being thankful; readiness to show appreciation for and to return kindness.",
      },
      {
        word: "Courage",
        pronunciation: "/ˈkɜːrɪdʒ/",
        definition: "The ability to do something that frightens one.",
      },
    ];

    // 根据日期选择单词 (1号选第1个，2号选第2个，7号选第7个，8号又回到第1个...)
    const day = new Date().getDate();
    const index = (day - 1) % localWords.length;
    const todayWord = localWords[index];

    request<any>({
      url: "/business/lyh/dailyWord/latest",
      method: "GET",
    })
      .then((res) => {
        if (res && res.word) {
          // 如果后端有数据，优先用后端
          this.setData({
            dailyWord: {
              word: res.word,
              pronunciation: res.pronunciation || "",
              definition: res.definition || "No definition available.",
            },
          });
        } else {
          // 后端无数据，使用本地轮播单词
          this.setData({
            dailyWord: todayWord,
          });
        }
      })
      .catch((err) => {
        console.error("Failed to load daily word, using local fallback", err);
        // 请求失败，使用本地轮播单词
        this.setData({
          dailyWord: todayWord,
        });
      });
  },

  checkCheckinStatus() {
    request<boolean>({
      url: "/business/lyh/checkin/status",
      method: "GET",
    })
      .then((res) => {
        this.setData({
          isCheckedIn: !!res,
        });
      })
      .catch((err) => {
        console.error("Failed to check checkin status", err);
      });
  },

  handleCheckin() {
    if (this.data.isCheckedIn) {
      return;
    }

    wx.showLoading({ title: "打卡中..." });

    request<any>({
      url: "/business/lyh/checkin/now",
      method: "POST",
    })
      .then((res) => {
        wx.hideLoading();
        wx.showToast({
          title: "打卡成功",
          icon: "success",
        });
        this.setData({
          isCheckedIn: true,
        });
      })
      .catch((err) => {
        wx.hideLoading();
        console.error("Checkin failed", err);
        wx.showToast({
          title: err.msg || "打卡失败",
          icon: "none",
        });
      });
  },

  getFeedList() {
    request<any>({
      url: "/business/lyh/community/list",
      method: "GET",
    })
      .then((res) => {
        if (res && res.length > 0) {
          const formattedList: FeedItem[] = res.map((item: any) => ({
            ...item,
            id: item.postId,
            name: item.nickName || "Unknown User",
            avatar: item.avatarUrl
              ? item.avatarUrl.startsWith("http") ||
                item.avatarUrl.startsWith("/assets")
                ? item.avatarUrl
                : BASE_URL + item.avatarUrl
              : "/assets/profile.jpg",
            time: item.timeStr || item.createTime,
            content: item.content,
            image: item.images
              ? item.images.startsWith("http")
                ? item.images
                : BASE_URL + item.images
              : null,
            likes: item.likesCount || 0,
            comments: item.commentsCount || 0,
            hasLiked: item.hasLiked || false,
          }));
          this.setData({
            feedList: formattedList,
          });
        } else {
          this.setData({ feedList: [] });
        }
      })
      .catch((err) => {
        console.error("Failed to load feed", err);
        this.setData({ feedList: [] });
      });
  },

  navigateToPractice() {
    wx.switchTab({
      url: "/pages/practice/index",
    });
  },

  navigateToFeature(e: any) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.switchTab({
        url: url,
      });
    }
  },

  onPullDownRefresh() {
    this.getFeedList();
    setTimeout(() => {
      wx.stopPullDownRefresh();
      wx.showToast({
        title: "Refreshed",
        icon: "none",
      });
    }, 1000);
  },

  handlePost() {
    const userInfo = wx.getStorageSync("userInfo");
    if (!userInfo) {
      wx.showToast({
        title: "Please login first",
        icon: "none",
      });
      setTimeout(() => {
        wx.switchTab({ url: "/pages/mine/index" });
      }, 1500);
      return;
    }

    wx.navigateTo({
      url: "/pages/post/index",
    });
  },

  handleLike(e: any) {
    const id = e.currentTarget.dataset.id;
    // Optimistic update for better UI response
    const feedList = this.data.feedList.map((item: any) => {
      if (item.id === id) {
        const hasLiked = !item.hasLiked;
        return {
          ...item,
          hasLiked: hasLiked,
          likes: hasLiked ? item.likes + 1 : item.likes - 1,
        };
      }
      return item;
    });

    this.setData({
      feedList: feedList,
    });

    request({
      url: `/business/lyh/community/like/${id}`,
      method: "POST",
    }).catch((err) => {
      console.error("Like failed", err);
    });
  },

  handleComment(e: any) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: "Add Comment",
      editable: true,
      placeholderText: "Write a comment...",
      success: (res) => {
        if (res.confirm && res.content) {
          request({
            url: `/business/lyh/community/comment/${id}`,
            method: "POST",
            data: {
              content: res.content,
            },
          })
            .then(() => {
              wx.showToast({
                title: "Comment added",
                icon: "success",
              });
              this.getFeedList(); // Refresh list
            })
            .catch((err) => {
              wx.showToast({
                title: "Failed to comment",
                icon: "none",
              });
            });
        }
      },
    });
  },

  previewImage(e: any) {
    const src = e.currentTarget.dataset.src;
    wx.previewImage({
      current: src,
      urls: [src],
    });
  },
});
