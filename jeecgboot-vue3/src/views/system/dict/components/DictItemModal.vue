<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="800px">
    <BasicForm @register="registerForm" >
      <template #itemColor="{ model, field }">
        <div class="item-tool">
          <div
              v-for="(item,index) in Colors"
              :style="{ color: item[0] }"
              :class="model.itemColor===item[0]?'item-active':''"
              class="item-color"
              @click="itemColorClick(item)">
            <div class="item-color-border"></div>
            <div class="item-back" :style="{ background: item[0] }"></div>
          </div>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { defineProps, ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { itemFormSchema } from '../dict.data';
  import { saveOrUpdateDictItem } from '../dict.api';
  import { Colors } from '/@/utils/dict/DictColors.js'
  
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const props = defineProps({ dictId: String });
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: itemFormSchema,
    showActionButtonGroup: false,
    mergeDynamicData: props,
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  //Đặt tiêu đề
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      values.dictId = props.dictId;
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateDictItem(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  
  /**
   * Sự kiện nhấp vào màu từ điển
   * 
   * @param index
   * @param item
   * @param model
   */
  function itemColorClick(item) {
    console.log(item)
    setFieldsValue({ itemColor: item[0] })
  }
  
</script>
<style lang="less" scoped>
   /*begin Kiểu cấu hình màu của từ điển*/
  .item-tool{
    display: flex;
    flex-wrap: wrap;
    .item-color{
      width: 18px;
      display: flex;
      justify-content: center;
      cursor: pointer;
      align-items: center;
      margin-right: 10px;
    }
    .item-back{
      width: 18px;
      height: 18px;
      border-radius: 50%;
    }
  }
  .item-color-border{
    visibility: hidden;
  }
  .item-active .item-color-border{
    visibility: visible;
    position: absolute;
    border: 1px solid;
    width: 24px;
    height: 24px;
    border-radius: 50%;
  }
   /*end Kiểu cấu hình màu của từ điển*/
</style>
