import { defineComponent, h, ref, watch } from 'vue';
import { randomString } from '/@/utils/common/compUtils';
import '../style/reload-effect.less';

// Hiệu ứng sửa dữ liệu
export default defineComponent({
  props: {
    vNode: null,
    // Có bật hiệu ứng không
    effect: Boolean,
  },
  emits: ['effectBegin', 'effectEnd'],
  setup(props, { emit }) {
    // vNode: null,
    const innerEffect = ref(props.effect);
    // Ứng phó nhiều hiệu ứng cùng lúc
    const effectIdx = ref(0);
    const effectList = ref<any[]>([]);

    watch(
      () => props.effect,
      () => (innerEffect.value = props.effect)
    );
    watch(
      () => props.vNode,
      (_vNode, old) => {
        if (props.effect && old != null) {
          let topLayer = renderSpan(old, 'top');
          effectList.value.push(topLayer);
        }
      },
      { immediate: true }
    );

    // Nội dung render theo điều kiện span
    function renderVNode() {
      if (props.vNode == null) {
        return null;
      }
      let bottom = renderSpan(props.vNode, 'bottom');
      // Đã bật hiệu ứng và có dữ liệu cũ thì hiển thị lớp hiệu ứng trên cùng
      if (innerEffect.value && effectList.value.length > 0) {
        emit('effectBegin');
        // 1.4s sau đó đóng hiệu ứng
        window.setTimeout(() => {
          let item = effectList.value[effectIdx.value];
          if (item && item.elm) {
            // Sau khi hiệu ứng kết thúc, phần hiển thị trước tiên đặt display thành none, thay vì xóa trực tiếp phần tử đó,
            // Mục đích là để tránh trang hiển thị lại, gây đặt lại animation
            item.elm.style.display = 'none';
          }
          // Khi tất cả animation theo cấp đã kết thúc mới xóa tất cả phần tử
          if (++effectIdx.value === effectList.value.length) {
            innerEffect.value = false;
            effectIdx.value = 0;
            effectList.value = [];
            emit('effectEnd');
          }
        }, 1400);
        return [effectList.value, bottom];
      } else {
        return bottom;
      }
    }

    // Nội dung render span
    function renderSpan(vNode, layer) {
      let options = {
        key: layer + effectIdx.value + randomString(6),
        class: ['j-vxe-reload-effect-span', `layer-${layer}`],
        style: {},
        // Giải thích logic mã: 【issues/1175]Khắc phục vấn đề title hiển thị không đúng sau khi di chuột lên vxetable
        title: vNode,

      };
      if (layer === 'top') {
        // Nội dung render mới nhất ở bên dưới
        options.style['z-index'] = 9999 - effectIdx.value;
      }
      return h('span', options, [vNode]);
    }

    return () =>
      h(
        'div',
        {
          class: ['j-vxe-reload-effect-box'],
        },
        [renderVNode()]
      );
  },
});
