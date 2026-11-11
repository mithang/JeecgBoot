<!-- Cài đặt phiên cổng ứng dụng bên trái -->
<template>
  <div class="space-page">
    <div class="header">
      <img class="header-image" :src="loginLogo" />
      <div class="header-name"> JEECG </div>
    </div>
    <div class="new-session" @click="handleNewSession">
      <div class="left-box">
        <div class="app-icon">
          <Icon icon="ant-design:edit-outlined" size="14"></Icon>
        </div>
        <div class="app-name">Hội thoại mới</div>
      </div>
    </div>
    <div class="session-scroll">
      <div class="section">
        <template v-for="app in apps">
          <div class="app-item" :class="activeKey === app.id ? 'active' : ''" @click="handleAppClick(app)">
            <div class="app-icon">
              <img :src="getAiImg(app)" />
            </div>
            <div class="app-name" :title="app.name">{{ app.name }}</div>
          </div>
        </template>
      </div>

      <div class="section">
        <div class="section-title">Hội thoại lịch sử</div>
        <div v-if="tasks.length" class="task-list">
          <div v-for="task in tasks" :key="task.id" class="task-item" :class="activeKey === task.id ? 'active' : ''" @click="handleTaskClick(task)">
            <div class="task-title" :title="task.title" v-if="!task.isEdit">{{ task.title }}</div>
            <a-input
              class="title"
              ref="inputRef"
              v-if="task.isEdit"
              :defaultValue="task.title"
              placeholder="Vui lòng nhập tiêu đề"
              @change="handleInputChange"
              @keyup.enter="inputBlur(task)"
            />
            <div class="icon-edit">
              <a-space>
                <span class="icon edit" @click.prevent.stop="handleEdit(task)" v-if="!task.isEdit">
                  <svg xmlns="http://www.w3.org/2000/svg" role="img" class="iconify iconify--ri" width="1em" height="1em" viewBox="0 0 24 24">
                    <path
                      fill="currentColor"
                      d="M6.414 15.89L16.556 5.748l-1.414-1.414L5 14.476v1.414zm.829 2H3v-4.243L14.435 2.212a1 1 0 0 1 1.414 0l2.829 2.829a1 1 0 0 1 0 1.414zM3 19.89h18v2H3z"
                    ></path>
                  </svg>
                </span>
                <span class="icon del">
                  <a-popconfirm
                    :overlayStyle="{ 'z-index': 9999 }"
                    title="Xác nhận xóa bản ghi này?"
                    placement="bottom"
                    ok-text="Xác định"
                    cancel-text="Hủy"
                    @confirm.prevent.stop="handleDel(task)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" role="img" class="iconify iconify--ri" width="1em" height="1em" viewBox="0 0 24 24">
                      <path
                        fill="currentColor"
                        d="M17 6h5v2h-2v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V8H2V6h5V3a1 1 0 0 1 1-1h8a1 1 0 0 1 1 1zm1 2H6v12h12zm-9 3h2v6H9zm4 0h2v6h-2zM9 4v2h6V4z"
                      ></path>
                    </svg>
                  </a-popconfirm>
                </span>
              </a-space>
            </div>
          </div>
        </div>
        <a-empty v-else description="Tạm thời chưa có hội thoại lịch sử" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import defaultImg from '@/views/super/airag/aiapp/img/ailogo.png';
  import { defHttp } from '@/utils/http/axios';
  import loginLogo from '/@/assets/images/logo.png';

  const emit = defineEmits(['register', 'app-click', 'task-click', 'new-session-click']);

  const defaultApp = ref({
    id: '1999373661846880258',
    name: 'Trợ lý trò chuyện',
  });
  //Danh sách ứng dụng
  const apps = ref([
    {
      id: '1998717610730352641',
      name: 'Giúp tôi viết',
      icon: 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/upload/test/helpWriting_1765520898059.png',
      prologue: 'Vui lòng nhập\nĐiểm xuất phát:\nĐiểm đến:\nSố người:',
    },
    {
      id: '1996471445272088578',
      name: 'Nhận diện hình tượng',
      icon: 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/temp/1dataOCR_1743065089791.png',
      prologue: 'Tải lên một hình ảnh, tôi sẽ nhận diện nội dung trong ảnh giúp bạn',
    },
    {
      id: '1902262577996546050',
      name: 'Nhìn hình kể chuyện',
      icon: 'https://jeecgdev.oss-cn-beijing.aliyuncs.com/temp/工具-图片解析_1743065064801.png',
      prologue: 'Tải lên một hình ảnh, tôi sẽ kể cho bạn câu chuyện trong ảnh',
    },    
    {
      id: '2008448202536456193',
      name: 'Chat2BI',
      icon: 'https://minio.jeecg.com/otatest/chatShow_1769395642452.png',
      prologue: 'Xin chào, tôi là tác tử tạo biểu đồ Chat2BI.',
      flowId: '2008379264947519489',
      type: 'chatFLow',
      presetQuestion: '[{"key":1,"descr":"Vui lòng thống kê tỷ lệ phân bố giới tính của người dùng hệ thống, và hiển thị bằng biểu đồ tròn và bảng danh sách.","update":true}]'
    },    
    {
      id: '2008090512835629057',
      name: 'AI Vẽ tranh',
      icon: 'https://minio.jeecg.com/otatest/AiWrite_1769395779558.png',
      prologue: 'Xin chào, tôi là tác tử vẽ AI.',
      presetQuestion: '[{"key":1,"descr":"Vui lòng tạo một hình ảnh cô gái trưởng thành phong cách anime Nhật Bản.","update":true}, {"key":2,"descr":"Vui lòng tạo một hình ảnh Na Tra cầm vũ khí trong truyện thần thoại Trung Quốc.","update":true}]',
      metadata:"{\"izDraw\":\"1\"}"
    },  
    {
      id: '1993651187913981953',
      name: 'Hướng dẫn mua sắm sản phẩm',
      icon: 'https://minio.jeecg.com/otatest/shoppingGuide_1769754188966.png',
      prologue: 'Muốn tìm hiểu sản phẩm nào, tôi có thể giới thiệu cho bạn.',
      presetQuestion: '[{"key":1,"sort":1,"descr":"HIP 0603T Series","update":false},{"key":2,"sort":2,"descr":"CHIP 1206HC Series","update":false},{"key":3,"sort":3,"descr":"BRICK 1032ST Series","update":true}]',
    },
  ]);

  //Dữ liệu ứng dụng
  const appData = ref<any>({});

  //Phiên
  const tasks = ref<any>([]);

  /**
   * key được nhấp
   */
  const activeKey = ref<string>('');

  /**
   * Lấy hình ảnh
   */
  const getAiImg = (app) => {
    return getFileAccessHttpUrl(app?.icon) || defaultImg;
  };
  let inputValue: string = '';
  const handleInputChange = (e) => {
    inputValue = e.target.value.trim();
  };

  // Chỉnh sửa
  const handleEdit = (item) => {
    item.isEdit = true;
    inputValue = item.title;
  };

  /**
   * Sự kiện nhấp ứng dụng
   *
   * @param app
   */
  function handleAppClick(app) {
    activeKey.value = app.id;
    appData.value = app;
    emit('app-click', app);
  }

  /**
   * Thêm phiên
   */
  function addSession(title, id) {
    activeKey.value = id;
    if (tasks.value?.length > 0) {
      let findIndex = tasks.value.findIndex((item) => item.id === id);
      if (findIndex >= 0) {
        return;
      }
    }
    tasks.value.unshift({ id: id, title: title });
  }

  /**
   * Lấy phiên
   */
  async function getSessionList() {
    const res = await defHttp.get(
      {
        url: '/airag/chat/getConversationsByType',
        params: { sessionType: 'portal' },
      },
      { isTransformResponse: false }
    );
    if (res && res.success) {
      tasks.value = res.result;
    } else {
      tasks.value = [];
    }
    return tasks.value;
  }

  /**
   * Sự kiện nhấp phiên
   *
   * @param task
   */
  function handleTaskClick(task) {
    if (task.id === activeKey.value) {
      return;
    }
    activeKey.value = task.id;
    emit('task-click', task.id, task.title);
  }

  // Mất tiêu điểm
  function inputBlur(item) {
    item.isEdit = false;
    item.title = inputValue;
    defHttp.put(
      {
        url: '/airag/chat/conversation/update/title',
        params: { id: item.id, title: inputValue, sessionType: 'portal' },
      },
      { joinParamsToUrl: true }
    );
  }

  /**
   * Xóa
   * @param data
   */
  function handleDel(data) {
    defHttp
      .delete(
        {
          url: '/airag/chat/conversation/' + data.id + '/portal',
        },
        { isTransformResponse: false }
      )
      .then(() => {
        getSessionList();
      });
  }

  /**
   * Hội thoại mới
   */
  function handleNewSession() {
    activeKey.value = '';
    emit('app-click', defaultApp.value);
  }

  onMounted(async () => {
    activeKey.value = '';
    const list = await getSessionList();
    if (list && list.length > 0) {
      const first = list[0];
      activeKey.value = first.id;
      emit('task-click', first.id, first.title);
    } else {
      emit('app-click', defaultApp.value);
    }
  });

  defineExpose({
    addSession,
  });
