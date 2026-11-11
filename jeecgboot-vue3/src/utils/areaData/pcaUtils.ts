import {areaList} from '@vant/area-data'
import {freezeDeep} from "@/utils/common/compUtils";

// Dữ liệu tỉnh/thành/quận huyện đã làm phẳng
export const pcaa = freezeDeep(usePlatPcaaData())

/**
 * Lấy dữ liệu tỉnh/thành/quận đã làm phẳng
 */
function usePlatPcaaData() {
  const {city_list: city, county_list: county, province_list: province} = areaList;
  const dataMap = new Map<string, Recordable>()
  const flatData: Recordable = {'86': province}
  // Tỉnh
  Object.keys(province).forEach((code) => {
    flatData[code] = {}
    dataMap.set(code.slice(0, 2), flatData[code])
  })
  // Thành phố
  Object.keys(city).forEach((code) => {
    flatData[code] = {}
    dataMap.set(code.slice(0, 4), flatData[code])
    // Điền cấp trên
    const getProvince = dataMap.get(code.slice(0, 2))
    if (getProvince) {
      getProvince[code] = city[code]
    }
  });
  // Quận/huyện
  Object.keys(county).forEach((code) => {
    // Điền cấp trên
    const getCity = dataMap.get(code.slice(0, 4))
    if (getCity) {
      getCity[code] = county[code]
    }
  });
  return flatData
}