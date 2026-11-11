<template>
  <div class="tenant-padding" :class="[`${prefixCls}`]">
    <div class="my-tenant">
      <span style="flex: 1">Tổ chức của tôi</span>
      <span class="invited" @click="invitedClick">Thông tin lời mời của tôi<span class="approved-count" v-if="invitedCount>0">{{invitedCount}}</span></span>
    </div>
    <div class="tenant-list" v-if="dataSource.length>0">
      <div v-for="item in dataSource" class="tenant-list-item" @click="drownClick(item)">
        <div class="tenant-title">
          <div class="item-left">
            <div class="item-name">{{ item.name }}</div>
            <div class="vip-message">
              <div class="item-house" @click.stop="copyClick(item.houseNumber)">
                <span>
                  Số nhà của tổ chức:{{ item.houseNumber }}
                  <Icon icon="ant-design:copy-outlined" style="font-size: 13px; margin-left: 2px" />
                </span>
              </div>
            </div>
          </div>
          <div class="item-right">
            <span v-if="item.userTenantStatus === '3'">
              <span class="pointer examine">Chờ duyệt</span>
              <span class="pointer cancel-apply" @click.stop="cancelApplyClick(item.tenantUserId)">Hủy đơn đăng ký</span>
            </span>
            <span v-else-if="item.userTenantStatus === '5'">
              <span class="pointer examine" @click="joinOrRefuseClick(item.tenantUserId,'1')">Tham gia</span>
              <span class="pointer cancel-apply" @click.stop="joinOrRefuseClick(item.tenantUserId,'4')">Từ chối</span>
            </span>
            <div v-else style="width: 75px"></div>
            <span style="margin-left: 24px">
              <Icon v-if="item.show" icon="ant-design:down-outlined" style="font-size: 13px; color: #707070" />
              <Icon v-else icon="ant-design:right-outlined" style="font-size: 13px; color: #707070" />
            </span>
          </div>
        </div>
        <div class="item-content" v-show="item.show">
          <div class="content-box">
            <div class="content-name"> Danh thiếp tổ chức </div>
            <div class="content-desc">
              <div class="flex-flow">
                <div class="content-des-text">Họ tên</div>
                <div style="font-size: 13px;color: #000000">
                  {{ userDetail.realname }}
                </div>
              </div>
              <div class="flex-flow">
                <div class="content-des-text">Phòng ban</div>
                <div style="font-size: 13px">
                  {{ userDetail.orgCodeTxt ? userDetail.orgCodeTxt : 'Chưa điền' }}
                </div>
              </div>
              <div class="flex-flow">
                <div class="content-des-text">Nghề nghiệp</div>
                <div style="font-size: 13px">
                  {{ userDetail.postText ? userDetail.postText : 'Chưa điền' }}
                </div>
              </div>
            </div>
          </div>
          <div class="footer-box">
            <span
              v-if="item.userTenantStatus !== '3'"
              @click.stop="footerClick('editTenant', item)"
              class="font-color333 flex-center margin-right40 font-size13 pointer"
            >
              <Icon icon="ant-design:edit-outlined" class="footer-icon" />
              <span>Xem danh thiếp đơn vị thuê</span>
            </span>
            <span v-else class="font-color9e flex-center margin-right40 font-size13">
              <Icon icon="ant-design:edit-outlined" class="footer-icon" />
              <span>Xem danh thiếp đơn vị thuê</span>
            </span>
            <span
              v-if="item.userTenantStatus !== '3'"
              @click.stop="footerClick('exitTenant', item)"
              class="font-color333 flex-center margin-right40 font-size13 pointer"
            >
              <Icon icon="ant-design:export-outlined" class="footer-icon" />
              <span>Rời khỏi đơn vị thuê</span>
            </span>
            <span v-else class="font-color9e flex-center margin-right40 font-size13">
              <Icon icon="ant-design:export-outlined" class="footer-icon" />
              <span>Rời khỏi đơn vị thuê</span>
            </span>
          </div>
        </div>
      </div>
    </div>
    <a-empty v-else description="Không có dữ liệu" style="position: relative;top: 50px;"/>
  </div>
  <a-modal v-model:open="tenantVisible" width="400px" wrapClassName="edit-tenant-setting">
    <template #title>
      <div style="font-size: 17px; font-weight: 700">Xem danh thiếp</div>
      <div style="color: #9e9e9e; margin-top: 10px; font-size: 13px"> Danh thiếp là thông tin cá nhân của bạn trong tổ chức này, chỉ hiển thị trong tổ chức này. </div>
    </template>
    <div style="margin-top: 24px; font-size: 13px; padding: 0 24px">
      <div class="font-color75">Họ tên</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.realname }}</div>
      <div>Phòng ban</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.orgCodeTxt ? userDetail.orgCodeTxt : 'Chưa điền' }}</div>
      <div>Chức vụ</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.postText ? userDetail.postText : 'Chưa điền' }}</div>
      <div>Địa điểm làm việc</div>
      <div class="margin-top6 margin-bottom-16">{{ userData.workPlace ? userData.workPlace : 'Chưa điền' }}</div>
      <div>Mã nhân viên</div>
      <div class="margin-top6 margin-bottom-16">{{ userDetail.workNo ? userDetail.workNo : 'Chưa điền' }}</div>
    </div>
  </a-modal>

  <!-- Rời khỏi đơn vị thuê -->
  <a-modal v-model:open="cancelVisible" width="800" destroy-on-close>
    <template #title>
      <div class="cancellation">
        <Icon icon="ant-design:warning-outlined" style="font-size: 20px;color: red"/>
        Rời khỏi đơn vị thuê {{myTenantInfo.name}}
      </div>
    </template>
    <a-form :model="formCancelState" ref="cancelTenantRef">
      <a-form-item name="tenantName">
        <a-row :span="24" style="padding: 20px 20px 0;font-size: 13px">
          <a-col :span="24">
            Vui lòng nhập tên đơn vị thuê
          </a-col>
          <a-col :span="24" style="margin-top: 10px">
            <a-input v-model:value="formCancelState.tenantName" @change="tenantNameChange"/>
          </a-col>
        </a-row>
      </a-form-item>
      <a-form-item name="loginPassword">
        <a-row :span="24" style="padding: 0 20px;font-size: 13px">
          <a-col :span="24">
            Vui lòng nhập mật khẩu đăng nhập của bạn
          </a-col>
          <a-col :span="24" style="margin-top: 10px">
            <a-input-password v-model:value="formCancelState.loginPassword" />
          </a-col>
        </a-row>
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="primary" @click="handleOutClick" :disabled="outBtnDisabled">Xác nhận</a-button>
      <a-button @click="handleCancelOutClick">Hủy</a-button>
    </template>
  </a-modal>

  <a-modal
    title="Thay đổi chủ sở hữu"
    v-model:open="owenVisible"
    width="800"
    destroy-on-close
    :cancelButtonProps="{display:'none'}"
    @ok="changeOwen">
      <div style="padding: 20px">
        <a-row :span="24">
          <div class="change-owen">
            Chỉ sau khi thay đổi chủ sở hữu,mới có thể thoát
          </div>
        </a-row>
        <a-row :span="24" style="margin-top: 10px">
          <UserSelect v-model:value="tenantOwen" izExcludeMy/>
        </a-row>
      </div>
  </a-modal>
  
  <!-- begin Thông tin lời mời của tôi -->
  <a-modal title="Thông tin lời mời của tôi" v-model:open="invitedVisible" :footer="null">
      <a-row :span="24" class="invited-row">
        <a-col :span="16">
          Tổ chức
        </a-col>
        <a-col :span="8">
          Thao tác
        </a-col>
      </a-row>
    <a-row :span="24" class="invited-row-list" v-for="item in invitedList">
      <a-col :span="16">
        {{item.name}}
      </a-col>
      <a-col :span="8">
        <span class="common" @click="joinOrRefuseClick(item.tenantUserId,'1')">Tham gia</span>
        <span class="common refuse" @click="joinOrRefuseClick(item.tenantUserId,'4')">Từ chối</span>
      </a-col>
    </a-row>
    <div style="height: 20px"></div>
  </a-modal>
  <!-- end Thông tin lời mời của tôi -->
