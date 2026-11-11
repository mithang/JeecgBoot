<#include "/common/utils.ftl">
<#-- update-begin---author:chenrui ---date:20240108  for：[issues/5755]vue Mã không thêm trường xóa logic---------- -->
<#-- update-begin---author:gj_liusqian ---date:20260806  for：[SQLServer Trường lớn] Loại trừ Text/LongText/Blob Làm điều kiện truy vấn---------- -->
<#if po.isQuery=='Y' && po.fieldName !='delFlag' && po.fieldDbType !='Text' && po.fieldDbType !='LongText' && po.fieldDbType !='Blob'>
<#-- update-end---author:gj_liusqian ---date:20260806  for：[SQLServer Trường lớn] Loại trừ Text/LongText/Blob Làm điều kiện truy vấn---------- -->
<#-- update-end---author:chenrui ---date:20240108  for：[issues/5755]vue Mã không thêm trường xóa logic---------- -->
<#assign query_flag=true>
	<#if query_field_no==2>
          <template v-if="toggleSearchStatus">
	</#if>
	<#assign query_field_dictCode="">
	<#if po.dictTable?default("")?trim?length gt 1>
	    <#assign need_select_tag = true>
        <#assign need_multi = true>
	    <#assign query_field_dictCode="${po.dictTable},${po.dictText},${po.dictField}">
	<#elseif po.dictField?default("")?trim?length gt 1>
	    <#assign need_select_tag = true>
        <#assign need_multi = true>
	    <#assign query_field_dictCode="${po.dictField}">
	</#if>
	<#if po.queryMode=='single'>
          <#if query_field_no gt 1>  </#if><a-col :lg="6">
            <#if query_field_no gt 1>  </#if><a-form-item name="${autoStringSuffixForModel(po)}">
              <#if query_field_no gt 1>  </#if><template #label><span title="${po.filedComment}"><#if po.filedComment?default("")?trim?length gt 4>${po.filedComment?substring(0,4)}<#else>${po.filedComment}</#if></span></template>
            <#if po.classType=='sel_search'>
              <#if query_field_no gt 1>  </#if><j-search-select placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" dict="${po.dictTable},${po.dictText},${po.dictField}" allow-clear />
            <#elseif po.classType=='sel_user'>
              <#-- update-begin---author:chenrui ---date:20240102  for：[issue/#5711]Sửa lỗi component chọn người dùng sau khi sinh code bị biến thành component chọn người dùng theo phòng ban---------- -->
              <#if query_field_no gt 1>  </#if><j-select-user placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" @change="(value)=>handleFormJoinChange('${po.fieldName}',value)" allow-clear />
              <#-- update-end---author:chenrui ---date:20240102  for：[issue/#5711]Sửa lỗi component chọn người dùng sau khi sinh code bị biến thành component chọn người dùng theo phòng ban---------- -->
            <#elseif po.classType=='switch'>
              <#if query_field_no gt 1>  </#if><j-switch placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" <#if po.dictField!= 'is_open'>:options="${po.dictField}"</#if> query />
            <#elseif po.classType=='sel_depart'>
              <#if query_field_no gt 1>  </#if><j-select-dept placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" checkStrictly allow-clear />
            <#elseif po.classType=='list_multi'>
              <#if query_field_no gt 1>  </#if><j-select-multiple placeholder="Vui lòng chọn${po.filedComment}" dictCode="${query_field_dictCode?default("")}" v-model:value="queryParam.${po.fieldName}" allow-clear />
            <#elseif po.classType=='cat_tree'>
              <#if query_field_no gt 1>  </#if><j-category-select placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" pcode="${po.dictField?default("")}" @change="(value) => handleFormChange('${po.fieldName}', value)" allow-clear />
            <#elseif po.classType=='date'>
              <#if query_field_no gt 1>  </#if><a-date-picker valueFormat="YYYY-MM-DD" placeholder="Vui lòng chọn${po.filedComment}" <#if po.extendParams?exists && po.extendParams.picker?exists>picker="${po.extendParams.picker}"</#if> v-model:value="queryParam.${po.fieldName}" allow-clear />
            <#elseif po.classType=='datetime'>
              <#if query_field_no gt 1>  </#if><a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" allow-clear />
            <#elseif po.classType=='time'>
              <#if query_field_no gt 1>  </#if><time-picker valueFormat="HH:mm:ss" placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" allow-clear />
            <#elseif po.classType=='pca'>
              <#if query_field_no gt 1>  </#if><j-area-linkage v-model:value="queryParam.${po.fieldName}" placeholder="Vui lòng chọn${po.filedComment}" saveCode="region" @change="(value)=>handleFormJoinChange('${po.fieldName}',value)" allow-clear />
            <#elseif po.classType=='sel_tree'>
              <#if query_field_no gt 1>  </#if><j-tree-select v-model:value="queryParam.${po.fieldName}" placeholder="Vui lòng chọn${po.filedComment}" <#if po.dictText??><#if po.dictText?split(',')[2]?? && po.dictText?split(',')[0]??>dict="${po.dictTable},${po.dictText?split(',')[2]},${po.dictText?split(',')[0]}" <#elseif po.dictText?split(',')[1]??>pidField:"${po.dictText?split(',')[1]}", <#elseif po.dictText?split(',')[3]??>hasChildField:"${po.dictText?split(',')[3]}"</#if> </#if>pidValue="${po.dictField}" allow-clear />
            <#elseif po.classType=='popup'>
              <#assign sourceFields = po.dictField?default("")?trim?split(",")/>
              <#assign targetFields = po.dictText?default("")?trim?split(",")/>
              <#if query_field_no gt 1>  </#if><j-popup
                <#if query_field_no gt 1>  </#if>placeholder="Vui lòng chọn${po.filedComment}" 
                <#if query_field_no gt 1>  </#if>v-model:value="queryParam.${po.fieldName}" 
                <#if query_field_no gt 1>  </#if>code="${po.dictTable}"
                <#if query_field_no gt 1>  </#if>:fieldConfig="[
                <#list sourceFields as fieldName>
                  <#if query_field_no gt 1>  </#if>{ source: '${fieldName}', target: '${dashedToCamel(targetFields[fieldName_index])}' },
                </#list>
                <#if query_field_no gt 1>  </#if>]"
                <#if query_field_no gt 1>  </#if>:multi="${po.extendParams.popupMulti?c}"
                <#if query_field_no gt 1>  </#if>:setFieldsValue="setFieldsValue" allow-clear />
            <#elseif po.classType=='popup_dict'>
                <#if query_field_no gt 1>  </#if><j-popup-dict
                <#if query_field_no gt 1>  </#if>placeholder="Vui lòng chọn${po.filedComment}"
                <#if query_field_no gt 1>  </#if>v-model:value="queryParam.${po.fieldName}"
                <#if query_field_no gt 1>  </#if>dictCode="${po.dictTable},${po.dictText},${po.dictField}"
                <#if query_field_no gt 1>  </#if>:multi="${po.extendParams.popupMulti?c}"
                <#if query_field_no gt 1>  </#if>allow-clear />
            <#elseif po.classType=='list' || po.classType=='radio' || po.classType=='checkbox'>
             <#--  ---------------------------Thả xuống hoặc chọn đơn: xác định từ điển dữ liệu là từ điển bảng hay từ điển thường------------------------------- -->
             <#if po.dictTable?default("")?trim?length gt 1>
              <#if query_field_no gt 1>  </#if><j-select-multiple placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" dictCode="${po.dictTable},${po.dictText},${po.dictField}" allow-clear />
              <#elseif po.dictField?default("")?trim?length gt 1>
              <#if query_field_no gt 1>  </#if><j-select-multiple placeholder="Vui lòng chọn${po.filedComment}" v-model:value="queryParam.${po.fieldName}" dictCode="${po.dictField}" allow-clear />
              <#else>
              <#if query_field_no gt 1>  </#if><a-input placeholder="Vui lòng nhập${po.filedComment}" v-model:value="queryParam.${po.fieldName}" allow-clear ></a-input>
             </#if>
            <#elseif po.fieldDbType=='int' || po.fieldDbType=='long' || po.fieldDbType=='double' || po.fieldDbType=='BigDecimal'>
              <#if query_field_no gt 1>  </#if><a-input-number placeholder="Vui lòng nhập${po.filedComment}" v-model:value="queryParam.${po.fieldName}"></a-input-number>           
            <#else>
              <#if query_field_no gt 1>  </#if><a-input placeholder="Vui lòng nhập${po.filedComment}" v-model:value="queryParam.${autoStringSuffixForModel(po)}" allow-clear ></a-input>
          </#if>
            <#if query_field_no gt 1>  </#if></a-form-item>
          <#if query_field_no gt 1>  </#if></a-col>
    <#elseif po.queryMode=='like'>
        <#if query_field_no gt 1>  </#if><a-col :lg="6">
        <#if query_field_no gt 1>  </#if><a-form-item name="${autoStringSuffixForModel(po)}">
        <#if query_field_no gt 1>  </#if><template #label><span title="${po.filedComment}"><#if po.filedComment?default("")?trim?length gt 4>${po.filedComment?substring(0,4)}<#else>${po.filedComment}</#if></span></template>
            <#if query_field_no gt 1>  </#if><JInput v-model:value="queryParam.${po.fieldName}"/>
        <#if query_field_no gt 1>  </#if></a-form-item>
        <#if query_field_no gt 1>  </#if></a-col>
	<#else>
          <#if query_field_no gt 1>  </#if><a-col :lg="6">
            <#if query_field_no gt 1>  </#if><a-form-item name="${autoStringSuffixForModel(po)}">
              <#if query_field_no gt 1>  </#if><template #label><span title="${po.filedComment}"><#if po.filedComment?default("")?trim?length gt 4>${po.filedComment?substring(0,4)}<#else>${po.filedComment}</#if></span></template>
      <#if po.classType=='date'>
              <#if query_field_no gt 1>  </#if><a-range-picker value-format="YYYY-MM-DD" <#if po.extendParams?exists && po.extendParams.picker?exists>picker="${po.extendParams.picker}"</#if> v-model:value="queryParam.${po.fieldName}" class="query-group-cust"/>
      <#elseif po.classType=='time'>
              <#if query_field_no gt 1>  </#if><a-time-range-picker value-format="HH:mm:ss" v-model:value="queryParam.${po.fieldName}" class="query-group-cust" />
      <#elseif po.classType=='datetime'>
              <#if query_field_no gt 1>  </#if><a-range-picker showTime value-format="YYYY-MM-DD HH:mm:ss" v-model:value="queryParam.${po.fieldName}" class="query-group-cust"/>
      <#else>
              <#if query_field_no gt 1>  </#if><JRangeNumber v-model:value="queryParam.${po.fieldName}" class="query-group-cust"></JRangeNumber>
      </#if>
            <#if query_field_no gt 1>  </#if></a-form-item>
          <#if query_field_no gt 1>  </#if></a-col>
  </#if>
      <#assign query_field_no=query_field_no+1>
  </#if>