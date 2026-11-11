<script lang="ts">
  import { defineComponent, h } from 'vue';
  import { Empty } from 'ant-design-vue';

  export default defineComponent({
    name: 'ErrorTip',
    props: {
      // Văn bản nhắc nhở
      tip: String,
      // Tên nhãn ở tầng ngoài
      tag: { type: String, default: 'div' },
    },
    render() {
      return h(
        this.tag,
        {
          class: ['error-tip'],
        },
        h(Empty, { description: this.tip })
      );
    },
  });
</script>

<style lang="less" scoped>
  .error-tip {
    padding: 20px;
    background: white;
    text-align: center;
  }
</style>