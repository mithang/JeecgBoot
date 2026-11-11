import { FormSchema } from '@/components/Form';

export const formSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: 'Mô hình',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1. Cần chọn mô hình hình ảnh đã có trong mô hình',
      '2. Mô hình ngôn ngữ Zhipu không hỗ trợ cài đặt kích thước',
      "3. Mô hình phiên bản cũ của openAi như (dall-e-2) cần chọn kích thước, mô hình phiên bản mới nhập trực tiếp 'Bản dọc: 9:16 là được'",
      '4. Hiện tại chỉ có mô hình Qwen Wanxiang (wanx2.1-imageedit, wan2.5-i2i-preview) hỗ trợ tạo ảnh từ ảnh',
      '5. wan2.5-i2i-preview hỗ trợ nhiều hình ảnh',
      '6. Hiện tại tạo ảnh từ văn bản bằng openAi hiệu quả tốt nhất',
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'content',
    label: 'Prompt',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 10,
      placeholder: 'Vui lòng nhập prompt, ví dụ: một chú mèo dễ thương, phong cách cyberpunk',
    },
  },
  {
    field: 'imageUrl',
    label: 'Hình tham khảo',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 2,
    },
  },
  {
    field: 'imageSize',
    label: 'Kích thước hình ảnh',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
        { label: '1:1 (1024x1024)', value: '1024*1024' },
        { label: '16:9 (1280x720)', value: '1280*720' },
        { label: '9:16 (720x1280)', value: '720*1280' },
        { label: '4:3 (1024x768)', value: '1024*768' },
        { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
];

/**
 * Biểu mẫu biểu đồ hỗn hợp
 */
export const mixFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: 'Mô hình',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1. Cần chọn mô hình hình ảnh đã có trong mô hình',
      '2. Hiện tại hỗ trợ mô hình Tongyi Wanxiang wan2.5-i2i-preview',
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'imageSize',
    label: 'Kích thước',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
          { label: '1:1 (1024x1024)', value: '1024*1024' },
          { label: '16:9 (1280x720)', value: '1280*720' },
          { label: '9:16 (720x1280)', value: '720*1280' },
          { label: '4:3 (1024x768)', value: '1024*768' },
          { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
  {
    field: 'imageUrl',
    label: 'Tải lên hình tượng',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 3,
      text: 'Tải lên hình tượng',
    },
    rules: [
      {
        required: true,
        validator: async (_, value) => {
          if (!value) {
            return Promise.reject('Vui lòng tải lên hình tượng');
          }
          const images = value.split(',');
          if (images.length < 2) {
            return Promise.reject('Tổng hợp ít nhất 2 hình ảnh');
          }
          return Promise.resolve();
        },
      },
    ],
  },
  {
    field: 'content',
    label: 'Prompt',
    component: 'InputTextArea',
    defaultValue:
      'Dùng hình 2 làm hình nền chủ thể duy nhất và hình ảnh cuối cùng, chỉ chuyển trang phục mà nhân vật trong hình 1 đang mặc sang nhân vật trong hình 2. Phải giữ nguyên hoàn toàn danh tính, khuôn mặt, ngũ quan, kiểu tóc, thể hình, tư thế cơ thể của nhân vật hình 2 cùng bố cục, môi trường và nền của hình 2, không được dùng hoặc sao chép khuôn mặt, đầu, tư thế cơ thể, danh tính nhân vật và nền của hình 1. Hình 1 chỉ làm tham khảo trang phục, tái hiện chính xác kiểu dáng, màu sắc, họa tiết và chất liệu trang phục của nó, để trang phục ôm tự nhiên vào nhân vật hình 2, giữ nguyên ánh sáng và phong cách hình ảnh vốn có của hình 2. Kết quả cuối cùng phải là người trong hình 2 mặc quần áo của hình 1, chứ không phải tạo ra nhân vật trong hình 1. Độ phân giải cao, phong cách tả thực',
    componentProps: {
      rows: 4,
      placeholder: 'Ví dụ đặt lọ hoa trong hình một vào trong hình hai',
    },
  },
];

/**
 * Biểu mẫu của việc vẽ
 */
