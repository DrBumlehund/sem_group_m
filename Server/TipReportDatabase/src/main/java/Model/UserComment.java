package Model;

import java.io.Serializable;

public class UserComment implements Serializable {
	private static final long serialVersionUID = -566997044370910024L;
	private int userId;
	private String username;
	private String comment;
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
