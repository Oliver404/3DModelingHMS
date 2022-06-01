package com.oliverbotello.a3dmodelinghms.model;

public class ItemEnt {
    public static final int STATUS_WAITING_TASK_ID = -10;
    public static final int STATUS_UPLOAD_FAIL = -4;
    public static final int STATUS_UPLOAD_PAUSE = -1;
    public static final int STATUS_UPLOADING = 0;
    public static final int STATUS_UPLOAD_SUCCESS = 1;
    public static final int STATUS_MODELING = 2;
    public static final int STATUS_MODEL_SUCCESS = 3;
    public static final int STATUS_MODEL_FAIL = 4;
    public static final int TYPE_TEXTURE = 100;
    public static final int TYPE_MODEL = 200;

    private String taskID;
    private String path;
    private int status;
    private int type;
    private float uploadProgress;

    public ItemEnt(String data) {
        fromString(data);
    }

    public ItemEnt(String taskID, String path, int status, int type) {
        this.taskID = taskID;
        this.path = path;
        this.status = status;
        this.type = type;
        this.uploadProgress = 0.0f;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return taskID + "|" + path + "|" + status + "|" + type;
    }

    public void fromString(String data) {
        String[] dataFields = data.split("[|]");
        taskID = dataFields[0];
        path = dataFields[1];
        status = Integer.parseInt(dataFields[2]);
        type = Integer.parseInt(dataFields[3]);
    }
}
