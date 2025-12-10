package model;

public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private java.sql.Timestamp publishTime; // 改为Timestamp更适合DATETIME
    private String recipients;
    private int userId;

    public Notice() {}

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public java.sql.Timestamp getPublishTime() { return publishTime; }
    public void setPublishTime(java.sql.Timestamp publishTime) { this.publishTime = publishTime; }

    public String getRecipients() { return recipients; }
    public void setRecipients(String recipients) { this.recipients = recipients; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}

