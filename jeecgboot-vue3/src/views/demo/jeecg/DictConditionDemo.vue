<template>
  <a-card title="SQLKiểm chứng chặn inject (GHSA-jgwm / GHSA-xmw4 / GHSA-g87f）" size="small" style="margin-bottom: 16px">
    <a-alert
      message="Các nút dưới đây sẽ dùng payload lỗ hổng để gọi API liên quan đến danh mục, khi bản sửa có hiệu lực thì tất cả đều phải bị chặn (success=false）。"
      type="warning"
      show-icon
    />
    <a-space direction="vertical" style="width: 100%; margin-top: 12px">
      <div v-for="(c, i) in cases" :key="i">
        <a-space>
          <a-button danger size="small" :loading="c.loading" @click="runCase(c)">{{ c.title }}</a-button>
          <a-tag v-if="c.status === 'blocked'" color="success">Đã chặn</a-tag>
          <a-tag v-else-if="c.status === 'leak'" color="error">Chưa chặn, tồn tại rủi ro</a-tag>
        </a-space>
        <div style="margin: 4px 0 0 4px; color: #999; font-size: 12px">{{ c.desc }}</div>
        <div v-if="c.msg" style="margin: 2px 0 0 4px; font-family: monospace; font-size: 12px; color: #d4380d">{{ c.msg }}</div>
      </div>
    </a-space>
  </a-card>

  <a-alert type="info" show-icon style="margin-bottom: 16px">
    <template #message>Cách viết điều kiện lọc của từ điển bảng (cách dùng hợp lệ)</template>
    <template #description>
      <div>1. Thả xuống / Tìm kiếm: dictCode viết thành "Tên bảng,Trường văn bản,Trường mã,Điều kiện lọc"</div>
      <div>2. Cây dropdown: dict viết thành "tên bảng where điều kiện lọc,Trường văn bản,trường mã", trong điều kiện lọc không được có dấu phẩy (dict tách theo dấu phẩy)</div>
      <div>3. Điều kiện lọc chỉ hỗ trợ tổ hợp "Trường So sánh Hằng số" (=、!=、&gt;、&lt;、like、in、between、is null、and/or, order by), không hỗ trợ chú thích, dấu gạch chéo ngược, truy vấn con và hàm</div>
      <div>4. Bản ghi liên kết hiển thị lại theo id, tìm kiếm theo từ khóa đều là binding tham số, không cần và cũng không được phép truyền điều kiện SQL</div>
    </template>
  </a-alert>
  <BasicForm
    :labelCol="{ span: 6 }"
    :wrapperCol="{ span: 14 }"
    :showResetButton="false"
    :showSubmitButton="false"
    :schemas="schemas"
    :actionColOptions="{ span: 24 }"
  />
