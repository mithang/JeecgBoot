export default {
  api: {
    operationFailed: 'Thao tác thất bại',
    errorTip: 'Thông báo lỗi',
    errorMessage: 'Thao tác thất bại, hệ thống bất thường!',
    timeoutMessage: 'Hết thời gian đăng nhập, vui lòng đăng nhập lại!',
    apiTimeoutMessage: 'Yêu cầu API quá thời gian, vui lòng làm mới trang và thử lại!',
    apiRequestFailed: 'Yêu cầu bị lỗi, vui lòng thử lại sau',
    networkException: 'Bất thường mạng',
    networkExceptionMsg: 'Mạng bất thường, vui lòng kiểm tra kết nối mạng của bạn có bình thường không!',

    errMsg401: 'Người dùng không có quyền (token, tên người dùng, mật khẩu sai)!',
    errMsg403: 'Người dùng đã được cấp quyền, nhưng truy cập bị cấm.!',
    errMsg404: 'Lỗi yêu cầu mạng, không tìm thấy tài nguyên này!',
    errMsg405: 'Lỗi yêu cầu mạng, phương thức yêu cầu không được phép!',
    errMsg408: 'Yêu cầu mạng quá thời gian!',
    errMsg500: 'Lỗi máy chủ, vui lòng liên hệ quản trị viên!',
    errMsg501: 'Mạng chưa được triển khai!',
    errMsg502: 'Lỗi mạng!',
    errMsg503: 'Dịch vụ không khả dụng, máy chủ tạm thời quá tải hoặc đang bảo trì!',
    errMsg504: 'Mạng quá thời gian!',
    errMsg505: 'Phiên bản http không hỗ trợ yêu cầu này!',

    registerMsg: 'Đăng ký thành công',
  },
  app: { logoutTip: 'Lưu ý', logoutMessage: 'Bạn có xác nhận thoát hệ thống không?', menuLoading: 'Đang tải menu...' },
  errorLog: {
    tableTitle: 'Danh sách nhật ký lỗi',
    tableColumnType: 'Loại',
    tableColumnDate: 'Thời gian',
    tableColumnFile: 'Tệp',
    tableColumnMsg: 'Thông tin lỗi',
    tableColumnStackMsg: 'Thông tin stack',

    tableActionDesc: 'Chi tiết',

    modalTitle: 'Chi tiết lỗi',

    fireVueError: 'Nhấp để kích hoạt lỗi vue',
    fireResourceError: 'Nhấp để kích hoạt lỗi tải tài nguyên',
    fireAjaxError: 'Nhấp để kích hoạt lỗi ajax',

    enableMessage: 'Chỉ có hiệu lực khi useErrorHandle=true trong `/src/settings/projectSetting.ts`.',
  },
  exception: {
    backLogin: 'Quay lại đăng nhập',
    backHome: 'Quay lại trang chủ',
    subTitle403: 'Xin lỗi, bạn không có quyền truy cập trang này.',
    subTitle404: 'Xin lỗi, trang bạn truy cập không tồn tại.',
    subTitle500: 'Xin lỗi, máy chủ báo lỗi.',
    noDataTitle: 'Trang hiện tại không có dữ liệu',
    networkErrorTitle: 'Lỗi mạng',
    networkErrorSubTitle: 'Xin lỗi, kết nối mạng của bạn đã bị ngắt, vui lòng kiểm tra mạng của bạn!',
  },
  lock: {
    unlock: 'Nhấp để mở khóa',
    alert: 'Mật khẩu khóa màn hình sai',
    backToLogin: 'Quay lại đăng nhập',
    entry: 'Vào hệ thống',
    placeholder: 'Mật khẩu khóa màn hình',
  },
  login: {
    backSignIn: 'Quay lại',
    signInFormTitle: 'Đăng nhập',
    mobileSignInFormTitle: 'Đăng nhập bằng điện thoại',
    qrSignInFormTitle: 'Đăng nhập bằng mã QR',
    signUpFormTitle: 'Đăng ký',
    forgetFormTitle: 'Đặt lại mật khẩu',

    signInTitle: 'Jeecg Boot',
    signInDesc: 'Là nền tảng low-code cấp doanh nghiệp có ảnh hưởng nhất tại Trung Quốc! Phát triển trực tuyến, thiết kế kéo thả trực quan, không cần code vẫn thực hiện được 80% chức năng cơ bản~',
    policy: 'Tôi đồng ý với chính sách bảo mật Qiaoqiaoyun',
    scanSign: `Sau khi quét mã, bạn có thể hoàn tất đăng nhập`,
    scanSuccess: `Quét mã thành công, đang đăng nhập`,

    loginButton: 'Đăng nhập',
    registerButton: 'Đăng ký',
    rememberMe: 'Ghi nhớ tôi',
    forgetPassword: 'Quên mật khẩu?',
    otherSignIn: 'Cách đăng nhập khác',

    // notify
    loginSuccessTitle: 'Đăng nhập thành công',
    loginSuccessDesc: 'Chào mừng quay trở lại',

    // placeholder
    accountPlaceholder: 'Vui lòng nhập tài khoản',
    passwordPlaceholder: 'Vui lòng nhập mật khẩu',
    inputCodePlaceholder: 'Vui lòng nhập mã xác minh',
    smsPlaceholder: 'Vui lòng nhập mã xác minh',
    mobilePlaceholder: 'Vui lòng nhập số điện thoại',
    mobileCorrectPlaceholder: 'Vui lòng nhập số điện thoại chính xác',
    policyPlaceholder: 'Chỉ có thể đăng ký sau khi đánh dấu chọn',
    diffPwd: 'Hai lần nhập mật khẩu không khớp',

    userName: 'Tài khoản',
    password: 'Mật khẩu',
    loginOrgCode: 'Phòng ban',
    inputCode: 'Mã xác minh',
    confirmPassword: 'Xác nhận mật khẩu',
    email: 'Email',
    smsCode: 'Mã xác minh qua SMS',
    mobile: 'Số điện thoại',

    subTitleText: 'Quay lại trang đăng nhập sau {0} giây',

    //Trang đặt lại mật khẩu
    authentication:'Xác minh danh tính',
    resetLoginPassword:'Đặt lại mật khẩu đăng nhập',
    resetSuccess:'Đặt lại thành công',
    nextStep:'Bước tiếp theo',
    goToLogin:'Đến đăng nhập'
  },
};
