import { BASE_URL, TOKEN_KEY } from "./constant";

interface RequestOption {
  url: string;
  method?: "GET" | "POST" | "PUT" | "DELETE";
  data?: any;
  header?: any;
}

const request = <T>(options: RequestOption): Promise<T> => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync(TOKEN_KEY);

    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || "GET",
      data: options.data,
      header: {
        "Content-Type": "application/json",
        Authorization: token ? `Bearer ${token}` : "",
        ...(options.header || {}),
      },
      success: (res: any) => {
        if (res.statusCode === 200) {
          const { code, msg, ...rest } = res.data;

          if (code === 200) {
            // 优先返回 data 字段，如果 data 不存在，则返回剩余字段（兼容 AjaxResult 和 AppRestResult）
            if (res.data.data !== undefined && res.data.data !== null) {
              resolve(res.data.data as T);
            } else {
              resolve(rest as T);
            }
          } else if (code === 401) {
            wx.removeStorageSync(TOKEN_KEY);
            wx.navigateTo({ url: "/pages/login/index" });
            reject(new Error("Unauthorized"));
          } else {
            wx.showToast({
              title: msg || "Error",
              icon: "none",
            });
            reject(new Error(msg || "Error"));
          }
        } else {
          wx.showToast({
            title: `HTTP Error: ${res.statusCode}`,
            icon: "none",
          });
          reject(new Error(`HTTP Error: ${res.statusCode}`));
        }
      },
      fail: (err) => {
        wx.showToast({
          title: "Network Error",
          icon: "none",
        });
        reject(err);
      },
    });
  });
};

export default request;
