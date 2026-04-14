import { ref } from 'vue'
import { categoryService } from '@/services/categoryApi'
import type { CategoryWithSubcategories } from '@/types/category'

const mockCategoriesTree: CategoryWithSubcategories[] = [
    {
        id: 1,
        name: 'Электроинструмент',
        slug: 'electro-tools',
        subcategories: [
            { id: 101, name: 'Дрели', slug: 'drills' },
            { id: 102, name: 'Шуруповёрты', slug: 'screwdrivers' },
            { id: 103, name: 'Перфораторы', slug: 'rotary-hammers' },
            { id: 104, name: 'Болгарки (УШМ)', slug: 'angle-grinders' },
            { id: 105, name: 'Электролобзики', slug: 'jigsaws' }
        ]
    },
    {
        id: 2,
        name: 'Ручной инструмент',
        slug: 'hand-tools',
        subcategories: [
            { id: 201, name: 'Молотки', slug: 'hammers' },
            { id: 202, name: 'Отвёртки', slug: 'screwdrivers-hand' },
            { id: 203, name: 'Гаечные ключи', slug: 'wrenches' },
            { id: 204, name: 'Пассатижи', slug: 'pliers' },
            { id: 205, name: 'Ножовки', slug: 'handsaws' }
        ]
    },
    {
        id: 3,
        name: 'Садовая техника',
        slug: 'garden',
        subcategories: [
            { id: 301, name: 'Газонокосилки', slug: 'lawn-mowers' },
            { id: 302, name: 'Триммеры', slug: 'trimmers' },
            { id: 303, name: 'Культиваторы', slug: 'cultivators' },
            { id: 304, name: 'Цепные пилы', slug: 'chain-saws' }
        ]
    },
    {
        id: 4,
        name: 'Автоинструмент',
        slug: 'auto-tools',
        subcategories: [
            { id: 401, name: 'Домкраты', slug: 'jacks' },
            { id: 402, name: 'Компрессоры', slug: 'compressors' },
            { id: 403, name: 'Наборы ключей', slug: 'wrench-sets' },
            { id: 404, name: 'Съёмники', slug: 'pullers' }
        ]
    },
    {
        id: 5,
        name: 'Крепёж и метизы',
        slug: 'fasteners',
        subcategories: [
            { id: 501, name: 'Саморезы', slug: 'self-tapping-screws' },
            { id: 502, name: 'Болты', slug: 'bolts' },
            { id: 503, name: 'Гайки', slug: 'nuts' },
            { id: 504, name: 'Шайбы', slug: 'washers' },
            { id: 505, name: 'Дюбели', slug: 'dowels' }
        ]
    },
    {
        id: 6,
        name: 'Расходные материалы',
        slug: 'consumables',
        subcategories: [
            { id: 601, name: 'Свёрла', slug: 'drill-bits' },
            { id: 602, name: 'Отрезные диски', slug: 'cutting-discs' },
            { id: 603, name: 'Буры', slug: 'drill-bits-sds' },
            { id: 604, name: 'Щётки', slug: 'brushes' },
            { id: 605, name: 'Наждачная бумага', slug: 'sandpaper' }
        ]
    }
]

export function useCatalogMenu() {
    const categories = ref<CategoryWithSubcategories[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)

    const fetchCategoriesTree = async () => {
        loading.value = true
        error.value = null

        try {
            const data = await categoryService.getCategoriesTree()
            categories.value = data
        } catch {
            categories.value = mockCategoriesTree
        } finally {
            loading.value = false
        }
    }

    return {
        categories,
        loading,
        error,
        fetchCategoriesTree
    }
}
