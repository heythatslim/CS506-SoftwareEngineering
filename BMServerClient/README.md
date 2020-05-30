# Budget Management Application: Server and Connector README

This is the Server and Connector code for UW BadgerBudget's BudgetManagement Application. It allows for the java socket server to be established as well as allows for the Connector to establish a proper connection to the database. There are just a few things that arre required to do to get the Code to work in sync with our application. 
  
  1. Since the normal app runs on Android Studio and we need to send messages seperately, what our group decided to do was to   put the Server and Connector classes into a different IDE, Eclipse. 
  
  2. Once you make a project we need to add both the MySQL Driver as well as BCrypt, which is used for a more secure login by 
  hashing user passwords that are to be stored in the database. While the processes are outlined in the README for the rest of 
  the Application, I will outline them here again just so you don't have to flip back and forth. 
  
  3. The other Github repo for the rest of the app can be found here: https://github.com/AliHZaidi/BudgetManagementApplication
 
  4. First, we'll download the mysql driver, which can be found at: https://dev.mysql.com/downloads/connector/j/5.1.html
  
  5. Afterwards, we'll download the JBCrpyt jar, which can be found at: 
  https://jar-download.com/artifacts/org.mindrot/jbcrypt/0.4/source-code
    a.) The filenames that are going into Eclipse for both of the downloaded .zip files are the following: 
        Mysql-connector-java-5.1.48-bin.jar and jbcrypt-0.4.jar
  6. After we have a project in Eclipse right click on your project go to ‘Build Path’ and click ‘Configure Build Path’. Click      on ‘Classpath’ to highlight it and then click on ‘Add External JARS…’ add both of the files specified above as References      Libraries
  7. To verify that everything worked, we should have the two filenames that were specified above in Referenced Libraries          after Apply is clicked when both JARs are added. 
  
  
  
 
