export const keywords: any = {
  list: [
    //------  Danh sáchapi -------
    // thuộc tính
    { text: '.acceptHrefParams', displayText: 'acceptHrefParams', superiors: 'this', desc: 'Lấy điều kiện trên thanh địa chỉ' },
    { text: '.currentPage', displayText: 'currentPage', superiors: 'this', desc: 'Lấy số trang hiện tại, mặc định1' },
    { text: '.currentTableName', displayText: 'currentTableName', desc: 'Lấy tên bảng hiện tại' },
    { text: '.description', displayText: 'description', superiors: 'this', desc: 'Lấy mô tả bảng hiện tại' },
    { text: '.hasChildrenField', displayText: 'hasChildrenField', superiors: 'this', desc: 'Nếu là danh sách dạng cây, lấy tên trường cho biết có nút con hay không' },
    { text: '.ID', displayText: 'ID', superiors: 'this', desc: 'Lấy cấu hình của bảng hiện tạiID' },
    { text: '.pageSize', displayText: 'pageSize', superiors: 'this', desc: 'Lấy số mục mỗi trang hiện tại, mặc định10' },
    { text: '.queryParam', displayText: 'queryParam', superiors: 'this', desc: 'Lấy điều kiện truy vấn của biểu mẫu truy vấn' },
    { text: '.selectedRowKeys', displayText: 'selectedRowKeys', superiors: 'this', desc: 'Lấy của hàng đã chọnidmảng của' },
    { text: '.selectedRows', displayText: 'selectedRows', superiors: 'this', desc: 'Lấy mảng dữ liệu của hàng đã chọn' },
    { text: '.sortField', displayText: 'sortField', superiors: 'this', desc: 'Lấy trường sắp xếp, mặc định ‘id’' },
    { text: '.sortType', displayText: 'sortType', superiors: 'this', desc: 'Lấy loại sắp xếp, mặc định tăng dần ‘asc’' },
    { text: '.total', displayText: 'total', superiors: 'this', desc: 'Lấy tổng số mục' },
    { text: '.loading', displayText: 'loading', superiors: 'this', desc: 'Thiết lập/Lấyloading' },
    // Phương thức
    { text: '.loadData()', displayText: 'loadData()', superiors: 'this', desc: 'Tải dữ liệu' },
    { text: '.clearSelectedRow()', displayText: 'clearSelectedRow()', superiors: 'this', desc: 'Xóa các hàng đã chọn' },
    {
      text: '.getLoadDataParams()',
      displayText: 'getLoadDataParams()',
      superiors: 'this',
      desc: 'Lấy mọi điều kiện truy vấn, trả về một đối tượng, bao gồm: biểu mẫu truy vấn, truy vấn nâng cao, tham số thanh địa chỉ, thông tin phân trang, thông tin sắp xếp, v.v.',
    },
    { text: '.isTree()', displayText: 'isTree()', superiors: 'this', desc: 'Phán đoán bảng hiện tại có phải dạng cây hay không, trả về giá trị boolean' },
    // sự kiện(Tiền tố)
    {
      text: `beforeEdit(row){
  return new Promise((resolve, reject) => {
    if(row.tenTruong == 'giaTriTruong'){
      reject('Kiểm thử~');
    }else{
      resolve();
    }
  })     
}`,
      displayText: 'beforeEdit(row){}',
      desc: 'Kích hoạt khi nhấp nút chỉnh sửa dưới cột thao tác, trả vềpromiseđối tượng',
    },
    {
      text: `beforeDelete(row){
	return new Promise((resolve, reject) => {
  	if(row.tenTruong == 'giaTriTruong'){
    	reject('Kiểm thử~');
    }else{
    	resolve();
    }
  })     
}`,
      displayText: 'beforeDelete(row){}',
      desc: 'Kích hoạt khi nhấp nút xóa dưới cột thao tác, trả vềpromiseđối tượng',
    },
    { text: 'console.log()', displayText: 'console.log()', desc: 'In nhật ký' },
  ],
  form: [
    //------ Biểu mẫuapi -------
    // thuộc tính
    { text: '.loading', displayText: 'loading', superiors: 'this', desc: 'Có đang tải hay không, cái trả về là mộtrefđối tượng' },
    { text: '.isUpdate', displayText: 'isUpdate', superiors: 'this', desc: 'Có phải trang chỉnh sửa hay không, cái trả về là mộtrefđối tượng' },
    { text: '.onlineFormRef', displayText: 'onlineFormRef', superiors: 'this', desc: 'bảng chính/của biểu mẫu bảng đơnrefđối tượng' },
    { text: '.refMap', displayText: 'refMap', superiors: 'this', desc: 'Biểu mẫu bảng con/bảng contablecủarefđối tượngmap，keylà tên bảng con' },
    { text: '.subActiveKey', displayText: 'subActiveKey', superiors: 'this', desc: 'đang kích hoạt của bảng contabChuỗi tương ứng với giá trị chỉ mục, từ ‘0’bắt đầu, giá trị trả về là mộtrefđối tượng' },
    { text: '.sh', displayText: 'sh', superiors: 'this', desc: 'bảng đơn/Trạng thái hiển thị/ẩn của trường bảng chính' },
    { text: '.submitFlowFlag', displayText: 'submitFlowFlag', superiors: 'this', desc: 'Có tự động gửi quy trình sau khi gửi biểu mẫu hay không, trả về mộtrefđối tượng' },
    { text: '.subFormHeight', displayText: 'subFormHeight', superiors: 'this', desc: 'chiều cao của biểu mẫu bảng con một-một, không cần thiết lập, trả về mộtrefđối tượng' },
    { text: '.subTableHeight', displayText: 'subTableHeight', superiors: 'this', desc: 'bảng con một-nhiềutablechiều cao của, không cần thiết lập, trả về mộtrefđối tượng' },
    { text: '.tableName', displayText: 'tableName', superiors: 'this', desc: 'Tên bảng hiện tại, cái trả về là mộtrefđối tượng' },
    { text: '.$nextTick', displayText: '$nextTick', superiors: 'this', desc: 'Cái được gọi làvue3củanextTick' },
    { text: '.tenTruong_load', displayText: 'tenTruong_load', superiors: 'this', desc: 'Kiểm soát việc tải trường hay không, thiết lập thànhfalseBiểu thị trường hiện tại không tải' },
    { text: '.tenTruong_disabled', displayText: 'tenTruong_disabled', superiors: 'this', desc: 'Kiểm soát việc vô hiệu hóa trường hay không, thiết lập thànhtrueBiểu thị trường hiện tại bị vô hiệu hóa' },
    // Phương thức
    { text: '.addSubRows(tableName, rows)', displayText: 'addSubRows(tableName, rows)', superiors: 'this', desc: 'về phía bảng con một-nhiềutableThêm dữ liệu vào trong' },
    {
      text: '.changeOptions(field, options)',
      texdisplayTextt: 'changeOptions(field, options)',
      superiors: 'this',
      desc: 'Thay đổi bảng đơn/bảng chính Tùy chọn dropdown của control dropdown',
    },
    { text: '.clearSubRows(tableName)', displayText: 'clearSubRows(tableName)', superiors: 'this', desc: 'Xóa sạch bảng con một-nhiềutabledữ liệu của' },
    {
      text: '.clearThenAddRows(tableName, rows)',
      displayText: 'clearThenAddRows(tableName, rows)',
      superiors: 'this',
      desc: 'Trước tiên xóa sạch bảng con một-nhiềutabledữ liệu của, rồi thêm dữ liệu vào trong',
    },
    { text: '.getFieldsValue()', displayText: 'getFieldsValue()', superiors: 'this', desc: 'Lấy bảng chính/bảng đơn Giá trị của tất cả các trường' },
    {
      text: '.getSubTableInstance(tableName)',
      displayText: 'getSubTableInstance(tableName)',
      superiors: 'this',
      desc: 'Lấy đối tượng instance của bảng con, đối tượng này có thể gọi bảng contablephương thức của',
    },
    { text: '.setFieldsValue(row)', displayText: 'setFieldsValue(row)', superiors: 'this', desc: 'Thiết lập bảng chính/bảng đơn Giá trị của trường' },
    {
      text: '.triggleChangeValues(values,id,target)',
      displayText: 'triggleChangeValues(values,id,target)',
      superiors: 'this',
      desc: 'Thay đổi bảng đơn/bảng chính/bảng con Giá trị của trường, thường dùng chochangesự kiện, trong đóid，targetCần thông quachangelấy qua tham số tích hợp của sự kiện, nếu không truyềnid，targetgiá trị của, thì cái thay đổi là trường của bảng chính',
    },
    { text: '.triggleChangeValue(field, value)', displayText: 'triggleChangeValue(field, value)', superiors: 'this', desc: 'Thiết lập bảng đơn/bảng chính Giá trị của trường' },
    {
      text: '.onlineFormValueChange(field, value, otherValus)',
      displayText: 'onlineFormValueChange(field, value, otherValus)',
      superiors: 'this',
      desc: 'Sau khi định nghĩa, khi giá trị biểu mẫu thay đổi sẽ kích hoạt phương thức này (vìjsTăng cườnghookCách thức này không hỗ trợ cách gốconlChange, vì vậy định nghĩa phương thức này)',
    },
    {
      text: '.changeSubTableOptions(tableName, field, options)',
      displayText: 'changeSubTableOptions(tableName, field, options)',
      superiors: 'this',
      desc: 'Thay đổi ô dropdown của bảng con một-mộtoptions',
    },
    {
      text: '.changeSubFormbleOptions(tableName, field, options)',
      displayText: 'changeSubFormbleOptions(tableName, field, options)',
      superiors: 'this',
      desc: 'Thay đổi ô dropdown của bảng con một-nhiềuoptions',
    },
    {
      text: '.changeRemoteOptions({ field, dict, label, type?, subTableName? })',
      displayText: 'changeRemoteOptions({ field, dict, label, type?, subTableName? })',
      superiors: 'this',
      desc: 'Thay đổi ô dropdown độngoptions',
    },
    {
      text: '.submitFormAndFlow()',
      displayText: 'submitFormAndFlow()',
      superiors: 'this',
      desc: 'Gửi biểu mẫu đồng thời khởi tạo quy trình',
    },
    // Sự kiện tiền tố khi gửi
    {
      text: `beforeSubmit(row){
	return new Promise((resolve, reject)=>{
    //Tại đây mô phỏng thời gian chờ, có thể cần khởi tạo yêu cầu
    setTimeout(()=>{
      if(row.tenTruong == 'giaTriTruong'){
        // Khi một trường nào đó không thỏa mãn yêu cầu thì có thểreject 
        reject('Kiểm thử~');
      }else{
        resolve();
      }
    },3000)
  })
}`,
      displayText: 'beforeSubmit(row){}',
      desc: 'Sự kiện tiền tố khi gửi',
    },
    // Sự kiện tải biểu mẫu
    {
      text: `loaded(){
  this.$nextTick(()=>{
    // let text = 'Kiểm thửjsPhần tăng cường thiết lập giá trị mặc định';
    // if(this.isUpdate.value === true){
    //   text = 'Kiểm thửjsPhần tăng cường sửa đổi giá trị biểu mẫu';
    // }
    this.setFieldsValue({
      tenTruong: giaTriSauKhiSua
    })
  })
}`,
      displayText: 'loaded(){}',
      desc: 'Sự kiện tải biểu mẫu',
    },
    // bảng đơn#Sự kiện thay đổi giá trị biểu mẫu
    {
      text: `onlChange(){
  return {
    tenTruong(){
      let value = event.value
      console.log(value)
      this.triggleChangeValues({'tenTruong': 'Giá trị sau khi sửa'})
    }
  }
 }`,
      displayText: 'onlChange(){}',
      desc: 'bảng đơn#Sự kiện thay đổi giá trị biểu mẫu',
    },
    // bảng con#Sự kiện thay đổi giá trị biểu mẫu
    {
      text: `tenBangCon_onlChange(){
  return {
    tenTruong(){
      let value = event.value;
      console.log(value);
      let row = {'tenTruong': 'Kiểm thử thay đổi giá trị một-nhiều:'+value};
      this.triggleChangeValues(row, event.row.id, event.target)
  }
  }
}`,
      displayText: 'tenBangCon_onlChange(){}',
      desc: 'bảng con#Sự kiện thay đổi giá trị biểu mẫu',
    },
    // Con sửa chính#Sự kiện thay đổi giá trị biểu mẫu
    {
      text: `tenBangCon_onlChange(){
  return {
    truongBangCon01(){
      this.getSubTableInstance('tenBangCon').getValues((err,values)=>{
        this.triggleChangeValues({'tenTruongBangChinh': 'Giá trị sau khi sửa'})
      }) 
    },
  }
}
`,
      displayText: 'tenBangCon_onlChange(){}',
      desc: 'Con sửa chính#Sự kiện thay đổi giá trị biểu mẫu',
    },
    // jsPhần tăng cường thực hiện liên động dropdown
    {
      text: `onlChange(){
  return {
    tenTruong01(){
      let value = event.value
      this.changeOptions('tenTruong02', 'Giá trị sau khi sửa');
    }
    tenTruong02(){
      let value = event.value
      this.changeOptions('tenTruong03', 'Giá trị sau khi sửa');
    }
  }
}`,
      displayText: 'changeOptions()',
      desc: 'jsPhần tăng cường thực hiện liên động dropdown',
    },
    { text: 'console.log()', displayText: 'console.log()', desc: 'In nhật ký' },
  ],
  common: [
    // JSTăng cường httpYêu cầu
    {
      text: `getAction('requestUrl', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'getAction(url, param)',
      desc: 'getYêu cầu',
    },
    {
      text: `postAction('requestUrl', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'postAction(url, param)',
      desc: 'postYêu cầu',
    },
    {
      text: `putAction('requestUrl', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'putAction(url, param)',
      desc: 'putYêu cầu',
    },
    {
      text: `deleteAction('requestUrl', { 'key': 'value'}).then(res => {
  console.log(res)
})`,
      displayText: 'deleteAction(url, param)',
      desc: 'deleteYêu cầu',
    },
    { text: 'this', displayText: 'this', desc: 'Ngữ cảnh' },
    {
      text: '.openCustomModal({title,width,row,formComponent,requestUrl,hide,show})',
      displayText: 'openCustomModal({title,width,row,formComponent,requestUrl,hide,show})',
      desc: 'Mở một popup-Tham khảo JsPhần tăng cường mở popup tùy chỉnh',
    },
  ],
};
