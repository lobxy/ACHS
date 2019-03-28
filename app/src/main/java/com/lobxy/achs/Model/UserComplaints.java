package com.lobxy.achs.Model;

public class UserComplaints {


    public UserComplaints() {
    }

    private String type, complaintId, happyCode, complaintInitTime, completionStatus, complaintCompletionTime, supervisorId, supervisorName;

    public UserComplaints(String type, String complaintId, String happyCode, String complaintInitTime, String completionStatus, String complaintCompletionTime, String supervisorId, String supervisorName) {
        this.type = type;
        this.complaintId = complaintId;
        this.happyCode = happyCode;
        this.complaintInitTime = complaintInitTime;
        this.completionStatus = completionStatus;
        this.complaintCompletionTime = complaintCompletionTime;
        this.supervisorId = supervisorId;
        this.supervisorName = supervisorName;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getComplaintCompletionTime() {
        return complaintCompletionTime;
    }

    public void setComplaintCompletionTime(String complaintCompletionTime) {
        this.complaintCompletionTime = complaintCompletionTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getHappyCode() {
        return happyCode;
    }

    public void setHappyCode(String happyCode) {
        this.happyCode = happyCode;
    }

    public String getComplaintInitTime() {
        return complaintInitTime;
    }

    public void setComplaintInitTime(String complaintInitTime) {
        this.complaintInitTime = complaintInitTime;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }
}
