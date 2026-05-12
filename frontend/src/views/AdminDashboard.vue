<script setup lang="ts">
import api from '@/api/auth'
import { ref, computed, onMounted } from 'vue'

interface Catalog    { id: number; name: string; slug: string; imageUrl?: string }
interface SubCatalog { id: number; catalogId: number; name: string; slug: string; imageUrl?: string }
interface Product    { id: number; name: string; description: string; price: number; quantity: number; catalogId: number | null; subCatalogId: number | null; imageUrls: string[]; characteristics: Record<string, string> }
interface Char       { name: string; type: string }
interface Notification { id: number; msg: string; type: 'error' | 'success' | 'info' }

interface AdminOrderItem { id: number; productId: number; name: string; price: number; quantity: number; imageUrl: string | null }
interface AdminOrder {
  id: number; total: number; deliveryCost: number; status: string; createdAt: string
  deliveryMethod: string; deliveryAddress: string | null
  userEmail: string; userFirstName: string | null; userLastName: string | null; userPhone: string | null
  items: AdminOrderItem[]
}


const activeTab = ref<'catalog' | 'orders'>('catalog')

const catalogs    = ref<Catalog[]>([])
const subCatalogs = ref<SubCatalog[]>([])
const products    = ref<Product[]>([])

// ── orders ──
const orders         = ref<AdminOrder[]>([])
const ordersLoading  = ref(false)
const expandedOrders = ref<Set<number>>(new Set())

// ── order filters ──
const orderFilterUser     = ref('')
const orderSortDir        = ref<'desc' | 'asc'>('desc')
const orderFilterDateFrom = ref('')
const orderFilterDateTo   = ref('')

const filteredOrders = computed(() => {
  let list = orders.value.slice()
  const q = orderFilterUser.value.trim().toLowerCase()
  if (q) {
    list = list.filter(o => {
      const name = `${o.userFirstName ?? ''} ${o.userLastName ?? ''}`.trim().toLowerCase()
      return name.includes(q) || o.userEmail.toLowerCase().includes(q)
    })
  }
  if (orderFilterDateFrom.value) {
    const from = new Date(orderFilterDateFrom.value + 'T00:00:00')
    list = list.filter(o => new Date(o.createdAt) >= from)
  }
  if (orderFilterDateTo.value) {
    const to = new Date(orderFilterDateTo.value + 'T23:59:59.999')
    list = list.filter(o => new Date(o.createdAt) <= to)
  }
  list.sort((a, b) => {
    const diff = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    return orderSortDir.value === 'asc' ? diff : -diff
  })
  return list
})

const groupedOrders = computed(() => {
  const groups = new Map<string, AdminOrder[]>()
  for (const order of filteredOrders.value) {
    const day = order.createdAt.slice(0, 10)
    if (!groups.has(day)) groups.set(day, [])
    groups.get(day)!.push(order)
  }
  return [...groups.entries()].map(([day, items]) => ({ day, items }))
})

function formatDayHeader(day: string) {
  return new Date(day + 'T12:00:00').toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })
}

const STATUS_LABELS: Record<string, string> = {
  pending:          'Ожидает',
  processing:       'В обработке',
  shipped:          'В доставке',
  delivered:        'Доставлен',
  ready_for_pickup: 'Готов к выдаче',
  picked_up:        'Выдан',
  cancelled:        'Отменён',
}

const DELIVERY_STATUS_KEYS = ['pending', 'processing', 'shipped', 'delivered', 'cancelled']
const PICKUP_STATUS_KEYS   = ['pending', 'processing', 'ready_for_pickup', 'picked_up', 'cancelled']

function orderStatusOptions(order: AdminOrder): [string, string][] {
  const keys = order.deliveryMethod === 'pickup' ? PICKUP_STATUS_KEYS : DELIVERY_STATUS_KEYS
  const all  = keys.includes(order.status) ? keys : [order.status, ...keys]
  return all.map(k => [k, STATUS_LABELS[k] ?? k])
}

async function loadOrders() {
  ordersLoading.value = true
  try {
    const res = await api.get('/admin/orders')
    orders.value = res.data
  } catch (e) { notify(apiError(e)) }
  finally { ordersLoading.value = false }
}

async function changeOrderStatus(order: AdminOrder, status: string) {
  try {
    const res = await api.patch(`/admin/orders/${order.id}/status`, { status })
    const idx = orders.value.findIndex(o => o.id === order.id)
    if (idx !== -1) orders.value[idx] = res.data
    notify('Статус обновлён', 'success')
  } catch (e) { notify(apiError(e)) }
}

function toggleExpand(id: number) {
  if (expandedOrders.value.has(id)) expandedOrders.value.delete(id)
  else expandedOrders.value.add(id)
}

function formatOrderDate(iso: string) {
  return new Date(iso).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatOrderPrice(n: number) {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 2 }).format(n)
}

function switchTab(tab: 'catalog' | 'orders') {
  activeTab.value = tab
  if (tab === 'orders' && orders.value.length === 0) loadOrders()
}

// --- catalog editing ---
const selectedCatalog    = ref<Catalog | null>(null)
const catalogPage        = ref(0)
const CAT_PER_PAGE       = 3

const selectedSubCatalog = ref<SubCatalog | null>(null)
const subCatalogPage     = ref(0)

// --- catalog modal ---
const showCatalogModal       = ref(false)
const catalogModalMode       = ref<'add' | 'edit'>('add')
const catalogModalName       = ref('')
const catalogModalImgFile    = ref<File | null>(null)
const catalogModalImgPreview = ref<string | null>(null)

