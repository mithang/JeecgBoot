<template>
  <div class="index-container-ty">
    <a-row type="flex" justify="start" :gutter="3">
      <a-col :sm="24" :lg="12">
        <a-card>
          <template #title>
            <div class="index-md-title">
              <img src="../../../../assets/images/daiban.png" />
              Việc cần làm của tôi[{{ dataSource1.length }}]
            </div>
          </template>

          <template v-if="dataSource1 && dataSource1.length > 0" #extra>
            <a @click="goPage"
              >Thêm
              <Icon icon="ant-design:double-right-outlined" />
            </a>
          </template>

          <a-table
            :class="'my-index-table tytable1'"
            ref="table1"
            size="small"
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource1"
            :pagination="false"
          >
            <template #ellipsisText="{ text }">
              <JEllipsis :value="text" :length="textMaxLength"></JEllipsis>
            </template>

            <template #dayWarnning="{ text, record }">
              <BellTwoTone style="font-size: 22px" :twoToneColor="getTipColor(record)" />
            </template>

            <template #action="{ text, record }">
              <a @click="handleData">Xử lý</a>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :sm="24" :lg="12">
        <a-card>
          <template #title>
            <div class="index-md-title">
              <img src="../../../../assets/images/zaiban.png" />
              Đang xử lý của tôi[{{ dataSource2.length }}]
            </div>
          </template>

          <template v-if="dataSource2 && dataSource2.length > 0" #extra>
            <a @click="goPage"
              >Thêm
              <Icon icon="ant-design:double-right-outlined" />
            </a>
          </template>

          <a-table
            :class="'my-index-table tytable2'"
            ref="table1"
            size="small"
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource2"
            :pagination="false"
          >
            <template #ellipsisText="{ text }">
              <JEllipsis :value="text" :length="textMaxLength"></JEllipsis>
            </template>

            <template #dayWarnning="{ text, record }">
              <BellTwoTone style="font-size: 22px" :twoToneColor="getTipColor(record)" />
            </template>

            <template #action="{ text, record }">
              <a @click="handleData">Xử lý</a>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :span="24">
        <div style="height: 5px"></div>
      </a-col>

      <a-col :sm="24" :lg="12">
        <a-card>
          <template #title>
            <div class="index-md-title">
              <img src="../../../../assets/images/guaz.png" />
              Treo sổ của tôi[{{ dataSource4.length }}]
            </div>
          </template>

          <a-table
            :class="'my-index-table tytable4'"
            ref="table1"
            size="small"
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource4"
            :pagination="false"
          >
            <template #ellipsisText="{ text }">
              <JEllipsis :value="text" :length="textMaxLength"></JEllipsis>
            </template>

            <template #dayWarnning="{ text, record }">
              <BellTwoTone style="font-size: 22px" :twoToneColor="getTipColor(record)" />
            </template>

            <template #action="{ text, record }">
              <a @click="handleData">Xử lý</a>
            </template>
          </a-table>
        </a-card>
      </a-col>

      <a-col :sm="24" :lg="12">
        <a-card>
          <template #title>
            <div class="index-md-title">
              <img src="../../../../assets/images/duban.png" />
              Giám sát của tôi[{{ dataSource3.length }}]
            </div>
          </template>

          <a-table
            :class="'my-index-table tytable3'"
            ref="table1"
            size="small"
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource3"
            :pagination="false"
          >
            <template #ellipsisText="{ text }">
              <JEllipsis :value="text" :length="textMaxLength"></JEllipsis>
            </template>

            <template #dayWarnning="{ text, record }">
              <BellTwoTone style="font-size: 22px" :twoToneColor="getTipColor(record)" />
            </template>

            <template #action="{ text, record }">
              <a @click="handleData">Xử lý</a>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import noDataPng from '/@/assets/images/nodata.png';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JEllipsis from '/@/components/Form/src/jeecg/components/JEllipsis.vue';
  import { BulbTwoTone, BellTwoTone } from '@ant-design/icons-vue';

  const tempSs1 = [
    {
      id: '001',
      orderNo: 'Điện[1]1267102',
      orderTitle: 'Thuốc có vấn đề',
      restDay: 1,
    },
    {
      id: '002',
      orderNo: 'Điện[4]5967102',
      orderTitle: 'Đã ănxxxThuốc của bệnh viện, bệnh càng ngày càng nặng',
      restDay: 0,
    },
    {
      id: '003',
      orderNo: 'Điện[3]5988987',
      orderTitle: 'Hôm nay đi siêu thị mua trứng, trứng đều hỏng cả',
      restDay: 7,
    },
    {
      id: '004',
      orderNo: 'Điện[2]5213491',
      orderTitle: 'xxCửa hàng thực thể Bảo bán giá caoxx',
      restDay: 5,
    },
    {
      id: '005',
      orderNo: 'Điện[1]1603491',
      orderTitle: 'Dụ dỗ bằng lợi tức, đồng ý hủy bảo hiểm rồi trừ một năm phí',
      restDay: 0,
    },
  ];

  const tempSs2 = [
    {
      id: '001',
      orderTitle: 'Tôi muốn khiếu nại siêu thị lớn này',
      orderNo: 'Điện[1]10299456',
      restDay: 6,
    },
    {
      id: '002',
      orderTitle: 'xxxBệnh viện kê đơn bừa bãi,Bán thuốc giả',
      orderNo: 'Điện[2]20235691',
      restDay: 0,
    },
    {
      id: '003',
      orderTitle: 'Tôi muốn hỏi cửa hàng này làm gì',
      orderNo: 'Điện[3]495867322',
      restDay: 7,
    },
    {
      id: '004',
      orderTitle: 'Tôi muốn tố cáo khách sạn Công viên Aosen, quận Triều Dương',
      orderNo: 'Điện[2]1193849',
      restDay: 3,
    },
    {
      id: '005',
      orderTitle: 'Hôm nay tôi ăn cơm ăn phải một hòn đá',
      orderNo: 'Điện[4]56782344',
      restDay: 9,
    },
  ];

  // 4-7 ngày
  const tip_green = 'rgba(0, 255, 0, 1)';
  // 1-3 ngày
  const tip_yellow = 'rgba(255, 255, 0, 1)';
  // Quá hạn
  const tip_red = 'rgba(255, 0, 0, 1)';

  const textMaxLength = 8;
  const $message = useMessage();

  const dataSource1 = ref([]);
  const dataSource2 = ref([]);
  const dataSource3 = ref([]);
  const dataSource4 = ref([]);
  const columns = [
    {
      title: '',
      dataIndex: '',
      key: 'rowIndex',
      width: 50,
      fixed: 'left',
      align: 'center',
      slots: { customRender: 'dayWarnning' },
    },
    {
      title: 'Số ngày còn lại',
      align: 'center',
      dataIndex: 'restDay',
      width: 80,
    },
    {
      title: 'Tiêu đề ticket',
      align: 'center',
      dataIndex: 'orderTitle',
      slots: { customRender: 'ellipsisText' },
    },
    {
      title: 'Mã ticket',
      align: 'center',
      dataIndex: 'orderNo',
    },
    {
      title: 'Thao tác',
      dataIndex: 'action',
      align: 'center',
      slots: { customRender: 'action' },
    },
  ];

  function getTipColor(rd) {
    let num = rd.restDay;
    if (num <= 0) {
      return tip_red;
    } else if (num >= 1 && num < 4) {
      return tip_yellow;
    } else if (num >= 4) {
      return tip_green;
    }
  }

  function mock() {
    dataSource1.value = tempSs1;
    dataSource2.value = tempSs2;
    dataSource3.value = tempSs1;
    dataSource4.value = tempSs2;
    ifNullDataSource(dataSource4, '.tytable4');
  }

  function ifNullDataSource(ds, tb) {
    if (!ds || ds.length == 0) {
      var tmp = document.createElement('img');
      tmp.src = noDataPng;
      tmp.width = 300;
      let tbclass = `${tb} .ant-table-placeholder`;
      document.querySelector(tbclass).innerHTML = '';
      document.querySelector(tbclass).appendChild(tmp);
    }
  }

  function handleData() {
    $message.createMessage.success('Xử lý xong');
  }

  function goPage() {
    $message.createMessage.success('Vui lòng chuyển trang theo nghiệp vụ cụ thể');
  }

  mock();
