/*!
* Admin Util for XXL-BOOT
* ================
*
* 1. openTab:     Mở trang mới, tương thích iframe và window.open
*
* @author       xuxueli
* @repository   https://github.com/xuxueli/xxl-boot
*/
$(function(){


    // ---------------------- openTab ----------------------

    /**
     * Mở trang mới, tương thích iframe và window.open
     *
     * @param tabSrc            Địa chỉ truy cập của tab
     * @param tabName           Tên hiển thị của tab
     * @param isCloseCurrent    Có đóng trang hiện tại hay không
     */
    window.openTab = function (tabSrc, tabName, isCloseCurrent) {
        // open tab
        if (window.parent.$.adminTab) {
            window.parent.$.adminTab.openTab({
                tabSrc: tabSrc,
                tabName: tabName
            }, isCloseCurrent)
        } else {
            if (isCloseCurrent) {
                window.open(tabSrc, '_self');
            } else {
                window.open(tabSrc, '_blank');
            }
        }
    }

    // ---------------------- isOpenWithTab ----------------------

    /**
     * Có mở trong Tab hay không
     */
    window.isOpenWithTab = function () {
        return !!window.parent.$.adminTab;
    }

});
