import { computed, h, Ref } from 'vue';
import { FormSchema, RenderCallbackParams } from '/@/components/Form';
import { Input, Button } from 'ant-design-vue';
import { FolderOpenOutlined } from '@ant-design/icons-vue';
import { bindMapFormSchema } from '/@/utils/common/compUtils';
import { usePermission } from '/@/hooks/web/usePermission';
import { rules } from '/@/utils/helper/validator';

const { isDisabledAuth } = usePermission();

export function useFormSchemas(_props, expandingConfig, handlers) {
  type SpanType = 'one' | 'tow' | 'three';
  // Bố cục động
  const mapFormSchema = bindMapFormSchema<SpanType>(
    {
      // một cột
      one: {
        colProps: { xs: 24, sm: 24 },
        itemProps: {
          labelCol: { xs: 24, sm: 2 },
          wrapperCol: { xs: 24, sm: 22 },
        },
      },
      // hai cột
      tow: {
        colProps: { xs: 24, sm: 12 },
        itemProps: {
          labelCol: { xs: 24, sm: 4 },
          wrapperCol: { xs: 24, sm: 20 },
        },
      },
      // ba cột
      three: {
        colProps: { xs: 24, sm: 8 },
        itemProps: {
          labelCol: { xs: 24, sm: 6 },
          wrapperCol: { xs: 24, sm: 18 },
        },
      },
    },
    'three'
  );

  // Biểu mẫu FormSchemas
  const formSchemas: FormSchema[] = [
    { label: '', field: 'id', component: 'Input', show: false },
    { label: '', field: 'tableVersion', component: 'Input', show: false },
    mapFormSchema({
      label: 'Tên bảng',
      field: 'tableName',
      component: 'Input',
      required: true,
      // Nếu số phiên bản là1 Biểu thị chưa từng sửa đổi Chưa từng đồng bộ Có thể sửa tên bảng
      dynamicDisabled: ({ model }) => model.tableVersion && model.tableVersion != 1,
      dynamicRules: ({ model, schema }) => {
        // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8492】onlineKiểm chứng tên bảng không cho phép nhập tiếng Trung
        return [
          {
            validator: (_, value) => {
              return new Promise((resolve, reject) => {
                let reg = /[\u4E00-\u9FA5]/g;
                if (reg.test(value)) {
                  reject('không cho phép nhập tiếng Trung');
                }
                resolve();
              });
            },
          },
          // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
          {
            validator: (_, value) => {
              return new Promise((resolve, reject) => {
                if (value.length > 50) {
                  reject('Tên bảng dài nhất50ký tự');
                }
                resolve();
              });
            },
          },
          // update-end--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
          ...rules.duplicateCheckRule('onl_cgform_head', 'table_name', model, schema, true),
        ];
        // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8492】onlineKiểm chứng tên bảng không cho phép nhập tiếng Trung
      },
    }),
    mapFormSchema({
      label: 'Mô tả bảng',
      field: 'tableTxt',
      component: 'Input',
      required: true,
      // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
      dynamicRules: ({ model, schema }) => {
        return [
          {
            validator: (_, value) => {
              return new Promise((resolve, reject) => {
                if (value.length > 200) {
                  reject('Mô tả bảng dài nhất200ký tự');
                }
                resolve();
              });
            },
          },
        ];
      },
      // update-end--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
    }),
    mapFormSchema({
      label: 'Loại bảng',
      field: 'tableType',
      component: 'Select',
      defaultValue: 1,
      componentProps: {
        options: [
          { label: 'bảng đơn', value: 1 },
          { label: 'bảng chính', value: 2 },
          { label: 'Bảng phụ', value: 3 },
        ],
        onChange: handlers.onTableTypeChange,
        allowClear: false,
      },
    }),
    // Tại đây là phần giữ chỗ, dùng để đưa relationType Đẩy tới phía bên phải nhất
    {
      label: '',
      field: 'relationType',
      component: 'InputNumber',
      render: () => '',
      colProps: { xs: 0, sm: 17 },
      ifShow: fieldIfShow,
    },
    mapFormSchema({
      label: '',
      field: 'relationType',
      component: 'RadioGroup',
      defaultValue: 0,
      componentProps: {
        options: [
          { label: 'một-nhiều', value: 0 },
          { label: 'một-một', value: 1 },
        ],
        allowClear: false,
        onChange: handlers.onRelationTypeChange,
      },
      colProps: { xs: 24, sm: 4 },
      itemProps: {
        colon: false,
        labelCol: { xs: 0, sm: 0 },
        wrapperCol: { xs: 24, sm: 24 },
      },
      ifShow: fieldIfShow,
    }),
    mapFormSchema({
      label: 'Số thứ tự',
      field: 'tabOrderNum',
      component: 'InputNumber',
      componentProps: {
        style: {
          width: '100%',
        },
      },
      colProps: { xs: 24, sm: 3 },
      itemProps: {
        labelCol: { xs: 24, sm: 7 },
        wrapperCol: { xs: 24, sm: 24 - 7 },
      },
      ifShow: fieldIfShow,
    }),
    mapFormSchema({
      label: 'Phân loại biểu mẫu',
      field: 'formCategory',
      component: 'JDictSelectTag',
      defaultValue: 'temp',
      componentProps: {
        dictCode: 'ol_form_biz_type',
        allowClear: false,
      },
    }),
    mapFormSchema({
      label: 'Chiến lược khóa chính',
      field: 'idType',
      component: 'Select',
      defaultValue: 'UUID',
      componentProps: {
        options: [
          { label: 'ID_WORKER(Tự tăng phân tán)', value: 'UUID' },
          // { label: 'NATIVE(Cách tăng tự động)', value: 'NATIVE' },
          // { label: 'SEQUENCE(Cách thức dãy (sequence))', value: 'SEQUENCE' },
        ],
        allowClear: false,
      },
    }),
    mapFormSchema({
      label: 'Tên số thứ tự',
      field: 'idSequence',
      component: 'Input',
      componentProps: {},
      ifShow: fieldIfShow,
    }),
    mapFormSchema({
      label: 'Hiển thị ô kiểm',
      field: 'isCheckbox',
      component: 'Select',
      defaultValue: 'Y',
      componentProps: {
        options: [
          { label: 'là', value: 'Y' },
          { label: 'Không', value: 'N' },
        ],
        allowClear: false,
      },
    }),
    mapFormSchema({
      label: 'Mẫu giao diện',
      field: 'themeTemplate',
      component: 'Select',
      defaultValue: 'normal',
      componentProps: {
        options: [
          { label: 'Chủ đề mặc định', value: 'normal' },
          { label: 'ERPChủ đề (theme)(một-nhiều)', value: 'erp' },
          { label: 'Chủ đề bảng con nhúng(một-nhiều)', value: 'innerTable' },
          { label: 'TABChủ đề (theme)(một-nhiều)', value: 'tab' },
        ],
        allowClear: false,
      },
      // Khi là bảng đơn thì vô hiệu hóa trường này
      dynamicDisabled: ({ model }) => model.tableType === 1,
      // update-begin--author:liaozhiyang---date:20231123---for：【QQYUN-7073】Nhắc nhởERP、bảng con nhúng không hỗ trợ truy vấn liên hợp
      dynamicRules() {
        return [
          {
            validator({}, value) {
              const data = expandingConfig.value;
              if (value === 'erp') {
                if (data.joinQuery) {
                  return Promise.reject('ERPkhông hỗ trợ chức năng truy vấn liên hợp');
                }
              } else if (value === 'innerTable') {
                if (data.joinQuery) {
                  return Promise.reject('Bảng con nhúng không hỗ trợ chức năng truy vấn liên hợp');
                }
              }
              return Promise.resolve();
            },
          },
        ];
      },
      // update-end--author:liaozhiyang---date:20231123---for：【QQYUN-7073】Nhắc nhởERP、bảng con nhúng không hỗ trợ truy vấn liên hợp
    }),
    mapFormSchema({
      label: 'Phong cách biểu mẫu',
      field: 'formTemplate',
      component: 'Select',
      defaultValue: '1',
      componentProps: {
        options: [
          { label: 'một cột', value: '1' },
          { label: 'hai cột', value: '2' },
          { label: 'ba cột', value: '3' },
          { label: 'bốn cột', value: '4' },
        ],
        placeholder: 'Vui lòng chọnPCPhong cách biểu mẫu',
        allowClear: false,
      },
    }),
    mapFormSchema({
      label: 'Phong cách biểu mẫu di động',
      field: 'formTemplateMobile',
      component: 'Select',
      defaultValue: '1',
      componentProps: {
        options: [
          { label: 'AntDesignMẫu (template)', value: '1' },
          { label: 'BootstrapMẫu (template)', value: '2' },
        ],
        placeholder: 'Vui lòng chọn phong cách biểu mẫu di động',
      },
      // Tạm thời ẩn trước
      ifShow: false,
    }),
    mapFormSchema({
      label: 'Thanh cuộn',
      field: 'scroll',
      component: 'Select',
      defaultValue: 1,
      componentProps: {
        options: [
          { label: 'Có', value: 1 },
          { label: 'Không có', value: 0 },
        ],
        allowClear: false,
      },
    }),
    mapFormSchema({
      label: 'Có phân trang hay không',
      field: 'isPage',
      component: 'Select',
      defaultValue: 'Y',
      componentProps: {
        options: [
          { label: 'là', value: 'Y' },
          { label: 'Không', value: 'N' },
        ],
        allowClear: false,
      },
    }),
    mapFormSchema({
      label: 'Có phải cây hay không',
      field: 'isTree',
      component: 'Select',
      defaultValue: 'N',
      componentProps: {
        options: [
          { label: 'là', value: 'Y' },
          { label: 'Không', value: 'N' },
        ],
        onChange: handlers.onIsTreeChange,
        allowClear: false,
      },
      dynamicRules({ model }) {
        return [
          {
            validator({}, value) {
              if (value === 'Y' && (model.tableType == 2 || model.tableType == 3)) {
                return Promise.reject('Bảng chính và bảng phụ không hỗ trợ loại cây!');
              } else {
              }
              return Promise.resolve();
            },
          },
        ];
      },
      // update-begin--author:liaozhiyang---date:20240604---for：【TV360X-125】Khi chọn bảng chính và bảng phụ thì ẩn cấu hình bảng cây
      show({ model, values }) {
        if (model.tableType == 2 || model.tableType == 3) {
          model.isTree = 'N';
          handlers.onIsTreeChange('N');
          return false;
        }
        return true;
      },
      // update-end--author:liaozhiyang---date:20240604---for：【TV360X-125】Khi chọn bảng chính và bảng phụ thì ẩn cấu hình bảng cây
    }),
    mapFormSchema({
      // Đừng xóa khoảng trắng, nếu không bố cục sẽ bị loạn
      label: ' ', // Cấu hình mở rộng
      field: 'extConfigJson',
      component: 'Input',
      slot: 'extConfigButton',
      itemProps: { colon: false },
      // khi là bảng con một-nhiều thì ẩn nút cấu hình mở rộng
      ifShow: ({ model }) => !(model.tableType === 3 && model.relationType === 0),
    }),
    mapFormSchema({
      label: 'Cha của biểu mẫu câyID',
      field: 'treeParentIdField',
      component: 'Input',
      ifShow: fieldIfShow,
    }),
    mapFormSchema({
      label: 'Có trường nút con hay không',
      field: 'treeIdField',
      component: 'Input',
      show: false,
    }),
    mapFormSchema({
      label: 'Danh sách mở biểu mẫu của cây',
      field: 'treeFieldname',
      required: true,
      component: 'Input',
      ifShow: fieldIfShow,
    }),
    mapFormSchema(
      {
        label: 'Bảng phụ',
        field: 'subTableStr',
        component: 'Input',
        componentProps: {
          disabled: true,
          placeholder: ' ',
          allowClear: false,
        },
        ifShow: handlers.ifShowOfSubTableStr,
      },
      'one'
    ),
  ];

  // Kiểm soát trường có hiển thị hay không
  function fieldIfShow({ field, model }: RenderCallbackParams) {
    switch (field) {
      case 'relationType':
      case 'tabOrderNum':
        return model.tableType === 3;
      case 'treeParentIdField':
      case 'treeIdField':
      case 'treeFieldname':
        return model.isTree === 'Y';
      case 'idSequence':
        return model.idType === 'SEQUENCE';
    }
    return true;
  }

  return { formSchemas };
}

