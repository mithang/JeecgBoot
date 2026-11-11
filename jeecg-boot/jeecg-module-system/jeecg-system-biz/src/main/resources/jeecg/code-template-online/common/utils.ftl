<#---->
<#-- freemarker một số phương thức tiện ích của freemarker -->
<#---->
<#-- Chuyển camelCase thành ký tự khác -->
<#-- @param str       Văn bản cần chuyển đổi -->
<#-- @param character Ký tự cần chuyển đổi thành -->
<#-- @param case      Chuyển đổi chữ hoa/thường (normal không chuyển đổi, lower chữ thường, upper chữ hoa) -->
<#function camelToChar(str, character, case='normal')>
  <#assign text=str?replace("([a-z])([A-Z]+)","$1${character}$2","r")/>
  <#if case=="upper">
    <#return text?upper_case>
  <#elseif case=="lower">
    <#return text?lower_case>
  <#else>
    <#return text>
  </#if>
</#function>
<#--Chuyển gạch dưới sang camelCase-->
<#function dashedToCamel(str)>
    <#assign text=""/>
    <#assign strlist = str?split("_")/>
    <#list strlist as v>
        <#assign text=text+v?cap_first/>
    </#list>
    <#return text?uncap_first>
</#function>
<#-- Chuyển camelCase sang snake_case -->
<#function camelToDashed(str, case='lower')>
  <#return camelToChar(str, "_", case)>
</#function>
<#---->
<#-- Chuyển camelCase sang kebab-case -->
<#function camelToHorizontal(str, case='normal')>
  <#return camelToChar(str, "-", case)>
</#function>
<#---->
<#-- Lấy v-model Thuộc tính -->
<#function getVModel po,suffix="">
  <#return "v-model=\"queryParam.${po.fieldName}${suffix}\"">
</#function>
<#-- Lấy thuộc tính placeholder -->
<#function getPlaceholder po,prefix,fillComment=true>
  <#if fillComment>
    <#return "placeholder=\"${prefix}${po.filedComment}\"">
  <#else>
    <#return "placeholder=\"${prefix}\"">
  </#if>
</#function>
<#-- ** Xác định một trường nào đó có được cấu hình kiểm tra hay không * -->
<#function poHasCheck po>
  <#if (po.fieldValidType!'')?trim?length gt 0 || po.nullable == 'N'>
    <#if po.fieldName != 'id'>
      <#if po.nullable == 'N'
      || po.fieldValidType == '*'
      || po.fieldValidType == 'only'
      || po.fieldValidType == 'n6-16'
      || po.fieldValidType == '*6-16'
      || po.fieldValidType == 's6-18'
      || po.fieldValidType == 'url'
      || po.fieldValidType == 'e'
      || po.fieldValidType == 'm'
      || po.fieldValidType == 'p'
      || po.fieldValidType == 's'
      || po.fieldValidType == 'n'
      || po.fieldValidType == 'z'
      || po.fieldValidType == 'money'
      || po.fieldValidType != ''
      >
        <#return true>
      </#if>
    </#if>
  </#if>
  <#return false>
</#function>
<#-- ** Nếu đã cấu hình kiểm tra thì hiển thị validatorRules * -->
<#function autoWriteRules po>
  <#if poHasCheck(po)>
    <#return ", validatorRules.${po.fieldName}">
  <#else>
    <#return "">
  </#if>
</#function>

<#-- ** Nếu là Blob thì hiển thị String * -->
<#function autoStringSuffix po>
  <#if  po.fieldDbType=='Blob'>
    <#return "'${po.fieldName}String'">
  <#else>
    <#return "'${po.fieldName}'">
  </#if>
</#function>

<#-- ** Nếu là Blob thì hiển thị theo kiểu model String * -->
<#function autoStringSuffixForModel po>
    <#if  po.fieldDbType=='Blob'>
        <#return "${po.fieldName}String">
    <#else>
        <#return "${po.fieldName}">
    </#if>
</#function>

