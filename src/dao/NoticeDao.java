package dao;

import model.Notice;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDao {
    public List<Notice> getSystemNotices() throws SQLException {
        List<Notice> noticeList = new ArrayList<>();
        String sql = "String sql = \"SELECT * FROM Notice WHERE title = '系统通知' ORDER BY publishTime DESC\";";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Notice n = new Notice();
                n.setNoticeId(rs.getInt("noticeId"));
                n.setTitle(rs.getString("title"));
                n.setContent(rs.getString("content"));
                n.setPublishTime(rs.getTimestamp("publishTime"));
                n.setRecipients(rs.getString("recipients"));
                n.setUserId(rs.getInt("userId"));
                noticeList.add(n);
            }
        }
        return noticeList;
    }

    // ֧ ֱ   ģ    ѯ+    ʱ ䷶Χ  ѯ
    public List<Notice> queryNotices(String title, java.sql.Date startDate, java.sql.Date endDate, String recipients) throws SQLException {
        List<Notice> noticeList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Notice WHERE 1=1");

        if (title != null && !title.isEmpty()) {
            sql.append(" AND title LIKE ?");
        }
        if (startDate != null) {
            sql.append(" AND publishTime >= ?");
        }
        if (endDate != null) {
            sql.append(" AND publishTime <= ?");
        }
        //          ˲ ѯ
        if (recipients != null && !recipients.isEmpty()) {
            sql.append(" AND recipients LIKE ?");
        }
        sql.append(" ORDER BY publishTime DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (title != null && !title.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + title + "%");
            }
            if (startDate != null) {
                pstmt.setDate(paramIndex++, startDate);
            }
            if (endDate != null) {
                pstmt.setDate(paramIndex++, endDate);
            }
            //    ý    ˲
            if (recipients != null && !recipients.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + recipients + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notice n = new Notice();
                    n.setNoticeId(rs.getInt("noticeId"));
                    n.setTitle(rs.getString("title"));
                    n.setContent(rs.getString("content"));
                    n.setPublishTime(rs.getTimestamp("publishTime"));
                    n.setRecipients(rs.getString("recipients"));
                    n.setUserId(rs.getInt("userId"));
                    noticeList.add(n);
                }
            }
        }
        return noticeList;
    }

    //     ID  ѯ
    public Notice getNoticeById(int noticeId) throws SQLException {
        String sql = "SELECT * FROM Notice WHERE noticeId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noticeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Notice n = new Notice();
                    n.setNoticeId(rs.getInt("noticeId"));
                    n.setTitle(rs.getString("title"));
                    n.setContent(rs.getString("content"));
                    n.setPublishTime(rs.getTimestamp("publishTime"));
                    n.setRecipients(rs.getString("recipients"));
                    n.setUserId(rs.getInt("userId"));
                    return n;
                }
            }
        }
        return null;
    }

    public boolean addNotice(Notice notice) throws SQLException {
        String getMaxIdSql = "SELECT MAX(noticeId) AS maxId FROM Notice";
        String insertSql = "INSERT INTO Notice(noticeId, title, content, publishTime, recipients, userId) VALUES (?, ?, ?, ?, ?, ?)";

        int newId = 1; // Ĭ  ֵ

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getMaxIdSql)) {

            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (!rs.wasNull() && maxId > 0) { //     null
                    newId = maxId + 1;
                }
            }

            //       Ϣ -   ӡ   в
            System.err.println("   ֪ͨ    : [" + newId + ", " + notice.getTitle() + ", " + notice.getContent() +
                    ", " + notice.getPublishTime() + ", " + notice.getRecipients() +
                    ", " + notice.getUserId() + "]");

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, newId);
                pstmt.setString(2, notice.getTitle());
                pstmt.setString(3, notice.getContent());
                pstmt.setTimestamp(4, notice.getPublishTime());
                pstmt.setString(5, notice.getRecipients());
                pstmt.setInt(6, notice.getUserId());

                int result = pstmt.executeUpdate();
                return result == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //      ϸ Ĵ     ־
            System.err.println("   ֪ͨʧ  : " + e.getMessage());
            System.err.println("SQL: " + insertSql);
            throw e; //      ׳  쳣
        }
    }

    //    ¹
    public boolean updateNotice(Notice notice) throws SQLException {
        String sql = "UPDATE Notice SET title=?, content=?, publishTime=?, recipients=?, userId=? WHERE noticeId=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getContent());
            pstmt.setTimestamp(3, notice.getPublishTime());
            pstmt.setString(4, notice.getRecipients());
            pstmt.setInt(5, notice.getUserId());
            pstmt.setInt(6, notice.getNoticeId());
            return pstmt.executeUpdate() == 1;
        }
    }

    // ɾ
    public boolean deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM Notice WHERE noticeId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noticeId);
            return pstmt.executeUpdate() == 1;
        }
    }
}

