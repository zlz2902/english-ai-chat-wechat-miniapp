"use strict";
// 强制重新加载
console.log("Index Page Loading - Version 2026-02-19-Backend-Connected");

let request;
try {
  const reqModule = require("../../utils/request.js");
  request = reqModule.default || reqModule;
} catch (e) {
  console.error("Failed to require request module:", e);
  request = (opts) => Promise.reject(new Error("Request module missing"));
}

let BASE_URL = "";
try {
  const constants = require("../../utils/constant.js");
  BASE_URL = constants.BASE_URL || "";
} catch (e) {
  console.warn("Failed to require constants:", e);
}

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
    feedList: [
      {
        id: 1,
        name: "Alex Johnson",
        avatar: "/assets/profile.jpg",
        time: "2h ago",
        content:
          "Just finished my daily practice! The AI feedback is super helpful.",
        likes: 12,
        comments: 3,
        hasLiked: false,
      },
      {
        id: 2,
        name: "Sarah Lee",
        avatar: "/assets/profile.jpg",
        time: "5h ago",
        content: "Learned some great idioms for business meetings today.",
        likes: 24,
        comments: 5,
        hasLiked: false,
      },
      {
        id: 3,
        name: "Mike Chen",
        avatar: "/assets/profile.jpg",
        time: "1d ago",
        content: "Day 30 streak! Consistency is key.",
        likes: 45,
        comments: 8,
        hasLiked: false,
      },
    ],
  },

  onLoad() {
    console.log("Index Page onLoad");
    this.updateDate();
    this.getDailyWord();
    this.checkCheckinStatus();
    this.getFeedList();
  },

  updateDate() {
    const now = new Date();
    const options = {
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

    request({
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
    request({
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

    request({
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
    request({
      url: "/business/lyh/community/list",
      method: "GET",
    })
      .then((res) => {
        // Handle TableDataInfo format (res.rows) or direct list
        const list = res.rows || res;
        
        if (list && list.length > 0) {
          const formattedList = list.map((item) => {
            console.log(
              "Processing item:",
              item.postId,
              "Avatar:",
              item.avatarUrl,
              "Images:",
              item.images,
            );
            
            // Handle avatar URL
            let avatar = item.avatarUrl;
            if (avatar && !avatar.startsWith("http") && !avatar.startsWith("/assets")) {
               avatar = BASE_URL + avatar;
            } else if (!avatar) {
               avatar = "/assets/profile.jpg";
            }
            
            // Handle post image URL
            let image = null;
            if (item.images) {
               if (item.images.startsWith("http") || item.images.startsWith("/assets")) {
                  image = item.images;
               } else {
                  image = BASE_URL + item.images;
               }
            }
            
            return {
              id: item.postId,
              name: item.nickName || "Unknown User",
              avatar: avatar,
              time: item.timeStr || item.createTime,
              content: item.content,
              image: image,
              likes: item.likesCount || 0,
              comments: item.commentsCount || 0,
              hasLiked: item.hasLiked || false, // Check boolean
            };
          });
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

  navigateToFeature(e) {
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

  handleLike(e) {
    const id = e.currentTarget.dataset.id;
    // Optimistic update for better UI response
    const feedList = this.data.feedList.map((item) => {
      if (item.id === id) {
        const hasLiked = !item.hasLiked;
        return Object.assign({}, item, {
          hasLiked: hasLiked,
          likes: hasLiked ? item.likes + 1 : item.likes - 1,
        });
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

  handleComment(e) {
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

  previewImage(e) {
    const src = e.currentTarget.dataset.src;
    wx.previewImage({
      current: src,
      urls: [src],
    });
  },
});
