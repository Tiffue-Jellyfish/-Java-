package service;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import model.Attendance;
import model.User;
import dao.AttendanceDao;
import dao.UserDao;
import util.AuthUtil;
import util.DateUtil;

public class AttendanceService {
    // 修正：初始化标准上下班时间（示例为 9:00 和 18:00，可根据实际需求调整）
    private static final Date STANDARD_START_TIME;
    private static final Date STANDARD_END_TIME;
    static {
        try {
            STANDARD_START_TIME = new SimpleDateFormat("HH:mm:ss").parse("09:00:00");
            STANDARD_END_TIME = new SimpleDateFormat("HH:mm:ss").parse("18:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化标准上下班时间失败", e);
        }
    }

    private final AttendanceDao attendanceDao;
    private final UserDao userDao;
    private final User currentUser;

    public AttendanceService(AttendanceDao attendanceDao, User currentUser) {
        if (attendanceDao == null) {
            throw new IllegalArgumentException("AttendanceDao不能为null");
        }
        this.attendanceDao = attendanceDao;
        this.userDao = new UserDao();
        this.currentUser = currentUser;
    }

    /**
     * 用户签到
     */
    public boolean signIn(int userId) {
        try {
            if (hasSignedInToday(userId)) {
                return false; // 今日已签到
            }

            Attendance attendance = new Attendance();
            attendance.setUserId(userId);
            attendance.setStartTime(new Date());
            attendance.setStatus("已签到");

            attendanceDao.addAttendance(attendance);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("签到失败: " + e.getMessage(), e);
        }
    }

