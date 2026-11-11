<template>
    <a-range-picker v-model:value="rangeValue" @change="handleChange" :show-time="datetime" :placeholder="placeholder" :picker="picker" :format="format || valueFormat" :valueFormat="valueFormat"/>
</template>

<script>
    import { defineComponent, ref, watch, computed } from 'vue';
    import { propTypes } from '/@/utils/propTypes';
    import { Form } from 'ant-design-vue';
    import dayjs from 'dayjs';
    import isoWeek from 'dayjs/plugin/isoWeek';
    import quarterOfYear from 'dayjs/plugin/quarterOfYear';

    dayjs.extend(isoWeek);
    dayjs.extend(quarterOfYear);

    const placeholder = ['Ngày bắt đầu', 'Ngày kết thúc']
    /**
     * Dùng cho truy vấn theo phạm vi
     */
    export default defineComponent({
        name: "JRangeDate",
        props:{
            value: propTypes.string.def(''),
            datetime: propTypes.bool.def(false),
            placeholder: propTypes.string.def(''),
            picker: propTypes.string.def('date'),
            format: propTypes.string.def(''),
            valueFormat: propTypes.string.def(''),
        },
        emits:['change', 'update:value'],
        setup(props, {emit}){
            const rangeValue = ref([])
            const formItemContext = Form.useInjectFormItemContext();

            watch(()=>props.value, (val)=>{
                if(val){
                    rangeValue.value = val.split(',')
                }else{
                    rangeValue.value = []
                }
            }, {immediate: true});

            const valueFormat = computed(()=>{
                if(props.valueFormat){
                    return props.valueFormat
                }
                if(props.datetime === true){
                    return 'YYYY-MM-DD HH:mm:ss'
                }else{
                    return 'YYYY-MM-DD'
                }
            });

            function handleChange(arr){
                let str = ''
                if(arr && arr.length>0){
                  // Giải thích logic mã: [issues/6368] rangeDateBỏ kiểm tra cho phép mục bắt đầu hoặc mục kết thúc trống để tương thíchallowEmpty
                  // update-begin--author:wangshuai---date:20260819---for：【LHZP-322][Trình thiết kế biểu mẫu] Hỗ trợ tuần, quý
                  str = arr.map(normalizePickerValue).join(',')
                  // update-end--author:wangshuai---date:20260819---for：【LHZP-322][Trình thiết kế biểu mẫu] Hỗ trợ tuần, quý
                }
                emit('change', str);
                emit('update:value', str);
                formItemContext.onFieldChange();
            }

          /**
           * Mã sau khi định dạng trả về của quý, tuần
           * @param value
           * @returns {*|string}
           */
            function normalizePickerValue(value){
                if(!value){
                    return value
                }
                if(props.picker === 'quarter'){
                    return dayjs(value).startOf('quarter').format('YYYY-MM-DD')
                }
                if(props.picker === 'week'){
                    return dayjs(value).startOf('isoWeek').format('YYYY-MM-DD')
                }
                return value
            }
            
            return {
                rangeValue,
                placeholder,
                valueFormat,
                handleChange
            }
        }
    });
</script>

<style scoped>

</style>
