<#assign fieldValidType = po.fieldValidType!''>
<#-- Kiểm tra không rỗng -->
<#if po.nullable == 'N' || fieldValidType == '*'>
{ required: true, message: 'Vui lòng nhập${po.filedComment}!'}<#rt>,
<#elseif fieldValidType!=''>
{ required: false}<#rt>,
</#if>
<#-- Kiểm tra duy nhất -->
<#if fieldValidType == 'only'>
 { validator: ${po.fieldName}Duplicatevalidate }<#rt>
<#-- 6 đến 16 chữ số -->
<#elseif fieldValidType == 'n6-16'>
 { pattern: /^\d{6,16}$|^(?=\d+\.\d+)[\d.]{7,17}$/, message: 'Vui lòng nhập số có 6 đến 16 chữ số!'}<#rt>,
<#-- 6 đến 16 ký tự bất kỳ -->
<#elseif fieldValidType == '*6-16'>
 { pattern: /^.{6,16}$/, message: 'Vui lòng nhập 6 đến 16 ký tự bất kỳ!'}<#rt>,
<#-- 6 đến chuỗi 18 ký tự -->
<#elseif fieldValidType == 's6-18'>
 { pattern: /^[a-z|A-Z]{6,18}$/, message: 'Vui lòng nhập chữ cái có 6 đến 18 ký tự!'},<#rt>,
<#-- URL -->
<#elseif fieldValidType == 'url'>
 { pattern: /^((ht|f)tps?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/, message: 'Vui lòng nhập địa chỉ trang web chính xác!'}<#rt>,
<#-- Email -->
<#elseif fieldValidType == 'e'>
 { pattern: /^([\w]+\.*)([\w]+)@[\w]+\.\w{3}(\.\w{2}|)$/, message: 'Vui lòng nhập đúng địa chỉ email!'}<#rt>,
<#-- Số điện thoại -->
<#elseif fieldValidType == 'm'>
 { pattern: /^1[3456789]\d{9}$/, message: 'Vui lòng nhập đúng số điện thoại di động!'}<#rt>,
<#-- Mã bưu chính -->
<#elseif fieldValidType == 'p'>
 { pattern: /^[0-9]\d{5}$/, message: 'Vui lòng nhập đúng mã bưu chính!'}<#rt>,
<#-- Chữ cái -->
<#elseif fieldValidType == 's'>
 { pattern: /^[A-Z|a-z]+$/, message: 'Vui lòng nhập chữ cái!'}<#rt>,
<#-- Số -->
<#elseif fieldValidType == 'n'>
 { pattern: /^-?\d+\.?\d*$/, message: 'Vui lòng nhập số!'}<#rt>,
<#-- Số nguyên -->
<#elseif fieldValidType == 'z'>
 { pattern: /^-?\d+$/, message: 'Vui lòng nhập số nguyên!'}<#rt>,
<#-- Số tiền -->
<#elseif fieldValidType == 'money'>
 { pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: 'Vui lòng nhập số tiền chính xác!'}<#rt>,
<#-- Kiểm tra bằng biểu thức chính quy -->
<#elseif fieldValidType != '' && fieldValidType != '*'>
 { pattern: '${fieldValidType}', message: 'Không phù hợp quy tắc kiểm chứng!'}<#rt>,
<#else>
	<#t>
</#if>