JeecgBoot Nền tảng phát triển low-code cấp doanh nghiệp
===============
Phiên bản mới nhất hiện tại: 3.9.6 (2026-11-05)

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/zhangdaiscott/jeecg-boot/blob/master/LICENSE)
[![](https://img.shields.io/badge/Author-北京国炬软件-orange.svg)](http://jeecg.com/aboutusIndex)
[![](https://img.shields.io/badge/version-3.9.6-brightgreen.svg)](https://github.com/jeecgboot/JeecgBoot)
[![GitHub stars](https://img.shields.io/github/stars/zhangdaiscott/jeecg-boot.svg?style=social&label=Stars)](https://github.com/zhangdaiscott/jeecg-boot)
[![GitHub forks](https://img.shields.io/github/forks/zhangdaiscott/jeecg-boot.svg?style=social&label=Fork)](https://github.com/zhangdaiscott/jeecg-boot)



## Giới thiệu
JeecgBoot-Vue3 áp dụng các công nghệ mới như Vue 3.0, Vite, Ant-Design-Vue 4, TypeScript, bao gồm các component được đóng gói lại, utils, hooks, menu động, kiểm tra quyền hạn, kiểm soát quyền hạn ở cấp nút bấm và các tính năng khác.
 
> Trình tạo mã mạnh mẽ giúp tạo mã front-end và back-end chỉ bằng một cú nhấp chuột! JeecgBoot dẫn đầu mô hình phát triển low-code (OnlineCoding -> Tạo mã -> MERGE thủ công), giúp giải quyết 70% công việc lặp lại trong các dự án Java, để nhà phát triển tập trung hơn vào nghiệp vụ. Vừa nhanh chóng nâng cao hiệu suất, tiết kiệm chi phí, vừa không mất đi tính linh hoạt


## Thiết lập môi trường phát triển

- [Chuẩn bị môi trường phát triển front-end](https://help.jeecg.com/setup/dev)
- [Khởi động nhanh dự án front-end](https://help.jeecg.com/setup/startup)
- [Khởi động dự án bằng IDEA](https://help.jeecg.com/java/setup/idea/startup)

## Tài liệu kỹ thuật

-   Tài liệu chính thức:[https://help.jeecg.com](https://help.jeecg.com)
-   Hướng dẫn nhanh:[Hướng dẫn nhanh](http://jeecg.com/doc/quickstart) | [Câu hỏi thường gặp](http://help.jeecg.com/qa) 
-   Nhóm QQ: 716913034, 964611995 (đầy), ⑩716488839 (đầy), ⑨808791225 (đầy), các nhóm khác đã đầy
-   Demo trực tuyến:  [Demo hệ thống](http://boot3.jeecg.com)   | [Demo APP](http://jeecg.com/appIndex)
> Để lấy tài khoản và mật khẩu đăng nhập hệ thống demo, vui lòng nhấp vào [Lấy tài khoản và mật khẩu](http://jeecg.com/doc/demo) để lấy


## Cài đặt và sử dụng

*   Cài đặt `Node.js, npm, pnpm` trong môi trường local
*   Yêu cầu phiên bản Node.js `Node 20+` trở lên
*   Yêu cầu phiên bản pnpm `10+` trở lên

 ` ( Vì Vite5 không còn hỗ trợ các phiên bản Node.js 14 / 16 / 17 / 19 đã EOL, hiện cần Node 20+ )`


  
- Get the project code

```bash
git clone https://github.com/jeecgboot/JeecgBoot.git
```

- Installation dependencies

```bash
cd JeecgBoot/jeecgboot-vue3

pnpm install
```

- Cấu hình địa chỉ API `.env.development`

```bash
VITE_PROXY = [["/jeecgboot","http://localhost:8080/jeecg-boot"],["/upload","http://localhost:3300/upload"]]
VITE_GLOB_DOMAIN_URL=http://localhost:8080/jeecg-boot
```

> Lưu ý: Thay `http://localhost:8080/jeecg-boot` bằng địa chỉ của bạn, các phần khác không cần thay đổi.


- run

```bash
pnpm dev
```


- build

```bash
pnpm build
```

## Kiến thức cần thiết trước khi bắt đầu

Dự án này yêu cầu kiến thức cơ bản về front-end, vui lòng đảm bảo nắm vững kiến thức cơ bản về Vue để xử lý một số vấn đề thường gặp. Bạn nên tìm hiểu trước các nội dung sau trước khi phát triển, việc hiểu và học trước những kiến thức này sẽ rất hữu ích cho việc hiểu dự án:

*   [Tài liệu JeecgBoot](http://help.jeecg.com)
*   [Tài liệu Vue3](https://cn.vuejs.org/)
*   [Tài liệu Vben](https://doc.vvbin.cn)
*   [Ant-Design-Vue](https://www.antdv.com/docs/vue/introduce-cn/)
*   [TypeScript](https://www.typescriptlang.org/)
*   [Vue-router](https://router.vuejs.org/zh)
*   [Es6](https://es6.ruanyifeng.com/)
*   [Vitejs](https://cn.vitejs.dev/guide/)
*   [Pinia (giải pháp thay thế vuex)](https://pinia.esm.dev/introduction.html)
*   [Vue-RFCS](https://github.com/vuejs/rfcs)
*   [Tài liệu vxetable](https://vxetable.cn)


##   Hỗ trợ trình duyệt

Khi **phát triển local** nên sử dụng trình duyệt `Chrome phiên bản mới nhất`, **không hỗ trợ** các phiên bản dưới `Chrome 90`.

**Môi trường production** hỗ trợ các trình duyệt hiện đại, không hỗ trợ IE.

| [![IE](https://raw.githubusercontent.com/alrra/browser-logos/master/src/archive/internet-explorer_9-11/internet-explorer_9-11_48x48.png)](http://godban.github.io/browsers-support-badges/)IE | [![ Edge](https://raw.githubusercontent.com/alrra/browser-logos/master/src/edge/edge_48x48.png)](http://godban.github.io/browsers-support-badges/)Edge | [![Firefox](https://raw.githubusercontent.com/alrra/browser-logos/master/src/firefox/firefox_48x48.png)](http://godban.github.io/browsers-support-badges/)Firefox | [![Chrome](https://raw.githubusercontent.com/alrra/browser-logos/master/src/chrome/chrome_48x48.png)](http://godban.github.io/browsers-support-badges/)Chrome | [![Safari](https://raw.githubusercontent.com/alrra/browser-logos/master/src/safari/safari_48x48.png)](http://godban.github.io/browsers-support-badges/)Safari |
| --- | --- | --- | --- | --- |
| không hỗ trợ | last 2 versions | last 2 versions | last 2 versions | last 2 versions |
