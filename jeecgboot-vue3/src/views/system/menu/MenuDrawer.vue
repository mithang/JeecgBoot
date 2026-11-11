<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" showFooter :width="adaptiveWidth" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" class="menuForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, useAttrs } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema, ComponentTypes } from './menu.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { list, saveOrUpdateMenu } from './menu.api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';
  import { useI18n } from "/@/hooks/web/useI18n";
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const { adaptiveWidth } = useDrawerAdaptiveWidth();
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const menuType = ref(0);
  const isButton = (type) => type === 2;
  const [registerForm, { setProps, resetFields, setFieldsValue, updateSchema, validate, clearValidate }] = useForm({
    labelCol: {
      md: { span: 4 },
      sm: { span: 6 },
    },
    wrapperCol: {
      md: { span: 20 },
      sm: { span: 18 },
    },
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    setDrawerProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    menuType.value = data?.record?.menuType;

    //Lấy thông tin cây thả xuống
    const treeData = await list();
    updateSchema([
      {
        field: 'parentId',
        // Giải thích logic mã: 【QQYUN-8379】Quốc tế hóa menu ở trang Quản lý menu
        componentProps: { treeData: translateMenu(treeData, 'name') },
      },
      {
        field: 'name',
        label: isButton(unref(menuType)) ? 'Nút/Quyền hạn' : 'Tên menu',
      },
      {
        field: 'url',
        required: !isButton(unref(menuType)),
        componentProps: {
          onChange: (e) => onUrlChange(e.target.value),
        },
      },
    ]);

    // Dù thêm mới hay chỉnh sửa đều có thể đặt giá trị cho biểu mẫu
    if (typeof data.record === 'object') {
      let values = { ...data.record };
      setFieldsValue(values);
      onUrlChange(values.url);
    }
    //Trong trường hợp loại nút, khi chỉnh sửa thì xóa bỏ kiểm tra của địa chỉ
    if (menuType.value == 2) {
      clearValidate();
    }
    //Vô hiệu hóa biểu mẫu
    setProps({ disabled: !attrs.showFooter });
  });
  //Lấy tiêu đề hộp thoại
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới menu' : 'Chỉnh sửa menu'));
  //Sự kiện gửi
  async function handleSubmit() {
    try {
      const values = await validate();
      // iframeTương thích
      if (ComponentTypes.IFrame === values.component) {
        values.component = values.frameSrc;
      }
      setDrawerProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateMenu(values, unref(isUpdate));
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  /** url Khi thay đổi, đặt động tên componentplaceholder */
  function onUrlChange(url) {
    let placeholder = '';
    let httpUrl = url;
    if (url != null && url != '') {
      if (url.startsWith('/')) {
        url = url.substring(1);
      }
      url = url.replaceAll('/', '-');
      // Đánh dấu đặc biệt
      url = url.replaceAll(':', '@');
      placeholder = `${url}`;
    } else {
      placeholder = 'Vui lòng nhập tên component';
    }
    updateSchema([{ field: 'componentName', componentProps: { placeholder } }]);
    // Giải thích logic mã: [QQYUN-4058]Xử lý thông minh khi thêm menu------------
    if (httpUrl != null && httpUrl != '') {
      if (httpUrl.startsWith('http://') || httpUrl.startsWith('https://')) {
        setFieldsValue({ component: httpUrl });
      }
    }
  }

  /**
  * 2024-03-06
  * liaozhiyang
  * Dịch tên menu
  */
  function translateMenu(data, key) {
    if (data?.length) {
      const { t } = useI18n();
      data.forEach((item) => {
        if (item[key]) {
          if (item[key].includes("t('") && t) {
            item[key] = new Function('t', `return ${item[key]}`)(t);
          }
        }
        if (item.children?.length) {
          translateMenu(item.children, key);
        }
      });
    }
    return data;
  }
</script>
