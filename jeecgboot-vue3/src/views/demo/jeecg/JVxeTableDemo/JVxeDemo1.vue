<template>
  <a-space>
    <a-button @click="onToggleLoading">Chuyển đổi trạng thái tải</a-button>
    <a-button @click="onToggleDisabled">Chuyển đổi trạng thái vô hiệu hóa</a-button>
  </a-space>
  <!--Trường hợp sử dụng này phải dùng height, dùng maxHeight thì tầng dưới có vấn đề-->
  <JVxeTable
    ref="tableRef"
    stripe
    toolbar
    rowNumber
    rowSelection
    rowExpand
    resizable
    asyncRemove
    clickSelectRow
    :height="480"
    :checkboxConfig="{ range: true }"
    :disabledRows="{ input: ['text--16', 'text--18'] }"
    :loading="loading"
    :disabled="disabled"
    :columns="columns"
    :dataSource="dataSource"
    @removed="onJVxeRemove"
    @valueChange="handleValueChange"
    @blur="handleBlur"
    :custom="true"
  >
    <template #toolbarSuffix>
      <a-button @click="handleTableCheck">Kiểm tra biểu mẫu</a-button>
      <a-tooltip placement="top" title="Lấy giá trị, bỏ qua kiểm tra biểu mẫu" :autoAdjustOverflow="true">
        <a-button @click="onGetData">Lấy dữ liệu</a-button>
      </a-tooltip>
      <a-tooltip placement="top" title="Mô phỏng tải 1000 bản ghi dữ liệu" :autoAdjustOverflow="true">
        <a-button @click="handleTableSet">Đặt giá trị</a-button>
      </a-tooltip>
      <a-button @click="onGetSelData">Lấy dữ liệu đã chọn</a-button>
      <a-button @click="onClearSel">Xóa lựa chọn</a-button>
      <a-button @click="onDelFirst">Xóa dòng dữ liệu đầu tiên</a-button>
      <a-button @click="onDelSel">Xóa dữ liệu đã chọn</a-button>
    </template>

    <template #expandContent="props">
      <div style="padding: 20px">
        <span>Hello! My name is: {{ props.row.input }}!</span>
      </div>
    </template>

    <template #planTimeRangeSlot="{ row, triggerChange }">
      <a-range-picker
        :value="row.planTimeRange"
        value-format="YYYY-MM-DD"
        :bordered="false"
        @change="(dates) => triggerChange(dates)"
      />
    </template>

    <template #myAction="props">
      <a @click="onLookRow(props)">Xem</a>
      <a-divider type="vertical" />
      <Popconfirm title="Bạn có chắc chắn muốn xóa không?" @confirm="onDeleteRow(props)">
        <a>Xóa</a>
      </Popconfirm>
    </template>
  </JVxeTable>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { random } from 'lodash-es';
  import { buildUUID } from '/@/utils/uuid';
  import dayjs from 'dayjs';
  import { pushIfNotExist } from '/@/utils/common/compUtils';

  const { createMessage } = useMessage();
  const tableRef = ref<JVxeTableInstance>();
  const loading = ref(false);
  const disabled = ref(false);
  const columns = ref<JVxeColumn[]>([
    {
      title: 'ID',
      key: 'id',
      type: JVxeTypes.hidden,
    },
    {
      title: 'Không thể chỉnh sửa',
      key: 'noEdit',
      type: JVxeTypes.normal,
      width: 180,
      defaultValue: 'noEdit-new',
    },
    {
      title: 'Văn bản một dòng',
      key: 'input',
      type: JVxeTypes.input,
      width: 180,
      defaultValue: '',
      placeholder: 'Vui lòng nhập${title}',
      validateRules: [
        {
          required: true, // Bắt buộc
          message: 'Vui lòng nhập${title}', // Văn bản hiển thị
        },
        {
          pattern: /^[a-z|A-Z][a-z|A-Z\d_-]*$/, // Regex
          message: 'Phải bắt đầu bằng chữ cái, có thể chứa số, dấu gạch dưới, dấu gạch ngang',
        },
        {
          unique: true,
          message: '${title}không được trùng lặp',
        },
        {
          handler({ cellValue, row, column }, callback, target) {
            // cellValue Giá trị đang kiểm chứng
            // callback(flag, message) Phương thức phải thực thi và chỉ được thực thi một lần
            //          flag = Có vượt qua kiểm chứng hay không, không điền hoặc điền null nghĩa là không thực hiện bất kỳ thao tác nào
            //          message = Loại nhắc nhở, mặc định dùng loại đã cấu hình message
            // target Đối tượng instance chỉnh sửa hàng
            if (cellValue === 'abc') {
              callback(false, '${title}không được làabc'); // false = Không đạt kiểm chứng
            } else {
              callback(true); // true = Đã qua kiểm chứng
            }
          },
          message: '${title}Gợi ý mặc định',
        },
      ],
    },
    {
      title: 'Văn bản nhiều dòng',
      key: 'textarea',
      type: JVxeTypes.textarea,
      width: 200,
    },
    {
      title: 'Số',
      key: 'number',
      type: JVxeTypes.inputNumber,
      width: 80,
      defaultValue: 32,
      // [Cột thống kê]sum = Tổng,average = Giá trị trung bình
      statistics: ['sum', 'average'],
    },
    {
      title: 'Dropdown',
      key: 'select',
      type: JVxeTypes.select,
      width: 180,
      // Tùy chọn danh sách thả xuống
      options: [
        { title: 'String', value: 'string' },
        { title: 'Integer', value: 'int' },
        { title: 'Double', value: 'double' },
        { title: 'Boolean', value: 'boolean' },
      ],
      // allowInput: true,
      allowSearch: true,
      placeholder: 'Vui lòng chọn',
    },
    {
      title: 'Dropdown_Từ điển',
      key: 'select_dict',
      type: JVxeTypes.select,
      width: 180,
      options: [],
      dictCode: 'sex',
      placeholder: 'Vui lòng chọn',
    },
    // update-begin--author:liaozhiyang---date:20260507---for：【issues/7617]Bảng con vxetable hỗ trợ danh mục popup
    {
      title: 'PopupTừ điển(Người dùng)',
      key: 'popupUser',
      type: JVxeTypes.popupDict,
      width: 220,
      // fieldConfig Định dạng: Báo cáo cgreportcode,Hiển thị trường(label),Trường lưu trữ(value)
      fieldConfig: 'report_user,realname,username',
      multi: true,
      placeholder: 'Vui lòng chọn',
    },
    // update-end--author:liaozhiyang---date:20260507---for：【issues/7617]Bảng con vxetable hỗ trợ danh mục popup
    {
      title: 'Dropdown_Chọn nhiều',
      key: 'select_multiple',
      type: JVxeTypes.selectMultiple,
      width: 205,
      options: [
        { title: 'String', value: 'string' },
        { title: 'Integer', value: 'int' },
        { title: 'Double', value: 'double' },
        { title: 'Boolean', value: 'boolean' },
      ],
      defaultValue: ['int', 'boolean'], // Nhiều mục mặc định
      // defaultValue: 'string,double,int', // Cũng có thể sử dụng cách này
      placeholder: 'Chọn nhiều',
    },
    {
      title: 'Dropdown_Tìm kiếm',
      key: 'select_search',
      type: JVxeTypes.selectSearch,
      width: 180,
      options: [
        { title: 'String', value: 'string' },
        { title: 'Integer', value: 'int' },
        { title: 'Double', value: 'double' },
        { title: 'Boolean', value: 'boolean' },
      ],
    },
    {
      title: 'Ngày giờ',
      key: 'datetime',
      type: JVxeTypes.datetime,
      width: 200,
      defaultValue: '2019-04-30 14:52:22',
      placeholder: 'Vui lòng chọn',
    },
    {
      title: 'Thời gian',
      key: 'time',
      type: JVxeTypes.time,
      width: 200,
      defaultValue: '14:52:22',
      placeholder: 'Vui lòng chọn',
    },
    {
      title: 'Checkbox',
      key: 'checkbox',
      type: JVxeTypes.checkbox,
      width: 100,
      customValue: ['Y', 'N'], // true ,false
      defaultChecked: false,
    },
    {
      title: 'Ngày bắt đầu dự kiến ~ Ngày kết thúc dự kiến',
      key: 'planTimeRange',
      type: JVxeTypes.slot,
      width: 280,
      slotName: 'planTimeRangeSlot',
    },
    {
      title: 'Control cây tùy chỉnh',
      key: 'sel_tree_demo',
      type: JVxeTypes.treeSelect,
      width: 200,
      // dict Định dạng: Tên bảng,Trường văn bản,Trường lưu trữ
      dict: 'sys_category,name,id',
      pidField: 'pid',
      pidValue: '0',
      hasChildField: 'has_child',
      multiple: true,
      placeholder: 'Vui lòng chọn',
    },
    {
      title: 'Cây từ điển phân loại',
      key: 'cat_tree_demo',
      type: JVxeTypes.catTreeSelect,
      width: 200,
      // pcode: Mã phân loại gốc,'0' Biểu thị tải tất cả nút gốc
      pcode: 'B01',
      multiple: true,
      placeholder: 'Vui lòng chọn',
    },
    {
      title: 'Thao tác',
      key: 'action',
      type: JVxeTypes.slot,
      fixed: 'right',
      minWidth: 120,
      align: 'center',
      slotName: 'myAction',
    },
  ]);
  const dataSource = ref<any[]>([]);

  /* Tạo dữ liệu ngẫu nhiên */
  function randomPage(current, pageSize, isLoading = false) {
    if (isLoading) {
      loading.value = true;
    }

    let randomDatetime = () => {
      let time = random(1000, 9999999999999);
      return dayjs(new Date(time)).format('YYYY-MM-DD HH:mm:ss');
    };

    let limit = (current - 1) * pageSize;

    let options = ['string', 'int', 'double', 'boolean'];

    let begin = Date.now();
    let values: any[] = [];
    for (let i = 0; i < pageSize; i++) {
      values.push({
        id: buildUUID(),
        noEdit: `noEdit-${limit + i + 1}`,
        input: `text-${limit + i + 1}`,
        textarea: `textarea-${limit + i + 1}`,
        number: random(0, 233),
        select: options[random(0, 3)],
        select_dict: random(1, 2).toString(),
        select_multiple: (() => {
          let length = random(1, 4);
          let arr = [];
          for (let j = 0; j < length; j++) {
            pushIfNotExist(arr, options[random(0, 3)]);
          }
          return arr.join(',');
        })(),
        select_search: options[random(0, 3)],
        // update-begin--author:liaozhiyang---date:20260507---for：【QQYUN-7961]Bảng con vxetable hỗ trợ danh mục popup
        popupUser: ['jeecg', 'admin'][random(0, 1)],
        // update-end--author:liaozhiyang---date:20260507---for：【QQYUN-7961]Bảng con vxetable hỗ trợ danh mục popup
        datetime: randomDatetime(),
        checkbox: ['Y', 'N'][random(0, 1)],
        planTimeRange: [dayjs().subtract(random(1, 30), 'day').format('YYYY-MM-DD'), dayjs().add(random(1, 30), 'day').format('YYYY-MM-DD')],
      });
    }

    dataSource.value = values;
    let end = Date.now();
    let diff = end - begin;

    if (isLoading && diff < pageSize) {
      setTimeout(() => (loading.value = false), pageSize - diff);
    }
  }

  randomPage(0, 20, true);

  function onLookRow(props) {
    createMessage.success('Vui lòng xem kết quả trong console');
    // Giới thiệu tham số:
    // props.value          Giá trị của ô hiện tại
    // props.row            Dữ liệu hàng hiện tại
    // props.rowId          Hàng hiện tạiID
    // props.rowIndex       Chỉ số hàng hiện tại
    // props.column         Cấu hình cột hiện tại
    // props.columnIndex    Chỉ số cột hiện tại
    // props.$table         vxeĐối tượng instance, có thể gọi phương thức tích hợp của vxe
    // props.target         JVXEInstance, có thể gọi phương thức tích hợp của JVXE
    // props.caseId         JVXEID duy nhất của instanceID
    // props.scrolling      Có đang cuộn không
    // props.triggerChange  Kích hoạt sự kiện change, dùng để thay đổi giá trị của slot
    console.log('Xem: ', { props });
  }

  // async function onDeleteRow(props) {
  //   // Gọi đồng bộ phương thức xóa
  //   const res = await tableRef.value?.removeRows(props.row);
  //   if (res && res.rows.length > 0) {
  //     createMessage.success('Xóa thành công');
  //   }
  // }

  async function onDeleteRow(props) {
    // Gọi không đồng bộ phương thức xóa
    const res = await tableRef.value?.removeRows(props.row, true);
    console.log('Xóa thành công~', res);
  }

  function handleValueChange(event) {
    console.log('handleValueChange.event: ', event);
  }

  // Giải thích logic mã: 【issues/636] JVxeTable thêm sự kiện blur
  function handleBlur(event){
    console.log("blur",event);
  }
  /** Kiểm tra biểu mẫu */
  function handleTableCheck() {
    tableRef.value!.validateTable().then((errMap) => {
      if (errMap) {
        console.log('Kiểm chứng biểu mẫu chưa đạt:', { errMap });
        createMessage.error('Kiểm chứng chưa đạt, vui lòng xem chi tiết trong console');
      } else {
        createMessage.success('Kiểm chứng thành công');
      }
    });
  }

  /** Lấy giá trị, bỏ qua kiểm tra biểu mẫu */
  function onGetData() {
    const values = tableRef.value!.getTableData();
    console.log('Lấy giá trị:', { values });
    createMessage.success('Lấy giá trị thành công, vui lòng xem kết quả trong console');
  }

  /** Mô phỏng tải 1000 bản ghi dữ liệu */
  function handleTableSet() {
    randomPage(1, 1000, true);
  }

  function onDelFirst() {
    const xTable = tableRef.value!.getXTable();
    const record = xTable.getTableData().fullData[0];
    tableRef.value!.removeRows(record);
  }

  function onDelSel() {
    const xTable = tableRef.value!.getXTable();
    xTable.removeCheckboxRow();
  }

  function onGetSelData() {
    createMessage.info('Vui lòng xem bảng điều khiển (console)');
    console.log(tableRef.value!.getSelectionData());
  }

  function onClearSel() {
    tableRef.value!.clearSelection();
  }

  function onToggleLoading() {
    loading.value = !loading.value;
  }

  function onToggleDisabled() {
    disabled.value = !disabled.value;
  }

  function doDelete(deleteRows) {
    let rowId;
    return new Promise((resolve) => {
      if (Array.isArray(deleteRows)) {
        rowId = deleteRows.filter((row) => row.id);
      } else {
        rowId = deleteRows.id;
      }
      console.log('Xóa rowId: ', rowId);
      setTimeout(() => resolve(true), 1500);
    });
  }

  /** Ví dụ xóa bất đồng bộ */
  async function onJVxeRemove(event) {
    const hideLoading = createMessage.loading('Đang xóa...', 0);
    try {
      // 1. Truyền về máy chủ event.deleteRows để xóa
      let flag = await doDelete(event.deleteRows);
      if (flag) {
        // Lưu ý: Nếu đã bật trạng thái loading của bảng, bắt buộc phải dừng trước rồi mới xóa, nếu không sẽ không thể xóa dữ liệu khỏi bảng
        // 2. Gọi event.confirmRemove Phương thức xác nhận xóa thành công
        // await tableRef.value!.removeSelection();
        await event.confirmRemove()
        createMessage.success('Xóa thành công!');
      } else {
        // 3. Nếu xóa thất bại, không gọi event.confirmRemove() Phương thức sẽ không xóa dữ liệu
        createMessage.warn('Xóa thất bại!');
      }
    } finally {
      hideLoading();
    }
  }
</script>
