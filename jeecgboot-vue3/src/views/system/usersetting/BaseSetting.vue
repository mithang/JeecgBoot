<template>
  <div class="account-padding" :class="[`${prefixCls}`]">
    <div class="user-setting-top">
      <div class="account-avatar">
        <CropperAvatar
          :uploadApi="uploadImg"
          :showBtn="false"
          :value="avatar"
          :btnProps="{ preIcon: 'ant-design:cloud-upload-outlined' }"
          @change="updateAvatar"
          width="80"
        />
        <div class="account-right border-bottom">
          <div v-if="!isEdit">
            <span class="font-size-17 account-name">{{ userInfo.realname }}</span>
            <a-tooltip content="Chỉnh sửa họ tên">
              <Icon class="pointer font-size-17 gray-bd account-icon" icon="ant-design:edit-outlined"
                    @click="editHandleClick" />
            </a-tooltip>
          </div>
          <div v-else>
            <a-input ref="accountNameEdit" :maxlength="100" v-model:value="userInfo.realname" @blur="editRealName" />
          </div>
          <div class="use-day">
            Sử dụng:<span>{{userInfo.createTimeText}}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="account-data border-bottom">
      <!-- Thông tin chi tiết -->
      <div class="account-detail">
        <div class="font-size-15 font-bold font-color-gray" style="margin-bottom: 16px">Thông tin chi tiết</div>
        <div class="margin-bottom-10 font-size-13">
          <span class="gray-75 item-label">Ngày sinh</span>
          <span class="gray-3">{{ userInfo.birthday }}</span>
        </div>
        <div class="margin-bottom-10 font-size-13">
          <span class="gray-75 item-label">Giới tính</span>
          <span class="gray-3">{{ userInfo.sexText }}</span>
        </div>
        <div class="margin-bottom-10 nowarp font-size-13">
          <span class="gray-75 item-label">Chức vụ</span>
          <span class="gray-3">{{ userInfo.postText ? userInfo.postText : "Chưa điền" }}</span>
        </div>
        <div class="font-size-13">
          <span class="item-label"></span>
          <span class="item-label pointer" style="color:#1e88e5" @click="openEditModal">Chỉnh sửa</span>
        </div>
      </div>
      <!-- Thông tin liên hệ -->
      <div class="account-info">
        <div class="font-size-15 font-bold font-color-gray" style="margin-bottom: 16px">Thông tin liên hệ</div>
        <div class="margin-bottom-10 font-size-13">
          <span class="gray-75 item-label">Email</span>
          <span class="gray-3">{{ userInfo.email ? userInfo.email : "Chưa điền" }}</span>
        </div>
        <div class="margin-bottom-10 font-size-13">
          <span class="gray-75 item-label">Điện thoại</span>
          <span class="gray-3">{{ userInfo.phone ? userInfo.phone : "Chưa điền" }}</span>
        </div>
      </div>
    </div>
    <div class="account-data">
      <!-- Chữ ký cá nhân -->
      <div class="account-detail">
        <div class="font-size-15 font-bold font-color-gray" style="margin-bottom: 16px">Chữ ký cá nhân</div>
        <div class="font-size-13 flex">
          <span class="gray-75 item-label">Chữ ký</span>
          <a-upload
            accept="jpg,jpeg,png"  
            :max-count="1"
            :multiple="false"
            name = "file"
            :headers="uploadHeader"
            :action="uploadUrl"
            v-model:fileList="uploadFileList"
            @beforeUpload="beforeUpload"
            @change="handleChange"
            list-type="picture-card"
            @preview="handlePreview"
          >
            <div v-if="uploadVisible">
              <UploadOutlined></UploadOutlined>
              <div class="ant-upload-text">Tải lên</div>
            </div>
          </a-upload>
          <a-modal :width="500" :open="previewVisible" :footer="null" @cancel="handleCancel">
            <img alt="example" style="width: 100%" :src="previewImage" />
          </a-modal>
        </div>
        <div style="font-size: 12px;color:#93a6aa" class="margin-bottom-10">
          <p>Kích thước tải lên đề xuất là200*80, hình ảnh có dung lượng không vượt quá 1M và định dạng png hoặc jpeg</p>
          <p>Cách tạo chữ ký thứ nhất: viết tay, quét rồi tải lên.</p>
          <p>Cách tạo chữ ký thứ hai: dùng chuyển đổi trực tuyến, sau khi tạo xong thì tải lên.
            <a href="http://www.diyiziti.com/qianming" target="_blank">http://www.diyiziti.com/qianming</a>
          </p>
        </div>
        <div class="margin-bottom-10 font-size-13 flex" style="margin-top: 10px">
          <span class="gray-75 item-label">Trạng thái bật</span>
          <a-switch v-model:checked="userInfo.signEnable" :checkedValue="1" :unCheckedValue="0" @change="handleEnableSignChange"></a-switch>
        </div>
      </div>
    </div>
  </div>
  <UserAccountModal @register="registerModal" @success="getUserDetail"></UserAccountModal>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { CollapseContainer } from '/@/components/Container';
