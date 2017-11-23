package Model;

import java.io.Serializable;

public class Vote implements Serializable {
    private static final long serialVersionUID = 3705062846965130495L;
    private int userId;
    private String username;
    private boolean upvote;

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

    public boolean isUpvote() {
        return upvote;
    }

    public void setUpvote(boolean upvote) {
        this.upvote = upvote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vote vote = (Vote) o;

        return userId == vote.userId;
    }

    @Override
    public int hashCode() {
        return userId;
    }
}
