<script setup lang="ts">
import api from '@/api/auth'
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminProductModal from '@/components/AdminProductModal.vue'

interface Catalog    { id: number; name: string; slug: string; imageUrl?: string }
interface SubCatalog { id: number; catalogId: number; name: string; slug: string; imageUrl?: string }
interface Product    { id: number; name: string; description: string; price: number; quantity: number; catalogId: number | null; subCatalogId: number | null; imageUrls: string[]; characteristics: Record<string, string> }
interface Notification { id: number; msg: string; type: 'error' | 'success' | 'info' }

interface AdminOrderItem { id: number; productId: number; name: string; price: number; quantity: number; imageUrl: string | null }
interface AdminOrder {
  id: number; total: number; deliveryCost: number; status: string; cancelledBy: string | null; createdAt: string
  deliveryMethod: string; deliveryAddress: string | null
  userEmail: string; userFirstName: string | null; userLastName: string | null; userPhone: string | null
  items: AdminOrderItem[]
}


const router = useRouter()
const activeTab = ref<'catalog' | 'orders'>('catalog')

const catalogs    = ref<Catalog[]>([])
const subCatalogs = ref<SubCatalog[]>([])
const products    = ref<Product[]>([])

// ── orders ──
const orders              = ref<AdminOrder[]>([])
const ordersLoading       = ref(false)
const expandedOrders      = ref<Set<number>>(new Set())
const collapsedDays       = ref<Set<string>>(new Set())
const ordersPage          = ref(0)
const ordersTotalPages    = ref(0)
const ordersTotalElements = ref(-1)
const ORDERS_PAGE_SIZE    = 20

// ── order filters ──
const orderFilterUser     = ref('')
const orderSortDir        = ref<'desc' | 'asc'>('desc')
const orderFilterDateFrom = ref('')
const orderFilterDateTo   = ref('')
const orderFilterStatus   = ref('')

const hasActiveFilters = computed(() =>
  !!(orderFilterUser.value.trim() || orderFilterDateFrom.value || orderFilterDateTo.value || orderFilterStatus.value)
)

