import { watch, onUnmounted } from 'vue';
import { buildUUID } from '/@/utils/uuid';
import { useGlobSetting } from '/@/hooks/setting';
import { useUserStore } from '/@/store/modules/user';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';
import { isArray } from '/@/utils/is';
import { getToken } from '/@/utils/auth';

// vxe socket
const vs = {
  // id duy nhất của trang, dùng để xác định cùng một người dùng, các trang khác nhauwebsocket
  pageId: buildUUID(),
  // webSocket Đối tượng
  ws: null,
  // Một số hằng số
  constants: {
    // Loại tin nhắn
    TYPE: 'type',
    // Dữ liệu tin nhắn
    DATA: 'data',
    // Loại thông báo: Kiểm tra heartbeat
    TYPE_HB: 'heart_beat',
    // Loại tin nhắn: cập nhật dữ liệu vxe table
    TYPE_UVT: 'update_vxe_table',
  },
  // Kiểm tra heartbeat
  heartCheck: {
    // Thời gian khoảng cách, bao lâu gửi một tin nhắn heartbeat
    interval: 10000,
    // Thời gian timeout của tin nhắn heartbeat, sau bao lâu tin nhắn heartbeat không có phản hồi thì kết nối lại
    timeout: 6000,
    timeoutTimer: -1,
    clear() {
      clearTimeout(this.timeoutTimer);
      return this;
    },
    start() {
      vs.sendMessage(vs.constants.TYPE_HB, '');
      // Nếu sau một khoảng thời gian nhất định vẫn chưa đặt lại, nghĩa là backend đã chủ động ngắt kết nối
      this.timeoutTimer = window.setTimeout(() => {
        vs.reconnect();
      }, this.timeout);
      return this;
    },
    // Tin nhắn heartbeat trả về
    back() {
      this.clear();
      window.setTimeout(() => this.start(), this.interval);
    },
  },

  /** Khởi tạo WebSocket */
  initialWebSocket() {
    if (this.ws === null) {
      const userId = useUserStore().getUserInfo?.id;
      const domainURL = useGlobSetting().uploadUrl!;
      const domain = domainURL.replace('https://', 'wss://').replace('http://', 'ws://');
      const url = `${domain}/vxeSocket/${userId}/${this.pageId}`;
      // Giải thích logic mã: v2.4.6 phía máy chủ websocket, tồn tại vấn đề về hiệu năng và bảo mật. #3278
      let token = (getToken() || '') as string;
      this.ws = new WebSocket(url, [token]);
      this.ws.onopen = this.on.open.bind(this);
      this.ws.onerror = this.on.error.bind(this);
      this.ws.onmessage = this.on.message.bind(this);
      this.ws.onclose = this.on.close.bind(this);
    }
  },

  // Gửi tin nhắn
  sendMessage(type, message) {
    try {
      let ws = this.ws;
      if (ws != null && ws.readyState === ws.OPEN) {
        ws.send(
          JSON.stringify({
            type: type,
            data: message,
          })
        );
      }
    } catch (err: any) {
      console.warn('[JVxeWebSocket] Gửi tin nhắn thất bại:(' + err.code + ')');
    }
  },

  /** Liên kết bảng VXE toàn cục */
  tableMap: new Map(),
  /** Thêm liên kết */
  addBind(map, key, value: VmArgs) {
    let binds = map.get(key);
    if (isArray(binds)) {
      binds.push(value);
    } else {
      map.set(key, [value]);
    }
  },
  /** Xóa liên kết */
  removeBind(map, key, value: VmArgs) {
    let binds = map.get(key);
    if (isArray(binds)) {
      for (let i = 0; i < binds.length; i++) {
        let bind = binds[i];
        if (bind === value) {
          binds.splice(i, 1);
          break;
        }
      }
      if (binds.length === 0) {
        map.delete(key);
      }
    } else {
      map.delete(key);
    }
  },
  // Gọi biểu mẫu đã liên kết
  callBind(map, key, callback) {
    let binds = map.get(key);
    if (isArray(binds)) {
      binds.forEach(callback);
    }
  },

  lockReconnect: false,
  /** Thử kết nối lại */
  reconnect() {
    if (this.lockReconnect) return;
    this.lockReconnect = true;
    setTimeout(() => {
      if (this.ws && this.ws.close) {
        this.ws.close();
      }
      this.ws = null;
      console.info('[JVxeWebSocket] Đang thử kết nối lại...');
      this.initialWebSocket();
      this.lockReconnect = false;
    }, 5000);
  },

  on: {
    open() {
      console.info('[JVxeWebSocket] Kết nối thành công');
      this.heartCheck.start();
    },
    error(e) {
      console.warn('[JVxeWebSocket] Kết nối xảy ra lỗi:', e);
      this.reconnect();
    },
    message(e) {
      // Phân tích tin nhắn
      let json;
      try {
        json = JSON.parse(e.data);
      } catch (e: any) {
        console.warn('[JVxeWebSocket] Nhận được thông báo không thể phân tích cú pháp:', e.data);
        return;
      }
      let type = json[this.constants.TYPE];
      let data = json[this.constants.DATA];
      switch (type) {
        // Kiểm tra heartbeat
        case this.constants.TYPE_HB:
          this.heartCheck.back();
          break;
        // Cập nhật dữ liệu form
        case this.constants.TYPE_UVT:
          this.callBind(this.tableMap, data.socketKey, (args) => this.onVM.onUpdateTable(args, ...data.args));
          break;
        default:
          console.warn('[JVxeWebSocket] Nhận được loại tin nhắn không nhận dạng được:' + type);
          break;
      }
    },
    close(e) {
      console.info('[JVxeWebSocket] Kết nối đã bị đóng:', e);
      this.reconnect();
    },
  },

  onVM: {
    /** Nhận được thông báo cập nhật bảng */
    onUpdateTable({ props, data, methods }: VmArgs, row, caseId) {
      if (data.caseId !== caseId) {
        const tableRow = methods.getIfRowById(row.id).row;
        // Lưu cục bộ và cập nhật dữ liệu
        if (tableRow) {
          if (props.reloadEffect) {
            data.reloadEffectRowKeysMap[row.id] = true;
          }
          Object.assign(tableRow, row, { id: tableRow.id });
          methods.getXTable().reloadRow(tableRow);
        }
      }
    },
  },
} as {
  ws: Nullable<WebSocket>;
} & Recordable;

type VmArgs = {
  props: JVxeTableProps;
  data: JVxeDataProps;
  methods: JVxeTableMethods;
};

export function useWebSocket(props: JVxeTableProps, data: JVxeDataProps, methods) {
  const args: VmArgs = { props, data, methods };
  watch(
    () => props.socketReload,
    (socketReload: boolean) => {
      if (socketReload) {
        vs.initialWebSocket();
        vs.addBind(vs.tableMap, props.socketKey, args);
      } else {
        vs.removeBind(vs.tableMap, props.socketKey, args);
      }
    },
    { immediate: true }
  );

  /** Gửi tin nhắn socket để cập nhật dòng */
  function socketSendUpdateRow(row) {
    vs.sendMessage(vs.constants.TYPE_UVT, {
      socketKey: props.socketKey,
      args: [row, data.caseId],
    });
  }

  onUnmounted(() => {
    vs.removeBind(vs.tableMap, props.socketKey, args);
  });

  return {
    socketSendUpdateRow,
  };
}
