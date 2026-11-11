<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
          <a-row>
            <a-col :span="24">
              <a-form-item label="Văn bản" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.name">
                <a-input v-model:value="formState.name" placeholder="Vui lòng nhập văn bản"></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Mật khẩu" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.miMa">
                <a-input-password v-model:value="formState.miMa" placeholder="Vui lòng nhập mật khẩu" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Danh sách thả xuống từ điển" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.xiala">
                <JDictSelectTag type="select" v-model:value="formState.xiala" dictCode="sex" placeholder="Vui lòng chọn dropdown từ điển" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Chọn một từ điển" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.danxuan">
                <JDictSelectTag type="radio" v-model:value="formState.danxuan" dictCode="sex" placeholder="Vui lòng chọn radio từ điển" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Chọn nhiều từ điển" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.duoxuan">
                <JCheckbox v-model:value="formState.duoxuan" dictCode="urgent_level" placeholder="Vui lòng chọn đa lựa chọn từ điển" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Công tắc" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.kaiguan">
                <JSwitch v-model:value="formState.kaiguan" :options="['1', '0']"></JSwitch>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Ngày" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.riqi">
                <a-date-picker placeholder="Vui lòng chọn ngày" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" v-model:value="formState.riqi" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Năm tháng ngày giờ phút giây" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.nyrsfm">
                <a-date-picker show-time v-model:value="formState.nyrsfm" style="width: 100%" valueFormat="YYYY-MM-DD HH:mm:ss" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Thời gian" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.shijian">
                <TimePicker placeholder="Vui lòng chọn thời gian" v-model:value="formState.shijian" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Tệp" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.wenjian">
                <JUpload v-model:value="formState.wenjian"></JUpload>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Hình ảnh" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.tupian">
                <JImageUpload :fileMax="2" v-model:value="formState.tupian"></JImageUpload>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Ô văn bản nhiều dòng" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.dhwb">
                <a-textarea v-model:value="formState.dhwb" rows="4" placeholder="Vui lòng nhập ô văn bản nhiều dòng" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Hộp tìm kiếm thả xuống bảng từ điển" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.xlss">
                <JSearchSelect v-model:value="formState.xlss" dict="sys_user,realname,username" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="popupPopup" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.popup">
                <JPopup
                  v-model:value="formState.popup"
                  :fieldConfig="[
                    { source: 'name', target: 'popup' },
                    { source: 'id', target: 'popback' },
                  ]"
                  code="demo"
                  :multi="true"
                  :setFieldsValue="setFieldsValue"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="popback" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.popback">
                <a-input v-model:value="formState.popback" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Cây từ điển phân loại" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.flzds">
                <JCategorySelect
                  @change="(value) => handleFormChange('flzds', value)"
                  v-model:value="formState.flzds"
                  pcode="B02"
                  placeholder="Vui lòng chọn cây danh mục phân loại"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Chọn phòng ban" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.bmxz">
                <JSelectDept v-model:value="formState.bmxz" :multi="true" type="array" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Chọn người dùng" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.yhxz">
                <JSelectUserByDept v-model:value="formState.yhxz" :multi="true" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Văn bản phong phú" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.fwb">
                <JEditor v-model:value="formState.fwb" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="markdown" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.markdownString">
                <JMarkdownEditor v-model:value="formState.markdownString"></JMarkdownEditor>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Tỉnh/Thành/Quận huyệnJAreaSelect" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.shq">
                <JAreaSelect v-model:value="formState.shq" placeholder="Vui lòng nhập tỉnh/thành/quận huyện" />
              </a-form-item>
            </a-col>
    
            <a-col :span="24">
              <a-form-item label="Tỉnh/Thành/Quận huyệnJAreaLinkage" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.jssq">
                <JAreaLinkage v-model:value="formState.jssq" placeholder="Vui lòng nhập tỉnh/thành/quận huyện" />
              </a-form-item>
            </a-col>
    
            <a-col :span="24">
              <a-form-item label="JInputPop" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.ldzje">
                <JInputPop
                  v-model:value="formState.ldzje"
                  placeholder="Vui lòng nhậpJInputPop"
                  @change="(value) => handleFormChange('ldzje', value)"
                ></JInputPop>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="JSelectInput" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.ldzjs">
                <JSelectInput
                  v-model:value="formState.ldzjs"
                  placeholder="Vui lòng chọnJSelectInput"
                  :options="ldzjsOptions"
                  @change="(value) => handleFormChange('ldzjs', value)"
                ></JSelectInput>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Danh sách thả xuống chọn nhiều" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zddtjxl">
                <JSelectMultiple v-model:value="formState.zddtjxl" placeholder="Vui lòng chọn dropdown nhiều lựa chọn" dictCode="sex"></JSelectMultiple>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Người dùng" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.yongHu">
                <JSelectUser v-model:value="formState.yongHu" placeholder="Vui lòng chọn người dùng"></JSelectUser>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Chức danh" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zhiWu">
                <JSelectPosition
                  v-model:value="formState.zhiWu"
                  placeholder="Vui lòng chọn chức danh"
                  @change="(value) => handleFormChange('zhiWu', value)"
                ></JSelectPosition>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Vai trò" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.jueSe">
                <JSelectRole v-model:value="formState.jueSe" placeholder="Vui lòng chọn vai trò" @change="(value) => handleFormChange('jueSe', value)"></JSelectRole>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Cây tùy chỉnh" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zdys">
                <JTreeSelect
                  ref="treeSelect"
                  placeholder="Vui lòng chọn cây tùy chỉnh"
                  v-model:value="formState.zdys"
                  dict="sys_category,name,id"
                  pidValue="0"
                  loadTriggleChange
                >
                </JTreeSelect>
              </a-form-item>
            </a-col>
    
            <a-col :span="24">
              <a-form-item label="Giá trị số" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.yuanjia">
                <a-input-number v-model:value="formState.yuanjia" placeholder="Vui lòng nhập kiểu double" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Nhập chữ cái từ 2 đến 10 ký tự" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.ywzz">
                <a-input v-model:value="formState.ywzz" placeholder="Vui lòng nhập chữ cái từ 2 đến 10 ký tự"></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="JTreeDict" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zdbxl">
                <JTreeDict
                  v-model:value="formState.zdbxl"
                  placeholder="Vui lòng chọnJTreeDict"
                  @change="(value) => handleFormChange('zdbxl', value)"
                ></JTreeDict>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="JCodeEditor" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.zdmrz">
                <JCodeEditor
                  v-model:value="formState.zdmrz"
                  placeholder="Vui lòng nhậpJCodeEditor"
                  @change="(value) => handleFormChange('zdmrz', value)"
                ></JCodeEditor>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="Tham số" :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.jsonParam">
                <JAddInput v-model:value="formState.jsonParam" placeholder="Tham số"></JAddInput>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, computed } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';
  import { TimePicker, Form } from 'ant-design-vue';
  import JCheckbox from '/@/components/Form/src/jeecg/components/JCheckbox.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JSwitch from '/@/components/Form/src/jeecg/components/JSwitch.vue';
  import JUpload from '/@/components/Form/src/jeecg/components/JUpload/JUpload.vue';
  import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUpload.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JPopup from '/@/components/Form/src/jeecg/components/JPopup.vue';
  import JCategorySelect from '/@/components/Form/src/jeecg/components/JCategorySelect.vue';
  import JSelectUserByDept from '/@/components/Form/src/jeecg/components/JSelectUserByDept.vue';
  import JEditor from '/@/components/Form/src/jeecg/components/JEditor.vue';
  import JMarkdownEditor from '/@/components/Form/src/jeecg/components/JMarkdownEditor.vue';
  import JTreeSelect from '/@/components/Form/src/jeecg/components/JTreeSelect.vue';
  import JInputPop from '/@/components/Form/src/jeecg/components/JInputPop.vue';
  import JSelectInput from '/@/components/Form/src/jeecg/components/JSelectInput.vue';
  import JSelectPosition from '/@/components/Form/src/jeecg/components/JSelectPosition.vue';
  import JSelectMultiple from '/@/components/Form/src/jeecg/components/JSelectMultiple.vue';
  import JInput from '/@/components/Form/src/jeecg/components/JInput.vue';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import JAreaSelect from '/@/components/Form/src/jeecg/components/JAreaSelect.vue';
  import JAreaLinkage from '/@/components/Form/src/jeecg/components/JAreaLinkage.vue';
  import JSelectRole from '/@/components/Form/src/jeecg/components/JSelectRole.vue';
  import JTreeDict from '/@/components/Form/src/jeecg/components/JTreeDict.vue';
  import JCodeEditor from '/@/components/Form/src/jeecg/components/JCodeEditor.vue';
  import JAddInput from '/@/components/Form/src/jeecg/components/JAddInput.vue';
  import { getValueType } from '/@/utils';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
  });
  // Vô hiệu hóa biểu mẫu
  const disabled = computed(()=>{
    return props.formDisabled;
  });
  const emit = defineEmits(['register', 'ok']);
  // Giải thích logic mã: Sửa kiểm tra ví dụ báo cáo--------------
  const formState = reactive<Record<string, any>>({
    name: '',
    miMa: '',
    ywzz: '',
    xiala: '',
    danxuan: '',
    duoxuan: '',
    riqi: '',
    shijian: '',
    wenjian: '',
    tupian: '',
    dhwb: '',
    xlss: '',
    popup: '',
    flzds: '',
    yhxz: '',
    fwb: '',
    shq: '',
    ldzje: '',
    ldzjs: '',
    zddtjxl: '',
    yongHu: '',
    zhiWu: '',
    jueSe: '',
    zdys: '',
    jssq: '',
    zdbxl: '',
    zdmrz: '',
    jsonParam: '',
    bmxz: '',
    yuanjia: '',
    nyrsfm: '',
  });
  const { createMessage } = useMessage();
  const formRef = ref();
  const useForm = Form.useForm;
  const url = reactive<any>({
    duplicateCheck: '/sys/duplicate/check',
    add: '/test/jeecgDemo/oneNative/add',
    edit: '/test/jeecgDemo/oneNative/edit',
  });
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //Kiểm tra biểu mẫu
  const validatorRules = {
    name: [{ required: false, message: 'Vui lòng nhập văn bản!' }],
    miMa: [{ required: false, message: 'Vui lòng nhập mật khẩu!' }],
    ywzz: [{ required: false }, { pattern: '^[a-z|A-Z]{2,10}$', message: 'Không phù hợp quy tắc kiểm chứng!' }],
    xiala: [{ required: false, message: 'Vui lòng chọn component dropdown!' }],
    danxuan: [{ required: false, message: 'Vui lòng chọn component chọn đơn!' }],
    duoxuan: [{ required: false, message: 'Vui lòng chọn component chọn nhiều!' }],
    riqi: [{ required: false, message: 'Vui lòng chọn ngày!' }],
    shijian: [{ required: false, message: 'Vui lòng chọn thời gian!' }],
    wenjian: [{ required: false, message: 'Vui lòng tải lên tệp!' }],
    tupian: [{ required: false, message: 'Vui lòng tải lên hình ảnh!' }],
    dhwb: [{ required: false, message: 'Vui lòng nhập văn bản nhiều dòng!' }],
    xlss: [{ required: false, message: 'Vui lòng chọn tìm kiếm thả xuống theo từ điển!' }],
    popup: [{ required: false, message: 'Vui lòng chọn popup!' }],
    flzds: [{ required: false, message: 'Vui lòng chọn cây danh mục phân loại!' }],
    yhxz: [{ required: false, message: 'Vui lòng chọn người dùng!' }],
    fwb: [{ required: false, message: 'Vui lòng nhập văn bản đa dạng thức!' }],
    shq: [{ required: false, message: 'Vui lòng chọn cấp tỉnh/thành!' }],
    ldzje: [{ required: false, message: 'Vui lòng nhậpJInputPop!' }],
    ldzjs: [{ required: false, message: 'Vui lòng chọn ô nhập thả xuống!' }],
    zddtjxl: [{ required: false, message: 'Vui lòng chọn ô nhập nhiều lựa chọn!' }],
    yongHu: [{ required: false, message: 'Vui lòng chọn người dùng!' }],
    zhiWu: [{ required: false, message: 'Vui lòng chọn chức danh!' }],
    jueSe: [{ required: false, message: 'Vui lòng chọn vai trò!' }],
    zdys: [{ required: false, message: 'Vui lòng chọn cây tùy chỉnh!' }],
    jssq: [{ required: false, message: 'Vui lòng chọn liên động ba cấp!' }],
    zdbxl: [{ required: false, message: 'Vui lòng chọnJTreeDict!' }],
    zdmrz: [{ required: false, message: 'Vui lòng nhậpJCodeEditor!' }],
    jsonParam: [{ required: false, message: 'Vui lòng nhập tham số!' }],
    bmxz: [{ required: false, message: 'Vui lòng chọn phòng ban!' }],
    yuanjia: [{ required: false, message: 'Vui lòng nhập số!' }],
    nyrsfm: [{ required: false, message: 'Vui lòng chọn năm tháng ngày giờ phút giây!' }],
  };
  // Giải thích logic mã: Sửa kiểm tra ví dụ báo cáo------------
  const { resetFields, validate, validateInfos } = useForm(formState, validatorRules, { immediate: false });
  const ldzjsOptions = ref([
    { label: 'Nam', value: '1' },
    { label: 'Nữ', value: '2' },
  ]);

  /**
   * Thêm mới
   */
  function add() {
    edit({});
  }

  /**
   * Chỉnh sửa
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      //Gán giá trị
      Object.assign(formState, record);
    });
  }

  /**
   * Dữ liệu gửi
   */
  async function submitForm() {
    // Kích hoạt kiểm tra biểu mẫu
    // Giải thích logic mã: Sửa kiểm tra ví dụ báo cáo------------
    await validate();
    confirmLoading.value = true;
    let httpurl = '';
    let method = '';
    //Định dạng thời gian
    let model = formState;
    if (!model.id) {
      httpurl += url.add;
      method = 'post';
    } else {
      httpurl += url.edit;
      method = 'put';
    }
    //Dữ liệu lặp lại nếu là mảng
    for (let data in formState) {
      //Nếu dữ liệu này là mảng và là kiểu chuỗi
      if (formState[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //Nếu là kiểu chuỗi thì cần đổi thành chuỗi tách bằng dấu phẩy
        if (valueType === 'string') {
          formState[data] = formState[data].join(',');
        }
      }
    }
    defHttp
      .request(
        {
          url: httpurl,
          params: model,
          method: method,
        },
        { isTransformResponse: false }
      )
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }

  /**
   * popupSự kiện callback thành công
   */
  function popupHandleSuccess(values) {
    Object.assign(formState, values);
  }

  /**
   *  popupSự kiện thay đổi giá trị component
   */
  function setFieldsValue(map) {
    Object.keys(map).map((key) => {
      formState[key] = map[key];
    });
  }

  /**
   * Kích hoạt sự kiện thay đổi giá trị
   * @param key
   * @param value
   */
  function handleFormChange(key, value) {
    formState[key] = value;
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 24px 24px 24px 24px;
  }
</style>
