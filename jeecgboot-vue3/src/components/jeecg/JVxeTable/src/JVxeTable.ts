import { defineComponent, h, ref, useSlots, computed, resolveComponent } from 'vue';
import { vxeEmits, vxeProps } from './vxe.data';
import { useData, useRefs } from './hooks/useData';
import { useColumns } from './hooks/useColumns';
import { useColumnsCache } from './hooks/useColumnsCache';
import { useMethods } from './hooks/useMethods';
import { useDataSource } from './hooks/useDataSource';
import { useDragSort } from './hooks/useDragSort';
import { useRenderComponents } from './hooks/useRenderComponents';
import { useFinallyProps } from './hooks/useFinallyProps';
import { JVxeTableProps } from './types';
import { Spin } from 'ant-design-vue';
import './style/index.less';

export default defineComponent({
  name: 'JVxeTable',
  inheritAttrs: false,
  props: vxeProps(),
  emits: [...vxeEmits],
  setup(props: JVxeTableProps, context) {
    const instanceRef = ref();
    const refs = useRefs();
    const slots = useSlots();
    const data = useData(props);
    const { methods, publicMethods, created } = useMethods(props, context, data, refs, instanceRef);
    created();
    useColumns(props, data, methods, slots);
    useDataSource(props, data, methods, refs);
    useDragSort(props, methods);
    // Giải thích logic mã: 【QQYUN-8566] JVXETable không thể nhớ cài đặt cột
    const { initSetting } = useColumnsCache({ cacheColumnsKey: props.cacheColumnsKey });
    initSetting(props);
    // Cuối cùng được truyền vào template props
    const finallyProps = useFinallyProps(props, data, methods);
    // Kết xuất component con
    const renderComponents = useRenderComponents(props, data, methods, slots);
    // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
    // Lưu trữ tham chiếu component trong giai đoạn setup, tránh mỗi lần render gọi resolveComponent để tìm kiếm
    const aSpinComp = Spin;
    const vxeGridComp = resolveComponent('vxe-grid');
    // Gộp vxeProps và data thành một computed, tránh việc render mỗi lần spread tạo ra đối tượng mới
    const vxeGridProps = computed(() => ({
      ...finallyProps.vxeProps.value,
      data: data.vxeDataSource.value,
    }));
    // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
    return {
      instanceRef,
      ...refs,
      ...publicMethods,
      ...finallyProps,
      ...renderComponents,
      vxeDataSource: data.vxeDataSource,
      aSpinComp,
      vxeGridComp,
      vxeGridProps,
    };
  },
  render() {
    return h(
      'div',
      {
        class: this.$attrs.class,
        style: this.$attrs.style,
      },
      h(
        this.aSpinComp,
        {
          spinning: this.loading,
          wrapperClassName: this.prefixCls,
        },
        {
          default: () => [
            this.renderSubPopover(),
            this.renderToolbar(),
            this.renderToolbarAfterSlot(),
            h(
              this.vxeGridComp,
              this.vxeGridProps,
              this.$slots
            ),
            this.renderPagination(),
            this.renderDetailsModal(),
          ],
        }
      )
    );
  },
  created() {
    this.instanceRef = this;
  },
});
