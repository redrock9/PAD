package nl.amsta09.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import nl.amsta09.model.Media;
import nl.amsta09.model.Photo;

public class SqlConnector {

    private static Connection connection;

    public SqlConnector() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://supersudonator.filmencode.nl/photoslider?"
                    + "user=PAD&password=fissafissaheey123&useUnicode=true&useJDBCCompliantTimezoneShift=true"
                    + "&useLegacyDatetimeCode=false&serverTimezone=UTC");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /*
     * Insert een theme naar de database
     */
    public void insertTheme(String themeName) throws SQLException, ClassNotFoundException {
        System.out.println("\n----------insertTheme commencing----------");

        Statement addThemeStatement = connection.createStatement();
        //Insert de gegevens in de database met een unieke ID
        String on = "true";
        String sql =  String.format("insert into theme (name, on) VALUES ('%s', '%s')", themeName, on);

        addThemeStatement.execute(sql);

    }

    /*
     * Verwijdert het gekozen Thema uit de database
     */
    public void deleteTheme(int idTheme) throws SQLException, ClassNotFoundException {
        Statement deleteThemeStatement = connection.createStatement();
        //Verwijdert de thema uit de tabel Thema
        String sql = ("DELETE FROM Thema"
                + "WHERE id = " + idTheme);

        deleteThemeStatement.execute(sql);
    }

    /*
     * Insert een media file naar de database
     */
    public void insertMedia(String filePath, String Media_Name) throws SQLException, ClassNotFoundException {

//        Class.forName("com.mysql.jdbc.Driver");
//        connection = DriverManager.getConnection("jdbc:mysql://localhost/photoslider", "root", "Aapjes-14");
        filePath = filePath.replace("\\", "\\\\\\\\");

        Statement addMediaStatement = connection.createStatement();
        //Insert een media file in de database
        String sql = ("INSERT INTO Media (name, filePath) VALUES ('" + Media_Name + "','" + filePath + "')");

        addMediaStatement.execute(sql);

        insertIntoMediaType(Media_Name);
    }

    /**
     * Voeg een media bestand toe aan de database.
     *
     * @param media
     */
    public void insertMedia(Media media) throws SQLException {
        executeUpdate(String.format("INSERT INTO media (name, filePath) VALUES ('%s','%s')", media.getName(), media.getRelativePath()));
        insertIntoMediaType(media.getName());
    }

    /*
    Verwijdert de gekozen Media uit de database
     */
    public void deleteMedia(URL MediafilePath) throws SQLException, ClassNotFoundException {

        Statement deleteMediaStatement = connection.createStatement();

        String sql = ("DELETE FROM Media"
                + "WHERE filePath = " + "'" + MediafilePath + "'");

        deleteMediaStatement.execute(sql);
    }

    /*
     * Activeert het gekozen media file
     */
    public void activateMedia(String Media_Name) throws SQLException {

        Statement activateMediaStatement = connection.createStatement();
        //Activeert de gekozen media file
        String sql = ("UPDATE Media"
                + "SET active = 'true'"
                + "WHERE filePath ='" + Media_Name + "'");

        activateMediaStatement.execute(sql);
    }

    /*
     * Deactiveerd het gekozen media file
     */
    public void disableMedia(String Media_Name) throws SQLException {

        Statement disableMediaStatement = connection.createStatement();
        //Deactiveerd de gekozen media file
        String sql = ("UPDATE Media"
                + "SET active = 'disable'"
                + "WHERE filePath ='" + Media_Name + "'");

        disableMediaStatement.execute(sql);
    }

    /*
    Voegt de Soundeffect toe aan de gekozen Foto
     */
    public void addSoundeffectToPhoto(int idPhoto, int idSoundeffect) throws SQLException, ClassNotFoundException {
        Statement addSoundeffectStatement = connection.createStatement();
        //Voegt de Soundeffect id toe aan de Foto
        String sql = ("INSERT INTO Photo VALUES ('" + idPhoto + ", " + idSoundeffect + "')");

        addSoundeffectStatement.execute(sql);

    }

    /*
     * Activeert het gekozen thema
     */
    public void activateTheme(String Theme_Name) throws SQLException {

        Statement activateThemaStatement = connection.createStatement();
        //Activeert het gekozen thema
        String sql = ("UPDATE Thema \n"
                + "SET on/off = 'true'\n"
                + "WHERE name ='" + Theme_Name + "'");

        activateThemaStatement.execute(sql);
    }

    /*
     * Deactiveerd het gekozen thema
     */
    public void disableTheme(String Theme_Name) throws SQLException {

        Statement disableThemaStatement = connection.createStatement();
        //Deactiveerd het gekozen thema
        String sql = ("UPDATE Thema \n"
                + "SET on/off = 'false'\n"
                + "WHERE name ='" + Theme_Name + "'");

        disableThemaStatement.execute(sql);
    }

    /*
     * Voegt het gekozen media file toe aan een gekozen thema
     */
    public void addMediaToTheme(int Theme_Id, int Media_Id) throws SQLException {

        Statement addMediaToThemeStatement = connection.createStatement();
        //Voegt gekozen media toe aan gekozen thema
        String sql = ("INSERT INTO theme_has_media VALUES ('" + Theme_Id + "','" + Media_Id + "’)");

        addMediaToThemeStatement.execute(sql);

    }

    public ArrayList<Photo> getAllPhoto(ArrayList<Photo> photoList) throws SQLException, ClassNotFoundException {
        System.out.println("\n----------getAllPhoto commencing----------");
        ResultSet result;
        Photo photo;

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost/photoslider", "root", "Aapjes-14");

        Statement getAllMediaStatement = connection.createStatement();
        //Krijgt alle media uit de database
        String sql = ("SELECT name, filePath \n"
                + "FROM media A \n"
                + "WHERE EXISTS (SELECT 1 \n"
                + "FROM   photo B \n"
                + "WHERE  A.Id = B.Id)");

        result = getAllMediaStatement.executeQuery(sql);
        System.out.println("test1");
        while (result.next()) {
            photo = new Photo(result.getString("filePath"), result.getString("name"));

            System.out.println(photo.getURL());
            photoList.add(photo);
        }
        System.out.println("test2");
        return photoList;

    }

    /*
     * Zet een media file in de correcte type tabel (photo, sound, soundeffect)
     */
    public void insertIntoMediaType(String Name) throws SQLException {
        String sql;
        Set<String> imageTypes = new HashSet<>();
        imageTypes.add("png");
        imageTypes.add("jpg");
        Set<String> soundTypes = new HashSet<>();
        soundTypes.add("wav");
        soundTypes.add("mp3");

        String fileType = null;
        int i = Name.lastIndexOf('.');
        if (i > 0) {
            fileType = Name.substring(i + 1);
        }

        if (imageTypes.contains(fileType)) {
            sql = ("INSERT INTO photo (id) VALUES ((SELECT id AS lastID FROM media ORDER BY id DESC LIMIT 1))");
        } else {
            sql = ("INSERT INTO song (id) VALUES ('LAST_INSERT_ID')");
        }
        Statement addIntoMediaType = connection.createStatement();

        addIntoMediaType.execute(sql);
    }

    /**
     * check of een bestand met een bepaalde naam al bestaat, om te voorkomen
     * dat het bestand overschreven wordt.
     *
     * @param media
     */
    public boolean mediaInDatabase(Media media) {
        try {
            ResultSet set = executeQuery(String.format("SELECT name FROM media WHERE filePath = '%s';", media.getRelativePath()));
            if (set.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Voer een update uit
     *
     * @param update
     */
    public static void executeUpdate(String update) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(update);

    }

    /**
     * Voer een query uit
     *
     * @param query
     * @return set
     */
    public static ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery(query);
        return set;
    }
}
