<template>
  <div>
    <b>Phím tắt thao tác bàn phím:</b>
    <div style="border: 1px solid #cccccc; padding: 8px; width: 740px">
      <pre>
             F2 | Nếu tồn tại, kích hoạt ô thành trạng thái chỉnh sửa
            Esc | Nếu tồn tại, hủy trạng thái chỉnh sửa ô
              ↑ | Nếu tồn tại, di chuyển đến ô phía trên
              ↓ | Nếu tồn tại, di chuyển đến ô phía dưới
              ← | Nếu tồn tại, di chuyển đến ô phía bên trái
              → | Nếu tồn tại, di chuyển đến ô phía bên phải
            Tab | Nếu tồn tại thì di chuyển sang ô bên phải; nếu di chuyển đến cột cuối cùng thì bắt đầu di chuyển từ hàng tiếp theo, cứ thế lặp lại
    Shift + Tab | Nếu tồn tại thì di chuyển sang ô bên trái, nếu di chuyển đến cột đầu tiên thì bắt đầu di chuyển từ hàng trước đó, cứ thế lặp lại
          Enter | Nếu tồn tại, hủy chỉnh sửa ô và di chuyển đến ô phía dưới
  Shift + Enter | Nếu tồn tại, hủy chỉnh sửa ô và di chuyển đến ô phía trên</pre
      >
    </div>

    <JVxeTable ref="tableRef" stripe toolbar rowNumber rowSelection keyboardEdit :columns="columns" :dataSource="dataSource"> </JVxeTable>
  </div>
</template>
<script lang="ts">
  import { ref, onMounted, nextTick, defineComponent } from 'vue';
  import { Popconfirm } from 'ant-design-vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';

  export default defineComponent({
    name: 'JVxeDemo5',
    components: { [Popconfirm.name]: Popconfirm },
    setup() {
      const tableRef = ref<JVxeTableInstance>();
      const columns = ref<JVxeColumn[]>([
        {
          title: 'Văn bản một dòng',
          key: 'input',
          type: JVxeTypes.input,
          width: 220,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
        },
        {
          title: 'Văn bản nhiều dòng',
          key: 'textarea',
          type: JVxeTypes.textarea,
          width: 240,
        },
        {
          title: 'Số',
          key: 'number',
          type: JVxeTypes.inputNumber,
          width: 120,
          defaultValue: 32,
        },
        {
          title: 'Ngày giờ',
          key: 'datetime',
          type: JVxeTypes.datetime,
          width: 240,
          defaultValue: '2019-04-30 14:51:22',
          placeholder: 'Vui lòng chọn',
        },
        {
          title: 'Thời gian',
          key: 'time',
          type: JVxeTypes.time,
          width: 220,
          defaultValue: '14:52:22',
          placeholder: 'Vui lòng chọn',
        },
        {
          title: 'Dropdown',
          key: 'select',
          type: JVxeTypes.select,
          width: 220,
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
          title: 'Checkbox',
          key: 'checkbox',
          type: JVxeTypes.checkbox,
          // width: 100,
          customValue: ['Y', 'N'], // true ,false
          defaultChecked: false,
        },
      ]);
      const dataSource = ref([]);

      function handleView(props) {
        // Giới thiệu tham số:
        // props.value          Giá trị của ô hiện tại
        // props.row            Dữ liệu hàng hiện tại
        // props.rowId          Hàng hiện tạiID
        // props.rowIndex       Chỉ số hàng hiện tại
        // props.column         Cấu hình cột hiện tại
        // props.columnIndex    Chỉ số cột hiện tại
        // props.$table         vxe-tableInstance, có thể gọivxe-tablePhương thức tích hợp sẵn
        // props.scrolling      Có đang cuộn không
        // props.reloadEffect   Có bật hiệu ứng làm mới dữ liệu không
        // props.triggerChange  Kích hoạt sự kiện change, dùng để thay đổi giá trị của slot
        console.log('props: ', props);
      }

      function handleDelete({ row }) {
        // Ví dụ sử dụng: Xóa dòng đang thao tác
        tableRef.value?.removeRows(row);
      }

      onMounted(async () => {
        console.log(tableRef.value);
        await nextTick();
        // Mặc định thêm năm dòng dữ liệu
        tableRef.value!.addRows([{ input: 'input_1' }, { input: 'input_2' }, { input: 'input_3' }, { input: 'input_4' }, { input: 'input_5' }], {
          setActive: false,
        });
      });

      return { tableRef, columns, dataSource, handleView, handleDelete };
    },
  });
</script>
