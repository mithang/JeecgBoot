<template>
  <div class="model-params-popover">
    <div class="params" v-if="type === 'model'">
      <span style="font-size:14px">Tham số</span>
      <a-select value="Tải đặt sẵn" style="width: 96px" size="small" @change="onLoadPreset">
        <a-select-option v-for="(preset, idx) of presets" :value="idx" :key="idx">
          <a-space>
            <Icon :icon="preset.icon" />
            <span>{{ preset.name }}</span>
          </a-space>
        </a-select-option>
      </a-select>
    </div>
    <!-- Nhiệt độ mô hình -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Nhiệt độ mô hình</span>
        <a-tooltip :title="tips.temperature">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="temperatureEnable" size="small"/>
        <a-slider v-bind="temperatureProps" v-model:value="model.temperature" :disabled="model['temperature'] === null"/>
        <a-input-number v-bind="temperatureProps" v-model:value="model.temperature" :disabled="model['temperature'] === null"/>
      </a-space>
    </div>
    <!-- Thuộc tính từ vựng -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Thuộc tính từ vựng</span>
        <a-tooltip :title="tips.topP">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="topPEnable" size="small"/>
        <a-slider v-bind="topPProps" v-model:value="model.topP" :disabled="model['topP'] === null"/>
        <a-input-number v-bind="topPProps" v-model:value="model.topP" :disabled="model['topP'] === null"/>
      </a-space>
    </div>
    <!-- Thuộc tính chủ đề -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Thuộc tính chủ đề</span>
        <a-tooltip :title="tips.presencePenalty">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="presencePenaltyEnable" size="small" />
        <a-slider v-bind="presencePenaltyProps" v-model:value="model.presencePenalty" :disabled="model['presencePenalty'] === null"/>
        <a-input-number v-bind="presencePenaltyProps" v-model:value="model.presencePenalty" :disabled="model['presencePenalty'] === null"/>
      </a-space>
    </div>
    <!-- Thuộc tính lặp lại -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Thuộc tính lặp lại</span>
        <a-tooltip :title="tips.frequencyPenalty">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="frequencyPenaltyEnable" size="small" />
        <a-slider v-bind="frequencyPenaltyProps" v-model:value="model.frequencyPenalty" :disabled="model['frequencyPenalty'] === null"/>
        <a-input-number v-bind="frequencyPenaltyProps" v-model:value="model.frequencyPenalty" :disabled="model['frequencyPenalty'] === null"/>
      </a-space>
    </div>
    <!-- Trả lời tối đa -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Trả lời tối đa</span>
        <a-tooltip :title="tips.maxTokens">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="maxTokensEnable" size="small" />
        <a-slider v-bind="maxTokensProps" v-model:value="model.maxTokens" :disabled="model['maxTokens'] === null"/>
        <a-input-number v-bind="maxTokensProps" v-model:value="model.maxTokens" :disabled="model['maxTokens'] === null"/>
      </a-space>
    </div>
    <!-- timeout quá thời gian chờ -->
    <div class="setting-item" v-if="type === 'model'">
      <div class="label">
        <span>Thời gian quá thời gian chờ</span>
        <a-tooltip :title="tips.timeout">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="timeoutEnable" size="small" />
        <a-slider v-bind="timeoutProps" v-model:value="model.timeout" :disabled="model['timeout'] === null"/>
        <a-input-number v-bind="timeoutProps" v-model:value="model.timeout" :disabled="model['timeout'] === null"/>
      </a-space>
    </div>
    <!-- top k -->
    <div class="setting-item" v-if="type === 'knowledge'">
      <div class="label">
        <span>Top K</span>
        <a-tooltip :title="tips.topNumber">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="topNumberEnable" size="small" />
        <a-slider v-bind="topNumberProps" v-model:value="model.topNumber" :disabled="model['topNumber'] === null"/>
        <a-input-number v-bind="topNumberProps" v-model:value="model.topNumber" :disabled="model['topNumber'] === null"/>
      </a-space>
    </div>
    <!-- Ngưỡng Score -->
    <div class="setting-item" v-if="type === 'knowledge'">
      <div class="label">
        <span>Ngưỡng Score</span>
        <a-tooltip :title="tips.similarity">
          <Icon icon="ant-design:question-circle" />
        </a-tooltip>
      </div>
      <a-space>
        <a-switch v-model:checked="similarityEnable" size="small" />
        <a-slider v-bind="similarityProps" v-model:value="model.similarity" :disabled="model['similarity'] === null"/>
        <a-input-number v-bind="similarityProps" v-model:value="model.similarity" :disabled="model['similarity'] === null"/>
      </a-space>
    </div>
  </div>