// --- subcatalog modal ---
const showSubModal       = ref(false)
const subModalMode       = ref<'add' | 'edit'>('add')
const subModalName       = ref('')
const subModalImgFile    = ref<File | null>(null)
const subModalImgPreview = ref<string | null>(null)

const productPage        = ref(0)
const PROD_PER_PAGE      = 3
const slideDirection     = ref<'left' | 'right'>('left')

// ── loading / submitting states ──
const savingProduct     = ref(false)
const savingCatalog     = ref(false)
const savingSub         = ref(false)
const deletingProductId = ref<number | null>(null)
const removingImageUrl  = ref<string | null>(null)

function prevProd() { slideDirection.value = 'right'; productPage.value-- }
function nextProd() { slideDirection.value = 'left';  productPage.value++ }

// --- add-product form ---
const productForm = ref({ id: null as number | null, name: '', description: '', price: 0, quantity: 1, catalogId: null as number | null, subCatalogId: null as number | null, imageUrls: [] as string[] })
const chars       = ref<Char[]>([])

// --- image upload ---
const selectedFiles  = ref<File[]>([])
const newPreviewUrls = ref<string[]>([])

function onFilesSelected(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files) return
  const added = Array.from(input.files)
  selectedFiles.value.push(...added)
  added.forEach(f => newPreviewUrls.value.push(URL.createObjectURL(f)))
  input.value = ''
}

function removeNewImage(index: number) {
  URL.revokeObjectURL(newPreviewUrls.value[index])
  newPreviewUrls.value.splice(index, 1)
  selectedFiles.value.splice(index, 1)
}

async function removeExistingImage(imageUrl: string) {
  if (!productForm.value.id) return
  removingImageUrl.value = imageUrl
  try {
    const res = await api.delete(`/admin/products/${productForm.value.id}/images`, { params: { imageUrl } })
    productForm.value.imageUrls = res.data.imageUrls
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { removingImageUrl.value = null }
}

// --- notifications ---
let _notifId = 0
const notifications = ref<Notification[]>([])

function notify(msg: string, type: Notification['type'] = 'error') {
  const id = ++_notifId
  notifications.value.push({ id, msg, type })
  setTimeout(() => dismiss(id), 4000)
}
function dismiss(id: number) {
  notifications.value = notifications.value.filter(n => n.id !== id)
}
function apiError(e: any): string {
  const status = e?.response?.status
  if (status === 401 || status === 403) return 'Нет доступа — войдите в аккаунт заново'
  if (status === 404) return 'Элемент не найден'
  if (status === 409) return e?.response?.data?.message ?? 'Такой элемент уже существует'
  return e?.response?.data?.message ?? e?.response?.data ?? 'Ошибка сервера'
}

// ---- computed slices ----
const visibleCatalogs = computed(() => {
  const s = catalogPage.value * CAT_PER_PAGE
  return catalogs.value.slice(s, s + CAT_PER_PAGE)
})
const canPrevCat = computed(() => catalogPage.value > 0)
const canNextCat = computed(() => (catalogPage.value + 1) * CAT_PER_PAGE < catalogs.value.length)

const filteredSubs = computed(() =>
  selectedCatalog.value ? subCatalogs.value.filter(s => s.catalogId === selectedCatalog.value!.id) : []
)
const visibleSubs = computed(() => {
  const s = subCatalogPage.value * CAT_PER_PAGE
  return filteredSubs.value.slice(s, s + CAT_PER_PAGE)
})
const canPrevSub = computed(() => subCatalogPage.value > 0)
const canNextSub = computed(() => (subCatalogPage.value + 1) * CAT_PER_PAGE < filteredSubs.value.length)

const filteredProducts = computed(() =>
  selectedSubCatalog.value
    ? products.value.filter(p => p.subCatalogId === selectedSubCatalog.value!.id)
    : selectedCatalog.value
      ? products.value.filter(p => p.catalogId === selectedCatalog.value!.id)
      : products.value
)
const visibleProducts = computed(() => {
  const s = productPage.value * PROD_PER_PAGE
  return filteredProducts.value.slice(s, s + PROD_PER_PAGE)
})
const canPrevProd = computed(() => productPage.value > 0)
const canNextProd = computed(() => (productPage.value + 1) * PROD_PER_PAGE < filteredProducts.value.length)

const formSubs = computed(() =>
  productForm.value.catalogId ? subCatalogs.value.filter(s => s.catalogId === productForm.value.catalogId) : []
)

async function loadAll() {
  try {
    const [c, s, p] = await Promise.all([
      api.get('/admin/catalogs'),
      api.get('/admin/subcatalogs'),
      api.get('/admin/products'),
    ])
    catalogs.value    = c.data
    subCatalogs.value = s.data
    products.value    = p.data
  } catch (e) { notify(apiError(e)) }
}
onMounted(loadAll)

// ---- catalog CRUD ----
function pickCatalog(c: Catalog) {
  if (selectedCatalog.value?.id === c.id) {
    selectedCatalog.value    = null
    selectedSubCatalog.value = null
    subCatalogPage.value     = 0
    productPage.value        = 0
    return
  }
  selectedCatalog.value    = c
  selectedSubCatalog.value = null
  subCatalogPage.value     = 0
  productPage.value        = 0
}

function openAddCatalog() {
  catalogModalMode.value       = 'add'
  catalogModalName.value       = ''
  catalogModalImgFile.value    = null
  catalogModalImgPreview.value = null
  showCatalogModal.value       = true
}

function openEditCatalog(c: Catalog) {
  catalogModalMode.value       = 'edit'
  catalogModalName.value       = c.name
  catalogModalImgFile.value    = null
  catalogModalImgPreview.value = c.imageUrl ?? null
  showCatalogModal.value       = true
}

function onCatalogImgSelected(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.[0]) return
  catalogModalImgFile.value    = input.files[0]
  catalogModalImgPreview.value = URL.createObjectURL(input.files[0])
  input.value = ''
}

