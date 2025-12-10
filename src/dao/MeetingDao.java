package dao;

import model.Meeting;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeetingDao {

    // 添加会议
    public int addMeeting(Meeting meeting) throws SQLException {
        String getMaxIdSql = "SELECT MAX(meetingId) AS maxId FROM Meeting";
        String insertSql = "INSERT INTO Meeting(meetingId, meetingTime, content, location, participants, recorder, userId) VALUES (?, ?, ?, ?, ?, ?, ?)";

        int newId = 1;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmtMaxId = conn.prepareStatement(getMaxIdSql);
             ResultSet rs = pstmtMaxId.executeQuery()) {

            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (maxId > 0) {
                    newId = maxId + 1;
                }
            }

            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql)) {
                // 使用Timestamp处理日期+时间
                pstmtInsert.setInt(1, newId);
                pstmtInsert.setTimestamp(2, new Timestamp(meeting.getMeetingTime().getTime()));
                pstmtInsert.setString(3, meeting.getContent());
                pstmtInsert.setString(4, meeting.getLocation());
                pstmtInsert.setString(5, meeting.getParticipants());
                pstmtInsert.setString(6, meeting.getRecorder());
                pstmtInsert.setInt(7, meeting.getUserId());

                int rows = pstmtInsert.executeUpdate();
                return rows > 0 ? newId : -1;
            }
        }
    }

    // 修改会议（增加时间处理）
    public boolean updateMeeting(Meeting meeting) throws SQLException {
        String sql = "UPDATE Meeting SET meetingTime=?, content=?, location=?, participants=?, recorder=?, userId=? WHERE meetingId=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 使用Timestamp处理日期+时间
            pstmt.setTimestamp(1, new Timestamp(meeting.getMeetingTime().getTime()));
            pstmt.setString(2, meeting.getContent());
            pstmt.setString(3, meeting.getLocation());
            pstmt.setString(4, meeting.getParticipants());
            pstmt.setString(5, meeting.getRecorder());
            pstmt.setInt(6, meeting.getUserId());
            pstmt.setInt(7, meeting.getMeetingId());
            return pstmt.executeUpdate() == 1;
        }
    }

    // 删除会议
    public boolean deleteMeeting(int meetingId) throws SQLException {
        String sql = "DELETE FROM Meeting WHERE meetingId=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, meetingId);
            return pstmt.executeUpdate() == 1;
        }
    }

    // 根据ID获取会议
    public Meeting getMeetingById(int meetingId) throws SQLException {
        String sql = "SELECT * FROM Meeting WHERE meetingId=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, meetingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMeetingFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // 查询所有会议
    public List<Meeting> getAllMeetings() throws SQLException {
        return queryMeetings(null, null, null, null, null);
    }

    // 多条件模糊查询会议
    public List<Meeting> queryMeetings(Date meetingTime, String content, String location, String participant, String recorder) throws SQLException {
        List<Meeting> meetings = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Meeting WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (meetingTime != null) {
            sql.append(" AND meetingTime >= ? AND meetingTime < ?");
            Timestamp start = new Timestamp(meetingTime.getTime());
            Timestamp end = new Timestamp(meetingTime.getTime() + 24 * 60 * 60 * 1000);
            params.add(start);
            params.add(end);
        }
        if (content != null && !content.isEmpty()) {
            sql.append(" AND content LIKE ?");
            params.add("%" + content + "%");
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND location LIKE ?");
            params.add("%" + location + "%");
        }
        if (participant != null && !participant.isEmpty()) {
            for (String p : participant.split("[,， ]+")) {
                sql.append(" AND participants LIKE ?");
                params.add("%" + p.trim() + "%");
            }
        }
        if (recorder != null && !recorder.isEmpty()) {
            for (String r : recorder.split("[,， ]+")) {
                sql.append(" AND recorder LIKE ?");
                params.add("%" + r.trim() + "%");
            }
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    meetings.add(extractMeetingFromResultSet(rs));
                }
            }
        }

        return meetings;
    }

    // 封装结果集为 Meeting 对象
    private Meeting extractMeetingFromResultSet(ResultSet rs) throws SQLException {
        Meeting meeting = new Meeting();
        meeting.setMeetingId(rs.getInt("meetingId"));
        meeting.setMeetingTime(rs.getDate("meetingTime"));
        meeting.setContent(rs.getString("content"));
        meeting.setLocation(rs.getString("location"));
        meeting.setParticipants(rs.getString("participants"));
        meeting.setRecorder(rs.getString("recorder"));
        meeting.setUserId(rs.getInt("userId"));
        return meeting;
    }
}
