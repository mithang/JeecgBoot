import {knowledgeDeleteAllDoc} from "./AiKnowledgeBase.api";
import {useMessage} from "@/hooks/web/useMessage";

const {createConfirmSync} = useMessage();

// Làm trống tài liệu
export async function doDeleteAllDoc(knowledgeId: string, reload: () => void) {
  const flag = await createConfirmSync({
    title: 'Làm trống tài liệu',
    content: () => (
      <p>
        <span>Xác nhận làm trống tất cả tài liệu?</span>
        <br/>
        <span style="color: #ee0000;">
          Thao tác này sẽ xóa tất cả tài liệu đã nhập, và không thể khôi phục, vui lòng thao tác thận trọng
        </span>
      </p>
    ),
  });
  if (!flag) {
    return;
  }
  knowledgeDeleteAllDoc(knowledgeId, reload)
}
