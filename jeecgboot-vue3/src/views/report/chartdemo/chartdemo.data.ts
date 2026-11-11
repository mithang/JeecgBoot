const colors = ['#4db6ac', '#ffb74d', '#64b5f6', '#e57373', '#9575cd', '#a1887f', '#90a4ae', '#4dd0e1', '#81c784', '#ff8a65'];
export const getData = (() => {
  let dottedBase = +new Date();
  const barDataSource: any[] = [];
  const barMultiData: any[] = [];
  const barLineData: any[] = [];
  const barLineColors: any[] = [];

  for (let i = 0; i < 20; i++) {
    let obj = { name: '', value: 0 };
    const date = new Date((dottedBase += 1000 * 3600 * 24));
    obj.name = [date.getFullYear(), date.getMonth() + 1, date.getDate()].join('-');
    obj.value = Math.random() * 200;
    barDataSource.push(obj);
  }

  for (let j = 0; j < 2; j++) {
    for (let i = 0; i < 20; i++) {
      let obj = { name: '', value: 0, type: 2010 + j + '' };
      const date = new Date(dottedBase + 1000 * 3600 * 24 * i);
      obj.name = [date.getFullYear(), date.getMonth() + 1, date.getDate()].join('-');
      obj.value = Math.random() * 200;
      barMultiData.push(obj);
    }
  }
  const pieData = [
    { value: 335, name: 'Điện thoại chăm sóc khách hàng' },
    { value: 310, name: 'Trang web chính thức của Audi' },
    { value: 234, name: 'Đưa tin trên truyền thông' },
    { value: 135, name: 'Tổng cục Kiểm tra chất lượng' },
    { value: 105, name: 'Khác' },
  ];
  const radarData = [
    { value: 75, name: 'Chính trị', type: 'Tổng hợp khoa học xã hội', max: 100 },
    { value: 65, name: 'Lịch sử', type: 'Tổng hợp khoa học xã hội', max: 100 },
    { value: 55, name: 'Địa lý', type: 'Tổng hợp khoa học xã hội', max: 100 },
    { value: 74, name: 'Hóa học', type: 'Tổng hợp khoa học xã hội', max: 100 },
    { value: 38, name: 'Vật lý', type: 'Tổng hợp khoa học xã hội', max: 100 },
    { value: 88, name: 'Sinh học', type: 'Tổng hợp khoa học xã hội', max: 100 },
  ];
  for (let j = 0; j < 2; j++) {
    for (let i = 0; i < 15; i++) {
      let obj = { name: '', value: 0, type: 2010 + j + '', seriesType: j >= 1 ? 'line' : 'bar' };
      const date = new Date(dottedBase + 1000 * 3600 * 24 * i);
      obj.name = [date.getFullYear(), date.getMonth() + 1, date.getDate()].join('-');
      obj.value = Math.random() * 200;
      barLineData.push(obj);
    }
    barLineColors.push(colors[j]);
  }
  return { barDataSource, barMultiData, pieData, barLineData, barLineColors,radarData };
})();
