/*!
* Admin Tab for XXL-BOOT
* ================
*
* 1. menu: 
*      - J_menuItem:        Menu
* 2. tab: 
*      - J_tabLeft:         Nút di chuyển sang trái
*      - J_menuTabs:
*          - J_menuTab :    Tab Menu
*      - J_tabRight:        Nút di chuyển sang phải
*      - J_tabCloseOther:   Đóng các Tab khác
*      - J_tabCloseAll :    Đóng tất cả các Tab
* 3. contont
*      - J_mainContent
*          - J_iframe :     iframe của trang
*
* @author       xuxueli
* @repository   https://github.com/xuxueli/xxl-boot
*/
(function($) {

    $.extend({
        adminTab: {
            initTab: function(options) {

                /**
                 * Nhấn vào menu: mở Tab, hiển thị trang Menu
                 */
                $('.J_menuItem').on('click', function (){
                    // Lấy thuộc tính của Tab
                    let tabSrc = $(this).attr('href');
                    let tabName = $.trim($(this).text());

                    // open tab
                    return openTab(tabSrc, tabName);
                });

                /**
                 * Đóng Tab (nút x): đóng trang menu
                 */
                $('.J_menuTabs').on('click', '.J_menuTab i', closeTab);

                /**
                 * Nhấn vào Tab (chuyển đổi/kích hoạt): hiển thị trang Tab + cập nhật trạng thái active của menu bên trái
                 */
                $('.J_menuTabs').on('click', '.J_menuTab', activeTab);

                /**
                 * Tab – nút di chuyển sang trái: xem các tab bị ẩn ở bên trái
                 */
                $('.J_tabLeft').on('click', scrollTabLeft);

                /**
                 * Tab – nút di chuyển sang phải: xem các tab bị ẩn ở bên phải
                 */
                $('.J_tabRight').on('click', scrollTabRight);

                /**
                 * Nút làm mới Tab: làm mới trang Tab đang active
                 */
                $('.tabReload').on('click', refreshTab);

                /**
                 * Đóng các tab khác
                 */
                $('.J_tabCloseOther').on('click', closeOtherTabs);

                /**
                 * Đóng tab hiện tại
                 */
                $('.tabCloseCurrent').on('click', tabCloseCurrent);

                /**
                 * Đóng tất cả các tab
                 */
                $('.J_tabCloseAll').on('click', tabCloseAll);

                /**
                 * Hiển thị toàn màn hình
                 */
                $('#fullScreen').on('click', function () {
                    let currentHash = window.location.hash;
                    $(document).toggleFullScreen();

                    // reset
                    if (currentHash) {
                        setTimeout(function (){
                            window.location.hash = currentHash;
                        },50)
                    }
                });

                /**
                 * Mặc định mở Tab menu: ưu tiên thử mở TAB theo đường dẫn url, nếu không được thì mở menu đầu tiên
                 */
                openDefaultTab();
            },
            openTab: function(options, isCloseCurrent) {
                // Trang hiện tại có bị đóng hay không
                if (isCloseCurrent) {
                    tabCloseCurrent();
                }
                // Mở trang Tab
                return openTab(options.tabSrc, options.tabName);
            }
        }
    });

    // -------------------- tab: open default --------------------

    /**
     * Mặc định mở Tab menu: khởi tạo menu trang chủ, sau đó thử mở TAB theo đường dẫn url
     */
    function openDefaultTab() {
        // load url
        let tabSrc = window.location.hash.slice(1);

        // 1. Menu trang chủ: khởi tạo
        let $firstMenuItem = $(".J_menuItem:first");
        if ($firstMenuItem.length > 0) {
            $firstMenuItem.click();
            // Logic đặc biệt của menu trang chủ, không cho phép đóng
            $('.J_menuTab[data-id="' + $firstMenuItem.attr('href') + '"] i').remove();
        }

        // 2. URL khớp với menu, khởi tạo
        if (tabSrc === '' || tabSrc === undefined || tabSrc === null) {
            // Nếu đường dẫn menu của URL không tồn tại thì bỏ qua
            return;
        }
        setTimeout(function (){
            var $menuItem = $('.J_menuItem').filter('a[href$="' + decodeURI(tabSrc) + '"]');
            if ($menuItem.length > 0) {
                // URL khớp với menu, khởi tạo
                $menuItem.click();
                return;
            } else {
                // Khớp thất bại, mở trực tiếp theo phương án dự phòng
                openTab(tabSrc, tabSrc);
            }
        }, 100)

    }

    // -------------------- tab: open、close --------------------

    /**
     * Mở Tab: theo url + tên
     *
     * @param tabSrc
     * @param tabName
     * @returns {boolean}
     */
    function openTab(tabSrc, tabName) {
        // 0、valid dateurl
        if (tabSrc === undefined || $.trim(tabSrc).length === 0){
            return false;
        }
        if (tabName === undefined || $.trim(tabName).length === 0){
            tabName = tabSrc;
        }
        let tabNameShow = tabName.length > 15 ? tabName.substring(0, 15) + '...' : tabName;

        // 1. Liên động menu + cập nhật anchor của trang (tham số hash)
        activeMenuAndPath(tabSrc);

        // 2. Khớp với Tab đã tồn tại, chuyển đổi/hiển thị active
        let tabExist = false;
        $('.J_menuTab').each(function () {
            // Khớp thành công
            if ($(this).data('id') === tabSrc) {
                // Tab có đang hiển thị hay không, nếu không thì chuyển sang hiển thị
                if (!$(this).hasClass('active')) {
                    $(this).addClass('active').siblings('.J_menuTab').removeClass('active');
                    scrollToTab(this);
                    // Hiển thị vùng nội dung tương ứng với Tab
                    $('.J_mainContent .J_iframe').each(function () {
                        if ($(this).data('id') === tabSrc) {
                            $(this).show().siblings('.J_iframe').hide();
                            return false;
                        }
                    });
                }
                tabExist = true;
                return false;
            }
        });
        if (tabExist) {
            return false;
        }

        // 3. Tab chưa tồn tại, khởi tạo Tab mới + IFrame
        // build Tab (other tab no-active)
        $('.J_menuTab').removeClass('active');
        var tabStr = '<a href="javascript:;" class="active J_menuTab" data-id="' + tabSrc + '" title="'+ tabName +'" >' + tabNameShow + ' <i class="fa fa-times-circle"></i></a>';

        // build IFrame (other ifame hide)
        var iframeStr = '<iframe class="J_iframe" width="100%" height="100%" src="' + tabSrc + '" frameborder="0" data-id="' + tabSrc + '" seamless></iframe>';

        // 4. Thêm Tab + IFrame
        // append iframe
        $('.J_mainContent').find('iframe.J_iframe').hide();
        $('.J_mainContent').append(iframeStr);
        // append tab
        $('.J_menuTabs .page-tabs-content').append(tabStr);

        // Thêm lớp che (overlay)
        NProgress.inc(0.2);
        NProgress.configure({
            easing: 'ease',         // Hàm easing của hiệu ứng (mặc định: 'ease')
            speed: 500,             // Tốc độ hiệu ứng (mili giây) (mặc định: 200)
            showSpinner: true      // Có hiển thị biểu tượng xoay hay không (mặc định: true)
        });
        NProgress.start();

        // load iframe
        let $iframe = $('.J_mainContent iframe:visible');
        $iframe.on('load', function () {
            NProgress.done();
        }).on('error', function () {
            NProgress.done();
            // Xử lý trường hợp tải thất bại, ngăn chuyển hướng
            console.error('iframe load error, src = ' + $(this).attr('src'));
        });

        // 5. Cuộn đến Tab đang được kích hoạt
        scrollToTab($('.J_menuTab.active'));
        return false;
    }

    /**
     * Đóng Tab: theo phần tử Tab được nhấn
     *
     * @returns {boolean}
     */
    function closeTab() {
        // param
        var closeTabId = $(this).parents('.J_menuTab').data('id');
        var currentWidth = $(this).parents('.J_menuTab').width();

        // process
        if ($(this).parents('.J_menuTab').hasClass('active')) {
            // 1. Phần tử hiện tại đang ở trạng thái hoạt động

            // Phía sau phần tử hiện tại có phần tử cùng cấp, đặt phần tử phía sau vào trạng thái hoạt động
            if ($(this).parents('.J_menuTab').next('.J_menuTab').length > 0) {
                // Tab tiếp theo, kích hoạt Tab
                var activeId = $(this).parents('.J_menuTab').next('.J_menuTab:eq(0)').data('id');
                $(this).parents('.J_menuTab').next('.J_menuTab:eq(0)').addClass('active');

                // Tab tiếp theo, kích hoạt iframe
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') === activeId) {
                        $(this).show().siblings('.J_iframe').hide();
                        return false;
                    }
                });

                var marginLeftVal = parseInt($('.page-tabs-content').css('margin-left'));
                if (marginLeftVal < 0) {
                    $('.page-tabs-content').animate({
                        marginLeft: (marginLeftVal + currentWidth) + 'px'
                    }, "fast");
                }

                //  Xóa Tab hiện tại
                $(this).parents('.J_menuTab').remove();

                // Xóa iframe hiện tại
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') === closeTabId) {
                        $(this).remove();
                        return false;
                    }
                });
            }

            // Phía sau phần tử hiện tại không có phần tử cùng cấp, đặt phần tử phía trước của phần tử hiện tại vào trạng thái hoạt động
            if ($(this).parents('.J_menuTab').prev('.J_menuTab').length > 0) {
                // Tab trước đó, kích hoạt Tab
                var activeId = $(this).parents('.J_menuTab').prev('.J_menuTab:last').data('id');
                $(this).parents('.J_menuTab').prev('.J_menuTab:last').addClass('active');

                // Tab trước đó, kích hoạt iframe
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') == activeId) {
                        $(this).show().siblings('.J_iframe').hide();
                        return false;
                    }
                });

                //  Xóa Tab hiện tại
                $(this).parents('.J_menuTab').remove();

                // Xóa iframe hiện tại
                $('.J_mainContent .J_iframe').each(function () {
                    if ($(this).data('id') === closeTabId) {
                        $(this).remove();
                        return false;
                    }
                });
            }
        } else {
            // 2. Phần tử hiện tại không ở trạng thái hoạt động

            //  Xóa Tab hiện tại
            $(this).parents('.J_menuTab').remove();

            // Xóa iframe hiện tại
            $('.J_mainContent .J_iframe').each(function () {
                if ($(this).data('id') == closeTabId) {
                    $(this).remove();
                    return false;
                }
            });

            // Cuộn đến Tab ở trạng thái active
            scrollToTab($('.J_menuTab.active'));
        }

        // 3. Liên động menu + cập nhật anchor của trang (tham số hash)
        activeMenuAndPath($('.page-tabs-content').find('.active').attr('data-id'));
        return false;
    }

    /**
     * Liên động menu + cập nhật anchor của trang (tham số hash)
     */
    function activeMenuAndPath(tabSrc) {

        // Cập nhật anchor của trang (tham số hash)
        window.location.hash = tabSrc;

        // Chuyển đổi/active menu
        $(".sidebar-menu ul li, .sidebar-menu li").removeClass("active");
        $('.J_menuItem').each(function () {
            if ($(this).attr('href') === tabSrc) {

                // Chuyển đổi/active menu
                $(this).parents("li").addClass("active");


                return true;
            }
        })
        return false;
    }

    /**
     * Nhấn vào Tab (chuyển đổi/kích hoạt): hiển thị trang Tab + cập nhật trạng thái active của menu bên trái
     */
    function activeTab() {
        // Đã kích hoạt active hay chưa, tránh xử lý trùng lặp
        if (!$(this).hasClass('active')) {
            // Thông tin Tab chờ kích hoạt
            let tabSrc = $(this).data('id');

            // Chuyển đổi hiển thị Iframe
            $('.J_mainContent .J_iframe').each(function () {
                if ($(this).data('id') === tabSrc) {
                    $(this).show().siblings('.J_iframe').hide();
                    return false;
                }
            });

            // Kích hoạt Tab
            $(this).addClass('active').siblings('.J_menuTab').removeClass('active');
            scrollToTab(this);

            // Liên động menu
            activeMenuAndPath(tabSrc);
        }
    }

    // -------------------- tab: scroll --------------------

    /**
     * Cuộn đến tab được chỉ định
     */
    function scrollToTab(element) {

        // Chiều rộng bên trái của phần tử hiện tại
        var marginLeftVal = calSumWidth($(element).prevAll()), marginRightVal = calSumWidth($(element).nextAll());
        // Chiều rộng vùng bên ngoài Tab
        var tabOuterWidth = calSumWidth($(".content-tabs").children().not(".J_menuTabs"));
        // Chiều rộng Tab trong vùng hiển thị
        var visibleWidth = $(".content-tabs").outerWidth(true) - tabOuterWidth;
        // Chiều rộng cuộn thực tế
        var scrollVal = 0;
        if ($(".page-tabs-content").outerWidth() < visibleWidth) {
            scrollVal = 0;
        } else if (marginRightVal <= (visibleWidth - $(element).outerWidth(true) - $(element).next().outerWidth(true))) {
            if ((visibleWidth - $(element).next().outerWidth(true)) > marginRightVal) {
                scrollVal = marginLeftVal;
                var tabElement = element;
                while ((scrollVal - $(tabElement).outerWidth()) > ($(".page-tabs-content").outerWidth() - visibleWidth)) {
                    scrollVal -= $(tabElement).prev().outerWidth();
                    tabElement = $(tabElement).prev();
                }
            }
        } else if (marginLeftVal > (visibleWidth - $(element).outerWidth(true) - $(element).prev().outerWidth(true))) {
            scrollVal = marginLeftVal - $(element).prev().outerWidth(true);
        }

        // Xử lý cuộn
        $('.page-tabs-content').animate({
            marginLeft: 0 - scrollVal + 'px'
        }, "fast");
    }

    /**
     * Tính tổng chiều rộng của tập hợp phần tử
     */
    function calSumWidth(elements) {
        var width = 0;
        $(elements).each(function () {
            width += $(this).outerWidth(true);
        });
        return width;
    }

    // -------------------- tab: scroll-left、scroll-right、 --------------------

    /**
     * Tab – nút di chuyển sang trái: xem các tab bị ẩn ở bên trái
     */
    function scrollTabLeft() {

        // Chiều rộng bên trái của phần tử hiện tại
        var marginLeftVal = Math.abs(parseInt($('.page-tabs-content').css('margin-left')));
        // Chiều rộng vùng bên ngoài Tab
        var tabOuterWidth = calSumWidth($(".content-tabs").children().not(".J_menuTabs"));
        // Chiều rộng tab trong vùng hiển thị
        var visibleWidth = $(".content-tabs").outerWidth(true) - tabOuterWidth;
        // Chiều rộng cuộn thực tế
        var scrollVal = 0;
        if ($(".page-tabs-content").width() < visibleWidth) {
            return false;
        } else {
            var tabElement = $(".J_menuTab:first");
            var offsetVal = 0;
            while ((offsetVal + $(tabElement).outerWidth(true)) <= marginLeftVal) {//Tìm phần tử gần tab hiện tại nhất
                offsetVal += $(tabElement).outerWidth(true);
                tabElement = $(tabElement).next();
            }
            offsetVal = 0;
            if (calSumWidth($(tabElement).prevAll()) > visibleWidth) {
                while ((offsetVal + $(tabElement).outerWidth(true)) < (visibleWidth) && tabElement.length > 0) {
                    offsetVal += $(tabElement).outerWidth(true);
                    tabElement = $(tabElement).prev();
                }
                scrollVal = calSumWidth($(tabElement).prevAll());
            }
        }

        // Xử lý cuộn
        $('.page-tabs-content').animate({
            marginLeft: 0 - scrollVal + 'px'
        }, "fast");
    }

    /**
     * Tab – nút di chuyển sang phải: xem các tab bị ẩn ở bên phải
     */
    function scrollTabRight() {

        // Chiều rộng bên trái của phần tử hiện tại
        var marginLeftVal = Math.abs(parseInt($('.page-tabs-content').css('margin-left')));
        // Chiều rộng vùng hiển thị không phải tab
        var tabOuterWidth = calSumWidth($(".content-tabs").children().not(".J_menuTabs"));
        // Chiều rộng tab trong vùng hiển thị
        var visibleWidth = $(".content-tabs").outerWidth(true) - tabOuterWidth;
        // Chiều rộng cuộn thực tế
        var scrollVal = 0;
        if ($(".page-tabs-content").width() < visibleWidth) {
            return false;
        } else {
            var tabElement = $(".J_menuTab:first");
            var offsetVal = 0;
            while ((offsetVal + $(tabElement).outerWidth(true)) <= marginLeftVal) {//Tìm phần tử gần tab hiện tại nhất
                offsetVal += $(tabElement).outerWidth(true);
                tabElement = $(tabElement).next();
            }
            offsetVal = 0;
            while ((offsetVal + $(tabElement).outerWidth(true)) < (visibleWidth) && tabElement.length > 0) {
                offsetVal += $(tabElement).outerWidth(true);
                tabElement = $(tabElement).next();
            }
            scrollVal = calSumWidth($(tabElement).prevAll());
            if (scrollVal > 0) {
                $('.page-tabs-content').animate({
                    marginLeft: 0 - scrollVal + 'px'
                }, "fast");
            }
        }
    }

    // -------------------- tab: refresh、close-other、close-current、close-all --------------------

    /**
     * Nút làm mới Tab: làm mới trang Tab đang active
     */
    function refreshTab() {

        // Hiển thị thanh tiến trình
        NProgress.inc(0.2);
        NProgress.configure({
            easing: 'ease',         // Hàm easing của hiệu ứng (mặc định: 'ease')
            speed: 500,             // Tốc độ hiệu ứng (mili giây) (mặc định: 200)
            showSpinner: true       // Có hiển thị biểu tượng xoay hay không (mặc định: true)
        });
        NProgress.start();

        // 1. Lấy Tab đang được kích hoạt
        var tabSrc = $('.page-tabs-content').find('.active').attr('data-id');
        var target = $('.J_iframe[data-id="' + tabSrc + '"]');
        var url = target.attr('src');

        // 2. Tải lại trang
        // target.attr('src', url).ready();
        target.attr('src', url).on('load', function () {
            NProgress.done();

            // 3. Liên động menu + cập nhật anchor của trang (tham số hash)
            activeMenuAndPath($('.page-tabs-content').find('.active').attr('data-id'));

        }).on('error', function () {
            NProgress.done();
            // Xử lý trường hợp tải thất bại, ngăn chuyển hướng
            console.error('iframe load error, src = ' + $(this).attr('src'));
        });
    }

    /**
     * Nhấn đúp vào Tab: làm mới trang menu
     */
    /*$('.J_menuTabs').on('dblclick', '.J_menuTab', refreshTab);*/

    /**
     * Cuộn đến tab đang được kích hoạt
     */
    /*$('.J_tabShowActive').on('click', showActiveTab);
    function showActiveTab(){
        scrollToTab($('.J_menuTab.active'));
    }*/

    /**
     * Đóng các tab khác
     */
    function closeOtherTabs(){
        $('.page-tabs-content').children("[data-id]").not(":first").not(".active").each(function () {
            $('.J_iframe[data-id="' + $(this).data('id') + '"]').remove();
            $(this).remove();
        });
        $('.page-tabs-content').css("margin-left", "0");
    }

    /**
     * Đóng tab hiện tại
     */
    function tabCloseCurrent() {
        $('.page-tabs-content').find('.active i').trigger("click");
    }

    /**
     * Đóng tất cả các tab
     */
    function tabCloseAll(){
        // Giữ lại Tab đầu tiên, xóa các Tab khác (Tab+iframe)
        $('.page-tabs-content').children("[data-id]").not(":first").each(function () {
            $('.J_iframe[data-id="' + $(this).data('id') + '"]').remove();
            $(this).remove();
        });
        // Kích hoạt/hiển thị Tab đầu tiên (Tab+iframe)
        $('.page-tabs-content').children("[data-id]:first").each(function () {
            $('.J_iframe[data-id="' + $(this).data('id') + '"]').show();
            $(this).addClass("active");
        });
        $('.page-tabs-content').css("margin-left", "0");

        // Liên động menu + cập nhật anchor của trang (tham số hash)
        activeMenuAndPath($('.page-tabs-content').find('.active').attr('data-id'));
    }


})(jQuery);
