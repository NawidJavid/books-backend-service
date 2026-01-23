package com.nawidali.sql_labb_2;

import java.sql.*;

/* Create a Java project in IntelliJ, set "Build tool" to Maven. To download a JDBC connector/driver:
   In the pom.xml file add th lines below (there might already be some dependencies).
   Then click the "M" symbol in the upper right corner of the pom-file to update the dependencies.
       <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>9.1.0</version>
        </dependency>
    </dependencies>

    if you are compiling and executing in a terminal you have to download a connector separately from
    http://www.mysql.com/products/connector/
    Add a classpath when running the application:
    > java -classpath .;C:\path_to_your_driver\mysql-connector-java-8/9.x.xx.jar
 */
public class JdbcTest {

    public static void main(String[] args) throws SQLException {

        if (args.length != 2) {
            System.out.println("Usage: java JdbcTest <username> <password>");
            System.exit(0);
        }

        String user = args[0]; // username (or use hardcoded values)
        String pwd = args[1]; // password
        System.out.println(user + ", *********");
        String database = "library"; // the name of the specific database
        String serverUrl = "jdbc:mysql://localhost:3306/" + database
                + "?UseClientEnc=UTF8";

        Connection conn = null;
        try {
            // open a connection
            conn = DriverManager.getConnection(serverUrl, user, pwd);
            System.out.println("Connected!");
            // execute a query
            executeQuery(conn, "SELECT 1");
        } finally {
            if (conn != null) conn.close();
            System.out.println("Connection closed.");
        }
    }

    public static void executeQuery(Connection con, String queryStr)
            throws SQLException {

        // try-with-resources
        try (Statement statement = con.createStatement()) {
            // execute the query
            ResultSet rs = statement.executeQuery(queryStr);
            // get the attribute names
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();
            for (int c = 1; c <= colCount; c++) {
                System.out.print(metaData.getColumnName(c) + "\t");
            }
            System.out.println();

            // for each tuple, get the attribute values
            while (rs.next()) {
                int eno = rs.getInt(1);
                String name = rs.getString(2);
                Date dob = rs.getDate(3);
                float salary = rs.getFloat(4);
                int dno = rs.getInt(5);
                // NB! In a "real" application this data (each tupel) would be converted into an object
                System.out.println("" + eno + ' ' + name + '\t' + dob + '\t' + salary + '\t' + dno);
            }
        } // at this point, the statement will automatically be closed (try-with-resources)
    }
}
