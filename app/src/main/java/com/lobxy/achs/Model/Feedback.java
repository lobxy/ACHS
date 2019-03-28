package com.lobxy.achs.Model;

public class Feedback {

    public Feedback() {
    }

    String userId, complaintId, feedback, supervisorId, supervisorName;
    private long rating;

    public Feedback(String userId, String complaintId, String feedback, long rating, String supervisorId, String supervisorName) {
        this.userId = userId;
        this.complaintId = complaintId;
        this.feedback = feedback;
        this.rating = rating;
        this.supervisorId = supervisorId;
        this.supervisorName = supervisorName;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }
}
