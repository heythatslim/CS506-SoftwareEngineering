import static org.junit.jupiter.api.Assertions.*;
import org.junit.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConnectorTest {

	Connector conn = new Connector();
	String username = "JUnit"; 
	String password = "password"; 
	String sq1 = "answer1"; 
	String sq2 = "answer2"; 
	String salary = "100"; 
	String birthday = "2/31/20"; 
	String name = "JUnitTest"; 
	String month = conn.getMonth(); 
	String year = conn.getYear(); 

	@Test
	/**
	 * Tests that the connection doesn't throw an exception and successfully connects
	 * to the MySQL database specified. 
	 */
	void testA() {
		try {
			Connection conn; 
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Users?allowPublicKeyRetrieval=true&useSSL=false", "root", "Kangaroo1");
			assertEquals(conn != null, true); 
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	@Test 
	/**
	 * Checks that the right value is returned when the user exists in the database
	 */
	void testB() {
		String actual = conn.insertUser(username, password, sq1, sq2, salary, birthday, name); 
		String expected = "User successfully inserted";
		//Create everything for the user
		conn.createUserDB(username); 
		conn.createUserTable(username);
		conn.createUserTransactionTable();
		conn.createUserCategoryTable(username);
		boolean exists = conn.userExists(username); 
		assertTrue(exists); 
	}

	@Test 
	/**
	 * Checks that the right value is returned when the user exists in the database
	 */
	void testC() {
		boolean exists = conn.userExists(username); 
		assertTrue(exists); 
	}


	/**
	 * Checks that the right value is returned when the user does not exist in the database
	 */
	@Test
	void testD() {
		String username = "NonExistingUser"; 
		boolean exists = conn.userExists(username); 
		assertFalse(exists); 
	}

	@Test
	/**
	 * Tests to make sure that a user name cannot be reused
	 */
	void testE() {
		String expected = "username already exists!";
		String insertDuplicateUserAttempt = conn.insertUser(username, password,sq1, sq2, salary, birthday, name); 
		assertEquals(insertDuplicateUserAttempt, expected ); 
	}

	@Test
	/**
	 * Tests to make sure that the username and password line up to simulate a log in. 
	 */
	void testF() {
		String actual = conn.passwordCheck(username, password);
		String expected = "true"; 
		assertEquals(actual, expected); 
	}

	@Test
	/**
	 * Tests to make sure that the connector knows when the password is invalid. 
	 */
	void testG() {
		String invalidPassword = "invalidpassword";
		String actual = conn.passwordCheck(username, invalidPassword);
		String expected = "Invalid Combination of Username and Password"; 
		assertEquals(actual, expected); 
	}

	@Test
	/**
	 * Tests to see whether or not the user info is gotten correctly
	 */
	void testH() {
		String actualString = ""; 
		String actual = conn.getUserInfo(username);
		String[] parsed = actual.split(" ");
		for (int i = 0; i < parsed.length; i++) {
			if (i != 1) {
				actualString += parsed[i] + " "; 
			} 
		}
		String expected = username + " " + sq1 + " " + sq2 + " " + salary + " " + birthday + " " + name + " "; 
		assertEquals(actualString, expected);
	}



	@Test
	/**
	 * Checks whether the correct salary is fetched from the database
	 */
	void testI() {
		String actual = conn.getSalary(username); 
		String expected = salary; 
		assertEquals(actual, expected); 
	}


	@Test
	/**
	 * Checks to see whether or not the users security questions are properly retrieved from the database. 
	 */
	void testJ() {
		String expected = sq1 +";" + sq2 + ";"; 
		String actual = conn.getSecurityQuestions(username); 
		assertEquals(actual, expected);
	}

	@Test
	/**
	 * Tests whether or not user info is successfully changed
	 */
	void testK() {
		String sq1 = "changed"; 
		String sq2 = "changed"; 
		String salary = "99999999"; 
		String birthday = "1/1/1111"; 
		String name = "changed"; 
		String result = conn.changeInfo(username, name, sq1, sq2, salary, birthday);
		String expected = "Info Changed Successfully"; 
		assertEquals(result, expected); 

	}

	@Test
	/**
	 * Checks whether the password is properly changed in the database per the users request
	 */
	void testL() {
		String newPass = "newpassword"; 
		String result = conn.changePassword(username, newPass); 
		String expected = "Password Changed Successfully"; 
		assertEquals(expected, result); 
	}

	@Test
	/**
	 * Checks to see whether the category is inserted properly
	 */
	void testM() {
		String categoryName = "Category";
		String budget = "500"; 
		String actual = conn.insertCategory(username, categoryName, budget); 
		String result = "Category Successfully Added";
		assertEquals(actual, result); 
	}

	@Test
	/**
	 * Tests whether insert transaction is successful. 
	 */
	void testN() {
		String type = "Income"; 
		String amount = "0"; 
		String date = "date"; 
		String category = "Category"; 
		String note = "test";  
		String result = conn.insertTransaction(username, type, amount, date, month, year, category, note);
		String expected = "Transaction inserted"; 
		assertEquals(result, expected);
	}
	@Test
	/**
	 * Checks to see whether or not the category is changed properly
	 */
	void testO() {
		String categoryName = "Test"; 
		String budget = "500"; 
		String oldCategoryName = "Category"; 
		String response = conn.editCategory(username, categoryName, budget, oldCategoryName); 
		String expected = "Category Updated";
		assertEquals(response, expected); 
	}

	@Test
	/**
	 * Test to see whether or not the transaction list is gotten properly after manually adding a transaction
	 */
	void testP() {
		String un = "JUnit"; 
		String type = "Income";
		String amount = "0"; 
		String date = "date";
		String category = "Test"; 
		String note = "test"; 
		conn.createUserDB(un); 
		conn.createUserTransactionTable();
		//Expected format for parsing
		String expected = type + " " + amount + " "+ date + " " + month + " " + year + " " + category + " " + note+ ";"; 
		String actual = conn.getTransactionList(); 
		String[] actualList = actual.split(" ");
		String total = "";
		for (int i = 1; i < actualList.length; i ++) {
			total+= actualList[i] + " "; 
		}
		total = total.trim(); 
		assertEquals(total,expected ); 
	}


	@Test
	/**
	 * Tests whether the user is able to properly delete transactions
	 */
	void testQ() {
		String categoryName = "Test"; 
		String result = conn.deleteCategory(username, categoryName); 
		String expected = "Category Successfully Deleted"; 
		assertEquals(result, expected); 
	}


	@Test
	/**
	 * Test to see whether or not the category list is gotten properly
	 */
	void testR() {
		String un = "JUnit"; 
		conn.createUserDB(un); 
		conn.createUserCategoryTable(un);
		String clothes = "Clothes";
		String clothesBudget = "100"; 
		String food = "Food"; 
		String foodBudget = "100"; 
		String groceries = "Groceries"; 
		String groceriesBudget = "200"; 
		String month = "May"; 
		String year = "2020"; 
		String expected = clothes + " " + clothesBudget + " " + month + " " + year + ";"+food + " " + foodBudget + " " + month + " " + year + ";" +groceries + " " + groceriesBudget + " " + month + " " + year + ";"; 
		String actual = conn.getCategory(un); 
		assertEquals(expected, actual); 
	}
	@Test
	/**
	 * Tests whether or not you can add the same category twice
	 */
	void testS() {
		boolean actual = conn.categoryExists("Food", username);
		assertTrue(actual); 

	}
	@Test
	/**
	 * Reset userinfo that was changed about
	 */
	void testT() {
		String sq1 = "answer1"; 
		String sq2 = "answer2"; 
		String salary = "100"; 
		String birthday = "2/31/20"; 
		String name = "JUnitTest"; 
		String result = conn.changeInfo(username, name, sq1, sq2, salary, birthday);
		String expected = "Info Changed Successfully"; 
		assertEquals(result, expected); 

	}
	@Test
	/**
	 * Reset password that was changed about
	 */
	void testU() {
		String newPass = "password"; 
		String result = conn.changePassword(username, newPass); 
		String expected = "Password Changed Successfully"; 
		assertEquals(expected, result); 
	}

	@Test
	/**
	 * Delete the user the was inserted into both dbs
	 */
	void testV() {
		String result = conn.deleteUser(username); 
		String expected = "User Info Successfully Deleted"; 
		assertEquals(expected, result); 
	}

}
