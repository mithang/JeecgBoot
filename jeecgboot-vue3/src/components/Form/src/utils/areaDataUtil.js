import {pcaa as REGION_DATA} from "@/utils/areaData/pcaUtils";
import { cloneDeep } from 'lodash-es';

// codechuyển thành đối tượng lớn chữ Hán
const CodeToText = {};
// Đối tượng lớn chuyển ký tự Hán sang code
const TextToCode = {};
const provinceObject = REGION_DATA['86']; // Đối tượng tỉnh
const regionData = [];
let provinceAndCityData = [];

CodeToText[''] = 'Tất cả';

// Tính tỉnh
for (const prop in provinceObject) {
  regionData.push({
    value: prop, // Giá trị code tỉnh
    label: provinceObject[prop], // Tên tỉnh (chữ Hán)
  });
  CodeToText[prop] = provinceObject[prop];
  TextToCode[provinceObject[prop]] = {
    code: prop,
  };
  TextToCode[provinceObject[prop]]['Tất cả'] = {
    code: '',
  };
}
// Tính thành phố
for (let i = 0, len = regionData.length; i < len; i++) {
  const provinceCode = regionData[i].value;
  const provinceText = regionData[i].label;
  const provinceChildren = [];
  for (const prop in REGION_DATA[provinceCode]) {
    provinceChildren.push({
      value: prop,
      label: REGION_DATA[provinceCode][prop],
    });
    CodeToText[prop] = REGION_DATA[provinceCode][prop];
    TextToCode[provinceText][REGION_DATA[provinceCode][prop]] = {
      code: prop,
    };
    TextToCode[provinceText][REGION_DATA[provinceCode][prop]]['Tất cả'] = {
      code: '',
    };
  }
  if (provinceChildren.length) {
    regionData[i].children = provinceChildren;
  }
}
provinceAndCityData = cloneDeep(regionData);

// Tính quận/huyện
for (let i = 0, len = regionData.length; i < len; i++) {
  const province = regionData[i].children;
  const provinceText = regionData[i].label;
  if (province) {
    for (let j = 0, len = province.length; j < len; j++) {
      const cityCode = province[j].value;
      const cityText = province[j].label;
      const cityChildren = [];
      for (const prop in REGION_DATA[cityCode]) {
        cityChildren.push({
          value: prop,
          label: REGION_DATA[cityCode][prop],
        });
        CodeToText[prop] = REGION_DATA[cityCode][prop];
        TextToCode[provinceText][cityText][REGION_DATA[cityCode][prop]] = {
          code: prop,
        };
      }
      if (cityChildren.length) {
        province[j].children = cityChildren;
      }
    }
  }
}

// Thêm tùy chọn "Tất cả"
const provinceAndCityDataPlus = cloneDeep(provinceAndCityData);
provinceAndCityDataPlus.unshift({
  value: '',
  label: 'Tất cả',
});
for (let i = 0, len = provinceAndCityDataPlus.length; i < len; i++) {
  const province = provinceAndCityDataPlus[i].children;
  if (province && province.length) {
    province.unshift({
      value: '',
      label: 'Tất cả',
    });
    for (let j = 0, len = province.length; j < len; j++) {
      const city = province[j].children;
      if (city && city.length) {
        city.unshift({
          value: '',
          label: 'Tất cả',
        });
      }
    }
  }
}

const regionDataPlus = cloneDeep(regionData);
regionDataPlus.unshift({
  value: '',
  label: 'Tất cả',
});
for (let i = 0, len = regionDataPlus.length; i < len; i++) {
  const province = regionDataPlus[i].children;
  if (province && province.length) {
    province.unshift({
      value: '',
      label: 'Tất cả',
    });

    for (let j = 0, len = province.length; j < len; j++) {
      const city = province[j].children;
      if (city && city.length) {
        city.unshift({
          value: '',
          label: 'Tất cả',
        });
      }
    }
  }
}
//--begin--@updateBy:liusq----date:20210922---for:Phương thức đáp ứng nhu cầu liên động ba cấp tỉnh/thành phố/quận huyện-----
//Dữ liệu tỉnh
const provinceOptions = [];
for (const prop in provinceObject) {
  provinceOptions.push({
    value: prop, // Giá trị code tỉnh
    label: provinceObject[prop], // Tên tỉnh (chữ Hán)
  });
}
/**
 * Lấy dữ liệu option của dropdown theo code
 * @param code
 * @returns []
 */
function getDataByCode(code) {
  let data = [];
  for (const prop in REGION_DATA[code]) {
    data.push({
      value: prop, // Giá trị code tỉnh
      label: REGION_DATA[code][prop], // Tên tỉnh (chữ Hán)
    });
  }
  return data;
}

/**
 * Lấy cấp bậc của tất cả tỉnh/thành/quận huyện
 * @type {Array}
 */
const pca = [];
Object.keys(provinceObject).map((province) => {
  pca.push({ id: province, text: provinceObject[province], pid: '86', index: 1 });
  const cityObject = REGION_DATA[province];
  Object.keys(cityObject).map((city) => {
    pca.push({ id: city, text: cityObject[city], pid: province, index: 2 });
    const areaObject = REGION_DATA[city];
    if (areaObject) {
      Object.keys(areaObject).map((area) => {
        pca.push({ id: area, text: areaObject[area], pid: city, index: 3 });
      });
    }
  });
});

/**
 * Suy ngược value theo codevalue
 * @param code
 * @param level
 * @returns {Array}
 */
function getRealCode(code, level) {
  let arr = [];
  getPcode(code, arr, level);
  return arr;
}
function getPcode(id, arr, index) {
  for (let item of pca) {
    if (item.id === id && item.index == index) {
      arr.unshift(id);
      if (item.pid != '86') {
        getPcode(item.pid, arr, --index);
      }
    }
  }
}
//--end--@updateBy:liusq----date:20210922---for:Phương thức đáp ứng nhu cầu liên động ba cấp tỉnh/thành phố/quận huyện-----
export { provinceAndCityData, regionData, provinceAndCityDataPlus, regionDataPlus, getDataByCode, provinceOptions, getRealCode };
