import type { FormSchema } from '@/components/Form';

/**
 * Schema biểu mẫu tạo video
 */
export const videoFormSchema: FormSchema[] = [
  {
    field: 'prompt',
    label: 'Mô tả video',
    component: 'InputTextArea',
    required: true,
    componentProps: {
      placeholder: 'Vui lòng mô tả nội dung video bạn muốn tạo, ví dụ: một chú chó Golden chạy trên bãi biển, sóng vỗ vào bờ, nắng đẹp',
      rows: 5,
      maxlength: 500,
      showCount: true,
    },
  },
];

/**
 * Phân loại tình huống
 */
export const categoryList = ['Trình diễn thông thường', 'Marketing sản phẩm', 'Giáo dục đào tạo', 'Thiết kế sáng tạo'];

/**
 * Prompt đặt sẵn dự phòng (dùng khi API backend không khả dụng)
 */
export const fallbackPrompts: Record<string, string[]> = {
  'Trình diễn thông thường': [
    'Một chú chó Golden chạy trên bãi cát vàng, sóng biển nhẹ nhàng vỗ vào bờ, nắng đẹp, cảnh quay chậm',
    'Quay từ trên cao toàn cảnh dãy núi hùng vĩ, mây mù bao quanh giữa các đỉnh núi, ống kính tiến chậm về phía trước',
    'Dưới cây hoa anh đào, cánh hoa rơi theo gió, một con suối nhỏ lặng lẽ chảy, không khí yên tĩnh của buổi chiều xuân',
  ],
  'Marketing sản phẩm': [
    'Một ly cà phê được rót từ từ vào cốc thủy tinh trong suốt, cà phê và sữa hòa quyện tạo thành họa tiết đẹp, cận cảnh macro',
    'Một chiếc đồng hồ thông minh cao cấp xoay chậm trên bệ trưng bày xoay, ánh sáng chiếu lên mặt đồng hồ phản chiếu ánh kim loại, nền đen',
    'Một đôi giày thể thao dẫm vào vũng nước bắn tung tóe, cận cảnh quay chậm, hình ảnh năng động tràn đầy sức sống',
  ],
  'Giáo dục đào tạo': [
    'Trái Đất từ góc nhìn vũ trụ xoay chậm, có thể thấy chi tiết của tầng khí quyển và tầng mây, nền trời sao',
    'Các trang của một cuốn sách bị gió thổi lật nhanh, chữ và hình minh họa lúc ẩn lúc hiện, hình tượng tri thức tuôn chảy',
    'Quá trình phân chia tế bào dưới kính hiển vi, phong cách trực quan hóa khoa học sắc màu tươi sáng',
  ],
  'Thiết kế sáng tạo': [
    'Một thành phố vị lai vào lúc hoàng hôn, ánh đèn neon phản chiếu trên mặt đường mưa, phong cách cyberpunk',
    'Mực tàu lan chậm trong nước, tạo thành ý cảnh tranh sơn thủy trừu tượng, hiệu ứng nghệ thuật phong cách Trung Quốc',
    'Cực quang dưới bầu trời sao nhảy múa trên không, sắc màu rực rỡ, hiệu ứng chụp timelapse',
  ],
};