export const drawFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: 'Mô hình',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1. Cần chọn mô hình hình ảnh đã có trong mô hình',
      '2. Mô hình ngôn ngữ Zhipu không hỗ trợ cài đặt kích thước',
      "3. Mô hình phiên bản cũ của openAi như (dall-e-2) cần chọn kích thước, mô hình phiên bản mới nhập trực tiếp 'Bản dọc: 9:16 là được'",
      '4. Hiện tại chỉ có mô hình Qwen Wanxiang (wanx2.1-imageedit, wan2.5-i2i-preview) hỗ trợ tạo ảnh từ ảnh',
      '5. wan2.5-i2i-preview hỗ trợ nhiều hình ảnh',
      '6. Hiện tại tạo ảnh từ văn bản bằng openAi hiệu quả tốt nhất',
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'content',
    label: 'Prompt',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      rows: 5,
      placeholder: 'Vui lòng nhập prompt, ví dụ: một chú mèo dễ thương',
    },
  },
  {
    field: 'imageSize',
    label: 'Kích thước hình ảnh',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
        { label: '1:1 (1024x1024)', value: '1024*1024' },
        { label: '16:9 (1280x720)', value: '1280*720' },
        { label: '9:16 (720x1280)', value: '720*1280' },
        { label: '4:3 (1024x768)', value: '1024*768' },
        { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
  {
    field: 'style',
    label: 'Phong cách',
    component: 'Select',
    defaultValue: 'modernOrganic',
    componentProps: {
      options: [
        { label: 'Cyberpunk', value: 'cyberpunk' },
        { label: 'Liên sao', value: 'star' },
        { label: 'Anime', value: 'anime' },
        { label: 'Manga Nhật Bản', value: 'japaneseComicsManga' },
        { label: 'Phong cách tranh thủy mặc', value: 'inkWashPaintingStyle' },
        { label: 'Nguyên tác', value: 'original' },
        { label: 'Tranh phong cảnh', value: 'landscape' },
        { label: 'Minh họa', value: 'illustration' },
        { label: 'Truyện tranh', value: 'manga' },
        { label: 'Hiện đại tự nhiên', value: 'modernOrganic' },
        { label: 'Sáng thế', value: 'genesis' },
        { label: 'Phong cách poster', value: 'posterstyle' },
        { label: 'Chủ nghĩa siêu thực', value: 'surrealism' },
        { label: 'Phác họa', value: 'sketch' },
        { label: 'Hiện thực', value: 'realism' },
        { label: 'Tranh màu nước', value: 'watercolorPainting' },
        { label: 'Chủ nghĩa lập thể', value: 'cubism' },
        { label: 'Đen trắng', value: 'blackAndWhite' },
        { label: 'Phong cách chụp ảnh phim', value: 'fmPhotography' },
        { label: 'Điện ảnh hóa', value: 'cinematic' },
        { label: 'Đặc điểm khuôn mặt rõ nét', value: 'clearFacialFeatures' },
      ],
    },
  },
  {
    field: 'visualAngle',
    label: 'Góc nhìn',
    component: 'Select',
    defaultValue: 'frontView',
    componentProps: {
      options: [
        { label: 'Góc nhìn rộng', value: 'wideView' },
        { label: 'Góc nhìn từ trên cao', value: 'birdView' },
        { label: 'Góc nhìn trên đỉnh', value: 'topView' },
        { label: 'Góc nhìn từ dưới lên', value: 'upview' },
        { label: 'Góc nhìn chính diện', value: 'frontView' },
        { label: 'Đặc tả phần đầu', value: 'headshot' },
        { label: 'Góc nhìn siêu rộng', value: 'ultrawideshot' },
        { label: 'Cảnh trung', value: 'mediumShot' },
        { label: 'Cảnh xa', value: 'longShot' },
        { label: 'Độ sâu trường ảnh', value: 'depthOfField' },
      ],
    },
  },
  {
    field: 'characterShot',
    label: 'Ống kính nhân vật',
    component: 'Select',
    defaultValue: 'fullLengthShot',
    componentProps: {
      options: [
        { label: 'Đặc tả khuôn mặt', value: 'faceShot' },
        { label: 'Đặc tả lớn', value: 'bigCloseUp' },
        { label: 'Đặc tả', value: 'closeUp' },
        { label: 'Từ eo trở lên', value: 'waistShot' },
        { label: 'Từ đầu gối trở lên', value: 'kneeShot' },
        { label: 'Ảnh toàn thân', value: 'fullLengthShot' },
        { label: 'Cảnh cực xa', value: 'extraLongShot' },
      ],
    },
  },
  {
    field: 'lighting',
    label: 'Ánh sáng đèn',
    component: 'Select',
    defaultValue: 'naturalLight',
    componentProps: {
      options: [
        { label: 'Ánh sáng lạnh', value: 'coldLight' },
        { label: 'Ánh sáng ấm', value: 'warmLight' },
        { label: 'Ánh sáng cứng', value: 'hardLighting' },
        { label: 'Ánh sáng kịch tính', value: 'dramaticLight' },
        { label: 'Ánh sáng phản xạ', value: 'reflectionLight' },
        { label: 'Sương mỏng', value: 'mistyFoggy' },
        { label: 'Ánh sáng tự nhiên', value: 'naturalLight' },
        { label: 'Ánh nắng', value: 'sunLight' },
        { label: 'Cảm xúc hóa', value: 'moody' },
      ],
    },
  },
];

/**
 * Biểu mẫu thay khuôn mặt
 */
export const faceSwapFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: 'Mô hình',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: [
      '1. Cần chọn mô hình hình ảnh đã có trong mô hình',
      '2. Hiện tại chỉ hỗ trợ mô hình Tongyi Wanxiang(wan2.5-i2i-preview)'
    ],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
    },
  },
  {
    field: 'sourceImage',
    label: 'Ảnh đại diện của bạn',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 1,
      text: 'Tải lên ảnh đại diện',
    },
  },
  {
    field: 'targetImage',
    label: 'Hình ngôi sao',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 1,
      text: 'Tải lên hình ngôi sao',
    },
  },
  {
    field: 'content',
    label: 'Prompt',
    component: 'InputTextArea',
    required: true,
    defaultValue:
      'Thay khuôn mặt của hình 2 vào vùng khuôn mặt của hình 1, giữ lại đặc trưng khuôn mặt và chi tiết ngũ quan của hình 2, giữ nguyên tư thế cơ thể, kiểu tóc, trang phục, bố cục và nền của hình 1, khuôn mặt hòa trộn tự nhiên, ánh sáng và tông da hài hòa, độ phân giải cao, phong cách tả thực',
    componentProps: {
      rows: 4,
      placeholder: 'Vui lòng nhập prompt thay khuôn mặt',
    },
  },
  {
    field: 'imageSize',
    label: 'Kích thước hình ảnh',
    component: 'Select',
    defaultValue: '720*1280',
    componentProps: {
      options: [
        { label: '1:1 (1024x1024)', value: '1024*1024' },
        { label: '16:9 (1280x720)', value: '1280*720' },
        { label: '9:16 (720x1280)', value: '720*1280' },
        { label: '4:3 (1024x768)', value: '1024*768' },
        { label: '3:4 (768x1024)', value: '768*1024' },
      ],
    },
  },
];
