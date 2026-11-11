import Editor from './canvas-editor.js';
import { reactive, ref } from 'vue';
import { debounce } from "lodash-es";
import { getFileAccessHttpUrl } from "@/utils/common/compUtils";

let instance: any = null;

const formState = reactive({
  family: '微软雅黑',
  size: 'Tiểu 4',
  name: 'wordChỉnh sửa trực tuyến',
  title: 'Nội dung chính',
  highlightColor: '#ffffff00',
  showCataLogDom: true,
  separatorColor: '#000000',
  separator: '0,0',
  separatorLineWidth: 1,
});

const isApple = typeof navigator !== 'undefined' && /Mac OS X/.test(navigator.userAgent);

//Phần tử DOM của kết quả tìm kiếm
const searchCollapseDom = ref<any>();
const searchInputDom = ref<any>();
const searchResultDom = ref<any>();
//Phần tử DOM của văn bản thay thế
const replaceInputDom = ref<any>();

/**
 * Tạo trình soạn thảo
 */
export async function createEditor(content: any) {
  let querySelector = document.querySelector('.jeecg__editor');
  // Khởi tạo trình soạn thảo
  const editorData = {
    header: content.header ? JSON.parse(content.header) : [],
    main: content.main ? JSON.parse(content.main) : [],
    footer: content.footer ? JSON.parse(content.footer) : [],
  };
  formState.name = content.name || 'wordChỉnh sửa trực tuyến';
  instance = new Editor(querySelector, editorData, {  scrollContainerSelector: ".editor-container", pageNumber: { format: "Thứ{pageNo}trang/Tổng cộng{pageCount}trang"} });
  settingPaper(content);
  const contextMenuList = await instance.register.getContextMenuList();
  // Sửa ví dụ menu chuột phải nội bộ
  //Lưu thành hình ảnh là dạng base64 nên đã vô hiệu hóa
  contextMenuList.forEach((menu) => {
    // Tìm mục menu theo key menu rồi sửa thuộc tính
    if (menu.key === 'imageChange') {
      menu.when = () => false;
    }
  });
  // Toàn màn hình
  const fullscreenDom:any = document.querySelector('.fullscreen')
  fullscreenDom.onclick = toggleFullscreen
  window.addEventListener('keydown', (evt) => {
    if (evt.key === 'F11') {
      toggleFullscreen();
      evt.preventDefault();
    }
  });
  document.addEventListener('fullscreenchange', () => {
    fullscreenDom.classList.toggle('exist')
  })
  function toggleFullscreen() {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  }
  //Thêm listener
  addListener();
  //Thêm trình lắng nghe khi trang hiển thị thay đổi
  addPageNoListListener();
  //Thêm listener theo dõi sự thay đổi của số trang hiện tại
  addPageSizeListener();
  //Thêm trình lắng nghe khi trang hiện tại thay đổi
  addIntersectionPageNoListener();
  //Lắng nghe sự thay đổi tỷ lệ thu phóng của trang hiện tại
  addPageScaleListener();
  //Lắng nghe thay đổi chế độ trang
  addPageModeListener();
  //Lắng nghe số từ
  addWordCountListener();
}

/**
 * Thêm listener
 */
