import {pcaa as REGION_DATA} from "@/utils/areaData/pcaUtils";

/**
 * Area Kiểu của thuộc tính all
 */
interface PlainPca {
  id: string;
  text: string;
  pid: string;
  index: Number;
}

/**
 * Lớp tiện ích tỉnh/thành/quận huyện -Giải quyết vấn đề dịch của component Tỉnh/Thành/Quận huyện trong danh sách
 */
class Area {
  all: PlainPca[];

  /**
   * Hàm khởi tạo
   * @param pcaa
   */
  constructor(pcaa?) {
    if (!pcaa) {
      pcaa = REGION_DATA;
    }
    let arr: PlainPca[] = [];
    const province = pcaa['86'];
    Object.keys(province).map((key) => {
      arr.push({ id: key, text: province[key], pid: '86', index: 1 });
      const city = pcaa[key];
      Object.keys(city).map((key2) => {
        arr.push({ id: key2, text: city[key2], pid: key, index: 2 });
        const qu = pcaa[key2];
        if (qu) {
          Object.keys(qu).map((key3) => {
            arr.push({ id: key3, text: qu[key3], pid: key2, index: 3 });
          });
        }
      });
    });
    this.all = arr;
  }

  get pca() {
    return this.all;
  }

  getCode(text) {
    if (!text || text.length == 0) {
      return '';
    }
    for (let item of this.all) {
      if (item.text === text) {
        return item.id;
      }
    }
  }
  
// Giải thích logic mã: [issue/382]Dữ liệu component Tỉnh/Thành/Quận huyện JAreaLinkage không hiển thị lại---
  getText(code,index=3) {
    if (!code || code.length == 0) {
      return '';
    }
    let arr = [];
    this.getAreaBycode(code, arr, index);
    return arr.join('/');
  }

  getRealCode(code) {
    let arr = [];
    this.getPcode(code, arr, 3);
    return arr;
  }

  getPcode(id, arr, index) {
    for (let item of this.all) {
      if (item.id === id && item.index == index) {
        arr.unshift(id);
        if (item.pid != '86') {
          this.getPcode(item.pid, arr, --index);
        }
      }
    }
  }

  getAreaBycode(code, arr, index) {
    for (let item of this.all) {
      if (item.id === code && item.index == index) {
        arr.unshift(item.text);
        if (item.pid != '86') {
          this.getAreaBycode(item.pid, arr, --index);
        }
      }
    }
  }
}
const jeecgAreaData = new Area();

// Tìm văn bản theo code
const getAreaTextByCode = function (code) {
  let index = 3;
  // Giải thích logic mã: Kiểm tra xem code có phải là chuỗi nhiều code phân tách bằng dấu phẩy không, nếu đúng thì lấy code cuối cùngcode ---
  if (code && code.includes(',')) {
    index = code.split(",").length;
    code = code.substr(code.lastIndexOf(',') + 1);
  }
  return jeecgAreaData.getText(code,index);
};

/**
 * 20260204
 * liaozhiyang
 * 【QQYUN-14694]online hỗ trợ cấu hình tỉnh, thành phố, huyện độc lập (tìm văn bản theo code (hỗ trợ chỉ tỉnh, chỉ tỉnh/thành phố, tỉnh/thành phố/quận huyện))
 * @param includeParent Có trả về đường dẫn của cấp cha hay không. Mặc định false chỉ trả về cấp cuối cùng (ví dụ「Trường Trị」), true trả về đường dẫn đầy đủ (ví dụ「Sơn Tây/Thành phố Trường Trị")
 * @param level Cấp bậc:1=Tỉnh,2=Thành phố,3=Huyện. Khi truyền vào thì dùng làm index, nếu không truyền thì suy ra từ code
 */
const getAreaTextByCodeAnyLevel = function (code: string | number | undefined, includeParent = false, level?: 1 | 2 | 3): string {
  if (!code) return '';
  const codeStr = String(code).trim();
  if (!codeStr.length) return '';

  let lastCode: string;
  let index: number;
  let resolvedItem: PlainPca | null = null;

  if (codeStr.includes(',')) {
    const parts = codeStr.split(',').map((s) => s.trim()).filter(Boolean);
    if (!parts.length) return '';
    index = level ?? parts.length;
    lastCode = level != null ? parts[level - 1] : parts[parts.length - 1];
    if (level != null) {
      resolvedItem = jeecgAreaData.all.find((it) => String(it.id) === lastCode && it.index == index) ?? null;
    }
  } else {
    const item = jeecgAreaData.all.find((it) => String(it.id) === codeStr);
    if (!item) return '';
    const itemIndex = item.index as number;
    index = level ?? itemIndex;
    lastCode = codeStr;
    if (level != null && level !== itemIndex) {
      const chain: string[] = [];
      jeecgAreaData.getPcode(codeStr, chain, itemIndex);
      const targetCode = chain[level - 1];
      if (targetCode) {
        lastCode = targetCode;
        resolvedItem = jeecgAreaData.all.find((it) => String(it.id) === targetCode && it.index == level) ?? null;
      } else {
        resolvedItem = item;
      }
    } else {
      resolvedItem = item;
    }
  }
  if (includeParent) {
    return jeecgAreaData.getText(lastCode, index);
  } else {
    const item = resolvedItem ?? jeecgAreaData.all.find((it) => String(it.id) === lastCode && it.index == index);
    return item ? item.text : '';
  }
};

export { getAreaTextByCode, getAreaTextByCodeAnyLevel };
