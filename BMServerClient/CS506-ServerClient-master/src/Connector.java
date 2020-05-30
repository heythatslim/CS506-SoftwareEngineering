import java.sql.*;
import java.util.Calendar;
import java.util.Date;

import org.mindrot.jbcrypt.BCrypt;



public class Connector {
	public Connection conn = null;
	private static int workload = 12; 

	/**
	 * Constructor for accessing the MySQL Database. The method takes in a query and retrieves the data 
	 * and returns back to the function above sending the client the data
	 * @param query
	 */
	public Connector() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Users?allowPublicKeyRetrieval=true&useSSL=false", "root", "Kangaroo1");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Will potentially create a user database from the users id that 
	 * contains all of their budget information. 
	 */
	public String createUserDB(String username) {
		String success = "Database Switched to BudgetInfo" + username; 
		String failure = "Unable to switch database"; 
		try {	
			Statement s = conn.createStatement(); 
			int result = s.executeUpdate("CREATE DATABASE IF NOT EXISTS BudgetInfo" + username);
			conn.setCatalog("BudgetInfo" + username);
			return success; 
		} catch (SQLException e) {
			e.printStackTrace();
			return failure; 
		}
	}

	/**
	 * Creates a table holding all of the users' login information 
	 * and personal information in the freshly created database for the user. 
	 * Also copies information from the users database into this table so the user 
	 * may access it.
	 */
	public String createUserTable(String username) {
		try {
			String copyUserInfo = "INSERT INTO `BudgetInfo" + username + "`.`users` SELECT * FROM Users.users WHERE username = " + "'" + username+ "';" ; 
			String query = "CREATE TABLE IF NOT EXISTS `Users` (\n" + 
					"  `userid` int(99) NOT NULL,\n" + 
					"  `username` varchar(26) NOT NULL,\n" + 
					"  `password` varchar(100) NOT NULL,\n" + 
					"  `securityquestion1` varchar(26) NOT NULL,\n" + 
					"  `securityquestion2` varchar(10) NOT NULL,\n" + 
					"  `salary` varchar(100) NOT NULL,\n" + 
					"  `birthday` varchar(100) NOT NULL,\n" + 
					"  `name` varchar(100) NOT NULL,\n" + 
					"   PRIMARY KEY (`username`)\n" +
					") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Table that will hold all of the user information';"; 

			Statement stmt = conn.createStatement();
			int resultSet = stmt.executeUpdate(query);
			if (userExists(username) == false) {
				stmt.executeUpdate(copyUserInfo); 
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "Could not create user table"; 
		}
		return "Could not create user table";
	}

	/**
	 * Inserts a new user into the Users table iff the user name hasn't already been taken. 
	 */
	public String insertUser(String username , String password, String sq1, String sq2, String salary, String birthday, String name) {
		try {
			boolean exists = userExists(username); 
			if (exists == false) { // Checks to see if the query returned that user name or not
				password = hashPW(password);
				Statement stmt = conn.createStatement(); 
				String insertUser = "INSERT INTO `Users`.`users` (`username`, `password`, `securityquestion1`, `securityquestion2`, `salary`, `birthday`, `name`) VALUES ('"+ username +"', '"+ password +"', '" + sq1+"', '"+ sq2+ "', '" + salary+ "', '" + birthday +"', '" + name + "');";
				int rs = stmt.executeUpdate(insertUser); //User is added to the database 
				return "User successfully inserted"; 
			} else {
				return ("username already exists!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ("username already exists!");
	}

	/**
	 * Deletes the given category in the database
	 * @param username
	 * @param categoryName
	 * @return
	 * 	String copyUserInfo = "INSERT INTO `BudgetInfo" + username + "`.`users` SELECT * FROM Users.users WHERE username = " + "'" + username+ "';" ; 

	 */
	public String deleteUser(String username) {
		String deletefromBI = "DROP DATABASE IF EXISTS BudgetInfo" + username + ";";
		String deletefromU = "DELETE FROM users WHERE (username = '" + username + "');"; 
		try {
			Statement stmt = conn.createStatement();
			int rs = stmt.executeUpdate(deletefromBI);
			conn.setCatalog("Users");
			stmt.executeUpdate(deletefromU);
			return "User Info Successfully Deleted";

		} catch (SQLException e) {
			e.printStackTrace();
			return "Unable to Delete User"; 
		} 
	}


	/**
	 * Checks to see if the user name exists in the table
	 * @return true: if the user name exists
	 * 		   false: user doesn't exist
	 */
	public boolean userExists(String username) {
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT username FROM Users WHERE username = " + "'" + username+ "'"; 
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (rs.getString(1).equals(username)) {
					return true; 
				}
			}
			return false; //Return false if it doesn't find the user in the database
		} catch (SQLException e) {
			e.printStackTrace();
			return false; 
		} 
	}

	public String getUserInfo(String username) {
		String info = ""; 
		String query = "SELECT * FROM `BudgetInfo" + username + "`.`users`;"; 
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			while(rs.next()) {
				for (int i = 2 ; i < 9; i++) {			
					info += rs.getString(i) + " "; 
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return info; 
	}

	/**
	 * Checks if a user name and password exist in the table. This method will be used for login 
	 * purposes. We will be storing the hash of the correct password in the table, so we will need
	 * to repeat the hash process and compare hashes.
	 * @return
	 */
	public String passwordCheck(String username, String password) {
		try {
			Statement stmt = conn.createStatement();
			boolean valid = checkHash(password, username); 
			if (valid) {
				return "true";  
			} else {
				return "Invalid Combination of Username and Password"; 
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return "Could not validate information"; 
		} 
	}
	/**
	 * 
	 * @param password
	 * @return
	 */
	public String hashPW(String password) {
		String storedHash = BCrypt.hashpw(password, BCrypt.gensalt(workload)); 
		return storedHash; 
	}

	public boolean checkHash(String password, String username) {
		String query = "SELECT password FROM Users WHERE username = '" + username + "';"; 
		Statement stmt;
		Boolean validate = false; 
		String hashedPW = ""; 
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				hashedPW = rs.getString(1); 
			}
			validate = BCrypt.checkpw(password, hashedPW); 
			return validate;
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return validate; 
	}

	/**
	 * Changes the user info with what was entered in the app
	 * @param name
	 * @param sq1
	 * @param sq2
	 * @param sq3
	 * @param birthday
	 * @return
	 */
	public String changeInfo(String username, String name, String sq1, String sq2, String salary, String birthday) {
		String changed = "Info Successfully Changed"; 
		String query = "UPDATE `BudgetInfo" + username + 
				"`.`users` SET name =" + "'" + name + "', birthday='" + 
				birthday + "', securityquestion1='" + sq1 + "', securityquestion2='" + 
				sq2 + "', salary = '" + salary + "' WHERE username=" +
				"'" + username+ "';" ; 
		try {
			Statement stmt = conn.createStatement();
			int rs = stmt.executeUpdate(query); 
			if (rs == 1) {
				conn.setCatalog("Users");
				String updateUsers = "UPDATE `Users`.`users` SET name = '" + name + "', securityquestion1= '" +
						sq1 +"', securityquestion2= '" + sq2 + "', salary='" + salary +
						"', birthday = '" + birthday +"' WHERE username='" + username +"';"; 
				stmt.executeUpdate(updateUsers); 
				conn.setCatalog("BudgetInfo" + username);
				return "Info Changed Successfully"; 

			} else {
				return "Unable to Change User Info"; 
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return changed;
	}

	/**
	 * Gets the salary for a user to 
	 * @param username
	 * @return
	 */
	public String getSalary(String username) {
		String query = "SELECT * FROM `BudgetInfo" + username + "`.`users`;"; 
		Statement stmt;
		String salary= ""; 
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				salary = rs.getString(6); 
			} 
			return salary; 
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return "0"; 
	}

	/**
	 * Will change an existing user's password, and will return false if the new password's
	 * hash is the same as the existing hash in our database
	 * @return
	 */
	public String changePassword(String username, String password) {	
		try {
			password = hashPW(password);
			String query = "UPDATE `BudgetInfo" + username + "`.`users` SET password = '" + password + "';"; 
			Statement stmt = conn.createStatement(); 
			int rs = stmt.executeUpdate(query); 
			if (rs == 1) {
				conn.setCatalog("Users");
				String updateUsers = "UPDATE `Users`.`users` SET password = '" + password + "' WHERE username='" + username +"';"; 
				stmt.executeUpdate(updateUsers); 
				conn.setCatalog("BudgetInfo" + username);
				return "Password Changed Successfully"; 
			} else {
				return "Unable to change password"; 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Could not change password"; 
	}

	/**
	 * Returns a list of the security questions from the user table
	 * @return
	 */
	public String getSecurityQuestions(String username) {
		String questions = ""; 
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM `Users`.`users` WHERE username = '" + username + "';"; 
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				//Gets all of the security question answers into a single string making it easy to parse. 
				questions += (rs.getString(4) + ";" + rs.getString(5) + ";");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return questions; 
	}

	/**
	 * Creates a table for a specific user of all their transactions
	 */
	public String createUserTransactionTable() {
		String query = "CREATE TABLE IF NOT EXISTS `Transaction` (\n" + 
				"  `orderId` int(10) NOT NULL AUTO_INCREMENT,\n" + 
				"  `type` varchar(25) NOT NULL,\n" + 
				"  `amount` varchar(100) NOT NULL,\n" + 
				"  `date` varchar(15) NOT NULL,\n" + 
				"  `month` varchar(15) NOT NULL,\n" + 
				"  `year` varchar(15) NOT NULL,\n" + 
				"  `category` varchar(15) NOT NULL,\n" + 
				"  `note` varchar(15) NOT NULL,\n" + 
				"	PRIMARY KEY(orderId)\n"+
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
		try {
			Statement stmt = conn.createStatement();
			int rs = stmt.executeUpdate(query); 
			return "User Transaction Table Created"; 
		} catch (SQLException e) {
			e.printStackTrace();
			return "Unable to create User Transaction Table"; 
		} 
	}

	/**
	 * Inserts a transaction into a specific user's transaction table
	 */
	public String insertTransaction(String username, String type, String amount, String date, String month, String year, String category, String note) {
		String insertTrans = "INSERT INTO `BudgetInfo" + username +"`.`transaction` ( `type`, `amount`, `date`, `month`,`year`,`category`,`note`) VALUES ('"+ type +"', '" + amount+"', '"+ date+ "', '" + month+ "', '" + year + "', '" + category + "', '" + note + "');";
		try {
			Statement stmt = conn.createStatement();
			int rs = stmt.executeUpdate(insertTrans); 
			if (rs == 1) {
				return "Transaction inserted";
			} else {
				return "Failed to Insert Transaction"; 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return "Could not insert transaction";
	}

	/**
	 * Gets the list of transactions that the user has in the database
	 */
	public String getTransactionList() {
		String transList = ""; 
		String query = "SELECT * FROM Transaction;"; 
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			while (rs.next()) {
				for (int i = 1 ; i <=8; i++) {
					if (i <=7) {
						transList += rs.getString(i) + " "; 
					} else if (i == 8) {
						transList += rs.getString(i) + ";"; 
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return transList; 
	}

	/**
	 * Deletes all of the transactions of a given category when the category is deleted. 
	 * @param username
	 * @param categoryName
	 * @return
	 */
	public String deleteTransaction(String username, String categoryName) {
		String query = "DELETE FROM `BudgetInfo" + username + "`.`Transaction` WHERE (category = '" + categoryName + "')";
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query); 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "Transaction Deleted" + categoryName; 
	}

	/**
	 * Creates a table for a specific user for all the categories that make
	 * up their budget
	 * @param username
	 */
	public String createUserCategoryTable(String username) {
		Statement stmt;
		String query = "CREATE TABLE IF NOT EXISTS `Category` (\n" + 
				"  `categoryName` varchar(25) NOT NULL, \n" + 
				"  `budget` varchar(25) NOT NULL,\n" +
				"  `month` varchar(25) NOT NULL,\n" +
				"  `year` varchar(25) NOT NULL,\n" +
				"	PRIMARY KEY(categoryName)\n"+
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"; 
		try {
			stmt = conn.createStatement();
			int rs = stmt.executeUpdate(query);
			//If table is successfully created, add the Default Categories to the database. 
			if (categoryExists("Food", username) == false || categoryExists("Groceries", username) == false || categoryExists("Clothes", username) == false) {
				insertCategory(username, "Food", "100"); 
				insertCategory(username, "Groceries", "200"); 
				insertCategory(username, "Clothes", "100"); 
			} else {
				return "User Table Query Executed"; 
			}
			//Insert the default categories into the category table		
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return "Unable to Create User Category Table";
	}

	/**
	 * Checks to make sure that the category isn't already in the database
	 * @param category
	 * @return
	 */
	public boolean categoryExists( String category, String username) {
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT categoryName FROM `BudgetInfo" + username + "`.`Category` WHERE categoryName = '" + category + "';"; 
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (rs.getString(1).equals(category)) {
					return true; 
				}
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false; 
		} 
	}

	/**
	 * Inserts a category into a specific user's category table
	 * @param username
	 * @param category
	 */
	public String insertCategory(String username, String categoryName, String budget) {
		String month = getMonth(); 
		String year = getYear(); 
		String insertCategory = "INSERT INTO `BudgetInfo" + username +"`.`Category` ( `categoryName`, `budget`, `month`, `year`) VALUES ('"+ categoryName +"' , '" + budget + "', '" + month + "', '" + year + "');";
		try {
			if (categoryExists(categoryName, username) == false) { //Category doesn't already exist
				Statement stmt = conn.createStatement();
				int rs = stmt.executeUpdate(insertCategory); 
				return "Category Successfully Added";
			} else {
				return  ("Category Already Exists. Pick a different name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return "Unable to Add Category";

	}

	/**
	 * Edits either the categories name, the catergories budget or both
	 */
	public String editCategory(String username, String categoryName, String budget, String oldCategoryName) {
		String editCategory = "UPDATE `BudgetInfo" + username + "`.`Category` SET categoryName = '" + categoryName + "', budget='" + budget+"' WHERE (`categoryName`= '" + oldCategoryName + "');"; 
		String updateTransactions = "UPDATE `BudgetInfo" + username + "`.`Transaction` SET category = '" + categoryName + "' WHERE (`category`= '" + oldCategoryName + "');";
		try {
			Statement stmt = conn.createStatement();
			int rs = stmt.executeUpdate(editCategory);
			if (rs == 1) {
				rs = stmt.executeUpdate(updateTransactions); 
				if(rs == 1) {
					return "Category Updated"; 
				} else {
					return "Unable to refactor transactions"; 
				}

			} else {
				return "Unable to update category"; 
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		return "Category Updated"; 
	}

	/**
	 * Gets array representation of the categories in a user's budget
	 */
	public String getCategory(String username) {
		String categories = ""; 
		String query = "SELECT * FROM `BudgetInfo" + username + "`.`Category`";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			while (rs.next()) {
				categories += rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) + ";"; 
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return categories; 
	}

	/**
	 * Deletes the given category in the database
	 * @param username
	 * @param categoryName
	 * @return
	 */
	public String deleteCategory(String username, String categoryName) {
		String deleteCategory = "DELETE FROM `BudgetInfo" + username +"`.`Category` WHERE (categoryName = '" + categoryName+"');";
		try {
			Statement stmt = conn.createStatement();
			int rs = stmt.executeUpdate(deleteCategory);
			if (rs == 1) {
				deleteTransaction(username, categoryName); 
				return "Category Successfully Deleted";
			} else {
				return "Unable to Delete Category"; 
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return "Unable to Delete Category"; 
		} 
	}

	/**
	 * Get year as a string to be used in the report page
	 * @return
	 */
	public String getYear() {
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);

		int year = cal.get(Calendar.YEAR);
		String yearString = String.valueOf(year);
		return yearString;
	}

	/**
	 * Get months as a string to be used in the report page
	 * @return
	 */
	public String getMonth(){
		String[] months = new String[]{"January", "February", "March", "April",
				"May", "June", "July", "August", "September", "October",
				"November", "December"};
		String monthString;
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);

		int month = cal.get(Calendar.MONTH);
		if (month == 0) {
			monthString = months[0];
		} else if (month == 1) {
			monthString = months[1];
		} else if (month == 2) {
			monthString = months[2];
		} else if (month == 3) {
			monthString = months[3];
		} else if (month == 4) {
			monthString = months[4];
		} else if (month == 5) {
			monthString = months[5];
		} else if (month == 6) {
			monthString = months[6];
		} else if (month == 7) {
			monthString = months[7];
		} else if (month == 8) {
			monthString = months[8];
		} else if (month == 9) {
			monthString = months[9];
		} else if (month == 10) {
			monthString = months[10];
		} else if (month == 11) {
			monthString = months[11];
		} else {
			return "Unable to retrieve month";
		}
		return monthString;
	}
}
