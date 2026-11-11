<#include "/common/utils.ftl">
    <#if col.isShow == 'Y' && poHasCheck(col)>
        validateRules: [
        <#if col.fieldName != 'id'>
            <#assign subFieldValidType = col.fieldValidType!''>
        <#-- Kiểm tra không rỗng -->
            <#if col.nullable == 'N' || subFieldValidType == '*'>
          { required: true, message: '${'$'}{title}Không được trống' },
            <#elseif subFieldValidType!=''>
          { required: false},
            </#if>
        <#-- Trong các trường hợp khác, chỉ cần có giá trị thì được coi là kiểm tra regex -->
            <#if subFieldValidType?length gt 0>
            <#assign subMessage = 'Định dạng không đúng'>
            <#if subFieldValidType == 'only' >
                <#assign subMessage = 'không được trùng lặp'>
            </#if>
          { pattern: "${subFieldValidType}", message: "${'$'}{title}${subMessage}" }
                <#t>
            </#if>
        </#if>
        ],
    </#if>
