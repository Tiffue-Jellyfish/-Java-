package service;

import dao.MeetingDao;
import model.Meeting;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class MeetingService {
    private MeetingDao meetingDao = new MeetingDao();

    public int addMeeting(Meeting meeting) {
        try {
            return meetingDao.addMeeting(meeting);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;  // 返回错误码，表示失败
        }
    }

    public boolean updateMeeting(Meeting meeting) {
        try {
            return meetingDao.updateMeeting(meeting);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMeeting(int meetingId) {
        try {
            return meetingDao.deleteMeeting(meetingId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Meeting> getAllMeetings() {
        try {
            return meetingDao.getAllMeetings();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Meeting> searchMeetings(Date meetingTime, String content, String location, String participant, String recorder) {
        try {
            return meetingDao.queryMeetings(meetingTime, content, location, participant, recorder);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Meeting getMeetingById(int meetingId) {
        try {
            return meetingDao.getMeetingById(meetingId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

