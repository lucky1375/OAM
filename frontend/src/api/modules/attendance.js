import request from '../request'

export const getTodayAttendance = () => request.get('/attendance/today')
export const checkIn = (location) => request.post('/attendance/check-in', location)
export const checkOut = (location) => request.post('/attendance/check-out', location)

export const listAttendanceRecords = (month) => request.get('/attendance/records', { params: { month } })
export const getAttendanceStats = (month) => request.get('/attendance/stats', { params: { month } })
export const getAttendanceOverview = (date) => request.get('/attendance/overview', { params: { date } })

export const submitAttendancePatch = (data) => request.post('/attendance/patches', data)
export const listAttendancePatches = () => request.get('/attendance/patches')

export const getOfficeConfig = () => request.get('/attendance/office-config')
export const updateOfficeConfig = (data) => request.put('/attendance/office-config', data)
