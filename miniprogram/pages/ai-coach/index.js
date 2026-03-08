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
        messages: [
            { 
              id: 1, 
              type: 'ai', 
              content: 'Hello! I am your AI English coach. I can help you correct your grammar, suggest better vocabulary, or answer your English questions. Try sending me a sentence!' 
            }
        ],
        inputValue: '',
        scrollIntoView: '',
        loading: false
    },

    onInput(e) {
        this.setData({
            inputValue: e.detail.value
        });
    },

    sendMessage() {
        const content = this.data.inputValue.trim();
        if (!content || this.data.loading) return;

        // 1. Add User Message
        const userMsg = {
            id: Date.now(),
            type: 'user',
            content: content
        };
        
        const messages = this.data.messages;
        messages.push(userMsg);

        this.setData({
            messages: messages,
            inputValue: '',
            scrollIntoView: `msg-${messages.length - 1}`,
            loading: true
        });

        // 2. Prepare History for Context (Optional: limit to last 10 messages)
        const history = messages.slice(-10).map(msg => ({
            role: msg.type === 'user' ? 'user' : 'assistant',
            content: msg.content
        }));

        // 3. Call Backend API
        request({
            url: "/business/lyh/chat/coach",
            method: "POST",
            data: {
                message: content,
                history: history
            }
        })
        .then(res => {
            const replyContent = (typeof res === 'string') ? res : (res.data || res.msg || "I couldn't understand that.");
            
            const aiMsg = {
                id: Date.now() + 1,
                type: 'ai',
                content: replyContent
            };
            
            const newMessages = this.data.messages;
            newMessages.push(aiMsg);
            
            this.setData({
                messages: newMessages,
                loading: false,
                scrollIntoView: `msg-${newMessages.length - 1}`
            });
        })
        .catch(err => {
            console.error("Coach API error", err);
            const errorMsg = {
                id: Date.now() + 1,
                type: 'ai',
                content: "Sorry, I'm having trouble connecting to the server. Please try again later."
            };
            const newMessages = this.data.messages;
            newMessages.push(errorMsg);
            
            this.setData({
                messages: newMessages,
                loading: false,
                scrollIntoView: `msg-${newMessages.length - 1}`
            });
        });
    }
});
