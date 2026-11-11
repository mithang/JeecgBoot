<template>
  <PageWrapper title="Số đơn:234231029431" contentBackground>
    <template #extra>
      <a-button> Thao tác 1 </a-button>
      <a-button> Thao tác 2 </a-button>
      <a-button type="primary"> Thao tác chính </a-button>
    </template>

    <template #footer>
      <a-tabs default-active-key="1">
        <a-tab-pane key="1" tab="Chi tiết" />
        <a-tab-pane key="2" tab="Quy tắc" />
      </a-tabs>
    </template>

    <div class="pt-4 m-4 desc-wrap">
      <a-descriptions size="small" :column="2">
        <a-descriptions-item label="Người tạo"> Khúc Lệ Lệ </a-descriptions-item>
        <a-descriptions-item label="Sản phẩm đã đặt"> XX Dịch vụ </a-descriptions-item>
        <a-descriptions-item label="Thời gian tạo"> 2017-01-10 </a-descriptions-item>
        <a-descriptions-item label="Chứng từ liên kết">
          <a>12421</a>
        </a-descriptions-item>
        <a-descriptions-item label="Ngày có hiệu lực"> 2017-07-07 ~ 2017-08-08 </a-descriptions-item>
        <a-descriptions-item label="Ghi chú"> Vui lòng xác nhận trong vòng hai ngày làm việc </a-descriptions-item>
      </a-descriptions>
      <a-card title="Tiến độ quy trình" :bordered="false">
        <a-steps :current="1" progress-dot size="small">
          <a-step title="Tạo dự án">
            <template #description> <div>Jeecg</div> <p>2016-12-12 12:32</p> </template>
          </a-step>
          <a-step title="Sơ duyệt của phòng ban">
            <template #description>
              <p>Chad</p>
            </template>
          </a-step>
          <a-step title="Kiểm tra lại tài chính" />
          <a-step title="Hoàn thành" />
        </a-steps>
      </a-card>

      <a-card title="Thông tin người dùng" :bordered="false" class="mt-5">
        <a-descriptions :column="3">
          <a-descriptions-item label="Họ tên người dùng"> Phó Tiểu Tiểu </a-descriptions-item>
          <a-descriptions-item label="Số thẻ thành viên"> XX 32943898021309809423 </a-descriptions-item>
          <a-descriptions-item label="Chứng minh nhân dân"> 3321944288191034921 </a-descriptions-item>
          <a-descriptions-item label="Thông tin liên hệ"> 18100000000 </a-descriptions-item>
          <a-descriptions-item label="Địa chỉ liên hệ" :span="2"> Khúc Lệ Lệ 18100000000 Ngã tư đường Hoàng Cô Sơn và đường Công Chuyên, quận Tây Hồ, thành phố Hàng Châu, tỉnh Chiết Giang </a-descriptions-item>
        </a-descriptions>

        <a-descriptions title="Nhóm thông tin" :column="3">
          <a-descriptions-item label="Dữ liệu nào đó"> 111 </a-descriptions-item>
          <a-descriptions-item label="Thời gian cập nhật dữ liệu này"> 2017-08-08 </a-descriptions-item>
          <a-descriptions-item label="Dữ liệu nào đó"> 725 </a-descriptions-item>
          <a-descriptions-item label="Thời gian cập nhật dữ liệu này"> 2017-08-08 </a-descriptions-item>
        </a-descriptions>

        <h4>Nhóm thông tin</h4>
        <a-card title="Nhóm thông tin nhiều cấp">
          <a-descriptions title="Tên nhóm" :column="3">
            <a-descriptions-item label="Người phụ trách"> Lâm Đông Đông </a-descriptions-item>
            <a-descriptions-item label="Mã vai trò"> 1234567 </a-descriptions-item>
            <a-descriptions-item label="Phòng ban trực thuộc"> XXCông ty - YYPhòng </a-descriptions-item>
            <a-descriptions-item label="Thời gian hết hạn"> 2017-08-08 </a-descriptions-item>
            <a-descriptions-item label="Mô tả" :span="2"> Đoạn mô tả này rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài rất dài... </a-descriptions-item>
          </a-descriptions>
          <a-divider />
          <a-descriptions title="Tên nhóm" :column="1">
            <a-descriptions-item label="Tên khoa học">
              Citrullus lanatus (Thunb.) Matsum. et NakaiCây dây leo sống một năm; thân, cành thô to, có gờ rõ rệt. Tua cuốn khá thô..
            </a-descriptions-item>
          </a-descriptions>
          <a-divider />
          <a-descriptions title="Tên nhóm" :column="1">
            <a-descriptions-item label="Người phụ trách"> Phó Tiểu Tiểu </a-descriptions-item>
            <a-descriptions-item label="Mã vai trò"> 1234568 </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-card>
      <a-card title="Bản ghi cuộc gọi đến của người dùng trong gần nửa năm qua" class="my-5">
        <Empty />
      </a-card>
      <BasicTable @register="registerTimeTable" />
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { Divider, Card, Empty, Descriptions, Steps, Tabs } from 'ant-design-vue';

  import { refundTimeTableSchema, refundTimeTableData } from './data';
  export default defineComponent({
    components: {
      BasicTable,
      PageWrapper,
      [Divider.name]: Divider,
      [Card.name]: Card,
      Empty,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
      [Steps.name]: Steps,
      [Steps.Step.name]: Steps.Step,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
    },
    setup() {
      const [registerTimeTable] = useTable({
        title: 'Tiến độ trả hàng',
        columns: refundTimeTableSchema,
        pagination: false,
        dataSource: refundTimeTableData,
        showIndexColumn: false,
        scroll: { y: 300 },
      });

      return {
        registerTimeTable,
      };
    },
  });
</script>
