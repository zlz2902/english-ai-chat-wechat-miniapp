// pages/practice/session/index.js
const request = require("../../../utils/request.js");
const { BASE_URL, TOKEN_KEY } = require("../../../utils/constant.js");

const recorder = wx.getRecorderManager();
let countdownTimer = null;
let innerAudioContext = null;

Page({
  data: {
    questionId: 0,
    questionText: "",
    topic: "",
    difficulty: "medium",
    difficultyLabel: "进阶",
    allowedTimeSec: 60,
    timeLeft: 60,
    
    isRecording: false,
    tempFilePath: "",
    durationSec: 0,
    audioUrl: "",
    
    isPlaying: false,
    uploading: false,
    score: null,
    showResult: false
  },

  onLoad(options) {
    const questionId = Number(options.id || 0);
    const text = decodeURIComponent(options.text || "");
    const diff = options.diff || "medium";
    const topic = decodeURIComponent(options.topic || "");
    
    const allowed = this.computeAllowedSeconds(diff);
    const label = this.getDifficultyLabel(diff);

    this.setData({
      questionId,
      questionText: text,
      difficulty: diff,
      difficultyLabel: label,
      topic,
      allowedTimeSec: allowed,
      timeLeft: allowed,
    });

    // 初始化录音监听
    recorder.onStop((res) => {
      const filePath = res.tempFilePath;
      const durationMs = res.duration || 0;
      const durationSec = Math.round(durationMs / 1000);
      
      this.setData({
        tempFilePath: filePath,
        durationSec,
        isRecording: false,
      });
      this.clearCountdown();
      
      // 自动上传
      this.uploadAudio(filePath);
    });

    recorder.onError((err) => {
      console.error("Recorder error:", err);
      this.setData({ isRecording: false });
      this.clearCountdown();
      wx.showToast({ title: "录音失败", icon: "none" });
    });
  },

  onUnload() {
    this.clearCountdown();
    if (innerAudioContext) {
      innerAudioContext.destroy();
    }
  },

  computeAllowedSeconds(diff) {
    if (diff === "easy") return 45;
    if (diff === "hard") return 90;
    return 60;
  },

  getDifficultyLabel(diff) {
    const map = {
      easy: "入门",
      medium: "进阶",
      hard: "困难"
    };
    return map[diff] || "进阶";
  },

  toggleRecording() {
    if (this.data.isRecording) {
      this.stopRecord();
    } else {
      this.startRecord();
    }
  },

  startRecord() {
    const allowed = this.data.allowedTimeSec;
    // 重置状态
    this.setData({ 
      isRecording: true, 
      timeLeft: allowed, 
      tempFilePath: "", 
      audioUrl: "", 
      durationSec: 0, 
      score: null,
      showResult: false
    });

    recorder.start({
      duration: allowed * 1000,
      sampleRate: 16000,
      format: "mp3",
      encodeBitRate: 48000,
      numberOfChannels: 1,
    });

    this.startCountdown();
  },

  stopRecord() {
    recorder.stop();
  },

  resetRecording() {
    this.setData({
      tempFilePath: "",
      audioUrl: "",
      durationSec: 0,
      score: null,
      showResult: false,
      timeLeft: this.data.allowedTimeSec
    });
  },

  startCountdown() {
    this.clearCountdown();
    countdownTimer = setInterval(() => {
      const r = this.data.timeLeft - 1;
      if (r <= 0) {
        this.clearCountdown();
      }
      this.setData({ timeLeft: Math.max(r, 0) });
    }, 1000);
  },

  clearCountdown() {
    if (countdownTimer) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  },

  uploadAudio(filePath) {
    wx.showLoading({ title: "处理中..." });
    const url = `${BASE_URL}/common/upload`;
    const token = wx.getStorageSync(TOKEN_KEY);
    
    wx.uploadFile({
      url,
      filePath,
      name: "file",
      header: {
        Authorization: token ? `Bearer ${token}` : ""
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          // 适配后端返回结构 { code, msg, url, fileName, newFileName }
          if (data.code === 200 && data.url) {
            this.setData({ audioUrl: data.url });
          } else {
            throw new Error(data.msg || "Upload failed");
          }
        } catch (e) {
          console.error("Upload parse error", e);
          wx.showToast({ title: "上传失败", icon: "none" });
        }
      },
      fail: (err) => {
        console.error("Upload failed", err);
        wx.showToast({ title: "网络错误", icon: "none" });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  playRecording() {
    if (this.data.isPlaying) {
      if (innerAudioContext) innerAudioContext.stop();
      return;
    }

    if (!this.data.audioUrl && !this.data.tempFilePath) return;

    innerAudioContext = wx.createInnerAudioContext();
    innerAudioContext.src = this.data.audioUrl || this.data.tempFilePath;
    innerAudioContext.play();
    
    this.setData({ isPlaying: true });

    innerAudioContext.onEnded(() => {
      this.setData({ isPlaying: false });
    });
    innerAudioContext.onStop(() => {
      this.setData({ isPlaying: false });
    });
    innerAudioContext.onError((res) => {
      this.setData({ isPlaying: false });
      wx.showToast({ title: "播放失败", icon: "none" });
    });
  },

  submitAnswer() {
    const { questionId, durationSec, allowedTimeSec, audioUrl, questionText } = this.data;
    
    if (!audioUrl) {
      wx.showToast({ title: "请等待录音上传", icon: "none" });
      return;
    }

    wx.showLoading({ title: "评分中..." });
    
    // 构造请求体
    const body = {
      questionId,
      durationSec,
      allowedTimeSec,
      answerText: "", // 暂无语音转文字
      audioUrl,
      remark: questionText.slice(0, 100)
    };

    // 使用 request 模块
    // 注意：request 模块默认导出可能是 module.exports 或 default
    const req = request.default || request;
    
    req({
      url: "/business/lyh/practice/record",
      method: "POST",
      data: body
    }).then(res => {
      // 兼容后端直接返回 data 或包装在 data 字段的情况
      const score = res.score !== undefined ? res.score : (res.data && res.data.score);
      
      if (score !== undefined) {
        this.setData({ 
          score,
          showResult: true
        });
      } else {
        throw new Error("Score missing");
      }
      wx.hideLoading();
    }).catch(err => {
      console.error("Submit failed", err);
      wx.hideLoading();
      wx.showToast({ title: "评分失败", icon: "none" });
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