</template>
<script lang="ts" setup>
  /**
   * Điều kiện lọc từ điển bảng / Ví dụ bản ghi liên kết + SQLXác minh chặn injection
   *
   * @author scott
   * @since 2026-10-27 GHSA-jgwm/GHSA-xmw4/GHSA-g87f Bổ sung ví dụ sử dụng và kiểm chứng hồi quy sau khi tăng cường kiểm chứng điều kiện danh mục
   */
  import { reactive } from 'vue';
  import { BasicForm, FormSchema } from '/@/components/Form';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  interface InjectCase {
    title: string;
    desc: string;
    request: () => Promise<any>;
    // Tiêm tham số hóa(jgwm)Sẽ không báo lỗi mà là không tra được dữ liệu, cần căn cứ vào lượng dữ liệu trả về để xác định có bị coi là giá trị thuần hay không
    leakWhenData?: boolean;
    loading: boolean;
    status: '' | 'blocked' | 'leak';
    msg: string;
  }

  // Payload được inject chỉ dùng để xác thực chặn, khi phát đi từ frontend sẽ tự động mang theo X-Sign Chữ ký
  const cases = reactive<InjectCase[]>([
    {
      title: 'jgwm: Bản ghi liên kết keyValues dấu ngoặc kép đóng bằng dấu gạch chéo ngược',
      desc: "/sys/dict/queryTableDataForLinkRecord  keyValues=a\\,) or 1=1#(Sau khi tham số hóa sẽ truy vấn được 0 bản ghi)",
      leakWhenData: true,
      loading: false,
      status: '',
      msg: '',
      request: () =>
        defHttp.get(
          {
            url: '/sys/dict/queryTableDataForLinkRecord',
            params: { tableName: 'sys_user', showFields: 'realname,username', valueField: 'id', keyValues: 'a\\,) or 1=1#', pageNo: 1, pageSize: -1 },
          },
          { isTransformResponse: false },
        ),
    },
    {
      title: 'xmw4: bảng từ điển # Chú thích + password# Trường',
      desc: "/sys/dict/loadDict  Điều kiện=username='admin' and password# like '%'",
      loading: false,
      status: '',
      msg: '',
      request: () =>
        defHttp.get(
          { url: "/sys/dict/loadDict/" + encodeURIComponent("sys_user,realname,username,username='admin' and password# like '%'"), params: { pageNo: 1, pageSize: 10 } },
          { isTransformResponse: false },
        ),
    },
    {
      title: 'xmw4: Tên bảng cây thả xuống chèn vào truy vấn con',
      desc: '/sys/dict/loadTreeData  tableName=sys_permission where 1=1 or (select 1 from sys_user)>0#',
      loading: false,
      status: '',
      msg: '',
      request: () =>
        defHttp.get(
          {
            url: '/sys/dict/loadTreeData',
            params: { pid: '', condition: '', pidField: 'parent_id', hasChildField: 'is_leaf', converIsLeafVal: 0, text: 'name', code: 'id', tableName: 'sys_permission where 1=1 or (select 1 from sys_user)>0#' },
          },
          { isTransformResponse: false },
        ),
    },
    {
      title: 'g87f:queryTableData literal null được bỏ qua (bypass)',
      desc: '/sys/dict/queryTableData  table=null&text=sys_user&code=password',
      loading: false,
      status: '',
      msg: '',
      request: () =>
        defHttp.get(
          { url: '/sys/dict/queryTableData', params: { table: 'null', text: 'sys_user', code: 'password', keyword: '%', pageSize: 10 } },
          { isTransformResponse: false },
        ),
    },
    {
      title: 'Lấy trực tiếp trường nhạy cảm sys_user.password',
      desc: '/sys/dict/loadDict/sys_user,password,id',
      loading: false,
      status: '',
      msg: '',
      request: () => defHttp.get({ url: '/sys/dict/loadDict/sys_user,password,id', params: { pageNo: 1, pageSize: 10 } }, { isTransformResponse: false }),
    },
  ]);

  async function runCase(c: InjectCase) {
    c.loading = true;
    c.status = '';
    c.msg = '';
    try {
      const res = await c.request();
      if (c.leakWhenData) {
        // Loại tham số hóa: payload bị xử lý như giá trị thuần thì không tra được dữ liệu(total=0)là an toàn; nếu or 1=1 Nếu có hiệu lực sẽ truy vấn được dữ liệu
        const r = res?.result || {};
        const total = r.total ?? (r.records ? r.records.length : 0);
        if (res?.success && total === 0) {
          c.status = 'blocked';
          c.msg = 'Chuỗi injection bị coi là giá trị tham số, truy vấn ra 0 bản ghi, injection không có hiệu lực';
          createMessage.success('Đã bảo vệ');
        } else {
          c.status = 'leak';
          c.msg = `Inject có hiệu lực, truy vấn được ${total} dòng dữ liệu!`;
          createMessage.error(c.title + ' Chưa chặn');
        }
      } else if (res?.success) {
        c.status = 'leak';
        c.msg = 'Phản hồi success=true, mã inject chưa bị chặn!';
        createMessage.error(c.title + ' Chưa chặn');
      } else {
        c.status = 'blocked';
        c.msg = res?.message || 'Máy chủ đã từ chối';
        createMessage.success('Đã chặn');
      }
    } catch (e: any) {
      c.status = 'blocked';
      c.msg = e?.message || e?.response?.data?.message || 'Yêu cầu bị từ chối';
      createMessage.success('Đã chặn');
    } finally {
      c.loading = false;
    }
  }

  const schemas: FormSchema[] = [
    {
      field: 'condDictSelect',
      component: 'JDictSelectTag',
      label: 'Dropdown bảng từ điển(Có điều kiện)',
      helpMessage: ["sys_user,realname,username,status = '1' and del_flag = 0"],
      componentProps: {
        dictCode: "sys_user,realname,username,status = '1' and del_flag = 0",
      },
      colProps: { span: 12 },
    },
    {
      field: 'condDictSelect',
      component: 'JEllipsis',
      label: 'Giá trị đã chọn',
      colProps: { span: 12 },
    },
    {
      field: 'condDictOrder',
      component: 'JDictSelectTag',
      label: 'Dropdown bảng từ điển(Điều kiện+Sắp xếp)',
      helpMessage: ['sys_user,realname,username,del_flag = 0 order by create_time desc'],
      componentProps: {
        dictCode: 'sys_user,realname,username,del_flag = 0 order by create_time desc',
      },
      colProps: { span: 12 },
    },
    {
      field: 'condDictOrder',
      component: 'JEllipsis',
      label: 'Giá trị đã chọn',
      colProps: { span: 12 },
    },
    {
      field: 'condSearchSelect',
      component: 'JSearchSelect',
      label: 'Tìm kiếm từ điển(Bất đồng bộ có điều kiện)',
      helpMessage: ['Sau khi nhập từ khóa, tìm kiếm phân trang trong phạm vi điều kiện lọc'],
      componentProps: {
        dict: "sys_user,realname,username,status = '1' and del_flag = 0",
        pageSize: 6,
        async: true,
      },
      colProps: { span: 12 },
    },
    {
      field: 'condSearchSelect',
      component: 'JEllipsis',
      label: 'Chọn giá trị',
      colProps: { span: 12 },
    },
    {
      field: 'condTreeSelect',
      component: 'JTreeSelect',
      label: 'Cây dropdown(Tên bảng có điều kiện)',
      helpMessage: ['sys_permission where menu_type != 2,name,id', 'Chỉ hiển thị menu, không hiển thị nút'],
      componentProps: {
        dict: 'sys_permission where menu_type != 2,name,id',
        pidField: 'parent_id',
        hasChildField: 'is_leaf',
        converIsLeafVal: 0,
      },
      colProps: { span: 12 },
    },
    {
      field: 'condTreeSelect',
      component: 'JEllipsis',
      label: 'Giá trị đã chọn',
      colProps: { span: 12 },
    },
    {
      field: 'linkRecord',
      component: 'JLinkTableCard',
      label: 'Bản ghi liên kết(Hiển thị lại theo id)',
      helpMessage: ['Gọi /sys/dict/queryTableDataForLinkRecord', 'Giá trị mặc định là id của admin, mở trang sẽ hiển thị lại theo keyValues', 'Tìm kiếm từ khóa trong popup dùng tham số keyword'],
      defaultValue: 'e9ca23d68d884d4ebb19d07889727dae',
      componentProps: {
        queryMode: 'table',
        tableName: 'sys_user',
        valueField: 'id',
        textField: 'realname,username,sex',
        imageField: 'avatar',
        multi: true,
      },
      colProps: { span: 12 },
    },
    {
      field: 'linkRecord',
      component: 'JEllipsis',
      label: 'Giá trị đã chọn(idphân tách bằng dấu phẩy)',
      colProps: { span: 12 },
    },
  ];
</script>
