<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">Không đặt</a-radio>
        <span class="tip-info">Ngày và tuần chỉ được đặt một trong hai</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.every" v-bind="beforeRadioAttrs">Mỗi ngày</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">Khoảng</a-radio>
        <span> Từ </span>
        <InputNumber v-model:value="valueRange.start" v-bind="typeRangeAttrs" />
        <span> Ngày đến </span>
        <InputNumber v-model:value="valueRange.end" v-bind="typeRangeAttrs" />
        <span> ngày </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">Lặp lại</a-radio>
        <span> Từ </span>
        <InputNumber v-model:value="valueLoop.start" v-bind="typeLoopAttrs" />
        <span> ngày bắt đầu, khoảng cách </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> ngày </span>
      </div>
<!--       Ngày làm việc tạm thời chưa hỗ trợ, sẽ báo lỗi, đã ẩn trước -->
<!--      <div class="item">-->
<!--        <a-radio :value="TypeEnum.work" v-bind="beforeRadioAttrs">Ngày làm việc</a-radio>-->
<!--        <span> Tháng này </span>-->
<!--        <InputNumber v-model:value="valueWork" v-bind="typeWorkAttrs" />-->
<!--        <span> Ngày, ngày làm việc gần nhất </span>-->
<!--      </div>-->
      <div class="item">
        <a-radio :value="TypeEnum.last" v-bind="beforeRadioAttrs">Ngày cuối cùng</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">Chỉ định</a-radio>
        <div class="list">
          <a-checkbox-group v-model:value="valueList">
            <template v-for="i in specifyRange" :key="i">
              <a-checkbox :value="i" v-bind="typeSpecifyAttrs">{{ i }}</a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, watch } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { TypeEnum, useTabEmits, useTabProps, useTabSetup } from './useTabMixin';

  export default defineComponent({
    name: 'DayUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '*',
      props: {
        week: { type: String, default: '?' },
      },
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const disabledChoice = computed(() => {
        return (props.week && props.week !== '?') || props.disabled;
      });
      const setup = useTabSetup(props, context, {
        defaultValue: '*',
        valueWork: 1,
        minValue: 1,
        maxValue: 31,
        valueRange: { start: 1, end: 31 },
        valueLoop: { start: 1, interval: 1 },
        disabled: disabledChoice,
      });
      const typeWorkAttrs = computed(() => ({
        disabled: setup.type.value !== TypeEnum.work || props.disabled || disabledChoice.value,
        ...setup.inputNumberAttrs.value,
      }));

      watch(
        () => props.week,
        () => {
          setup.updateValue(disabledChoice.value ? '?' : setup.computeValue.value);
        }
      );

      return { ...setup, typeWorkAttrs };
    },
  });
</script>
