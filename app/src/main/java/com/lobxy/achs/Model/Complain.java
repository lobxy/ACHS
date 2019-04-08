package com.lobxy.achs.Model;

public class Complain {

    private String type, name, address, complaintID, description, email, contact, site,
            happyCode, visitTime, complaintInitTime, completionStatus, supervisorAssigned,
            complaintCompletionTime, userId, imageUrl;

    public Complain() {
    }

    public Complain(String imageUrl, String userId, String complaintID, String name, String email, String address, String contact, String site, String type, String description,
                    String visitTime, String complaintInitTime, String happyCode, String completionStatus,
                    String supervisorAssigned, String complaintCompletionTime) {
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.complaintID = complaintID;
        this.name = name;
        this.email = email;
        this.address = address;
        this.contact = contact;
        this.site = site;
        this.type = type;
        this.description = description;
        this.visitTime = visitTime;
        this.complaintInitTime = complaintInitTime;
        this.happyCode = happyCode;
        this.completionStatus = completionStatus;
        this.supervisorAssigned = supervisorAssigned;
        this.complaintCompletionTime = complaintCompletionTime;

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getSupervisorAssigned() {
        return supervisorAssigned;
    }

    public void setSupervisorAssigned(String supervisorAssigned) {
        this.supervisorAssigned = supervisorAssigned;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public String getComplaintID() {
        return complaintID;
    }

    public void setComplaintID(String id) {
        this.complaintID = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
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


}