const groupedOrders = computed(() => {
  const groups = new Map<string, AdminOrder[]>()
  for (const order of orders.value) {
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

async function loadOrders(resetPage = false) {
  if (resetPage) {
    ordersPage.value = 0
    collapsedDays.value = new Set()
  }
  ordersLoading.value = true
  try {
    const params: Record<string, unknown> = {
      page: ordersPage.value,
      size: ORDERS_PAGE_SIZE,
      sort: orderSortDir.value,
    }
    if (orderFilterUser.value.trim())  params.search   = orderFilterUser.value.trim()
    if (orderFilterDateFrom.value)     params.dateFrom = orderFilterDateFrom.value
    if (orderFilterDateTo.value)       params.dateTo   = orderFilterDateTo.value
    if (orderFilterStatus.value)       params.status   = orderFilterStatus.value

    const res = await api.get('/admin/orders', { params })
    orders.value             = res.data.content
    ordersTotalPages.value   = res.data.totalPages
    ordersTotalElements.value = res.data.totalElements
  } catch (e) { notify(apiError(e)) }
  finally { ordersLoading.value = false }
}

let _searchDebounce: ReturnType<typeof setTimeout> | null = null
watch(orderFilterUser, () => {
  if (_searchDebounce) clearTimeout(_searchDebounce)
  _searchDebounce = setTimeout(() => loadOrders(true), 400)
})
watch([orderFilterDateFrom, orderFilterDateTo, orderSortDir, orderFilterStatus], () => loadOrders(true))

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

function toggleDay(day: string) {
  if (collapsedDays.value.has(day)) collapsedDays.value.delete(day)
  else collapsedDays.value.add(day)
}

function formatOrderDate(iso: string) {
  return new Date(iso).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatOrderPrice(n: number) {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 2 }).format(n)
}

function switchTab(tab: 'catalog' | 'orders') {
  activeTab.value = tab
  if (tab === 'orders' && ordersTotalElements.value === -1) loadOrders()
}

// --- catalog editing ---
const selectedCatalog    = ref<Catalog | null>(null)
const catalogPage        = ref(0)
const CAT_PER_PAGE       = 3

const selectedSubCatalog = ref<SubCatalog | null>(null)
const subCatalogPage     = ref(0)

// --- search ---
const searchCatalog    = ref('')
const searchSubCatalog = ref('')
const searchProduct    = ref('')

watch(searchCatalog,    () => { catalogPage.value    = 0 })
watch(searchSubCatalog, () => { subCatalogPage.value = 0 })
watch(searchProduct,    () => { productPage.value    = 0 })

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
const subModalCatalogId  = ref<number | null>(null)

const productPage        = ref(0)
const PROD_PER_PAGE      = 3
const slideDirection     = ref<'left' | 'right'>('left')
const catSlideDirection  = ref<'left' | 'right'>('left')
const subSlideDirection  = ref<'left' | 'right'>('left')

// ── loading / submitting states ──
const savingCatalog     = ref(false)
const savingSub         = ref(false)
const deletingProductId = ref<number | null>(null)

function prevProd() { slideDirection.value    = 'right'; productPage.value-- }
function nextProd() { slideDirection.value    = 'left';  productPage.value++ }
function prevCat()  { catSlideDirection.value = 'right'; catalogPage.value-- }
function nextCat()  { catSlideDirection.value = 'left';  catalogPage.value++ }
function prevSub()  { subSlideDirection.value = 'right'; subCatalogPage.value-- }
function nextSub()  { subSlideDirection.value = 'left';  subCatalogPage.value++ }


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
const matchedCatalogs = computed(() => {
  const q = searchCatalog.value.trim().toLowerCase()
  return q ? catalogs.value.filter(c => c.name.toLowerCase().includes(q)) : catalogs.value
})
const visibleCatalogs = computed(() => {
  const s = catalogPage.value * CAT_PER_PAGE
  return matchedCatalogs.value.slice(s, s + CAT_PER_PAGE)
})
const canPrevCat = computed(() => catalogPage.value > 0)
const canNextCat = computed(() => (catalogPage.value + 1) * CAT_PER_PAGE < matchedCatalogs.value.length)

const filteredSubs = computed(() => {
  const byCatalog = selectedCatalog.value
    ? subCatalogs.value.filter(s => s.catalogId === selectedCatalog.value!.id)
    : subCatalogs.value
  const q = searchSubCatalog.value.trim().toLowerCase()
  return q ? byCatalog.filter(s => s.name.toLowerCase().includes(q)) : byCatalog
})
const visibleSubs = computed(() => {
  const s = subCatalogPage.value * CAT_PER_PAGE
  return filteredSubs.value.slice(s, s + CAT_PER_PAGE)
})
const canPrevSub = computed(() => subCatalogPage.value > 0)
const canNextSub = computed(() => (subCatalogPage.value + 1) * CAT_PER_PAGE < filteredSubs.value.length)

const filteredProducts = computed(() => {
  let list = selectedSubCatalog.value
    ? products.value.filter(p => p.subCatalogId === selectedSubCatalog.value!.id)
    : selectedCatalog.value
      ? products.value.filter(p => p.catalogId === selectedCatalog.value!.id)
      : products.value
  const q = searchProduct.value.trim().toLowerCase()
  return q ? list.filter(p => p.name.toLowerCase().includes(q)) : list
})
const visibleProducts = computed(() => {
  const s = productPage.value * PROD_PER_PAGE
  return filteredProducts.value.slice(s, s + PROD_PER_PAGE)
})
const canPrevProd = computed(() => productPage.value > 0)
const canNextProd = computed(() => (productPage.value + 1) * PROD_PER_PAGE < filteredProducts.value.length)


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
    searchSubCatalog.value   = ''
    searchProduct.value      = ''
    return
  }
  selectedCatalog.value    = c
  selectedSubCatalog.value = null
  subCatalogPage.value     = 0
  productPage.value        = 0
  searchSubCatalog.value   = ''
  searchProduct.value      = ''
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
  subModalMode.value      = 'add'
  subModalName.value      = ''
  subModalCatalogId.value = selectedCatalog.value.id
  showSubModal.value      = true
}

function openEditSub(s: SubCatalog) {
  subModalMode.value      = 'edit'
  subModalName.value      = s.name
  subModalCatalogId.value = s.catalogId
  showSubModal.value      = true
}

