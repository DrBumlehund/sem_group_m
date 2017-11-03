package Database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Model.User;

public class DatabaseFacade {
	
	private static DatabaseFacade instance;
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private String DB_URL = "jdbc:mysql://localhost:3306/asset_management";
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
			connection.setAutoCommit(false);
			String sql = "INSERT INTO User (username, password) VALUES (?, ?);";
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
			connection.setAutoCommit(false);
			String sql = "SELECT * FROM User WHERE username = ? && password = ?;";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, password);
			
			ResultSet result  = statement.executeQuery();
			User user = new User();
			user.setUsername(username);
			
	        while (result.next()) {
	        	user.setId(result.getInt("id"));
	        }
			
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebException(e);
		}
	}
}
 