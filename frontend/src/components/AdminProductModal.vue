<script setup lang="ts">
import api from '@/api/auth'
import { ref, computed, watch, onMounted } from 'vue'

interface Catalog    { id: number; name: string; slug: string }
interface SubCatalog { id: number; catalogId: number; name: string; slug: string }
interface Product    { id: number; name: string; description?: string; price: number; quantity: number; catalogId?: number | null; subCatalogId?: number | null; imageUrls: string[]; characteristics: Record<string, string> }
interface Char       { name: string; type: string }

const props = defineProps<{ product: Product | null }>()

const emit = defineEmits<{
  close:   []
  saved:   []
  deleted: []
}>()

const catalogs    = ref<Catalog[]>([])
const subCatalogs = ref<SubCatalog[]>([])

const productForm    = ref(emptyForm())
const chars          = ref<Char[]>([])
const selectedFiles  = ref<File[]>([])
const newPreviewUrls = ref<string[]>([])

const saving         = ref(false)
const deleting       = ref(false)
const removingImgUrl = ref<string | null>(null)
const errorMsg       = ref('')

const formSubs = computed(() =>
  productForm.value.catalogId
    ? subCatalogs.value.filter(s => s.catalogId === productForm.value.catalogId)
    : []
)

function emptyForm() {
  return { id: null as number | null, name: '', description: '', price: 0, quantity: 1, catalogId: null as number | null, subCatalogId: null as number | null, imageUrls: [] as string[] }
}

watch(() => props.product, (p) => {
  errorMsg.value = ''
  clearNewPreviews()
  selectedFiles.value = []
  if (p) {
    productForm.value = {
      ...p,
      description:  p.description  ?? '',
      catalogId:    p.catalogId    ?? null,
      subCatalogId: p.subCatalogId ?? null,
      imageUrls:    [...(p.imageUrls ?? [])],
    }
    chars.value = Object.entries(p.characteristics ?? {}).map(([name, type]) => ({ name, type }))
  } else {
    productForm.value = emptyForm()
    chars.value = []
  }
}, { immediate: true })

onMounted(async () => {
  const [c, s] = await Promise.all([api.get('/admin/catalogs'), api.get('/admin/subcatalogs')])
  catalogs.value    = c.data
  subCatalogs.value = s.data
})

function clearNewPreviews() {
  newPreviewUrls.value.forEach(u => URL.revokeObjectURL(u))
  newPreviewUrls.value = []
}

function onFilesSelected(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files) return
  Array.from(input.files).forEach(f => {
    selectedFiles.value.push(f)
    newPreviewUrls.value.push(URL.createObjectURL(f))
  })
  input.value = ''
}

function removeNewImage(i: number) {
  URL.revokeObjectURL(newPreviewUrls.value[i])
  newPreviewUrls.value.splice(i, 1)
  selectedFiles.value.splice(i, 1)
}

function onPaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        selectedFiles.value.push(file)
        newPreviewUrls.value.push(URL.createObjectURL(file))
      }
    }
  }
}

async function removeExistingImage(imageUrl: string) {
  if (!productForm.value.id) return
  removingImgUrl.value = imageUrl
  try {
    const res = await api.delete(`/admin/products/${productForm.value.id}/images`, { params: { imageUrl } })
    productForm.value.imageUrls = res.data.imageUrls
  } catch (e) { errorMsg.value = apiError(e) }
  finally { removingImgUrl.value = null }
}

async function save() {
  if (!productForm.value.name.trim()) { errorMsg.value = 'Введите название товара'; return }
  errorMsg.value = ''
  saving.value = true
  try {
    const characteristics = Object.fromEntries(
      chars.value.filter(c => c.name.trim()).map(c => [c.name.trim(), c.type.trim()])
    )
    const payload = { ...productForm.value, characteristics }
    let savedId: number
    if (payload.id) {
      await api.put(`/admin/products/${payload.id}`, payload)
      savedId = payload.id
    } else {
      const res = await api.post('/admin/products', payload)
      savedId = res.data.id
    }
    if (selectedFiles.value.length > 0) {
      const form = new FormData()
      selectedFiles.value.forEach(f => form.append('files', f))
      await api.post(`/admin/products/${savedId}/images`, form)
    }
    emit('saved')
    close()
  } catch (e) { errorMsg.value = apiError(e) }
  finally { saving.value = false }
}

