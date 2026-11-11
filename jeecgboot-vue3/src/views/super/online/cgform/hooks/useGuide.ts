import { onMounted, ref } from 'vue';
import { useDesign } from '/@/hooks/web/useDesign';
import intro from 'intro.js';
import 'intro.js/minified/introjs.min.css';

export const useGuide = () => {
  const { prefixVar } = useDesign('');
  const aiCreateTable = ref(`${prefixVar}-online-aiCreateTable`);
  const newAddBtn = ref(`${prefixVar}-online-newAddBtn`);
  const customBtn = ref(`${prefixVar}-online-customBtn`);
  const enhanceJsBtn = ref(`${prefixVar}-online-enhanceJsBtn`);
  const enhanceSqlBtn = ref(`${prefixVar}-online-enhanceSqlBtn`);
  const enhanceJavaBtn = ref(`${prefixVar}-online-enhanceJavaBtn`);
  const exportDbBtn = ref(`${prefixVar}-online-exportDbBtn`);
  const codeGeneratorBtn = ref(`${prefixVar}-online-codeGenerator`);
  const key = `${prefixVar}-online-guide`;
  const guide = () => {
    let boot = intro();
    boot.setOptions({
      nextLabel: 'Bước tiếp theo',
      prevLabel: 'Bước trước',
      //skipLabel: 'Bỏ qua',
      doneLabel: 'Hoàn thành',
      disableInteraction: true,
      steps: [
        {
          title: 'Bước một',
          element: document.querySelector(`.${newAddBtn.value}`)!,
          intro: 'Nhấp<strong>Thêm mới</strong>nút, tạo mới một bảng.',
        },
        {
          title: 'Bước hai',
          intro: `Tìm dữ liệu vừa mới tạo trong danh sách, tại cột thao tác nhấp vào<strong>"Thêm"</strong>, lựa chọn<strong>"Đồng bộ cơ sở dữ liệu"</strong>。`,
        },
        {
          title: 'Bước ba',
          intro: `Tìm dữ liệu vừa mới tạo trong danh sách, tại cột thao tác nhấp vào<strong>"Thêm"</strong>, lựa chọn<strong>"Kiểm thử chức năng"</strong>。`,
        },
        {
          title: 'AITạo bảng',
          element: document.querySelector(`.${aiCreateTable.value}`)!,
          intro: `Nhập từ trang trí là có thể thông quaAITạo bảng làm việc`,
        },
        {
          title: 'Sinh mã',
          element: document.querySelector(`.${codeGeneratorBtn.value}`)!,
          intro: `Chọn một bản ghi, thông qua sinh mã có thể đưa biểu mẫu đã cấu hình xong, tạo mã frontend và backend bằng một nhấp, nhu cầu phức tạp có thể phát triển tiếp trên cơ sở này.`,
        },
        {
          title: 'Nút tùy chỉnh',
          element: document.querySelector(`.${customBtn.value}`)!,
          intro: `Chọn một bản ghi, nhấp nút tùy chỉnh, cấu hình thông tin liên quan của nút là có thể ở bản ghi hiện tại<strong>"Kiểm thử chức năng"</strong>Trang thêm mới một nút`,
        },
        {
          title: 'JSTự tăng mạnh (tăng cưỡng bức)',
          element: document.querySelector(`.${enhanceJsBtn.value}`)!,
          intro: `Chọn một bản ghi, thông quajsPhần tăng cường có thể đóng vai trò<strong>"Nút tùy chỉnh"</strong>Thêm các thao tác khác nhau, có thể thao tác dữ liệu danh sách và biểu mẫu, v.v., cũng có thể thêm sự kiện tiền tố của biểu mẫu.`,
        },
        {
          title: 'SQLTăng cường',
          element: document.querySelector(`.${enhanceSqlBtn.value}`)!,
          intro: `Chọn một bản ghi, thông qua tăng cườngSQL, có thể liên kết sửa đổi dữ liệu nghiệp vụ.`,
        },
        {
          title: 'javaTăng cường',
          element: document.querySelector(`.${enhanceJavaBtn.value}`)!,
          intro: `Chọn một bản ghi, thông quaJavaPhần tăng cường có thể thực hiện các chức năng bổ sung khi biểu mẫu thêm mới, sửa đổi và xóa dữ liệu, tương tự nhưspringtrong đóAOPlập trình theo khía cạnh (AOP).`,
        },
        {
          title: 'Nhập bảng cơ sở dữ liệu',
          element: document.querySelector(`.${exportDbBtn.value}`)!,
          intro: `Có thể trực tiếp nhập các bảng đã có trong cơ sở dữ liệu để sinh biểu mẫu.`,
        },
      ],
    });
    boot.start();
  };
  onMounted(() => {
    if (!localStorage.getItem(key)) {
      setTimeout(() => {
        guide();
        localStorage.setItem(key, '1');
      }, 2e3);
    }
  });
  return {
    newAddBtn,
    customBtn,
    enhanceJsBtn,
    enhanceSqlBtn,
    exportDbBtn,
    enhanceJavaBtn,
    codeGeneratorBtn,
    aiCreateTable
  };
};
