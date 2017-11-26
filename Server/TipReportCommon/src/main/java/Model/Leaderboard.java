package Model;

import java.io.Serializable;

public class Leaderboard implements Serializable {
    private static final long serialVersionUID = 3049108453681867681L;
    private User[] topList;

    public User[] getTopList() {
        return topList;
    }

    public void setTopList(User[] topList) {
        this.topList = topList;
    }
}
