<template>
  <Loading :loading="loading" content="Đang tải, vui lòng đợi"></Loading>
  <div class="content" style="height: 100vh; display: flex; flex-direction: column; overflow: hidden; background-color: #f0f2f5">
    <div style="width: 100%; flex: none" class="header-box">
      <a-layout>
        <a-layout-header class="space-between header">
          <div class="align-center header-title">
            <div class="header-name ellipsis" style="cursor: pointer" :title="formState.name" @click="showInputClick" v-if="!showInput">
              {{ formState.name }}
            </div>
            <div v-else class="header-name">
              <a-input ref="nameRef" style="border-bottom: 1px solid #73a2e3; border-radius: 0" :bordered="false" v-model:value="formState.name" @pressEnter="saveTemplateNameClick" @blur="saveTemplateNameClick"></a-input>
            </div>
          </div>
        </a-layout-header>
        <a-layout-content>
          <div class="center">
            <div class="menu" editor-component="menu">
              <div class="menu-item">
                <!--  Lưu  -->
                <div class="jeecg-menu-item-save" title="Lưu mẫu" @click="saveTemplate">
                  <i />
                </div>
                <!--  Tải lên  -->
                <div class="jeecg-menu-item-upload" title="Tải lên" @click="uploadWordFile">
                  <i />
                  <input
                    ref="uploadWordFileRef"
                    type="file"
                    accept=".docx"
                    @change="uploadDocxChange"
                    style="display: none"/>
                </div>
                <div style="display: none">
                  <input id="uploadDocxBtn" type="file" accept=".docx" />
                </div>
                <div class="jeecg-menu-item-download" title="Xuất mẫu" @click="downloadTemplate">
                  <i />
                </div>
                <!--  Xem trước  -->
                <!-- <div class="menu-item-preview" title="Xem trước">
                   <i />
                 </div>-->
                <div class="jeecg-menu-item-undo no-allow" @click="undo" :title="isApple ? 'Hoàn tác(⌘+Z)' : 'Hoàn tác(Ctrl+Z)'">
                  <i />
                </div>
                <div class="jeecg-menu-item-redo no-allow" @click="redo" :title="isApple ? '(Làm lại⌘)' : '(CtrlY)'">
                  <i />
                </div>
                <div class="jeecg-menu-item-painter" title="Cọ định dạng(Nhấp đúp có thể sử dụng liên tục)" @click="painterClick" @dblclick="painterDblClick">
                  <i />
                </div>
                <div class="jeecg-menu-item-format" title="Xóa định dạng" @click="clearFormat">
                  <i />
                </div>
              </div>
              <a-divider type="vertical" style="border-inline-start: 1px solid #cfd2d8; height: 20px" />
              <div class="menu-item">
                <div class="jeecg-menu-item-font" @click="fontFamilyClick('.jeecg-menu-item-font', '.options')">
                  <span class="select" title="Phông chữ">{{ formState.family }}</span>
                  <div class="options">
                    <ul>
                      <li v-for="item in fontStyleOption" :data-family="item.value" :style="{ 'font-family': item.value}" @click="font(item.value, item.label)">{{ item.label }}</li>
                    </ul>
                  </div>
                </div>
                <div title="Cỡ chữ" class="jeecg-menu-item-size" @click="fontFamilyClick('.jeecg-menu-item-size', '.options')">
                  <span class="select" title="Phông chữ">{{ formState.size }}</span>
                  <div class="options">
                    <ul>
                      <li v-for="item in fontSizeOption" :data-size="item.value" @click="fontSize(item.value, item.label)">{{ item.label }}</li>
                    </ul>
                  </div>
                </div>
                <div class="jeecg-menu-item-size-add" :title="`Tăng cỡ chữ(${isApple ? '⌘' : 'Ctrl'}+[)`" @click="sizeAdd">
                  <i />
                </div>
                <div class="jeecg-menu-item-size-minus" :title="`Giảm cỡ chữ(${isApple ? '⌘' : 'Ctrl'}+])`" @click="sizeMinus">
                  <i />
                </div>
                <div class="jeecg-menu-item-bold" :title="`In đậm(${isApple ? '⌘' : 'Ctrl'}+B)`" @click="blob">
                  <i />
                </div>
                <div class="jeecg-menu-item-italic" :title="`Nghiêng(${isApple ? '⌘' : 'Ctrl'}+I)`" @click="itailc">
                  <i />
                </div>
                <div
                  class="jeecg-menu-item-underline"
                  :title="`Gạch chân(${isApple ? '⌘' : 'Ctrl'}+U)`"
                  @click="fontFamilyClick('.jeecg-menu-item-underline', '.options')"
                >
                  <i @click.stop="underline('')" />
                  <span class="select" />
                  <div class="options">
                    <ul>
                      <li v-for="item in underlineOptions" :data-decoration-style="item.value" @click.stop="underline(item.value)">
                        <i />
                      </li>
                    </ul>
                  </div>
                </div>
                <div class="jeecg-menu-item-strikeout" title="Gạch ngang(Ctrl+Shift+X)" @click="strikeout">
                  <i />
                </div>
                <div class="jeecg-menu-item-superscript" :title="`Chỉ số trên(${isApple ? '⌘' : 'Ctrl'}+Shift+,)`" @click="superscript">
                  <i />
                </div>
                <div class="jeecg-menu-item-subscript" :title="`Chỉ số dưới(${isApple ? '⌘' : 'Ctrl'}+Shift+.)`" @click="subscript">
                  <i />
                </div>
                <div class="jeecg-menu-item-color" title="Màu chữ" @click="colorClick">
                  <i />
                  <span />
                  <input id="color" ref="colorRef" type="color" @input="color">
                </div>
                <div class="jeecg-menu-item-highlight" title="Tô sáng" @click="highLightColorClick">
                  <i />
                  <span />
                  <input id="highlight" type="color">
                </div>
                <div class="jeecg-menu-item-cellcolor" title="Màu ô" @click="cellColorClick">
                  <i />
                  <!-- <span></span> -->
                  <input ref="cellColorRef" id="cellcolor" type="color" @input="cellColor">
                </div>
              </div>
              <a-divider type="vertical" style="border-inline-start: 1px solid #cfd2d8; height: 20px" />
              <div class="menu-item">
                <div class="jeecg-menu-item-title" @click="fontFamilyClick('.jeecg-menu-item-title', '.options')">
                  <i class="page-mode-i"/>
                  <span class="select" title="Chuyển đổi tiêu đề">{{formState.title}}</span>
                  <div class="options">
                    <ul>
                      <li v-for="(item,index) in titleOptions" style="font-size: 16px" :title="`Ctrl+${isApple ? 'Option' : 'Alt'}+${index}`" :data-level="item.value" :style="{fontSize: item.fontSize}" @click="title(item.value)">{{item.label}}</li>
                    </ul>
                  </div>
                </div>
                <div class="jeecg-menu-item-left" title="Căn trái" @click="leftAlign" :title="`Căn trái(${isApple ? '⌘' : 'Ctrl'}+L)`">
                  <i />
                </div>
                <div class="jeecg-menu-item-center" title="Căn giữa" @click="centerAlign" :title="`Căn giữa(${isApple ? '⌘' : 'Ctrl'}+E)`">
                  <i />
                </div>
                <div class="jeecg-menu-item-right" title="Căn phải" @click="rightAlign" :title="`Căn phải(${isApple ? '⌘' : 'Ctrl'}+R)`">
                  <i />
                </div>
                <div class="jeecg-menu-item-alignment" title="Căn đều hai bên" @click="alignmentAlign" :title="`Căn đều hai bên(${isApple ? '⌘' : 'Ctrl'}+J)`">
                  <i />
                </div>
                <div class="jeecg-menu-item-row-margin" @click="fontFamilyClick('.jeecg-menu-item-row-margin', '.options')">
                  <i title="Khoảng cách dòng" class="page-mode-i" />
                  <div class="options">
                    <ul>
                      <li v-for="item in marginOptions" :data-rowmargin="item.value" @click="rowMargin(item.value)">{{item.label}}</li>
                    </ul>
                  </div>
                </div>
                <div class="jeecg-menu-item-list" @click="fontFamilyClick('.jeecg-menu-item-list', '.options')">
                  <i class="page-mode-i" />
                  <div class="options" :title="`Danh sách(${isApple ? '⌘' : 'Ctrl'}+Shift+U)`">
                    <ul>
                      <li  @click="list(null,null)">
                        <span>Hủy danh sách</span>
                      </li>
                      <li data-list-type="ol" data-list-style="decimal" @click="list('ol','decimal')">
                        <span>Danh sách có thứ tự:</span>
                        <ol>
                          <li>________</li>
                        </ol>
                      </li>
                      <li data-list-type="ul" data-list-style="disc" @click="list('ul','disc')">
                        <span>Danh sách dấu chấm tròn đặc:</span>
                        <ul style="list-style-type: disc !important;">
                          <li>________</li>
                        </ul>
                      </li>
                      <li data-list-type="ul" data-list-style="circle" @click="list('ul','circle')">
                        <span>Danh sách dấu chấm tròn rỗng:</span>
                        <ul style="list-style-type: circle !important;">
                          <li>________</li>
                        </ul>
                      </li>
                      <li data-list-type="ul" data-list-style="square" @click="list('ul','square')">
                        <span>Danh sách khối vuông rỗng:</span>
                        <ul style="list-style-type: square !important;">
                          <li>________</li>
                        </ul>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
              <a-divider type="vertical" style="border-inline-start: 1px solid #cfd2d8; height: 20px" />
              <div class="menu-item">
                <!-- <div class="jeecg-menu-item-chart">
                   <i title="Biểu đồ" />
                 </div>-->
                <div class="jeecg-menu-item-table" @click="tableClick">
                  <i title="Bảng" class="page-mode-i"/>
                </div>
                <div class="jeecg-menu-item-table-collapse page-mode-i" style="display: none">
                  <div class="table-close" @click="recoveryTable">×</div>
                  <div class="table-title page-mode-i" >
                    <span class="table-select page-mode-i">Chèn</span>
                    <span class="page-mode-i">Bảng</span>
                  </div>
                  <div class="table-panel page-mode-i" @mousemove="tableMousemove" @click="tableCellClick"/>
                </div>
                <div class="jeecg-menu-item-image" @click="uploadImage">
                  <i title="Hình ảnh" />
                  <input
                    ref="uploadImageRef"
                    id="image"
                    type="file"
                    accept=".png, .jpg, .jpeg, .svg, .gif"
                    @change="uploadImageChange"
                  >
                </div>
                <!--<div class="jeecg-menu-item-barcode">
                  <i title="Mã vạch" />
                </div>
                <div class="jeecg-menu-item-qrcode">
                  <i title="Mã QR" />
                </div>-->
                <div class="jeecg-menu-item-hyperlink" @click="hyperlinkClick">
                  <i title="Siêu liên kết" />
                </div>
                <div class="jeecg-menu-item-separator" @click.stop="separatorClick">
                  <i title="Đường phân cách" class="page-mode-i"/>
                  <div class="options page-mode-i">
                    <ul>
                      <li class="page-mode-i" v-for="item in separatorOption" :data-separator="item.value" @click.stop="separatorActiveClick(item.value)">
                        <i />
                      </li>
                    </ul>
                    <div class="more page-mode-i">
                      <div class="page-mode-i">Màu sắc:<a-input @click.stop="" type="color" class="page-mode-i color" v-model:value="formState.separatorColor"/></div>
                      <div class="page-mode-i">Độ đậm(px):<a-input-number @click.stop="" type="tel" class="page-mode-i lineWidth"  v-model:value="formState.separatorLineWidth"/></div>
                      <div class="page-mode-i" style="margin-top: 10px;">
                        <a-button size="small" type="primary" @click.prevent.stop="separatorOk">Xác nhận</a-button>
                        <a-button @click.prevent.stop="separatorCancel" size="small" style="margin-left: 6px">Hủy</a-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="jeecg-menu-item-watermark" @click="fontFamilyClick('.jeecg-menu-item-watermark', '.options')">
                  <i title="Watermark(Thêm, xóa)" class="page-mode-i"/>
                  <div class="options">
                    <ul>
                      <li data-menu="add" @click="addWatermarkClick">Thêm watermark</li>
                      <li data-menu="delete" @click="deleteWatermarkClick">Xóa watermark</li>
                    </ul>
                  </div>
                </div>
                <div class="jeecg-menu-item-page-break" title="Dấu ngắt trang" @click="pageBreak">
                  <i />
                </div>
              </div>
              <a-divider type="vertical" style="border-inline-start: 1px solid #cfd2d8; height: 20px" />
              <div class="menu-item">
                <div :title="`Tìm kiếm và thay thế(${isApple ? '⌘' : 'Ctrl'}+F)`" class="jeecg-menu-item-search" data-menu="search" title="Tìm và thay thế" @click="searchClick">
                  <i class="page-mode-i"/>
                </div>
                <div class="jeecg-menu-item-search-collapse" data-menu="search" style="display: none">
                  <div class="jeecg-menu-item-search-collapse-search">
                    <input type="text" @input="searchInput" @keydown="searchKeyDown">
                    <label class="search-result" />
                    <div class="arrow-left" @click="arrowLeft">
                      <i />
                    </div>
                    <div class="arrow-right" @click="arrowRight">
                      <i />
                    </div>
                    <span @click="closeSearch">×</span>
                  </div>
                  <div class="jeecg-menu-item-search-collapse-replace">
                    <input type="text">
                    <button @click="replaceText">Thay thế</button>
                  </div>
                </div>
                <div :title="`In(${isApple ? '⌘' : 'Ctrl'}+P)`" class="jeecg-menu-item-print" data-menu="print" title="In" @click="printTemplate">
                  <i />
                </div>
              </div>
            </div>
            <div class="catalog" editor-component="catalog">
              <div class="jeecg-catalog-header">
                <span>Danh mục</span>
                <div class="jeecg-catalog-header-close" @click="closeDirectory">
                  <i />
                </div>
              </div>
              <div class="jeecg-catalog-main" />
            </div>
            <div class="editor-container">
              <div class="jeecg__editor" />
            </div>
          </div>
        </a-layout-content>
        <a-layout-footer>
          <div class="footer" editor-component="footer">
            <div>
              <div class="catalog-mode" title="Danh mục" @click="openDirectory">
                <i />
              </div>
              <div class="page-mode" @click="fontFamilyClick('.page-mode', '.options')">
                <i title="Chế độ trang(phân trang, trang liên tục)" class="page-mode-i"/>
                <div class="options">
                  <ul>
                    <li v-for="item in pageModeOptions" :data-page-mode="item.value" :class="item.value === 'paging'?'active':''" @click="pageMode(item.value)">{{item.label}}</li>
                  </ul>
                </div>
              </div>
              <span>Số trang hiển thị:<span class="page-no-list">1</span></span>
              <span>Trang:<span class="page-no">1</span>/<span class="page-size" >1</span></span>
              <span>Số từ:<span class="word-count">0</span></span>
            </div>
            <div>
              <div class="page-scale-minus" title="Thu nhỏ(Ctrl+-)" @click="scaleMiuns">
                <i />
              </div>
              <span
                class="page-scale-percentage"
                title="Hiển thị tỷ lệ(Nhấp để khôi phụcCtrl+0)"
              >100%</span>
              <div class="page-scale-add" title="Phóng to(Ctrl+=)" @click="scaleAdd">
                <i />
              </div>
              <div class="paper-size" @click="fontFamilyClick('.paper-size', '.options')">
                <i title="Loại giấy" class="page-mode-i"/>
                <div class="options" title="Khổ giấy">
                  <ul>
                    <li v-for="item in paperSizeOption" :data-paper-size="item.value" :class="item.label === 'A4'?'active':''" @click="paperSize(item.value)">{{item.label}}</li>
                  </ul>
                </div>
              </div>
              <div class="paper-direction" @click="fontFamilyClick('.paper-direction', '.options')">
                <i title="Hướng giấy" class="page-mode-i"/>
                <div class="options">
                  <ul>
                    <li v-for="item in paperDirectionOptions" :data-paper-direction="item.value" :class="item.value == 'vertical'?'active':''" @click="paperDirection(item.value)">{{item.label}}</li>
                  </ul>
                </div>
              </div>
              <div class="paper-margin" title="Lề trang" @click="paperMarginClick">
                <i />
              </div>
              <div class="fullscreen" title="Hiển thị toàn màn hình">
                <i />
              </div>
            </div>
          </div>
        </a-layout-footer>
      </a-layout>
    </div>
    <!--  Lề trang  -->
    <PagerMarginDrawer @register="registerPagerMarginDrawer" @ok="setPaperMargin"></PagerMarginDrawer>
    <!--  Màu nổi bật  -->
    <HighlightColorModal @register="registerHighlightColorModal" @ok="setHighlightColor"></HighlightColorModal>
    <!--  Siêu liên kết  -->
    <HyperlinkDrawer @register="registerHyperlinkDrawer" @ok="setHyperlink"></HyperlinkDrawer>
    <!--  Watermark  -->
    <WatermarkDrawer @register="registerWatermarkDrawer" @ok="setWatermark"></WatermarkDrawer>
  </div>
