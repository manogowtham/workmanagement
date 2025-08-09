package com.banyan.workmanagement.dto;

public class WorkTimeDTO {
    private String inTime;
    private String outTime;

    public WorkTimeDTO() {
    }

    public WorkTimeDTO(String inTime, String outTime) {
        this.inTime = inTime;
        this.outTime = outTime;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }
}
