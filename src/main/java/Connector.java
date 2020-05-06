import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

class Connector {

    static Connection establishConnection() throws SQLException {

        Properties connectionPropert = new Properties();
        connectionPropert.put("user", "root");
        connectionPropert.put("password", "xxxxxxxxxxx");

            return DriverManager.getConnection("jdbc:mysql://localhost/car_park?serverTimezone=UTC",
                    connectionPropert);
    }
}