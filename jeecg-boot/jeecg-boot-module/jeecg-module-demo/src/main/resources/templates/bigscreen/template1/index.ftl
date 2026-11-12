<#assign base=springMacroRequestContext.getContextUrl("")>
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>JEECG ROOM Phòng chỉ huy tác chiến</title>
    <link href="${base}/bigscreen/template1/css/easyui.css" rel="stylesheet" type="text/css">
    <link href="${base}/bigscreen/template1/css/room.css" rel="stylesheet" type="text/css" />


    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/jquery.min.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/jquery.easyui.min.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/echarts.min.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/echarts-wordcloud.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/china.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/geoCoord.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/room.js"></script>
    <script language="javascript" type="text/javascript" src="${base}/bigscreen/template1/js/resize.js"></script>
</head>

<body>
    <div id="main">
        <!-- Làm mới -->
        <div id="refresh">
            <span id="refreshTime">Thời gian làm mới lần cuối:2018-05-06 23:13.24</span>
        </div>
        <!-- Kết thúc làm mới -->

        <!-- Bắt đầu khu vực thanh tiến độ-->
        <div id="y_gauge1"></div>
        <div id="y_gauge2"></div>
        <div id="y_gauge3"></div>
        <div id="y_gauge4"></div>
        <!-- Kết thúc khu vực thanh tiến độ-->

        <!-- Bắt đầu biểu đồ xoắn ốc -->
        <div id="orderStatus"></div>
        <div class="contentButton" style="top:822px;left:453px">
            <a class="a1" href="javascript:void(0);" onclick="javascript:openDialog('modalDlg');">&nbsp;</a>
        </div>
        <!-- Kết thúc biểu đồ xoắn ốc -->

        <!-- Bắt đầu bản đồ -->
        <div id="map"></div>
        <!-- Kết thúc bản đồ -->

        <!-- Bắt đầu biểu đồ tròn sản phẩm -->
        <div id="productPie" style="width: 900px; height: 590px;"></div>
        <!-- Kết thúc biểu đồ tròn sản phẩm -->

        <!-- Bắt đầu biểu đồ tiến độ nghiệp vụ -->
        <div id="businessProgress"></div>
        <div class="contentButton" style="top:822px;left:3679px">
            <a class="a1" href="javascript:void(0);">&nbsp;</a>
        </div>
        <!-- Kết thúc biểu đồ tiến độ nghiệp vụ -->

        <!-- Bắt đầu hiển thị kế hoạch sản xuất-->
        <div id="plan"></div>
        <div class="contentButton" style="top:1402px;left:453px">
            <a href="javascript:void(0);">&nbsp;</a>
        </div>
        <!-- Kết thúc hiển thị kế hoạch sản xuất-->

        <!-- Bắt đầu hiển thị chất lượng sản xuất-->
        <div id="quality"></div>
        <div class="contentButton" style="top:1402px;left:1532px">
            <a href="javascript:void(0);">&nbsp;</a>
        </div>
        <!-- Kết thúc hiển thị chất lượng sản xuất-->

        <!-- Bắt đầu hiển thị chăm sóc khách hàng và khiếu nại-->
        <div id="produce">
            <table width="100%" cellpadding="6" cellspacing="0">
                <tr class="row1">
                    <td rowspan="2"><span id="currentDate">2018/04/25</span></td>
                    <td colspan="2">Khiếu nại sản phẩm</td>
                    <td colspan="2">Khiếu nại logistics</td>
                    <td colspan="2">Khiếu nại sau bán hàng</td>
                </tr>
                <tr class="row1">
                    <td>Chất lượng</td>
                    <td>Dịch vụ</td>
                    <td>Chất lượng</td>
                    <td>Dịch vụ</td>
                    <td>Chất lượng</td>
                    <td>Dịch vụ</td>
                </tr>
                <tr class="row2">
                    <td>Đã xử lý</td>
                    <td>48</td>
                    <td>48</td>
                    <td>48</td>
                    <td>48</td>
                    <td>48</td>
                    <td>48</td>
                </tr>
                <tr class="row1">
                    <td>Đang xử lý</td>
                    <td>34</td>
                    <td>34</td>
                    <td>34</td>
                    <td>34</td>
                    <td>34</td>
                    <td>34</td>
                </tr>
                <tr class="row2">
                    <td>Chưa xử lý</td>
                    <td>30</td>
                    <td>28</td>
                    <td>28</td>
                    <td>26</td>
                    <td>25</td>
                    <td>8</td>
                </tr>
                <tr class="row2">
                    <td>Tổng cộng</td>
                    <td>30</td>
                    <td>28</td>
                    <td>28</td>
                    <td>26</td>
                    <td>25</td>
                    <td>8</td>
                </tr>
                <tr class="row1">
                    <td>Tổng cộng</td>
                    <td colspan="2">22</td>
                    <td colspan="2">65</td>
                    <td colspan="2">44</td>
                </tr>
            </table>
        </div>
        <div class="contentButton" style="top:1402px;left:2598px">
            <a href="javascript:void(0);">&nbsp;</a>
        </div>
        <!-- Kết thúc hiển thị chăm sóc khách hàng và khiếu nại-->

        <!-- Bắt đầu hiển thị đám mây từ-->
        <div id="wordCloud"></div>
        <div class="contentButton" style="top:1402px;left:3679px">
            <a href="javascript:void(0);">&nbsp;</a>
        </div>
        <!-- Kết thúc đám mây từ-->

        <!-- Bắt đầu khu vực dashboard-->
        <!-- <div id="gauge1"></div>
            <div class="gaugeTitle" style="left:2200px;top:480px;"><sapn id="vg1">32</sapn>&nbsp;m<sup>3</sup>/d</div>
            <div id="gauge2"></div>
            <div class="gaugeTitle" style="left:2550px;top:480px;"><sapn id="vg2">32</sapn>&nbsp;KVA</div>
            <div id="gauge3"></div>
            <div class="gaugeTitle" style="left:2910px;top:480px;"><sapn id="vg3">32</sapn>&nbsp;Nm<sup>3</sup>/h</div>
            <div id="gauge4"></div>
            <div class="gaugeTitle" style="left:2380px;top:750px;"><sapn id="vg4">32</sapn>&nbsp;m<sup>3</sup>/m</div>
            <div id="gauge5"></div>
            <div class="gaugeTitle" style="left:2730px;top:750px;"><sapn id="vg5">32</sapn>&nbsp;t/h</div> -->
        <!-- Kết thúc khu vực dashboard-->

        <!--Cửa sổ bật lên-->
        <!--<div id="popWindow">
                <div style="padding:20px;font-size:32px; background-color:#051E3C;color:#B7E1FF; border-bottom:1px solid #09F">Tiêu đề cửa sổ popup</div>
            </div>-->
        <!--Kết thúc cửa sổ popup---->
    </div>>

    <!--Cửa sổ popup chỉnh sửa người dùng hệ thống-->
    <div id="modalDlg" class="easyui-dialog" title="Cửa sổ bật lên" data-options="modal:true,closed:true,buttons:
        [{
                    text:'Xác nhận',
                    iconCls:'icon-ok',
                    handler:function(){
                        $('#modalDlg').dialog('close');
                    }
                },{
                    text:'Hủy',
                    handler:function(){
                        $('#modalDlg').dialog('close');
                    }
                }]"
        style="padding:10px">
        <table width="100%" cellpadding="5">
            <tr>
                <td width="80" align="center">Tên người dùng:</td>
                <td><input type="text" name="updateUsername" id="updateUsername" value=""></td>
            </tr>
            <tr>
                <td align="center">Mật khẩu đăng nhập:</td>
                <td><input type="text" name="updateUserpass" id="updateUserpass" value=""></td>
            </tr>
            <tr>
                <td align="center">&nbsp;</td>
                <td height="30">Nếu không cần đổi mật khẩu, vui lòng để trống</td>
            </tr>
            <tr>
                <td align="center">Loại người dùng:</td>
                <td>
                    <select name="updateUserType" id="updateUserType">
                        <option value="">--Vui lòng chọn--</option>
                        <option value="administrator">Quản trị viên</option>
                        <option value="user">Người dùng hệ thống</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td align="center">Trạng thái người dùng:</td>
                <td>
                    <input type="radio" name="updateUserStatus" id="updateUserStatus1" value="0"><label for="updateUserStatus1">Kích hoạt</label>&nbsp;&nbsp;
                    <input type="radio" name="updateUserStatus" id="updateUserStatus2" value="1"><label for="updateUserStatus2">Vô hiệu hóa</label>
                </td>
            </tr>
            <tr>
                <td align="center">Mô tả người dùng:</td>
                <td>
                    <input type="text" name="updateUserDescription" id="updateUserDescription" value="">
                </td>
            </tr>
        </table>
    </div>
    <!--Kết thúc cửa sổ popup chỉnh sửa người dùng hệ thống-->

</body>

</html>