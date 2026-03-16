import request from "../../../utils/request";
import { BASE_URL } from "../../../utils/constant";

const recorder = wx.getRecorderManager();
let countdownTimer: number | null = null;

Page({
  data: {
    questionId: 0,
    text: "",
    diff: "medium",
    allowedTimeSec: 60,
    remainingSec: 60,
    recording: false,
    tempFilePath: "",
    durationSec: 0,
    uploading: false,
    score: null as number | null,
  },

  onLoad(options: any) {
    const questionId = Number(options.id || 0);
    const text = decodeURIComponent(options.text || "");
    const diff = options.diff || "medium";
    const allowed = this.computeAllowedSeconds(diff);
    this.setData({
      questionId,
      text,
      diff,
      allowedTimeSec: allowed,
      remainingSec: allowed,
    });
    recorder.onStop((res: any) => {
      const filePath = res.tempFilePath;
      const durationMs = res.duration || 0;
      const durationSec = Math.round(durationMs / 1000);
      this.setData({
        tempFilePath: filePath,
        durationSec,
        recording: false,
      });
      this.clearCountdown();
    });
    recorder.onError(() => {
      this.setData({ recording: false });
      this.clearCountdown();
    });
  },

  computeAllowedSeconds(diff: string) {
    if (diff === "easy") return 45;
    if (diff === "hard") return 90;
    return 60;
  },

  startRecord() {
    if (this.data.recording) return;
    const allowed = this.data.allowedTimeSec;
    this.setData({ recording: true, remainingSec: allowed, tempFilePath: "", durationSec: 0, score: null });
    recorder.start({
      duration: allowed * 1000,
      sampleRate: 16000,
      format: "mp3",
      encodeBitRate: 48000,
      numberOfChannels: 1,
    } as any);
    this.startCountdown();
  },

  stopRecord() {
    if (!this.data.recording) return;
    recorder.stop();
  },

  startCountdown() {
    this.clearCountdown();
    countdownTimer = setInterval(() => {
      const r = this.data.remainingSec - 1;
      if (r <= 0) {
        this.clearCountdown();
      }
      this.setData({ remainingSec: Math.max(r, 0) });
    }, 1000) as any;
  },

  clearCountdown() {
    if (countdownTimer) {
      clearInterval(countdownTimer as any);
      countdownTimer = null;
    }
  },

  async submitResult() {
    const { tempFilePath, questionId, durationSec, allowedTimeSec, text } = this.data as any;
    if (!tempFilePath || !questionId) {
      wx.showToast({ title: "请先录音", icon: "none" });
      return;
    }
    this.setData({ uploading: true });
    try {
      const uploadRes: any = await this.uploadAudio(tempFilePath);
      const audioUrl = uploadRes.url || uploadRes.data?.url || "";
      const body = {
        questionId,
        durationSec,
        allowedTimeSec,
        answerText: "",
        audioUrl,
        remark: text.slice(0, 100),
      };
      const resp: any = await request({
        url: "/business/lyh/practice/record",
        method: "POST",
        data: body,
      });
      const score = resp?.score ?? null;
      this.setData({ score });
      wx.showToast({ title: `得分 ${score}`, icon: "none" });
    } catch (e) {
      wx.showToast({ title: "提交失败", icon: "none" });
    } finally {
      this.setData({ uploading: false });
    }
  },

  uploadAudio(filePath: string) {
    const url = `${BASE_URL}/common/upload`;
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url,
        filePath,
        name: "file",
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            resolve(data);
          } catch (e) {
            resolve(res);
          }
        },
        fail: reject,
      });
    });
  },
});
