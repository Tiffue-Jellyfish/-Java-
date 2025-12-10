package model;

public class FileInfo {
    private int fileId;
    private String fileType;
    private String fileName;
    private String storagePath;
    private String recorder;
    private int userId;
    private Integer isdeleted; // 修改为 Integer 支持 null 值

    // 构造方法
    public FileInfo() {}

    public FileInfo(int fileId, String fileType, String fileName, String storagePath, String recorder, int userId, int isDeleted) {
        this.fileId = fileId;
        this.fileType = fileType;
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.recorder = recorder;
        this.userId = userId;
        this.isdeleted = isDeleted;
    }

    // Getter和Setter方法
    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
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

    public Integer getIsdeleted() { // 注意方法名是 getIsdeleted 不是 getIsDeleted
        return isdeleted;
    }

    public void setIsdeleted(Integer isdeleted) {
        this.isdeleted = isdeleted;
    }
}