</script>

<style scoped lang="less">
  .my-index-table {
    height: 270px;

    table {
      font-size: 14px !important;
    }
  }

  .index-container-ty {
    margin: 12px 12px 0;

    :deep(.ant-card-body) {
      padding: 10px 12px 0 12px;
    }

    :deep(.ant-card-head) {
      line-height: 24px;
      min-height: 24px;
      background: #7196fb !important;

      .ant-card-head-title {
        padding-top: 6px;
        padding-bottom: 6px;
      }

      .ant-card-extra {
        padding: 0;

        a {
          color: #fff;
        }

        a:hover {
          color: #152ede;
        }
      }
    }

    :deep(.ant-table-footer) {
      text-align: right;
      padding: 6px 12px 6px 6px;
      background: #fff;
      border-top: 2px solid #f7f1f1;
    }

    .index-md-title {
      position: relative;
      width: 100%;
      color: #fff;
      font-size: 21px;
      font-family: cursive;
      padding-left: 25px;

      img {
        position: absolute;
        height: 25px;
        left: 0;
      }
    }

    :deep(.ant-table-thead > tr > th),
    :deep(.ant-table-tbody > tr > td) {
      border-bottom: 1px solid #90aeff;
    }

    :deep(
      .ant-table-small
      > .ant-table-content
      > .ant-table-fixed-left
      > .ant-table-body-outer
      > .ant-table-body-inner
      > table
      > .ant-table-thead
      > tr
      > th),
    :deep(
      .ant-table-small
      > .ant-table-content
      > .ant-table-fixed-right
      > .ant-table-body-outer
      > .ant-table-body-inner
      > table
      > .ant-table-thead
      > tr
      > th) {
      border-bottom: 1px solid #90aeff;
    }

    :deep(.ant-table-small > .ant-table-content > .ant-table-scroll > .ant-table-body > table > .ant-table-thead > tr > th) {
      border-bottom: 1px solid #90aeff;
    }

    :deep(.ant-table-small) {
      border: 1px solid #90aeff;
    }

    :deep(.ant-table-placeholder) {
      padding: 0;
      height: 215px;
    }
  }
</style>
