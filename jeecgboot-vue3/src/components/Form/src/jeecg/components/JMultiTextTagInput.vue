<template>
  <div class="multi-text-tag-input" :class="{ 'is-focused': isFocused }" @click="focusInput">
    <span v-for="(tag, index) in tags" :key="index" class="mtti-tag">
      <span class="mtti-tag-text">{{ tag }}</span>
      <span class="mtti-tag-close" @click.stop="removeTag(index)">×</span>
    </span>
    <input
      ref="inputRef"
      v-model="inputValue"
      class="mtti-input"
      :placeholder="tags.length === 0 ? placeholder : ''"
      @focus="onFocus"
      @blur="onBlur"
      @keydown.enter.prevent="handleEnter"
      @keydown="handleKeydown"
      @compositionstart="isComposing = true"
      @compositionend="isComposing = false"
    />
    <div v-if="showDropdown" class="mtti-dropdown">
      <div class="mtti-dropdown-item" @mousedown.prevent="handleDropdownClick">
        Sử dụng"{{ inputValue }}"
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, nextTick } from 'vue';

export default defineComponent({
  name: 'JMultiTextTagInput',
  props: {
    value: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      default: 'Sau khi nhập nhấn Enter để thêm, dùng dấu phẩy để tách nhiều từ khóa',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['change', 'update:value'],
  setup(props, {emit}) {
    const inputRef = ref<HTMLInputElement | null>(null);
    const inputValue = ref('');
    const isFocused = ref(false);
    const isComposing = ref(false);

    // Chuyển chuỗi phân tách bằng dấu phẩy thành mảng
    const tags = computed(() => {
      if (!props.value || props.value.trim() === '') {
        return [];
      }
      return props.value.split(',').map(s => s.trim()).filter(s => s !== '');
    });

    const showDropdown = computed(() => {
      return isFocused.value && inputValue.value && inputValue.value.trim() !== '';
    });

    // Phát sự kiện thay đổi
    function emitChange(newTags: string[]) {
      const val = newTags.filter(s => s.trim() !== '').join(',');
      emit('update:value', val);
      emit('change', val);
    }

    // Thêm một hoặc nhiều nhãn
    function addTags(values: string[]) {
      const existing = [...tags.value];
      for (const v of values) {
        const trimmed = v.trim();
        if (trimmed && !existing.includes(trimmed)) {
          existing.push(trimmed);
        }
      }
      emitChange(existing);
    }

    // Thêm một thẻ
    function addTag(value: string) {
      addTags([value]);
    }

    // Xóa nhãn
    function removeTag(index: number) {
      const newTags = [...tags.value];
      newTags.splice(index, 1);
      emitChange(newTags);
    }

    // Xử lý phím Enter
    function handleEnter() {
      if (isComposing.value) {
        return;
      }
      const text = inputValue.value.trim();
      if (text) {
        // Kiểm tra có chứa dấu phẩy hay không, tự động tách
        if (text.includes(',')) {
          const parts = text.split(',').map(s => s.trim()).filter(s => s !== '');
          addTags(parts);
        } else {
          addTag(text);
        }
        inputValue.value = '';
      }
    }

    // Xử lý phím bấm: dấu phẩy tự động tách (dấu phẩy tiếng Trung và dấu phẩy tiếng Anh)
    function handleKeydown(e: KeyboardEvent) {
      if (isComposing.value) {
        return;
      }
      // Nếu nhập dấu phẩy (dấu phẩy tiếng Anh hoặc dấu phẩy tiếng Trung), tự động tách
      if (e.key === ',' || e.key === '，') {
        e.preventDefault();
        const text = inputValue.value.trim();
        if (text) {
          addTag(text);
          inputValue.value = '';
        }
      }
      // Phím Backspace: nếu ô nhập trống, xóa thẻ cuối cùng
      if (e.key === 'Backspace' && inputValue.value === '' && tags.value.length > 0) {
        removeTag(tags.value.length - 1);
      }
    }

    // Nhấp tùy chọn dropdown
    function handleDropdownClick() {
      const text = inputValue.value.trim();
      if (text) {
        addTag(text);
        inputValue.value = '';
        nextTick(() => {
          inputRef.value?.focus();
        });
      }
    }

    function onFocus() {
      isFocused.value = true;
    }

    function onBlur() {
      // Đóng chậm, để mousedown có cơ hội kích hoạt
      setTimeout(() => {
        isFocused.value = false;
        // Khi mất focus sẽ tự động thêm nội dung đang nhập
        const text = inputValue.value.trim();
        if (text) {
          if (text.includes(',')) {
            const parts = text.split(',').map(s => s.trim()).filter(s => s !== '');
            addTags(parts);
          } else {
            addTag(text);
          }
          inputValue.value = '';
        }
      }, 200);
    }

    function focusInput() {
      inputRef.value?.focus();
    }

    return {
      inputRef,
      inputValue,
      isFocused,
      isComposing,
      tags,
      showDropdown,
      handleEnter,
      handleKeydown,
      handleDropdownClick,
      onFocus,
      onBlur,
      focusInput,
      removeTag,
    };
  },
});
</script>

<style scoped lang="less">
.multi-text-tag-input {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  min-height: 32px;
  padding: 2px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
  cursor: text;
  transition: border-color 0.3s;
  gap: 4px;

  &:hover {
    border-color: #4096ff;
  }

  &.is-focused {
    border-color: #4096ff;
    box-shadow: 0 0 0 2px rgba(5, 145, 255, 0.1);
  }
}

.mtti-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 6px;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  max-width: 200px;
  overflow: hidden;
}

.mtti-tag-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 160px;
}

.mtti-tag-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
  margin-left: 4px;
  cursor: pointer;
  color: #999;
  font-size: 12px;
  line-height: 1;
  border-radius: 50%;
  transition: all 0.2s;

  &:hover {
    color: #fff;
    background: #ff4d4f;
  }
}

.mtti-input {
  flex: 1;
  min-width: 60px;
  height: 24px;
  border: none;
  outline: none;
  font-size: 13px;
  color: #333;
  background: transparent;
  padding: 0 2px;

  &::placeholder {
    color: #bfbfbf;
  }
}

.mtti-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08);
  z-index: 1050;
  overflow: hidden;
}

.mtti-dropdown-item {
  padding: 6px 12px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f5f5;
  }
}
</style>
