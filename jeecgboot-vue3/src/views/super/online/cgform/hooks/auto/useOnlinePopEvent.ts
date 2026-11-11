import type { InjectionKey } from 'vue';
import type { Emitter } from '/@/utils/mitt';
import { createContext, useContext } from '/@/hooks/core/useContext';
import mitt from '/@/utils/mitt';
import {onMounted, onUnmounted} from 'vue'

export interface OnlineEmitterContextProps {
  /*activeName?: 'onlineEvent'*/
  onlineEmitter: Emitter
}
const key: InjectionKey<OnlineEmitterContextProps> = Symbol();

/**
 * Mã sự kiện-Mở popup
 */
export const EVENT_OPEN_CODE: string = 'openpopmodal';
/**
 * Mã sự kiện-Đóng popup, lấy dữ liệu biểu mẫu
 */
const EVENT_SUCCESS_CODE: string = 'successpopmodal';

/**
 * Thiết lập sự kiện popup-onlineDanh sách
 * @param callback
 */
export function useOnlineListPopEvent(callback){
  const emitter = mitt();
  function openPopModal(params){
    callback(params);
    console.log('Kích hoạt sự kiện hoàn tất,', params)
  }

  emitter.on(EVENT_OPEN_CODE, openPopModal)
/*  onUnmounted(()=>{
    emitter.off(EVENT_OPEN_CODE, openPopModal)
    console.log('Gỡ gắn kết sự kiện hoàn tất-createOpenPopModalEvent')
  });
  onMounted(()=>{
   
    console.log('Gắn sự kiện hoàn tất-createOpenPopModalEvent')
  });*/
  createOnlineEventContext({
    onlineEmitter: emitter
  });
  console.log('Gắn sự kiện hoàn tất,')
}

/**
 * Đóng popup, trả về dữ liệu biểu mẫu
 * @param params
 */
export function useOnlinePopFormEvent(){
  const { onlineEmitter } = useOnlineEventContext();
  function emitFormData(data){
    onlineEmitter.emit(EVENT_SUCCESS_CODE, data)
  }
  return {
    emitFormData
  }
}


/**
 * Đóng popup, trả về dữ liệu biểu mẫu
 * @param params
 */
export function useOnlineFormEvent(callback){
  const context = useOnlineEventContext();
  const { onlineEmitter } = context;
  function emitData(data){
    callback(data);
    console.log('useOnlineFormEventKích hoạt sự kiện hoàn tất,', data)
  }
  onUnmounted(()=>{
    onlineEmitter && onlineEmitter.off(EVENT_SUCCESS_CODE, emitData)
    console.log('Gỡ gắn kết sự kiện hoàn tất-createOpenPopModalEvent')
  });
  onMounted(()=>{
    onlineEmitter && onlineEmitter.on(EVENT_SUCCESS_CODE, emitData)
    console.log('Gắn sự kiện hoàn tất-createOpenPopModalEvent')
  });
  function openPopModal(emitData){
    console.log('openPopModal', emitData)
    onlineEmitter && onlineEmitter.emit(EVENT_OPEN_CODE, emitData)
  }
  return {
    openPopModal
  }
}

/**
 * Kích hoạt sự kiện popup
 * @param params
 */
/*export function getOnlinePopEvent(){
  const { onlineEmitter } = useOnlineEventContext();
  return {
    onlineEmitter,
    eventCode: EVENT_OPEN_CODE
  };
}*/

function createOnlineEventContext(context: OnlineEmitterContextProps) {
  return createContext<OnlineEmitterContextProps>(context, key, { readonly: false, native: true });
}

export function useOnlineEventContext() {
  return useContext<OnlineEmitterContextProps>(key);
}