function addListener() {
  //Phông chữ
  const fontDom: any = document.querySelector('.jeecg-menu-item-font');
  const fontSelectDom = fontDom.querySelector('.select');
  const fontOptionDom = fontDom.querySelector('.options');
  //Cỡ chữ
  const jeecgSizeDom: any = document.querySelector('.jeecg-menu-item-size');
  const sizeOptionDom = jeecgSizeDom.querySelector('.options');
  //Gạch chân
  const underlineDom: any = document.querySelector('.jeecg-menu-item-underline');
  //In đậm
  const boldDom: any = document.querySelector('.jeecg-menu-item-bold');
  //Nghiêng
  const italicDom: any = document.querySelector('.jeecg-menu-item-italic');
  //Gạch ngang
  const strikeoutDom: any = document.querySelector('.jeecg-menu-item-strikeout');
  //Chỉ số trên
  const subscriptDom: any = document.querySelector('.jeecg-menu-item-subscript');
  //Chỉ số dưới
  const superscriptDom: any = document.querySelector('.jeecg-menu-item-superscript');
  //Màu sắc
  const colorControlDom:any = document.querySelector('#color');
  const colorDom:any = document.querySelector('.jeecg-menu-item-color');
  const colorSpanDom:any = colorDom.querySelector('span');
  //Căn trái
  const leftDom:any = document.querySelector('.jeecg-menu-item-left');
  //Căn giữa
  const centerDom:any = document.querySelector('.jeecg-menu-item-center');
  //Căn phải
  const rightDom:any = document.querySelector('.jeecg-menu-item-right');
  //Căn đều hai bên
  const alignmentDom:any = document.querySelector('.jeecg-menu-item-alignment');
  //Khoảng cách dòng
  const rowMarginDom:any = document.querySelector('.jeecg-menu-item-row-margin');
  const rowOptionDom = rowMarginDom.querySelector('.options')
  //Màu nổi bật
  const highlightDom:any = document.querySelector('.jeecg-menu-item-highlight');
  const highlightSpanDom = highlightDom.querySelector('span');
  const highlightControlDom:any = document.querySelector('#highlight');
  //Tiêu đề
  const titleDom:any = document.querySelector('.jeecg-menu-item-title')
  const titleOptionDom = titleDom.querySelector('.options');
  //Danh sách
  const listDom:any = document.querySelector('.jeecg-menu-item-list');
  const listOptionDom = listDom.querySelector('.options');
  //Đường phân cách
  const separatorDom:any = document.querySelector('.jeecg-menu-item-separator');
  const separatorOptionDom = separatorDom.querySelector('.options');
  //Tìm kiếm
  searchCollapseDom.value = document.querySelector('.jeecg-menu-item-search-collapse');
  searchResultDom.value = searchCollapseDom.value.querySelector('.search-result');
  replaceInputDom.value = document.querySelector('.jeecg-menu-item-search-collapse-replace input')
  searchInputDom.value = document.querySelector('.jeecg-menu-item-search-collapse-search input')
  // Lắng nghe sự kiện nội bộ
  instance.listener.rangeStyleChange = function (payload) {
    let undoDom: any = document.querySelector('.jeecg-menu-item-undo');
    // Hoàn tác
    if (undoDom && undoDom.classList) {
      payload.undo ? undoDom.classList.remove('no-allow') : undoDom.classList.add('no-allow');
    }
    // Làm lại
    let redoDom: any = document.querySelector('.jeecg-menu-item-redo');
    if (redoDom && redoDom.classList) {
      payload.redo ? redoDom.classList.remove('no-allow') : redoDom.classList.add('no-allow');
    }
    let painterDom = document.querySelector('.jeecg-menu-item-painter');
    // Cọ định dạng
    if (painterDom && painterDom.classList) {
      payload.painter ? painterDom.classList.add('active') : painterDom.classList.remove('active');
    }
    //Phông chữ
    fontOptionDom.querySelectorAll('li').forEach((li) => li.classList.remove('active'));
    const curFontDom = fontOptionDom.querySelector(`[data-family='${payload.font}']`);
    if (curFontDom) {
      formState.family = curFontDom.innerText;
      fontSelectDom.style.fontFamily = payload.font;
      curFontDom.classList.add('active');
    }

    //Cỡ chữ
    sizeOptionDom.querySelectorAll('li').forEach((li) => li.classList.remove('active'));
    const curSizeDom = sizeOptionDom.querySelector(`[data-size='${payload.size}']`);
    if (curSizeDom) {
      formState.size = curSizeDom.innerText;
      curSizeDom.classList.add('active');
    } else {
      formState.size = `${payload.size}`;
    }
    //In đậm
    if (boldDom && boldDom.classList) {
      payload.bold ? boldDom.classList.add('active') : boldDom.classList.remove('active');
    }
    //Nghiêng
    if (italicDom && italicDom.classList) {
      payload.italic ? italicDom.classList.add('active') : italicDom.classList.remove('active');
    }

    //Gạch chân
    if (underlineDom && underlineDom.classList) {
      payload.underline ? underlineDom.classList.add('active') : underlineDom.classList.remove('active');
    }
    //Gạch ngang
    if (strikeoutDom && strikeoutDom.classList) {
      payload.strikeout ? strikeoutDom.classList.add('active') : strikeoutDom.classList.remove('active');
    }
    //Chỉ số trên
    payload.type === 'superscript' ? superscriptDom.classList.add('active') : superscriptDom.classList.remove('active');

    //Chỉ số dưới
    payload.type === 'subscript' ? subscriptDom.classList.add('active') : subscriptDom.classList.remove('active');

    //Màu chữ
    if (colorDom && colorDom.classList) {
      if (payload.color) {
        colorDom.classList.add('active');
        colorControlDom.value = payload.color;
        colorSpanDom.style.backgroundColor = payload.color;
      } else {
        colorDom.classList.remove('active');
        colorControlDom.value = '#000000';
        colorSpanDom.style.backgroundColor = '#000000';
      }
    }
    
    //Cách căn chỉnh
    leftDom.classList.remove('active')
    centerDom.classList.remove('active')
    rightDom.classList.remove('active')
    alignmentDom.classList.remove('active')
    if (payload.rowFlex && payload.rowFlex === 'right') {
      rightDom.classList.add('active')
    } else if (payload.rowFlex && payload.rowFlex === 'center') {
      centerDom.classList.add('active')
    } else if (payload.rowFlex && payload.rowFlex === 'alignment') {
      alignmentDom.classList.add('active')
    } else {
      leftDom.classList.add('active')
    }
    //Khoảng cách dòng
    rowOptionDom.querySelectorAll('li').forEach(li => li.classList.remove('active'))
    const curRowMarginDom = rowOptionDom.querySelector(
      `[data-rowmargin='${payload.rowMargin}']`
    )
    curRowMarginDom.classList.add('active')
    
    //Màu nổi bật
    if (payload.highlight) {
      highlightDom.classList.add('active')
      formState.highlightColor = payload.highlight
      highlightControlDom.value = payload.highlight
      highlightSpanDom.style.backgroundColor = payload.highlight
    } else {
      highlightDom.classList.remove('active')
      formState.highlightColor = '#ffffff00';
      highlightControlDom.value = '#ffffff00';
      highlightSpanDom.style.backgroundColor = '#ffffff00'
    }
    
    // Tiêu đề
    titleOptionDom.querySelectorAll('li') .forEach(li => li.classList.remove('active'));
    if (payload.level) {
      const curTitleDom = titleOptionDom.querySelector(`[data-level='${payload.level}']` );
      formState.title = curTitleDom.innerText;
      curTitleDom.classList.add('active');
    } else {
      formState.title = 'Nội dung chính';
      titleOptionDom.querySelector('li:first-child').classList.add('active');
    }
    
    //Danh sách
    listOptionDom.querySelectorAll('li') .forEach(li => li.classList.remove('active'));
    if (payload.listType) {
      listDom.classList.add('active')
      const listType = payload.listType
      const listStyle = payload.listType === 'ol' ? 'decimal' :payload.listStyle;
      const curListDom = listOptionDom.querySelector( `[data-list-type='${listType}'][data-list-style='${listStyle}']`);
      if (curListDom) {
        curListDom.classList.add('active');
      }
    } else {
      listDom.classList.remove('active');
    }
    //Đường phân cách
    separatorOptionDom.querySelectorAll('li') .forEach(li => li.classList.remove('active'))
    if (payload.type === 'separator') {
      const separator = payload.dashArray.join(',') || '0,0'
      formState.separator = separator;
      const curSeparatorDom = separatorOptionDom.querySelector(`[data-separator='${separator}']`)
      if (curSeparatorDom) {
        curSeparatorDom.classList.add('active');
      }
      if(payload.color) {
        formState.separatorColor = payload.color;
      } else {
        formState.separatorColor = "#000000";
      }
      if(payload.lineWidth) {
        formState.separatorLineWidth = payload.lineWidth;
      } else {
        formState.separatorLineWidth = 1;
      }
    }
  };
}

/**
 * Trang hiển thị đã thay đổi
 */
function addPageNoListListener() {
  instance.listener.visiblePageNoListChange = function (payload) {
    const text = payload.map(i => i + 1).join('、');
    let querySelector:any = document.querySelector('.page-no-list');
    querySelector.innerText = text
  }
}

/**
 * Số trang hiện tại thay đổi
 */
function addPageSizeListener(){
  instance.listener.pageSizeChange = function (payload) {
    let querySelector:any = document.querySelector('.page-size');
    querySelector!.innerText = `${payload}`
  }
}

