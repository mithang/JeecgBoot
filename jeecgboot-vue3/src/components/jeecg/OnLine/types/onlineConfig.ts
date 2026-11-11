interface ScopedSlots {
  customRender: string;
}

interface HrefSlots {
  // Địa chỉ liên kết
  href: string;
  // fieldHref_Tên trường
  slotName: string;
}

interface OnlineColumn {
  dataIndex?: string;
  title?: string;
  key?: string;
  fieldType?: string;
  width?: number | string;
  align?: string;
  sorter?: string | boolean;
  isTotal?: string | number | boolean;
  groupTitle?: string;
  // Khi siêu liên kết, khớp với slotName trong HrefSlots
  scopedSlots?: ScopedSlots;
  // Thường dùng cho danh mục, danh mục truyền sang là chuỗi mã danh mục, sau đó chuyển qua hàm
  customRender?: string | Function;
  // Không biết loại này dùng để làm gì
  hrefSlotName?: string;
  showLength?: number | string;
  children?: OnlineColumn[];
  sortOrder?: string;
  // Loại control tương ứng với slot(Danh sách)
  slots?: ScopedSlots;
  //Vượt quá chiều rộng sẽ tự động rút gọn, tạm thời chưa hỗ trợ dùng cùng sắp xếp và lọc.
  ellipsis?: boolean;
  // Có phải cột cố định không
  fixed?: boolean | 'left' | 'right';
  //Loại trường int/string 
  dbType?:string;
  //Dùng trường của bảng khác
  linkField?:string;
  fieldExtendJson?:string
  resizable?: boolean;
}

export { OnlineColumn, HrefSlots };
