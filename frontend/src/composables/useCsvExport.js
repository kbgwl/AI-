/**
 * CSV 导出 composable
 * 共享导出逻辑，消除 4 处重复
 */
export function useCsvExport() {
  function exportCsv(headers, data, filename) {
    const csvContent = [
      headers.join(','),
      ...data.map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    ].join('\n')

    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${filename}.csv`
    link.click()
    URL.revokeObjectURL(url)
  }

  return { exportCsv }
}
