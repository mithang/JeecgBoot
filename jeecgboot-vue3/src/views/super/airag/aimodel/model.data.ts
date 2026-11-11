import { FormSchema } from '@/components/Form';

import anthropic from './icon/anthropic.png';
import deepspeek from './icon/deepspeek.png';
import ollama from './icon/ollama.png';
import OpenAi from './icon/OpenAi.png';
import qianfan from './icon/qianfan.png';
import qianwen from './icon/qianwen.png';
import zhipuai from './icon/zhipuai.png';
import xinference from './icon/xinference.svg';
import vllm from './icon/vllm.png';
import imstdio from './icon/imstdio.png';
import gemini from './icon/gemini.png';
import kimi from './icon/kimi.ico';
import minimax from './icon/minimax.png';
import volcengine from './icon/volcengine.png';
import { ref } from 'vue';

/**
 * Biểu mẫu
 */
export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tên mô hình',
    field: 'name',
    required: true,
    component: 'Input',
  },
  {
    label: 'Loại mô hình',
    field: 'modelType',
    slot: 'modelType',
    required: true,
    component: 'Select',
  },
  {
    label: 'Mô hình cơ sở',
    field: 'modelName',
    required: true,
    slot: 'modelName',
    component: 'Select',
  },
  {
    label: 'Tên miền API',
    field: 'baseUrl',
    required: true,
    component: 'Input'
  },
  {
    label: 'API Key',
    field: 'apiKey',
    required: ({ values }) => values.provider !== 'XINFERENCE',
    component: 'InputPassword',
    componentProps: {
      autocomplete: 'new-password',
    },
    ifShow: ({ values }) => {
      if(values.provider==="OLLAMA"){
        return false;
      }
      return true;
    },
  },
  {
    label: 'Secret Key',
    field: 'secretKey',
    required: false,
    component: 'InputPassword',
    ifShow: ({ values }) => {
      if (
        values.provider === 'DEEPSEEK' ||
        values.provider === 'OLLAMA' ||
        values.provider === 'OPENAI' ||
        values.provider === 'ZHIPU' ||
        values.provider === 'QWEN' ||
        values.provider === 'ANTHROPIC' ||
        values.provider === 'XINFERENCE' ||
        values.provider === 'VLLM' ||
        values.provider === 'LMSTDIO' ||
        values.provider === 'GOOGLE' ||
        values.provider === 'KIMI' ||
        values.provider === 'MINIMAX' ||
        values.provider === 'VOLCENGINE'
      ) {
        return false;
      }
      return true;
    },
  },
  {
    label: 'Giao thức HTTP1.1',
    field: 'httpVersionOne',
    component: 'Switch',
    defaultValue: 1,
    helpMessage: 'Có dùng giao thức HTTP1.1 hay không, trong trường hợp lâu không phản hồi, có thể thử tắt công tắc này',
    componentProps: {
      checkedValue: 1,
      unCheckedValue: 0,
    },
    ifShow: ({ values }) => {
      //update-begin---author:wangshuai---date:20261013---for：【issues/9888】Ứng dụng AI khi gọi mô hình deepseek1.5 Ollama cục bộ luôn báo lỗi, trợ lý AI thì không sao---
      return values.provider === 'VLLM' || values.provider === 'LMSTDIO' || values.provider === 'XINFERENCE' || values.provider === 'OLLAMA';
      //update-end---author:wangshuai---date:20261013---for：【issues/9888】Ứng dụng AI khi gọi mô hình deepseek1.5 Ollama cục bộ luôn báo lỗi, trợ lý AI thì không sao---
    },
  },
  {
    label: 'Tham số bổ sung',
    field: 'extraParams',
    slot: 'extraParams',
    component: 'Input',
    ifShow: ({ values }) => values.modelType === 'LLM',
  },
  {
    label: 'Nhà cung cấp',
    field: 'provider',
    component: 'Input',
    show: false,
  },
];

/**
 * Ánh xạ đường dẫn hình ảnh
 *
 * @param name
 */
export const imageList = ref<any>({
  ANTHROPIC: anthropic,
  DEEPSEEK: deepspeek,
  OLLAMA: ollama,
  OPENAI: OpenAi,
  QIANFAN: qianfan,
  QWEN: qianwen,
  ZHIPU: zhipuai,
  XINFERENCE: xinference,
  VLLM: vllm,
  LMSTDIO: imstdio,
  GOOGLE: gemini,
  KIMI: kimi,
  MINIMAX: minimax,
  VOLCENGINE: volcengine,
});
