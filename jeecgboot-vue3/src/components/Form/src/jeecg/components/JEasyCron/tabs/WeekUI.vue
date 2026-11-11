<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">Không đặt</a-radio>
        <span class="tip-info">Ngày và tuần chỉ được đặt một trong hai</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">Khoảng</a-radio>
        <span> Từ </span>
        <a-select v-model:value="valueRange.start" :options="weekOptions" v-bind="typeRangeSelectAttrs" />
        <span> Đến </span>
        <a-select v-model:value="valueRange.end" :options="weekOptions" v-bind="typeRangeSelectAttrs" />
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">Lặp lại</a-radio>
        <span> Từ </span>
        <a-select v-model:value="valueLoop.start" :options="weekOptions" v-bind="typeLoopSelectAttrs" />
        <span> Bắt đầu, khoảng cách </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> Ngày </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">Chỉ định</a-radio>
        <div class="list list-cn">
          <a-checkbox-group v-model:value="valueList">
            <template v-for="opt in weekOptions" :key="i">
              <a-checkbox :value="opt.value" v-bind="typeSpecifyAttrs">{{ opt.label }}</a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { computed, watch, defineComponent } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { useTabProps, useTabEmits, useTabSetup, TypeEnum } from './useTabMixin';

  const WEEK_MAP_EN = {
    '1': 'SUN',
    '2': 'MON',
    '3': 'TUE',
    '4': 'WED',
    '5': 'THU',
    '6': 'FRI',
    '7': 'SAT',
  };

  const WEEK_MAP_CN = {
    '1': 'Chủ nhật',
    '2': 'Thứ Hai',
    '3': 'Thứ Ba',
    '4': 'Thứ Tư',
    '5': 'Thứ Năm',
    '6': 'Thứ Sáu',
    '7': 'Thứ Bảy',
  };

  export default defineComponent({
    name: 'WeekUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '?',
      props: {
        day: { type: String, default: '*' },
      },
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const disabledChoice = computed(() => {
        return (props.day && props.day !== '?') || props.disabled;
      });
      const setup = useTabSetup(props, context, {
        defaultType: TypeEnum.unset,
        defaultValue: '?',
        minValue: 1,
        maxValue: 7,
        // 0,7biểu thị Chủ nhật, 1 biểu thị Thứ hai
        valueRange: { start: 1, end: 7 },
        valueLoop: { start: 2, interval: 1 },
        disabled: disabledChoice,
      });
      const weekOptions = computed(() => {
        let options: { label: string; value: number }[] = [];
        for (let weekKey of Object.keys(WEEK_MAP_CN)) {
          let weekName: string = WEEK_MAP_CN[weekKey];
          options.push({
            value: Number.parseInt(weekKey),
            label: weekName,
          });
        }
        return options;
      });

      const typeRangeSelectAttrs = computed(() => ({
        class: ['w80'],
        disabled: setup.typeRangeAttrs.value.disabled,
      }));

      const typeLoopSelectAttrs = computed(() => ({
        class: ['w80'],
        disabled: setup.typeLoopAttrs.value.disabled,
      }));

      watch(
        () => props.day,
        () => {
          setup.updateValue(disabledChoice.value ? '?' : setup.computeValue.value);
        }
      );

      return {
        ...setup,
        weekOptions,
        typeLoopSelectAttrs,
        typeRangeSelectAttrs,
        WEEK_MAP_CN,
        WEEK_MAP_EN,
      };
    },
  });
</script>