</template>

<script lang="ts" name="tenant-setting" setup>
import { onMounted, ref, unref } from "vue";
import { getTenantListByUserId, cancelApplyTenant, exitUserTenant, changeOwenUserTenant, agreeOrRefuseJoinTenant } from "./UserSetting.api";
import { useUserStore } from "/@/store/modules/user";
import { CollapseContainer } from "/@/components/Container";
import { getFileAccessHttpUrl, userExitChangeLoginTenantId } from "/@/utils/common/compUtils";
import headerImg from "/@/assets/images/header.jpg";
import {useMessage} from "/@/hooks/web/useMessage";
import { initDictOptions } from '/@/utils/dict';
import { uniqWith } from 'lodash-es';
import { Modal } from 'ant-design-vue';
import UserSelect from '/@/components/Form/src/jeecg/components/userSelect/index.vue';
import {router} from "/@/router";
import { useDesign } from '/@/hooks/web/useDesign';

const { prefixCls } = useDesign('j-user-tenant-setting-container');
//Nguồn dữ liệu
const dataSource = ref<any>([]);
const userStore = useUserStore();

//Nguồn dữ liệu
const { createMessage } = useMessage();
//Từ điển phòng ban
const departOptions = ref<any>([]);
//Chỉnh sửa đơn vị thuê có ẩn hay không
const tenantVisible = ref<boolean>(false);
//Dữ liệu người dùng
const userData = ref<any>([]);
//Người dùng
const userDetail = ref({
  realname: userStore.getUserInfo.realname,
  workNo: userStore.getUserInfo.workNo,
  orgCodeTxt: userStore.getUserInfo.orgCodeTxt,
  postText: userStore.getUserInfo.postText,
});
/**
 * Khởi tạo dữ liệu đơn vị thuê
 */
  async function initDataSource() {
  //Lấy dữ liệu người dùng
    // Giải thích logic mã: [QQYUN-5608]Sau khi nhập người dùng, sau khi mời,Chỉ cần người được nhập đồng ý,Thêm mới thông tin được mời-----------
    getTenantListByUserId({ userTenantStatus: '1,3,5' }).then((res) => {
      if (res.success) {
        if(res.result && res.result.length>0){
          let result = res.result;
          //Lưu trữ các mảng bình thường và đang duyệt
          let normal:any = [];
          //Lưu trữ thông tin được mời
          let invited:any = [];
          for (let i = 0; i < result.length; i++) {
            let status = result[i].userTenantStatus;
            //Các mục có trạng thái là mời được đưa vào mảng invited
            if(status === '5'){
              invited.push(result[i]);
            }
            normal.push(result[i]);
          }
          dataSource.value = normal;
          invitedList.value = invited;
          invitedCount.value = invited.length;
        }else{
          setInitedValue();
        }
      } else {
        setInitedValue();
      }
    });
  }
  function setInitedValue() {
    dataSource.value = [];
    invitedList.value = [];
    invitedCount.value = 0;  
  }

  /**
   * Sao chép cổng thông tin
   * @param value
   */
  function copyClick(value) {
    // Tạo phần tử input
    const el = document.createElement('input');
    // Gán cho phần tử input văn bản cần sao chép
    el.setAttribute('value', value);
    // Chèn phần tử input vào trang
    document.body.appendChild(el);
    // Chọn văn bản của phần tử input
    el.select();
    // Sao chép nội dung vào bảng tạm (clipboard)
    document.execCommand('copy');
    // Xóa phần tử input
    document.body.removeChild(el);
    createMessage.success('Sao chép thành công');
  };

  /**
   * Hủy đơn đăng ký
   * @param id
   */
  function cancelApplyClick(id) {
    Modal.confirm({
      title: 'Hủy đơn đăng ký',
      content: 'Có hủy đơn đăng ký không',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => {
        cancelApplyTenant({ tenantId: id }).then((res) => {
          if (res.success) {
            createMessage.success('Hủy đơn đăng ký thành công');
            initDataSource();
          }else{
            createMessage.warning(res.message);
          }
        }).catch((e)=>{
           createMessage.warning(e.message);
        });
      },
    });
  };

  /**
   * Sự kiện mở rộng/đóng
   */
  function drownClick(value) {
    if (!value.show) {
      value.show = true;
    } else {
      value.show = false;
    }
  };

  /**
   * Lấy văn bản phòng ban
   * @param value
   */
  function getDepartText(value) {
    let arr = departOptions.value.filter((item) => {
      item.value == value;
    });
    if (arr && arr.length > 0) {
      return arr[0].label;
    }
    return 'Chưa điền';
  };

  /**
   * Sự kiện nhấp vào văn bản ở cuối
   */
  function footerClick(type, item) {
    userData.value = item;
    //Chỉnh sửa danh thiếp tổ chức
    if (type === 'editTenant') {
      tenantVisible.value = true;
    }else if(type === 'exitTenant'){
      //Rời khỏi đơn vị thuê
      formCancelState.value = {loginPassword:'', tenantName:''};
      outBtnDisabled.value = true;
      cancelVisible.value = true;
      myTenantInfo.value = item;
    }
  }

  //Hộp thoại rời khỏi đơn vị thuê
  const cancelVisible = ref<boolean>(false);
  //Dữ liệu rời khỏi đơn vị thuê
  const formCancelState = ref<any>({});
  //Dữ liệu đơn vị thuê
  const myTenantInfo = ref<any>({});
  //Nút xác nhận trong hộp thoại hủy đơn vị thuê có thể nhấn hay không
  const outBtnDisabled = ref<boolean>(true);
  //Chủ sở hữu
  const tenantOwen = ref<string>('');
  //Hộp thoại chủ sở hữu
  const owenVisible = ref<boolean>(false);

  /**
   * Sự kiện thay đổi giá trị tên đơn vị thuê
   */
  function tenantNameChange() {
    let name = unref(myTenantInfo).name;
    let tenantName = unref(formCancelState).tenantName;
    if(name === tenantName){
      outBtnDisabled.value = false;
    }else{
      outBtnDisabled.value = true;
    }
  }

  /**
   * Sự kiện nhấp xác nhận thoát
   */
  async function handleOutClick() {
    if(!unref(formCancelState).loginPassword){
        createMessage.warning("Vui lòng nhập mật khẩu đăng nhập");
        return;
    }
    console.log("myTenantInfo::::",myTenantInfo);
    await exitUserTenant({ id: unref(myTenantInfo).tenantUserId, loginPassword: unref(formCancelState).loginPassword }).then((res) => {
      if (res.success) {
        createMessage.success(res.message);
        cancelVisible.value = false;
        initDataSource();
        // Giải thích logic mã: 【QQYUN-6822】7、Người dùng đăng nhập với nhiều tư cách đơn vị thuê, sau khi rời các đơn vị thuê và chỉ còn một đơn vị thuê thì hiển thị trống---
        userExitChangeLoginTenantId(unref(myTenantInfo).tenantUserId);
      } else {
        if (res.message === 'assignedOwen') {
          //Cần chỉ định người thay đổi
          owenVisible.value = true;
          cancelVisible.value = false;
        // Giải thích logic mã: 【QQYUN-5270】Sau khi rời khỏi tất cả đơn vị thuê đứng tên, đăng nhập lại sẽ nhắc mọi đơn vị thuê đều bị đóng băng. Chủ sở hữu được nhắc đi tới hủy tài khoản------------
        }else if(res.message === 'cancelTenant'){
          cancelVisible.value = false;
          let fullPath = router.currentRoute.value.fullPath;
          Modal.confirm({
            title: 'Bạn là chủ sở hữu của tổ chức này',
            content: 'Tổ chức này không còn thành viên khác, bạn cần đi tới hủy tài khoản',
            okText: 'Đi tới hủy tài khoản',
            okType: 'danger',
            cancelText: 'Hủy',
            onOk: () => {
              if(fullPath === '/system/usersetting'){
                return;
              }
              router.push('/myapps/settings/organization/organMessage/'+unref(myTenantInfo).tenantUserId)
            }
          })
        } else {
          createMessage.warning(res.message);
        }
      }
    }).catch((res) => {
      createMessage.warning(res.message);
    })
  }

  /**
   * Sự kiện hủy khi rời khỏi đơn vị thuê
   */
  function handleCancelOutClick() {
    cancelVisible.value = false;
    outBtnDisabled.value = true;
  }

  /**
   * Thay đổi chủ sở hữu
   */
  function changeOwen() {
    if(!unref(tenantOwen)){
      createMessage.warning("Vui lòng chọn người thay đổi chủ sở hữu");
      return;
    }
    changeOwenUserTenant({ userId:unref(tenantOwen), tenantId:unref(myTenantInfo).tenantUserId }).then((res) =>{
      if(res.success){
        createMessage.success(res.message);
        initDataSource();
        // Giải thích logic mã: 【QQYUN-6822】7、Người dùng đăng nhập với nhiều tư cách đơn vị thuê, sau khi rời các đơn vị thuê và chỉ còn một đơn vị thuê thì hiển thị trống---
        userExitChangeLoginTenantId(unref(myTenantInfo).tenantUserId);
      } else {
        createMessage.warning(res.message);
      }
    })
  }
  
  //Số lượng lời mời
  const invitedCount = ref<number>(0);
  //Thông tin được mời
  const invitedList = ref<any>([]);
  //Hộp thoại thông tin được mời
  const invitedVisible = ref<boolean>(false);

  /**
   * Sự kiện nhấp vào thông tin được mời
   */
  function invitedClick() {
    invitedVisible.value = true;
  }

  /**
   * Sự kiện nhấp vào tham gia tổ chức
   */
  async function joinOrRefuseClick(tenantId,status) {
    await agreeOrRefuseJoinTenant( { tenantId:Number.parseInt(tenantId), status:status });
    initDataSource();
  }

  onMounted(() => {
    initDataSource();
  });

