package model;

import java.sql.Date;

public class Meeting {
    private int meetingId;
    private Date meetingTime;
    private String content;
    private String location;
    private String participants;
    private String recorder;
    private int userId;

    public Meeting() {}

    public Meeting(int meetingId, Date meetingTime, String content, String location, String participants, String recorder, int userId) {
        this.meetingId = meetingId;
        this.meetingTime = meetingTime;
        this.content = content;
        this.location = location;
        this.participants = participants;
        this.recorder = recorder;
        this.userId = userId;
    }

    // Getters and Setters
    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public Date getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(Date meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getRecorder() {
        return recorder;
    }

    public void setRecorder(String recorder) {
        this.recorder = recorder;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}