/**
 * Thêm: Trang hiện tại thay đổi
 */
function addIntersectionPageNoListener(){
  instance.listener.intersectionPageNoChange = function (payload) {
    let querySelector:any = document.querySelector('.page-no');
    querySelector.innerText = `${ payload + 1 }`
  }
}

/**
 * Tỷ lệ thu phóng của trang hiện tại thay đổi
 */
function addPageScaleListener(){
  instance.listener.pageScaleChange = function (payload) {
    let querySelector:any = document.querySelector('.page-scale-percentage');
    querySelector.innerText = `${Math.floor(payload * 10 * 10)}%`
  }
}

/**
 * Chế độ trang đã thay đổi
 */
function addPageModeListener(){
  const pageModeDom:any = document.querySelector('.page-mode')
  const pageModeOptionsDom = pageModeDom.querySelector('.options')
  instance.listener.pageModeChange = function (payload) {
    const activeMode = pageModeOptionsDom.querySelector(
      `[data-page-mode='${payload}']`
    )
    pageModeOptionsDom.querySelectorAll('li').forEach(li => li.classList.remove('active'))
    activeMode.classList.add('active')
  }
}

/**
 * Số ký tự
 */
const handleContentChange = async function () {
  // Số ký tự
  const wordCount = await instance.command.getWordCount();
  let querySelector:any = document.querySelector('.word-count');
  querySelector.innerText = `${ wordCount || 0 }`;
  if(formState.showCataLogDom){
    updateCatalog();
  }
}

/**
 * Lắng nghe số từ
 */
async function addWordCountListener(){
  instance.listener.contentChange = debounce(handleContentChange,200);
}

/**
 * Thiết lập giấy
 * @param content
 */
function settingPaper(content) {
  if(content.width && content.height){
    //Đặt khổ giấy và hiển thị lại
    instance.command.executePaperSize(content.width,content.height);
    const paperSizeDom:any = document.querySelector('.paper-size');
    const paperSizeDomOptionsDom = paperSizeDom.querySelector('.options')
    let pagers = paperSizeDomOptionsDom.querySelectorAll('li');
    for (let index = 0; index < pagers.length; index++) {
      let element = pagers[index];
      element.classList.remove('active')
      if(element.dataset.paperSize == (content.height+"*"+content.width)||element.dataset.paperSize == (content.width+"*"+content.height)) {
        element.classList.add('active')
      }
    }
    //Đặt hướng giấy và hiển thị lại
    if(content.paperDirection){
      let pageDirection = content.paperDirection;
      instance.command.executePaperDirection(pageDirection);
      const paperDirectionDom:any = document.querySelector('.paper-direction')
      const paperDirectionDomOptionsDom = paperDirectionDom.querySelector('.options')
      let pagerDirections = paperDirectionDomOptionsDom.querySelectorAll('li')
      for (let index = 0; index < pagerDirections.length; index++) {
        let element = pagerDirections[index];
        element.classList.remove('active')
        if(element.dataset.paperDirection ==  pageDirection){
          element.classList.add('active')
        }
      }
    }
    //Thiết lập watermark
    let watermark = content.watermark;
    if(watermark){
      let watermarkObj = JSON.parse(watermark);
      instance.command.executeAddWatermark(watermarkObj)
    }
    //Thiết lập lề
    let margins = content.margins;
    if(margins && margins != "[]"){
      margins = JSON.parse(margins);
      instance.command.executeSetPaperMargin([
        Number(margins[0]),
        Number(margins[1]),
        Number(margins[2]),
        Number(margins[3])
      ])
    }
  }
}

/**
 * Nhật ký cập nhật
 */
async function updateCatalog() {
  const catalog = await instance.command.getCatalog()
  const catalogMainDom:any = document.querySelector('.jeecg-catalog-main')
  catalogMainDom.innerHTML = ''
  if (catalog) {
    const appendCatalog = (
      parent,
      catalogItems
    ) => {
      for (let c = 0; c < catalogItems.length; c++) {
        const catalogItem = catalogItems[c]
        const catalogItemDom = document.createElement('div')
        catalogItemDom.classList.add('catalog-item')
        // Render
        const catalogItemContentDom = document.createElement('div')
        catalogItemContentDom.classList.add('jeecg-catalog-item-content')
        const catalogItemContentSpanDom = document.createElement('span')
        catalogItemContentSpanDom.innerText = catalogItem.name
        catalogItemContentDom.append(catalogItemContentSpanDom)
        // Định vị
        catalogItemContentDom.onclick = () => {
          instance.command.executeLocationCatalog(catalogItem.id)
        }
        catalogItemDom.append(catalogItemContentDom)
        if (catalogItem.subCatalog && catalogItem.subCatalog.length) {
          appendCatalog(catalogItemDom, catalogItem.subCatalog)
        }
        // Thêm vào
        parent.append(catalogItemDom)
      }
    }
    appendCatalog(catalogMainDom, catalog)
  }
}

