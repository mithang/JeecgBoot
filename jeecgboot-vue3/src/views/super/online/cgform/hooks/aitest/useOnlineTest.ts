import { FormActionType, JCodeEditor } from '/@/components/Form';
import { ref } from 'vue';

export function useOnlineTest(data, methods, form: Nullable<FormActionType>) {
  // OnlineCông tắc kiểm thử đơn vị (unit test)
  const aiTestMode = ref(false);
  const aiTestTable = ref([]);
  const aiTableList = ref([]);

  function initVirtualData() {
  }

  // Nút tùy chỉnh
  // Phiên bản mã nguồn mở không bao gồmAIDữ liệu kiểm thử, tại đây là triển khai rỗng; chữ ký tham số giống với CustomButtonList.vue giữ lời gọi của nhất quán (LHZP-195）
  async function genButtons(code, existingButtonCodes: string[] = []) {
  }

  // Tạo rajavaTăng cường
  function genEnhanceJavaData(code) {
  }

  // Tạo rajsTăng cường
  function genEnhanceJsData(tableName, type, codeEditor: InstanceType<typeof JCodeEditor>) {
  }

  // Tùy chỉnhsqlTăng cường
  function genEnhanceSqlData(code, tableName) {
  }

  /**
   * Tải thông tin cấu hình
   */
  function setTaleConfig() {
  }

  function tableJsonGetHelper(pickAfter) {
    console.log('Thông tin cấu hình của bảng', JSON.stringify(pickAfter));
    console.log('---------------------------------------');
  }

  /**
   * json Lấy trợ lý nhỏ
   * @param fields
   */
  function fieldsJsonGetHelper(fields) {
  }

  function refreshCacheTableName(oldValue, newValue) {
  }

  function getCacheTableName(name) {
  }

  // noinspection JSUnusedGlobalSymbols
  return {
    aiTestMode,
    aiTestTable,
    aiTableList,
    initVirtualData,
    genButtons,
    genEnhanceJavaData,
    genEnhanceJsData,
    genEnhanceSqlData,
    setTaleConfig,
    tableJsonGetHelper,
    fieldsJsonGetHelper,
    refreshCacheTableName,
    getCacheTableName,
  };
}
