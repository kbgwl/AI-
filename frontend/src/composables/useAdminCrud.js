import { ref, reactive } from 'vue'

/**
 * 管理后台 CRUD composable
 * 吸收列表、对话框、表单、加载、保存、删除的通用模式
 */
export function useAdminCrud(apiModule, options = {}) {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const dialogVisible = ref(false)
  const editing = ref(null)
  const form = ref({})
  const submitting = ref(false)

  const page = ref(1)
  const pageSize = ref(options.pageSize || 20)

  async function load(params = {}) {
    loading.value = true
    try {
      const res = await apiModule.list({ page: page.value, size: pageSize.value, ...params })
      list.value = res?.records || res || []
      total.value = res?.total || list.value.length
    } catch (e) {
      console.error('Load failed:', e)
    } finally {
      loading.value = false
    }
  }

  function openAdd() {
    editing.value = null
    form.value = options.defaultForm ? { ...options.defaultForm() } : {}
    dialogVisible.value = true
  }

  function openEdit(item) {
    editing.value = item
    form.value = { ...item }
    dialogVisible.value = true
  }

  async function save() {
    submitting.value = true
    try {
      if (editing.value) {
        await apiModule.update(form.value)
      } else {
        await apiModule.add(form.value)
      }
      dialogVisible.value = false
      await load()
    } finally {
      submitting.value = false
    }
  }

  async function remove(id) {
    await apiModule.delete(id)
    await load()
  }

  return {
    list,
    total,
    loading,
    dialogVisible,
    editing,
    form,
    submitting,
    page,
    pageSize,
    load,
    openAdd,
    openEdit,
    save,
    remove
  }
}
