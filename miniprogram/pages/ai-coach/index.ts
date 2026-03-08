Page({
  data: {
    messages: [
      { id: 1, type: 'ai', content: 'Hello! I am your AI English coach. How can I help you today?' }
    ],
    inputValue: '',
    scrollIntoView: ''
  },

  onInput(e: any) {
    this.setData({
      inputValue: e.detail.value
    })
  },

  sendMessage() {
    const content = this.data.inputValue.trim()
    if (!content) return

    const newMsg = {
      id: Date.now(),
      type: 'user',
      content: content
    }

    const messages = this.data.messages
    messages.push(newMsg)

    this.setData({
      messages: messages,
      inputValue: '',
      scrollIntoView: `msg-${messages.length - 1}`
    })

    // Mock AI Response
    setTimeout(() => {
      this.reply(content)
    }, 1000)
  },

  reply(userContent: string) {
    let replyText = "I see. Could you tell me more?"
    if (userContent.toLowerCase().includes('hello')) {
      replyText = "Hi there! Ready to practice some English?"
    } else if (userContent.toLowerCase().includes('name')) {
      replyText = "I'm SpeakUp AI, your personal English tutor."
    }

    const aiMsg = {
      id: Date.now() + 1,
      type: 'ai',
      content: replyText
    }

    const messages = this.data.messages
    messages.push(aiMsg)

    this.setData({
      messages: messages,
      scrollIntoView: `msg-${messages.length - 1}`
    })
  }
})