    /**
     * 用户签退
     */
    public boolean signOut(int userId) {
        try {
            Attendance todayRecord = getTodayAttendance(userId);
            if (todayRecord == null || todayRecord.getEndTime() != null) {
                return false; // 没有签到记录或已签退
            }

            todayRecord.setEndTime(new Date());
            calculateAttendanceStatus(todayRecord); // 重新计算状态和工时
            todayRecord.setStatus("已完成");

            attendanceDao.updateAttendance(todayRecord);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("签退失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查今日是否已签到
     */
    public boolean hasSignedInToday(int userId) {
        Attendance todayRecord = getTodayAttendance(userId);
        return todayRecord != null && todayRecord.getStartTime() != null;
    }

    /**
     * 检查今日是否已签退
     */
    public boolean hasSignedOutToday(int userId) {
        Attendance todayRecord = getTodayAttendance(userId);
        return todayRecord != null && todayRecord.getEndTime() != null;
    }

    /**
     * 获取今日考勤记录
     */
    public Attendance getTodayAttendance(int userId) {
        Date today = DateUtil.getStartOfDay(new Date());
        List<Attendance> records = attendanceDao.getAttendancesByDate(today, userId);
        return records.isEmpty() ? null : records.get(0);
    }


    public void addAttendance(Attendance attendance) {
        // 修复权限验证逻辑：
        // 1. 管理员始终可以添加
        // 2. 员工需要满足两个条件：
        //    a) 拥有attendance_add权限
        //    b) 只能添加自己的考勤记录
        if (!AuthUtil.isAdmin(currentUser)) {
            // 检查权限字符串
            if (!currentUser.hasPermission("attendance_add")) {
                throw new SecurityException("添加考勤记录需要权限: attendance_add");
            }

            // 检查是否操作自己的记录
            if (attendance.getUserId() != currentUser.getUserId()) {
                throw new SecurityException("只能添加自己的考勤记录");
            }
        }

        // 自动计算考勤状态
        calculateAttendanceStatus(attendance);

        // 调用DAO层新增方法
        if (!attendanceDao.addAttendance(attendance)) {
            throw new RuntimeException("添加考勤记录失败");
        }
    }

    public void updateAttendance(Attendance attendance) {
        if (!AuthUtil.isAdmin(currentUser) && attendance.getUserId() != currentUser.getUserId()) {
            throw new SecurityException("只能修改自己的考勤记录");
        }
        calculateAttendanceStatus(attendance);
        attendanceDao.updateAttendance(attendance);
    }

    public Attendance getAttendanceById(int attendanceId) {
        Attendance att = attendanceDao.getAttendanceById(attendanceId);
        if (!AuthUtil.isAdmin(currentUser) && att.getUserId() != currentUser.getUserId()) {
            throw new SecurityException("无权查看他人考勤记录");
        }
        return att;
    }

    public List<Attendance> getAllAttendances() {
        checkAdminPermission("查看所有考勤记录");
        return attendanceDao.getAllAttendances();
    }

    /**
     * 自动计算考勤状态
     */
    private void calculateAttendanceStatus(Attendance attendance) {
        if (attendance.getStartTime() == null || attendance.getEndTime() == null) {
            attendance.setStatus("缺勤");
            return;
        }

        Time startTime = new Time(attendance.getStartTime().getTime());
        Time endTime = new Time(attendance.getEndTime().getTime());

        // 基于修复后的标准时间判断迟到、早退
        boolean isLate = startTime.after(STANDARD_START_TIME);
        boolean isEarlyLeave = endTime.before(STANDARD_END_TIME);

        StringBuilder statusBuilder = new StringBuilder();

        // 计算迟到情况
        if (isLate) {
            long lateMinutes = (startTime.getTime() - STANDARD_START_TIME.getTime()) / (1000 * 60);
            statusBuilder.append("迟到").append(lateMinutes).append("分钟");
        }

        // 计算早退情况
        if (isEarlyLeave) {
            if (isLate) {
                statusBuilder.append("且");
            }
            long earlyMinutes = (STANDARD_END_TIME.getTime() - endTime.getTime()) / (1000 * 60);
            statusBuilder.append("早退").append(earlyMinutes).append("分钟");
        }

        // 完全正常的情况
        if (!isLate && !isEarlyLeave) {
            statusBuilder.append("正常");
        }

        attendance.setStatus(statusBuilder.toString());

        // 计算工时
        calculateWorkingTime(attendance);
    }

    /**
     * 计算工作时长
     */
    private void calculateWorkingTime(Attendance attendance) {
        if (attendance.getStartTime() == null || attendance.getEndTime() == null) {
            attendance.setWorkingHours("0小时0分钟");
            return;
        }

        long diffMillis = attendance.getEndTime().getTime() - attendance.getStartTime().getTime();
        long hours = diffMillis / (1000 * 60 * 60);
        long minutes = (diffMillis / (1000 * 60)) % 60;

        attendance.setWorkingHours(hours + "小时" + minutes + "分钟");
    }

    public List<Attendance> getAttendancesByUserId(int userId) {
        try {
            // 先验证用户ID是否存在
            if (!userDao.userExists(userId)) {
                throw new RuntimeException("用户ID " + userId + " 不存在");
            }

            // 检查权限
            if (!AuthUtil.isAdmin(currentUser) && userId != currentUser.getUserId()) {
                throw new SecurityException("只能查看自己的考勤记录");
            }

            return attendanceDao.getAttendancesByUserId(userId);
        } catch (Exception e) {
            // 添加详细错误日志
            System.err.println("按用户ID查询服务层错误: " + e.getMessage());
            throw new RuntimeException("按用户ID查询考勤记录失败: " + e.getMessage(), e);
        }
    }

    public List<Attendance> getAttendancesByStatus(String string) {
        checkAdminPermission("按状态查询考勤记录");
        return attendanceDao.getAttendancesByStatus(string);
    }

    public void deleteAttendance(int attendanceId) {
        try {
            // 先查询记录是否存在
            Attendance att = getAttendanceById(attendanceId);
            if (att == null) {
                throw new RuntimeException("考勤记录不存在，ID=" + attendanceId);
            }

            // 检查权限
            if (!AuthUtil.isAdmin(currentUser) && att.getUserId() != currentUser.getUserId()) {
                throw new SecurityException("无权删除他人考勤记录");
            }

            // 执行删除
            attendanceDao.deleteAttendance(attendanceId);

        } catch (Exception e) {
            throw new RuntimeException("删除考勤记录失败，ID=" + attendanceId, e);
        }
    }

    public List<Attendance> getAttendancesByDateAndUser(Date date, int userId) {
        if (!isAdmin() && userId != currentUser.getUserId()) {
            throw new SecurityException("只能查询自己的考勤记录");
        }
        return attendanceDao.getAttendancesByDate(date, userId);
    }

    public List<Attendance> getAttendancesByDateRange(Date startDate, Date endDate) {
        if (isAdmin()) {
            return attendanceDao.getAttendancesByDateRange(startDate, endDate, null);
        }
        return attendanceDao.getAttendancesByDateRange(startDate, endDate, currentUser.getUserId());
    }

    public List<Attendance> getAttendancesByDateRangeAndUser(Date startDate, Date endDate, int userId) {
        if (!isAdmin() && userId != currentUser.getUserId()) {
            throw new SecurityException("只能查询自己的考勤记录");
        }
        return attendanceDao.getAttendancesByDateRange(startDate, endDate, userId);
    }

    public List<Attendance> getAttendancesAfterTime(Date date, Time time, Integer userId) {
        if (userId != null && !isAdmin() && !userId.equals(currentUser.getUserId())) {
            throw new SecurityException("只能查询自己的考勤记录");
        }
        return attendanceDao.getAttendancesAfterCheckTime(date, time);
    }

    public List<Attendance> getAttendancesByDate(String dateString) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        if (isAdmin()) {
            return attendanceDao.getAttendancesByDate(date, null);
        }
        return attendanceDao.getAttendancesByDate(date, currentUser.getUserId());
    }

    public List<Attendance> getAttendancesByDate(Date date) {
        try {
            List<Attendance> result;
            if (isAdmin()) {
                result = attendanceDao.getAttendancesByDate(date, null);
            } else {
                result = attendanceDao.getAttendancesByDate(date, currentUser.getUserId());
            }

            if (result.isEmpty()) {
                throw new Exception("没有找到指定日期的考勤记录");
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("日期查询失败: " + ex.getMessage(), ex);
        }
    }

    public List<Attendance> getAttendancesByDateAndUser(String dateString, int userId) throws ParseException {
        if (!isAdmin() && userId != currentUser.getUserId()) {
            throw new SecurityException("只能查询自己的考勤记录");
        }
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        return attendanceDao.getAttendancesByDate(date, userId);
    }

    public List<Attendance> getAttendancesByDateRange(String startDateString, String endDateString) throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateString);
        if (isAdmin()) {
            return attendanceDao.getAttendancesByDateRange(startDate, endDate, null);
        }
        return attendanceDao.getAttendancesByDateRange(startDate, endDate, currentUser.getUserId());
    }

    private void checkAdminPermission(String operation) {
        if (!AuthUtil.isAdmin(currentUser)) {
            throw new SecurityException(operation + "需要管理员权限");
        }
    }

    private boolean isAdmin() {
        return AuthUtil.isAdmin(currentUser);
    }
}



