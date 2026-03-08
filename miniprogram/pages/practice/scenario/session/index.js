"use strict";
let request;
try {
  const reqModule = require("../../../../utils/request.js");
  request = reqModule.default || reqModule;
} catch (e) {
  console.error("Failed to require request module:", e);
  request = (opts) => Promise.reject(new Error("Request module missing"));
}

Page({
  data: {
    scenarioId: 0,
    title: "",
    level: "medium",
    role: "user",
    personaName: "",
    personaPrompt: "",
    options: [],
    history: [],
    toView: "",
    inputValue: ""
  },

  onLoad(options) {
    const scenarioId = Number(options.id || 0);
    const title = decodeURIComponent(options.title || "");
    const level = options.level || "medium";
    this.setData({ scenarioId, title, level });
    this.loadScript(scenarioId);
  },

  loadScript(id) {
    request({
      url: `/business/lyh/scenario/open/${id}`,
      method: "GET",
    })
      .then((res) => {
        const data = res.data || res; // Handle different response wrappers
        let script = [];
        try {
          if (data.scriptJson) {
            script = JSON.parse(data.scriptJson);
          }
        } catch (e) {
          console.error("Parse scriptJson error", e);
        }

        const first = script[0] || { 
          speaker: "Agent", 
          text: "Hello! I am ready to practice with you.", 
          options: [] 
        };
        
        this.setData({
          personaName: data.personaName || "Assistant",
          personaPrompt: data.personaPrompt || "You are a helpful assistant.",
          options: first.options || [],
          history: [{ speaker: first.speaker || "Agent", text: first.text || "" }],
          toView: "msg-0"
        });
      })
      .catch((err) => {
        console.error("Load scenario failed", err);
        // Fallback
        this.setData({
          personaName: "Assistant",
          history: [{ speaker: "Agent", text: "Hello! How can I help you today?" }],
          toView: "msg-0"
        });
      });
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value });
  },

  onSend() {
    const text = this.data.inputValue.trim();
    if (!text) return;
    this.sendMessage(text);
    this.setData({ inputValue: "" });
  },

  chooseOption(e) {
    const text = e.currentTarget.dataset.text;
    if (text) {
      this.sendMessage(text);
      // Optional: Clear options after selection to enter free chat mode
      this.setData({ options: [] });
    }
  },

  sendMessage(text) {
    // 1. Add User Message
    const hist = this.data.history.slice();
    hist.push({ speaker: "You", text: text });
    
    this.setData({ 
      history: hist, 
      toView: `msg-${hist.length - 1}` 
    });

    // 2. Call Backend API
    const reqBody = {
      scenarioId: this.data.scenarioId,
      message: text,
      history: hist.map(h => ({
        role: h.speaker === "You" ? "user" : "assistant", // Map UI speaker to API role
        content: h.text
      }))
    };

    request({
      url: "/business/lyh/chat/completions",
      method: "POST",
      data: reqBody
    })
    .then(res => {
      // res is usually the payload directly if request.js unwraps it
      const content = (typeof res === 'string') ? res : (res.data || res.msg || "Error parsing response");

      const newHist = this.data.history.slice();
      newHist.push({ speaker: this.data.personaName || "Agent", text: content });
      
      this.setData({
        history: newHist,
        toView: `msg-${newHist.length - 1}`
      });
    })
    .catch(err => {
      console.error("Chat API error", err);
      const newHist = this.data.history.slice();
      newHist.push({ speaker: "System", text: "Network error, please try again." });
      this.setData({
        history: newHist,
        toView: `msg-${newHist.length - 1}`
      });
    });
  },
  
  goBack() {
    wx.navigateBack();
  }
});