</script>

<style lang="less" scoped>
.tenant-padding{
  padding: 30px 40px 0 20px;
}
.my-tenant{
  display: flex;
  font-size: 17px;
  font-weight: 700!important;
  /*begin Tương thích chế độ tối*/
  color: @text-color;
  /*end Tương thích chế độ tối*/
  margin-bottom: 20px;
  .invited{
    font-size: 14px;
    text-align: right;
    cursor: pointer;
  }
}
.tenant-list{
  box-sizing: border-box;
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
}
.tenant-list-item{
  /*begin Tương thích chế độ tối*/
  border: 1px solid @border-color-base;
  /*end Tương thích chế độ tối*/
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
  overflow: hidden;
  padding: 0 25px;
  width: 100%;
  .item-name{
    align-items: center;
    box-sizing: border-box;
    display: flex;
    justify-content: space-between;
    padding: 14px 0;
    cursor: pointer;
    font-size:17px;
    /*begin Tương thích chế độ tối*/
    color: @text-color;
    /*end Tương thích chế độ tối*/
    font-weight: 700!important;
  }
}
.tenant-list-item:hover{
  box-shadow: 0 1px 2px 0 rgba(0,0,0,0.2);
}
.pointer {
  cursor: pointer;
}

.examine {
  color: #2c9cff;
  font-size: 13px;
}

