import request from "../../../utils/request";
import { BASE_URL } from "../../../utils/constant";

Page({
  data: {
    scenarios: [] as any[],
  },

  onLoad() {
    this.getScenarios();
  },

  getScenarios() {
    request<{ rows: any[] }>({
      url: "/business/lyh/scenario/list",
      method: "GET",
    })
      .then((res) => {
        const scenarios = res.rows.map((item: any) => {
          const rawImg: string = item.imageUrl || "";
          let image = "/assets/banner/banner01.jpg";
          if (rawImg) {
            image = rawImg.startsWith("http")
              ? rawImg
              : BASE_URL
                ? `${BASE_URL}${encodeURI(rawImg)}`
                : rawImg;
          }
          return {
            id: item.scenarioId,
            title: item.title,
            image,
            level: this.mapLevel(item.level).value,
            levelLabel: this.mapLevel(item.level).label,
            description: item.description,
          };
        });

        this.setData({
          scenarios: scenarios,
        });
      })
      .catch((err) => {
        console.error("Failed to load scenarios", err);
      });
  },

  mapLevel(val: string) {
    const map: any = {
      "1": { label: "初级", value: "easy" },
      "2": { label: "中级", value: "medium" },
      "3": { label: "高级", value: "hard" },
    };
    return map[val] || { label: "中级", value: "medium" };
  },
});
