# Tài liệu component báo cáo

## Biểu đồ cột

##### Cách tham chiếu

```js
import Bar from '/@/components/chart/Bar.vue';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |

##### chartData Ví dụ

```json
[
  {
    "name": "1tháng",
    "value": 320
  },
  {
    "name": "2tháng",
    "value": 457
  },
  {
    "name": "3tháng",
    "value": 182
  }
]
```

##### Ví dụ mã

```html
<template>
  <Bar :chartData="chartData"></Bar>
</template>

<script lang="ts" setup>
  import Bar from '/@/components/chart/Bar.vue';
  const chartData = [
    {
      name: '1tháng',
      value: 320,
    },
    {
      name: '2tháng',
      value: 457,
    },
    {
      name: '3tháng',
      value: 182,
    },
  ];
</script>

<style></style>
```

## Biểu đồ cột nhiều cột

##### Cách tham chiếu

```js
import BarMulti from '/@/components/chart/BarMulti.vue';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |

##### chartData Ví dụ

```json
[
  {
    "name": "1tháng",
    "value": 320,
    "type": "2021"
  },
  {
    "name": "2tháng",
    "value": 457,
    "type": "2021"
  },
  {
    "name": "3tháng",
    "value": 182,
    "type": "2021"
  },
  {
    "name": "1tháng",
    "value": 240,
    "type": "2022"
  },
  {
    "name": "2tháng",
    "value": 357,
    "type": "2022"
  },
  {
    "name": "3tháng",
    "value": 456,
    "type": "2022"
  }
]
```

## Biểu đồ cột mini

Giống biểu đồ cột, chỉ cần sửa cấu hình là được

## Biểu đồ diện tích

##### Cách tham chiếu

```js
import Line from '/@/components/chart/Line.vue';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |
| option    | object |      | Mục cấu hình     |

##### chartData Ví dụ

```json
[
  {
    "name": "1tháng",
    "value": 320
  },
  {
    "name": "2tháng",
    "value": 457
  },
  {
    "name": "3tháng",
    "value": 182
  }
]
```

## Biểu đồ đường nhiều dòng

##### Cách tham chiếu

```js
import LineMulti from '/@/components/chart/LineMulti.vue';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |
| option    | object |      | Mục cấu hình     |

##### chartData Ví dụ

Cùng biểu đồ cột

## Biểu đồ hình tròn

##### Cách tham chiếu

```js
import Pie from '/@/components/chart/Pie';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |
| option    | object |      | Mục cấu hình     |

##### chartData Ví dụ

```json
[
  { "name": "Tháng 1", "value": 40 },
  { "name": "Tháng 2", "value": 21 },
  { "name": "Tháng 3", "value": 17 },
  { "name": "Tháng 4", "value": 13 },
  { "name": "Tháng 5", "value": 9 }
]
```

## Biểu đồ radar

##### Cách tham chiếu

```js
import Radar from '/@/components/chart/Radar';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |
| option    | object |      | Mục cấu hình     |

##### chartData Ví dụ

```json
[
  { "item": "Tháng 1", "score": 40 },
  { "item": "Tháng 2", "score": 20 },
  { "item": "Tháng 3", "score": 67 },
  { "item": "Tháng 4", "score": 43 },
  { "item": "Tháng 5", "score": 90 }
]
```

## Bảng điều khiển

##### Cách tham chiếu

```js
import Gauge from '/@/components/chart/Gauge';
```

##### Danh sách tham số

| Tên tham số    | Loại   | Bắt buộc | Giải thích       |
| --------- | ------ | ---- | ---------- |
| chartData | array  | ✔️   | Nguồn dữ liệu báo cáo |
| width     | number |      | Chiều rộng báo cáo   |
| height    | number |      | Chiều cao báo cáo   |
| option    | object |      | Mục cấu hình     |

## Danh sách xếp hạng

##### Cách tham chiếu

```js
import RankList from '@/components/chart/RankList';
```

##### Danh sách tham số

| Tên tham số | Loại   | Bắt buộc | Giải thích                     |
| ------ | ------ | ---- | ------------------------ |
| title  | string |      | Tiêu đề báo cáo                 |
| list   | array  |      | Dữ liệu danh sách xếp hạng             |
| height | number |      | Chiều cao báo cáo, mặc định chiều cao tự thích ứng |

##### list Ví dụ

```json
[
  { "name": "Cửa hàng số 1 Triều Dương, Bắc Kinh", "total": 1981 },
  { "name": "Cửa hàng số 2 Triều Dương, Bắc Kinh", "total": 1359 },
  { "name": "Cửa hàng số 3 Triều Dương, Bắc Kinh", "total": 1354 },
  { "name": "Cửa hàng số 4 Triều Dương, Bắc Kinh", "total": 263 },
  { "name": "Cửa hàng số 5 Triều Dương, Bắc Kinh", "total": 446 },
  { "name": "Cửa hàng số 6 Triều Dương, Bắc Kinh", "total": 796 }
]
```
