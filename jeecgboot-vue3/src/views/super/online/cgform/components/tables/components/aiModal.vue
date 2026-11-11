<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle()"
    :width="600"
    :max-height="500"
    wrapClassName="ai-suggestion-modal"
    @register="registerModal"
    @open-change="handleOpen"
    :canFullscreen="false"
  >
    <div class="ai-modal-content">
      <template v-if="active == 'one'">
        <div class="ai-header">
          <SvgIcon name="robot" size="40" />
          <p class="title">Thêm trường cần gợi ý chuyên nghiệp? Hãy thửAIĐề xuất trường thông minh</p>
          <p class="tip">Có thể nhập từ bổ nghĩa tương ứng, như: Bảng đăng ký thông tin nhân viên</p>
        </div>
        <div class="ai-body">
          <a-input v-model:value.trim="one.qualifier" placeholder="Vui lòng nhập từ trang trí" @pressEnter="handleConfirm"></a-input>
        </div>
      </template>
      <template v-else>
        <div class="two">
          <div class="field-list field-header">
            <div class="field-row">
              <a-checkbox v-model:checked="two.checked" :indeterminate="two.indeterminate" @change="handleAllChange"></a-checkbox>
              <div class="field-list field-content">
                <span class="dbFieldName">Tên trường</span>
                <span class="dbFieldTxt">Ghi chú trường</span>
              </div>
            </div>
          </div>
          <div class="field-list">
            <div v-for="(item, index) in two.data" :key="index" class="field-row">
              <a-checkbox v-model:checked="item.checked" @change="handleItemChange"></a-checkbox>
              <div class="field-content">
                <a-input class="dbFieldName" v-model:value="item.dbFieldName" placeholder="Tên trường"></a-input>
                <a-input class="dbFieldTxt" v-model:value="item.dbFieldTxt" placeholder="Ghi chú trường"></a-input>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
    <template #footer>
      <template v-if="active == 'one'">
        <a-button @click="handleCancel">Hủy</a-button>
        <a-button type="primary" @click="handleConfirm" :loading="one.loading">{{ one.loading ? 'Đang tạo...' : 'Đề xuất tạo' }}</a-button>
      </template>
      <template v-else>
        <a-button type="primary" @click="handleAdd" :disabled="!two.checked && !two.indeterminate">Thêm{{ count() }}trường vàoonline</a-button>
      </template>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { defineEmits } from 'vue';
  import { SvgIcon } from '/@/components/Icon';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { defHttp } from '/@/utils/http/axios';
  import { cloneDeep } from 'lodash-es';
  const configUrl = {
    fields: '/online/cgform/api/aigc/fields',
  };
  const props = defineProps(['dataSource', 'DBtableRef', 'tableName', 'isUpdate']);
  const emit = defineEmits(['register', 'close', 'generate']);
  const { createMessage } = useMessage();
  const [registerModal, { closeModal }] = useModalInner();
  const active = ref('one');
  const one = reactive({
    title: 'AIĐề xuất trường',
    qualifier: '',
    loading: false,
  });
  const two = reactive({
    title: 'Trường đề xuất',
    checked: false,
    indeterminate: false,
    data: [],
  });
  const getTitle = () => {
    return active.value == 'one' ? one.title : two.title;
  };
  const handleOpen = (visible) => {
    if (visible === false) {
      setTimeout(() => {
        emit('close');
      }, 400);
    }
  };
  const filter = (data: any = []) => {
    // Lọc bỏ các trường đã tồn tại
    const filteredData = data.filter((item) => {
      return !props.DBtableRef.getTableData().some((existingItem) => existingItem.dbFieldName === item.dbFieldName);
    });
    if (data.length == 0 || filteredData.length == 0) {
      createMessage.warn('Vui lòng đổi một từ trang trí khác rồi thử lại~');
    } else {
      // Thiết lập các trường còn lại ở trạng thái đã chọn
      filteredData.forEach((item) => {
        item.checked = true;
        // Thiết lập quy tắc khác
        if (['Date', 'Datetime'].includes(item.dbType)) {
          item.fieldShowType = item.dbType == 'Datetime' ? 'datetime' : 'date';
        }
      });

      two.data = filteredData;
      two.checked = true;
      active.value = 'two';
    }
  };
  const handleAllChange = ({ target }) => {
    two.data.forEach((item: any) => (item.checked = target.checked));
  };
  const handleItemChange = () => {
    const value = two.data.filter((item: any) => item.checked === true);
    two.checked = value.length === two.data.length ? true : false;
    two.indeterminate = value.length && two.checked == false ? true : false;
  };
  const handleConfirm = () => {
    if (one.qualifier.length == 0) {
      createMessage.warn('Vui lòng nhập từ trang trí~');
      return;
    }
    one.loading = true;
    defHttp
      .post(
        { url: `${configUrl.fields}?prompt=${one.qualifier}&code=${props.isUpdate && props.tableName ? props.tableName : ''}`, timeout: 1e3 * 60 * 5 },
        { isTransformResponse: false }
      )
      .then((res) => {
        if (res.code == 200) {
          filter(res.result);
        } else {
          createMessage.warn(res.message);
        }
        one.loading = false;
      })
      .catch((err) => {
        one.loading = false;
        console.log(err);
      });
  };
  const handleAdd = () => {
    const data = cloneDeep(two.data);
    const _data = data.filter((item: any) => item.checked);
    _data.forEach((item: any) => delete item.checked);
    emit('generate', _data);
    closeModal();
  };
  const count = () => {
    return two.data.filter((item: any) => item.checked).length;
  };
  const handleCancel = () => {
    closeModal();
  };
</script>

<style lang="less" scoped>
  .ai-modal-content {
    padding-top: 16px;
    p {
      margin-bottom: 0;
    }
    .ai-header {
      text-align: center;
      margin-bottom: 16px;
      .title {
        font-size: 16px;
        font-weight: bold;
      }
      .tip {
        font-size: 12px;
        line-height: 18px;
        color: #a0a0a0;
        margin-top: 6px;
      }
    }
    .ai-body {
      padding: 16px;
      padding-top: 0;
    }
  }
  .two {
    padding: 16px;
    padding-top: 0;
  }
  .field-header {
    font-weight: bold;
  }
  .field-list {
    padding: 0 16px;
    .field-row {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      .field-content {
        flex: 1;
        margin-left: 12px;
        display: flex;
        .dbFieldTxt,
        .dbFieldName {
          width: 50%;
        }
        .dbFieldName {
          margin-right: 12px;
        }
      }
    }
  }
</style>
