package connection;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class SQLConnection
{
    private Connection connection;
    public void connect() throws SQLException{
        String url = "jdbc:sqlite:./db/app_database.db";
        this.connection = DriverManager.getConnection(url);
    }
    public void disconnect(){
        try{
            if (this.connection != null && !this.connection.isClosed()){
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createDb(){
        try{
            connection = this.connection;
            Statement stmt  = connection.createStatement();
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS music(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT,
                        path TEXT,
                        is_favorite BIT
                    );
                    """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertMusic(String name, String path, boolean isFavorite){
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO music (name, path, is_favorite) VALUES (?, ?, ?)")
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, path);
            preparedStatement.setBoolean(3, isFavorite);
            preparedStatement.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteMusic(String path){
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM music WHERE path = ?"
        )) {
            preparedStatement.setString(1, path);
            preparedStatement.execute();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ArrayList<File> updateMusicList(ArrayList<File> songs){

        ArrayList<File> list = new ArrayList<>();
        String query = "SELECT path FROM music";
        try {
            try (Statement stmt = connection.createStatement();
                 ResultSet resultSet = stmt.executeQuery(query)){

                while (resultSet.next()){
                    File newFile = new File(resultSet.getString("path"));
                    if (!songs.contains(newFile)){
                        list.add(newFile);
                        songs.add(newFile);
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }
    public void refreshSongs(){
        
    }
}
