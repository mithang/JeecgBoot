<template>
  <!--  <div>-->
  <!--    <a-input-->
  <!--        v-show="!departIds"-->
  <!--        @click="openSelect"-->
  <!--        placeholder="Vui lòng nhấp để chọn phòng ban"-->
  <!--        v-model="departNames"-->
  <!--        readOnly-->
  <!--        :disabled="componentDisabled"-->
  <!--        class="jvxe-select-input">-->
  <!--      <a-icon slot="prefix" type="cluster" title="Control chọn phòng ban"/>-->
  <!--    </a-input>-->
  <!--    <j-select-depart-modal-->
  <!--        ref="innerDepartSelectModal"-->
  <!--        :modal-width="modalWidth"-->
  <!--        :multi="multi"-->
  <!--        :rootOpened="rootOpened"-->
  <!--        :depart-id="departIds"-->
  <!--        @ok="handleOK"-->
  <!--        @initComp="initComp"/>-->
  <!--    <span style="display: inline-block;height:100%;padding-left:14px" v-if="departIds">-->
  <!--      <span @click="openSelect" style="display: inline-block;vertical-align: middle">{{ departNames }}</span>-->
  <!--      <a-icon style="margin-left:5px;vertical-align: middle" type="close-circle" @click="handleEmpty" title="Xóa sạch"/>-->
  <!--    </span>-->
  <!--  </div>-->
  <div :class="[prefixCls]">
    <JSelectDept v-bind="getProps" @change="handleChange" />
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  //import { JSelectDept } from '/@/components/Form';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  // import JSelectDepartModal from '@/components/jeecgbiz/modal/JSelectDepartModal'
  import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
  import { isArray, isEmpty, isString } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeDepartSelectCell',
    components: { JSelectDept },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, useCellDesign } = useJVxeComponent(props);
      const { prefixCls } = useCellDesign('depart-select');

      const selectedValue = computed(() => {
        let val: any = innerValue.value;
        if (val == null) {
          return val;
        }
        if (isEmpty(val)) {
          return [];
        }
        if (isArray(val)) {
          return val;
        }
        if (isString(val)) {
          return (<string>val).split(',');
        }
        return [val];
      });

      const multiple = computed(() => cellProps.value['multi'] != false);

      const getProps = computed(() => {
        return {
          ...cellProps.value,
          value: selectedValue.value,
          showButton: false,
          // Không cho phép tìm kiếm
          showSearch: false,
          // Đặt số lượng hiển thị tối đa
          maxTagCount: 1,
          // Viết lại hiển thị nhắc nhở, bỏ dấu ba chấm
          maxTagPlaceholder: ({ length }) => '+' + length,
          // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1002]Tối ưu cách hiển thị component người dùng và component phòng ban khi chỉnh sửa hàng ở trang chi tiết
          isDetailsMode: cellProps.value.disabledTable,
          // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-1002]Tối ưu cách hiển thị component người dùng và component phòng ban khi chỉnh sửa hàng ở trang chi tiết
        };
      });

      function handleChange(values) {
        handleChangeCommon(values.join(','));
      }

      return {
        prefixCls,
        selectedValue,
        multiple,
        cellProps,
        getProps,
        handleChange,
      };
    },

    data() {
      return {
        // departNames: '',
        // departIds: '',
        // selectedOptions: [],
        // customReturnField: 'id',
      };
    },
    computed: {
      // custProps() {
      //   const { departIds, originColumn: col, caseId, cellProps } = this
      //   return {
      //     ...cellProps,
      //     value: departIds,
      //     field: col.field || col.key,
      //     groupId: caseId,
      //     class: 'jvxe-select',
      //   }
      // },
      // componentDisabled() {
      //   if (this.cellProps.disabled == true) {
      //     return true
      //   }
      //   return false
      // },
      // modalWidth() {
      //   if (this.cellProps.modalWidth) {
      //     return this.cellProps.modalWidth
      //   } else {
      //     return 500
      //   }
      // },
      // multi() {
      //   if (this.cellProps.multi == false) {
      //     return false
      //   } else {
      //     return true
      //   }
      // },
      // rootOpened() {
      //   if (this.cellProps.open == false) {
      //     return false
      //   } else {
      //     return true
      //   }
      // },
    },
    watch: {
      // innerValue: {
      //   immediate: true,
      //   handler(val) {
      //     if (val == null || val === '') {
      //       this.departIds = ''
      //     } else {
      //       this.departIds = val
      //     }
      //   },
      // },
    },
    methods: {
      // openSelect() {
      //   this.$refs.innerDepartSelectModal.show()
      // },
      // handleEmpty() {
      //   this.handleOK('')
      // },
      // handleOK(rows, idstr) {
      //   let value = ''
      //   if (!rows && rows.length <= 0) {
      //     this.departNames = ''
      //     this.departIds = ''
      //   } else {
      //     value = rows.map(row => row[this.customReturnField]).join(',')
      //     this.departNames = rows.map(row => row['departName']).join(',')
      //     this.departIds = idstr
      //   }
      //   this.handleChangeCommon(this.departIds)
      // },
      // initComp(departNames) {
      //   this.departNames = departNames
      // },
      // handleChange(value) {
      //   this.handleChangeCommon(value)
      // },
    },
    enhanced: {
      switches: {
        visible: true,
      },
      translate: {
        enabled: false,
      },
      aopEvents: {
        editActived({ $event }) {
          dispatchEvent({
            $event,
            props: this.props,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: true,
          });
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  // noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-vxe-cell-depart-select';

  .@{prefix-cls} {
    // Giới hạn độ dài tối đa của tag là 100px, tránh xuống dòng khi chọn option có quá nhiều chữ
    .ant-select .ant-select-selection-overflow-item {
      max-width: 80px;
    }
  }
</style>