</template>

<script lang="ts" setup>
  import { Loading } from '/@/components/Loading';
  import { nextTick, onUnmounted, ref } from 'vue';
  import { createEditor, method } from '@/components/wordtpl/DocDesign';
  import PagerMarginDrawer from './components/PagerMarginDrawer.vue'
  import HyperlinkDrawer from './components/HyperlinkDrawer.vue'
  import WatermarkDrawer from './components/WatermarkDrawer.vue'
  import HighlightColorModal from './components/HighlightColorModal.vue'
  import { useDrawer } from "@/components/Drawer";
  import { defHttp } from "@/utils/http/axios";
  import { getFileAccessHttpUrl } from "@/utils/common/compUtils";
  import { useModal } from "@/components/Modal";
  
  const props = defineProps({
    //Nội dung (bao gồm hình ảnh, đầu trang, chân trang)
    content: { type: Object, default: {} },
    //Đường dẫn tải lên hình ảnh
    uploadImageUrl: { type: String, default: '/sys/common/upload' },
    //Đường dẫn tải lên tệp
    uploadFileUrl: { type: String, default: '/wordtpl/template/parse/file' },
  });

  let emit = defineEmits(['download', 'save']);
  
  //Kiểu chữ
  const fontStyleOption = [
    { label: '微软雅黑', value: 'Microsoft YaHei' },
    { label: '宋体', value: '宋体' },
    { label: '黑体', value: '黑体' },
    { label: '仿宋', value: '仿宋' },
    { label: '楷体', value: '楷体' },
    { label: '等线', value: '等线' },
    { label: '华文琥珀', value: '华文琥珀' },
    { label: '华文楷体', value: '华文楷体' },
    { label: '华文隶书', value: '华文隶书' },
    { label: '华文新魏', value: '华文新魏' },
    { label: '华文行楷', value: '华文行楷' },
    { label: '华文中宋', value: '华文中宋' },
    { label: '华文彩云', value: '华文彩云' },
    { label: 'Arial', value: 'Arial' },
    { label: 'Segoe UI', value: 'Segoe UI' },
    { label: 'Ink Free', value: 'Ink Free' },
    { label: 'Fantasy', value: 'Fantasy' },
  ];

  //Phông chữ
  const fontSizeOption = [
    { label: 'Sơ hiệu', value: '56' },{ label: 'Tiểu sơ', value: '48' },{ label: 'Số 1', value: '34' },{ label: 'Tiểu 1', value: '32' },
    { label: 'Số 2', value: '29' },{ label: 'Tiểu 2', value: '24' }, { label: 'Số 3', value: '21' }, { label: 'Tiểu 3', value: '20' },
    { label: 'Số 4', value: '18' }, { label: 'Tiểu 4', value: '16' }, { label: 'Số 5', value: '14' },  { label: 'Tiểu 5', value: '12' },
    { label: 'Số 6', value: '10' },{ label: 'Tiểu 6', value: '8' },  { label: 'Số 7', value: '7' },  { label: 'Số 8', value: '6' },
    { label: '5', value: '5' }, { label: '9', value: '9' },{ label: '13', value: '13' },{ label: '15', value: '15' },
    { label: '17', value: '17' }, { label: '19', value: '19' },{ label: '23', value: '23' },{ label: '25', value: '25' },
    { label: '26', value: '26' }, { label: '27', value: '27' },{ label: '30', value: '30' },{ label: '31', value: '31' },
    { label: '33', value: '33' },  { label: '35', value: '35' },{ label: '36', value: '36' },{ label: '37', value: '37' },
    { label: '38', value: '38' }, { label: '39', value: '39' },{ label: '40', value: '40' },{ label: '41', value: '41' },
    { label: '42', value: '42' }, { label: '43', value: '43' },{ label: '44', value: '44' },{ label: '45', value: '45' },
    { label: '46', value: '46' }, { label: '47', value: '47' },{ label: '50', value: '50' },{ label: '51', value: '51' },
    { label: '52', value: '52' }, { label: '52', value: '52' },{ label: '54', value: '54' },{ label: '57', value: '57' },
    { label: '58', value: '58' }, { label: '59', value: '59' },{ label: '60', value: '60' },{ label: '60', value: '60' },
    { label: '62', value: '62' }, { label: '63', value: '63' },{ label: '64', value: '64' },{ label: '65', value: '65' },
    { label: '66', value: '66' },{ label: '67', value: '67' },{ label: '68', value: '68' },{ label: '69', value: '69' },
    { label: '70', value: '70' },{ label: '71', value: '71' },{ label: '72', value: '72' }
  ];

  //Gạch chân
  const underlineOptions = [
    { label: 'solid', value: 'solid' },
    { label: 'double', value: 'double' },
    { label: 'dashed', value: 'dashed' },
    { label: 'dotted', value: 'dotted' },
    { label: 'wavy', value: 'wavy' },
  ];
  
  //Tiêu đề
  const titleOptions = [
    { label: 'Nội dung chính', value: null, fontSize: '16px' },
    { label: 'Tiêu đề1', value: 'first', fontSize: '26px' },
    { label: 'Tiêu đề2', value: 'second', fontSize: '24px' },
    { label: 'Tiêu đề3', value: 'third', fontSize: '22px' },
    { label: 'Tiêu đề4', value: 'fourth', fontSize: '20px' },
    { label: 'Tiêu đề5', value: 'fifth', fontSize: '18px' },
    { label: 'Tiêu đề6', value: 'sixth', fontSize: '16px' },
  ];
  
  //Khoảng cách dòng
  const marginOptions = [
    { label: '1', value: '1' },
    { label: '1.25', value: '1.25' },
    { label: '1.5', value: '1.5' },
    { label: '1.75', value: '1.75' },
    { label: '2', value: '2' },
    { label: '2.5', value: '2.5' },
    { label: '3', value: '3' },
  ]
  
  //Đường phân cách
  const separatorOption = [
    { label: '0,0', value: '0,0' },
    { label: '1,1', value: '1,1' },
    { label: '3,1', value: '3,1' },
    { label: '4,4', value: '4,4' },
    { label: '7,3,3,3', value: '7,3,3,3' },
    { label: '6,2,2,2,2,2', value: '6,2,2,2,2,2' },
  ]
  
  //Khổ giấy
  const paperSizeOption = [
    { label: 'A4', value: '794*1123' },
    { label: 'A2', value: '1593*2251' },
    { label: 'A3', value: '1125*1593' },
    { label: 'A5', value: '565*796' },
    { label: '5phong bì số', value: '412*488' },
    { label: '6phong bì số', value: '450*866' },
    { label: '7phong bì số', value: '609*862' },
    { label: '9phong bì số', value: '862*1221' },
    { label: 'Giấy khổ Legal', value: '813*1266' },
    { label: 'Giấy thư', value: '813*1054' },
  ]
  
  //Chế độ trang
  const pageModeOptions = [
    { label: 'Phân trang', value: 'paging' },
    { label: 'Trang liên tục', value: 'continuity' },
  ]  
  
  //Hướng giấy
  const paperDirectionOptions = [
    { label: 'Dọc', value: 'vertical' },
    { label: 'Ngang', value: 'horizontal' },
  ]

  //Trạng thái tải
  const loading = ref<boolean>(false);
  let { formState, save, clearFormat, undo, redo, isApple, painterClick, painterDblClick, font, 
    fontSize, sizeAdd, sizeMinus, blob, itailc, underline, strikeout, superscript, subscript,
    color, pageMode, scaleAdd, scaleMiuns, paperSize, paperDirection, printTemplate, getPaperMargin,
    setPaperMargin, updateCatalog, setWordImage, setWordContent, leftAlign, centerAlign, rightAlign, alignmentAlign,
    rowMargin, pageBreak, cellColor, setHighlightColor, title, list, tabCell, separator, setHyperlink, getHyperlink,
    setWatermark, deleteWatermarkClick, arrowLeft, arrowRight, searchInput, searchKeyDown, replaceText, closeSearch } = method();
  //của màuref
  const colorRef = ref();
  initEditor();
  //Phông chữoption
  const fontOptionDom = ref<any>();
  //Cỡ chữoption
  const sizeOptionDom = ref<any>();
  //Gạch chânoption
  const underlineOptionDom = ref<any>();
  //Chế độ trangoption
  const pageModeOptionDom = ref<any>();
  //Khổ giấyoption
  const paperSizeOptionDom = ref<any>();
  //Hướng giấyoption
  const paperDirectionOptionDom = ref<any>();
  //Khoảng cách dòngoption
  const rowMarginOptionDom = ref<any>();
  //Tiêu đềOption
  const titleOptionDom = ref<any>();
  //Danh sáchOption
  const listOptionDom = ref<any>();
  //tableLớp gấp
  const tableCollapseDom = ref<any>();
  //Đường phân cách
  const separatorOptionDom = ref<any>();
  //Watermark
  const watermarkOptionDom = ref<any>();
  //Tìm và thay thế
  const searchCollapseDom = ref<any>();
  //của lềDrawer
  const [registerPagerMarginDrawer, { openDrawer: openMarginDrawer }] = useDrawer();
  //của siêu liên kếtDrawer
  const [registerHyperlinkDrawer, { openDrawer: openHyperlinkDrawer }] = useDrawer();
  //của watermarkDrawer
  const [registerWatermarkDrawer, { openDrawer: openHyperWatermarkDrawer }] = useDrawer();
  //của hình ảnh đã tải lênref
  const uploadImageRef = ref();
  //của tệp đã tải lênref
  const uploadWordFileRef = ref();
  //Màu nền ôref
  const cellColorRef = ref();
  //Mảng cột ô
  const tableCellList = ref<any>([]);
  //Hàng thứ mấy hiện tại
  const rowIndex = ref<number>(0);
  //Cột thứ mấy hiện tại
  const colIndex = ref<number>(0);
  //Popupmodel
  const [registerHighlightColorModal, { openModal: openHighlightColorModal }] = useModal();
  
  function initEditor() {
    setTimeout(async () => {
      const fontDom: any = document.querySelector('.jeecg-menu-item-font');
      fontOptionDom.value = fontDom.querySelector('.options');
      //Cỡ chữ
      const jeecgSizeDom: any = document.querySelector('.jeecg-menu-item-size');
      sizeOptionDom.value = jeecgSizeDom.querySelector('.options');
      //Gạch chân
      const underlineDom: any = document.querySelector('.jeecg-menu-item-underline');
      underlineOptionDom.value = underlineDom.querySelector('.options');   
      //Phân trang
      const pageModeDom: any = document.querySelector('.page-mode');
      pageModeOptionDom.value = pageModeDom.querySelector('.options');
      //Phân trang
      const pageSizeDom: any = document.querySelector('.paper-size');
      paperSizeOptionDom.value = pageSizeDom.querySelector('.options');
      //Hướng giấy
      const paperDirectionDom: any = document.querySelector('.paper-direction');
      paperDirectionOptionDom.value = paperDirectionDom.querySelector('.options'); 
      //Khoảng cách dòng
      const rowMarginDom: any = document.querySelector('.jeecg-menu-item-row-margin');
      rowMarginOptionDom.value = rowMarginDom.querySelector('.options');
      //Tiêu đề
      const titleDom: any = document.querySelector('.jeecg-menu-item-title');
      titleOptionDom.value = titleDom.querySelector('.options');
      //Danh sách
      const listDom: any = document.querySelector('.jeecg-menu-item-list');
      listOptionDom.value = listDom.querySelector('.options'); 
      //Bảng
      tableCollapseDom.value = document.querySelector('.jeecg-menu-item-table-collapse');
      //Đường phân cách
      let separatorDom:any = document.querySelector('.jeecg-menu-item-separator');
      separatorOptionDom.value = separatorDom.querySelector('.options');
      //Watermark
      let watermarkDom:any = document.querySelector('.jeecg-menu-item-watermark');
      watermarkOptionDom.value = watermarkDom.querySelector('.options');
      //Tìm và thay thế
      searchCollapseDom.value = document.querySelector('.jeecg-menu-item-search-collapse');
      
      setTableStyle()
      // update-begin--author:liaozhiyang---date:20270724---for:【LHZP-1452] Sửa lỗi mục lục Word cần sửa nội dung chính mới hiển thị
      await createEditor(props.content);
      // Sau khi khởi tạo xong chủ động render mục lục một lần, tránh việc phải nhấn nút mục lục mới hiển thị
      if (formState.showCataLogDom) {
        updateCatalog();
      }
      // update-end--author:liaozhiyang---date:20270724---for:【LHZP-1452] Sửa lỗi mục lục Word cần sửa nội dung chính mới hiển thị
      // Thêm trình lắng nghe sự kiện sau khi component được mount
      document.addEventListener('click', handleClickOutside);
      //Lắng nghe sự kiện phím Ctrl hoặc Cmd và phím S
      document.addEventListener('keydown', handleKeyDown);
    }, 300);
  }

  /**
   * Sự kiện nhấp hộp thả xuống
   */
  function fontFamilyClick(parentClass, childrenClass) {
    const font: any = document.querySelector(parentClass);
    const fontOption = font.querySelector(childrenClass);
    if (parentClass === '.jeecg-menu-item-font') {
      hiddenVisible('font');
    } else if (parentClass === '.jeecg-menu-item-size') {
      hiddenVisible('size');
    } else if (parentClass === '.jeecg-menu-item-underline') {
      hiddenVisible('underline');
    } else if(parentClass === '.page-mode'){
      hiddenVisible('pageMode');
    } else if(parentClass === '.paper-size'){
      hiddenVisible('paperSize');
    } else if(parentClass === '.paper-direction'){
      hiddenVisible('paperDirection');
    } else if(parentClass === '.jeecg-menu-item-row-margin'){
      hiddenVisible('rowMargin');
    }else if(parentClass === '.jeecg-menu-item-title'){
      hiddenVisible('title');
    }else if(parentClass === '.jeecg-menu-item-list'){
      hiddenVisible('list');
    }else if(parentClass === '.jeecg-menu-item-separator'){
      hiddenVisible('separator');
    }else if(parentClass === '.jeecg-menu-item-watermark'){
      hiddenVisible('watermark');
    }
    fontOption.classList.toggle('visible');
    setDropdownContentPosition(font, fontOption);
  }

  /**
   * Đặt vị trí của bảng mở rộng một cách động
   * @param parentNode
   * @param dropdownNode
   */
  function setDropdownContentPosition(parentNode, dropdownNode) {
    const dropdownContent = dropdownNode;
    const rect = parentNode.getBoundingClientRect();
    const screenWidth = window.innerWidth;
    dropdownContent.style.top = `${rect.bottom}px`;
    dropdownContent.style.left = `${rect.left}px`;
    if (rect.left + dropdownContent.offsetWidth > screenWidth) {
      dropdownContent.style.left = `${rect.right - dropdownContent.offsetWidth}px`;
    }
  }

  /**
   * Lắng nghe sự kiện đóng
   * @param event
   */
  const handleClickOutside = (event) => {
    if (!event.target.closest('.select') && !event.target.closest(".page-mode-i")) {
      hiddenVisible();
    }
  };

  /**
   * Sự kiện nhấp vào màu
   */
  function colorClick() {
    colorRef.value.click();
  }

  /**
   * Xuất mẫu
   */
  function downloadTemplate() {
    emit('download', props.content);
  }

  /**
   * Lưu
   */
  function saveTemplate() {
    let saveData = save();
    const params = Object.assign(props.content, saveData);
    emit('save', params);
  }

  /**
   * Ẩn hiển thị hộp thả xuống
   * 
   * @param value
   */
  function hiddenVisible(value = ''){
      if(!value || value !== 'font'){
        fontOptionDom.value.classList.remove('visible');
      }
      if(!value || value !== 'size'){
        sizeOptionDom.value.classList.remove('visible');
      }
      if(!value || value !== 'underline'){
        underlineOptionDom.value.classList.remove('visible');
      }
      if(!value || value !== 'pageMode'){
        pageModeOptionDom.value.classList.remove('visible');
      }   
      if(!value || value !== 'paperSize'){
        paperSizeOptionDom.value.classList.remove('visible');
      }
      if(!value || value !== 'paperDirection'){
        paperDirectionOptionDom.value.classList.remove('visible');
      }
      if(!value || value !== 'rowMargin'){
        rowMarginOptionDom.value.classList.remove('visible');
      }  
      if(!value || value !== 'title'){
        titleOptionDom.value.classList.remove('visible');
      } 
      if(!value || value !== 'list'){
        listOptionDom.value.classList.remove('visible');
      }
      if(!value || value !== 'table'){
        recoveryTable();
      } 
      if(!value || value !== 'separator'){
        separatorOptionDom.value.classList.remove('visible');
      } 
      if(!value || value !== 'watermark'){
        watermarkOptionDom.value.classList.remove('visible');
      }
  }

  /**
   * Sự kiện nhấp vào lề
   */
  function paperMarginClick() {
    let values = getPaperMargin();
    openMarginDrawer(true,{
      ...values
    })
  }

  /**
   * Mở mục lục
   */
  function openDirectory() {
    const catalogDom:any = document.querySelector('.catalog');
    catalogDom.style.display = 'block';
    formState.showCataLogDom = true;
    updateCatalog();
  }
  
  /**
   * Đóng mục lục
   */
  function closeDirectory() {
    const catalogDom:any = document.querySelector('.catalog');
    formState.showCataLogDom = false;
    catalogDom.style.display = 'none';
  }
  
  /**
   * Tải lên hình ảnh
   */
  function uploadImage(){
    uploadImageRef.value.click();
  }
  //Sự kiện tải hình ảnh lên thành công
  const uploadImageSuccess = (fileInfo) => {
    if(fileInfo.success){
      let fileUrl = getFileAccessHttpUrl(fileInfo.message);
      setWordImage(fileUrl);
    }
  }
  
  /**
   * Sự kiện thay đổi tải tệp lên
   * @param e
   */
  function uploadImageChange(e) {
    let file = e.target.files[0];
    defHttp.uploadFile({ url: props.uploadImageUrl },  { file: file, filename: file.name, data: { biz: 'word' } }, { success: uploadImageSuccess });
  }

  //Sự kiện tải tệp lên thành công
  const uploadFileSuccess = (fileInfo) => {
    if(fileInfo.success){
      setWordContent(fileInfo.result);
    }
  }
  
  /**
   * Tải lên tệp
   */
  function uploadWordFile() {
    uploadWordFileRef.value.click();
  }

  /**
   * Tải lên tệpchange
   * @param e
   */
  function uploadDocxChange(e) {
    let file = e.target.files[0];
    defHttp.uploadFile({ url: props.uploadFileUrl },  { file: file, filename: file.name }, { success: uploadFileSuccess });
  }

  /**
   * Sự kiện nhấn vào màu nền của ô
   */
  function cellColorClick() {
    cellColorRef.value.click();
  }

  /**
   * Sự kiện nhấp vào màu tô sáng
   */
  function highLightColorClick() {
    openHighlightColorModal(true,{
      highlightColor:formState.highlightColor
    })
  }

  /**
   * Cài đặttable9*9Kiểu
   */
  function setTableStyle() {
    const tablePanel:any = document.querySelector('.table-panel');
    // Vẽ hàng và cột
    tableCellList.value = []
    for (let i = 0; i < 10; i++) {
      const tr = document.createElement('tr');
      tr.classList.add('table-row');
      const trCellList = []
      for (let j = 0; j < 10; j++) {
        const td = document.createElement('td');
        td.classList.add('table-cel');
        tr.append(td);
        trCellList.push(td);
      }
      tablePanel.append(tr);
      tableCellList.value.push(trCellList);
    }
  }
  /**
   * Sự kiện nhấp vào bảng
   */
  function tableClick() {
    hiddenVisible('table');
    const tableDom = document.querySelector('.jeecg-menu-item-table');
    tableCollapseDom.value.style.display = 'block';
    setDropdownContentPosition(tableDom, tableCollapseDom.value);
  }

  /**
   * Xóa tất cả lựa chọn ô
   */
  function removeAllTableCellSelect() {
    tableCellList.value.forEach(tr => {
      tr.forEach(td => td.classList.remove('active'))
    })
  }

  /**
   * Đặt nội dung tiêu đề
   * @param payload
   */
  function setTableTitle(payload) {
    const tableTitle:any = document.querySelector('.table-select');
    tableTitle.innerText = payload;
  }

  /**
   * Khôi phục trạng thái ban đầu
   */
  function recoveryTable() {
    // Khôi phục kiểu chọn, tiêu đề, dòng/cột đã chọn
    removeAllTableCellSelect()
    setTableTitle('Chèn')
    colIndex.value = 0
    rowIndex.value = 0
    // Ẩnpanel
    tableCollapseDom.value.style.display = 'none';
  }

  /**
   * Sự kiện di chuyển chuột trên bảng
   * @param evt
   */
  function tableMousemove(evt) {
    const celSize = 16;
    const rowMarginTop = 10;
    const celMarginRight = 6;
    const { offsetX, offsetY } = evt;
    // Xóa tất cả lựa chọn
    removeAllTableCellSelect()
    colIndex.value = Math.ceil(offsetX / (celSize + celMarginRight)) || 1;
    rowIndex.value = Math.ceil(offsetY / (celSize + rowMarginTop)) || 1;
    // Thay đổi kiểu chọn
    tableCellList.value.forEach((tr, trIndex) => {
      tr.forEach((td, tdIndex) => {
        if (tdIndex < colIndex.value && trIndex < rowIndex.value) {
          td.classList.add('active');
        }
      })
    })
    // Thay đổi tiêu đề bảng
    setTableTitle(`${rowIndex.value}×${colIndex.value}`);
  }

  /**
   * tableSự kiện nhấp đơn ô bảng
   */
  function tableCellClick() {
    tabCell(rowIndex.value,colIndex.value,recoveryTable);
  }

  /**
   * Sự kiện nhấp siêu liên kết
   */
  function hyperlinkClick() {
    let hyperlink = getHyperlink();
    openHyperlinkDrawer(true,{
      ...hyperlink
    })
  }

  /**
   * Thêm watermark
   */
  function addWatermarkClick(){
    openHyperWatermarkDrawer(true,{});
  }

  /**
   * Tìm sự kiện nhấp chuột
   */
  function searchClick() {
    searchCollapseDom.value.style.display = 'block';
    const searchDom:any =document.querySelector('.jeecg-menu-item-search');
    const searchInputDom:any = document.querySelector('.jeecg-menu-item-search-collapse-search input')
    const searchCollapseRect = searchCollapseDom.value.getBoundingClientRect();
    const bodyRect = document.body.getBoundingClientRect();
    const searchRect = searchDom.getBoundingClientRect();
    if (searchRect.left + searchCollapseRect.width > bodyRect.width) {
      searchCollapseDom.value.style.right = '0px';
      searchCollapseDom.value.style.left = 'unset';
    } else {
      searchCollapseDom.value.style.right = 'unset';
      searchCollapseDom.value.style.left = searchRect.left + 'px';
    }
    searchInputDom.focus();
  }


  /**
   * Kiểm tra có nhấn phím Ctrl hoặc Cmd và phím S hay không
   * @param event
   */
  function handleKeyDown(event) {
    // Kiểm tra có nhấn phím Ctrl hoặc Cmd và phím S hay không
    if ((event.ctrlKey || event.metaKey) && event.key === 's') {
      event.preventDefault();
      //Lưu
      saveTemplate();
    }
  }

  //Có hiển thị ô nhập không
  const showInput = ref<boolean>(false);
  //Ô nhập tiêu đề
  const nameRef = ref();
  
  /**
   * Hiển thị hoặc ẩn ô nhập
   */
  function showInputClick() {
    showInput.value = true;
    nextTick(()=>{
      nameRef.value.focus();
      nameRef.value.select();
    })
  }

  /**
   * Sự kiện lưu tên mẫu
   */
  function saveTemplateNameClick() {
    showInput.value = false;
    let saveData = save();
    if(formState.name != props.content.name){
      const params = Object.assign(props.content, saveData, { name: formState.name });
      emit('save', params);
    }
  }

  /**
   * Sự kiện nhấp dấu phân cách
   */
  function separatorClick() {
    hiddenVisible('separator');
    const separator: any = document.querySelector(".jeecg-menu-item-separator");
    const separatorOption = separator.querySelector(".options");
    separatorOption.classList.add('visible');
    setDropdownContentPosition(separator, separatorOption);
  }

  /**
   * Sự kiện xác nhận đường phân cách
   */
  function separatorOk() {
    const separators: any = document.querySelector(".jeecg-menu-item-separator");
    const separatorOption = separators.querySelector(".options");
    separatorOption.classList.remove('visible');
    console.log("Đã vào đây1111")
    separator();
  }
  
  /**
   * Sự kiện hủy đường phân cách
   */
  function separatorCancel() {
    separatorOptionDom.value.classList.remove('visible');
    formState.separatorColor = '#000000';
    formState.separatorLineWidth = 1;
    formState.separator = '0,0';
    separatorOptionDom.value.querySelectorAll('li') .forEach(li => li.classList.remove('active'));
  }

  /**
   * Sự kiện chọn thả xuống của đường phân cách
   * @param value
   */
  function separatorActiveClick(value) {
    separatorOptionDom.value.querySelectorAll('li') .forEach(li => li.classList.remove('active'));
    const curSeparatorDom:any = separatorOptionDom.value.querySelector(`[data-separator='${value}']`);
    if (curSeparatorDom) {
      formState.separator = value;
      curSeparatorDom.classList.add('active');
    }
  }
  
  
  onUnmounted(() => {
    // Trước khi component bị gỡ, loại bỏ event listener để tránh rò rỉ bộ nhớ
    document.removeEventListener('click', handleClickOutside);
    // Loại bỏ sự kiện bàn phím
    document.removeEventListener('keydown', handleKeyDown);
  });
</script>

<style scoped lang="scss" src="./style/DocDesign.css"></style>
