package com.lobxy.achs.Model;

public class Supervisor {
    public Supervisor() {
    }

    private String name, email, contact, site, uid, password;
    long count;

    public Supervisor(String name, String email, String contact, String site, String uid, String password, long count) {
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.site = site;
        this.uid = uid;
        this.password = password;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
