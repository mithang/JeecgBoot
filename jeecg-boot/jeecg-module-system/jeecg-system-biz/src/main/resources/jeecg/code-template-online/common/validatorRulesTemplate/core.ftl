<#include "/common/utils.ftl">
<#if po.isShow == 'Y' && poHasCheck(po)>
    <#if po.fieldName != 'id'>
           ${po.fieldName}: [
        <#assign fieldValidType = po.fieldValidType!''>
    <#-- Kiểm tra không rỗng -->
        <#if po.nullable == 'N' || fieldValidType == '*'>
              { required: true, message: 'Vui lòng nhập${po.filedComment}!'},
        <#elseif fieldValidType!=''>
              { required: false},
        </#if>
    <#-- Kiểm tra duy nhất -->
        <#if fieldValidType == 'only'>
              { validator: (rule, value, callback) => validateDuplicateValue(<#if sub?default("")?trim?length gt 1>'${sub.tableName}'<#else>'${tableName}'</#if>, '${po.fieldDbName}', value, this.model.id, callback)},
        <#-- 6 đến 16 chữ số -->
        <#elseif fieldValidType == 'n6-16'>
              { pattern: /^\d{6,16}$|^(?=\d+\.\d+)[\d.]{7,17}$/, message: 'Vui lòng nhập số có 6 đến 16 chữ số!'},
        <#-- 6 đến 16 ký tự bất kỳ -->
        <#elseif fieldValidType == '*6-16'>
              { pattern: /^.{6,16}$/, message: 'Vui lòng nhập 6 đến 16 ký tự bất kỳ!'},
        <#-- 6 đến chuỗi 18 ký tự -->
        <#elseif fieldValidType == 's6-18'>
          { pattern: /^[a-z|A-Z]{6,18}$/, message: 'Vui lòng nhập chữ cái có 6 đến 18 ký tự!'},
        <#-- URL -->
        <#elseif fieldValidType == 'url'>
              { pattern: /^((ht|f)tps?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/, message: 'Vui lòng nhập địa chỉ trang web chính xác!'},
        <#-- Email -->
        <#elseif fieldValidType == 'e'>
              { pattern: /^([\w]+\.*)([\w]+)@[\w]+\.\w{3}(\.\w{2}|)$/, message: 'Vui lòng nhập đúng địa chỉ email!'},
        <#-- Số điện thoại -->
        <#elseif fieldValidType == 'm'>
              { pattern: /^1[3456789]\d{9}$/, message: 'Vui lòng nhập đúng số điện thoại di động!'},
        <#-- Mã bưu chính -->
        <#elseif fieldValidType == 'p'>
              { pattern: /^[0-9]\d{5}$/, message: 'Vui lòng nhập đúng mã bưu chính!'},
        <#-- Chữ cái -->
        <#elseif fieldValidType == 's'>
              { pattern: /^[A-Z|a-z]+$/, message: 'Vui lòng nhập chữ cái!'},
        <#-- Số -->
        <#elseif fieldValidType == 'n'>
              { pattern: /^-?\d+\.?\d*$/, message: 'Vui lòng nhập số!'},
        <#-- Số nguyên -->
        <#elseif fieldValidType == 'z'>
              { pattern: /^-?\d+$/, message: 'Vui lòng nhập số nguyên!'},
        <#-- Số tiền -->
        <#elseif fieldValidType == 'money'>
              { pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: 'Vui lòng nhập số tiền chính xác!'},
        <#-- Kiểm tra bằng biểu thức chính quy -->
        <#elseif fieldValidType != '' && fieldValidType != '*'>
              { pattern: '${fieldValidType}', message: 'Không phù hợp quy tắc kiểm chứng!'},
        <#-- Không kiểm tra -->
        <#else>
            <#t>
        </#if>
           ],
    </#if>
</#if>