import { CropperAvatar } from '/@/components/Cropper';
import { useMessage } from '/@/hooks/web/useMessage';
import headerImg from '/@/assets/images/header.jpg';
import { defHttp } from '/@/utils/http/axios';
import { useUserStore } from '/@/store/modules/user';
import { uploadImg } from '/@/api/sys/upload';
import { getFileAccessHttpUrl, getRandom } from '/@/utils/common/compUtils';
import dayjs from 'dayjs';
import { ajaxGetDictItems, getDictItemsByCode, initDictOptions } from '/@/utils/dict';
import { userEdit, getUserData, queryNameByCodes } from './UserSetting.api';
import UserAccountModal from './commponents/UserAccountModal.vue';
import { useModal } from '/@/components/Modal';
import { cloneDeep } from 'lodash-es';
import { useDesign } from '/@/hooks/web/useDesign';
import { getToken } from "@/utils/auth";
import { uploadUrl } from "@/api/common/api";
import { UploadOutlined } from "@ant-design/icons-vue";

//TODO Khi từ điển cách ly theo đơn vị thuê, dữ liệu sẽ không truy vấn được, mặc định một mục
const sexOption = getDictItemsByCode("sex") || [{text:'Nam',value:'1'},{text:'Nữ',value:'2'}];
const { createMessage } = useMessage();
const userStore = useUserStore();
  const { prefixCls } = useDesign('j-base-setting-container');
//Có chỉnh sửa không
const isEdit = ref<boolean>(false);
//Thông tin người dùng
const userInfo = ref<any>({});
//Khi chỉnh sửa, input kích hoạt sự kiện
const accountNameEdit = ref();
const [registerModal, { openModal }] = useModal();
//Tính toán động ảnh đại diện
const avatar = computed(() => {
  return getFileAccessHttpUrl(userInfo.value.avatar) || headerImg;
});
//headers
const uploadHeader = computed(() => {
  let headers = {};
  headers['X-Access-Token'] = getToken();
  return headers;
});
const { createMessage: $message } = useMessage();
//Danh sách tải lên
const uploadFileList = ref<any>([]);
//Tính toán có thể tiếp tục tải lên hay không
const uploadVisible = computed(() => {
  if(!uploadFileList){
    return true;
  }
  return uploadFileList.value.length < 1;
});
//Xem trước hình ảnh
const previewVisible = ref<boolean>(false);
//Đường dẫn xem trước hình ảnh
const previewImage = ref<string>('');

/**
 * Cập nhật ảnh đại diện người dùng
 */
function updateAvatar(src: string, data: string) {
  const userinfo = userStore.getUserInfo;
  userinfo.avatar = data;
  userStore.setUserInfo(userinfo);
  if (data) {
    updateUserInfo({ avatar: data, id: userinfo.id });
  }
}

/**
 * Cập nhật thông tin người dùng
 * @params Tham số
 */
function updateUserInfo(params) {
  userEdit(params).then((res) => {
    if (!res.success) {
      createMessage.warn(res.message);
    }
  });
}

/**
 * Sự kiện nhấp vào nút chỉnh sửa
 */