async function saveSubModal() {
  if (!subModalName.value.trim()) { notify('Введите название подкаталога'); return }
  savingSub.value = true
  try {
    const slug = subModalName.value.trim().toLowerCase().replace(/\s+/g, '-')
    if (subModalMode.value === 'add') {
      await api.post('/admin/subcatalogs', { name: subModalName.value.trim(), slug, catalogId: subModalCatalogId.value })
    } else {
      await api.put(`/admin/subcatalogs/${selectedSubCatalog.value!.id}`, {
        name: subModalName.value.trim(), slug, catalogId: subModalCatalogId.value
      })
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
const productToEdit    = ref<Product | null>(null)

function openAddProduct() {
  productToEdit.value    = null
  showProductModal.value = true
}

function editProduct(p: Product) {
  productToEdit.value    = p
  showProductModal.value = true
}

async function removeProduct(id: number) {
  if (!confirm('Удалить товар?')) return
  deletingProductId.value = id
  try {
    await api.delete(`/admin/products/${id}`)
    await loadAll()
  } catch (e) { notify(apiError(e)) }
  finally { deletingProductId.value = null }
}
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
              <button class="btn btn--green btn--sm" @click="loadOrders()">&#8635; Обновить</button>
            </div>

            <!-- Filter bar -->
            <div v-if="ordersTotalElements > 0 || hasActiveFilters" class="orders-filter-bar">
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
              <select
                v-model="orderFilterStatus"
                class="orders-filter-status"
              >
                <option value="">Все статусы</option>
                <option value="pending">Ожидает</option>
                <option value="processing">В обработке</option>
                <option value="shipped">В доставке</option>
                <option value="delivered">Доставлен</option>
                <option value="ready_for_pickup">Готов к выдаче</option>
                <option value="picked_up">Выдан</option>
                <option value="cancelled">Отменён</option>
              </select>
              <button
                v-if="hasActiveFilters"
                class="btn btn--sm orders-reset-btn"
                @click="orderFilterUser = ''; orderFilterDateFrom = ''; orderFilterDateTo = ''; orderFilterStatus = ''"
              >✕ Сбросить</button>
            </div>

            <div v-if="ordersLoading" class="orders-loading">Загрузка...</div>
            <div v-else-if="orders.length === 0 && !hasActiveFilters" class="empty-hint" style="padding:20px 0">Заказов нет</div>
            <div v-else-if="orders.length === 0 && hasActiveFilters" class="empty-hint" style="padding:20px 0">Нет заказов по выбранным фильтрам</div>

            <div v-else class="orders-list">
              <template v-for="group in groupedOrders" :key="group.day">

                <div class="orders-day-header" @click="toggleDay(group.day)">
                  <span>{{ formatDayHeader(group.day) }} <span class="orders-day-count">({{ group.items.length }})</span></span>
                  <span class="orders-day-chevron" :class="{ 'orders-day-chevron--collapsed': collapsedDays.has(group.day) }">▼</span>
                </div>
              <div v-show="!collapsedDays.has(group.day)" class="orders-day-body">
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
                    <span v-if="order.cancelledBy === 'user'" class="order-cancelled-by-user">Отменён пользователем</span>
                    <select
                      class="order-status-select"
                      :class="`order-status-select--${order.status}`"
                      :value="order.status"
                      :disabled="order.cancelledBy === 'user'"
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
              </div>
              </template>
            </div>

            <!-- Pagination -->
            <div v-if="ordersTotalPages > 1" class="orders-pagination">
              <button
                class="orders-pagination__btn"
                :disabled="ordersPage === 0"
                @click="ordersPage--; loadOrders()"
              >← Назад</button>
              <span class="orders-pagination__info">
                {{ ordersPage + 1 }} / {{ ordersTotalPages }}
                <span class="orders-pagination__total">({{ ordersTotalElements }} заказов)</span>
              </span>
              <button
                class="orders-pagination__btn"
                :disabled="ordersPage >= ordersTotalPages - 1"
                @click="ordersPage++; loadOrders()"
              >Вперёд →</button>
            </div>
          </div>

          <!-- ===== Catalog & Products ===== -->
          <div v-if="activeTab === 'catalog'" class="section-box">

              <!-- Catalog editing -->
              <div class="section-header">
                <h3 class="section-title">Каталог</h3>
                <input
                  v-model="searchCatalog"
                  class="admin-search-input"
                  placeholder="Поиск..."
                  type="text"
                />
                <div class="section-actions">
                  <button class="btn btn--green"  @click="openAddCatalog">+ Добавить</button>
                  <button v-if="selectedCatalog" class="btn btn--orange" @click="openEditCatalog(selectedCatalog)">Изменить</button>
                </div>
              </div>

              <div class="carousel-row">
                <button class="arrow" :disabled="!canPrevCat" @click="prevCat">&#8592;</button>
                <div class="carousel-items carousel-items--overflow">
                  <transition :name="'slide-' + catSlideDirection" mode="out-in">
                    <div class="carousel-items__inner" :key="catalogPage + '_' + searchCatalog">
                      <button
                        v-for="c in visibleCatalogs" :key="c.id"
                        class="carousel-tag"
                        :class="{ 'carousel-tag--active': selectedCatalog?.id === c.id }"
                        @click="pickCatalog(c)"
                      >{{ c.name }}</button>
                      <span v-if="visibleCatalogs.length === 0" class="empty-hint">{{ searchCatalog ? 'Ничего не найдено' : 'Нет каталогов' }}</span>
                    </div>
                  </transition>
                </div>
                <button class="arrow" :disabled="!canNextCat" @click="nextCat">&#8594;</button>
              </div>

              <!-- Subcatalog editing -->
              <div class="section-header" style="margin-top:24px">
                <h3 class="section-title">Подкаталог</h3>
                <input
                  v-model="searchSubCatalog"
                  class="admin-search-input"
                  placeholder="Поиск..."
                  type="text"
                />
                <div class="section-actions">
                  <button class="btn btn--green" @click="openAddSub" :disabled="!selectedCatalog">+ Добавить</button>
                  <button v-if="selectedSubCatalog" class="btn btn--orange" @click="openEditSub(selectedSubCatalog)">Изменить</button>
                </div>
              </div>

              <div class="carousel-row">
                <button class="arrow" :disabled="!canPrevSub" @click="prevSub">&#8592;</button>
                <div class="carousel-items carousel-items--overflow">
                  <transition :name="'slide-' + subSlideDirection" mode="out-in">
                    <div class="carousel-items__inner" :key="subCatalogPage + '_' + searchSubCatalog + '_' + selectedCatalog?.id">
                      <button
                        v-for="s in visibleSubs" :key="s.id"
                        class="carousel-tag"
                        :class="{ 'carousel-tag--active': selectedSubCatalog?.id === s.id }"
                        @click="pickSub(s)"
                      >{{ s.name }}</button>
                      <span v-if="visibleSubs.length === 0" class="empty-hint">
                        {{ searchSubCatalog ? 'Ничего не найдено' : selectedCatalog ? 'Нет подкаталогов' : 'Выберите каталог или введите поиск' }}
                      </span>
                    </div>
                  </transition>
                </div>
                <button class="arrow" :disabled="!canNextSub" @click="nextSub">&#8594;</button>
              </div>

              <!-- Product search -->
              <div class="section-header" style="margin-top:24px; margin-bottom:0">
                <h3 class="section-title">Товары</h3>
                <input
                  v-model="searchProduct"
                  class="admin-search-input"
                  placeholder="Поиск по названию..."
                  type="text"
                />
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
                          <div class="product-card__img" @click="router.push(`/product/${p.id}`)">
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
    <AdminProductModal
      v-if="showProductModal"
      :product="productToEdit"
      @close="showProductModal = false"
      @saved="showProductModal = false; loadAll(); notify('Товар сохранён', 'success')"
      @deleted="showProductModal = false; loadAll(); notify('Товар удалён', 'success')"
    />

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
        <label class="form-row" style="margin-bottom:10px">
          <span class="form-label">Каталог:</span>
          <span class="form-input-static">{{ catalogs.find(c => c.id === subModalCatalogId)?.name ?? '—' }}</span>
        </label>
        <label class="form-row">
          <span class="form-label">Название:</span>
          <input v-model="subModalName" class="form-input" :disabled="savingSub" @keyup.enter="saveSubModal" />
        </label>
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
  @include page-layout;
  background: #ececec;
}

.container { @include container; }

.admin-layout {
  display: flex;
  background: #fff;
  border: 1px solid #d0d0d0;
  border-radius: $radius-sm;
  overflow: hidden;
  min-height: 500px;

  @include below-md { flex-direction: column; }
}

.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid #d0d0d0;

  @include below-md {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #d0d0d0;
    display: flex;
    flex-wrap: wrap;
  }
}

.sidebar-tab {
  display: block;
  width: 100%;
  padding: 12px $gap-md;
  background: #fff;
  border: none;
  border-bottom: 1px solid $color-border-light;
  text-align: left;
  font-size: $font-base;
  color: #333;
  cursor: pointer;
  line-height: 1.4;
  transition: background 0.15s;

  &:hover { background: $color-bg-light; }
  &--active { background: #ececec; font-weight: 600; }

  @include below-md {
    width: auto;
    flex: 1;
    border-bottom: none;
    border-right: 1px solid $color-border-light;
    text-align: center;
    &:last-child { border-right: none; }
  }
}

.admin-content {
  flex: 1;
  padding: $gap-lg;
  background: $color-bg-light;
  min-width: 0;

  @include below-sm { padding: $gap-md; }
}

.section-box {
  background: #fff;
  border: 1px solid #d0d0d0;
  border-radius: $radius-sm;
  padding: $gap-md $gap-lg;

  @include below-sm { padding: $gap-md; }
}

.section-title {
  text-align: center;
  font-size: $font-md;
  font-weight: 600;
  color: #333;
  margin-bottom: 14px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: $gap-sm;

  .section-title { margin-bottom: 0; }
}

.section-actions {
  display: flex;
  gap: $gap-sm;

  &--disabled {
    opacity: 0.45;
    pointer-events: none;
  }
}

.admin-search-input {
  flex: 1;
  max-width: 180px;
  height: 26px;
  padding: 0 $gap-sm;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  font-size: $font-sm;
  font-family: inherit;
  background: #fafafa;

  &:focus { outline: none; border-color: $color-primary; background: #fff; }
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

  &--sm { height: 24px; padding: 0 10px; font-size: $font-sm; }
}

.btn-spinner {
  display: inline-block;
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: $radius-full;
  animation: btn-spin 0.55s linear infinite;
  flex-shrink: 0;

  &--sm { width: 10px; height: 10px; border-width: 1.5px; }
}

@keyframes btn-spin { to { transform: rotate(360deg); } }

.upload-btn--disabled {
  opacity: 0.45;
  cursor: not-allowed;
  pointer-events: none;
}

.carousel-row {
  display: flex;
  align-items: center;
  gap: $gap-sm;
  margin-bottom: 4px;
}

.carousel-items {
  display: flex;
  gap: $gap-sm;
  flex: 1;
  min-height: 36px;
  align-items: center;

  &--overflow { overflow: hidden; }

  &__inner {
    display: flex;
    gap: $gap-sm;
    width: 100%;
    align-items: center;
  }
}

.carousel-tag {
  flex: 1;
  height: 34px;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  background: #fff;
  font-size: $font-base;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;

  &:hover { background: $color-bg-light; }
  &--active { background: #fff3cd; border-color: $color-primary; font-weight: 600; }
}

.arrow {
  width: 28px;
  height: 28px;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  background: #fff;
  cursor: pointer;
  font-size: $font-lg;
  line-height: 1;
  flex-shrink: 0;
  transition: background 0.15s;

  &:hover:not(:disabled) { background: $color-bg-light; }
  &:disabled { opacity: 0.35; cursor: default; }
}

.empty-hint {
  font-size: $font-sm;
  color: $color-text-faint;
}

.image-upload-section {
  max-width: 480px;
  margin-top: $gap-md;
  margin-bottom: 4px;
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
    background: rgba(0,0,0,0.55);
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
  border: 1px solid $color-primary;
  border-radius: $radius-pill;
  font-size: $font-sm;
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
    font-size: $font-xs;
    color: inherit;
    opacity: 0.7;
    line-height: 1;
    transition: opacity 0.15s;
    &:hover { opacity: 1; }
  }
}

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
  border: 1px solid $color-border;
  border-radius: $radius-sm;
  overflow: hidden;
  background: #fff;

  &__img {
    width: 100%;
    aspect-ratio: 4/3;
    background: #d0d0d0;
    overflow: hidden;
    cursor: pointer;

    img { width: 100%; height: 100%; object-fit: cover; display: block; }
    &:hover img { opacity: 0.85; }
  }

  &__body { padding: $gap-sm; }
  &__name { font-size: $font-base; font-weight: 500; margin-bottom: 4px; }
  &__price { font-size: $font-base; color: #333; margin-bottom: $gap-sm; }
  &__actions { display: flex; gap: 6px; }
}

.modal-image-section { margin-top: $gap-md; }

.modal-image-label {
  font-size: $font-base;
  color: #333;
  margin-bottom: $gap-sm;
}

.modal-image-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  overflow: hidden;

  img { width: 100%; height: 100%; object-fit: cover; display: block; }
}

.modal-image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: $radius-full;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 11px;
  cursor: pointer;
  @include flex-center;
  padding: 0;
  transition: background 0.15s;

  &:hover { background: $color-danger; }
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
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
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);

  @include below-xs { width: calc(100vw - 32px); padding: $gap-md; }
  &--wide { width: 560px; }
}