/** Lấy Tham số mở rộng FormSchemas */
export function useExtendConfigFormSchemas(_props, handlers) {
  type SpanType = 'left' | 'right';
  // formItem giá trị liên kết của, thống nhất bố cục
  const mapFormSchema = bindMapFormSchema<SpanType>(
    {
      left: {
        colProps: { xs: 24, sm: 9 },
        itemProps: {
          labelCol: { xs: 24, sm: 11 },
          wrapperCol: { xs: 24, sm: 13 },
        },
        style: { width: '100%' },
      },
      right: {
        colProps: { xs: 24, sm: 15 },
        itemProps: {
          labelCol: { xs: 24, sm: 4 },
          wrapperCol: { xs: 24, sm: 19 },
        },
        style: { width: '100%' },
      },
    },
    'left'
  );

  // khi là bảng con một-một chỉ hiển thị cố định cột thao tác, kéo độ rộng cột, biểu mẫuLabelĐộ dài
  function isNotOneToOneSub() {
    const { tableType, relationType } = _props.parentForm.getFieldsValue(['tableType', 'relationType']);
    return !(tableType === 3 && relationType === 1);
  }

  // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu (bảng phụ đi theo bảng chính, không cấu hình riêng)
  function isNotSubTable() {
    const { tableType } = _props.parentForm.getFieldsValue(['tableType']);
    return tableType !== 3;
  }
  // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu

  const baseFormSchemas: FormSchema[] = [
    // Popup
    mapFormSchema(
      {
        label: 'Popup toàn màn hình mặc định',
        field: 'modelFullscreen',
        ifShow: isNotOneToOneSub,
        component: 'RadioButtonGroup',
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
        },
      },
      'left'
    ),
    mapFormSchema(
      {
        label: 'Độ rộng popup',
        field: 'modalMinWidth',
        component: 'InputNumber',
        ifShow: isNotOneToOneSub,
        componentProps: {
          style: 'width: 70%',
          placeholder: 'Độ rộng tối thiểu của popup (đơn vị:px）',
        },
        // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-77】Sau khi popup toàn màn hình, ô nhập độ rộng bị vô hiệu hóa
        dynamicDisabled: ({ model }) => !!model.modelFullscreen,
        // update-end--author:liaozhiyang---date:20240520---for：【TV360X-77】Sau khi popup toàn màn hình, ô nhập độ rộng bị vô hiệu hóa
      },
      'right'
    ),
    //----------------------------Bình luận biểu mẫu begin-----------------------------------------
    mapFormSchema(
      {
        label: 'Bật bình luận biểu mẫu',
        field: 'commentStatus',
        component: 'RadioButtonGroup',
        ifShow: isNotOneToOneSub,
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
        },
      },
      'left'
    ),
    // Tại đây là phần giữ chỗ (placeholder)
    mapFormSchema(
      {
        label: '',
        field: 'commentStatus',
        component: 'InputNumber',
        ifShow: isNotOneToOneSub,
        render: () => '',
      },
      'right'
    ),
    // Kích hoạt truy vấn liên hợp
    mapFormSchema(
      {
        label: 'Kích hoạt truy vấn liên hợp',
        field: 'joinQuery',
        component: 'RadioButtonGroup',
        ifShow: isNotOneToOneSub,
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
          onChange: handlers.onJoinQueryChange,
        },
      },
      'left'
    ),
    // Tại đây là phần giữ chỗ (placeholder)
    mapFormSchema(
      {
        label: '',
        field: 'joinQuery',
        component: 'InputNumber',
        ifShow: isNotOneToOneSub,
        render: () => '',
      },
      'right'
    ),
    // In báo cáo Jimu
    mapFormSchema(
      {
        label: 'Tích hợp báo cáo Jimu',
        field: 'reportPrintShow',
        component: 'RadioButtonGroup',
        ifShow: isNotOneToOneSub,
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
          onChange: handlers.onReportPrintShowChange,
        },
      },
      'left'
    ),
    mapFormSchema(
      {
        label: 'Địa chỉ báo cáo',
        field: 'reportPrintUrl',
        component: 'Input',
        ifShow: isNotOneToOneSub,
        componentProps: {
          style: 'width: 70%',
        },
        dynamicDisabled: ({ model }) => !model.reportPrintShow,
        dynamicRules: ({ model }) => {
          return [
            { required: !!model.reportPrintShow, message: 'Vui lòng nhập địa chỉ báo cáo!' },
            {
              validator({}, value) {
                if (/\/jmreport\/view\/{Báo cáo JimuID}/.test(value)) {
                  return Promise.reject('Vui lòng đưa{Báo cáo JimuID}Thay thế bằng báo cáo Jimu thậtID！');
                }
                return Promise.resolve();
              },
            },
          ];
        },
      },
      'right'
    ),
    // update-begin--author:liaozhiyang---date:20231213---for：【QQYUN-7421】vue3Trước tiên ghi chú (comment) chức năng biểu mẫu thiết kế tích hợp
    // mapFormSchema(
    //   {
    //     label: 'Tích hợp thiết kế biểu mẫu',
    //     field: 'isDesForm',
    //     component: 'RadioButtonGroup',
    //     componentProps: {
    //       options: [
    //         { label: 'Bật', value: 'Y' },
    //         { label: 'Đóng', value: 'N' },
    //       ],
    //       buttonStyle: 'solid',
    //       onChange: handlers.onIsDesformChange,
    //     },
    //   },
    //   'left'
    // ),
    // mapFormSchema(
    //   {
    //     label: 'Mã biểu mẫu',
    //     field: 'desFormCode',
    //     component: 'Input',
    //     componentProps: {
    //       style: 'width: 80%',
    //     },
    //     dynamicDisabled: ({ model }) => model.isDesForm !== 'Y',
    //     dynamicRules: ({ model }) => {
    //       return [{ required: model.isDesForm === 'Y', message: 'Vui lòng nhập mã biểu mẫu!' }];
    //     },
    //   },
    //   'right'
    // ),
    // update-end--author:liaozhiyang---date:20231213---for：【QQYUN-7421】vue3Trước tiên ghi chú (comment) chức năng biểu mẫu thiết kế tích hợp
    // Cột thao tác của danh sách
    mapFormSchema(
      {
        label: 'Cố định cột thao tác',
        field: 'tableFixedAction',
        component: 'RadioButtonGroup',
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
        },
        defaultValue: 1,
        // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-69】Khi là bảng cây, vô hiệu hóa cố định cột thao tác
        dynamicDisabled: () => _props.parentForm.getFieldsValue(['isTree']).isTree === 'Y',
        // update-end--author:liaozhiyang---date:20260713---for：【LHZP-69】Khi là bảng cây, vô hiệu hóa cố định cột thao tác
      },
      'left'
    ),
    mapFormSchema(
      {
        label: 'Cách cố định',
        field: 'tableFixedActionType',
        component: 'Select',
        componentProps: {
          options: [
            { label: 'Cố định sang phía bên phải', value: 'right' },
            { label: 'Cố định sang phía bên trái', value: 'left' },
          ],
          style: 'width: 70%',
        },
        defaultValue: 'right',
        // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-69】Khi là bảng cây, vô hiệu hóa cách cố định
        dynamicDisabled: ({ model }) => !model.tableFixedAction || _props.parentForm.getFieldsValue(['isTree']).isTree === 'Y',
        // update-end--author:liaozhiyang---date:20260713---for：【LHZP-69】Khi là bảng cây, vô hiệu hóa cách cố định
        dynamicRules: ({ model }) => {
          return [{ required: !!model.tableFixedAction, message: 'Vui lòng chọn cách cố định!' }];
        },
      },
      'right'
    ),
    // Kéo để điều chỉnh độ rộng cột
    mapFormSchema(
      {
        label: 'Kéo độ rộng cột',
        field: 'canResizeColumn',
        component: 'RadioButtonGroup',
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
        },
        defaultValue: 0,
      },
      'left'
    ),
    // Tại đây là phần giữ chỗ (placeholder)
    mapFormSchema(
      {
        label: '',
        field: 'canResizeColumn',
        component: 'InputNumber',
        render: () => '',
      },
      'right'
    ),
    //--------------------------Bình luận biểu mẫu end-----------------------------------------
    // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
    mapFormSchema(
      {
        label: 'Biểu mẫuLabelĐộ dài',
        field: 'formLabelLengthShow',
        component: 'RadioButtonGroup',
        helpMessage: 'Số lượng ký tự văn bản của ghi chú trường',
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
          onChange: handlers.onFormLabelLengthShow,
        },
      },
      'left'
    ),
    mapFormSchema(
      {
        label: 'LabelĐộ dài',
        field: 'formLabelLength',
        component: 'InputNumber',
        componentProps: {
          style: 'width: 70%',
          placeholder: 'Biểu mẫu tùy chỉnhLabelĐộ dài',
        },
        dynamicDisabled: ({ model }) => !model.formLabelLengthShow,
        dynamicRules: ({ model }) => {
          return [{ required: !!model.formLabelLengthShow, message: 'Vui lòng điền biểu mẫulabelĐộ dài' }];
        },
      },
      'right'
    ),
    // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
    mapFormSchema(
      {
        label: 'Kích hoạt liên kết ngoài',
        field: 'enableExternalLink',
        component: 'RadioButtonGroup',
        ifShow: isNotOneToOneSub,
        componentProps: {
          options: [
            {label: 'Bật', value: 1},
            {label: 'Đóng', value: 0},
          ],
          buttonStyle: 'solid',
          defaultValue: 0,
          // onChange: handlers.onFormLabelLengthShow,
        },
      },
      'left'
    ),
    mapFormSchema(
      {
        label: 'Thao tác được cho phép',
        field: 'externalLinkActions',
        component: 'JCheckbox',
        ifShow: isNotOneToOneSub,
        itemProps: { colon: true },
        componentProps: {
          options: [
            {label: 'Thêm mới', value: 'add'},
            {label: 'Chỉnh sửa', value: 'edit'},
            {label: 'Chi tiết', value: 'detail'},
          ],
        },
        dynamicDisabled: ({model}) => !model.enableExternalLink,
      },
      'right'
    ),
    // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
    mapFormSchema(
      {
        label: 'Bật đa nguồn dữ liệu',
        field: 'enableMultiDataSource',
        component: 'RadioButtonGroup',
        ifShow: isNotSubTable,
        // update-begin--author:jeecg---date:20260513---for：【QQYUN-15337】Bổ sung phần thuyết minh sử dụng tại vị trí bật đa nguồn dữ liệu/Những điểm cần lưu ý
        helpMessage: [
          'Sau khi bật: việc tạo bảng đồng bộ, thêm/xóa/sửa/tra dữ liệu, truy vấn danh sách, nhập/xuất của biểu mẫu này đều đi qua nguồn dữ liệu đã chọn; bảng phụ tự động theo bảng chính.',
          '✓ bản ghi liên kết/Bản ghi liên kết dropdown đã hỗ trợ đa nguồn dữ liệu, sẽ tự động định tuyến theo nguồn dữ liệu của biểu mẫu chứa bảng được liên kết, không cần cấu hình thêm.',
          '⚠ Đã kết nối với phê duyệt quy trình(BPM)Biểu mẫu của không nên cấu hình nguồn dữ liệu — engine quy trình đi qua cơ sở dữ liệu chính, vui lòng đặt biểu mẫu kết nối quy trình vào cơ sở dữ liệu chính (không bật đa nguồn dữ liệu).',
          '⚠ Sau khi nguồn dữ liệu thay đổi, biểu mẫu này và bảng phụ của nó sẽ bị đặt thành"Chưa đồng bộ", cần đồng bộ tạo bảng lại; bảng vật lý sẽ không tự động chuyển kho, bảng của kho cũ/Dữ liệu cần tự dọn dẹp.',
        ],
        helpComponentProps: {
          maxWidth: '550px',
          showIndex: true,
          fontSize: '13px',
          color: '#4e5969',
          backgroundColor: '#ffffff',
        },
        // update-end--author:jeecg---date:20260513---for：【QQYUN-15337】Bổ sung phần thuyết minh sử dụng tại vị trí bật đa nguồn dữ liệu/Những điểm cần lưu ý
        componentProps: {
          options: [
            { label: 'Bật', value: 1 },
            { label: 'Đóng', value: 0 },
          ],
          buttonStyle: 'solid',
          defaultValue: 0,
          onChange: handlers.onEnableMultiDataSourceChange,
        },
      },
      'left'
    ),
    mapFormSchema(
      {
        label: 'Chọn nguồn dữ liệu',
        field: 'dbSource',
        component: 'JDictSelectTag',
        ifShow: isNotSubTable,
        itemProps: { colon: true },
        componentProps: {
          dictCode: 'sys_data_source,name,code',
          style: 'width: 70%',
          // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】Cho phép không chọn nguồn dữ liệu (khi không chọn thì dùng nguồn dữ liệu mặc định)
          placeholder: 'Vui lòng chọn nguồn dữ liệu (nếu không chọn thì đi qua nguồn dữ liệu mặc định; đồng bộ tạo bảng và thêm/xóa/sửa/truy vấn dữ liệu đều đi qua nguồn dữ liệu này)',
          // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】Cho phép không chọn nguồn dữ liệu (khi không chọn thì dùng nguồn dữ liệu mặc định)
        },
        dynamicDisabled: ({ model }) => !model.enableMultiDataSource,
      },
      'right'
    ),
    // Bảng phụ: chỉ khi đã kế thừa được nguồn dữ liệu của bảng chính, mới hiển thị chỉ đọc nguồn dữ liệu đó (chưa kế thừa/Khi là nguồn dữ liệu mặc định thì không trưng bày trường này)
    mapFormSchema(
      {
        label: 'Nguồn dữ liệu',
        field: 'dbSource',
        component: 'JDictSelectTag',
        ifShow: ({ model }) => !isNotSubTable() && !!model.dbSource,
        helpMessage: 'Nguồn dữ liệu của bảng phụ đi theo bảng chính, vui lòng thiết lập trong cấu hình mở rộng của bảng chính tương ứng',
        componentProps: {
          dictCode: 'sys_data_source,name,code',
          disabled: true,
        },
      },
      'left'
    ),
    // Phần giữ chỗ, ghép thành một hàng với nguồn dữ liệu chỉ đọc ở trên
    mapFormSchema(
      {
        label: '',
        field: 'dbSource',
        component: 'Input',
        ifShow: ({ model }) => !isNotSubTable() && !!model.dbSource,
        render: () => '',
      },
      'right'
    ),
    // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
    // Cách thức xuất hình ảnh
    mapFormSchema(
      {
        label: 'Cách thức xuất hình ảnh',
        field: 'imageExportMode',
        component: 'RadioButtonGroup',
        helpMessage: 'Việc nhúng hình ảnh sẽ làm tăng Excel dung lượng tệp; đường dẫn hình ảnh phù hợp với trường hợp chỉ giữ địa chỉ tệp',
        itemProps: { class: 'image-export-mode' },
        componentProps: {
          options: [
            { label: 'Nhúng hình ảnh', value: 'embed' },
            { label: 'Đường dẫn hình ảnh', value: 'path' },
          ],
          buttonStyle: 'solid',
        },
        defaultValue: 'path',
      },
      'left'
    ),
    // Tại đây là phần giữ chỗ (placeholder)
    mapFormSchema(
      {
        label: '',
        field: 'imageExportMode',
        component: 'InputNumber',
        render: () => '',
      },
      'right'
    ),
  ];

  // update-begin--author:jeecg---date:20260810---for：【LHZP-122】Bảng phụ một-nhiều chỉ cho phép cấu hình popup toàn màn hình mặc định, độ rộng popup và biểu mẫu Label Độ dài, các cấu hình mở rộng còn lại thống nhất vô hiệu hóa
  const oneToManySubEnabledFields = new Set(['modelFullscreen', 'modalMinWidth', 'formLabelLengthShow', 'formLabelLength']);
  const formSchemas = baseFormSchemas.map((schema) => {
    if (oneToManySubEnabledFields.has(schema.field)) {
      return schema;
    }
    const originalDynamicDisabled = schema.dynamicDisabled;
    return {
      ...schema,
      dynamicDisabled: (params) => {
        const { tableType, relationType } = _props.parentForm.getFieldsValue(['tableType', 'relationType']);
        if (tableType === 3 && relationType === 0) {
          return true;
        }
        return typeof originalDynamicDisabled === 'function' ? originalDynamicDisabled(params) : originalDynamicDisabled ?? false;
      },
    };
  });
  // update-end--author:jeecg---date:20260810---for：【LHZP-122】Bảng phụ một-nhiều chỉ cho phép cấu hình popup toàn màn hình mặc định, độ rộng popup và biểu mẫu Label Độ dài, các cấu hình mở rộng còn lại thống nhất vô hiệu hóa
  return { formSchemas };
}

