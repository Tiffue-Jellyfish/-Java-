package service;

import dao.NoticeDao;
import model.Notice;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class NoticeService {

    private NoticeDao noticeDao = new NoticeDao();

    public String getSystemNoticeContent() {
        try {
            // 获取所有系统通知（按发布时间倒序）
            List<Notice> systemNotices = noticeDao.getSystemNotices();
            if (systemNotices != null && !systemNotices.isEmpty()) {
                // 返回最新的系统通知内容
                return systemNotices.get(0).getContent();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "欢迎使用办公室管理系统！"; // 默认消息
    }

    // 新增：保存系统通知
    public boolean saveSystemNotice(String content) {
        try {
            // 获取所有系统通知
            List<Notice> systemNotices = noticeDao.getSystemNotices();
            Notice notice;

            if (systemNotices != null && !systemNotices.isEmpty()) {
                // 更新最新的系统通知
                notice = systemNotices.get(0);
                notice.setContent(content);
                notice.setPublishTime(new java.sql.Timestamp(System.currentTimeMillis()));
                return noticeDao.updateNotice(notice);
            } else {
                // 创建新的系统通知
                notice = new Notice();
                notice.setTitle("系统通知");
                notice.setContent(content);
                notice.setPublishTime(new java.sql.Timestamp(System.currentTimeMillis()));
                notice.setRecipients("ALL");
                notice.setUserId(1); // 系统管理员ID
                return noticeDao.addNotice(notice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 支持标题关键字及时间范围查询
     * @param title 标题关键字（可为null或空）
     * @param start 起始日期（可为null）
     * @param end   结束日期（可为null）
     * @return 符合条件的公告列表
     */
    public List<Notice> searchNotices(String title, Date start, Date end, String recipients) {
        try {
            java.sql.Date sqlStart = null;
            java.sql.Date sqlEnd = null;
            if (start != null) {
                sqlStart = new java.sql.Date(start.getTime());
            }
            if (end != null) {
                sqlEnd = new java.sql.Date(end.getTime());
            }
            // 传递接收人参数到DAO
            return noticeDao.queryNotices(title, sqlStart, sqlEnd, recipients);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Notice getNoticeById(int noticeId) {
        try {
            return noticeDao.getNoticeById(noticeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addNotice(Notice notice) {
        try {
            return noticeDao.addNotice(notice);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateNotice(Notice notice) {
        try {
            return noticeDao.updateNotice(notice);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNotice(int noticeId) {
        try {
            return noticeDao.deleteNotice(noticeId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

