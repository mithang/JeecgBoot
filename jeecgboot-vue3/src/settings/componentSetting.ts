// Dùng để cấu hình chung cho một số component, không cần sửa component

import type { SorterResult } from '../components/Table';

export default {
  // Cấu hình bảng
  table: {
    // Cấu hình chung cho request API của bảng, có thể ghi đè trong prop của component
    // Hỗ trợ định dạng xxx.xxx.xxx
    fetchSetting: {
      // Trường trang hiện tại truyền cho backend
      pageField: 'pageNo',
      // Trường số bản ghi hiển thị mỗi trang truyền cho backend
      sizeField: 'pageSize',
      // Trường dữ liệu bảng mà API trả về
      listField: 'records',
      // Trường tổng số mà API trả về
      totalField: 'total',
    },
    // Các tùy chọn phân trang
    pageSizeOptions: ['10', '50', '80', '100'],
    // Kích thước mặc định của bảng
    defaultSize: 'middle',
    // Mặc định mỗi trang hiển thị bao nhiêu mục
    defaultPageSize: 10,
    // Phương thức sắp xếp mặc định
    defaultSortFn: (sortInfo: SorterResult) => {
      // Giải thích logic mã: VUEN-2199【Trình thiết kế biểu mẫu】Sắp xếp nhiều trường
      if(sortInfo instanceof Array){
        let sortInfoArray:any[] = []
        for(let item of sortInfo){
          let info = getSort(item);
          if(info){
            sortInfoArray.push(info)
          }
        }
        return {
          sortInfoString: JSON.stringify(sortInfoArray)
        }
      }else{
        let info = getSort(sortInfo)
        return info || {}
      }
    },
    // Phương thức lọc tùy chỉnh
    defaultFilterFn: (data: Partial<Recordable<string[]>>) => {
      return data;
    },
    // Giải thích logic mã: 【issues/1188】BasicTable thêm định nghĩa kiểu scrollToFirstRowOnChange
    scrollToFirstRowOnChange: false,
  },
  // Cấu hình component cuộn
  scrollbar: {
    // Có dùng kiểu cuộn gốc không
    // Sau khi bật, menu, popup, drawer sẽ dùng component thanh cuộn gốc
    native: false,
  },
  // Cấu hình biểu mẫu
  form: {
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
      xl: { span: 6 },
      xxl: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
    // Dấu hai chấm mặc định của biểu mẫu
    colon: true,
  },
};

/**
 * Lấy thông tin sắp xếp
 * @param item
 */
function getSort(item){
  const { field, order } = item;
  if (field && order) {
    let sortType = 'ascend' == order ? 'asc' : 'desc';
    return {
      // Trường sắp xếp
      column: field,
      // Cách sắp xếp asc/desc
      order: sortType,
    };
  }
  return ''
}
