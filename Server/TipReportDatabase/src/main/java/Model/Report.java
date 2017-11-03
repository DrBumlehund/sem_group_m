package Model;

import java.io.Serializable;
import java.util.ArrayList;

public class Report implements Serializable {
	private static final long serialVersionUID = 5972999490607843127L;
	private User user;
	private double latitude;
	private double longitude;
	private String Comment;
	private ArrayList<Byte[]> images = new ArrayList<Byte[]>();
	private int upvotes;
	private int downvotes;
	private ArrayList<String> userComments = new ArrayList<String>();
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
	public int getUpvotes() {
		return upvotes;
	}
	public void setUpvotes(int upvotes) {
		this.upvotes = upvotes;
	}
	public int getDownvotes() {
		return downvotes;
	}
	public void setDownvotes(int downvotes) {
		this.downvotes = downvotes;
	}
	public ArrayList<String> getUserComments() {
		return userComments;
	}
	public void setUserComments(ArrayList<String> userComments) {
		this.userComments = userComments;
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
	
}