function editHandleClick() {
  isEdit.value = true;
  setTimeout(() => {
    accountNameEdit.value.focus();
  }, 100);
}

/**
 * Sửa họ tên thật
 */
function editRealName() {
  if (userInfo.value.realname) {
    updateUserInfo({ realname: userInfo.value.realname, id: userInfo.value.id });
    userStore.setUserInfo(userInfo.value);
  } else {
    createMessage.warn("Vui lòng nhập họ tên");
  }
  isEdit.value = false;
}

/**
 * Lấy thông tin ngày sinh
 */
function getBirthDay(val) {
  if (val) {
    return dayjs(val).format("YYYY-MM-DD");
  } else {
    return "Chưa điền";
  }
}

/**
 * Lấy giới tính
 * @param val
 */
function getSex(val) {
  let findOption = sexOption.find(item => parseInt(item.value) === val);
  let sex = "Chưa điền";
  if (findOption) {
    sex = findOption.text;
  }
  return sex;
}

/**
 * Mở hộp thoại chỉnh sửa
 */
function openEditModal() {
  let value = cloneDeep(userInfo.value);
  openModal(true, {
    record: value
  });
}

/**
 * Lấy thông tin người dùng
 */
function getUserDetail() {
  uploadFileList.value = [];
  getUserData().then((async res => {
    if (res.success) {
      if (res.result) {
        res.result.sexText = getSex(res.result.sex);
        res.result.birthday = getBirthDay(res.result.birthday);
        res.result.createTimeText = getDiffDay(res.result.createTime);
        userInfo.value = res.result;
        if(userInfo.value.sign){
          let sign = userInfo.value.sign;
          let url = getFileAccessHttpUrl(sign);
          uploadFileList.value.push({
            uid: getRandom(10),
            name: getFileName(sign),
            status: 'done',
            url: url,
            response: {
              status: 'history',
              message: sign,
            },
          })
        }
      } else {
        userInfo.value = {};
      }
    }
  }));
}

/**
 * Lấy thời gian sử dụng
 * @param date
 */
function getDiffDay(date) {
  // Tính chênh lệch số ngày giữa hai ngày
  let totalDays, diffDate
  let createDate = Date.parse(date);
  let nowDate = new Date().getTime();
  // Chuyển cả hai ngày thành định dạng mili giây, sau đó trừ nhau
  diffDate = Math.abs(nowDate - createDate) // Lấy giá trị tuyệt đối của số mili giây chênh lệch
  totalDays = Math.floor(diffDate / (1000 * 3600 * 24)) // Làm tròn xuống
  return totalDays+" ngày";
}

/**
 * Trước khi tải hình ảnh lên, tiến hành kiểm chứng
 * 
 * @param file
 */
function beforeUpload({ file }) {
  const isJpgOrPng = file.type === "image/jpeg" || file.type === "image/png" || file.type === "image/jpg";
  if (!isJpgOrPng) {
    $message.error("Định dạng tệp tải lên chỉ có thể làjpg/png");
    return false;
  }
  const isLimit = file.size / 1024 / 1024 < 1;
  if (!isLimit) {
    $message.error("Dung lượng hình ảnh tải lên không được vượt quá 1MB!");
    return false;
  }
  return true;
}

/**
 * Sự kiện tải lên thành công
 */
function handleChange({ file, fileList }) {
  if (file.status === 'error') {
    createMessage.error(`${file.name} Tải lên thất bại.`);
  }
  if (file.status === 'done' && file.response.success === false) {
    const failIndex = uploadFileList.value.findIndex((item) => item.uid === file.uid);
    if (failIndex != -1) {
      uploadFileList.value.splice(failIndex, 1);
    }
    createMessage.warning(file.response.message);
    return;
  }
  if (file.status != 'uploading') {
    fileList.forEach((file) => {
      if (file.status === 'done') {
        //Sự kiện tải lên thành công
        uploadSuccess(file.response.message);
      }
    });
    if (file.status === 'removed') {
      handleDelete();
    }
  }
}

/**
 * Loại bỏ
 */
function handleDelete() {
  updateUser({ sign: "", id: userInfo.value.id },"Xóa chữ ký cá nhân thành công")
}

