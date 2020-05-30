import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;
public class Server
{
	//initialize socket and input stream
	private ServerSocket server = null;
	private BufferedReader in =  null;
	private PrintWriter out;
	// constructor with port

	public Server(int port)
	{
		// starts server and waits for a connection
		try
		{
			server = new ServerSocket(port);
			System.out.println("Server started");
			while (true) {
				Socket clientSocket = server.accept();
				System.out.println("Client accepted");
				// takes input from the client socket
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())),true);
				String line = "";
				String message = "";

				while ((line = in.readLine()) != null) {
					System.out.println(line);
					message+=line; 
					Connector conn = new Connector(); 

					if (message.contains("createuser;")) {
						String op = message.substring(11); 
						System.out.println(op);
						String[] userInfo = op.split(" "); 
						String result = conn.insertUser(userInfo[0], userInfo[1], userInfo[2], userInfo[3], userInfo[4], userInfo[5], userInfo[6]); 
						out.println(result);
						String db = conn.createUserDB(userInfo[0]); 
						conn.createUserTable(userInfo[0]);
						conn.createUserTransactionTable();
						conn.createUserCategoryTable(userInfo[0]); 
					} else if (message.contains("login;")) {
						String userInfo = message.substring(6);
						String[] unpw = userInfo.split(" "); 
						String username = unpw[0]; 
						System.out.println("username: " + username);
						String password = unpw[1]; 
						System.out.println("password: " + password);
						boolean exists = conn.userExists(username);
						if (conn.userExists(username)) {
							String validate = conn.passwordCheck(username, password);
							if (validate.equals("true")) {
								conn.createUserDB(username);
								out.println(validate); 
							} else {
								out.println(validate);
							}
						} else {
							out.println("Invalid Username/Password");
						}
					} else if (message.contains("gettransactions;")) {
						String[] parsed = message.split(";"); 
						String username = parsed[1]; 
						conn.createUserDB(username); 
						String transactions = conn.getTransactionList(); 
						out.println(transactions);
					} else if (message.contains("getcategories;")) {
						String[] parsed = message.split(";"); 
						String username = parsed[1]; 
						System.out.println(username);
						conn.createUserDB(username); 
						String categories = conn.getCategory(username);
						out.println(categories); 
					} else if (message.contains("inserttransaction;")) {
						String query = message.substring(18); 
						String[] parsed = query.split(" "); 
						String username = parsed[0]; 
						String type = parsed[1]; 
						String amount = parsed[2]; 
						String date = parsed[3]; 
						String month = parsed[4];
						String year = parsed[5]; 
						String category = parsed[6];
						String note = parsed[7];
						String result = conn.insertTransaction(username, type, amount, date, month, year, category, note); 
						out.println(result); 
					} else if (message.contains("insertcategories;")) {
						String query = message.substring(17); 
						String[] parsed = query.split(" "); 
						String username = parsed[0]; 
						String categoryName = parsed[1];
						String budget = parsed[2]; 
						String result = conn.insertCategory(username, categoryName, budget);
						System.out.println(result);
						out.println(result); 
					} else if (message.contains("deletecategories;")) {
						String query = message.substring(17);
						String[] parsed = query.split(" "); 
						String username = parsed[0];
						String categoryName = parsed[1]; 
						String response = conn.deleteCategory(username, categoryName); 
						out.println(response);
					} else if (message.contains("getuserinfo;")) {
						String[] parsed = message.split(";");
						String username = parsed[1]; 
						String result = conn.getUserInfo(username);
						out.println(result);
					} else if (message.contains("changeuserinfo;")) {
						String query = message.substring(15); 
						String[] parsed = query.split(" ");
						String username = parsed[0];
						String name = parsed[1]; 
						String sq1 = parsed[2]; 
						String sq2 = parsed[3];
						String sq3 = parsed[4];
						String birthday = parsed[5];
						String result = conn.changeInfo(username, name, sq1, sq2, sq3, birthday); 
						out.println(result); 
					} else if (message.contains("changepassword;")) {
						String query = message.substring(15); 
						String[] parsed = query.split(" "); 
						String username = parsed[0]; 
						String password = parsed[1]; 
						String result = conn.changePassword(username, password); 
						out.println(result); 
					} else if (message.contains("passwordcheck;")) {
						String query = message.substring(14);
						String[] parsed = query.split(" "); 
						String username = parsed[0];
						String password = parsed[1]; 
						String response = conn.passwordCheck(username, password);
						out.println(response); 
					} else if (message.contains("getsalary;")) {
						String[] query = message.split(";");
						String username = query[1];
						String result = conn.getSalary(username);
						System.out.println(result);
						out.println(result);			
					} else if (message.contains("updatecategory;")) {
						String query = message.substring(15);
						String[] parsed = query.split(" ");
						String username = parsed[0];
						String categoryName = parsed[1];
						String budget = parsed[2]; 
						String oldCategoryName = parsed[3]; 
						String result = conn.editCategory(username, categoryName, budget, oldCategoryName);
						System.out.println(result);
						out.println(result); 
					} else if (message.contains("getquestions;")) {
						String[] query = message.split(";"); 
						String username = query[1]; 
						String response = conn.getSecurityQuestions(username);
						out.println(response); 
					} else {
						out.println("Invalid Client Syntax.");
					}
				}
			}
		}
		catch(IOException i){
			i.printStackTrace(); 
		}
	}

	public static void main(String[] args) {
		Server server = new Server(6868);
	}
}