async function deleteProduct() {
  if (!productForm.value.id || !confirm('Удалить товар?')) return
  errorMsg.value = ''
  deleting.value = true
  try {
    await api.delete(`/admin/products/${productForm.value.id}`)
    emit('deleted')
    close()
  } catch (e) { errorMsg.value = apiError(e) }
  finally { deleting.value = false }
}

function addChar()         { chars.value.push({ name: '', type: '' }) }
function removeChar(i: number) { chars.value.splice(i, 1) }

function close() {
  if (saving.value || deleting.value) return
  clearNewPreviews()
  emit('close')
}

function apiError(e: any): string {
  const s = e?.response?.status
  if (s === 401 || s === 403) return 'Нет доступа'
  if (s === 404) return 'Элемент не найден'
  if (s === 409) return e?.response?.data?.message ?? 'Уже существует'
  return e?.response?.data?.message ?? e?.response?.data ?? 'Ошибка сервера'
}
</script>

<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="modal modal--wide">
      <h3 class="modal-title">{{ productForm.id ? 'Изменение товара' : 'Добавление товара' }}</h3>

      <div v-if="errorMsg" class="modal-error">{{ errorMsg }}</div>

      <!-- Characteristics -->
      <div class="char-section">
        <div class="char-section__header">
          <span class="char-section__label">Характеристики:</span>
          <button class="btn btn--green btn--sm" @click="addChar">+ Добавить</button>
        </div>
        <div v-if="chars.length" class="char-list">
          <div v-for="(c, i) in chars" :key="i" class="char-row">
            <input v-model="c.name" class="form-input char-input" placeholder="Название" />
            <span class="char-sep">:</span>
            <input v-model="c.type" class="form-input char-input" placeholder="Значение" />
            <button class="char-delete" @click="removeChar(i)" title="Удалить">&#10005;</button>
          </div>
        </div>
        <p v-else class="char-empty">Нет характеристик</p>
      </div>

      <!-- Form fields -->
      <div class="form-grid">
        <label class="form-row">
          <span class="form-label">Название:</span>
          <input v-model="productForm.name" class="form-input" />
        </label>
        <label class="form-row form-row--top">
          <span class="form-label">Описание:</span>
          <textarea v-model="productForm.description" class="form-input form-textarea" rows="5" placeholder="Введите описание товара..."></textarea>
        </label>
        <label class="form-row">
          <span class="form-label">Цена (₽):</span>
          <input v-model.number="productForm.price" type="number" min="0" step="0.01" class="form-input" />
        </label>
        <label class="form-row">
          <span class="form-label">Количество:</span>
          <input v-model.number="productForm.quantity" type="number" min="0" class="form-input" />
        </label>
        <label class="form-row">
          <span class="form-label">Каталог:</span>
          <select v-model="productForm.catalogId" class="form-input" @change="productForm.subCatalogId = null">
            <option :value="null">— не выбран —</option>
            <option v-for="c in catalogs" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </label>
        <label class="form-row">
          <span class="form-label">Подкаталог:</span>
          <select v-model="productForm.subCatalogId" class="form-input">
            <option :value="null">— не выбран —</option>
            <option v-for="s in formSubs" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </label>
      </div>

      <!-- Image upload -->
      <div class="image-upload-section" @paste="onPaste">
        <p class="image-upload-label">Фотографии товара: <span class="paste-hint">Ctrl+V для вставки</span></p>
        <div class="image-grid" v-if="productForm.imageUrls.length || newPreviewUrls.length">
          <div v-for="url in productForm.imageUrls" :key="url" class="image-thumb">
            <img :src="url" alt="" />
            <button class="image-thumb__remove" :disabled="removingImgUrl === url" @click="removeExistingImage(url)" title="Удалить">
              {{ removingImgUrl === url ? '…' : '✕' }}
            </button>
          </div>
          <div v-for="(url, i) in newPreviewUrls" :key="'new-' + i" class="image-thumb image-thumb--new">
            <img :src="url" alt="" />
            <button class="image-thumb__remove" @click="removeNewImage(i)" title="Удалить">&#10005;</button>
          </div>
        </div>
        <label class="upload-btn">
          <input type="file" accept="image/*" multiple hidden @change="onFilesSelected" />
          + Добавить фото
        </label>
      </div>

      <!-- Actions -->
      <div class="form-actions">
        <button class="btn btn--green" :disabled="saving" @click="save">
          <span v-if="saving" class="btn-spinner"></span>
          {{ saving ? 'Сохранение...' : (productForm.id ? 'Сохранить' : 'Добавить') }}
        </button>
        <button v-if="productForm.id" class="btn btn--red" :disabled="saving || deleting" @click="deleteProduct">
          <span v-if="deleting" class="btn-spinner"></span>
          {{ deleting ? 'Удаление...' : 'Удалить' }}
        </button>
        <button class="btn btn--orange" :disabled="saving || deleting" @click="close">Отмена</button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  @include flex-center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: $radius-md;
  padding: 28px 32px;
  width: 380px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.18);

  @include below-xs { width: calc(100vw - 32px); padding: $gap-md; }
  &--wide { width: 560px; @include below-sm { width: calc(100vw - 32px); } }
}