const SURROGATE_PAIR_REG = /[\uD800-\uDBFF][\uDC00-\uDFFF]/ // unicodeCặp đại diện (surrogate pair)
const EMOJI_REG =
  /[#*0-9]\uFE0F?\u20E3|[\xA9\xAE\u203C\u2049\u2122\u2139\u2194-\u2199\u21A9\u21AA\u231A\u231B\u2328\u23CF\u23ED-\u23EF\u23F1\u23F2\u23F8-\u23FA\u24C2\u25AA\u25AB\u25B6\u25C0\u25FB\u25FC\u25FE\u2600-\u2604\u260E\u2611\u2614\u2615\u2618\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638-\u263A\u2640\u2642\u2648-\u2653\u265F\u2660\u2663\u2665\u2666\u2668\u267B\u267E\u267F\u2692\u2694-\u2697\u2699\u269B\u269C\u26A0\u26A7\u26AA\u26B0\u26B1\u26BD\u26BE\u26C4\u26C8\u26CF\u26D1\u26E9\u26F0-\u26F5\u26F7\u26F8\u26FA\u2702\u2708\u2709\u270F\u2712\u2714\u2716\u271D\u2721\u2733\u2734\u2744\u2747\u2757\u2763\u27A1\u2934\u2935\u2B05-\u2B07\u2B1B\u2B1C\u2B55\u3030\u303D\u3297\u3299]\uFE0F?|[\u261D\u270C\u270D](?:\uFE0F|\uD83C[\uDFFB-\uDFFF])?|[\u270A\u270B](?:\uD83C[\uDFFB-\uDFFF])?|[\u23E9-\u23EC\u23F0\u23F3\u25FD\u2693\u26A1\u26AB\u26C5\u26CE\u26D4\u26EA\u26FD\u2705\u2728\u274C\u274E\u2753-\u2755\u2795-\u2797\u27B0\u27BF\u2B50]|\u26D3\uFE0F?(?:\u200D\uD83D\uDCA5)?|\u26F9(?:\uFE0F|\uD83C[\uDFFB-\uDFFF])?(?:\u200D[\u2640\u2642]\uFE0F?)?|\u2764\uFE0F?(?:\u200D(?:\uD83D\uDD25|\uD83E\uDE79))?|\uD83C(?:[\uDC04\uDD70\uDD71\uDD7E\uDD7F\uDE02\uDE37\uDF21\uDF24-\uDF2C\uDF36\uDF7D\uDF96\uDF97\uDF99-\uDF9B\uDF9E\uDF9F\uDFCD\uDFCE\uDFD4-\uDFDF\uDFF5\uDFF7]\uFE0F?|[\uDF85\uDFC2\uDFC7](?:\uD83C[\uDFFB-\uDFFF])?|[\uDFC4\uDFCA](?:\uD83C[\uDFFB-\uDFFF])?(?:\u200D[\u2640\u2642]\uFE0F?)?|[\uDFCB\uDFCC](?:\uFE0F|\uD83C[\uDFFB-\uDFFF])?(?:\u200D[\u2640\u2642]\uFE0F?)?|[\uDCCF\uDD8E\uDD91-\uDD9A\uDE01\uDE1A\uDE2F\uDE32-\uDE36\uDE38-\uDE3A\uDE50\uDE51\uDF00-\uDF20\uDF2D-\uDF35\uDF37-\uDF43\uDF45-\uDF4A\uDF4C-\uDF7C\uDF7E-\uDF84\uDF86-\uDF93\uDFA0-\uDFC1\uDFC5\uDFC6\uDFC8\uDFC9\uDFCF-\uDFD3\uDFE0-\uDFF0\uDFF8-\uDFFF]|\uDDE6\uD83C[\uDDE8-\uDDEC\uDDEE\uDDF1\uDDF2\uDDF4\uDDF6-\uDDFA\uDDFC\uDDFD\uDDFF]|\uDDE7\uD83C[\uDDE6\uDDE7\uDDE9-\uDDEF\uDDF1-\uDDF4\uDDF6-\uDDF9\uDDFB\uDDFC\uDDFE\uDDFF]|\uDDE8\uD83C[\uDDE6\uDDE8\uDDE9\uDDEB-\uDDEE\uDDF0-\uDDF5\uDDF7\uDDFA-\uDDFF]|\uDDE9\uD83C[\uDDEA\uDDEC\uDDEF\uDDF0\uDDF2\uDDF4\uDDFF]|\uDDEA\uD83C[\uDDE6\uDDE8\uDDEA\uDDEC\uDDED\uDDF7-\uDDFA]|\uDDEB\uD83C[\uDDEE-\uDDF0\uDDF2\uDDF4\uDDF7]|\uDDEC\uD83C[\uDDE6\uDDE7\uDDE9-\uDDEE\uDDF1-\uDDF3\uDDF5-\uDDFA\uDDFC\uDDFE]|\uDDED\uD83C[\uDDF0\uDDF2\uDDF3\uDDF7\uDDF9\uDDFA]|\uDDEE\uD83C[\uDDE8-\uDDEA\uDDF1-\uDDF4\uDDF6-\uDDF9]|\uDDEF\uD83C[\uDDEA\uDDF2\uDDF4\uDDF5]|\uDDF0\uD83C[\uDDEA\uDDEC-\uDDEE\uDDF2\uDDF3\uDDF5\uDDF7\uDDFC\uDDFE\uDDFF]|\uDDF1\uD83C[\uDDE6-\uDDE8\uDDEE\uDDF0\uDDF7-\uDDFB\uDDFE]|\uDDF2\uD83C[\uDDE6\uDDE8-\uDDED\uDDF0-\uDDFF]|\uDDF3\uD83C[\uDDE6\uDDE8\uDDEA-\uDDEC\uDDEE\uDDF1\uDDF4\uDDF5\uDDF7\uDDFA\uDDFF]|\uDDF4\uD83C\uDDF2|\uDDF5\uD83C[\uDDE6\uDDEA-\uDDED\uDDF0-\uDDF3\uDDF7-\uDDF9\uDDFC\uDDFE]|\uDDF6\uD83C\uDDE6|\uDDF7\uD83C[\uDDEA\uDDF4\uDDF8\uDDFA\uDDFC]|\uDDF8\uD83C[\uDDE6-\uDDEA\uDDEC-\uDDF4\uDDF7-\uDDF9\uDDFB\uDDFD-\uDDFF]|\uDDF9\uD83C[\uDDE6\uDDE8\uDDE9\uDDEB-\uDDED\uDDEF-\uDDF4\uDDF7\uDDF9\uDDFB\uDDFC\uDDFF]|\uDDFA\uD83C[\uDDE6\uDDEC\uDDF2\uDDF3\uDDF8\uDDFE\uDDFF]|\uDDFB\uD83C[\uDDE6\uDDE8\uDDEA\uDDEC\uDDEE\uDDF3\uDDFA]|\uDDFC\uD83C[\uDDEB\uDDF8]|\uDDFD\uD83C\uDDF0|\uDDFE\uD83C[\uDDEA\uDDF9]|\uDDFF\uD83C[\uDDE6\uDDF2\uDDFC]|\uDF44(?:\u200D\uD83D\uDFEB)?|\uDF4B(?:\u200D\uD83D\uDFE9)?|\uDFC3(?:\uD83C[\uDFFB-\uDFFF])?(?:\u200D(?:[\u2640\u2642]\uFE0F?(?:\u200D\u27A1\uFE0F?)?|\u27A1\uFE0F?))?|\uDFF3\uFE0F?(?:\u200D(?:\u26A7\uFE0F?|\uD83C\uDF08))?|\uDFF4(?:\u200D\u2620\uFE0F?|\uDB40\uDC67\uDB40\uDC62\uDB40(?:\uDC65\uDB40\uDC6E\uDB40\uDC67|\uDC73\uDB40\uDC63\uDB40\uDC74|\uDC77\uDB40\uDC6C\uDB40\uDC73)\uDB40\uDC7F)?)|\uD83D(?:[\uDC3F\uDCFD\uDD49\uDD4A\uDD6F\uDD70\uDD73\uDD76-\uDD79\uDD87\uDD8A-\uDD8D\uDDA5\uDDA8\uDDB1\uDDB2\uDDBC\uDDC2-\uDDC4\uDDD1-\uDDD3\uDDDC-\uDDDE\uDDE1\uDDE3\uDDE8\uDDEF\uDDF3\uDDFA\uDECB\uDECD-\uDECF\uDEE0-\uDEE5\uDEE9\uDEF0\uDEF3]\uFE0F?|[\uDC42\uDC43\uDC46-\uDC50\uDC66\uDC67\uDC6B-\uDC6D\uDC72\uDC74-\uDC76\uDC78\uDC7C\uDC83\uDC85\uDC8F\uDC91\uDCAA\uDD7A\uDD95\uDD96\uDE4C\uDE4F\uDEC0\uDECC](?:\uD83C[\uDFFB-\uDFFF])?|[\uDC6E\uDC70\uDC71\uDC73\uDC77\uDC81\uDC82\uDC86\uDC87\uDE45-\uDE47\uDE4B\uDE4D\uDE4E\uDEA3\uDEB4\uDEB5](?:\uD83C[\uDFFB-\uDFFF])?(?:\u200D[\u2640\u2642]\uFE0F?)?|[\uDD74\uDD90](?:\uFE0F|\uD83C[\uDFFB-\uDFFF])?|[\uDC00-\uDC07\uDC09-\uDC14\uDC16-\uDC25\uDC27-\uDC3A\uDC3C-\uDC3E\uDC40\uDC44\uDC45\uDC51-\uDC65\uDC6A\uDC79-\uDC7B\uDC7D-\uDC80\uDC84\uDC88-\uDC8E\uDC90\uDC92-\uDCA9\uDCAB-\uDCFC\uDCFF-\uDD3D\uDD4B-\uDD4E\uDD50-\uDD67\uDDA4\uDDFB-\uDE2D\uDE2F-\uDE34\uDE37-\uDE41\uDE43\uDE44\uDE48-\uDE4A\uDE80-\uDEA2\uDEA4-\uDEB3\uDEB7-\uDEBF\uDEC1-\uDEC5\uDED0-\uDED2\uDED5-\uDED7\uDEDC-\uDEDF\uDEEB\uDEEC\uDEF4-\uDEFC\uDFE0-\uDFEB\uDFF0]|\uDC08(?:\u200D\u2B1B)?|\uDC15(?:\u200D\uD83E\uDDBA)?|\uDC26(?:\u200D(?:\u2B1B|\uD83D\uDD25))?|\uDC3B(?:\u200D\u2744\uFE0F?)?|\uDC41\uFE0F?(?:\u200D\uD83D\uDDE8\uFE0F?)?|\uDC68(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?\uDC68|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D(?:[\uDC68\uDC69]\u200D\uD83D(?:\uDC66(?:\u200D\uD83D\uDC66)?|\uDC67(?:\u200D\uD83D[\uDC66\uDC67])?)|[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uDC66(?:\u200D\uD83D\uDC66)?|\uDC67(?:\u200D\uD83D[\uDC66\uDC67])?)|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]))|\uD83C(?:\uDFFB(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?\uDC68\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D\uDC68\uD83C[\uDFFC-\uDFFF])))?|\uDFFC(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?\uDC68\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D\uDC68\uD83C[\uDFFB\uDFFD-\uDFFF])))?|\uDFFD(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?\uDC68\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D\uDC68\uD83C[\uDFFB\uDFFC\uDFFE\uDFFF])))?|\uDFFE(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?\uDC68\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D\uDC68\uD83C[\uDFFB-\uDFFD\uDFFF])))?|\uDFFF(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?\uDC68\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D\uDC68\uD83C[\uDFFB-\uDFFE])))?))?|\uDC69(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:\uDC8B\u200D\uD83D)?[\uDC68\uDC69]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D(?:[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uDC66(?:\u200D\uD83D\uDC66)?|\uDC67(?:\u200D\uD83D[\uDC66\uDC67])?|\uDC69\u200D\uD83D(?:\uDC66(?:\u200D\uD83D\uDC66)?|\uDC67(?:\u200D\uD83D[\uDC66\uDC67])?))|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]))|\uD83C(?:\uDFFB(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:[\uDC68\uDC69]|\uDC8B\u200D\uD83D[\uDC68\uDC69])\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D[\uDC68\uDC69]\uD83C[\uDFFC-\uDFFF])))?|\uDFFC(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:[\uDC68\uDC69]|\uDC8B\u200D\uD83D[\uDC68\uDC69])\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D[\uDC68\uDC69]\uD83C[\uDFFB\uDFFD-\uDFFF])))?|\uDFFD(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:[\uDC68\uDC69]|\uDC8B\u200D\uD83D[\uDC68\uDC69])\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D[\uDC68\uDC69]\uD83C[\uDFFB\uDFFC\uDFFE\uDFFF])))?|\uDFFE(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:[\uDC68\uDC69]|\uDC8B\u200D\uD83D[\uDC68\uDC69])\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D[\uDC68\uDC69]\uD83C[\uDFFB-\uDFFD\uDFFF])))?|\uDFFF(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D\uD83D(?:[\uDC68\uDC69]|\uDC8B\u200D\uD83D[\uDC68\uDC69])\uD83C[\uDFFB-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83D[\uDC68\uDC69]\uD83C[\uDFFB-\uDFFE])))?))?|\uDC6F(?:\u200D[\u2640\u2642]\uFE0F?)?|\uDD75(?:\uFE0F|\uD83C[\uDFFB-\uDFFF])?(?:\u200D[\u2640\u2642]\uFE0F?)?|\uDE2E(?:\u200D\uD83D\uDCA8)?|\uDE35(?:\u200D\uD83D\uDCAB)?|\uDE36(?:\u200D\uD83C\uDF2B\uFE0F?)?|\uDE42(?:\u200D[\u2194\u2195]\uFE0F?)?|\uDEB6(?:\uD83C[\uDFFB-\uDFFF])?(?:\u200D(?:[\u2640\u2642]\uFE0F?(?:\u200D\u27A1\uFE0F?)?|\u27A1\uFE0F?))?)|\uD83E(?:[\uDD0C\uDD0F\uDD18-\uDD1F\uDD30-\uDD34\uDD36\uDD77\uDDB5\uDDB6\uDDBB\uDDD2\uDDD3\uDDD5\uDEC3-\uDEC5\uDEF0\uDEF2-\uDEF8](?:\uD83C[\uDFFB-\uDFFF])?|[\uDD26\uDD35\uDD37-\uDD39\uDD3D\uDD3E\uDDB8\uDDB9\uDDCD\uDDCF\uDDD4\uDDD6-\uDDDD](?:\uD83C[\uDFFB-\uDFFF])?(?:\u200D[\u2640\u2642]\uFE0F?)?|[\uDDDE\uDDDF](?:\u200D[\u2640\u2642]\uFE0F?)?|[\uDD0D\uDD0E\uDD10-\uDD17\uDD20-\uDD25\uDD27-\uDD2F\uDD3A\uDD3F-\uDD45\uDD47-\uDD76\uDD78-\uDDB4\uDDB7\uDDBA\uDDBC-\uDDCC\uDDD0\uDDE0-\uDDFF\uDE70-\uDE7C\uDE80-\uDE88\uDE90-\uDEBD\uDEBF-\uDEC2\uDECE-\uDEDB\uDEE0-\uDEE8]|\uDD3C(?:\u200D[\u2640\u2642]\uFE0F?|\uD83C[\uDFFB-\uDFFF])?|\uDDCE(?:\uD83C[\uDFFB-\uDFFF])?(?:\u200D(?:[\u2640\u2642]\uFE0F?(?:\u200D\u27A1\uFE0F?)?|\u27A1\uFE0F?))?|\uDDD1(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\uD83C[\uDF3E\uDF73\uDF7C\uDF84\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83E\uDDD1|\uDDD1\u200D\uD83E\uDDD2(?:\u200D\uD83E\uDDD2)?|\uDDD2(?:\u200D\uD83E\uDDD2)?))|\uD83C(?:\uDFFB(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D(?:\uD83D\uDC8B\u200D)?\uD83E\uDDD1\uD83C[\uDFFC-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF84\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83E\uDDD1\uD83C[\uDFFB-\uDFFF])))?|\uDFFC(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D(?:\uD83D\uDC8B\u200D)?\uD83E\uDDD1\uD83C[\uDFFB\uDFFD-\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF84\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83E\uDDD1\uD83C[\uDFFB-\uDFFF])))?|\uDFFD(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D(?:\uD83D\uDC8B\u200D)?\uD83E\uDDD1\uD83C[\uDFFB\uDFFC\uDFFE\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF84\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83E\uDDD1\uD83C[\uDFFB-\uDFFF])))?|\uDFFE(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D(?:\uD83D\uDC8B\u200D)?\uD83E\uDDD1\uD83C[\uDFFB-\uDFFD\uDFFF]|\uD83C[\uDF3E\uDF73\uDF7C\uDF84\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83E\uDDD1\uD83C[\uDFFB-\uDFFF])))?|\uDFFF(?:\u200D(?:[\u2695\u2696\u2708]\uFE0F?|\u2764\uFE0F?\u200D(?:\uD83D\uDC8B\u200D)?\uD83E\uDDD1\uD83C[\uDFFB-\uDFFE]|\uD83C[\uDF3E\uDF73\uDF7C\uDF84\uDF93\uDFA4\uDFA8\uDFEB\uDFED]|\uD83D[\uDCBB\uDCBC\uDD27\uDD2C\uDE80\uDE92]|\uD83E(?:[\uDDAF\uDDBC\uDDBD](?:\u200D\u27A1\uFE0F?)?|[\uDDB0-\uDDB3]|\uDD1D\u200D\uD83E\uDDD1\uD83C[\uDFFB-\uDFFF])))?))?|\uDEF1(?:\uD83C(?:\uDFFB(?:\u200D\uD83E\uDEF2\uD83C[\uDFFC-\uDFFF])?|\uDFFC(?:\u200D\uD83E\uDEF2\uD83C[\uDFFB\uDFFD-\uDFFF])?|\uDFFD(?:\u200D\uD83E\uDEF2\uD83C[\uDFFB\uDFFC\uDFFE\uDFFF])?|\uDFFE(?:\u200D\uD83E\uDEF2\uD83C[\uDFFB-\uDFFD\uDFFF])?|\uDFFF(?:\u200D\uD83E\uDEF2\uD83C[\uDFFB-\uDFFE])?))?)/g

/**
 * Tách văn bản
 * @param text
 */
function splitText(text){
  const UNICODE_SYMBOL_REG = new RegExp(
    `${EMOJI_REG.source}|${SURROGATE_PAIR_REG.source}`,
    'g'
  )
  const data:any = []
  const symbolMap:any = new Map();
  for (const match of text.matchAll(UNICODE_SYMBOL_REG)) {
    symbolMap.set(match.index, match[0])
  }
  let t = 0
  while (t < text.length) {
    const symbol = symbolMap.get(t)
    if (symbol) {
      data.push(symbol)
      t += symbol.length
    } else {
      data.push(text[t])
      t++
    }
  }
  return data
}

/**
 * Phương thức
 */
export function method() {
  /**
   * Xóa định dạng
   */
  function clearFormat() {
    instance.command.executeFormat();
  }

  /**
   * Hoàn tác
   */
  function undo() {
    instance.command.executeUndo();
  }

  /**
   * Làm lại
   */
  function redo() {
    instance.command.executeRedo();
  }

  let isFirstClick = true;
  let painterTimeout = 0;

  /**
   * Cọ định dạng (nhấp đơn)
   */
  function painterClick() {
    if (isFirstClick) {
      isFirstClick = false;
      painterTimeout = window.setTimeout(() => {
        isFirstClick = true;
        instance.command.executePainter({
          isDblclick: false,
        });
      }, 200);
    } else {
      window.clearTimeout(painterTimeout);
    }
  }

  /**
   * Nhấp đúp cọ định dạng
   */
  function painterDblClick() {
    isFirstClick = true;
    window.clearTimeout(painterTimeout);
    instance.command.executePainter({
      isDblclick: true,
    });
  }

  /**
   * Phông chữ
   * @param family
   * @param name
   */
  function font(family, name) {
    formState.family = name;
    instance.command.executeFont(family);
  }

  /**
   * Cỡ chữ
   * @param size
   * @param name
   */
  function fontSize(size, name) {
    formState.size = name;
    instance.command.executeSize(Number(size));
  }

  /**
   * Phông chữ+
   */
  function sizeAdd() {
    instance.command.executeSizeAdd();
  }

  /**
   * Phông chữ-
   */
  function sizeMinus() {
    instance.command.executeSizeMinus();
  }

  /**
   * In đậm
   */
  function blob() {
    instance.command.executeBold();
  }

  /**
   * Nghiêng
   */
  function itailc() {
    instance.command.executeItalic();
  }

  /**
   * Gạch chân
   * @param value
   */
  function underline(value) {
    //Gạch chân
    const underlineDom: any = document.querySelector('.jeecg-menu-item-underline');
    const underlineOptionDom = underlineDom.querySelector('.options');
    if(underlineOptionDom && underlineOptionDom.classList){
      underlineOptionDom.classList.remove('visible');
    }
    if (value) {
      instance.command.executeUnderline({
        style: value,
      });
    } else {
      instance.command.executeUnderline({
        style: '',
      });
    }
  }

  /**
   * Gạch ngang
   */
  function strikeout() {
    instance.command.executeStrikeout();
  }

  /**
   * Chỉ số trên
   */
  function superscript() {
    instance.command.executeSuperscript();
  }

  /**
   * Chỉ số dưới
   */
  function subscript() {
    instance.command.executeSubscript();
  }

  /**
   * Màu sắc
   * @param e
   */
  function color(e) {
    instance.command.executeColor(e.target.value);
  }

  /**
   * Màu nền ô
   * @param e
   */
  function cellColor(e) {
    instance.command.executeTableTdBackgroundColor(e.target.value);
  }

  /**
   * Phân trang liên tục
   * @param value
   */
  function pageMode(value) {
    instance.command.executePageMode(value)
  }

  /**
   * Phóng to
   */
  function scaleAdd() {
    instance.command.executePageScaleAdd();
  }

  /**
   * Thu nhỏ
   */
  function scaleMiuns() {
    instance.command.executePageScaleMinus();
  }

  /**
   * Khổ giấy
   * @param value
   */
  function paperSize(value) {
    const [width, height] = value.split('*').map(Number);
    instance.command.executePaperSize(width, height);
    // Hiển thị lại trạng thái giấy
    const paperSizeDom:any = document.querySelector('.paper-size');
    const paperSizeDomOptionsDom = paperSizeDom.querySelector('.options');
    paperSizeDomOptionsDom.querySelectorAll('li').forEach(child => child.classList.remove('active'));
    let querySelector:any = paperSizeDomOptionsDom.querySelector( `[data-paper-size='${value}']` );
    querySelector.classList.add('active')
  }

  /**
   * Hướng giấy
   */
  function paperDirection(value) {
    instance.command.executePaperDirection(value);
    // Hiển thị lại trạng thái hướng giấy
    const paperDirectionDom:any = document.querySelector('.paper-direction');
    const paperDirectionDomOptionsDom =  paperDirectionDom.querySelector('.options');
    paperDirectionDomOptionsDom.querySelectorAll('li').forEach(child => child.classList.remove('active'));
    let querySelector:any = paperDirectionDomOptionsDom.querySelector( `[data-paper-direction='${value}']`);
    querySelector.classList.add('active');
  }

  /**
   * In
   */
  function printTemplate() {
    instance.command.executePrint();
  }

  /**
   * Lấy lề
   */
  function getPaperMargin() {
    const [topMargin, rightMargin, bottomMargin, leftMargin] = instance.command.getPaperMargin();
    return {
      marginTop: topMargin,
      marginBottom: bottomMargin,
      marginLeft: leftMargin,
      marginRight: rightMargin,
    }
  }

  /**
   * Thiết lập lề
   * @param values
   */
  function setPaperMargin(values) {
    instance.command.executeSetPaperMargin([
      values.marginTop,
      values.marginRight,
      values.marginBottom,
      values.marginLeft
    ])
  }

  /**
   * Thiết lập hình ảnh
   * @param url
   */
  function setWordImage(url) {
    let options = instance.command.getOptions();
    let paperDirection = options.paperDirection;
    let pageWidth = options.width;
    let pageHeight = options.height;
    if(paperDirection == "horizontal"){
      pageWidth = options.height;
      pageHeight = options.width;
    }
    const img = new Image();
    img.src =url;
    img.onload = () => {
      let width = img.width || 0;
      let height = img.height || 0;
      width = width>pageWidth?pageWidth:width;
      height = height>pageHeight?pageHeight:height;
      instance.command.executeImage({
        value: url,
        width: width,
        height: height
      })
    };
  }

  /**
   * Nội dung gán khi tải tệp lên
   */
  async function setWordContent(content) {
    if(content){
      let main = JSON.parse(content.main);
      //Đặt image có thể truy cậpimage
      main = await setViewImage(main,'import');
      instance.command.executeSetValue({
        "header":  JSON.parse(content.header) || [],
        "main": main,
        "footer": JSON.parse(content.footer) || []
      });
      settingPaper(content);
    }
  }

  /**
   * Đặt image có thể truy cậpimage
   * @param main
   * @param type
   */
  function setViewImage(main,type) {
    for (const item of main) {
      if(item.type === 'image'){
        if(type === 'original'){
          if(item['oldValue']){
            item.value = item['oldValue'];
          }
        }else{
          item['oldValue'] = item.value;
          item.value = getFileAccessHttpUrl(item.value);
        }
      }
      if(item.trList && item.trList.length>0){
        for (const tr of item.trList) {
          if(tr.tdList && tr.tdList.length>0){
            let tdList = tr.tdList;
            for (const td of tdList) {
              let tdValue = td.value;
              if(tdValue && tdValue.length>0){
                for (const itemValue of tdValue) {
                  if(itemValue.type === 'image'){
                    if(type === 'original'){
                      if(itemValue.oldValue){
                        itemValue.value = itemValue['oldValue'];
                      }
                    }else{
                      itemValue['oldValue'] = itemValue.value;
                      itemValue.value = getFileAccessHttpUrl(itemValue.value);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return main;
  }

  /**
   * Căn trái
   */
  function leftAlign() {
    instance.command.executeRowFlex("left");
  } 
  
  /**
   * Căn giữa
   */
  function centerAlign() {
    instance.command.executeRowFlex("center");
  }

  /**
   * Căn phải
   */
  function rightAlign() {
    instance.command.executeRowFlex("right");
  }  
  
  /**
   * Căn đều hai bên
   */
  function alignmentAlign() {
    instance.command.executeRowFlex("alignment");
  }

  /**
   * Khoảng cách dòng
   * @param value
   */
  function rowMargin(value) {
    instance.command.executeRowMargin(Number(value));
  }

  /**
   * Dấu ngắt trang
   */
  function pageBreak() {
    instance.command.executePageBreak();
  }

  /**
   * Đặt màu làm nổi bật
   * @param value
   */
  function setHighlightColor(value) {
    let highlightColor = value.highlightColor;
    if(highlightColor) {
      instance.command.executeHighlight(highlightColor)
    }
  }

  /**
   * Tiêu đề
   * @param value
   */
  function title(value) {
    instance.command.executeTitle(value || null);
  }

  /**
   * Danh sách
   * @param listType
   * @param listStyle
   */
  function list(listType,listStyle) {
    instance.command.executeList(listType, listStyle);
  }

  /**
   * Bảng
   * @param rowIndex
   * @param colIndex
   * @param recoveryTable
   */
  function tabCell(rowIndex,colIndex,recoveryTable) {
    instance.command.executeInsertTable(rowIndex, colIndex);
    recoveryTable();
  }

  /**
   * Đường phân cách
   * @param value
   */
  function separator() {
    let payload:any = [];
    const separatorDash = formState.separator?formState.separator?.split(',').map(Number): "0,0".split(',').map(Number);
    if (separatorDash) {
      const isSingleLine = separatorDash.every(d => d === 0)
      if (!isSingleLine) {
        payload = separatorDash
      }
    }
    instance.command.executeSeparator(payload,formState.separatorLineWidth,formState.separatorColor);
  }

  /**
   * Siêu liên kết
   * @param value
   */
  function setHyperlink(value) {
    instance.command.executeHyperlink({
      type: 'hyperlink',
      value: '',
      url:value.url,
      valueList: splitText(value.name).map(n => ({
        value: n,
        size: 16
      }))
    })
  }

  /**
   * Lấy siêu liên kết
   */
  function getHyperlink() {
    let rangeText = instance.command.getRangeText();
    return {
      name: rangeText,
      url: ''
    }
  }

  /**
   * Thiết lập watermark
   * @param watermark
   */
  function setWatermark(watermark) {
    const repeat = watermark.repeat === '1';
    instance.command.executeAddWatermark({
      data: watermark.data,
      color: watermark.color,
      size: watermark.size,
      opacity: watermark.opacity,
      repeat,
      gap:
        repeat && watermark.horizontalGap && watermark.verticalGap
          ? [
            watermark.horizontalGap,
            watermark.verticalGap
          ]
          : undefined
    });
  }

  /**
   * Xóa watermark
   */
  function deleteWatermarkClick() {
    instance.command.executeDeleteWatermark();
  }

  /**
   * Sự kiện nhập văn bản tìm kiếm
   * @param evt
   */
  function searchInput(evt) {
    let value = evt.target.value;
    instance.command.executeSearch(value || null);
    setSearchResult();
  }  
  
  /**
   * Sự kiện bàn phím văn bản tìm kiếm
   * @param evt
   */
  function searchKeyDown(evt) {
    if (evt.key === 'Enter') {
      let value = evt.target.value;
      instance.command.executeSearch(value || null);
      setSearchResult();
    }
  }
  
  /**
   * Mục trước
   */
  function arrowLeft() {
    instance.command.executeSearchNavigatePre();
    setSearchResult();
  }

  /**
   * Mục tiếp theo
   */
  function arrowRight() {
    instance.command.executeSearchNavigateNext();
    setSearchResult();
  }

  /**
   * Thay thế
   */
  function replaceText() {
    console.log("searchResultDom.value:::",searchResultDom.value)
    console.log("replaceInputDom.value:::",replaceInputDom.value)
    const searchValue = searchInputDom.value.value;
    const replaceValue = replaceInputDom.value.value;
    if (searchValue && replaceValue && searchValue !== replaceValue) {
      instance.command.executeReplace(replaceValue);
    }
  }

  /**
   * Đóng tìm kiếm
   */
  function closeSearch() {
    searchCollapseDom.value.style.display = 'none';
    replaceInputDom.value.value = '';
    searchInputDom.value.value = '';
    instance.command.executeSearch(null);
    setSearchResult();
  }

  /**
   * Đặt kết quả tìm kiếm
   */
  function setSearchResult() {
    const result = instance.command.getSearchNavigateInfo();
    if (result) {
      const { index, count } = result;
      searchResultDom.value.innerText = `${index}/${count}`;
    } else {
      searchResultDom.value.innerText = '';
    }
  }
  
  /**
   * Lưu
   */
  function save() {
    let tplSettings = instance.command.getValue();
    let options = instance.command.getOptions();
    let paperDirection = options.paperDirection;
    let paperMargin = instance.command.getPaperMargin();
    tplSettings.data.main = setViewImage(tplSettings.data.main,'original');
    return {
      header: JSON.stringify(tplSettings.data.header),
      main: JSON.stringify(tplSettings.data.main),
      footer: JSON.stringify(tplSettings.data.footer),
      paperDirection: paperDirection,
      watermark: JSON.stringify(tplSettings.options.watermark),
      margins: JSON.stringify(paperMargin),
      height: options.height,
      width: options.width,
    };
  }

  return {
    formState,
    clearFormat,
    undo,
    redo,
    isApple,
    painterClick,
    painterDblClick,
    font,
    fontSize,
    sizeAdd,
    sizeMinus,
    blob,
    itailc,
    underline,
    strikeout,
    superscript,
    subscript,
    color,
    save,
    pageMode,
    scaleAdd,
    scaleMiuns,
    paperSize,
    paperDirection,
    printTemplate,
    getPaperMargin,
    setPaperMargin,
    updateCatalog,
    setWordImage,
    setWordContent,
    leftAlign,
    centerAlign,
    rightAlign,
    alignmentAlign,
    rowMargin,
    pageBreak,
    cellColor,
    setHighlightColor,
    title,
    list,
    tabCell,
    separator,
    setHyperlink,
    getHyperlink,
    setWatermark,
    deleteWatermarkClick,
    arrowLeft,
    arrowRight,
    searchInput,
    searchKeyDown,
    replaceText,
    closeSearch,
  };
}
