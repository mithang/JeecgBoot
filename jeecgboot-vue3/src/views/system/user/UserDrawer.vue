<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="getTitle"
    :width="adaptiveWidth"
    @ok="handleSubmit"
    :showFooter="showFooter"
    destroyOnClose
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { defineComponent, ref, computed, unref, useAttrs } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './user.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { saveOrUpdateUser, getUserRoles, getUserDepartList, getAllRolesListNoByTenant, getAllRolesList } from './user.api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';
  import { getTenantId } from "/@/utils/auth";

  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const rowId = ref('');
  const departOptions = ref([]);
  let isFormDepartUser = false;
  let isRoleDisabled = false;
  let disabledDepartValue = '';
  //Cấu hình biểu mẫu
  const [registerForm, { setProps, resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 90,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  // TODO [VUEN-527] https://www.teambition.com/task/6239beb894b358003fe93626
  const showFooter = ref(true);
  //Gán giá trị cho biểu mẫu
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    showFooter.value = data?.showFooter ?? true;
    setDrawerProps({ confirmLoading: false, showFooter: showFooter.value });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      //Định nghĩa thông tin đơn vị thuê thành mảng
   /*   if (data.record.relTenantIds && !Array.isArray(data.record.relTenantIds)) {
        data.record.relTenantIds = data.record.relTenantIds.split(',');
      } else {
        //【issues/I56C5I】Trong Quản lý người dùng, nhấn hai lần liên tiếp vào chỉnh sửa thì cấu hình đơn vị thuê bị mất
        //data.record.relTenantIds = [];
      }*/

      //Kiểm tra vai trò/gán giá trị/xử lý try catch, nếu không việc chỉnh sửa sẽ có vấn đề
      try {
        const userRoles = await getUserRoles({ userid: data.record.id });
        if (userRoles && userRoles.length > 0) {
          data.record.selectedroles = userRoles;
        }
      } catch (error) {}

      //Kiểm tra phòng ban trực thuộc/gán giá trị
      const userDepart = await getUserDepartList({ userId: data.record.id });
      if (userDepart && userDepart.length > 0) {
        data.record.selecteddeparts = userDepart;
        let selectDepartKeys = Array.from(userDepart, ({ key }) => key);
        data.record.selecteddeparts = selectDepartKeys.join(',');
        departOptions.value = userDepart.map((item) => {
          return { label: item.title, value: item.key };
        });
      }
      //Phòng ban phụ trách/gán giá trị
      data.record.departIds && !Array.isArray(data.record.departIds) && (data.record.departIds = data.record.departIds.split(','));
      // Giải thích logic mã: [issues/772]Tránh hiển thị bất thường do giá trị trống------------
      data.record.departIds =  (!data.record.departIds || data.record.departIds == '') ? [] : data.record.departIds;
    }
    //Xử lý tình huống danh sách người dùng của vai trò(Có quan hệ với danh sách vai trò)
    data.selectedroles && (await setFieldsValue({ selectedroles: data.selectedroles }));
    // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1737】API chỉnh sửa người dùng phòng ban, bổ sung tham sốupdateFromPage:"deptUsers"
    isFormDepartUser = data?.departDisabled === true || data?.roleDisabled === true;
    isRoleDisabled = data?.roleDisabled === true;
    disabledDepartValue = data?.departDisabled ? data.record?.selecteddeparts || '' : '';
    // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1737】API chỉnh sửa người dùng phòng ban, bổ sung tham sốupdateFromPage:"deptUsers"
    //Khi chỉnh sửa thì ẩn mật khẩu / danh sách vai trò ẩn thông tin vai trò / ở Phòng ban của tôi thì ẩn phòng ban trực thuộc
    updateSchema([
      {
        field: 'password',
        // 【QQYUN-8324】
        ifShow: !unref(isUpdate),
      },
      {
        field: 'confirmPassword',
        ifShow: !unref(isUpdate),
      },
      {
        field: 'departIds',
        componentProps: { options: departOptions },
      },
      {
        field: 'selecteddeparts',
        //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1141]【Quản lý hệ thống】Phòng ban và vai trò của người dùng không hiển thị khi chỉnh sửa tại mục Phòng ban của tôi------------
        show: true,
        componentProps: { disabled: !!data?.departDisabled },
        //update-end---author:wangshuai ---date:20260811  for：[LHZP-1141]【Quản lý hệ thống】Phòng ban và vai trò của người dùng không hiển thị khi chỉnh sửa tại mục Phòng ban của tôi------------
      },
      {
        field: 'selectedroles',
        //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1141]【Quản lý hệ thống】Phòng ban và vai trò của người dùng không hiển thị khi chỉnh sửa tại mục Phòng ban của tôi------------
        show: !data.isRole,
        //Kiểm tra có phải chế độ đa đơn vị thuê hay không
        componentProps:{
          api: data.tenantSaas?getAllRolesList:getAllRolesListNoByTenant,
          disabled: !!data?.roleDisabled,
          immediate: !!data?.roleDisabled,
        //update-end---author:wangshuai ---date:20260811  for：[LHZP-1141]【Quản lý hệ thống】Phòng ban và vai trò của người dùng không hiển thị khi chỉnh sửa tại mục Phòng ban của tôi------------
        }
      },
      // Giải thích logic mã: 【issues/4935】Trong giao diện chỉnh sửa người dùng của đơn vị thuê, hộp thả xuống đơn vị thuê chưa được lọc, hiển thị tất cả đơn vị thuê hiện có trong hệ thống------------
      {
        field: 'relTenantIds',
        componentProps:{
          disabled: !!data.tenantSaas,
        },
      },
    ]);
    // Giải thích logic mã: 【issues/4935】Trong giao diện chỉnh sửa người dùng của đơn vị thuê, hộp thả xuống đơn vị thuê chưa được lọc, hiển thị tất cả đơn vị thuê hiện có trong hệ thống------------
    if(!unref(isUpdate) && data.tenantSaas){
      await setFieldsValue({ relTenantIds: getTenantId().toString() })
    }
    // Dù thêm mới hay chỉnh sửa đều có thể đặt giá trị cho biểu mẫu
    if (typeof data.record === 'object') {
      setFieldsValue({
        ...data.record,
      });
    }
    // Khi ẩn phần chân thì vô hiệu hóa toàn bộ biểu mẫu
    // Giải thích logic mã: VUEN-1117【issue】Các vấn đề mã nguồn mở tuần 0523
    setProps({ disabled: !showFooter.value });
    if(unref(isUpdate)){
      updateSchema([
        //Sửa tham số của vị trí chính và vị trí kiêm nhiệm
        {
          field: 'mainDepPostId',
          componentProps: { params: { departIds: data.record.selecteddeparts, parentId: data.record.selecteddeparts } },
        },
        {
          field: 'otherDepPostId',
          componentProps: { params: { departIds: data.record.selecteddeparts, parentId: data.record.selecteddeparts } },
        }
      ]);
    }
    //Quản lý phòng ban, thêm mới người dùng: khi thêm nhân viên dưới một vị trí thì mặc định vị trí hiện tại là vị trí chính
    updateSchema([
      {
        field: 'mainDepPostId',
        defaultValue: data?.mainDepPostId || '',
      }
    ])
  });
  //Lấy tiêu đề
  const getTitle = computed(() => {
    // Giải thích logic mã: 【QQYUN-8389】Thay đổi tiêu đề (title) của ngăn kéo chi tiết người dùng hệ thống
    if (!unref(isUpdate)) {
      return 'Thêm mới người dùng';
    } else {
      return unref(showFooter) ? 'Chỉnh sửa người dùng' : 'Chi tiết người dùng';
    }
  });
  const { adaptiveWidth } = useDrawerAdaptiveWidth();

  //Sự kiện gửi
  async function handleSubmit() {
    try {
      let values = await validate();
      if (disabledDepartValue) {
        values.selecteddeparts = disabledDepartValue;
      }
      if (!isFormDepartUser && (!values.selecteddeparts || values.selecteddeparts.length === 0)) {
        values.mainDepPostId = '';
        values.otherDepPostId = '';
      }
      setDrawerProps({ confirmLoading: true });
      values.userIdentity === 1 && (values.departIds = '');
      let isUpdateVal = unref(isUpdate);
      // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1737】API chỉnh sửa người dùng phòng ban, bổ sung tham sốupdateFromPage:"deptUsers"
      let params = values;
      if (isRoleDisabled) {
        params = { ...params, updateFromPage: 'deptUsers' };
      }
      // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1737】API chỉnh sửa người dùng phòng ban, bổ sung tham sốupdateFromPage:"deptUsers"
      //Gửi biểu mẫu
      await saveOrUpdateUser(params, isUpdateVal);
      //Đóng cửa sổ bật lên
      closeDrawer();
      //Làm mới danh sách
      emit('success',{isUpdateVal ,values});
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
<style scoped lang="less">
  :deep(.ant-input-number){
    width: 100%;
  }
</style>