.cancel-apply {
  margin-left: 24px;
  color: red;
  font-size: 13px;
}

.item-content {
  transition: ease-in 2s;

  .content-box {
    /*begin Tương thích chế độ tối*/
    border-top: 1px solid @border-color-base;
    /*end Tương thích chế độ tối*/
    box-sizing: border-box;
    display: flex;
    padding: 24px 0;
  }

  .content-name {
    /*begin Tương thích chế độ tối*/
    color: @text-color;
    /*end Tương thích chế độ tối*/
    text-align: center;
    width: 100px;
    font-size: 13px;
  }

  .content-desc {
    flex: 1;
    min-width: 0;
  }

  .content-des-text {
    /*begin Tương thích chế độ tối*/
    color: @text-color;
    /*end Tương thích chế độ tối*/
    text-align: left;
    width: 76px;
    font-size: 13px;
  }
}

.flex-flow {
  display: flex;
  min-width: 0;
}

.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 0;
}

.footer-box {
  /*begin Tương thích chế độ tối*/
  border-top: 1px solid @border-color-base;
  /*end Tương thích chế độ tối*/
  box-sizing: border-box;
  display: flex;
  padding: 24px 0;
  color: #757575;
}

.margin-right40 {
  margin-right: 40px;
}

/*begin Tương thích chế độ tối*/
.font-color333 {
  color: @text-color;
  font-weight: normal;
}

