<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  autocomplete: { type: String, default: '' },
  id: { type: String, default: '' },
  inputClass: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])

const showPassword = ref(false)
</script>

<template>
  <div class="password-field">
    <input
      :id="props.id"
      :type="showPassword ? 'text' : 'password'"
      class="password-field__input"
      :class="props.inputClass"
      :value="props.modelValue"
      :placeholder="props.placeholder"
      :autocomplete="props.autocomplete"
      :disabled="props.disabled"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <button
      type="button"
      class="password-field__toggle"
      @click="showPassword = !showPassword"
    >
      <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
        <circle cx="12" cy="12" r="3"/>
      </svg>
      <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
        <line x1="1" y1="1" x2="23" y2="23"/>
      </svg>
    </button>
  </div>
</template>

<style lang="scss" scoped>
.password-field {
  position: relative;
  width: 100%;

  &__input {
    width: 100%;
    padding: 10px 42px 10px 14px;
    border: 1px solid $color-border;
    border-radius: $radius-pill;
    font-size: $font-md;
    background: #fff;
    transition: border-color 0.2s;
    box-sizing: border-box;
    &:focus { outline: none; border-color: $color-primary; }
    &::placeholder { color: $color-text-faint; }
    &:disabled { background: $color-bg; color: $color-text-secondary; cursor: default; }
  }

  &__toggle {
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    cursor: pointer;
    color: $color-text-muted;
    padding: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: color 0.15s;
    &:hover { color: $color-dark; }
  }
}
</style>
