<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="Cấu hình quy tắc dữ liệu" width="450px" destroyOnClose>
    <a-tabs defaultActiveKey="1">
      <a-tab-pane tab="Quy tắc dữ liệu" key="1">
        <a-checkbox-group v-model:value="dataRuleChecked" v-if="dataRuleList.length > 0">
          <a-row>
            <a-col :span="24" v-for="(item, index) in dataRuleList" :key="'dr' + index">
              <a-checkbox :value="item.id">{{ item.ruleName }}</a-checkbox>
            </a-col>

            <a-col :span="24">
              <div style="width: 100%; margin-top: 15px">
                <a-button @click="saveDataRuleForRole" type="primary" size="small"> <Icon icon="ant-design:save-outlined"></Icon>Nhấn Lưu</a-button>
              </div>
            </a-col>
          </a-row>
        </a-checkbox-group>
        <div v-else><h3>Không có thông tin cấu hình!</h3></div>
      </a-tab-pane>
    </a-tabs>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { queryDataRule, saveDataRule } from '../role.api';
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  // Dữ liệu khai báo
  const functionId = ref('');
  const roleId = ref('');
  const dataRuleList = ref([]);
  const dataRuleChecked = ref([]);

  /**
   * Dữ liệu
   */
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false });
    //của quyền hạnid
    functionId.value = data.functionId;
    //của vai tròid
    roleId.value = data.roleId;
    //Truy vấn dữ liệu
    const res = await queryDataRule({ functionId: unref(functionId), roleId: unref(roleId) });
    if (res.success) {
      dataRuleList.value = res.result.datarule;
      if (res.result.drChecked) {
        dataRuleChecked.value = res.result.drChecked.split(',');
      }
    }
  });

  /**
   * Đặt lại
   */
  function reset() {
    functionId.value = '';
    roleId.value = '';
    dataRuleList.value = [];
    dataRuleChecked.value = [];
  }

  /**
   * Gửi
   */
  async function saveDataRuleForRole() {
    if (!unref(dataRuleChecked) || unref(dataRuleChecked).length == 0) {
      createMessage.warning('Lưu ý, hiện chưa chọn bất kỳ quyền dữ liệu nào!');
    }
    let params = {
      permissionId: unref(functionId),
      roleId: unref(roleId),
      dataRuleIds: unref(dataRuleChecked).join(','),
    };
    await saveDataRule(params);
    //Đóng cửa sổ bật lên
    closeDrawer();
    //Làm mới danh sách
    emit('success');
  }
</script>
