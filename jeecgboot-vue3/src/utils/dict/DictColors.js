const whiteColor = '#ffffff'
const blackColor = '#666666'

export const Colors = [
  // Màu nền, màu chữ
  ['#2196F3', whiteColor],
  ['#08C9C9', whiteColor],
  ['#00C345', whiteColor],
  ['#FAD714', whiteColor],
  ['#FF9300', whiteColor],
  ['#F52222', whiteColor],
  ['#EB2F96', whiteColor],
  ['#7500EA', whiteColor],
  ['#2D46C4', whiteColor],
  ['#484848', whiteColor],
  // --------------------
  ['#C9E6FC', blackColor],
  ['#C3F2F2', blackColor],
  ['#C2F1D2', blackColor],
  ['#FEF6C6', blackColor],
  ['#FFE5C2', blackColor],
  ['#FDCACA', blackColor],
  ['#FACDE6', blackColor],
  ['#DEC2FA', blackColor],
  ['#CCD2F1', blackColor],
  ['#D3D3D3', blackColor],
]

export const NONE_COLOR = ['#e9e9e9', blackColor]

/**
 * Trả về một iterator màu, mỗi lần gọi trả về một màu, khi dùng hết màu thì bắt đầu lại từ đầu
 * @param {number} initIndex Chỉ mục màu ban đầu
 * @returns {{getIndex: function, next: function}}
 */
export function getColorIterator(initIndex = 0) {
  let index = initIndex;
  if (index < 0 || index >= Colors.length) {
    index = 0;
  }
  return {
    getIndex: () => index,
    next() {
      const color = Colors[index];
      index = (index + 1) % Colors.length;
      return color;
    },
  }
}

/**
 * Lấy tọa độ và màu hiện tại theo màu
 */
export function getItemColor(color) {
  if(!color){
    return NONE_COLOR[1];
  }
  let colorIndex = Colors.findIndex((value)=>{
    return value[0] === color;
  })
  if(colorIndex === -1){
    return NONE_COLOR[1];
  }
  return Colors[colorIndex][1];
}