<#-- ** Tạo truy vấn nâng cao * -->
<#function superQueryFieldList po>
    <#assign superQuery_dictTable="">
    <#assign superQuery_dictText="">
    <#if po.dictTable?default("")?trim?length gt 1>
        <#assign superQuery_dictTable="${po.dictTable}">
    </#if>
    <#if po.dictText?default("")?trim?length gt 1>
        <#assign superQuery_dictText="${po.dictText}">
    </#if>
  <#if po.classType=="popup">
      <#return "{type:'${po.classType}',value:'${po.fieldName}',text:'${po.filedComment}', popup:{code:'${po.dictTable}',field:'${po.dictField?split(',')[0]}',orgFields:'${po.dictField?split(',')[0]}',destFields:'${po.dictText?split(',')[0]}'}}">
  <#elseif po.classType=="sel_user" || po.classType=="sel_depart" || po.classType=="datetime" || po.classType=="date" || po.classType=="pca" || po.classType=="switch">
      <#return "{type:'${po.classType}',value:'${po.fieldName}',text:'${po.filedComment}'}">
  <#else>
      <#if po.classType=="sel_search" || po.classType=="list_multi">
          <#return "{type:'${po.classType}',value:'${po.fieldName}',text:'${po.filedComment}',dictTable:\"${superQuery_dictTable}\", dictText:'${superQuery_dictText}', dictCode:'${po.dictField}'}">
      <#elseif po.dictTable?? && po.dictTable!="" && po.classType!="sel_tree" && po.classType!="cat_tree" && po.classType!="link_down">
          <#return "{type:'${po.fieldDbType}',value:'${po.fieldName}',text:'${po.filedComment}',dictCode:\"${po.dictTable},${po.dictText},${po.dictField}\"}">
      <#elseif po.dictField?? && po.classType!="sel_tree" && po.classType!="cat_tree" && po.classType!="link_down">
          <#return "{type:'${po.fieldDbType}',value:'${po.fieldName}',text:'${po.filedComment}',dictCode:'${po.dictField}'}">
      <#elseif po.fieldDbType=="Text" || po.fieldDbType=="LongText">
          <#return "{type:'string',value:'${po.fieldName}',text:'${po.filedComment}'}">
      <#elseif po.fieldDbType=="Blob">
          <#return "{type:'byte',value:'${po.fieldName}',text:'${po.filedComment}'}">
      <#elseif po.fieldDbType=="BigDecimal" || po.fieldDbType=="double">
          <#return "{type:'number',value:'${po.fieldName}',text:'${po.filedComment}'}">
      <#else>
          <#return "{type:'${po.fieldDbType}',value:'${po.fieldName}',text:'${po.filedComment}'}">
      </#if>
  </#if>
</#function>
<#-- update-begin---author:chenrui ---date:20231228  for:[QQYUN-7527]vue3 Tạo mã mặc định kèm theo truy vấn nâng cao---------- -->
<#-- ** Tạo truy vấn nâng cao(Vue3 * -->
<#function superQueryFieldListForVue3(po,order)>
<#-- Định dạng ngày truy vấn nâng cao -->
    <#assign picker=''>
    <#if po.extendParams?exists && po.extendParams.picker?exists>
        <#assign picker='fieldExtendJson:"{\\"picker\\":\\"${po.extendParams.picker}\\"}",'>
    </#if>
<#-- Hiển thị trường/DB Loại -->
    <#assign baseAttrs="view: '${po.classType}', type: 'string',">
    <#if po.fieldDbType=='int' || po.fieldDbType=='long' || po.fieldDbType=='double' || po.fieldDbType=='BigDecimal'>
        <#assign baseAttrs="view: 'number', type: 'number',">
    </#if>

