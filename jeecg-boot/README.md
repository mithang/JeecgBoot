JeecgBoot — Nền tảng phát triển low-code
===============

Phiên bản mới nhất hiện tại: 3.9.6 (2026-11-05) 


[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/zhangdaiscott/jeecg-boot/blob/master/LICENSE)
[![](https://img.shields.io/badge/Author-北京国炬软件-orange.svg)](http://jeecg.com/aboutusIndex)
[![](https://img.shields.io/badge/version-3.9.6-brightgreen.svg)](https://github.com/zhangdaiscott/jeecg-boot)
[![GitHub stars](https://img.shields.io/github/stars/zhangdaiscott/jeecg-boot.svg?style=social&label=Stars)](https://github.com/zhangdaiscott/jeecg-boot)
[![GitHub forks](https://img.shields.io/github/forks/zhangdaiscott/jeecg-boot.svg?style=social&label=Fork)](https://github.com/zhangdaiscott/jeecg-boot)



Giới thiệu dự án
-----------------------------------

<h3 align="center">Nền tảng low-code AI cấp doanh nghiệp</h3>

JeecgBoot là một nền tảng low-code AI dựa trên quy trình BPM và sinh mã, giúp doanh nghiệp nhanh chóng hiện thực hóa phát triển low-code và xây dựng ứng dụng AI.
Áp dụng kiến trúc tách biệt front-end/back-end (Ant Design & Vue3, SpringBoot4, SpringCloud Alibaba, Mybatis-plus), trình sinh mã mạnh mẽ giúp sinh front-end và back-end chỉ bằng một cú nhấp chuột, không cần viết mã bằng tay.
Nền tảng dẫn đầu mô hình phát triển low-code AI: AI tạo sinh → Viết mã trực tuyến → Sinh mã nguồn → Hợp nhất thủ công, giải quyết 80% công việc lặp lại trong dự án Java, nâng cao hiệu quả, tiết kiệm chi phí và vẫn đảm bảo tính linh hoạt.
Sở hữu khả năng kiểm soát quyền hạn mạnh mẽ và chi tiết, hỗ trợ cài đặt quyền hạn nút bấm và quyền hạn dữ liệu, đáp ứng nhu cầu của các hệ thống nghiệp vụ lớn. Các tính năng bao gồm biểu mẫu trực tuyến, thiết kế biểu mẫu, thiết kế quy trình, thiết kế cổng thông tin, thiết kế báo cáo và màn hình lớn, văn phòng OA, ứng dụng AI, cơ sở tri thức AI, quản lý mô hình lớn, điều phối luồng AI, trò chuyện AI, hỗ trợ nhiều mô hình lớn AI như ChatGPT, DeepSeek, Ollama.

`Báo cáo được AI tăng cường:` Báo cáo Jimu là một công cụ báo cáo Web và màn hình lớn cấp doanh nghiệp mã nguồn mở mạnh mẽ, do chính đội ngũ tự nghiên cứu phát triển. Thông qua thao tác kéo-thả zero-code, công cụ giúp người dùng dễ dàng xây dựng nhiều loại báo cáo phức tạp và màn hình lớn dữ liệu như xếp hình khối, đáp ứng toàn diện nhu cầu trực quan hóa và phân tích dữ liệu của doanh nghiệp, hỗ trợ tạo ra và ứng dụng hiệu quả các sản phẩm dữ liệu cấp doanh nghiệp.

`Low-code được AI tăng cường:` Cung cấp nền tảng ứng dụng AI hoàn chỉnh và trưởng thành, bao gồm quản lý ứng dụng AI, quản lý mô hình AI, trợ lý trò chuyện thông minh, hỏi đáp trên cơ sở tri thức, điều phối luồng và trình thiết kế, tạo bảng bằng AI cùng nhiều tính năng khác. Nền tảng tương thích với nhiều mô hình lớn phổ biến, bao gồm ChatGPT, DeepSeek, Ollama, Zhipu AI, Qwen, v.v., giúp doanh nghiệp xây dựng hiệu quả các ứng dụng thông minh và thúc đẩy sự kết hợp sâu sắc giữa phát triển low-code và AI.

`Phát triển do AI dẫn dắt:` Ra mắt năng lực phát triển do AI dẫn dắt hoàn toàn mới, hỗ trợ tạo hệ thống hoàn chỉnh chỉ bằng một câu nói, cung cấp hai lựa chọn: chế độ zero-code (xây dựng hệ thống bằng một câu nói, không cần viết mã) và chế độ sinh mã (dựa trên jeecg-codegen, tự động sinh mã hoàn chỉnh và SQL tạo bảng). Đồng thời hỗ trợ tự động vẽ sơ đồ quy trình và thiết kế biểu mẫu chỉ bằng một câu nói, loại bỏ các bước vẽ tay rườm rà.[Video demo trên Bilibili](https://www.bilibili.com/video/BV1KKwTzJEbX/) | [So sánh skill và hướng dẫn sử dụng](https://help.jeecg.com/java/ai/skills/skill-comparison/)

`Tôn chỉ của JEECG:` JEECG hướng tới việc hiện thực hóa xây dựng nhanh zero-code cho các tính năng đơn giản thông qua nền tảng OnlineCoding, đồng thời đối với các tính năng phức tạp thì dùng trình sinh mã để tạo mã rồi hợp nhất thủ công, tạo ra mô hình phát triển low-code thông minh và linh hoạt, giải quyết hiệu quả vấn đề thiếu linh hoạt phổ biến của các sản phẩm low-code hiện nay, nâng cao hiệu quả phát triển đồng thời đảm bảo khả năng mở rộng và tùy chỉnh của hệ thống.

`Quy trình nghiệp vụ JEECG:` Quy trình nghiệp vụ JEECG dùng công cụ luồng công việc BPM để thực hiện phê duyệt nghiệp vụ, mở rộng API tác vụ để nhà phát triển viết logic nghiệp vụ; biểu mẫu cung cấp nhiều giải pháp như trình thiết kế biểu mẫu, biểu mẫu cấu hình trực tuyến và biểu mẫu viết bằng mã. Thông qua thiết kế tách biệt quy trình và biểu mẫu (liên kết lỏng) cùng cấu hình linh hoạt cho các node tác vụ, vừa đảm bảo tính an toàn và bảo mật của quy trình doanh nghiệp, vừa giảm đáng kể khối lượng công việc của nhà phát triển.

Năng lực AI nổi bật
-----------------------------------

JeecgBoot ra mắt năng lực phát triển do AI dẫn dắt hoàn toàn mới, **hỗ trợ tạo hệ thống hoàn chỉnh chỉ bằng một câu nói**, đơn giản hóa đáng kể quy trình phát triển.

**Hai chế độ, lựa chọn linh hoạt:**

| Chế độ | Mô tả |
|------|------|
| Chế độ zero-code | Chỉ một câu nói là xây dựng được hệ thống, không cần viết bất kỳ mã nào |
| Chế độ sinh mã | Dựa trên jeecg-codegen, tự động sinh mã hoàn chỉnh và SQL tạo bảng |

**Thêm nhiều năng lực AI:**
- Hỗ trợ tự động vẽ sơ đồ quy trình chỉ bằng một câu nói, loại bỏ các bước vẽ tay rườm rà
- Hỗ trợ tự động thiết kế biểu mẫu chỉ bằng một câu nói, nhanh chóng hoàn thành việc xây dựng biểu mẫu

**Video demo:** [Xem video trên Bilibili](https://www.bilibili.com/video/BV1KKwTzJEbX/)

**Tài liệu chính thức:** [So sánh skill và hướng dẫn sử dụng](https://help.jeecg.com/java/ai/skills/skill-comparison/)


Danh sách Skill AI Skills
-----------------------------------

Kết hợp với kỹ năng AI Skills của Claude Code, JeecgBoot đã hiện thực hóa **phát triển low-code được dẫn dắt bằng ngôn ngữ tự nhiên**, chỉ một câu nói là hoàn thành toàn bộ quy trình tự động hóa từ nhu cầu đến mã nguồn/cấu hình.[Tài liệu chi tiết](https://help.jeecg.com/java/ai/skills/skill-comparison/) | [Tải Skills](https://github.com/jeecgboot/skills)

| Skill | Kỹ năng | Giới thiệu tính năng | Có cần viết mã không |
|-------|------|----------|---------------|
| `jeecg-codegen` | AI tạo toàn bộ mã nguồn bằng một câu nói | Nhu cầu bằng ngôn ngữ tự nhiên được tự động chuyển thành toàn bộ mã CRUD của JeecgBoot, gồm mã Java back-end + mã Vue3 front-end + SQL tạo bảng + SQL quyền hạn menu, hỗ trợ sinh ngay cả khi chưa có bảng, thậm chí sinh toàn bộ mã của một hệ thống | Không (AI tự sinh mã nguồn) |
| `jeecg-onlform` | AI tạo biểu mẫu Online bằng một câu nói | Nhu cầu bằng ngôn ngữ tự nhiên được tự động chuyển thành biểu mẫu Online của JeecgBoot, hoàn thành toàn bộ quy trình tự động từ cấu hình biểu mẫu → đồng bộ cơ sở dữ liệu → sinh SQL menu | Không |
| `jeecg-onlreport` | AI tạo báo cáo Online bằng một câu nói | Chuyển nhu cầu bằng ngôn ngữ tự nhiên thành báo cáo Online của JeecgBoot, hoàn thành toàn bộ quy trình tự động từ viết SQL → phân tích trường → cấu hình báo cáo → tạo báo cáo | Không |
| `jeecg-desform` | AI vẽ biểu mẫu bằng một câu nói | Mô tả nhu cầu biểu mẫu bằng ngôn ngữ tự nhiên được tự động chuyển thành biểu mẫu thiết kế của JeecgBoot, hoàn thành toàn bộ quy trình tự động từ phân tích nhu cầu → sinh JSON → tạo API, thậm chí có thể tạo cả một hệ thống | Không |
| `jeecg-bpmn` | AI vẽ quy trình bằng một câu nói | Chuyển mô tả quy trình phê duyệt bằng ngôn ngữ tự nhiên thành Flowable BPMN 2.0 XML, và tự động tạo quy trình trong hệ thống JeecgBoot qua API | Không |



Dự án phù hợp
-----------------------------------
Nền tảng low-code JeecgBoot tương thích với mọi dự án phát triển J2EE, hỗ trợ nội địa hóa theo chuẩn Xinchuang, đặc biệt phù hợp với các kịch bản như SAAS, hệ thống quản lý thông tin doanh nghiệp (MIS), hệ thống văn phòng nội bộ (OA), hệ thống hoạch định nguồn lực doanh nghiệp (ERP), hệ thống quản lý quan hệ khách hàng (CRM) và cơ sở tri thức AI. Chế độ phát triển Merge thủ công bán thông minh của nó có thể nâng cao hiệu quả phát triển trên 70% và giảm đáng kể chi phí phát triển. Đồng thời, JeecgBoot còn là một nền tảng phát triển AI full-stack, giúp doanh nghiệp nhanh chóng xây dựng và triển khai các ứng dụng AI cá nhân hóa.


**Khả năng tương thích Xinchuang**
- Hệ điều hành: Các hệ thống nội địa như Kylin, Galaxy Kylin hầu như đều dựa trên nhân Linux nên có khả năng tương thích tốt.
- Cơ sở dữ liệu: Dameng, Kingbase, TiDB
- Phần mềm trung gian: TongTech TongWeb, TongRDS, BES AppServer, CacheDB, [Tài liệu cấu hình Xinchuang](https://help.jeecg.com/java/tongweb-deploy/)




#### Giới thiệu dự án

| Tên dự án | Mô tả | 
|------------------|--------------------------------------------------------------------|
| `jeecg-boot` | Mã nguồn back-end JAVA (kiến trúc microservice SpringBoot4) |
| `jeecgboot-vue3` | Mã nguồn front-end VUE3 (ngăn xếp công nghệ mới nhất: vue3 + vite8 + antd4 + ts) |



Khởi động dự án
-----------------------------------

> Tài khoản và mật khẩu mặc định: admin/123456

- [Thiết lập môi trường phát triển](https://help.jeecg.com/java/setup/tools)
- [Khởi động front-end và back-end bằng IDEA (chế độ đơn thể)](https://help.jeecg.com/java/setup/idea/startup)
- [Khởi động một chạm bằng Docker (chế độ đơn thể)](https://help.jeecg.com/java/docker/quick)
- [Khởi động front-end và back-end bằng IDEA (chế độ microservice)](https://help.jeecg.com/java/springcloud/switchcloud/monomer)
- [Khởi động một chạm bằng Docker (chế độ microservice)](https://help.jeecg.com/java/docker/quickcloud)


Tài liệu kỹ thuật
-----------------------------------

- Trang web chính thức:   [http://www.jeecg.com](http://www.jeecg.com)
- Demo trực tuyến: [Demo nền tảng](https://boot3.jeecg.com) | [Demo APP](https://jeecg.com/appIndex)
- Hướng dẫn nhập môn: [Bắt đầu nhanh](http://www.jeecg.com/doc/quickstart)  | [Sử dụng sinh mã](https://help.jeecg.com/java/codegen/online) | [Tài liệu phát triển](https://help.jeecg.com)  | [Sổ tay ứng dụng AI](https://help.jeecg.com/aigc) | [Video hướng dẫn](http://jeecg.com/doc/video)
- Hỗ trợ kỹ thuật: [Gửi phản hồi vấn đề](https://github.com/jeecgboot/JeecgBoot/issues/new?template=bug_report.md)    | [Trải nghiệm low-code trong một phút](https://jeecg.blog.csdn.net/article/details/106079007)
- Nhóm trao đổi QQ: 716913034, 964611995 (đầy), ⑩716488839 (đầy), ⑨808791225 (đầy), các nhóm khác (đầy)


Giới thiệu nền tảng ứng dụng AI
-----------------------------------

Một nền tảng phát triển AI full-stack, nhằm giúp nhà phát triển nhanh chóng xây dựng và triển khai các ứng dụng AI cá nhân hóa.

Nền tảng JeecgBoot cung cấp một bộ mô-đun quản lý hệ thống ứng dụng AI hoàn chỉnh, tương tự như `Dify` — một `nền tảng phát triển ứng dụng AIGC` + `hỏi đáp trên cơ sở tri thức`, là một nền tảng ứng dụng AI dựa trên mô hình ngôn ngữ lớn LLM và hệ thống hỏi đáp cơ sở tri thức RAG.
Giao diện trực quan của nó kết hợp điều phối luồng AI, pipeline RAG, quản lý cơ sở tri thức, quản lý mô hình, kết nối thư viện vector, khả năng quan sát khi chạy thời gian thực, v.v., giúp bạn nhanh chóng đi từ nguyên mẫu đến sản xuất và sở hữu năng lực dịch vụ AI.

- [Giới thiệu chuyên đề chi tiết, vui lòng nhấp để xem](README-AI.md)

- Video giới thiệu AI

[![](https://jeecgos.oss-cn-beijing.aliyuncs.com/files/jeecg_aivideo.png)](https://www.bilibili.com/video/BV1zmd7YFE4w)


Tại sao nên chọn JeecgBoot?
-----------------------------------
- 1. Áp dụng khung tách biệt front-end/back-end mới nhất, phổ biến (Spring Boot 4 + MyBatis + Shiro/SpringAuthorizationServer + Ant Design4 + Vue3), dễ tiếp cận; trình sinh mã ít phụ thuộc, khả năng mở rộng linh hoạt, có thể nhanh chóng thực hiện phát triển lần hai.
- 2. Front-end đã thay đổi phiên bản lớn, phiên bản mới nhất áp dụng các phương án công nghệ mới như Vue3.0 + TypeScript + Vite8 + Ant Design Vue4.
- 3. Hỗ trợ microservice Spring Cloud Alibaba (Nacos, Gateway, Sentinel, Skywalking), cung cấp cơ chế đơn giản, hỗ trợ chuyển đổi tự do giữa đơn thể và microservice (nhờ đó đáp ứng được nhu cầu của nhiều loại dự án).
- 4. Hiệu quả phát triển cao, hỗ trợ tạo bảng trực tuyến và tạo bảng bằng AI, cung cấp trình sinh mã mạnh mẽ; các mô hình dữ liệu như bảng đơn, danh sách cây, một-nhiều, một-một; chức năng thêm/sửa/xóa/tìm kiếm được sinh chỉ bằng một cú nhấp chuột, cấu hình menu dùng được ngay.
- 5. Trình sinh mã cung cấp cơ chế mẫu mạnh mẽ, hỗ trợ mẫu tùy chỉnh, hiện cung cấp bốn bộ mẫu theo phong cách (hai bộ cho bảng đơn, một bộ cho mô hình cây, ba bộ cho một-nhiều).
- 6. Cung cấp công cụ trực quan hóa báo cáo và màn hình lớn mạnh mẽ, hỗ trợ kết nối nhiều nguồn dữ liệu phong phú, có thể nhanh chóng tạo báo cáo, màn hình lớn và thiết kế cổng thông tin bằng cách kéo-thả; hỗ trợ nhiều loại biểu đồ: biểu đồ cột, biểu đồ đường, biểu đồ phân tán, biểu đồ tròn, biểu đồ vành khăn, biểu đồ diện tích, biểu đồ phễu, biểu đồ tiến độ, bảng điều khiển, biểu đồ radar, bản đồ, v.v.
- 7. Năng lực low-code: Biểu mẫu trực tuyến (không cần viết mã, thông qua cấu hình biểu mẫu trực tuyến để thực hiện thêm/sửa/xóa/tìm kiếm biểu mẫu, hỗ trợ các mô hình bảng đơn, cây, một-nhiều, một-một, giúp ai cũng có thể viết mã), phát triển zero-code bằng cấu hình trực tuyến, what-you-see-is-what-you-get với 23 loại control.
- 8. Năng lực low-code: Báo cáo trực tuyến, biểu đồ trực tuyến (không cần viết mã, thông qua cấu hình trực tuyến để tạo báo cáo dữ liệu và báo cáo đồ họa, có thể nhanh chóng trích xuất dữ liệu, giảm áp lực phát triển, giúp ai cũng có thể viết mã).
- 9. Online hỗ trợ phát triển tăng cường trực tuyến, cung cấp trình soạn thảo mã trực tuyến, hỗ trợ tô sáng mã, gợi ý mã và nhiều tính năng khác, hỗ trợ nhiều ngôn ngữ (Java, SQL, JavaScript, v.v.).
- 10. Đóng gói hoàn chỉnh các tính năng cơ sở như người dùng, vai trò, menu, tổ chức, danh mục dữ liệu, tác vụ định kỳ trực tuyến, hỗ trợ cấp quyền truy cập, quyền hạn nút bấm, quyền hạn dữ liệu và nhiều tính năng khác.
- 11. UI front-end cung cấp thư viện thành phần phong phú, hỗ trợ nhiều thành phần thường dùng như bảng, control cây, hộp thả xuống, bộ chọn ngày, v.v., đáp ứng nhiều nhu cầu nghiệp vụ phức tạp [Tài liệu thư viện thành phần UI](https://help.jeecg.com/category/ui%E7%BB%84%E4%BB%B6%E5%BA%93).
- 12. Cung cấp khung APP đi kèm, một bộ mã nguồn tương thích đa thiết bị đầu cuối: mini-program, H5, Android, iOS, HarmonyOS Next.
- 13. Khung APP phiên bản mới áp dụng ngăn xếp công nghệ mới nhất như Uniapp, Vue3.0, Vite, Wot-design-uni, TypeScript, bao gồm các tính năng như thành phần đóng gói lần hai, chặn route, chặn yêu cầu. Đã tích hợp hoàn hảo với JeecgBoot: hiện đã triển khai các tính năng đăng nhập, thông tin người dùng, danh bạ, thông báo, trang chủ di động, lưới chín ô, trò chuyện, biểu mẫu Online, bảng điều khiển, v.v., cung cấp nhiều thành phần phong phú.
- 14. Cung cấp một bộ tính năng nền tảng ứng dụng AI hoàn thiện, từ mô hình AI, cơ sở tri thức đến xây dựng ứng dụng AI, giúp doanh nghiệp nhanh chóng triển khai dịch vụ AI và tăng tốc nâng cấp thông minh.
- 15. Năng lực AI: Hiện JeecgBoot hỗ trợ các mô hình lớn AI như ChatGPT và DeepSeek; phiên bản mới nhất hiện mặc định dùng DeepSeek, nhanh hơn và chất lượng cao hơn. Hiện đã cung cấp các tính năng như trợ lý trò chuyện AI, cơ sở tri thức AI, ứng dụng AI, tạo bảng bằng AI, báo cáo AI, v.v.
- 16. Cung cấp bảng chỉnh sửa dòng mới JVXETable, dễ dàng đáp ứng nhiều bố cục ERP phức tạp, với hiệu năng cao hơn, khả năng mở rộng linh hoạt hơn và tính năng mạnh mẽ hơn.
- 17. Phong cách trang chủ của nền tảng cung cấp nhiều chế độ kết hợp, hỗ trợ tùy chỉnh phong cách; hỗ trợ thiết kế cổng thông tin và tùy chỉnh trang chủ.
- 18. Đóng gói dùng chung thường dùng, nhiều lớp công cụ (tác vụ định kỳ, giao diện SMS, gửi email, nhập/xuất Excel, v.v.), cơ bản đáp ứng 80% nhu cầu của dự án.
- 19. Nhập/xuất Excel đơn giản, hỗ trợ xuất bảng đơn và xuất theo chế độ bảng một-nhiều; mã nguồn được sinh ra đã kèm sẵn tính năng nhập/xuất.
- 20. Tích hợp công cụ báo cáo thông minh, in báo cáo, báo cáo hình ảnh và xuất dữ liệu rất thuận tiện, có thể tạo báo cáo PDF, Excel, Word, v.v. một cách cực kỳ dễ dàng.
- 21. Áp dụng công nghệ tách biệt front-end/back-end, phong cách UI trang tinh tế, đóng gói sẵn các thành phần thường dùng: thời gian, control bảng dòng, control hiển thị cắt ngắn, thành phần báo cáo, trình soạn thảo, v.v.
- 22. Bộ lọc truy vấn: Chức năng truy vấn được tự động sinh, back-end tự động ghép SQL để thêm điều kiện truy vấn; hỗ trợ nhiều chế độ khớp (khớp toàn bộ / truy vấn mờ / truy vấn chứa / truy vấn không khớp).
- 23. Quyền hạn dữ liệu (kiểm soát quyền hạn dữ liệu tinh vi, kiểm soát đến cấp hàng, cấp danh sách, cấp trường biểu mẫu, để mỗi người thấy dữ liệu khác nhau và thao tác trên các trường khác nhau trên cùng một trang).
- 24. Cơ chế bảo mật API, có thể kiểm soát chi tiết việc cấp quyền cho API, rất đơn giản để thực hiện việc mỗi client chỉ thấy dữ liệu của mình; đồng thời cung cấp tính năng OpenAPI xác thực dựa trên AK và SK.
- 25. Cộng đồng hỗ trợ sôi động; trong những năm gần đây, cùng với sự gia tăng của các mối đe dọa mạng, nhóm đã tích lũy nhiều kinh nghiệm về bảo mật và quản lý lỗ hổng, có thể cung cấp cho doanh nghiệp giải pháp bảo mật toàn diện.
- 26. Kiểm soát quyền hạn áp dụng RBAC (Role-Based Access Control — kiểm soát truy cập dựa trên vai trò).
- 27. Kiểm tra (validation) trên trang được tự động sinh (bắt buộc nhập, kiểm tra số, kiểm tra số tiền, kiểm tra thời gian và không gian, v.v.).
- 28. Hỗ trợ chế độ dịch vụ SaaS, cung cấp phương án kiến trúc đa thuê bao SaaS.
- 29. Dịch vụ tệp phân tán, tích hợp các bên thứ ba xuất sắc như MinIO, Alibaba OSS, cung cấp khả năng tải lên và quản lý tệp thuận tiện, đồng thời hỗ trợ cả lưu trữ cục bộ.
- 30. Tương thích với các cơ sở dữ liệu phổ biến, một bộ mã nguồn tương thích hoàn toàn với MySQL, PostgreSQL, Oracle, SQL Server, MariaDB, Dameng, Kingbase và các cơ sở dữ liệu phổ biến khác.
- 31. Tích hợp luồng công việc Flowable, chỉ cần cấu hình hướng chuyển của quy trình trên trang là có thể đơn giản hóa đáng kể việc phát triển luồng công việc BPM; dùng trình thiết kế quy trình BPM để vẽ hướng đi của quy trình là một luồng công việc cơ bản đã hoàn thành, chỉ cần viết rất ít mã Java.
- 32. Năng lực low-code: Thiết kế quy trình trực tuyến, dùng công cụ quy trình mã nguồn mở Flowable để vẽ quy trình trực tuyến, tùy chỉnh biểu mẫu, gắn biểu mẫu vào quy trình và luân chuyển nghiệp vụ.
- 33. Đa nguồn dữ liệu: Cách sử dụng cực kỳ đơn giản, cấu hình nguồn dữ liệu trực tuyến để thuận tiện lấy dữ liệu từ các nguồn khác.
- 34. Cung cấp phương án tích hợp đăng nhập một lần (SSO) CAS, trong dự án đã có sẵn mã tích hợp hoàn chỉnh.
- 35. Năng lực low-code: Trình thiết kế biểu mẫu, hỗ trợ người dùng tùy chỉnh bố cục biểu mẫu, hỗ trợ biểu mẫu bảng đơn và một-nhiều, hỗ trợ các control như select, radio, checkbox, textarea, date, popup, danh sách, macro, v.v.
- 36. Cơ chế tích hợp API chuyên nghiệp, thống nhất dùng API RESTful, tích hợp tài liệu API trực tuyến Swagger-UI, xác thực bảo mật bằng JWT token, thuận tiện cho client tích hợp.
- 37. Tính năng truy vấn kết hợp nâng cao, cấu hình trực tuyến hỗ trợ truy vấn liên kết bảng chính – bảng con, có thể lưu lịch sử truy vấn.
- 38. Cung cấp nhiều loại giám sát hệ thống, theo dõi tình trạng vận hành của hệ thống theo thời gian thực (giám sát Redis, Tomcat, JVM, thông tin máy chủ, truy vết yêu cầu, giám sát SQL).
- 39. Trung tâm tin nhắn (hỗ trợ SMS, email, đẩy WeChat, v.v.); tích hợp cơ chế thông báo tin nhắn WebSocket.
- 40. Hỗ trợ đa ngôn ngữ, cung cấp giải pháp quốc tế hóa.
- 41. Nhật ký ghi lại thay đổi dữ liệu, có thể ghi lại nội dung mỗi lần dữ liệu thay đổi, xem các thay đổi lịch sử qua tính năng so sánh phiên bản.
- 42. Cung cấp plugin in ấn đơn giản, dễ dùng, hỗ trợ nhiều trình duyệt như Google Chrome, Firefox, IE11+.
- 43. Back-end phát triển theo từng mô-đun bằng Maven; front-end hỗ trợ route động theo menu.
- 44. Cung cấp mã ví dụ phong phú, bao phủ các kịch bản nghiệp vụ thường dùng, thuận tiện cho việc học tập và tham khảo.


Kiến trúc kỹ thuật:
-----------------------------------

#### Back-end

- IDE khuyến nghị: IDEA (bắt buộc cài plugin lombok)
- Ngôn ngữ: Java, mặc định jdk17 (jdk21, jdk24)
- Quản lý dependency: Maven
- Khung cơ sở: Spring Boot 4.1.0
- Khung microservice: Spring Cloud Alibaba 2025.1.0.0
- Khung tầng persistence: MybatisPlus 3.5.16
- Công cụ báo cáo: JimuReport 2.5.1
- Thiết kế màn hình lớn: JimuBI v2.5.0
- Khung bảo mật: Apache Shiro 3.0.0, Jwt 4.5.0
- Ngăn xếp công nghệ microservice: Spring Cloud Alibaba, Nacos, Gateway, Sentinel, Skywalking
- Pool kết nối cơ sở dữ liệu: Alibaba Druid 1.2.28
- Mô hình lớn AI: Hỗ trợ `ChatGPT`, `DeepSeek`, `Qwen` và nhiều chế độ thông dụng khác
- Ghi nhật ký: logback
- Bộ nhớ đệm: Redis
- Khác: autopoi, fastjson, poi, Swagger-ui, quartz, lombok (đơn giản hóa mã nguồn), v.v.
- Mặc định cung cấp script cơ sở dữ liệu MySQL5.7+
- [Cơ sở dữ liệu khác, cần tự chuyển đổi](https://my.oschina.net/jeecg/blog/4905722)


#### Front-end

- Yêu cầu môi trường front-end: Node.js yêu cầu phiên bản `Node 20+` trở lên, pnpm yêu cầu phiên bản `9+` trở lên
  ` (Vite không còn hỗ trợ Node.js 18 đã kết thúc vòng đời (EOL). Hiện cần dùng Node.js 20.19+ hoặc 22.12+)`

- Quản lý dependency: node, npm, pnpm
- IDE front-end khuyến nghị: IDEA, WebStorm, Vscode
- Áp dụng các phương án công nghệ mới như Vue3.0 + TypeScript + Vite8 + Ant-Design-Vue4, bao gồm thành phần đóng gói lần hai, utils, hooks, menu động, kiểm tra quyền hạn, kiểm soát quyền hạn cấp nút bấm và nhiều tính năng khác
- Ngăn xếp công nghệ mới nhất: Vue3.0 + TypeScript + Vite8 + ant-design-vue4 + pinia + echarts + unocss + vxe-table + qiankun + es6




#### Thư viện được hỗ trợ

| Cơ sở dữ liệu | Hỗ trợ |
| --- | --- |
|   MySQL   |  √   |
|  Oracle11g   |  √   |
|  Sqlserver2017   |  √   |
|   PostgreSQL   |  √   |
|   MariaDB   |  √   |
|   Dameng   |  √   |
|   Kingbase   |  √   |
|   TiDB   |  √   |



 
## Giải pháp microservice


- 1. Đăng ký và phát hiện dịch vụ Nacos √
- 2. Trung tâm cấu hình thống nhất Nacos √
- 3. Cổng định tuyến gateway (ba cách tải) √
- 4. HTTP phân tán Feign √
- 5. Ngắt mạch, hạ cấp và giới hạn lưu lượng Sentinel √
- 6. Tệp phân tán MinIO, Alibaba OSS √ 
- 7. Kiểm soát quyền hạn thống nhất JWT + Shiro √
- 8. Giám sát dịch vụ SpringBootAdmin √
- 9. Truy vết liên kết Skywalking [Tài liệu tham khảo](https://help.jeecg.com/java/springcloud/super/skywarking)
- 10. Phần mềm trung gian tin nhắn RabbitMQ √
- 11. Tác vụ phân tán xxl-job √ 
- 12. Giao dịch phân tán Seata
- 13. Bộ nhật ký phân tán nhẹ Loki + Grafana
- 14. Hỗ trợ docker-compose, k8s, jenkins
- 15. Đăng nhập một lần (SSO) CAS √
- 16. Giới hạn lưu lượng định tuyến √



Cấu trúc thư mục back-end
-----------------------------------
```
Cấu trúc dự án
├─jeecg-boot-parent (POM cha: dependency của dự án, tổ chức modules)
│  ├─jeecg-boot-base-core (mô-đun dùng chung: lớp công cụ, config, quyền hạn, bộ lọc truy vấn, annotation, v.v.)
│  ├─jeecg-module-demo    Mã ví dụ
│  ├─jeecg-module-system  Thư mục quản lý hệ thống System
│  │  ├─jeecg-system-biz    Các tính năng như quyền hạn quản lý hệ thống System
│  │  ├─jeecg-system-start  Dự án khởi động đơn thể System (8080)
│  │  ├─jeecg-system-api    API đối ngoại của mô-đun quản lý hệ thống System
│  │  │  ├─jeecg-system-cloud-api   API microservice do mô-đun System cung cấp ra bên ngoài
│  │  │  ├─jeecg-system-local-api   API đơn thể do mô-đun System cung cấp ra bên ngoài
│  ├─jeecg-server-cloud           --Mô-đun microservice
     ├─jeecg-cloud-gateway       --Mô-đun cổng microservice (9999)
     ├─jeecg-cloud-nacos       --Mô-đun dịch vụ Nacos (dịch vụ: 8848, bảng điều khiển: 18080)
     ├─jeecg-system-cloud-start  --Dự án khởi động microservice System (7001)
     ├─jeecg-demo-cloud-start    --Dự án khởi động microservice Demo (7002)
     ├─jeecg-visual
        ├─jeecg-cloud-monitor       --Mô-đun giám sát microservice (9111)
        ├─jeecg-cloud-xxljob        --Máy chủ tác vụ định kỳ xxljob cho microservice (9080)
        ├─jeecg-cloud-sentinel     --Máy chủ Sentinel (9000)
        ├─jeecg-cloud-test           -- Ví dụ kiểm thử microservice (nhiều ví dụ khác nhau)
           ├─jeecg-cloud-test-more         -- Ví dụ kiểm thử microservice (Feign, ngắt mạch và hạ cấp, xxljob, khóa phân tán)
           ├─jeecg-cloud-test-rabbitmq     -- Ví dụ kiểm thử microservice (RabbitMQ)
           ├─jeecg-cloud-test-seata          -- Ví dụ kiểm thử microservice (giao dịch phân tán Seata)
           ├─jeecg-cloud-test-shardingsphere    -- Ví dụ kiểm thử microservice (chia cơ sở dữ liệu – chia bảng)
```




#### Sơ đồ kiến trúc microservice
![Sơ đồ kiến trúc microservice](https://jeecgos.oss-cn-beijing.aliyuncs.com/files/jeecgboot_springcloud2022.png "Nhập tiêu đề hình ảnh tại đây")





