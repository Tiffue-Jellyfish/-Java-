package dao;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Attendance;
import util.DBUtil;
import util.DateUtil;

public class AttendanceDao {
    public static final String TABLE_NAME = "attendance";
    public static final String COLUMN_ID = "attendanceId";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_START_TIME = "startTime";
    public static final String COLUMN_END_TIME = "endTime";
    public static final String COLUMN_STATUS = "status";

    public boolean addAttendance(Attendance attendance) {
        if (attendance == null) {//判断attendance的传入对象是否为null
            System.err.println("添加失败: attendance对象为null");
            return false;
        }
        // 获取当前最大ID
        int newId = 1;
        String getMaxIdSql = "SELECT MAX(" + COLUMN_ID + ") AS maxId FROM " + TABLE_NAME;
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getMaxIdSql)) {

            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (!rs.wasNull() && maxId > 0) {
                    newId = maxId + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取最大考勤ID失败: " + e.getMessage());
            return false;
        }

        // 设置新ID
        attendance.setId(newId);

        // 修改插入SQL（包含ID字段）
        String insertSql = "INSERT INTO attendance(attendanceId, userId, startTime, endTime, status) VALUES(?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            // 设置参数（包含ID）
            stmt.setInt(1, attendance.getId());
            stmt.setInt(2, attendance.getUserId());
            stmt.setTimestamp(3, attendance.getStartTime() != null ?
                    new Timestamp(attendance.getStartTime().getTime()) : null);
            stmt.setTimestamp(4, attendance.getEndTime() != null ?
                    new Timestamp(attendance.getEndTime().getTime()) : null);
            stmt.setString(5, attendance.getStatus());

            // 执行插入
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("添加考勤记录失败，没有行受影响");
                return false;
            }

            System.out.println("成功插入考勤记录，ID: " + attendance.getId());
            return true;

        } catch (SQLException e) {
            System.err.println("添加考勤记录失败: " + e.getMessage());
            System.err.println("SQL: " + insertSql);
            System.err.println("参数: [" + attendance.getId() + ", " + attendance.getUserId() + ", "
                    + (attendance.getStartTime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(attendance.getStartTime()) : "null") + ", "
                    + (attendance.getEndTime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(attendance.getEndTime()) : "null") + ", "
                    + attendance.getStatus() + "]");
            return false;
        }
    }

    // 更新考勤记录
    public boolean updateAttendance(Attendance attendance) {
        if (attendance == null || attendance.getId() <= 0) {
            System.err.println("更新失败: 无效的attendance对象或ID");
            return false;
        }

        String sql = String.format(
                "UPDATE %s SET %s=?, %s=?, %s=?, %s=? WHERE %s=?",
                TABLE_NAME, COLUMN_USER_ID, COLUMN_START_TIME,
                COLUMN_END_TIME, COLUMN_STATUS, COLUMN_ID
        );

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置参数
            pstmt.setInt(1, attendance.getUserId());
            pstmt.setTimestamp(2, attendance.getStartTime() != null ?
                    new Timestamp(attendance.getStartTime().getTime()) : null);
            pstmt.setTimestamp(3, attendance.getEndTime() != null ?
                    new Timestamp(attendance.getEndTime().getTime()) : null);
            pstmt.setString(4, attendance.getStatus());
            pstmt.setInt(5, attendance.getId());

            // 执行更新
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("更新考勤记录失败，没有行受影响");
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("更新考勤记录失败: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("参数: [" + attendance.getId() + ", "
                    + attendance.getUserId() + ", "
                    + (attendance.getStartTime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(attendance.getStartTime()) : "null") + ", "
                    + (attendance.getEndTime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(attendance.getEndTime()) : "null") + ", "
                    + attendance.getStatus() + "]");
            return false;
        }
    }

    // 删除考勤记录
    public boolean deleteAttendance(int attendanceId) {
        if (attendanceId <= 0) {
            System.err.println("删除失败: 无效的attendanceId");
            return false;
        }

        String sql = String.format(
                "DELETE FROM %s WHERE %s=?",
                TABLE_NAME, COLUMN_ID
        );

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置参数
            pstmt.setInt(1, attendanceId);

            // 执行删除
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("删除考勤记录失败，没有行受影响");
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("删除考勤记录失败: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("attendanceId: " + attendanceId);
            return false;
        }
    }

    public List<Attendance> getAttendancesByStatus(String statusName) {
        List<Attendance> allAttendances = getAllAttendances();
        List<Attendance> filtered = new ArrayList<>();

        // 将输入拆分为多个状态条件（支持 "迟到,早退" 或 "迟到"）
        String[] queryStatuses = statusName.split(",");

        for (Attendance att : allAttendances) {
            String status = att.getStatus();
            if (status == null) continue;

            // 只要满足其中一个条件即通过
            for (String queryStatus : queryStatuses) {
                if (matchesStatus(status, queryStatus.trim())) {
                    filtered.add(att);
                    break; // 避免重复添加
                }
            }
        }
        return filtered;
    }

    /**
     * 状态匹配逻辑（支持复合状态判断）
     * @param actualStatus 数据库存储的实际状态
     * @param queryStatus 查询条件状态
     * @return 是否匹配
     */
    private boolean matchesStatus(String actualStatus, String queryStatus) {
        // 处理"正常"状态（保持原逻辑）
        if ("正常".equals(queryStatus)) {
            return "正常".equals(actualStatus) ||
                    (!actualStatus.contains("迟到") && !actualStatus.contains("早退"));
        }

        // 处理复合状态：拆分为多个条件，要求全部满足
        if (queryStatus.contains(",")) {
            String[] requiredStatuses = queryStatus.split(",");
            for (String req : requiredStatuses) {
                if (!actualStatus.contains(req.trim())) {
                    return false; // 任一条件缺失即失败
                }
            }
            return true; // 所有条件均满足
        }

        // 处理单一状态：只需包含目标状态
        return actualStatus.contains(queryStatus);
    }



    public List<Attendance> getAttendancesByDate(Date date, Integer userId) {
        String sql = "SELECT " + COLUMN_ID + ", " + COLUMN_USER_ID + ", " +
                COLUMN_START_TIME + ", " + COLUMN_END_TIME + ", " + COLUMN_STATUS +
                " FROM " + TABLE_NAME + " WHERE " +
                (userId != null ? COLUMN_USER_ID + " = ? AND " : "") +
                "CONVERT(date, " + COLUMN_START_TIME + ") = CONVERT(date, ?) " +
                "ORDER BY " + COLUMN_START_TIME + " DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (userId != null) {
                pstmt.setInt(paramIndex++, userId);
            }
            pstmt.setTimestamp(paramIndex++, new Timestamp(date.getTime()));

            ResultSet rs = pstmt.executeQuery();
            List<Attendance> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapResultSetToAttendance(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("按日期查询考勤记录失败: " + e.getMessage(), e);
        }
    }






    public Attendance getAttendanceById(int attendanceId) {
        String sql = "SELECT attendanceId, userId, startTime, endTime, status " +
                "FROM attendance WHERE attendanceId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attendanceId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Attendance att = new Attendance();
                att.setId(rs.getInt("attendanceId"));
                att.setUserId(rs.getInt("userId"));
                att.setStartTime(rs.getTimestamp("startTime"));
                att.setEndTime(rs.getTimestamp("endTime"));
                att.setStatus(rs.getString("status"));
                att.calculateWorkingTime();
                return att;
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("查询考勤记录失败，ID=" + attendanceId, e);
        }
    }



    public List<Attendance> getAttendancesByUserId(int userId) {
        String sql = String.format(
                "SELECT %s, %s, %s, %s, %s " +
                        "FROM %s WHERE %s = ? ORDER BY %s DESC",
                COLUMN_ID, COLUMN_USER_ID, COLUMN_START_TIME,
                COLUMN_END_TIME, COLUMN_STATUS,
                TABLE_NAME, COLUMN_USER_ID, COLUMN_START_TIME
        );

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 添加日志输出查询参数
            System.out.println("执行用户ID查询，userId=" + userId);

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            List<Attendance> attendances = new ArrayList<>();
            while (rs.next()) {
                attendances.add(mapResultSetToAttendance(rs));
            }

            // 添加日志输出查询结果数量
            System.out.println("查询到" + attendances.size() + "条记录");

            return attendances;
        } catch (SQLException e) {
            // 更详细的错误日志
            System.err.println("按用户ID查询失败，SQL: " + sql);
            System.err.println("错误详情: " + e.getMessage());
            throw new RuntimeException("按用户ID查询考勤记录失败: " + e.getMessage(), e);
        }
    }

    public List<Attendance> getAttendancesByDateRange(Date startDate, Date endDate, Integer userId) {
        Date start = DateUtil.getStartOfDay(startDate);
        Date end = DateUtil.getEndOfDay(endDate);

        String sql = "SELECT * FROM attendance WHERE " +
                (userId != null ? "userId = ? AND " : "") +
                "startTime BETWEEN ? AND ? " +
                "ORDER BY startTime DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (userId != null) {
                pstmt.setInt(paramIndex++, userId);
            }
            pstmt.setTimestamp(paramIndex++, new Timestamp(start.getTime()));
            pstmt.setTimestamp(paramIndex++, new Timestamp(end.getTime()));

            ResultSet rs = pstmt.executeQuery();
            List<Attendance> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapResultSetToAttendance(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("按日期范围查询考勤记录失败", e);
        }
    }



    public List<Attendance> getAttendancesAfterCheckTime(Date date, Time checkTime) {
        String sql = String.format(
                "SELECT %s, %s, %s, %s, %s " +
                        "FROM %s " +
                        "WHERE DATE(%s) = ? " +
                        "AND TIME(%s) >= ? " +
                        "ORDER BY %s DESC",
                COLUMN_ID, COLUMN_USER_ID, COLUMN_START_TIME,
                COLUMN_END_TIME, COLUMN_STATUS, TABLE_NAME,
                COLUMN_START_TIME, COLUMN_START_TIME, COLUMN_START_TIME
        );
        List<Attendance> attendances = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(date.getTime()));
            pstmt.setTime(2, checkTime);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attendances.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询指定时间后的签到记录失败", e);
        }
        return attendances;
    }


    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt(COLUMN_ID));  // 使用COLUMN_ID常量而不是"id"
        attendance.setUserId(rs.getInt(COLUMN_USER_ID));
        attendance.setStartTime(rs.getTimestamp(COLUMN_START_TIME));
        attendance.setEndTime(rs.getTimestamp(COLUMN_END_TIME));
        attendance.setStatus(rs.getString(COLUMN_STATUS));
        attendance.calculateWorkingTime();
        return attendance;
    }

    // 修改getAllAttendances方法
    public List<Attendance> getAllAttendances() {
        String sql = String.format(
                "SELECT %s, %s, %s, %s, %s " +
                        "FROM %s ORDER BY %s DESC",
                COLUMN_ID, COLUMN_USER_ID, COLUMN_START_TIME,
                COLUMN_END_TIME, COLUMN_STATUS, TABLE_NAME, COLUMN_START_TIME
        );
        List<Attendance> attendances = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                attendances.add(mapResultSetToAttendance(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询所有考勤记录失败", e);
        }
        return attendances;
    }
}
