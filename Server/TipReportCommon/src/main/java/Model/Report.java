package Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Report implements Serializable {
    private static final long serialVersionUID = 5972999490607843127L;
    private User user;
    private int id;
    private double latitude;
    private double longitude;
    private String Comment;
    private ArrayList<Byte[]> images = new ArrayList<Byte[]>();
    private ArrayList<Vote> votes;
    private ArrayList<UserComment> userComments = new ArrayList<UserComment>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public ArrayList<Byte[]> getImages() {
        return images;
    }

    public void setImages(ArrayList<Byte[]> images) {
        this.images = images;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Vote> getVotes() {
        return votes;
    }

    public void setVotes(ArrayList<Vote> votes) {
        this.votes = votes;
    }

    public ArrayList<UserComment> getUserComments() {
        return userComments;
    }

    public void setUserComments(ArrayList<UserComment> userComments) {
        this.userComments = userComments;
    }

}
