"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
Page({
    data: {
        userInfo: {},
        isLogin: false,
        isAdmin: false,
        studyStats: {
            totalDays: 0,
            totalWords: 0,
            score: 0
        }
    },
    onShow() {
        const userInfo = wx.getStorageSync('userInfo');
        const token = wx.getStorageSync('token');
        const roles = wx.getStorageSync('roles') || [];
        if (token && userInfo) {
            this.setData({
                userInfo: userInfo,
                isLogin: true,
                isAdmin: roles.includes('admin')
            });
            this.fetchStudyStats();
        }
        else {
            this.setData({
                userInfo: { nickName: '点击登录' },
                isLogin: false,
                isAdmin: false,
                studyStats: { totalDays: 0, totalWords: 0, score: 0 }
            });
        }
    },
    fetchStudyStats() {
        const req = require('../../utils/request.js');
        const request = req.default || req;
        request({
            url: '/business/lyh/stats/my',
            method: 'GET'
        }).then(res => {
            if (res) {
                this.setData({ studyStats: res });
            }
        }).catch(err => {
            console.error('Fetch stats failed', err);
        });
    },
    handleEditProfile() {
        if (!this.data.isLogin) {
            wx.navigateTo({ url: '/pages/login/index' });
            return;
        }
        wx.showActionSheet({
            itemList: ['Change Avatar', 'Change Nickname'],
            success: (res) => {
                if (res.tapIndex === 0) {
                    this.handleEditAvatar();
                }
                else if (res.tapIndex === 1) {
                    wx.showModal({
                title: '修改昵称',
                        editable: true,
                placeholderText: '输入新的昵称',
                        success: (res) => {
                            if (res.confirm && res.content) {
                                const updatedUser = __assign(__assign({}, this.data.userInfo), { nickName: res.content });
                                this.setData({ userInfo: updatedUser });
                                wx.setStorageSync('userInfo', updatedUser);
                        wx.showToast({ title: '已更新', icon: 'success' });
                            }
                        }
                    });
                }
            }
        });
    },
    handleEditAvatar() {
        wx.chooseMedia({
            count: 1,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                const tempFilePath = res.tempFiles[0].tempFilePath;
                const updatedUser = __assign(__assign({}, this.data.userInfo), { avatarUrl: tempFilePath });
                this.setData({ userInfo: updatedUser });
                wx.setStorageSync('userInfo', updatedUser);
                wx.showToast({ title: '头像已更新', icon: 'success' });
            }
        });
    },
    handleChangePassword() {
        if (!this.data.isLogin) {
            wx.navigateTo({ url: '/pages/login/index' });
            return;
        }
        wx.showModal({
            title: '请输入旧密码',
            editable: true,
            placeholderText: '输入旧密码',
            confirmText: '下一步',
            success: (resOld) => {
                if (resOld.confirm && resOld.content) {
                    const oldPwd = resOld.content;
                    wx.showModal({
                        title: '请输入新密码',
                        editable: true,
                        placeholderText: '输入新密码',
                        confirmText: '提交',
                        success: (resNew) => {
                            if (resNew.confirm && resNew.content) {
                                const newPwd = resNew.content;
                                const url = `/system/user/profile/updatePwd?oldPassword=${encodeURIComponent(oldPwd)}&newPassword=${encodeURIComponent(newPwd)}`;
                                const req = require('../../utils/request.js');
                                const request = req.default || req;
                                request({
                                    url,
                                    method: 'PUT'
                                })
                                    .then(() => {
                                    wx.showToast({ title: '修改成功', icon: 'success' });
                                })
                                    .catch((err) => {
                                    console.error('updatePwd failed', err);
                                });
                            }
                        }
                    });
                }
            }
        });
    },
    handleAbout() {
        wx.showModal({
            title: '关于我们',
            content: '由李雨寰开发的智聊英语App，致力于为广大使用者提供一个学习的平台。',
            showCancel: false,
            confirmText: '知道了'
        });
    },
    handleLogout() {
        wx.showModal({
            title: '提示',
            content: '确定要退出登录吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync('token');
                    wx.removeStorageSync('userInfo');
                    this.setData({
                        userInfo: { nickName: '点击登录' },
                        isLogin: false
                    });
                    wx.reLaunch({
                        url: '/pages/login/index'
                    });
                }
            }
        });
    },
    navigateTo(e) {
        const url = e.currentTarget.dataset.url;
        if (url) {
            wx.navigateTo({ url });
        }
    }
});
