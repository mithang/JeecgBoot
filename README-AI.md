Giới thiệu nền tảng ứng dụng AIGC
===============

Một nền tảng phát triển AI full-stack, nhằm giúp nhà phát triển nhanh chóng xây dựng và triển khai các ứng dụng AI cá nhân hóa.

Mô-đun chức năng AIGC của nền tảng JeecgBoot tương tự như `Dify` — một `nền tảng phát triển ứng dụng AIGC` + `hỏi đáp trên cơ sở tri thức`, là một nền tảng ứng dụng AI dựa trên mô hình ngôn ngữ lớn LLM và hệ thống hỏi đáp cơ sở tri thức RAG.
Giao diện trực quan của nó kết hợp điều phối luồng AI, pipeline RAG, quản lý cơ sở tri thức, quản lý mô hình, kết nối thư viện vector, khả năng quan sát khi chạy thời gian thực, v.v., giúp bạn nhanh chóng đi từ nguyên mẫu đến sản xuất và sở hữu năng lực dịch vụ AI.



### Video giới thiệu AI

[![](https://jeecgos.oss-cn-beijing.aliyuncs.com/files/jeecg_aivideo.png)](https://www.bilibili.com/video/BV1zmd7YFE4w)


##### Các mô-đun chức năng lớn

- Nền tảng phát triển ứng dụng AI
- Hệ thống cơ sở tri thức AI
- Quản lý mô hình lớn AI
- Điều phối luồng AI
- Trò chuyện AI hỗ trợ hình ảnh
- Trợ lý trò chuyện AI (hỏi đáp thông minh)
- Tạo bảng bằng AI (Biểu mẫu Online)
- Viết bài bằng AI (CMS)
- Gợi ý trường biểu mẫu bằng AI (Trình thiết kế biểu mẫu)



#### Dify `VS` JEECG AI

> So với Dify, JEECG AI thể hiện ưu thế rõ rệt ở nhiều khía cạnh, đặc biệt là trong xử lý tài liệu, giữ nguyên định dạng và hình ảnh. Dưới đây là một số ưu điểm cụ thể:
> - Nhập thư viện tài liệu Markdown:
> JEECG AI cho phép người dùng nhập trực tiếp toàn bộ thư viện tài liệu Markdown, không chỉ giữ nguyên định dạng Markdown mà còn hỗ trợ nhập hình ảnh, đảm bảo tính đầy đủ của nội dung tài liệu và hiệu quả trực quan.
> - Định dạng trả lời trong hội thoại đẹp mắt:
> Trong quá trình trò chuyện, JEECG AI có thể giữ nguyên định dạng gốc của nội dung trả lời, không làm mất hình ảnh, giúp bài viết đầu ra đẹp mắt hơn, không bị lỗi định dạng, đồng thời hỗ trợ hiển thị hình ảnh.
> - Nhập tài liệu PDF và chuyển đổi định dạng:
> Khi xử lý tài liệu PDF, JEECG AI có thể giữ nguyên định dạng gốc và hình ảnh tốt hơn, đảm bảo nội dung sau chuyển đổi nhất quán với tài liệu gốc. Tính năng này thể hiện kém ở nhiều sản phẩm AI, nhưng JEECG AI đã tối ưu hóa đáng kể ở khía cạnh này.


| Tính năng | Dify | Jeecg AI |
|------------|------------------|-----------------------------------------|
| Luồng công việc AI | Có | Có |
| Tìm kiếm vector qua pipeline RAG | Có | Có |
| Quản lý mô hình AI | Có | Có |
| Quản lý ứng dụng AI | Có | Có |
| Cơ sở tri thức AI | Có | Có |
| Định hướng sản phẩm | Một nền tảng phát triển ứng dụng LLM độc lập | Nền tảng kết hợp cả low-code và ứng dụng AIGC |
| Tích hợp nghiệp vụ | Khả năng tích hợp nghiệp vụ yếu | Tích hợp với hệ thống nghiệp vụ thuận tiện hơn, gọi API và logic hệ thống dễ dàng hơn |
| Luồng nghiệp vụ AI | Chú trọng quy trình logic AI | Điều phối luồng AI đóng vai trò công cụ nghiệp vụ của low-code, người dùng có thể cấu hình nhiều luồng nghiệp vụ và luồng AI khác nhau qua luồng AI |
| Ngôn ngữ triển khai | python + react | JAVA + vue3 |
| Tải lên thư viện tài liệu Markdown (hỗ trợ hình ảnh) | Không hỗ trợ | Hỗ trợ |
| Trò chuyện AI hỗ trợ gửi và hiển thị hình ảnh | Hỗ trợ | Hỗ trợ |



### Tài liệu kỹ thuật

- [Tài liệu phát triển AIGC](https://help.jeecg.com/aigc)
- [Cài đặt thư viện vector pgvector](https://help.jeecg.com/aigc/config)



## Đặc điểm tính năng

- Luồng AI: Cung cấp công cụ thiết kế luồng AI mạnh mẽ, hỗ trợ điều phối quá trình làm việc của AI, đáp ứng các kịch bản nghiệp vụ phức tạp, hỗ trợ xây dựng trên canvas và xem tình trạng chạy của luồng AI theo thời gian thực.
- Luồng AI dưới dạng dịch vụ: Thông qua điều phối luồng AI, bạn có thể tạo tác tử thông minh mình cần, kết hợp AI với các node phát triển tùy chỉnh để tạo API chức năng, giúp bạn ngay lập tức sở hữu nhiều loại API tác tử thông minh.
- Tính năng trò chuyện với trợ lý AI: Tích hợp các mô hình AI như ChatGPT, DeepSeek, Zhipu AI, mô hình lớn riêng tư, cung cấp trò chuyện thông minh và tính năng AI tạo sinh, kết hợp sâu với cơ sở tri thức để cung cấp tri thức chính xác hơn.
- Tính năng RAG: Bao phủ mọi nội dung từ nạp tài liệu đến truy xuất, hỗ trợ trích xuất văn bản từ PDF, PPT và các định dạng tài liệu phổ biến khác, hỗ trợ tạo sinh tăng cường bằng truy xuất (RAG), tích hợp dữ liệu chưa được huấn luyện với mô hình AI để nâng cao khả năng tương tác thông minh.
- Cơ sở tri thức AI: Huấn luyện bằng cách nhập tài liệu hoặc các cặp hỏi – đáp có sẵn, để mô hình AI có thể trả lời câu hỏi theo cách trò chuyện tương tác dựa trên tài liệu.
- Quản lý mô hình: Hỗ trợ kết nối nhiều mô hình lớn, bao gồm mô hình lớn riêng tư chạy cục bộ (DeepSeek / Llama 3 / Qwen 2, v.v.), mô hình lớn công cộng trong nước (Tongyi Qianwen / Tencent Hunyuan / ByteDance Doubao / Baidu Qianfan / Zhipu AI / Kimi, v.v.) và mô hình lớn công cộng nước ngoài (OpenAI / Claude / Gemini, v.v.);
- Nhúng liền mạch: Nhúng một chạm bằng Iframe, hỗ trợ nhúng nhanh trợ lý trò chuyện AI vào hệ thống bên thứ ba, giúp hệ thống nhanh chóng có khả năng hỏi đáp thông minh và nâng cao sự hài lòng của người dùng.




#### Trải nghiệm trực tuyến

- Demo JeecgBoot:  https://boot3.jeecg.com
- Xây dựng cơ sở tri thức AI trực tuyến với Qiaoqiaoyun:https://app.qiaoqiaoyun.com


## Trao đổi kỹ thuật

- Tài liệu phát triển:https://help.jeecg.com/aigc 
- Nhóm QQ: 716913034, 964611995 (đầy), 716488839 (đầy)


## Danh sách tính năng

- Quản lý ứng dụng AI (ứng dụng thường, ứng dụng luồng nâng cao)
- Quản lý mô hình AI
- Cơ sở tri thức AI
- Nền tảng ứng dụng AI (thường, kết nối luồng AI)
- Điều phối luồng AI
- Trò chuyện AI hỗ trợ nhúng vào bên thứ ba
- Kết nối thư viện vector AI



## Các mô hình AI được hỗ trợ

| Mô hình lớn AI | Hỗ trợ |
|---------------| --- |
| DeepSeek      |  √   |
| ChatGTP       |  √   |
| Qwq           |  √   |
| Kho tri thức | √ |
| claude        |  √   |
| Mô hình VL | √ |
| Mô hình lớn Qianfan | √ |
| Tongyi Qianwen | √ |
| Mô hình lớn tự dựng cục bộ bằng Ollama | √ |
| v.v. | √ |




## AIGC có thể làm gì

Mô-đun AIGC là một công cụ điều phối luồng tự động dựa trên AI và nền tảng xây dựng ứng dụng trò chuyện, có thể giúp người dùng nhanh chóng tạo API luồng AI và ứng dụng trò chuyện, nâng cao hiệu quả.
Dưới đây là một số kịch bản ứng dụng và ví dụ cụ thể:

- Bạn có thể cần một API dịch thuật, có thể xây dựng bằng điều phối luồng AI.
- Bạn có thể cần một công cụ chuyển đổi API, có thể xây dựng bằng điều phối luồng AI. (Ví dụ: định dạng trả về của API mà JimuReport yêu cầu khác với hệ thống của bạn, bạn có thể dùng API AI để tự động chuyển đổi)
- Bạn có thể cần một chatbot, có thể xây dựng bằng điều phối luồng AI.
- Bạn có thể cần một quy trình tự động, có thể xây dựng bằng điều phối luồng AI.
- Bạn có thể cần một quy trình tự động xử lý tệp, có thể kết hợp luồng AI với script Python để thao tác trên máy tính, tệp tin, v.v.


## Trình diễn tính năng nền tảng ứng dụng AI

Danh sách mô hình AI

![](https://oscimg.oschina.net/oscnet//a5fb3e0d69ca1706b0de221535c7acaa.png)

Chọn mô hình AI và cấu hình tham số của bạn

![](https://oscimg.oschina.net/oscnet//1f941472758a5fc227f54f2683953b8e.png)


Cơ sở tri thức AI hỗ trợ nhập văn bản thủ công, nhập tài liệu PDF/Word/Excel, v.v., hỗ trợ huấn luyện bằng cặp hỏi – đáp

![](https://oscimg.oschina.net/oscnet//150bb33f48d6c8e2ae059e2a58f4200b.png)

![](https://oscimg.oschina.net/oscnet//032d16c915b0f79318935484c81df260.png)



Luồng AI cung cấp công cụ thiết kế luồng AI mạnh mẽ, hỗ trợ điều phối quá trình làm việc của AI, đáp ứng các kịch bản nghiệp vụ phức tạp, hỗ trợ xây dựng trên canvas và xem tình trạng chạy của luồng AI theo thời gian thực.

![](https://oscimg.oschina.net/oscnet//f40f9aa275cd4aea94e1c209513151e2.png)


Các node hiện được hỗ trợ gồm: Bắt đầu, Kết thúc, Node cơ sở tri thức AI, Node AI, Node phân loại, Node rẽ nhánh, Node JAVA, Node script, Node quy trình con, Node yêu cầu HTTP, Node trả lời trực tiếp và nhiều node khác

![](https://oscimg.oschina.net/oscnet//6d86480ab1bbfab5b2e6992b416b2152.png)

Cấu hình hạng mục node

![](https://oscimg.oschina.net/oscnet//90a5f76b6b4fc406e2e2b87245b35459.png)

Chạy trực tuyến để xem kết quả

![](https://oscimg.oschina.net/oscnet//bc9817a7bbd94936a5a3e885abe3cb38.png)


Cấu hình ứng dụng AI, hỗ trợ cấu hình luồng AI và cấu hình AI đơn giản

![](https://oscimg.oschina.net/oscnet//a853d9be4d3756806799ad025e722df8.png)![](https://oscimg.oschina.net/oscnet//d3bcbf5977c6fb75a8f996e1e40590be.png)

Có thể liên kết nhiều cơ sở tri thức; phía bên phải là phần trả lời thông minh của AI, bạn có thể xây dựng tác tử thông minh của riêng mình, ví dụ như một "bậc thầy thơ ca" hay "trợ lý dịch thuật"

![](https://oscimg.oschina.net/oscnet//c26a848136be3e22ec1e0651e78976c2.png)

Có thể tích hợp ứng dụng trò chuyện đã tạo vào hệ thống bên thứ ba

![](https://oscimg.oschina.net/oscnet//39c6f589ef46f0454b229915ffa263f4.png)