import BasicForm from './src/BasicForm.vue';
import { defineAsyncComponent } from 'vue';
export * from './src/types/form';
export * from './src/types/formItem';

export { useComponentRegister } from './src/hooks/useComponentRegister';
export { useForm } from './src/hooks/useForm';

export const ApiSelect = defineAsyncComponent(() => import('./src/components/ApiSelect.vue'));
export const RadioButtonGroup = defineAsyncComponent(() => import('./src/components/RadioButtonGroup.vue'));
export const ApiTreeSelect = defineAsyncComponent(() => import('./src/components/ApiTreeSelect.vue'));
export const ApiRadioGroup = defineAsyncComponent(() => import('./src/components/ApiRadioGroup.vue'));
//JeecgComponent tùy chỉnh
export const JAreaLinkage = defineAsyncComponent(() => import('./src/jeecg/components/JAreaLinkage.vue'));
export const JSelectUser = defineAsyncComponent(() => import('./src/jeecg/components/JSelectUser.vue'));
export const JSelectDept = defineAsyncComponent(() => import('./src/jeecg/components/JSelectDept.vue'));
export const JSelectDepartPost = defineAsyncComponent(() => import('./src/jeecg/components/JSelectDepartPost.vue'));
export const JSelectUserByDeptPost = defineAsyncComponent(() => import('./src/jeecg/components/JSelectUserByDeptPost.vue'));
export const JCodeEditor = defineAsyncComponent(() => import('./src/jeecg/components/JCodeEditor.vue'));
export const JCategorySelect = defineAsyncComponent(() => import('./src/jeecg/components/JCategorySelect.vue'));
export const JSelectMultiple = defineAsyncComponent(() => import('./src/jeecg/components/JSelectMultiple.vue'));
export const JPopup = defineAsyncComponent(() => import('./src/jeecg/components/JPopup.vue'));
export const JAreaSelect = defineAsyncComponent(() => import('./src/jeecg/components/JAreaSelect.vue'));
export const JEasyCron = defineAsyncComponent(() => import('./src/jeecg/components/JEasyCron/EasyCronInput.vue'));
export const JEasyCronInner = defineAsyncComponent(() => import('./src/jeecg/components/JEasyCron/EasyCronInner.vue'));
export const JEasyCronModal = defineAsyncComponent(() => import('./src/jeecg/components/JEasyCron/EasyCronModal.vue'));
export const JCheckbox = defineAsyncComponent(() => import('./src/jeecg/components/JCheckbox.vue'));
export const JInput = defineAsyncComponent(() => import('./src/jeecg/components/JInput.vue'));
export const JEllipsis = defineAsyncComponent(() => import('./src/jeecg/components/JEllipsis.vue'));
export const JDictSelectTag = defineAsyncComponent(() => import('./src/jeecg/components/JDictSelectTag.vue'));
export const JTreeSelect = defineAsyncComponent(() => import('./src/jeecg/components/JTreeSelect.vue'));
export const JSearchSelect = defineAsyncComponent(() => import('./src/jeecg/components/JSearchSelect.vue'));
export const JSelectUserByDept = defineAsyncComponent(() => import('./src/jeecg/components/JSelectUserByDept.vue'));
export const JSelectUserByDepartment = defineAsyncComponent(() => import('./src/jeecg/components/JSelectUserByDepartment.vue'));
// update-begin--author:liaozhiyang---date:20260227---for:【QQYUN-14751]tinymce rich text, JEasyCron, JLinkTableCard tải bất đồng bộ
export const JEditor = defineAsyncComponent(() => import('./src/jeecg/components/JEditor.vue'));
// update-end--author:liaozhiyang---date:20260227---for:【QQYUN-14751]tinymce rich text, JEasyCron, JLinkTableCard tải bất đồng bộ
export const JImageUpload = defineAsyncComponent(() => import('./src/jeecg/components/JImageUpload.vue'));
// JeecgKiểm chứng tùy chỉnh
// update-begin--author:liaozhiyang---date:20260303---for:【QQYUN-14815]JCronValidator được comment khỏi Form để ngăn tải trang chủ, đổi sang import trực tiếp trong nghiệp vụ
// export { default as JCronValidator } from '/@/components/Form/src/jeecg/components/JEasyCron/validator';
// update-end--author:liaozhiyang---date:20260303---for:【QQYUN-14815]JCronValidator được comment khỏi Form để ngăn tải trang chủ, đổi sang import trực tiếp trong nghiệp vụ
export { BasicForm };
