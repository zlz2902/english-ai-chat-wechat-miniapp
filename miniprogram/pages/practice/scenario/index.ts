import request from '../../../utils/request';

Page({
  data: {
    scenarios: [] as any[]
  },

  onLoad() {
    this.getScenarios();
  },

  getScenarios() {
    request<{ rows: any[] }>({
      url: '/business/lyh/scenario/list',
      method: 'GET'
    }).then(res => {
      const scenarios = res.rows.map((item: any) => ({
        id: item.scenarioId,
        title: item.title,
        image: item.imageUrl || '/assets/banner/banner01.jpg', // Fallback
        level: this.mapLevel(item.level).value,
        levelLabel: this.mapLevel(item.level).label,
        description: item.description
      }));
      
      this.setData({
        scenarios: scenarios
      });
    }).catch(err => {
      console.error('Failed to load scenarios', err);
    });
  },

  mapLevel(val: string) {
    const map: any = {
      '1': { label: '初级', value: 'easy' },
      '2': { label: '中级', value: 'medium' },
      '3': { label: '高级', value: 'hard' }
    };
    return map[val] || { label: '中级', value: 'medium' };
  }
})
