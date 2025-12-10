package model;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Attendance {
    private int attendanceId;
    private int userId;
    private Date startTime;
    private Date endTime;
    private String status;
    private long workingMinutes;
    private String workingHours;



    // 在构造函数中生成ID
    public Attendance() {
        // 不再生成ID
    }
    private static final Time STANDARD_START = Time.valueOf("09:00:00");
    private static final Time STANDARD_END = Time.valueOf("18:00:00");

    public void calculateStatus() {
        if (this.startTime == null || this.endTime == null) {
            this.status = "缺勤";
            return;
        }

        Time start = new Time(this.startTime.getTime());
        Time end = new Time(this.endTime.getTime());

        boolean isLate = start.after(STANDARD_START);
        boolean isEarlyLeave = end.before(STANDARD_END);

        StringBuilder statusBuilder = new StringBuilder();

        // 计算迟到分钟数
        if (isLate) {
            long lateMinutes = (start.getTime() - STANDARD_START.getTime()) / (1000 * 60);
            statusBuilder.append("迟到").append(lateMinutes).append("分钟");
        }

        // 计算早退分钟数
        if (isEarlyLeave) {
            if (isLate) {
                statusBuilder.append("且");
            }
            long earlyMinutes = (STANDARD_END.getTime() - end.getTime()) / (1000 * 60);
            statusBuilder.append("早退").append(earlyMinutes).append("分钟");
        }

        // 完全正常
        if (!isLate && !isEarlyLeave) {
            statusBuilder.append("正常");
        }

        this.status = statusBuilder.toString();
    }



    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public Attendance(int attendanceId, int userId, Date startTime, Date endTime, String status) {
        this.attendanceId = attendanceId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        calculateWorkingTime();
    }

    public void calculateWorkingTime() {
        if (startTime == null) {
            this.status = "缺勤";
            this.workingMinutes = 0;
            return;
        }

        // 设置标准工作时间
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);

        // 标准上班时间(9:00)
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date standardStartTime = cal.getTime();

        // 标准下班时间(18:00)
        cal.set(Calendar.HOUR_OF_DAY, 18);
        Date standardEndTime = cal.getTime();

        // 判断迟到
        long lateMinutes = TimeUnit.MILLISECONDS.toMinutes(
                startTime.getTime() - standardStartTime.getTime());

        // 判断早退
        long earlyLeaveMinutes = 0;
        if (endTime != null) {
            earlyLeaveMinutes = TimeUnit.MILLISECONDS.toMinutes(
                    standardEndTime.getTime() - endTime.getTime());
        }

        // 计算实际工作时间
        if (endTime != null) {
            long diffInMillis = endTime.getTime() - startTime.getTime();
            this.workingMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        } else {
            this.workingMinutes = 0;
        }

        // 判断状态 - 修正后的逻辑
        if (startTime.after(standardStartTime)) {
            // 迟到的情况
            if (endTime == null) {
                this.status = String.format("迟到%d分钟,缺勤下午", lateMinutes);
            } else if (endTime.before(standardEndTime)) {
                this.status = String.format("迟到%d分钟,早退%d分钟", lateMinutes, earlyLeaveMinutes);
            } else {
                this.status = String.format("迟到%d分钟", lateMinutes);
            }
        } else {
            // 没有迟到的情况
            if (endTime == null) {
                this.status = "缺勤下午";
            } else if (endTime.before(standardEndTime)) {
                this.status = String.format("早退%d分钟", earlyLeaveMinutes);
            } else {
                this.status = "正常";
            }
        }

        // 特殊情况处理
        if (workingMinutes <= 0) {
            this.status = "缺勤";
        }
    }

    // Getter 和 Setter 方法
    public int getId() {
        return attendanceId;
    }

    public void setId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        calculateWorkingTime();
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        calculateWorkingTime();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getWorkingMinutes() {
        return workingMinutes;
    }

    public String getWorkingHours() {
        return String.format("%d小时%d分钟", workingMinutes / 60, workingMinutes % 60);
    }
}
