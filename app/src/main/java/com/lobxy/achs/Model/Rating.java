package com.lobxy.achs.Model;

public class Rating {

    public Rating() {
    }

    private String description;
    private long rating;

    public Rating(String description, long rating) {
        this.description = description;
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }
}
