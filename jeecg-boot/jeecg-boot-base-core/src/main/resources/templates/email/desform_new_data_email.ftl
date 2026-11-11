<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8">
</head>

<body>
<div  class="box-content">
    <div  class="info-top">
        <img src="https://qiaoqiaoyun.oss-cn-beijing.aliyuncs.com/site/qqyunemaillogo.png" style="width: 35px;height:35px; background: #5e8ee5; border-radius: 5px;" />
        <div style="color:#fff;">
            <strong>【Quan trọng】Nhắc nhở có dữ liệu mới</strong>
        </div>
    </div>
    <div class="info-wrap">
        <div class="tips" style="padding:15px;">
            <p style="margin: 10px 0;">
                Kính gửi  ${userName} Người dùng,Xin chào：
            </p>
            Biểu mẫu của bạn  <a style="color: #006eff;" href="${formLink}" target="_blank" rel="noopener">【${formName}】</a>
            Trong ${createTime} Đã thêm mới 1 bản ghi dữ liệu.

            ${dataMarkdown}

            <p>
                Nếu cần xem thêm, vui lòng nhấp vào
                <a style="color: #006eff;" href="${moreLink}" target="_blank" rel="noopener">[Xem tất cả dữ liệu]</a>
            </p>
        </div>
        <div class="footer">Nền tảng Qiaoqiao Cloud</div>
        <div class="footer" id="currentTime"></div>
    </div>
    <div style="width: 600px; margin: 0 auto;  margin-top: 50px; font-size: 12px; -webkit-font-smoothing: subpixel-antialiased; text-size-adjust: 100%;">
        <p style="text-align: center; line-height: 20.4px; text-size-adjust: 100%; font-family: 'Microsoft YaHei'!important; padding: 0px !important; margin: 0px !important; color: #7e8890 !important;">
            <span class="appleLinks">Copyright © 2023-2024 北京国炬信息技术有限公司. Bảo lưu mọi quyền.</span>
        </p>
        <p style="text-align: center;line-height: 20.4px; text-size-adjust: 100%; font-family: 'Microsoft YaHei'!important; padding: 0px !important; margin: 0px; color: #7e8890 !important; margin-top: 10px;">
            <span class="appleLinks">Email này do hệ thống tự động gửi, vui lòng không trả lời trực tiếp email này!</span>
        </p>
    </div>
</div>
</body>

<style>
    .box-content{
        width: 80%;
        margin: 20px auto;
        max-width: 800px;
        min-width: 600px;
    }

    .info-top{
        display: flex;
        align-items: center;
        padding: 15px 25px;
        border-top-left-radius: 10px;
        border-top-right-radius: 10px;
        background: #4ea3f2;
        color: #fff;
        overflow: hidden;
        line-height: 32px;
    }

    .info-wrap{
        border-bottom-left-radius: 10px;
        border-bottom-right-radius: 10px;
        border:1px solid #ddd;
        overflow: hidden;
        padding: 15px 15px 20px;
    }

    .footer{
        text-align: right;
        color: #999;
        padding: 0 15px 15px;
    }
</style>

</html>