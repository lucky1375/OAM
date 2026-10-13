// 中国法定节假日判断工具
//
// 数据来源：国务院办公厅《关于2026年部分节假日安排的通知》，由
// NateScarlet/holiday-cn 项目（GitHub）按年整理并二次校验。
// 此处为了离线自包含，将 2026 年数据内置；跨年后可追加新年度数据。
//
// - holidays: { 'yyyy-MM-dd': 节日名 }  → 法定放假日
// - workdays: { 'yyyy-MM-dd': '补班' }  → 调休补班日（周末上班）

const HOLIDAYS = {
  '2026-01-01': '元旦', '2026-01-02': '元旦', '2026-01-03': '元旦',
  '2026-02-15': '春节', '2026-02-16': '春节', '2026-02-17': '春节',
  '2026-02-18': '春节', '2026-02-19': '春节', '2026-02-20': '春节',
  '2026-02-21': '春节', '2026-02-22': '春节', '2026-02-23': '春节',
  '2026-04-04': '清明节', '2026-04-05': '清明节', '2026-04-06': '清明节',
  '2026-05-01': '劳动节', '2026-05-02': '劳动节', '2026-05-03': '劳动节',
  '2026-05-04': '劳动节', '2026-05-05': '劳动节',
  '2026-06-19': '端午节', '2026-06-20': '端午节', '2026-06-21': '端午节',
  '2026-09-25': '中秋节', '2026-09-26': '中秋节', '2026-09-27': '中秋节',
  '2026-10-01': '国庆节', '2026-10-02': '国庆节', '2026-10-03': '国庆节',
  '2026-10-04': '国庆节', '2026-10-05': '国庆节', '2026-10-06': '国庆节',
  '2026-10-07': '国庆节'
}

const WORKDAYS = {
  '2026-01-04': '补班', '2026-02-14': '补班', '2026-02-28': '补班',
  '2026-05-09': '补班', '2026-09-20': '补班', '2026-10-10': '补班'
}

/**
 * 获取某天的休息/工作类型
 * @param {number} year
 * @param {number} month 0-11
 * @param {number} day
 * @returns {{ kind: string, name: string | null }}
 *   kind: 'holiday'(法定放假) | 'rest'(正常周末休息) | 'work'(调休补班) | 'normal'(普通工作日)
 */
export function getDayType(year, month, day) {
  const key = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  if (HOLIDAYS[key]) return { kind: 'holiday', name: HOLIDAYS[key] }
  if (WORKDAYS[key]) return { kind: 'work', name: WORKDAYS[key] }
  const weekday = new Date(year, month, day).getDay()
  if (weekday === 0 || weekday === 6) return { kind: 'rest', name: '周末' }
  return { kind: 'normal', name: null }
}

export async function loadYear() {
  return { holidays: HOLIDAYS, workdays: WORKDAYS }
}

export default { getDayType, loadYear }