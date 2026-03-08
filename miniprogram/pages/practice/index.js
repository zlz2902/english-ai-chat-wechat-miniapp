"use strict";
Page({
    data: {},
    navigateToQuestionBank() {
        wx.navigateTo({
            url: '/pages/practice/question-bank/index'
        });
    },
    navigateToScenario() {
        wx.navigateTo({
            url: '/pages/practice/scenario/index'
        });
    }
});
