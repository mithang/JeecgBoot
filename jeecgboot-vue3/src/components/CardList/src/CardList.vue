<template>
  <div class="p-2">
    <div class="bg-white mb-2 p-4">
      <BasicForm @register="registerForm" />
    </div>
    {{ sliderProp.width }}
    <div class="bg-white p-2">
      <List :grid="{ gutter: 5, xs: 1, sm: 2, md: 4, lg: 4, xl: 6, xxl: grid }" :data-source="data" :pagination="paginationProp">
        <template #header>
          <div class="flex justify-end space-x-2"
            ><slot name="header"></slot>
            <Tooltip>
              <template #title>
                <div class="w-50">Số lượng hiển thị mỗi hàng</div><Slider id="slider" v-bind="sliderProp" v-model:value="grid" @change="sliderChange"
              /></template>
              <Button><TableOutlined /></Button>
            </Tooltip>
            <Tooltip @click="fetch">
              <template #title>Làm mới</template>
              <Button><RedoOutlined /></Button>
            </Tooltip>
          </div>
        </template>
        <template #renderItem="{ item }">
          <ListItem>
            <Card>
              <template #title></template>
              <template #cover>
                <div :class="height">
                  <Image :src="item.imgs[0]" />
                </div>
              </template>
              <template class="ant-card-actions" #actions>
                <!--              <SettingOutlined key="setting" />-->
                <EditOutlined key="edit" />
                <Dropdown
                  :trigger="['hover']"
                  :dropMenuList="[
                    {
                      text: 'Xóa',
                      event: '1',
                      popConfirm: {
                        title: 'Có xác nhận xóa không',
                        confirm: handleDelete.bind(null, item.id),
                      },
                    },
                  ]"
                  popconfirm
                >
                  <EllipsisOutlined key="ellipsis" />
                </Dropdown>
              </template>

              <CardMeta>
                <template #title>
                  <TypographyText :content="item.name" :ellipsis="{ tooltip: item.address }" />
                </template>
                <template #avatar>
                  <Avatar :src="item.avatar" />
                </template>
                <template #description>{{ item.time }}</template>
              </CardMeta>
            </Card>
          </ListItem>
        </template>
      </List>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { EditOutlined, EllipsisOutlined, RedoOutlined, TableOutlined } from '@ant-design/icons-vue';
  import { List, Card, Image, Typography, Tooltip, Slider, Avatar } from 'ant-design-vue';
  import { Dropdown } from '/@/components/Dropdown';
  import { BasicForm, useForm } from '/@/components/Form';
  import { propTypes } from '/@/utils/propTypes';
  import { Button } from '/@/components/Button';
  import { isFunction } from '/@/utils/is';
  import { useSlider, grid } from './data';
  const ListItem = List.Item;
  const CardMeta = Card.Meta;
  const TypographyText = Typography.Text;
  // Lấy thuộc tính slider
  const sliderProp = computed(() => useSlider(4));
  // Tham số component nhận
  const props = defineProps({
    // Tham số yêu cầu API
    params: propTypes.object.def({}),
    //api
    api: propTypes.func,
  });
  //Cung cấp phương thức nội bộ
  const emit = defineEmits(['getMethod', 'delete']);
  //Dữ liệu
  const data = ref([]);
  // Chuyển đổi số lượng mỗi hàng
  // coverHình ảnh tự thích ứng chiều cao
  //Sửa pageSize và yêu cầu lại dữ liệu

  const height = computed(() => {
    return `h-${120 - grid.value * 6}`;
  });
  //Biểu mẫu
  const [registerForm, { validate }] = useForm({
    schemas: [{ field: 'type', component: 'Input', label: 'Loại' }],
    labelWidth: 80,
    baseColProps: { span: 6 },
    actionColOptions: { span: 24 },
    autoSubmitOnEnter: true,
    submitFunc: handleSubmit,
  });
  //Gửi biểu mẫu
  async function handleSubmit() {
    const data = await validate();
    await fetch(data);
  }
  function sliderChange(n) {
    pageSize.value = n * 4;
    fetch();
  }

  // Tự động gửi yêu cầu và công khai phương thức nội bộ
  onMounted(() => {
    fetch();
    emit('getMethod', fetch);
  });

  async function fetch(p = {}) {
    const { api, params } = props;
    if (api && isFunction(api)) {
      const res = await api({ ...params, page: page.value, pageSize: pageSize.value, ...p });
      data.value = res.items;
      total.value = res.total;
    }
  }
  //Liên quan đến phân trang
  const page = ref(1);
  const pageSize = ref(36);
  const total = ref(0);
  const paginationProp = ref({
    showSizeChanger: false,
    showQuickJumper: true,
    pageSize,
    current: page,
    total,
    showTotal: (total) => `Tổng ${total} mục`,
    onChange: pageChange,
    onShowSizeChange: pageSizeChange,
  });

  function pageChange(p, pz) {
    page.value = p;
    pageSize.value = pz;
    fetch();
  }
  function pageSizeChange(current, size) {
    pageSize.value = size;
    fetch();
  }

  async function handleDelete(id) {
    emit('delete', id);
  }
</script>