/**
 * Sự kiện tải lên thành công
 * @param url
 */
function uploadSuccess(url) {
   updateUser({ sign: url, id: userInfo.value.id },"Tải lên chữ ký cá nhân thành công")
}

function updateUser(params,message) {
  userEdit(params).then((res) => {
    if(res.success){
      createMessage.success(message);
    }
  });
}

/**
 * Xem trước hình ảnh
 * @param file
 */
function handlePreview(file) {
  previewImage.value = file.url || file.thumbUrl;
  previewVisible.value = true;
}

/**
 * Lấy tên tệp
 * @param path
 */
function getFileName(path) {
  if (path.lastIndexOf('\\') >= 0) {
    let reg = new RegExp('\\\\', 'g');
    path = path.replace(reg, '/');
  }
  return path.substring(path.lastIndexOf('/') + 1);
}

/**
 * Đóng xem trước hình ảnh
 */
function handleCancel() {
  previewVisible.value = false;
}

/**
 * Cập nhật trạng thái bật của chữ ký cá nhân
 */
function handleEnableSignChange() {
  updateUser({ signEnable: userInfo.value.signEnable, id: userInfo.value.id },"Sửa thành công")
}

onMounted(async () => {
  getUserDetail();
});
</script>

<style lang="less">
    // Giải thích logic mã: [issues/563]Một phần chủ đề tối không hoạt động
  @prefix-cls: ~'@{namespace}-j-base-setting-container';

  .@{prefix-cls}{
    .user-setting-top {
      padding-top: 40px;
      width: 100%;
      border-bottom: 1px solid @border-color-base;
      display: flex;
      padding-bottom: 40px;
    }

    .account-avatar {
      align-items: center;
      display: flex;
      margin-right: 30px;
      flex: 1;
    }

    .change-avatar {
      img {
        display: block;
        margin-bottom: 15px;
        border-radius: 50%;
      }
    }

    .account-right {
      margin-left: 25px !important;
    }

    .font-size-15 {
      font-size: 15px;
    }

    .font-size-17 {
      font-size: 17px;
    }

    .pointer {
      cursor: pointer;
    }

    .account-name {
      white-space: nowrap;
      overflow: hidden;
      width: 200px;
      text-overflow: ellipsis;
      line-height: 32px !important;
      /*begin Tương thích chế độ tối*/
      color: @text-color;
      /*end Tương thích chế độ tối*/
      font-weight: 500;
    }

    .gray-bd {
      color: #bdbdbd;
    }

    .account-icon {
      margin-left: 4px;
    }

    .account-data {
      width: 100% !important;
      display: flex;
      min-width: 0;
    }

    .account-detail {
      width: 40%;
      display: flex;
      flex-direction: column;
      padding: 40px 0;

      .item-label {
        display: inline-block;
        text-align: left;
        width: 80px;
      }
    }

    .font-bold {
      font-weight: 700 !important;
    }

    .margin-bottom-10 {
      margin-bottom: 10px;
    }

    .account-info {
      width: 60%;
      display: flex;
      flex-direction: column;
      padding: 40px 0;
      box-sizing: border-box;
      margin-left: 10px;

      .item-label {
        display: inline-block;
        text-align: left;
        width: 80px;
      }
    }

    .nowarp {
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }

    .account-padding {
      padding-left: 20px !important;
      padding-right: 40px !important;
    }

    .use-day {
      /*begin Tương thích chế độ tối*/
      color: @text-color;
      /*end Tương thích chế độ tối*/
      margin-top: 10px;
      font-size: 13px;
      span {
        color: #1e88e5;
        margin-left: 5px;
      }
    }
    .font-size-13 {
      font-size: 13px;
    }
    .ant-upload-select,.ant-upload-list-item-container{
      width: 200px !important;
      height: 80px !important;
    }
    .ant-upload-list-item-thumbnail .ant-upload-list-item-image {
      object-fit: cover !important;
    }
    p{
      margin-bottom: 5px;
    }
    .border-bottom{
      border-bottom: 1px solid @border-color-base;
    }
  }
</style>