function removeCatalogImg() {
  catalogModalImgFile.value    = null
  catalogModalImgPreview.value = null
}

async function saveCatalogModal() {
  if (!catalogModalName.value.trim()) { notify('Введите название каталога'); return }
  savingCatalog.value = true
  try {
    const slug = catalogModalName.value.trim().toLowerCase().replace(/\s+/g, '-')
    let id: number
    if (catalogModalMode.value === 'add') {
      const res = await api.post('/admin/catalogs', { name: catalogModalName.value.trim(), slug })
      id = res.data.id
    } else {
      await api.put(`/admin/catalogs/${selectedCatalog.value!.id}`, { name: catalogModalName.value.trim(), slug })
      id = selectedCatalog.value!.id
    }
    if (catalogModalImgFile.value) {
      const form = new FormData()
      form.append('file', catalogModalImgFile.value)
      await api.post(`/admin/catalogs/${id}/image`, form)
    }
    showCatalogModal.value = false
    selectedCatalog.value  = null
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { savingCatalog.value = false }
}

async function deleteCatalogInModal() {
  if (!confirm('Удалить каталог?')) return
  savingCatalog.value = true
  try {
    await api.delete(`/admin/catalogs/${selectedCatalog.value!.id}`)
    showCatalogModal.value = false
    selectedCatalog.value  = null
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { savingCatalog.value = false }
}

// ---- subcatalog CRUD ----
function pickSub(s: SubCatalog) {
  if (selectedSubCatalog.value?.id === s.id) {
    selectedSubCatalog.value = null
    productPage.value        = 0
    return
  }
  selectedSubCatalog.value = s
  productPage.value        = 0
}

function openAddSub() {
  if (!selectedCatalog.value) { notify('Сначала выберите каталог'); return }
  subModalMode.value       = 'add'
  subModalName.value       = ''
  subModalImgFile.value    = null
  subModalImgPreview.value = null
  showSubModal.value       = true
}

function openEditSub(s: SubCatalog) {
  subModalMode.value       = 'edit'
  subModalName.value       = s.name
  subModalImgFile.value    = null
  subModalImgPreview.value = s.imageUrl ?? null
  showSubModal.value       = true
}

function onSubImgSelected(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.[0]) return
  subModalImgFile.value    = input.files[0]
  subModalImgPreview.value = URL.createObjectURL(input.files[0])
  input.value = ''
}

function removeSubImg() {
  subModalImgFile.value    = null
  subModalImgPreview.value = null
}

async function saveSubModal() {
  if (!subModalName.value.trim()) { notify('Введите название подкаталога'); return }
  savingSub.value = true
  try {
    const slug = subModalName.value.trim().toLowerCase().replace(/\s+/g, '-')
    let id: number
    if (subModalMode.value === 'add') {
      const res = await api.post('/admin/subcatalogs', { name: subModalName.value.trim(), slug, catalogId: selectedCatalog.value!.id })
      id = res.data.id
    } else {
      await api.put(`/admin/subcatalogs/${selectedSubCatalog.value!.id}`, {
        name: subModalName.value.trim(), slug, catalogId: selectedSubCatalog.value!.catalogId
      })
      id = selectedSubCatalog.value!.id
    }
    if (subModalImgFile.value) {
      const form = new FormData()
      form.append('file', subModalImgFile.value)
      await api.post(`/admin/subcatalogs/${id}/image`, form)
    }
    showSubModal.value = false
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { savingSub.value = false }
}

async function deleteSubInModal() {
  if (!confirm('Удалить подкаталог?')) return
  savingSub.value = true
  try {
    await api.delete(`/admin/subcatalogs/${selectedSubCatalog.value!.id}`)
    showSubModal.value       = false
    selectedSubCatalog.value = null
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { savingSub.value = false }
}

// ---- product CRUD ----
const showProductModal = ref(false)

function openAddProduct() {
  resetForm()
  showProductModal.value = true
}

function editProduct(p: Product) {
  productForm.value    = { ...p, imageUrls: [...(p.imageUrls ?? [])] }
  chars.value          = Object.entries(p.characteristics ?? {}).map(([name, type]) => ({ name, type }))
  selectedFiles.value  = []
  newPreviewUrls.value.forEach(u => URL.revokeObjectURL(u))
  newPreviewUrls.value = []
  showProductModal.value = true
}
async function removeProduct(id: number) {
  if (!confirm('Удалить товар?')) return
  deletingProductId.value = id
  try {
    await api.delete(`/admin/products/${id}`)
    if (showProductModal.value) showProductModal.value = false
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { deletingProductId.value = null }
}
async function saveProduct() {
  if (!productForm.value.name.trim()) { notify('Введите название товара'); return }
  savingProduct.value = true
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
    await loadAll()
    resetForm()
    showProductModal.value = false
  } catch (e) { notify(apiError(e)) }
  finally { savingProduct.value = false }
}
function resetForm() {
  productForm.value = { id: null, name: '', description: '', price: 0, quantity: 1, catalogId: null, subCatalogId: null, imageUrls: [] }
  chars.value = []
  selectedFiles.value = []
  newPreviewUrls.value.forEach(u => URL.revokeObjectURL(u))
  newPreviewUrls.value = []
}

// ---- characteristics ----
function addChar() { chars.value.push({ name: '', type: '' }) }
function removeChar(i: number) { chars.value.splice(i, 1) }
</script>

<template>
  <main class="admin-page">
    <div class="container">
      <div class="admin-layout">

        <!-- Sidebar -->
        <aside class="admin-sidebar">
          <button class="sidebar-tab" :class="{ 'sidebar-tab--active': activeTab === 'catalog' }" @click="switchTab('catalog')">
            Каталог / Товары
          </button>
          <button class="sidebar-tab" @click="openAddProduct">
            + Добавить товар
          </button>
          <button class="sidebar-tab" :class="{ 'sidebar-tab--active': activeTab === 'orders' }" @click="switchTab('orders')">
            Заказы
          </button>
        </aside>

        <!-- Content -->
        <div class="admin-content">

          <!-- ===== Orders ===== -->
          <div v-if="activeTab === 'orders'" class="section-box">
            <div class="section-header">
              <h3 class="section-title">Заказы</h3>
              <button class="btn btn--green btn--sm" @click="loadOrders">&#8635; Обновить</button>
            </div>

            <!-- Filter bar -->
            <div v-if="!ordersLoading && orders.length > 0" class="orders-filter-bar">
              <input
                v-model="orderFilterUser"
                class="orders-filter-input"
                placeholder="Поиск по заказчику..."
                type="text"
              />
              <div class="orders-filter-dates">
                <input v-model="orderFilterDateFrom" class="orders-filter-date" type="date" title="С даты" />
                <span class="orders-filter-sep">—</span>
                <input v-model="orderFilterDateTo" class="orders-filter-date" type="date" title="По дату" />
              </div>
              <button
                class="orders-sort-btn"
                :title="orderSortDir === 'desc' ? 'Сначала новые' : 'Сначала старые'"
                @click="orderSortDir = orderSortDir === 'desc' ? 'asc' : 'desc'"
              >{{ orderSortDir === 'desc' ? '↓ Новые' : '↑ Старые' }}</button>
              <button
                v-if="orderFilterUser || orderFilterDateFrom || orderFilterDateTo"
                class="btn btn--sm orders-reset-btn"
                @click="orderFilterUser = ''; orderFilterDateFrom = ''; orderFilterDateTo = ''"
              >✕ Сбросить</button>
            </div>

            <div v-if="ordersLoading" class="orders-loading">Загрузка...</div>
            <div v-else-if="orders.length === 0" class="empty-hint" style="padding:20px 0">Заказов нет</div>
            <div v-else-if="groupedOrders.length === 0" class="empty-hint" style="padding:20px 0">Нет заказов по выбранным фильтрам</div>

            <div v-else class="orders-list">
              <template v-for="group in groupedOrders" :key="group.day">
                <div class="orders-day-header">{{ formatDayHeader(group.day) }}</div>
              <div v-for="order in group.items" :key="order.id" class="order-row">
                <!-- Order header -->
                <div class="order-row__head" @click="toggleExpand(order.id)">
                  <span class="order-row__id">#{{ order.id }}</span>
                  <span class="order-row__date">{{ formatOrderDate(order.createdAt) }}</span>
                  <span class="order-row__user">
                    {{ order.userFirstName || order.userLastName ? `${order.userFirstName ?? ''} ${order.userLastName ?? ''}`.trim() : order.userEmail }}
                    <span class="order-row__email">({{ order.userEmail }})</span>
                  </span>
                  <span class="order-row__total">{{ formatOrderPrice(order.total) }}</span>
                  <div class="order-row__status-wrap" @click.stop>
                    <select
                      class="order-status-select"
                      :class="`order-status-select--${order.status}`"
                      :value="order.status"
                      @change="changeOrderStatus(order, ($event.target as HTMLSelectElement).value)"
                    >
                      <option v-for="[val, label] in orderStatusOptions(order)" :key="val" :value="val">{{ label }}</option>
                    </select>
                  </div>
                  <span class="order-row__toggle">{{ expandedOrders.has(order.id) ? '▲' : '▼' }}</span>
                </div>

                <!-- Order items (expanded) -->
                <div v-if="expandedOrders.has(order.id)" class="order-row__items">
                  <div v-for="item in order.items" :key="item.id" class="order-item">
                    <img v-if="item.imageUrl" :src="item.imageUrl" class="order-item__img" alt="" />
                    <div v-else class="order-item__img order-item__img--placeholder"></div>
                    <span class="order-item__name">{{ item.name }}</span>
                    <span class="order-item__qty">{{ item.quantity }} шт.</span>
                    <span class="order-item__price">{{ formatOrderPrice(item.price) }}</span>
                    <span class="order-item__subtotal">{{ formatOrderPrice(item.price * item.quantity) }}</span>
                  </div>
                  <div class="order-row__footer">
                    <span
                      v-if="order.deliveryAddress"
                      class="order-footer__address"
                    >
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none" style="flex-shrink:0;margin-top:1px">
                        <path d="M6 1C4.07 1 2.5 2.57 2.5 4.5c0 2.625 3.5 6.5 3.5 6.5s3.5-3.875 3.5-6.5C9.5 2.57 7.93 1 6 1zm0 4.75a1.25 1.25 0 1 1 0-2.5 1.25 1.25 0 0 1 0 2.5z" fill="#f4b942"/>
                      </svg>
                      {{ order.deliveryAddress }}
                    </span>
                    <span v-if="order.userPhone" class="order-footer__phone">Тел: {{ order.userPhone }}</span>
                    <span class="order-footer__delivery">
                      {{ order.deliveryMethod === 'pickup' ? 'Самовывоз' : 'Доставка' }}: {{ formatOrderPrice(order.deliveryCost) }}
                    </span>
                    <span class="order-footer__total">Итого: {{ formatOrderPrice(order.total) }}</span>
                  </div>
                </div>
              </div>
              </template>
            </div>
          </div>

          <!-- ===== Catalog & Products ===== -->
          <div v-if="activeTab === 'catalog'" class="section-box">

              <!-- Catalog editing -->
              <div class="section-header">
                <h3 class="section-title">Каталог</h3>
                <div class="section-actions">
                  <button class="btn btn--green"  @click="openAddCatalog">+ Добавить</button>
                  <button v-if="selectedCatalog" class="btn btn--orange" @click="openEditCatalog(selectedCatalog)">Изменить</button>
                </div>
              </div>

              <div class="carousel-row">
                <button class="arrow" :disabled="!canPrevCat" @click="catalogPage--">&#8592;</button>
                <div class="carousel-items">
                  <button
                    v-for="c in visibleCatalogs" :key="c.id"
                    class="carousel-tag"
                    :class="{ 'carousel-tag--active': selectedCatalog?.id === c.id }"
                    @click="pickCatalog(c)"
                  >{{ c.name }}</button>
                  <span v-if="visibleCatalogs.length === 0" class="empty-hint">Нет каталогов</span>
                </div>
                <button class="arrow" :disabled="!canNextCat" @click="catalogPage++">&#8594;</button>
              </div>

              <!-- Subcatalog editing -->
              <div class="section-header" style="margin-top:24px">
                <h3 class="section-title">Подкаталог</h3>
                <div class="section-actions" :class="{ 'section-actions--disabled': !selectedCatalog }">
                  <button class="btn btn--green" @click="openAddSub" :disabled="!selectedCatalog">+ Добавить</button>
                  <button v-if="selectedSubCatalog" class="btn btn--orange" @click="openEditSub(selectedSubCatalog)">Изменить</button>
                </div>
              </div>

              <div class="carousel-row">
                <button class="arrow" :disabled="!canPrevSub" @click="subCatalogPage--">&#8592;</button>
                <div class="carousel-items">
                  <button
                    v-for="s in visibleSubs" :key="s.id"
                    class="carousel-tag"
                    :class="{ 'carousel-tag--active': selectedSubCatalog?.id === s.id }"
                    @click="pickSub(s)"
                  >{{ s.name }}</button>
                  <span v-if="visibleSubs.length === 0" class="empty-hint">
                    {{ selectedCatalog ? 'Нет подкаталогов' : 'Сначала выберите каталог' }}
                  </span>
                </div>
                <button class="arrow" :disabled="!canNextSub" @click="subCatalogPage++">&#8594;</button>
              </div>

              <!-- Product filter indicator -->
              <div class="product-filter-bar">
                <span v-if="selectedSubCatalog" class="filter-badge">
                  Подкаталог: {{ selectedSubCatalog.name }}
                  <button class="filter-badge__clear" @click="selectedSubCatalog = null; productPage = 0" title="Сбросить">✕</button>
                </span>
                <span v-else-if="selectedCatalog" class="filter-badge">
                  Каталог: {{ selectedCatalog.name }}
                  <button class="filter-badge__clear" @click="selectedCatalog = null; selectedSubCatalog = null; productPage = 0" title="Сбросить">✕</button>
                </span>
                <span v-else class="filter-badge filter-badge--all">Все товары ({{ products.length }})</span>
              </div>

              <!-- Product cards -->
              <div class="carousel-row" style="margin-top:8px; align-items:flex-start">
                <button class="arrow" style="margin-top:60px" :disabled="!canPrevProd" @click="prevProd">&#8592;</button>
                <div class="product-cards-viewport">
                  <transition :name="'slide-' + slideDirection" mode="out-in">
                    <div class="product-cards" :key="productPage">
                      <template v-if="visibleProducts.length > 0">
                        <div v-for="p in visibleProducts" :key="p.id" class="product-card">
                          <div class="product-card__img">
                            <img v-if="p.imageUrls?.length" :src="p.imageUrls[0]" alt="" />
                          </div>
                          <div class="product-card__body">
                            <p class="product-card__name">{{ p.name }}</p>
                            <p class="product-card__price">{{ p.price.toFixed(2) }} ₽</p>
                            <div class="product-card__actions">
                              <button class="btn btn--orange btn--sm" :disabled="deletingProductId === p.id" @click="editProduct(p)">Изменить</button>
                              <button class="btn btn--red btn--sm" :disabled="deletingProductId === p.id" @click="removeProduct(p.id)">
                                <span v-if="deletingProductId === p.id" class="btn-spinner btn-spinner--sm"></span>
                                {{ deletingProductId === p.id ? '...' : 'Удалить' }}
                              </button>
                            </div>
                          </div>
                        </div>
                      </template>
                      <span v-else class="empty-hint" style="padding:20px 0">Нет товаров</span>
                    </div>
                  </transition>
                </div>
                <button class="arrow" style="margin-top:60px" :disabled="!canNextProd" @click="nextProd">&#8594;</button>
              </div>

          </div>

        </div>
      </div>
    </div>

    <!-- Product modal -->
    <div v-if="showProductModal" class="modal-backdrop" @click.self="!savingProduct && (resetForm(), showProductModal = false)">
      <div class="modal modal--wide">
        <h3 class="modal-title">{{ productForm.id ? 'Изменение товара' : 'Добавление товара' }}</h3>

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
        <div class="image-upload-section" style="margin-top:16px">
          <p style="font-size:13px;color:#333;margin-bottom:8px">Фотографии товара:</p>
          <div class="image-grid" v-if="productForm.imageUrls.length || newPreviewUrls.length">
            <div v-for="url in productForm.imageUrls" :key="url" class="image-thumb">
              <img :src="url" alt="" />
              <button class="image-thumb__remove" :disabled="removingImageUrl === url" @click="removeExistingImage(url)" title="Удалить">
                {{ removingImageUrl === url ? '…' : '✕' }}
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

        <div class="form-actions" style="max-width:none">
          <button class="btn btn--green" :disabled="savingProduct" @click="saveProduct">
            <span v-if="savingProduct" class="btn-spinner"></span>
            {{ savingProduct ? 'Сохранение...' : (productForm.id ? 'Сохранить' : 'Добавить') }}
          </button>
          <button v-if="productForm.id" class="btn btn--red" :disabled="savingProduct || deletingProductId === productForm.id" @click="removeProduct(productForm.id!)">
            <span v-if="deletingProductId === productForm.id" class="btn-spinner"></span>
            {{ deletingProductId === productForm.id ? 'Удаление...' : 'Удалить' }}
          </button>
          <button class="btn btn--orange" :disabled="savingProduct" @click="resetForm(); showProductModal = false">Отмена</button>
        </div>
      </div>
    </div>

    <!-- Notifications -->
    <teleport to="body">
      <div class="notif-container">
        <transition-group name="notif">
          <div
            v-for="n in notifications"
            :key="n.id"
            class="notif"
            :class="`notif--${n.type}`"
            @click="dismiss(n.id)"
          >{{ n.msg }}</div>
        </transition-group>
      </div>
    </teleport>

    <!-- Catalog modal -->
    <div v-if="showCatalogModal" class="modal-backdrop" @click.self="!savingCatalog && (showCatalogModal = false)">
      <div class="modal">
        <h3 class="modal-title">{{ catalogModalMode === 'add' ? 'Добавить каталог' : 'Изменить каталог' }}</h3>
        <label class="form-row">
          <span class="form-label">Название:</span>
          <input v-model="catalogModalName" class="form-input" :disabled="savingCatalog" @keyup.enter="saveCatalogModal" />
        </label>
        <div class="modal-image-section">
          <p class="modal-image-label">Фото категории:</p>
          <div v-if="catalogModalImgPreview" class="modal-image-preview">
            <img :src="catalogModalImgPreview" alt="" />
            <button class="modal-image-remove" :disabled="savingCatalog" @click="removeCatalogImg">&#10005;</button>
          </div>
          <label v-else class="upload-btn" :class="{ 'upload-btn--disabled': savingCatalog }">
            <input type="file" accept="image/*" hidden :disabled="savingCatalog" @change="onCatalogImgSelected" />
            + Добавить фото
          </label>
        </div>
        <div class="form-actions" style="margin-top:20px">
          <button class="btn btn--green" :disabled="savingCatalog" @click="saveCatalogModal">
            <span v-if="savingCatalog" class="btn-spinner"></span>
            {{ savingCatalog ? 'Сохранение...' : (catalogModalMode === 'add' ? 'Добавить' : 'Сохранить') }}
          </button>
          <button v-if="catalogModalMode === 'edit'" class="btn btn--red" :disabled="savingCatalog" @click="deleteCatalogInModal">Удалить</button>
          <button class="btn btn--orange" :disabled="savingCatalog" @click="showCatalogModal = false">Отмена</button>
        </div>
      </div>
    </div>

    <!-- SubCatalog modal -->
    <div v-if="showSubModal" class="modal-backdrop" @click.self="!savingSub && (showSubModal = false)">
      <div class="modal">
        <h3 class="modal-title">{{ subModalMode === 'add' ? 'Добавить подкаталог' : 'Изменить подкаталог' }}</h3>
        <label class="form-row">
          <span class="form-label">Название:</span>
          <input v-model="subModalName" class="form-input" :disabled="savingSub" @keyup.enter="saveSubModal" />
        </label>
        <div class="modal-image-section">
          <p class="modal-image-label">Фото подкатегории:</p>
          <div v-if="subModalImgPreview" class="modal-image-preview">
            <img :src="subModalImgPreview" alt="" />
            <button class="modal-image-remove" :disabled="savingSub" @click="removeSubImg">&#10005;</button>
          </div>
          <label v-else class="upload-btn" :class="{ 'upload-btn--disabled': savingSub }">
            <input type="file" accept="image/*" hidden :disabled="savingSub" @change="onSubImgSelected" />
            + Добавить фото
          </label>
        </div>
        <div class="form-actions" style="margin-top:20px">
          <button class="btn btn--green" :disabled="savingSub" @click="saveSubModal">
            <span v-if="savingSub" class="btn-spinner"></span>
            {{ savingSub ? 'Сохранение...' : (subModalMode === 'add' ? 'Добавить' : 'Сохранить') }}
          </button>
          <button v-if="subModalMode === 'edit'" class="btn btn--red" :disabled="savingSub" @click="deleteSubInModal">Удалить</button>
          <button class="btn btn--orange" :disabled="savingSub" @click="showSubModal = false">Отмена</button>
        </div>
      </div>
    </div>

  </main>
</template>

<style lang="scss" scoped>
.admin-page {
  background: #ececec;
  min-height: calc(100vh - 110px);
  padding: 20px 0 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.admin-layout {
  display: flex;
  background: white;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  overflow: hidden;
  min-height: 500px;
}

.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid #d0d0d0;
}

.sidebar-tab {
  display: block;
  width: 100%;
  padding: 12px 16px;
  background: white;
  border: none;
  border-bottom: 1px solid #e8e8e8;
  text-align: left;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  line-height: 1.4;
  transition: background 0.15s;

  &:hover { background: #f5f5f5; }
  &--active { background: #ececec; font-weight: 600; }
}

.admin-content {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
}

.section-box {
  background: white;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  padding: 20px 24px;
}

.section-title {
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 14px;
}

// --- section header with actions ---
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  .section-title { margin-bottom: 0; }
}

.section-actions {
  display: flex;
  gap: 8px;

  &--disabled {
    opacity: 0.45;
    pointer-events: none;
  }
}

// --- buttons ---
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 30px;
  padding: 0 16px;
  border: none;
  border-radius: 3px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: filter 0.15s;

  &:hover:not(:disabled) { filter: brightness(0.9); }
  &:disabled { opacity: 0.45; cursor: not-allowed; }

  &--green  { background: #2ecc40; color: white; }
  &--orange { background: #f4b942; color: white; }
  &--red    { background: #e74c3c; color: white; }

  &--sm { height: 24px; padding: 0 10px; font-size: 12px; }
}

.btn-spinner {
  display: inline-block;
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: btn-spin 0.55s linear infinite;
  flex-shrink: 0;

  &--sm {
    width: 10px;
    height: 10px;
    border-width: 1.5px;
  }
}

@keyframes btn-spin {
  to { transform: rotate(360deg); }
}

.upload-btn--disabled {
  opacity: 0.45;
  cursor: not-allowed;
  pointer-events: none;
}

// --- carousel ---
.carousel-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.carousel-items {
  display: flex;
  gap: 8px;
  flex: 1;
  min-height: 36px;
  align-items: center;
}

.carousel-tag {
  flex: 1;
  height: 34px;
  border: 1px solid #ccc;
  border-radius: 3px;
  background: white;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;

  &:hover { background: #f5f5f5; }
  &--active { background: #fff3cd; border-color: #f4b942; font-weight: 600; }
}

.arrow {
  width: 28px;
  height: 28px;
  border: 1px solid #ccc;
  border-radius: 3px;
  background: white;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  flex-shrink: 0;
  transition: background 0.15s;

  &:hover:not(:disabled) { background: #f5f5f5; }
  &:disabled { opacity: 0.35; cursor: default; }
}

.empty-hint {
  font-size: 12px;
  color: #aaa;
}

// --- image upload ---
.image-upload-section {
  max-width: 480px;
  margin-top: 16px;
  margin-bottom: 4px;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.image-thumb {
  position: relative;
  width: 80px;
  height: 80px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &--new { border-color: #f4b942; }

  &__remove {
    position: absolute;
    top: 2px;
    right: 2px;
    width: 18px;
    height: 18px;
    border: none;
    border-radius: 50%;
    background: rgba(0,0,0,0.55);
    color: white;
    font-size: 10px;
    line-height: 1;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;

    &:hover { background: #e74c3c; }
  }
}

.upload-btn {
  display: inline-block;
  height: 30px;
  padding: 0 14px;
  background: #f5f5f5;
  border: 1px dashed #bbb;
  border-radius: 3px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  line-height: 30px;
  transition: border-color 0.15s, background 0.15s;

  &:hover { border-color: #f4b942; background: #fffbf0; }
}

// --- product filter bar ---
.product-filter-bar {
  margin-top: 12px;
  margin-bottom: 4px;
}

.filter-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #fff3cd;
  border: 1px solid #f4b942;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: #7a5a00;

  &--all {
    background: #e8f5e9;
    border-color: #2ecc40;
    color: #1a6e29;
  }

  &__clear {
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    font-size: 11px;
    color: inherit;
    opacity: 0.7;
    line-height: 1;
    transition: opacity 0.15s;
    &:hover { opacity: 1; }
  }
}

// --- product cards ---
.product-cards-viewport {
  flex: 1;
  overflow: hidden;
}

.product-cards {
  display: flex;
  gap: 12px;
  min-height: 160px;
  align-items: flex-start;
}

// slide animations
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}
.slide-left-enter-from  { opacity: 0; transform: translateX(32px); }
.slide-left-leave-to    { opacity: 0; transform: translateX(-32px); }
.slide-right-enter-from { opacity: 0; transform: translateX(-32px); }
.slide-right-leave-to   { opacity: 0; transform: translateX(32px); }

.product-card {
  flex: 0 0 calc((100% - 24px) / 3);
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
  background: white;

  &__img {
    width: 100%;
    aspect-ratio: 4/3;
    background: #d0d0d0;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__body {
    padding: 8px;
  }

  &__name {
    font-size: 13px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  &__price {
    font-size: 13px;
    color: #333;
    margin-bottom: 8px;
  }

  &__actions {
    display: flex;
    gap: 6px;
  }
}

// --- product form ---
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
}

.form-label {
  width: 100px;
  flex-shrink: 0;
  font-size: 13px;
  color: #333;
  text-align: right;
}

.form-input {
  flex: 1;
  height: 30px;
  padding: 0 8px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 13px;
  font-family: inherit;

  &:focus { outline: none; border-color: #f4b942; }
  &:disabled { background: #f5f5f5; }
}

select.form-input { height: 32px; }

.form-textarea {
  height: auto;
  padding: 6px 8px;
  resize: vertical;
  line-height: 1.5;
}

.form-row--top {
  align-items: flex-start;

  .form-label { padding-top: 6px; }
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end;
  max-width: 480px;
}

.char-section {
  margin-bottom: 14px;

  &__header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  &__label {
    font-size: 13px;
    color: #333;
    font-weight: 500;
  }
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

.char-input {
  flex: 1;
}

.char-sep {
  font-size: 14px;
  color: #888;
  flex-shrink: 0;
}

.char-delete {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: #fce;
  color: #e74c3c;
  font-size: 11px;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: background 0.15s;

  &:hover { background: #e74c3c; color: white; }
}

.char-empty {
  font-size: 12px;
  color: #aaa;
  margin: 0;
}

// --- modal image section ---
.modal-image-section {
  margin-top: 16px;
}

.modal-image-label {
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
}

.modal-image-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.modal-image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: white;
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: background 0.15s;

  &:hover { background: #e74c3c; }
}

// --- modal ---
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: white;
  border-radius: 8px;
  padding: 28px 32px;
  width: 380px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);

  &--wide { width: 560px; }
}

.modal-title {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
}

// --- notifications ---
.notif-container {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 9999;
  pointer-events: none;
  min-width: 280px;
  max-width: 480px;
}

.notif {
  padding: 10px 18px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  pointer-events: all;
  box-shadow: 0 2px 12px rgba(0,0,0,0.18);

  &--error   { background: #e74c3c; }
  &--success { background: #2ecc40; }
  &--info    { background: #3498db; }
}

.notif-enter-active,
.notif-leave-active { transition: all 0.25s ease; }
.notif-enter-from   { opacity: 0; transform: translateY(-16px); }
.notif-leave-to     { opacity: 0; transform: translateY(-8px); }

// ── orders ──────────────────────────────────────────────────────────────────
.orders-filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 0 8px;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}

.orders-filter-input {
  height: 28px;
  padding: 0 8px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 13px;
  font-family: inherit;
  flex: 1;
  min-width: 150px;
  max-width: 220px;
  &:focus { outline: none; border-color: #f4b942; }
}

.orders-filter-dates {
  display: flex;
  align-items: center;
  gap: 4px;
}

.orders-filter-date {
  height: 28px;
  padding: 0 6px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 12px;
  font-family: inherit;
  width: 128px;
  &:focus { outline: none; border-color: #f4b942; }
}

.orders-filter-sep {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.orders-sort-btn {
  height: 28px;
  padding: 0 10px;
  border: 1px solid #ccc;
  border-radius: 3px;
  background: white;
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  flex-shrink: 0;
  transition: border-color 0.15s, background 0.15s;
  &:hover { border-color: #f4b942; background: #fffbf0; }
}

.orders-reset-btn {
  background: #eee;
  color: #555;
  &:hover:not(:disabled) { filter: brightness(0.92); }
}

.orders-day-header {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 10px 4px 4px;
  border-bottom: 1px solid #eee;
  margin-bottom: 4px;

  &:first-child { padding-top: 4px; }
}

.orders-loading {
  text-align: center;
  padding: 24px;
  color: #888;
  font-size: 13px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-row {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;

  &__head {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    background: #fafafa;
    cursor: pointer;
    flex-wrap: wrap;
    transition: background 0.15s;
    &:hover { background: #f0f0f0; }
  }

  &__id {
    font-size: 13px;
    font-weight: 700;
    color: #2c3e50;
    min-width: 36px;
    flex-shrink: 0;
  }

  &__date {
    font-size: 12px;
    color: #888;
    flex-shrink: 0;
    min-width: 110px;
  }

  &__user {
    flex: 1;
    font-size: 13px;
    color: #333;
    min-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__email {
    font-size: 11px;
    color: #aaa;
  }

  &__total {
    font-size: 13px;
    font-weight: 600;
    color: #2c3e50;
    flex-shrink: 0;
    min-width: 90px;
    text-align: right;
  }

  &__status-wrap {
    flex-shrink: 0;
  }

  &__toggle {
    font-size: 10px;
    color: #aaa;
    flex-shrink: 0;
    width: 14px;
    text-align: center;
  }

  &__items {
    border-top: 1px solid #eee;
    padding: 10px 12px 6px;
    background: white;
  }

  &__footer {
    display: flex;
    gap: 16px;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
    margin-top: 8px;
    flex-wrap: wrap;
  }
}

.order-status-select {
  height: 26px;
  padding: 0 6px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
  &:focus { border-color: #f4b942; }

  &--pending           { background: #fff8e1; color: #795548; }
  &--processing        { background: #e3f2fd; color: #1565c0; }
  &--shipped           { background: #f3e5f5; color: #6a1b9a; }
  &--delivered         { background: #e8f5e9; color: #2e7d32; }
  &--ready_for_pickup  { background: #fff3e0; color: #e65100; }
  &--picked_up         { background: #e8f5e9; color: #2e7d32; }
  &--cancelled         { background: #fce4ec; color: #c62828; }
}

.order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
  &:last-of-type { border-bottom: none; }

  &__img {
    width: 40px;
    height: 40px;
    object-fit: cover;
    border-radius: 3px;
    border: 1px solid #eee;
    flex-shrink: 0;

    &--placeholder {
      background: #eee;
    }
  }

  &__name {
    flex: 1;
    font-size: 13px;
    color: #333;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__qty {
    font-size: 12px;
    color: #666;
    flex-shrink: 0;
    min-width: 44px;
    text-align: right;
  }

  &__price {
    font-size: 12px;
    color: #888;
    flex-shrink: 0;
    min-width: 72px;
    text-align: right;
  }

  &__subtotal {
    font-size: 13px;
    font-weight: 600;
    color: #2c3e50;
    flex-shrink: 0;
    min-width: 80px;
    text-align: right;
  }
}

.order-footer__address {
  flex-basis: 100%;
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 12px;
  color: #2c3e50;
  font-weight: 500;
  text-align: left;
  order: -1;
}

.order-footer__phone,
.order-footer__delivery,
.order-footer__total {
  font-size: 12px;
  color: #555;
}

.order-footer__total {
  font-weight: 700;
  color: #2c3e50;
}
</style>
