package nl.amsta09.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

import nl.amsta09.model.Media;
import nl.amsta09.model.Photo;
import nl.amsta09.model.Theme;
import nl.amsta09.model.Audio;
import nl.amsta09.app.Settings;

/**
 * Met deze class kan een object aangemaakt worden dat als API dient voor de database.
 * 
 * @author Hugo Thunnissen
 */
public class SqlConnector {

    private static Connection connection;

    public SqlConnector() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/photoslider?"
                    + "user=PAD&password=fissafissaheey123&useUnicode=true&useJDBCCompliantTimezoneShift=true"
                    + "&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false");
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
        String on = "0";
        String sql = String.format("INSERT INTO theme (name, `on`) VALUES ('%s', '%s')", themeName, on);

        addThemeStatement.execute(sql);

    }

    public void setThemeFirst(Theme theme) throws SQLException, ClassNotFoundException {
        Statement addThemeStatement = connection.createStatement();
        //Insert de gegevens in de database met een unieke ID
        String on = "showFirst";
        String sql1 = String.format("DELETE FROM settings WHERE settingName = 'showFirst'");
        addThemeStatement.execute(sql1);
        String sql2 = String.format("INSERT INTO settings (settingName, itemId) VALUES ('%s', '%s')", on, theme.getId());
        addThemeStatement.execute(sql2);
    }

    public Theme getFirstTheme() throws SQLException {
        ResultSet set = executeQuery(String.format("SELECT * FROM theme INNER JOIN settings ON theme.id = settings.itemId;"));
        set.next();
        Theme theme = new Theme(set.getString("theme.name"), set.getInt("theme.id"));
        theme.setPhotoList(getAllPhotosFromTheme(theme));
        theme.setMusicList(getAllMusicsFromTheme(theme));
        return theme;
    }

    /*
     * Verwijdert het gekozen Thema uit de database
     */
    public void deleteTheme(Theme theme) throws SQLException, ClassNotFoundException {
        Statement deleteThemeStatement = connection.createStatement();
        //Verwijdert de thema uit de tabel Thema
        String sql = ("DELETE FROM theme_has_media WHERE theme_id = " + theme.getId());
        deleteThemeStatement.execute(sql);
        String sql2 = ("DELETE FROM theme WHERE id = " + theme.getId());
        deleteThemeStatement.execute(sql2);
    }

    /*
     * Insert een media file naar de database
     */
    public void insertMedia(String filePath, Media media) throws SQLException, ClassNotFoundException {

        filePath = filePath.replace("\\", "\\\\\\\\");

        Statement addMediaStatement = connection.createStatement();
        //Insert een media file in de database
        String sql = ("INSERT INTO Media (name, filePath) VALUES ('" + media.getName() + "','" + filePath + "')");

        addMediaStatement.execute(sql);

        insertIntoMediaType(media);
    }


    /*
    Verwijdert de gekozen Media uit de database
     */
    public void deleteMedia(int id) throws SQLException{

        Statement deleteMediaStatement = connection.createStatement();
        String sql = ("DELETE FROM media"
                + " WHERE id='" + id + "'");

        deleteMediaStatement.execute(sql);
    }

    /*
     * Activeert het gekozen media file
     */
    public void activateMedia(Media media) throws SQLException {

        Statement activateMediaStatement = connection.createStatement();
        //Activeert de gekozen media file
        String sql = ("UPDATE Media"
                + "SET active = 'true'"
                + "WHERE filePath ='" + media.getName() + "'");

        activateMediaStatement.execute(sql);
    }

    /*
     * Deactiveerd het gekozen media file
     */
    public void disableMedia(Media media) throws SQLException {

        Statement disableMediaStatement = connection.createStatement();
        //Deactiveerd de gekozen media file
        String sql = ("UPDATE Media"
                + "SET active = 'disable'"
                + "WHERE filePath ='" + media.getName() + "'");

        disableMediaStatement.execute(sql);
    }

    /*
    Voegt de Soundeffect toe aan de gekozen Foto
     */
    public void addSoundeffectToPhoto(Photo photo, Audio soundeffect) throws SQLException, ClassNotFoundException {
        Statement addSoundeffectStatement = connection.createStatement();
        //Voegt de Soundeffect id toe aan de Foto
        String sql = ("INSERT INTO Photo VALUES ('" + photo.getId() + ", " + soundeffect.getId() + "')");

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
    Voegt de nieuwe instellingen voor het geluid toe
     */
    public void insertMusicSettings(Settings setting, String name) throws SQLException {
        Statement settings = connection.createStatement();
        int on;
        if (setting.getSound()) {
            on = 1;
        } else {
            on = 0;
        }
        String sql1 = ("DELETE FROM settings WHERE settingName = '" + name + "'");
        settings.execute(sql1);
        String sql2 = ("INSERT INTO settings (settingName, OnOff) VALUES ('" + name + "','" + on + "')");
        settings.execute(sql2);
    }

    /*
    Haalt de instelligen op vanuit de Database
     */
    public int getSettingFromDatabase(String nameSetting) throws SQLException {
        int turnOnOff;
        ResultSet getSettings = executeQuery("SELECT * FROM settings INNER JOIN theme ON settings.settingName = '" + nameSetting + "'");
        getSettings.next();
        turnOnOff = getSettings.getInt("OnOff");
        return turnOnOff;
    }

    /*
     * Voegt het gekozen media file toe aan een gekozen thema
     */
    public void addMediaToTheme(int Theme_Id, Media media) throws SQLException {

        Statement addMediaToThemeStatement = connection.createStatement();
        //Voegt gekozen media toe aan gekozen thema
        String sql = ("INSERT INTO theme_has_media VALUES ('" + Theme_Id + "','" + media.getId() + "')");

        addMediaToThemeStatement.execute(sql);
    }

    /*
     * Verwijderd het gekozen media file van een gekozen thema
     */
    public void deleteMediaFromTheme(int Theme_Id, int Media_Id) throws SQLException {

        Statement deleteMediaFromThemeStatement = connection.createStatement();
        //Voegt gekozen media toe aan gekozen thema
        String sql = ("DELETE FROM theme_has_media WHERE theme_id = '" + Theme_Id + "'"
                + "AND media_id = '" + Media_Id + "'");

        deleteMediaFromThemeStatement.execute(sql);
    }

    /**
     * Voeg een media bestand toe aan de juiste tabel in de database aan de hand
     * van het soort media.
     *
     * @param media
     */
    public void insertIntoMediaType(Media media) throws SQLException {
        if (media instanceof Photo) {
            executeUpdate("INSERT INTO photo (id) VALUES ((SELECT id AS lastID FROM media ORDER BY id DESC LIMIT 1))");
        } else if (media instanceof Audio) {
            executeUpdate("INSERT INTO song (id) VALUES ((SELECT id AS lastID FROM media ORDER BY id DESC LIMIT 1))");
        } else {
            System.out.println("Niet gelukt te kijken wat voor media");
        }
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
     * Haal de id op van een media bestand aan de hand van de naam.
     *
     * @return mediaId
     */
    public int getMediaIdFrom(Media media) throws SQLException {
        ResultSet set = executeQuery(String.format("SELECT id FROM media WHERE filePath = '%s';", media.getRelativePath()));
        set.next();
        return set.getInt("id");
    }

    /**
     * De hoogste theme id die in de database aanwezig is.
     *
     * @return maxThemeId
     */
    public int getMaxThemeId() throws SQLException, ThemeNotFoundException {
        ResultSet set = executeQuery("Select id FROM theme ORDER BY id DESC LIMIT 1;");
        set.next();
        return set.getInt("id");
    }

    /**
     * Theme dat bij de id hoort.
     *
     * @param id
     * @return theme;
     */
    public Theme getThemeById(int id) throws SQLException, ThemeNotFoundException {
        Theme theme;
        ResultSet themeSet = executeQuery("SELECT * FROM theme WHERE id = " + id);
        if (themeSet.next()) {
            theme = new Theme(themeSet.getString("theme.name"), themeSet.getInt("theme.id"));
        } else {
            throw new ThemeNotFoundException("Thema niet gevonden");
        }
        theme.setPhotoList(getAllPhotosFromTheme(theme));
        theme.setMusicList(getAllMusicsFromTheme(theme));
        return theme;
    }

    /**
     * Random thema.
     *
     * @return randomTheme
     */
    public Theme getRandomTheme() throws SQLException {
        ResultSet themeSet = executeQuery("SELECT * FROM theme ORDER BY RAND() LIMIT 1;");
        themeSet.next();
        Theme theme = new Theme(themeSet.getString("theme.name"), themeSet.getInt("theme.id"));
        theme.setPhotoList(getAllPhotosFromTheme(theme));
        theme.setMusicList(getAllMusicsFromTheme(theme));
        return theme;
    }

    /**
     * Theme met een ander id.
     *
     * @param theme
     * @return otherTheme
     * @throws SQLException
     */
    public Theme getRandomThemeThatIsNot(Theme theme) throws SQLException {
        Theme otherTheme;
        ResultSet themeSet = executeQuery("SELECT * FROM theme WHERE id != " + theme.getId()
                + " ORDER BY RAND() LIMIT 1;");
        themeSet.next();
        otherTheme = new Theme(themeSet.getString("theme.name"), themeSet.getInt("theme.id"));
        otherTheme.setPhotoList(getAllPhotosFromTheme(otherTheme));
        otherTheme.setMusicList(getAllMusicsFromTheme(otherTheme));
        return otherTheme;
    }

    /**
     * Haal een actief theme op.
     *
     * @param id
     * @return theme
     */
    public Theme getActiveThemeById(int id) throws ThemeNotFoundException, SQLException {
        Theme theme;
        ResultSet themeSet = executeQuery("SELECT * FROM theme INNER JOIN theme_has_media ON theme.id = theme_has_media.theme_id WHERE theme.id =" + id);
        if (themeSet.next()) {
            theme = new Theme(themeSet.getString("theme.name"), themeSet.getInt("theme.id"));
        } else {
            throw new ThemeNotFoundException("Thema niet gevonden");
        }
        theme.setPhotoList(getAllPhotosFromTheme(theme));
        //theme.setMusicList(getAllMusicsFromTheme(theme));
        return theme;
    }

    /**
     * Haal alle foto's van een bepaald thema op.
     *
     * @param theme
     * @return photos
     */
    public ArrayList<Photo> getAllPhotosFromTheme(Theme theme) throws SQLException {
        ArrayList<Photo> photos = new ArrayList<>();
        ResultSet set = executeQuery(String.format("SELECT * FROM photo INNER JOIN media ON photo.id = media.id WHERE media.id IN (SELECT media_id FROM "
                + "theme_has_media WHERE theme_id = %s);", theme.getId()));
        while (set.next()) {
            photos.add(new Photo(set.getString("media.filePath"), set.getString("media.name"), set.getInt("media.id"), theme.getName()));
        }
        return photos;
    }

    /**
     * Haal alle muziek van een bepaald thema op.
     *
     * @param theme
     * @return audio
     */
    public ArrayList<Audio> getAllMusicsFromTheme(Theme theme) throws SQLException {
        ArrayList<Audio> musics = new ArrayList<>();
        ResultSet set = executeQuery(String.format("SELECT * FROM song INNER JOIN media ON song.id = media.id WHERE media.id IN (SELECT media_id FROM "
                + "theme_has_media WHERE theme_id = %s);", theme.getId()));
        while (set.next()) {
            musics.add(new Audio(set.getString("media.filePath"), set.getString("media.name"), set.getInt("media.id"), theme.getName()));
        }
        return musics;
    }

    /**
     * Haal een foto uit de database aan de hand van de opgegeven id.
     *
     * @param id
     * @return photo
     */
    public Photo getPhotoById(int id) throws SQLException {
        ResultSet set = executeQuery(String.format("SELECT * FROM photo INNER JOIN media ON photo.id = media.id WHERE media.id = %s;", id));
        set.next();
        return new Photo(set.getString("media.filePath"), set.getString("media.name"), set.getInt("media.id"), set.getString("media.id"));
    }

    public Audio getMusicById(int id) throws SQLException {
        ResultSet set = executeQuery(String.format("SELECT * FROM song INNER JOIN media ON song.id = media.id WHERE media.id = %s;", id));
        set.next();
        return new Audio(set.getString("media.filePath"), set.getString("media.name"), set.getInt("media.id"), set.getString("media.id"));
    }

    /**
     * Alle thema's in de database.
     *
     * @return themes
     */
    public ArrayList<Theme> getAllThemes() throws SQLException {
        ArrayList<Theme> themes = new ArrayList<>();
        ResultSet set = executeQuery("SELECT * FROM theme;");
        while (set.next()) {
            themes.add(new Theme(set.getString("name"), set.getInt("id")));
        }
        return themes;
    }

    /**
     * Haal alle foto's op uit de database.
     *
     * @return photos
     */
    public ArrayList<Photo> getAllPhoto() throws SQLException, ClassNotFoundException {
        ArrayList<Photo> photoList = new ArrayList<>();
        ResultSet result = executeQuery("SELECT * FROM media INNER JOIN photo ON media.id = photo.id;");
        while (result.next()) {
            photoList.add(new Photo(
                    result.getString("media.filePath"),
                    result.getString("media.name"),
                    result.getInt("media.id")));
        }
        return photoList;
    }

	/**
	 * Haal alle foto's op die niet bij het gegeven thema horen.
	 * @param theme
	 * @return photos
	 */
    public ArrayList<Photo> getAllPhotosNotInTheme(Theme theme)throws SQLException{
        ArrayList<Photo> photoList = new ArrayList<>();
        ResultSet result = executeQuery("SELECT * FROM media INNER JOIN photo ON media.id = photo.id "+
                     "WHERE media.id NOT IN (SELECT media_id FROM theme_has_media WHERE theme_id = "+ 
                     theme.getId() + ");");
        while (result.next()) {
            photoList.add(new Photo(
            			result.getString("media.filePath"), 
            			result.getString("media.name"), 
            			result.getInt("media.id")));
        }
        return photoList;
    }

    /**
     * Haal alle foto's op uit de database.
     * @return photos
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public ArrayList<Audio> getAllAudio() throws SQLException, ClassNotFoundException {
        ArrayList<Audio> audioList = new ArrayList<>();
        ResultSet result = executeQuery("SELECT * FROM media INNER JOIN song ON media.id = song.id;");
        while (result.next()) {
            Audio audio;
            audioList.add(audio = new Audio(
                    result.getString("media.filePath"),
                    result.getString("media.name"),
                    result.getInt("media.id")));
        }
        return audioList;
    }

    /**
     * Voeg een media bestand toe aan de database.
     *
     * @param media
     */
    public void insertMedia(Media media) throws SQLException {
        executeUpdate(String.format("INSERT INTO media (name, filePath) VALUES ('%s','%s')", media.getName(), media.getRelativePath()));
        insertIntoMediaType(media);
    }

    /**
     * Voer een update uit
     *
     * @param update
     */
    public void executeUpdate(String update) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(update);

    }

    /**
     * Voer een query uit
     *
     * @param query
     * @return set
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery(query);
        return set;
    }

    public void deleteTheme(Media media) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class ThemeNotFoundException extends Exception {

        public ThemeNotFoundException(String message) {
            super(message);
        }

    }
}