<#-- Trường mở rộng của điều khiển loại đặc biệt -->
    <#assign extAttrs="">
    <#assign dictCode="">
    <#if po.dictTable?default('')?trim?length gt 1 && po.dictText?default('')?trim?length gt 1 && po.dictField?default("")?trim?length gt 1>
        <#-- update-begin---author:chenrui ---date:20231228  for:fix Danh mục có điều kiện chứa dấu nháy đơn gây lỗi biên dịch js---------- -->
        <#assign dictCode="dictTable: \"${po.dictTable}\", dictCode: '${po.dictField}', dictText: '${po.dictText}'">
        <#-- update-begin---author:chenrui ---date:20231228  for:fix Danh mục có điều kiện chứa dấu nháy đơn gây lỗi biên dịch js---------- -->
    <#elseif po.dictField?default("")?trim?length gt 1>
        <#assign dictCode="dictCode: '${po.dictField}'">
    <#else>
        <#assign dictCode="dictCode: ''">
    </#if>

    <#if po.classType=='list' || po.classType=='list_multi' || po.classType=='sel_search' || po.classType=='checkbox' || po.classType=='radio'>
        <#assign extAttrs="${dictCode},">
    <#elseif po.classType=='cat_tree'>
    <#-- Cây từ điển phân loại -->
        <#-- update-begin---author:chenrui ---date:20240109  for：[issue/5787]Thêm phán đoán khác rỗng để tránh ngoại lệ con trỏ null khi sinh mã---------- -->
        <#assign extAttrs="pcode: '${po.dictField?default('')}',">
        <#-- update-end---author:chenrui ---date:20240109  for：[issue/5787]Thêm phán đoán khác rỗng để tránh ngoại lệ con trỏ null khi sinh mã---------- -->
    <#elseif po.classType=='sel_tree'>
    <#-- Cây tùy chỉnh -->
        <#if po.dictText??>
        <#-- dictText Ví dụ:id,pid,name,has_child -->
            <#if po.dictText?split(',')[2]?? && po.dictText?split(',')[0]??>
                <#assign extAttrs="dict: '${po.dictTable},${po.dictText?split(',')[2]},${po.dictText?split(',')[0]}'">
            <#elseif po.dictText?split(',')[1]??>
                <#assign extAttrs="pidField: '${po.dictText?split(',')[1]}'">
            <#elseif po.dictText?split(',')[3]??>
                <#assign extAttrs="hasChildField: '${po.dictText?split(',')[3]}'">
            </#if>
        </#if>
        <#-- update-begin---author:chenrui ---date:20240109  for：[issue/5787]Thêm phán đoán khác rỗng để tránh ngoại lệ con trỏ null khi sinh mã---------- -->
        <#assign extAttrs="${extAttrs}, pidValue: '${po.dictField?default('')}',">
        <#-- update-end---author:chenrui ---date:20240109  for：[issue/5787]Thêm phán đoán khác rỗng để tránh ngoại lệ con trỏ null khi sinh mã---------- -->
    <#elseif po.classType=='popup'>
    <#-- popup -->
        <#-- update-begin---author:chenrui ---date:20240109  for：[issue/5787]Thêm phán đoán khác rỗng để tránh ngoại lệ con trỏ null khi sinh mã---------- -->
        <#if po.dictText?default("")?trim?length gt 1 && po.dictText?index_of(',') gt 0 && po.dictField?default("")?trim?length gt 1>
        <#-- update-begin---author:chenrui ---date:20240109  for：[issue/5787]Thêm phán đoán khác rỗng để tránh ngoại lệ con trỏ null khi sinh mã---------- -->
        <#-- Nếu có nhiều trường điền lại,Tìm trường nguồn tương ứng với trường popup -->
            <#assign orgFieldIx=po.dictText?split(',')?seq_index_of(po.fieldDbName)>
            <#assign orgField=po.dictField?split(',')[orgFieldIx]>
        <#else>
            <#assign orgField=po.dictField?default("")>
        </#if>
        <#assign extAttrs="code: '${po.dictTable?default('')}', orgFields: '${orgField}', destFields: '${po.fieldName}', popupMulti: false,">
    <#elseif po.classType=='popup_dict'>
    <#-- popup Từ điển -->
    <#-- author:wangshuai--- date:20260831--for: 【LHZP-1027] Tạo mã — truy vấn nâng cao: popup danh mục được tạo không đúng, không tạo ra mục danh mục/【LHZP-1028] Sinh code truy vấn nâng cao: bản ghi liên kết chưa sinh mục từ điển  -->
        <#assign extAttrs="${dictCode}, code: '${po.dictTable?default('')}', orgFields: '${po.dictField?default('')}', destFields: '${po.dictText?default('')}', popupMulti: ${po.extendParams.popupMulti?c},">
    <#elseif po.classType=='link_table'>
    <#-- Bản ghi liên kết tải các tùy chọn theo danh mục bảng trong truy vấn nâng cao -->
        <#assign extAttrs="${dictCode},">
    <#-- author:wangshuai--- date:20260831--for: 【LHZP-1027] Tạo mã — truy vấn nâng cao: popup danh mục được tạo không đúng, không tạo ra mục danh mục/【LHZP-1028] Sinh code truy vấn nâng cao: bản ghi liên kết chưa sinh mục từ điển  -->
    </#if>

    <#return "${po.fieldName}: {title: '${po.filedComment}',order: ${order},${baseAttrs}${extAttrs}${picker}}" >
</#function>
<#-- update-end---author:chenrui ---date:20231228  for:[QQYUN-7527]vue3 Tạo mã mặc định kèm theo truy vấn nâng cao---------- -->


<#-- vue3 Lấy chiều rộng của modal biểu mẫu-->
<#function getModalWidth fieldRowNum>
    <#assign modal_width = 800>
    <#if fieldRowNum==2>
        <#assign modal_width = 896>
    <#elseif fieldRowNum==3>
        <#assign modal_width = 1024>
    <#elseif fieldRowNum==4>
        <#assign modal_width = 1280>
    </#if>
    <#return modal_width>
</#function>

<#-- vue3 Lấy biểu mẫu colspan -->
<#function getFormSpan fieldRowNum>
    <#assign form_span = 24>
    <#if fieldRowNum==2>
        <#assign form_span = 12>
    <#elseif fieldRowNum==3>
        <#assign form_span = 8>
    <#elseif fieldRowNum==4>
        <#assign form_span = 6>
    </#if>
    <#return form_span>
</#function>

<#-- vue3 native Kiểm tra tên trường không phải là pidField  -->
<#function isNotPidField(tableVo, fieldDbName) >
  <#assign flag = true>
  <#if tableVo??>
    <#if tableVo.extendParams??>
      <#if tableVo.extendParams.pidField?default("")?trim == fieldDbName>
        <#assign flag = false>
      </#if>
    </#if>
  </#if>
  <#return flag>
</#function>

<#-- vue3 native Lấy trường phạm vi -->
<#function getRangeField(columns) >
  <#assign rangeField = "">
  <#list columns as po>
      <#if po.isQuery=='Y'>
          <#if po.queryMode=='group'>
              <#if po.fieldDbType=='int' || po.fieldDbType=='long' || po.fieldDbType=='double' || po.fieldDbType=='BigDecimal' || po.classType=='time' || po.classType=='date' || po.classType=='datetime'>
                  <#assign rangeField = rangeField + "${po.fieldName},">
              </#if>
          </#if>
      </#if>
  </#list>
  <#return rangeField>
</#function>