import {defineStore} from 'pinia';
import {useUserStoreWithOut} from "@/store/modules/user";
import {removeCacheByDynKey} from "@/utils/auth";
import {TOKEN_KEY} from "@/enums/cacheEnum";
import {getUserInfo} from "@/api/sys/user";

interface StateType {
  cgformRecord: Nullable<Recordable>,
  dataRecord: Nullable<Recordable>,
}

const userStore = useUserStoreWithOut()

export const useShareStore = defineStore('online-cgform-share', {
  state: (): StateType => ({
    cgformRecord: null,
    dataRecord: null,
  }),
  getters: {

    getCgformRecord(): Nullable<Recordable> {
      return this.cgformRecord;
    },
    getDataRecord(): Nullable<Recordable> {
      return this.dataRecord;
    },

  },
  actions: {

    // Kiểm tra url Tham số có mang theo hay khôngtoken
    async checkUrlToken(): Promise<boolean> {
      const token = new URLSearchParams(window.location.search).get('token');
      const flag = token != null && token.length > 0;
      if (flag) {
        userStore.setToken(token);
        // Kiểm tra token Có hiệu lực hay không
        try {
          const res = await getUserInfo();
          if (!res?.userInfo) {
            throw new Error('tokenKhông hợp lệ');
          } else {
            userStore.setUserInfo(res.userInfo)
            if (res.sysAllDictItems) {
              userStore.setAllDictItems(res.sysAllDictItems);
            }
          }
          return true;
        } catch (e) {
          userStore.setToken('');
          removeCacheByDynKey(TOKEN_KEY)
          return false;
        }
      }
      return flag;
    },

    setCgformRecord(value: Nullable<Recordable>) {
      this.cgformRecord = value;
    },

    setDataRecord(value: Nullable<Recordable>) {
      this.dataRecord = value;
    },

  }
});

