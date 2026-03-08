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
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    calendarDays: [],
    totalDays: 0,
    missedDays: 0,
    hasCheckedInToday: false,
    checkinList: [] // Stores 'YYYY-MM-DD' strings
  },

  onLoad() {
    this.fetchCheckins();
  },

  onShow() {
    // Refresh when showing page (e.g. after back)
    this.fetchCheckins();
  },

  fetchCheckins() {
    request({
      url: "/business/lyh/checkin/my-list",
      method: "GET"
    }).then(res => {
      // res is typically the list of checkin objects or data wrapper
      // Based on request.js, it resolves res.data.data or rest
      // Backend returns List<LyhCheckin>
      const list = Array.isArray(res) ? res : (res.rows || []);
      const checkinDates = list.map(item => {
        // Handle date string or timestamp
        // backend checkinDate is Date, likely returned as "2024-07-02" or timestamp
        // Let's assume standard JSON serialization "yyyy-MM-dd" or timestamp
        if (typeof item.checkinDate === 'string') {
           return item.checkinDate.split(' ')[0]; // Handle "2024-07-02 00:00:00"
        }
        return new Date(item.checkinDate).toISOString().split('T')[0];
      });

      this.setData({
        checkinList: checkinDates,
        totalDays: checkinDates.length
      }, () => {
        this.generateCalendar(this.data.year, this.data.month);
      });
      
      this.checkTodayStatus(checkinDates);
      this.calculateStats(checkinDates);
    }).catch(err => {
      console.error("Fetch checkins failed", err);
    });
  },

  checkTodayStatus(dates) {
    const today = new Date();
    const todayStr = this.formatDate(today);
    this.setData({
      hasCheckedInToday: dates.includes(todayStr)
    });
  },

  calculateStats(dates) {
    // Missed days in current month
    // Count days from 1st to yesterday that are NOT in dates
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const today = now.getDate();
    
    let missed = 0;
    // Iterate from day 1 to yesterday
    for (let d = 1; d < today; d++) {
      const dateStr = this.formatDate(new Date(year, month - 1, d));
      if (!dates.includes(dateStr)) {
        missed++;
      }
    }
    this.setData({ missedDays: missed });
  },

  generateCalendar(year, month) {
    const firstDay = new Date(year, month - 1, 1).getDay(); // 0 (Sun) - 6 (Sat)
    const daysInMonth = new Date(year, month, 0).getDate();
    const daysInPrevMonth = new Date(year, month - 1, 0).getDate();
    
    const days = [];
    
    // Prev Month Padding
    for (let i = 0; i < firstDay; i++) {
      days.push({
        day: daysInPrevMonth - firstDay + 1 + i,
        type: 'prev',
        isChecked: false,
        isToday: false
      });
    }
    
    // Current Month
    const today = new Date();
    const isCurrentMonth = today.getFullYear() === year && (today.getMonth() + 1) === month;
    
    for (let i = 1; i <= daysInMonth; i++) {
      const dateStr = this.formatDate(new Date(year, month - 1, i));
      const isChecked = this.data.checkinList.includes(dateStr);
      const isToday = isCurrentMonth && today.getDate() === i;
      
      days.push({
        day: i,
        type: 'current',
        isChecked: isChecked,
        isToday: isToday
      });
    }
    
    // Next Month Padding (to fill 42 cells - 6 rows, or just 35 - 5 rows)
    // Let's just fill the last row
    const remaining = 7 - (days.length % 7);
    if (remaining < 7) {
      for (let i = 1; i <= remaining; i++) {
        days.push({
          day: i,
          type: 'next',
          isChecked: false,
          isToday: false
        });
      }
    }
    
    this.setData({ calendarDays: days });
  },

  prevMonth() {
    let { year, month } = this.data;
    if (month === 1) {
      year--;
      month = 12;
    } else {
      month--;
    }
    this.setData({ year, month });
    this.generateCalendar(year, month);
  },

  nextMonth() {
    let { year, month } = this.data;
    if (month === 12) {
      year++;
      month = 1;
    } else {
      month++;
    }
    this.setData({ year, month });
    this.generateCalendar(year, month);
  },

  handleCheckin() {
    if (this.data.hasCheckedInToday) return;
    
    wx.showLoading({ title: 'Signing in...' });
    request({
      url: "/business/lyh/checkin/now",
      method: "POST"
    }).then(res => {
      wx.hideLoading();
      wx.showToast({ title: 'Success', icon: 'success' });
      this.fetchCheckins(); // Refresh
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: err.message || 'Failed', icon: 'none' });
    });
  },

  formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
});