</script>

<style scoped lang="less">
  .space-page {
    padding: 12px 16px;
    height: 100%;
    background: #fbfcff; // Nền sáng bên trái, phân biệt với bên phải
    border-right: 1px solid #eef2f6; // Đường phân cách với phía phải
    display: flex; // Bố cục dọc, trên cùng cố định, phía dưới cuộn
    flex-direction: column;
    box-sizing: border-box;
  }

  .space-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  .space-title {
    font-size: 18px;
    font-weight: 600;
  }

  .section {
    margin-top: 12px;
  }

  .section-title {
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .app-item {
    padding: 8px;
    display: flex;
    align-items: center;
    cursor: pointer;
    margin-bottom: 2px;
  }

  .app-item:hover {
    background: rgba(0, 0, 0, 0.04);
    border-radius: 10px;
  }

  .app-icon {
    width: 14px;
    height: 14px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .app-name {
    margin-left: 10px;
    font-size: 14px;
    color: #1f2329;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .new-session {
    margin-top: 10px;
    align-items: center;
    border-radius: 12px;
    display: flex;
    justify-content: space-between;
    flex-shrink: 0;
    height: 36px;
    margin-right: 1px;
    overflow: visible;
    padding: 6px 10px !important;
    position: relative;
    transition: all 0.15s ease-in-out;
    cursor: pointer;
    margin-bottom: 2px;
    border: 1px solid #e0ecff;
    background: #f5f8ff;
  }

  .new-session:hover {
    background: #eaf2ff;
    border-color: #d4e5ff;
  }

  .new-session.active {
    background: #eaf2ff;
    border-color: #cfe0ff;
  }

  .new-session .left-box {
    display: flex;
    align-items: center;
  }

  .new-session .app-icon {
    width: 20px;
    height: 20px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #eaf2ff;
    color: #3761eb;
  }

  .new-session .app-name {
    margin-left: 10px;
    font-size: 14px;
    color: #3761eb;
    font-weight: 600;
  }

  .task-list {
    position: relative;
  }

  .task-item {
    align-items: center;
    border-radius: 12px;
    display: flex;
    flex-shrink: 0;
    height: 36px;
    margin-right: 1px;
    overflow: visible;
    padding: 6px 10px !important;
    position: relative;
    transition: all 0.15s ease-in-out;
    cursor: pointer;
    margin-bottom: 2px;
    justify-content: space-between;
  }

  .task-item:hover {
    background: rgba(0, 0, 0, 0.04);
    border-radius: 12px;

    .edit,
    .del {
      display: block;
    }
  }

  .task-title {
    font-size: 14px;
    color: #1f2329;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .active {
    background: rgba(0, 0, 0, 0.04);
    border-radius: 12px;
    .edit {
      display: block;
    }
  }

  .edit,
  .del {
    display: none;
  }

  .icon-edit {
    display: flex;
  }
  .session-scroll {
    flex: 1; // Chiếm hết không gian còn lại
    min-height: 0; // Cho phép phần tử con cuộn đúng trong container
    overflow-y: auto; // Chỉ khu vực danh sách cuộn
    margin-bottom: 20px;

    // Kiểu thanh cuộn tùy chỉnh
    &::-webkit-scrollbar {
      width: 7px;
      height: 8px;
    }
    &::-webkit-scrollbar-thumb {
      background-color: #d9dfe7;
      border-radius: 4px;
    }
    &::-webkit-scrollbar-track {
      background-color: transparent;
    }
  }

  .header {
    margin-left: 0;
    display: flex;
    height: 40px;
    padding: 0;
    background-color: #fff;
    .header-image {
      width: 36px;
      height: 36px;
    }
    .header-name {
      color: rgba(0, 0, 0, 0.85);
      font-size: 16px;
      align-self: center;
      margin-left: 8px;
    }
  }
</style>