.modal-title {
  text-align: center;
  font-size: $font-lg;
  font-weight: 600;
  margin-bottom: $gap-md;
}

.modal-error {
  background: #fce4ec;
  color: #c62828;
  border: 1px solid #ef9a9a;
  border-radius: $radius-sm;
  padding: $gap-sm 12px;
  font-size: $font-base;
  margin-bottom: 14px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 30px;
  padding: 0 $gap-md;
  border: none;
  border-radius: $radius-sm;
  font-size: $font-base;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: filter 0.15s;

  &:hover:not(:disabled) { filter: brightness(0.9); }
  &:disabled { opacity: 0.45; cursor: not-allowed; }

  &--green  { background: #2ecc40; color: #fff; }
  &--orange { background: $color-primary; color: #fff; }
  &--red    { background: $color-danger; color: #fff; }
  &--sm     { height: 24px; padding: 0 10px; font-size: $font-sm; }
}

.btn-spinner {
  display: inline-block;
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: $radius-full;
  animation: spin 0.55s linear infinite;
  flex-shrink: 0;
}

@keyframes spin { to { transform: rotate(360deg); } }

.char-section {
  margin-bottom: 14px;

  &__header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: $gap-sm;
  }

  &__label { font-size: $font-base; color: #333; font-weight: 500; }
}

.char-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 480px;
}

.char-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.char-input { flex: 1; }

.char-sep { font-size: $font-md; color: $color-text-muted; flex-shrink: 0; }

.char-delete {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: $radius-full;
  background: #fce;
  color: $color-danger;
  font-size: $font-xs;
  cursor: pointer;
  flex-shrink: 0;
  @include flex-center;
  padding: 0;
  transition: background 0.15s;

  &:hover { background: $color-danger; color: #fff; }
}

.char-empty { font-size: $font-sm; color: $color-text-faint; margin: 0; }

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 480px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;

  &--top {
    align-items: flex-start;
    .form-label { padding-top: 6px; }
  }
}

.form-label {
  width: 100px;
  flex-shrink: 0;
  font-size: $font-base;
  color: #333;
  text-align: right;
}

.form-input {
  flex: 1;
  height: 30px;
  padding: 0 $gap-sm;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  font-size: $font-base;
  font-family: inherit;

  &:focus { outline: none; border-color: $color-primary; }
  &:disabled { background: $color-bg-light; }
}

select.form-input { height: 32px; }

.form-textarea {
  height: auto;
  padding: 6px $gap-sm;
  resize: vertical;
  line-height: 1.5;
}

.image-upload-section {
  max-width: 480px;
  margin-top: $gap-md;
  margin-bottom: 4px;
}

.image-upload-label { font-size: $font-base; color: #333; margin-bottom: $gap-sm; }

.paste-hint {
  font-size: $font-xs;
  color: $color-text-muted;
  font-weight: 400;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $gap-sm;
  margin-bottom: 10px;
}

.image-thumb {
  position: relative;
  width: 80px;
  height: 80px;
  border: 1px solid $color-border;
  border-radius: $radius-sm;
  overflow: hidden;
  flex-shrink: 0;

  img { width: 100%; height: 100%; object-fit: cover; }

  &--new { border-color: $color-primary; }

  &__remove {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 18px;
    height: 18px;
    border: none;
    border-radius: $radius-full;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    font-size: 10px;
    line-height: 1;
    cursor: pointer;
    @include flex-center;
    padding: 0;

    &:hover { background: $color-danger; }
  }
}

.upload-btn {
  display: inline-block;
  height: 30px;
  padding: 0 14px;
  background: $color-bg-light;
  border: 1px dashed #bbb;
  border-radius: $radius-sm;
  font-size: $font-base;
  color: $color-text;
  cursor: pointer;
  line-height: 30px;
  transition: border-color 0.15s, background 0.15s;

  &:hover { border-color: $color-primary; background: $color-primary-light; }
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: $gap-md;
  justify-content: flex-end;
  flex-wrap: wrap;
}
</style>
