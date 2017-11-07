package Database;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import Model.Report;
import Model.User;
import Model.UserComment;
import Model.Vote;

public class DatabaseFacade {
	
	private static DatabaseFacade instance;
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private String DB_URL = "jdbc:mysql://localhost:3306/tip_report";
    private static final String USER = "simon";
    private static final String PASS = "123456789";
	
	public static DatabaseFacade getInstance() {
		if (instance == null) {
			instance = new DatabaseFacade();
		}
		
		return instance;
	}
	
	private DatabaseFacade () {
	    try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public User addUser(String username, String password) {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){
			String sql = "INSERT INTO user (username, password) VALUES (?, ?);";
			PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS );
			statement.setString(1, username);
			statement.setString(2, password);
			
			int affectedRows  = statement.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Create user failed, no rows affected.");
			}
			
			User user = new User();
			user.setUsername(username);
	        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                user.setId(generatedKeys.getInt(1));
	            }
	            else {
	                throw new SQLException("Create user failed, no ID obtained.");
	            }
	        }
			
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}

	public User getUser(String username, String password) {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){
			String sql = "SELECT * FROM user WHERE username = ? && password = ?;";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, password);
			
			ResultSet result  = statement.executeQuery();
			User user = new User();
			user.setUsername(username);
			
	        while (result.next()) {
	        	user.setId(result.getInt("id"));
	        }
	        
	        if (user.getId() == 0) {
	        	throw new WebException("Incorrect username or password.");
	        }
			
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}

	public ArrayList<Report> getReportCoordinates() {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){
			String sql = "SELECT id, longitude, latitude FROM report;";
			PreparedStatement statement = connection.prepareStatement(sql);
			ResultSet result  = statement.executeQuery();

			ArrayList<Report> reports = new ArrayList<Report>();
	        while (result.next()) {
	        	int id = result.getInt("id");
	        	double latitude = result.getInt("latitude");
	        	double longitude = result.getInt("longitude");
	        	
	        	Report report = new Report();
	        	report.setLongitude(longitude);
	        	report.setLatitude(latitude);
	        	report.setId(id);
	        	reports.add(report);
	        }
			
			return reports;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}

	public ArrayList<Report> getReports() {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){

		    CallableStatement cs = connection.prepareCall("CALL getReports()");
		    boolean isResultSet  = cs.execute();

			if (!isResultSet) {
			  throw new WebException("The first result is not a ResultSet.");
			} 

			// First result set will contain the reports
			ResultSet result = cs.getResultSet();
			ArrayList<Report> reports = new ArrayList<Report>();
	        while (result.next()) {
	        	int id = result.getInt("id");
	        	double latitude = result.getInt("latitude");
	        	double longitude = result.getInt("longitude");
	        	String comment = result.getString("comment");
	        	int userId = result.getInt("id_user");
	        	String username = result.getString("username");

	        	Report report = new Report();
	        	if (userId != 0) {
	        		User user = new User();
	        		user.setId(userId);
	        		user.setUsername(username);
	        		report.setUser(user);
	        	}
	        	
	        	report.setLongitude(longitude);
	        	report.setLatitude(latitude);
	        	report.setId(id);
	        	report.setComment(comment);
	        	reports.add(report);
	        }

			// Second result set will contain the comments
	        cs.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
	        result = cs.getResultSet();
	        while (result.next()) {
	        	int reportId = result.getInt("user_comment_report_id");
	        	int userId = result.getInt("user_comment_id");
	        	String username = result.getString("user_comment_name");
	        	String userComment = result.getString("user_comment");

	        	Report report = getReportWithId(reportId, reports);
	        	UserComment comment = new UserComment();
	        	comment.setComment(userComment);
	        	comment.setUserId(userId);
	        	comment.setUsername(username);
	        	
	        	report.getUserComments().add(comment);
	        }
	        
			// Third result set will contain the votes
	        cs.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
	        result = cs.getResultSet();
	        while (result.next()) {
	        	int reportId = result.getInt("user_vote_report_id");
	        	int userId = result.getInt("user_vote_id");
	        	String username = result.getString("user_vote_name");
	        	boolean userUpvote = result.getBoolean("user_upvote");

	        	Report report = getReportWithId(reportId, reports);
	        	Vote vote = new Vote();
	        	vote.setUpvote(userUpvote);
	        	vote.setUserId(userId);
	        	vote.setUsername(username);
	        	
	        	report.getVotes().add(vote);
	        }
	        
			// Forth result set will contain the images
	        cs.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
	        result = cs.getResultSet();
	        while (result.next()) {
	        	int reportId = result.getInt("id_report");
	        	String imgPath = result.getString("img_path");
	        	Report report = getReportWithId(reportId, reports);
	        	
		        // TODO: Read images from file system
	        	// report.getImages().add(image);
	        }
			
	        
			return reports;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}
	
	private Report getReportWithId(int id, ArrayList<Report> reports) {
		for (Report report : reports) {
			if (report.getId() == id) {
				return report;
			}
		}
		return null;
	}

	public Report addReport(double latitude, double longitude, String comment, int userId) {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){
		    CallableStatement cs = connection.prepareCall("{CALL addReport(?, ?, ?, ?, ?)}");

		    cs.setDouble(1, latitude);
		    cs.setDouble(2, longitude);
		    cs.setString(3, comment);
		    cs.setInt(4, userId);
		    cs.registerOutParameter(5, Types.INTEGER);

		    boolean isResultSet = cs.execute();
        	Report report = null;

			if (!isResultSet) {
			  throw new WebException("The first result is not a ResultSet.");
			} else {
				ResultSet result = cs.getResultSet();
		        while (result.next()) {
		        	int reportId = result.getInt("LID");
		        	report = new Report();
		        	User user = new User();
		        	user.setId(userId);
		        	report.setLatitude(latitude);
		        	report.setLongitude(longitude);
		        	report.setComment(comment);
		        	report.setUser(user);
		        	report.setId(reportId);
	        	}
			}
	        
			return report;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}

	public UserComment addComment(int reportId, String comment, int userId) {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){
			String sql = "INSERT INTO comment_report (id_user, id_report, comment) VALUES (?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, userId);
			statement.setInt(2, reportId);
			statement.setString(3, comment);
			int affectedRows  = statement.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Create comment failed, no rows affected.");
			}
			
			UserComment userComment = new UserComment();
			userComment.setComment(comment);
			userComment.setUserId(userId);
	        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	            	userComment.setId(generatedKeys.getInt(1));
	            }
	            else {
	                throw new SQLException("Create user failed, no ID obtained.");
	            }
	        }
	        
			return userComment;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}

	public Vote addVote(int reportId, boolean upvote, int userId) {
		try (Connection connection = DriverManager.getConnection(DB_URL,USER,PASS)){
			String sql = "INSERT INTO vote_report (id_user, id_report, upvote) VALUES (?, ?, ?)" + 
					" ON DUPLICATE KEY UPDATE (upvote=VALUES(upvote));";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			statement.setInt(2, reportId);
			statement.setBoolean(3, upvote);
			int affectedRows  = statement.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Create vote failed, no rows affected.");
			}
			
			Vote vote = new Vote();
			vote.setUpvote(upvote);
			vote.setUserId(userId);
	        
			return vote;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}
}
 