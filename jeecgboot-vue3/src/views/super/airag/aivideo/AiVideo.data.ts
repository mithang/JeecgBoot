import { FormSchema } from '@/components/Form';
import { h } from 'vue';
import { Button } from 'ant-design-vue';

/**
 * Cấu hình biểu mẫu tạo video
 */
export const videoFormSchemas: FormSchema[] = [
  // {
  //   label: 'Mô hình',
  //   field: 'model',
  //   component: 'JDictSelectTag',
  //   required: true,
  //   defaultValue: 'video-generation-1',
  //   componentProps: {
  //     dictCode: "airag_model where model_type = 'VIDEO' and activate_flag = 1,name,id",
  //     placeholder: 'Vui lòng chọn mô hình tạo video',
  //   },
  // },
  {
    label: 'Kích thước video',
    field: 'size',
    component: 'Select',
    defaultValue: '1920x1080',
    componentProps: {
      options: [
        { label: '1280x720 (720P)', value: '1280x720' },
        { label: '720x1280', value: '720x1280' },
        { label: '1024x1024', value: '1024x1024' },
        { label: '1920x1080 (1080P)', value: '1920x1080' },
        { label: '1080x1920', value: '1080x1920' },
        { label: '2048x1080 (2K)', value: '2048x1080' },
        { label: '3840x2160 (4K)', value: '3840x2160' },
      ],
      placeholder: 'Vui lòng chọn kích thước video',
    },
  },
  {
    label: 'Tốc độ khung hình video',
    field: 'fps',
    component: 'Select',
    defaultValue: 30,
    componentProps: {
      options: [
        { label: '30 FPS', value: 30 },
        { label: '60 FPS', value: 60 },
      ],
      placeholder: 'Vui lòng chọn tốc độ khung hình video',
    },
  },
  {
    label: 'Thời lượng video',
    field: 'duration',
    component: 'Select',
    defaultValue: 5,
    componentProps: {
      options: [
        { label: '5 giây', value: 5 },
        { label: '10 giây', value: 10 },
      ],
      placeholder: 'Vui lòng chọn thời lượng video',
    },
  },
  {
    label: 'Có phải hiệu ứng âm thanh tổng hợp ai hay không',
    field: 'izAiAudio',
    component: 'Select',
    defaultValue: 0,
    componentProps: {
      options: [
        { label: 'Không', value: 0 },
        { label: 'Là', value: 1 },
      ],
    },  
  }
];
