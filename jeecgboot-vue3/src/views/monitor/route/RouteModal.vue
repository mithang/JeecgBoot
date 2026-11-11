<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="getTitle" width="30%" @ok="handleSubmit" destroyOnClose showFooter>
    <a-form ref="formRef" :label-col="labelCol" :wrapper-col="wrapperCol" :model="router" :rules="validatorRules">
      <a-form-item label="ID định tuyến" name="routerId">
        <a-input v-model:value="router.routerId" placeholder="ID định tuyến duy nhất" />
      </a-form-item>
      <a-form-item label="Tên định tuyến" name="name">
        <a-input v-model:value="router.name" placeholder="Tên định tuyến" />
      </a-form-item>
      <a-form-item label="URI định tuyến" name="uri">
        <a-input v-model:value="router.uri" placeholder="URL định tuyến" />
      </a-form-item>
      <a-form-item label="Trạng thái định tuyến" name="status">
        <a-switch default-checked :checked-value="1" :un-checked-value="0" v-model:checked="router.status" />
      </a-form-item>

      <a-form-item name="predicates" label="Điều kiện định tuyến">
        <div v-for="(item, index) in router.predicates">
          <!-- Khi name nằm trong noKeyRouter thì không cần chỉ định key -->
          <template v-if="noKeyRouter.includes(item.name)">
            <a-divider
              >{{ item.name }}
              <DeleteOutlined size="22" @click="removePredicate(router, index)" />
            </a-divider>
            <div>
              <template v-for="(tag, tagIndx) in item.args">
                <a-input
                  ref="inputRef2"
                  v-if="tagIndx == currentTagIndex && index == currentNameIndex"
                  type="text"
                  size="small"
                  :style="{ width: '190px' }"
                  v-model:value="state.inputValue"
                  @change="handleInputChange"
                  @blur="handleInputEditConfirm(item, tag, tagIndx)"
                  @keyup.enter="handleInputEditConfirm(item, tag, tagIndx)"
                />
                <a-tag
                  v-else
                  :key="tag"
                  style="margin-bottom: 2px"
                  :closable="true"
                  @close="() => removeTag(item, tag)"
                  @click="editTag(item, tag, tagIndx, index)"
                >
                  {{ tag }}
                </a-tag>
              </template>
              <a-input
                ref="inputRef"
                v-if="state.inputVisible && index == currentNameIndex"
                type="text"
                size="small"
                :style="{ width: '100px' }"
                v-model:value="state.inputValue"
                @change="handleInputChange"
                @blur="handleInputConfirm(item)"
                @keyup.enter="handleInputConfirm(item)"
              />
              <a-tag v-else style="background: #fff; borderstyle: dashed; margin-bottom: 2px" @click="showInput(item, index)">
                <PlusOutlined size="22" />
                Tạo mới {{ item.name }}
              </a-tag>
            </div>
          </template>
          <!-- Khi name không nằm trong noKeyRouter thì cần chỉ định key -->
          <template v-if="!noKeyRouter.includes(item.name)">
            <a-divider
              >{{ item.name }}
              <DeleteOutlined size="22" @click="removePredicate(router, index)" />
            </a-divider>
            <div>
              <template v-for="(value, key) in item.args">
                <a-row>
                  <a-col :span="5" style="margin-top: 8px">
                    <span v-if="key == 'header'">Tên Header</span>
                    <span v-if="key == 'regexp'">Giá trị tham số</span>
                    <span v-if="key == 'param'">Tên tham số</span>
                    <span v-if="key == 'name'">Tên tham số</span>
                  </a-col>
                  <a-col :span="18">
                    <a-input
                      :defaultValue="value"
                      placeholder="Giá trị tham số"
                      style="width: 70%; margin-right: 8px; margin-top: 3px"
                      @change="(e) => valueChange(e, item.args, key)"
                    />
                  </a-col>
                </a-row>
              </template>
            </div>
          </template>
        </div>
        <p class="btn" style="padding-top: 10px">
          <a-dropdown trigger="click">
            <template #overlay>
              <a-menu>
                <a-menu-item :key="item.name" v-for="item in tagArray" @click="predicatesHandleMenuClick(item)">{{ item.name }}</a-menu-item>
              </a-menu>
            </template>
            <a-button type="dashed" style="margin-left: 8px; width: 100%">
              Thêm điều kiện định tuyến
              <DownOutlined :size="22" />
            </a-button>
          </a-dropdown>
        </p>
      </a-form-item>
      <a-form-item name="predicates" label="Bộ lọc">
        <div v-for="(item, index) in router.filters">
          <a-divider
            >{{ item.name }}
            <DeleteOutlined size="22" @click="removeFilter(router, index)" />
          </a-divider>
          <div v-for="(tag, index) in item.args" :key="tag.key">
            <a-form-item-rest>
              <a-input v-model:value="tag.key" placeholder="Khóa tham số" style="width: 45%; margin-right: 8px" />
              <a-input v-model:value="tag.value" placeholder="Giá trị tham số" style="width: 40%; margin-right: 8px; margin-top: 3px" />
            </a-form-item-rest>
            <CloseOutlined :size="22" @click="removeFilterParams(item, index)" />
          </div>
          <a-button type="dashed" style="margin-left: 28%; width: 37%; margin-top: 5px" size="small" @click="addFilterParams(item)">
            <DownOutlined :size="22" />
            Thêm tham số
          </a-button>
        </div>
        <p class="btn" style="padding-top: 10px">
          <a-dropdown trigger="click">
            <template #overlay>
              <a-menu @click="filterHandleMenuClick">
                <a-menu-item :key="item.key" :name="item.name" v-for="item in filterArray">{{ item.name }}</a-menu-item>
              </a-menu>
            </template>
            <a-button type="dashed" style="margin-left: 8px; width: 100%">
              Thêm bộ lọc
              <DownOutlined />
            </a-button>
          </a-dropdown>
        </p>
      </a-form-item>
    </a-form>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, useAttrs, reactive, nextTick } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { saveOrUpdateRoute } from './route.api';
  import { DeleteOutlined } from '@ant-design/icons-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { CloseOutlined } from '@ant-design/icons-vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const inputRef = ref();
  const inputRef2 = ref();
  let state = reactive({
    inputVisible: false,
    inputValue: '',
  });
  const currentNameIndex = ref(0);
  const currentTagIndex = ref(-1);
  const validatorRules = {
    routerId: [{ required: true, message: 'routerId không được để trống', trigger: 'blur' }],
    name: [{ required: true, message: 'Tên định tuyến không được để trống', trigger: 'blur' }],
    uri: [{ required: true, message: 'uri không được để trống', trigger: 'blur' }],
  };
  const noKeyRouter = ['Path', 'Host', 'Method', 'After', 'Before', 'Between', 'RemoteAddr'];
  const filterArray = [/*{ key: 0, name: 'Bộ ngắt mạch' },*/ { key: 1, name: 'Bộ lọc giới hạn lưu lượng' }];
  const tagArray = ref([
    {
      name: 'Path',
      args: [],
    },
    {
      name: 'Header',
      args: {
        header: '',
        regexp: '',
      },
    },
    {
      name: 'Query',
      args: {
        param: '',
        regexp: '',
      },
    },
    {
      name: 'Method',
      args: [],
    },
    {
      name: 'Host',
      args: [],
    },
    {
      name: 'Cookie',
      args: {
        name: '',
        regexp: '',
      },
    },
    {
      name: 'After',
      args: [],
    },
    {
      name: 'Before',
      args: [],
    },
    {
      name: 'Between',
      args: [],
    },
    {
      name: 'RemoteAddr',
      args: [],
    },
  ]);
  const formRef = ref();
  let router = reactive({});

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    isUpdate.value = !!data?.isUpdate;
    setDrawerProps({ confirmLoading: false });
    initRouter();
    if (unref(isUpdate)) {
      router = Object.assign(router, data.record);
    }
  });
  /**
   * Tiêu đề
   */
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới định tuyến' : 'Chỉnh sửa định tuyến'));

  // Xóa mục cấu hình điều kiện định tuyến
  function removeTag(item, removedTag) {
    let tags = item.args.filter((tag) => tag !== removedTag);
    item.args = tags;
  }

  // Khởi tạo tham số
  function initRouter() {
    router = Object.assign(router, {
      id: '',
      routerId: '',
      name: '',
      uri: '',
      status: 1,
      predicates: [],
      filters: [],
    });
  }

  // Thêm tùy chọn định tuyến
  function predicatesHandleMenuClick(e) {
    router.predicates.push({
      args: e.args,
      name: e.name,
    });
  }

  /**
   * Sự kiện sửa giá trị
   * @param e
   * @param item
   * @param key
   */
  function valueChange(e, item, key) {
    item[key] = e.target.value;
  }

  function editTag(item, tag, tagIndex, index) {
    currentNameIndex.value = index;
    currentTagIndex.value = tagIndex;
    state.inputValue = tag;
    nextTick(() => {
      inputRef2.value[0].focus();
    });
  }

  // Hiển thị ô nhập
  function showInput(item, index) {
    state.inputValue = '';
    state.inputVisible = true;
    currentNameIndex.value = index;
    nextTick(() => {
      inputRef.value[0].focus();
    });
  }

  // Sự kiện thay đổi ô nhập tùy chọn định tuyến
  function handleInputChange(e) {
    console.info('change', e);
    console.info('change', e.target.value);
    //state.value = e.target.value;
    //state.tag=true;
  }

  // Xóa điều kiện định tuyến
  function removePredicate(item, index) {
    item.predicates.splice(index, 1);
  }

  // Xóa tham số bộ lọc
  function removeFilterParams(item, index) {
    item.args.splice(index, 1);
  }

  // Xóa bộ lọc
  function removeFilter(item, index) {
    item.filters.splice(index, 1);
  }

  // Thêm tham số bộ lọc
  function addFilterParams(item) {
    item.args.push({
      key: 'key' + item.args.length + 1,
      value: '',
    });
  }

  // Sự kiện thêm bộ lọc
  function filterHandleMenuClick(e) {
    if (e.key == 0) {
      router.filters.push({
        args: [
          {
            key: 'name',
            value: 'default',
          },
          {
            key: 'fallbackUri',
            value: 'forward:/fallback',
          },
        ],
        name: 'Hystrix',
        title: filterArray[0].name,
      });
    }
    console.info('test', router);
    if (e.key == 1) {
      router.filters.push({
        args: [
          {
            key: 'key-resolver',
            value: '#{@ipKeyResolver}',
          },
          {
            key: 'redis-rate-limiter.replenishRate',
            value: 20,
          },
          {
            key: 'redis-rate-limiter.burstCapacity',
            value: 20,
          },
        ],
        name: 'RequestRateLimiter',
        title: filterArray[0].name,
      });
    }
  }

  // Xác nhận ô nhập
  function handleInputConfirm(item) {
    let tags = item.args;
    const inputValue = state.inputValue;
    if (inputValue && tags.indexOf(inputValue) === -1) {
      item.args = [...tags, state.inputValue];
    }
    state.inputVisible = false;
    state.inputValue = '';
    currentTagIndex.value = -1;
    currentNameIndex.value = -1;
  }

  // Xác nhận ô nhập
  function handleInputEditConfirm(item, tag, index) {
    const inputValue = state.inputValue;
    if (inputValue) {
      item.args[index] = state.inputValue;
    }
    currentTagIndex.value = -1;
    currentNameIndex.value = -1;
  }

  // Đóng popup
  function handleCancel() {}

  /**
   * Gửi
   */
  async function handleSubmit() {
    await formRef.value.validate().then(() => {
      try {
        setDrawerProps({ confirmLoading: true });
        // Xây dựng lại đối tượng gửi biểu mẫu, lưu ý không được sửa đối tượng router, sửa mảng thành chuỗi dễ gây rối giao diện
        let params = Object.assign({}, router, {
          predicates: JSON.stringify(router.predicates),
          filters: JSON.stringify(router.filters),
        });
        // Gửi biểu mẫu
        saveOrUpdateRoute({ router: params }).then(() => {
          closeDrawer();
          // Làm mới danh sách
          emit('success');
        });
      } finally {
        setDrawerProps({ confirmLoading: false });
      }
    });
  }
</script>