.modal-title {
  text-align: center;
  font-size: $font-lg;
  font-weight: 600;
  margin-bottom: $gap-md;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
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

.form-input-static {
  flex: 1;
  height: 30px;
  padding: 0 $gap-sm;
  border: 1px solid $color-border;
  border-radius: $radius-sm;
  font-size: $font-base;
  background: $color-bg-light;
  color: $color-text-secondary;
  display: flex;
  align-items: center;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: $gap-md;
  justify-content: flex-end;
  max-width: 480px;
  flex-wrap: wrap;
}

.notif-container {
  position: fixed;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  gap: $gap-sm;
  z-index: 9999;
  pointer-events: none;
  min-width: 280px;
  max-width: 480px;
}

.notif {
  padding: 10px 18px;
  border-radius: $radius-sm;
  font-size: $font-base;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  pointer-events: all;
  box-shadow: 0 2px 12px rgba(0,0,0,0.18);

  &--error   { background: $color-danger; }
  &--success { background: #2ecc40; }
  &--info    { background: #3498db; }
}

.notif-enter-active,
.notif-leave-active { transition: all 0.25s ease; }
.notif-enter-from   { opacity: 0; transform: translateY(-16px); }
.notif-leave-to     { opacity: 0; transform: translateY(-8px); }

.orders-filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $gap-sm;
  padding: 10px 0 $gap-sm;
  border-bottom: 1px solid $color-border-light;
  margin-bottom: $gap-sm;
}

.orders-filter-input {
  height: 28px;
  padding: 0 $gap-sm;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  font-size: $font-base;
  font-family: inherit;
  flex: 1;
  min-width: 150px;
  max-width: 220px;
  &:focus { outline: none; border-color: $color-primary; }
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
  border-radius: $radius-sm;
  font-size: $font-sm;
  font-family: inherit;
  width: 128px;
  &:focus { outline: none; border-color: $color-primary; }
}

.orders-filter-sep {
  font-size: $font-sm;
  color: $color-text-muted;
  flex-shrink: 0;
}

.orders-filter-status {
  height: 28px;
  padding: 0 6px;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  font-size: $font-sm;
  font-family: inherit;
  background: #fff;
  cursor: pointer;
  flex-shrink: 0;
  &:focus { outline: none; border-color: $color-primary; }
}

.orders-sort-btn {
  height: 28px;
  padding: 0 10px;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  background: #fff;
  font-size: $font-sm;
  font-family: inherit;
  cursor: pointer;
  flex-shrink: 0;
  transition: border-color 0.15s, background 0.15s;
  &:hover { border-color: $color-primary; background: $color-primary-light; }
}

.orders-reset-btn {
  background: $color-bg;
  color: $color-text;
  &:hover:not(:disabled) { filter: brightness(0.92); }
}

.orders-day-header {
  font-size: $font-sm;
  font-weight: 600;
  color: $color-text-secondary;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 10px 6px 6px;
  border-bottom: 1px solid $color-border-light;
  margin-bottom: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  user-select: none;
  border-radius: $radius-sm;
  transition: background 0.15s;

  &:first-child { padding-top: 4px; }
  &:hover { background: $color-bg; }
}

.orders-day-count {
  font-weight: 400;
  color: $color-text-faint;
  text-transform: none;
  letter-spacing: 0;
}

.orders-day-chevron {
  font-size: $font-sm;
  color: $color-text-faint;
  flex-shrink: 0;
  transition: transform 0.2s ease;

  &--collapsed { transform: rotate(-90deg); }
}

.orders-day-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.orders-loading {
  text-align: center;
  padding: $gap-lg;
  color: $color-text-muted;
  font-size: $font-base;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.order-row {
  border: 1px solid $color-border;
  border-radius: $radius-sm;
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
    &:hover { background: $color-bg; }
  }

  &__id {
    font-size: $font-base;
    font-weight: 700;
    color: $color-dark;
    min-width: 36px;
    flex-shrink: 0;
  }

  &__date {
    font-size: $font-sm;
    color: $color-text-muted;
    flex-shrink: 0;
    min-width: 110px;
  }

  &__user {
    flex: 1;
    font-size: $font-base;
    color: #333;
    min-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__email {
    font-size: $font-xs;
    color: $color-text-faint;
  }

  &__total {
    font-size: $font-base;
    font-weight: 600;
    color: $color-dark;
    flex-shrink: 0;
    min-width: 90px;
    text-align: right;
  }

  &__status-wrap { flex-shrink: 0; }

  &__toggle {
    font-size: 10px;
    color: $color-text-faint;
    flex-shrink: 0;
    width: 14px;
    text-align: center;
  }

  &__items {
    border-top: 1px solid $color-border-light;
    padding: 10px 12px 6px;
    background: #fff;
  }

  &__footer {
    display: flex;
    gap: $gap-md;
    justify-content: flex-end;
    padding-top: $gap-sm;
    border-top: 1px solid $color-bg;
    margin-top: $gap-sm;
    flex-wrap: wrap;
  }
}

.order-cancelled-by-user {
  display: inline-block;
  padding: 2px 8px;
  background: #fce4ec;
  color: #c62828;
  border: 1px solid #ef9a9a;
  border-radius: 10px;
  font-size: $font-xs;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.order-status-select {
  height: 26px;
  padding: 0 6px;
  border: 1px solid #ccc;
  border-radius: $radius-sm;
  font-size: $font-sm;
  cursor: pointer;
  outline: none;
  transition: border-color 0.15s;
  &:disabled { opacity: 0.5; cursor: not-allowed; }
  &:focus { border-color: $color-primary; }

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
  border-bottom: 1px solid $color-bg-light;
  &:last-of-type { border-bottom: none; }

  &__img {
    width: 40px;
    height: 40px;
    object-fit: cover;
    border-radius: $radius-sm;
    border: 1px solid $color-border-light;
    flex-shrink: 0;
    &--placeholder { background: $color-border-light; }
  }

  &__name {
    flex: 1;
    font-size: $font-base;
    color: #333;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__qty {
    font-size: $font-sm;
    color: $color-text-secondary;
    flex-shrink: 0;
    min-width: 44px;
    text-align: right;
  }

  &__price {
    font-size: $font-sm;
    color: $color-text-muted;
    flex-shrink: 0;
    min-width: 72px;
    text-align: right;
  }

  &__subtotal {
    font-size: $font-base;
    font-weight: 600;
    color: $color-dark;
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
  font-size: $font-sm;
  color: $color-dark;
  font-weight: 500;
  text-align: left;
  order: -1;
}

.order-footer__phone,
.order-footer__delivery,
.order-footer__total {
  font-size: $font-sm;
  color: $color-text;
}

.order-footer__total {
  font-weight: 700;
  color: $color-dark;
}

.orders-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $gap-md;
  padding: $gap-md 0 4px;

  &__btn {
    padding: 6px $gap-md;
    border: 1px solid $color-border;
    border-radius: $radius-sm;
    background: #fff;
    font-size: $font-base;
    cursor: pointer;
    transition: background 0.15s, border-color 0.15s;

    &:hover:not(:disabled) { background: $color-bg-light; border-color: #bbb; }
    &:disabled { opacity: 0.4; cursor: default; }
  }

  &__info {
    font-size: $font-base;
    color: $color-text;
    white-space: nowrap;
  }

  &__total {
    color: $color-text-faint;
    font-size: $font-sm;
    margin-left: 4px;
  }
}
</style>