.font-color9e {
  color: @text-color;
}

.font-color75 {
  color: @text-color;
}
/*end Tương thích chế độ tối*/

.font-size13 {
  font-size: 13px;
}

.footer-icon {
  font-size: 13px !important;
  margin-right: 13px;
  position: relative;
  top: 0px;
}
:deep(.edit-tenant-setting) {
  color: #0a8fe9;
}
.margin-top6 {
  margin-top: 6px;
}
.margin-bottom-16 {
  margin-bottom: 16px;
}
.item-right {
  align-items: center;
  display: flex;
  .buy-margin{
    margin-left: 10px;
    width: 66px;
    border-radius: 20px;
    background: rgba(255, 154, 0, 1);
    height: 28px;
    line-height: 28px;
    cursor: pointer;
    text-align: center;
    span{
      font-size: 14px;
      font-weight: 400;
      color: #ffffff;
    }
  }
  .ordinary-user{
    margin-left: 10px;
    width: 66px;
    span{
      font-size: 14px;
      font-weight: 400;
      color: #9e9e9e;
    }
  }
}
.tenant-title {
  align-items: center;
  box-sizing: border-box;
  display: flex;
  justify-content: space-between;
  padding: 24px 0;
  .vip-message{
    display: flex;
    .vip-message-margin{
      margin-right: 20px;
    }
  }
}
.change-owen{
  font-size: 14px;
  font-weight: 700;
}
// Giải thích logic mã: Kiểu của hộp thoại được mời------------
.approved-count{
  background: #ffd2d2;
  border-radius: 19px;
  color: red;
  display: inline-block;
  font-weight: 500;
  height: 19px;
  line-height: 18px;
  margin-left: 8px;
  min-width: 19px;
  padding: 0 6px;
  text-align: center;
}

.invited-row{
  padding: 10px 34px;
}
.invited-row-list{
  padding: 0px 34px;
  .common{
    color: #1e88e5;
    cursor: pointer;
  }
  .refuse{
    color: red;
    margin-left: 20px;
  }
}
.pointer{
  cursor: pointer;
}
</style>

<style lang="less">
  // Giải thích logic mã: [issues/563]Một phần chủ đề tối không hoạt động
@prefix-cls: ~'@{namespace}-j-user-tenant-setting-container';
/*begin Tương thích chế độ tối*/
.@{prefix-cls} {

  .my-tenant{
    color: @text-color;
  }

  .tenant-list-item{
    border: 1px solid @border-color-base;

    .item-name{
      color: @text-color;
    }
  }

  .item-content {

    .content-box {
      border-top: 1px solid @border-color-base;
    }

    .content-name {
      color: @text-color;
    }

    .content-des-text {
      color: @text-color;
    }
  }
  .footer-box {
    border-top: 1px solid @border-color-base;
  }

  /*begin Tương thích chế độ tối*/
  .font-color333 {
    color: @text-color;
  }

  .font-color9e {
    color: @text-color;
  }

  .font-color75 {
    color: @text-color;
  }
}
/*end Tương thích chế độ tối*/
</style>