/** Lấy Sinh mã FormSchemas */
export function useCodeGeneratorFormSchemas(_, handlers, single: Ref<boolean>) {
  type SpanType = 'one' | 'tow' | 'towOne';
  // Bố cục động
  const mapFormSchema = bindMapFormSchema<SpanType>(
    {
      // một cột
      one: {
        colProps: { xs: 24, sm: 24 },
        itemProps: {
          labelCol: { xs: 24, sm: 5 },
          wrapperCol: { xs: 24, sm: 16 },
        },
      },
      // một cột trong hai cột
      towOne: {
        colProps: { xs: 24, sm: 24 },
        itemProps: {
          labelCol: { xs: 24, sm: 3 },
          wrapperCol: { xs: 24, sm: 20 },
        },
      },
      // hai cột
      tow: {
        colProps: { xs: 24, sm: 12 },
        itemProps: {
          labelCol: { xs: 24, sm: 6 },
          wrapperCol: { xs: 24, sm: 16 },
        },
      },
    },
    'one'
  );
  const getColSize = computed<SpanType>(() => (single.value ? 'one' : 'tow'));
  // Do cần thay đổi bố cục động nên sử dụng computed
  const formSchemas = computed<FormSchema[]>(() => [
    mapFormSchema(
      {
        label: 'Thư mục sinh mã',
        field: 'projectPath',
        render: ({ model, field }) =>
          h(
            Input.Search,
            {
              value: model[field],
              onChange: (e) => {
                model[field] = e.target.value;
                handlers.onProjectPathChange(e);
              },
              onSearch: handlers.onProjectPathSearch,
              disabled: isDisabledAuth('online:codeGenerate:projectPath'),
            },
            {
              enterButton: () =>
                h(
                  Button,
                  {
                    preIcon: 'ant-design:folder-open',
                    disabled: isDisabledAuth('online:codeGenerate:projectPath'),
                  },
                  {
                    default: () => 'Duyệt xem',
                    icon: () => h(FolderOpenOutlined),
                  }
                ),
            }
          ),
        component: 'InputSearch',
        required: true,
        // Nếu số phiên bản là1 Biểu thị chưa từng sửa đổi Chưa từng đồng bộ Có thể sửa tên bảng
      },
      single.value ? 'one' : 'towOne'
    ),
    mapFormSchema(
      {
        label: 'Phong cách trang',
        field: 'jspMode',
        component: 'Select',
        componentProps: {
          options: handlers.jspModeOptions.value,
          // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-895】Bỏ kiểu trangx
          allowClear: false,
          // update-end--author:liaozhiyang---date:20240603---for：【TV360X-895】Bỏ kiểu trangx
        },
      },
      getColSize.value
    ),
    mapFormSchema(
      {
        label: 'Thuyết minh chức năng',
        field: 'ftlDescription',
        component: 'Input',
      },
      getColSize.value
    ),
    { label: 'Mô hình dữ liệu', field: 'jformType', component: 'Input', show: false },
    mapFormSchema(
      {
        label: 'Tên bảng',
        field: 'tableName_tmp',
        required: true,
        dynamicDisabled: true,
        component: 'Input',
      },
      getColSize.value
    ),
    mapFormSchema(
      {
        label: 'Tên lớp thực thể',
        field: 'entityName',
        required: true,
        component: 'Input',
        componentProps: {
          placeholder: 'Vui lòng nhập tên lớp thực thể (entity)(Viết hoa chữ cái đầu)',
        },
      },
      getColSize.value
    ),
    mapFormSchema(
      {
        label: 'Tên gói(Chữ thường)',
        field: 'entityPackage',
        component: 'Input',
        rules: [{ required: true, pattern: /^[a-zA-Z0-9._]*$/, message: 'Tên gói bắt buộc điền, và chỉ cho phép kết hợp chữ cái, chữ số, dấu gạch dưới, dấu chấm' }],
      },
      getColSize.value
    ),
    mapFormSchema(
      {
        label: 'Kiểu dáng phân tầng mã',
        field: 'packageStyle',
        component: 'Select',
        componentProps: {
          disabled: true,
          options: [
            { label: 'Phân tầng nghiệp vụ', value: 'service' },
            { label: 'Phân tầng mã', value: 'project' },
          ],
        },
      },
      getColSize.value
    ),
    mapFormSchema(
      {
        label: 'Mã trang',
        field: 'vueStyle',
        required: true,
        component: 'Input',
        defaultValue: 'vue3',
        slot: 'pageCode',
        // componentProps: {
        //   options: [
        //     { label: 'Vue3(BasicForm)', value: 'vue3' },
        //     { label: 'Vue3Nguyên bản (native)(a-form)', value: 'vue3Native' },
        //     { label: 'Vue2', value: 'vue' },
        //   ],
        // },
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-1057】Trang sinh mã: di chuột vào mã thì hiển thị phần thuyết minh
        // dynamicPropskey: 'options',
        // dynamicPropsVal: ({ model }) => {
        //   if (model.jspMode && (model.jspMode == 'innerTable' || model.jspMode == 'tab')) {
        //     return [
        //       { value: 'vue3', label: 'Biểu mẫu được đóng gói(BasicForm)' },
        //     ];
        //   } else {
        //     return [
        //       { value: 'vue3', label: 'Biểu mẫu được đóng gói(BasicForm)' },
        //       { value: 'vue3Native', label: 'Biểu mẫu nguyên bản(a-form)' },
        //     ];
        //   }
        // },
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1057】Trang sinh mã: di chuột vào mã thì hiển thị phần thuyết minh
      },
      getColSize.value
    ),
    { label: 'Mã cần tạo', field: 'codeTypes', component: 'Input', show: false },
  ]);

  return { formSchemas };
}
