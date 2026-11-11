<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Biến người dùng" :width="1000" @ok="handleSubmit" destroyOnClose>
    <div class="p-4">
      <div class="mb-4 text-gray-500"> Dùng để lưu trữ dữ liệu cần lưu trữ bền vững và đọc trong quá trình mỗi người dùng sử dụng dự án, ví dụ như tùy chọn ngôn ngữ, cài đặt cá nhân hóa của người dùng. </div>

      <JVxeTable
        ref="tableRef"
        toolbar
        dragSort
        :maxHeight="500"
        :columns="columns"
        :dataSource="dataSource"
        :toolbarConfig="{ btns: ['remove', 'clearSelection'] }"
      >
        <template #toolbarSuffix>
          <a-button type="primary" @click="handleAdd">
            <Icon icon="ant-design:plus-outlined" />
            Thêm mới
          </a-button>
        </template>

        <template #action="props">
          <div class="action-group">
            <a-switch v-model:checked="props.row.enable" checked-children="Mở" un-checked-children="Đóng" size="small" class="ml-2" />

            <a-popconfirm title="Bạn có chắc muốn xóa?" @confirm="handleDelete(props)">
              <Icon icon="ant-design:delete-outlined" class="cursor-pointer hover:text-red-500 ml-2" size="18" color="red" />
            </a-popconfirm>
          </div>
        </template>
      </JVxeTable>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { Icon } from '/src/components/Icon';
  import { Button, Checkbox, Switch, Popconfirm } from 'ant-design-vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/src/components/jeecg/JVxeTable/types';
  import { useMessage } from '/src/hooks/web/useMessage';

  export default defineComponent({
    name: 'AiUserVariablesModal',
    components: {
      BasicModal,
      Icon,
      AButton: Button,
      ACheckbox: Checkbox,
      ASwitch: Switch,
      APopconfirm: Popconfirm,
    },
    emits: ['register', 'ok'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      const tableRef = ref<JVxeTableInstance>();

      // Định nghĩa cấu hình cột
      const columns = ref<JVxeColumn[]>([
        {
          title: 'Tên',
          key: 'name',
          type: JVxeTypes.input,
          width: 200,
          placeholder: 'Vui lòng nhập tên biến',
          validateRules: [
            { required: true, message: 'Tên không được để trống' },
            { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: 'Chỉ hỗ trợ chữ cái, chữ số và gạch dưới, và bắt đầu bằng chữ cái' },
          ],
        },
        {
          title: 'Mô tả',
          key: 'description',
          type: JVxeTypes.input,
          width: 300,
          placeholder: 'Mô tả trường',
        },
        {
          title: 'Giá trị mặc định',
          key: 'defaultValue',
          type: JVxeTypes.input,
          width: 200,
          placeholder: 'Giá trị mặc định',
        },
        {
          title: 'Thao tác',
          key: 'action',
          type: JVxeTypes.slot,
          slotName: 'action',
          width: 220,
          align: 'center',
        },
      ]);

      const dataSource = ref<any[]>([]);

      const [registerModal, { setModalProps, closeModal }] = useModalInner((data) => {
        // Nếu đã truyền vào dữ liệu có sẵn, tiến hành khởi tạo
        if (data && data.variables) {
          dataSource.value = JSON.parse(data.variables);
        } else {
          dataSource.value = [];
        }
      });

      // Thêm dòng mới
      const handleAdd = () => {
        if (dataSource.value.length > 9) {
          createMessage.warn('Hỗ trợ tối đa 10 biến!');
          return;
        }
        tableRef.value?.addRows({
          name: '',
          description: '',
          defaultValue: '',
          enable: true,
        });
      };

      // Xóa dòng
      const handleDelete = (props) => {
        tableRef.value?.removeRows(props.row);
      };

      const handleSubmit = async () => {
        // Kiểm chứng bảng
        const errMap = await tableRef.value?.validateTable();
        if (errMap) {
          createMessage.error('Vui lòng kiểm tra việc điền biểu mẫu có đúng hay không');
          return;
        }

        // Lấy dữ liệu toàn lượng
        const tableData = tableRef.value?.getTableData();
        console.log('Lưu biến người dùng:', tableData);

        closeModal();
        emit('ok', tableData);
        createMessage.success('Lưu thành công');
      };

      return {
        registerModal,
        tableRef,
        columns,
        dataSource,
        handleAdd,
        handleDelete,
        handleSubmit,
      };
    },
  });
</script>

<style scoped>
  .p-4 {
    padding: 1rem;
  }

  .mb-4 {
    margin-bottom: 1rem;
  }

  .text-gray-500 {
    color: #6b7280;
  }

  .ml-2 {
    margin-left: 0.5rem;
  }

  .cursor-pointer {
    cursor: pointer;
  }

  .hover\:text-red-500:hover {
    color: #ef4444;
  }

  .action-group {
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
