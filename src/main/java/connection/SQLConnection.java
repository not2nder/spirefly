package connection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection
{
    private Connection connection;
    public boolean connect(){
        try{
            String url = "jdbc:sqlite:C:/Spirefly/cfg/database.db";
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
    public boolean disconnect(){
        try{
            if (!this.connection.isClosed()){
                this.connection.close();
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
}