</template>

<script lang="ts">
  import { ref, computed } from 'vue';
  import { cloneDeep, omit } from 'lodash-es';

  export default {
    name: 'AiModelSeniorForm',
    components: {},
    props: {
      modelParams: {
        type: Object,
        default: {}
      },
      type: {
        type: String,
        default: 'model'
      }
    },
    emits: ['success', 'register', 'updateModel'],
    setup(props, { emit }) {
      // Tham số đặt sẵn
      const presets = [
        {
          name: 'Sáng tạo',
          icon: 'fxemoji:star',
          params: {
            temperature: 0.8,
            topP: 0.9,
            presencePenalty: 0.1,
            frequencyPenalty: 0.1,
            maxTokens: null,
            timeout: 60,
          },
        },
        {
          name: 'Cân bằng',
          icon: 'noto:balance-scale',
          params: {
            temperature: 0.5,
            topP: 0.8,
            presencePenalty: 0.2,
            frequencyPenalty: 0.3,
            maxTokens: null,
            timeout: 60,
          },
        },
        {
          name: 'Chính xác',
          icon: 'twemoji:direct-hit',
          params: {
            temperature: 0.2,
            topP: 0.7,
            presencePenalty: 0.5,
            frequencyPenalty: 0.5,
            maxTokens: null,
            timeout: 60,
          },
        },
      ];

      // Giới thiệu tham số
      const tips = {
        temperature: 'Giá trị càng lớn, nội dung trả lời càng đa dạng, sáng tạo và ngẫu nhiên; đặt là 0 thì trả lời dựa trên sự thật, muốn có đáp án chính xác nên giảm tham số này; trò chuyện hằng ngày gợi ý 0.5-0.8.',
        topP: 'Giá trị càng nhỏ, nội dung Ai tạo ra càng đơn điệu và cũng dễ hiểu hơn; giá trị càng lớn, phạm vi từ vựng trong trả lời của Ai càng lớn, càng đa dạng.',
        presencePenalty: 'Giá trị càng lớn, càng giúp Ai kiểm soát việc đưa vào chủ đề mới tốt hơn, gợi ý tinh chỉnh nhẹ hoặc giữ nguyên.',
        frequencyPenalty: 'Giá trị càng lớn, càng giúp Ai tránh lặp lại những gì đã nói trước đó tốt hơn, gợi ý tinh chỉnh nhẹ hoặc giữ nguyên.',
        maxTokens:
          'Đặt kích thước nội dung trả lời tối đa của Ai, sẽ ảnh hưởng đến độ dài kết quả trả về. Trò chuyện thông thường gợi ý 500-800; tạo văn ngắn gợi ý 800-2000; tạo mã gợi ý 2000-3600; tạo văn dài gợi ý khoảng 4000 (hoặc chọn mô hình trả lời dài)',
        topNumber: 'Dùng để lọc ra các đoạn văn bản có độ tương đồng cao nhất với câu hỏi của người dùng. Hệ thống đồng thời sẽ điều chỉnh động số lượng phân đoạn theo kích thước cửa sổ ngữ cảnh của mô hình đã chọn.',
        similarity: 'Dùng để đặt ngưỡng độ tương đồng cho việc lọc đoạn văn bản.',
        timeout: 'Thời gian chờ phản hồi AI tối đa, đơn vị là giây.'
      };

      // Tham số: nhiệt độ
      const temperatureProps = ref<any>({
        min: 0.1,
        max: 1,
        step: 0.1,
      });

      // Tham số: thuộc tính từ vựng
      const topPProps = ref<any>({
        min: 0.1,
        max: 1,
        step: 0.1,
      });
      // Tham số: thuộc tính chủ đề
      const presencePenaltyProps = ref<any>({
        min: -2,
        max: 2,
        step: 0.1,
      });
      // Tham số: thuộc tính lặp lại
      const frequencyPenaltyProps = ref<any>({
        min: -2,
        max: 2,
        step: 0.1,
      });
      // Tham số: trả lời tối đa
      const maxTokensProps = ref<any>({
        min: 1,
        max: 16000,
        step: 1,
      });    
      
      // Tham số: topk
      const topNumberProps = ref<any>({
        min: 1,
        max: 10,
        step: 1,
      });     
      
      // Tham số: Ngưỡng Score
      const similarityProps = ref<any>({
        min: 0.1,
        max: 1,
        step: 0.1,
      });
      
      //Tham số: thời gian quá thời gian chờ
      const timeoutProps = ref<any>({
        min: 1,
        step: 1,
        max: 3600,
      })
      
      
      //Đối tượng tham số
      const model = ref<any>(props.modelParams || {})
      
      //Nhiệt độ mô hình có được chọn hay không
      const temperatureEnable = computed({
        get:()=> model.value.temperature != null,
        set:(value) => model.value.temperature = !value? null: 0.7
      });    
      
      //Thuộc tính từ vựng có được chọn hay không
      const topPEnable = computed({
        get:()=> model.value.topP != null,
        set:(value) => model.value.topP = !value? null: 0
      });
      
      //Thuộc tính từ vựng có được chọn hay không
      const presencePenaltyEnable = computed({
        get:()=> model.value.presencePenalty != null,
        set:(value) => model.value.presencePenalty = !value? null: 0
      });  
      
      //Thuộc tính lặp lại có được chọn hay không
      const frequencyPenaltyEnable = computed({
        get:()=> model.value.frequencyPenalty != null,
        set:(value) => model.value.frequencyPenalty = !value? null: 0
      });   
      
      //Trả lời tối đa
      const maxTokensEnable = computed({
        get:()=> model.value.maxTokens != null,
        set:(value) => model.value.maxTokens = !value? null: 520
      });
        
      //top k
      const topNumberEnable = computed({
        get:()=> model.value.topNumber != null,
        set:(value) => model.value.topNumber = !value? null: 4
      });   
      
      //Ngưỡng Score
      const similarityEnable = computed({
        get:()=> model.value.similarity != null,
        set:(value) => model.value.similarity = !value? null: 0.74
      });    
      
      //timeoutThời gian quá thời gian chờ
      const timeoutEnable = computed({
        get:()=> model.value.timeout != null,
        set:(value) => model.value.timeout = !value? null: 60
      });
      
      // Tải đặt sẵn
      function onLoadPreset(idx: number) {
        const preset = presets[idx];
        if (!preset) {
          return;
        }
        model.value = preset.params;
      }

      /**
       * Cập nhật tham số
       *
       * @param model
       */
      function emitChange() {
        return model.value;
      }

      /**
       * Đặt giá trị modal
       * @param values
       */
      function setModalParams(values){
        model.value = values
      }
      
      return {
        presets,
        onLoadPreset,
        tips,
        temperatureProps,
        topPProps,
        presencePenaltyProps,
        model,
        frequencyPenaltyProps,
        temperatureEnable,
        maxTokensProps,
        emitChange,
        topPEnable,
        presencePenaltyEnable,
        frequencyPenaltyEnable,
        maxTokensEnable,
        topNumberEnable,
        topNumberProps,
        similarityEnable,
        similarityProps,
        setModalParams,
        timeoutEnable,
        timeoutProps,
      };
    },
  };
</script>

<style lang="less" scoped>
  .model-params-popover {
    font-size: 14px;
    width: 100%;
     .params{
       display: flex;
       justify-content: space-between;
     } 
    .setting-item{
      margin-top: 10px;
    }
    .setting-item .label {
      > span {
        vertical-align: middle;

        &.app-iconify {
          cursor: help;
          color: #888888;
        }
      }
    }

    .ant-space {
      .ant-slider {
        width: 380px;
      }

      .ant-input-number {
        width: 110px;
        min-width: 80px;
      }
    }
  }
</style>
