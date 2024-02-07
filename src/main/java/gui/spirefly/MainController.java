package gui.spirefly;

import connection.SQLConnection;
import javafx.scene.shape.SVGPath;
import manager.FileManager;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainController {
    @FXML
    private Label lbSongTitle;
    @FXML
    private Slider slProgress;
    @FXML
    private Slider slVolume;
    @FXML
    private Button btPlay;
    @FXML
    private ImageView imvLoopBt;
    @FXML
    private Button btMute;
    @FXML
    private ImageView imvCover;
    @FXML
    private VBox vbSongs;
    @FXML
    private Label lbImgTitle;
    private final ArrayList<File> songs = new ArrayList<File>();
    private boolean isPLayin;
    private boolean isLooped = false;
    private Media media;
    private MediaPlayer mediaPlayer;
    private int songNumber;
    private SQLConnection conn;
    private FileManager manager;

    @FXML
    public void initialize() throws SQLException {

        conn = new SQLConnection();

        conn.connect();
        conn.createDb(); //CREATE IF NOT EXISTS

        refreshSongList();

        if (!songs.isEmpty()){

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            changeSong(songNumber);
        }

        btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));

        slVolume.setValue(50);

        slVolume.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(slVolume.getValue()*0.01);
            }
        });

        System.out.println(songs.toString());

        conn.disconnect();
    }

    @FXML
    public void play() {
        if (mediaPlayer != null){
            if (isPLayin){
                btPlay.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PLAY));
                mediaPlayer.pause();
            } else {
                btPlay.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PAUSE));
                mediaPlayer.play();
            }
            isPLayin = !isPLayin;

            slProgress.setMax(media.getDuration().toSeconds());

            mediaPlayer.currentTimeProperty().addListener(((observableValue, oldValue, newValue) -> {
                slProgress.setValue(newValue.toSeconds());
            }));

            autoplay();
        }
    }
    @FXML
    void slideTo() {
        if (mediaPlayer != null){
            mediaPlayer.seek(Duration.seconds(slProgress.getValue()));
        }
    }

    @FXML
    public void next() {
        if (songs.size() > 1){
            if (songNumber < songs.size()-1){
                changeSong(songNumber+1);
            } else {
                changeSong(0);
            }
        }
    }
    @FXML
    public void previous() {
        if (songs.size() < 1){
            if (songNumber > 0 && songNumber <= songs.size()-1){
                changeSong(songNumber-1);
            } else {
                changeSong(songs.size()-1);
            }
        }
    }

    @FXML
    void mute() {
        mediaPlayer.setMute(!mediaPlayer.isMute());
        if (mediaPlayer.isMute()){
            btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_OFF));
        } else {
            btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));
        }
    }

    @FXML
    void changeImvCover() {
        manager = new FileManager();
        imvCover.setImage(new Image(manager.choose().toString())); //change cover image (BETA) - mudar imagem de capa (BETA)
    }
    @FXML
    void setLoop(){
        // Activates song loop to true value - Ativa o loop de música para true
        if (!isLooped){
            imvLoopBt.setImage(new Image(String.valueOf(getClass().getResource("/assets/icons/player_icons/loop_on.png"))));
        } else {
            imvLoopBt.setImage(new Image(String.valueOf(getClass().getResource("/assets/icons/player_icons/loop_off.png"))));
        }
        isLooped = !isLooped;
    }
    @FXML
    void addSong() throws SQLException {
        conn.connect();
        manager = new FileManager();
        File file = (File) manager.choose(); //FileChoose to choose the song - FileChoose para escolher a múscia
        if (file != null){
            conn.insertMusic(file.getName(),file.toString(),false);
        }
        Label newSong = new Label(file.getName().substring(0,file.getName().lastIndexOf('.')));

        songs.add(file);

        newSong.setOnMouseClicked(event -> {
            changeSong(songs.indexOf(file));
        });

        vbSongs.getChildren().add(newSong);
    }

    public void deleteSong(File song){
        try {
            conn.connect();
            conn.deleteMusic(String.valueOf(song));
            vbSongs.getChildren().remove(songs.indexOf(song));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
    }

    public void changeSong(int index){

        if (mediaPlayer != null && (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING || mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) && songNumber == index){
            return;
        }

        songNumber = index;

        mediaPlayer.stop();
        mediaPlayer.dispose();

        mediaPlayer = null;
        media = null;
        System.gc();

        try{
            media = new Media(songs.get(index).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
        } catch (Exception e){
            e.printStackTrace();
        }

        mediaPlayer.setOnReady(() -> {
            slProgress.setMin(0);
            slProgress.setMax(media.getDuration().toSeconds());
        });

        slVolume.setValue(50);

        slProgress.setMax(media.getDuration().toSeconds());

        media.getMetadata().addListener(new MapChangeListener<String,Object>(){
            @Override
            public void onChanged(Change<? extends String, ?> ch) {
                if(ch.wasAdded()){

                    String key=ch.getKey();

                    switch (key){
                        case "image":
                            imvCover.setImage((Image) ch.getValueAdded());
                            break;
                        case "artist":
                            lbImgTitle.setText(ch.getValueAdded().toString());
                        case "title":
                            lbSongTitle.setText(ch.getValueAdded().toString());
                            break;
                    }
                }
            }
        });

        FadeTransition fade = new FadeTransition(Duration.millis(500),lbSongTitle); //set fade to title - Criar um fade para o título

        fade.setFromValue(0);
        fade.setToValue(1.0);
        fade.play();

        lbSongTitle.setText(songs.get(songNumber).getName().replace(".mp3",""));
        lbImgTitle.setText(songs.get(songNumber).getName());
        imvCover.setImage(new Image(String.valueOf(getClass().getResource("/assets/icons/player_covers/default_2.png"))));

        mediaPlayer.play();

        mediaPlayer.currentTimeProperty().addListener(((observableValue, oldValue, newValue) -> {
            slProgress.setValue(newValue.toSeconds());
        }));

        btPlay.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PAUSE));
        btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));

        isPLayin = true;
        autoplay();
    }

    public void autoplay(){
        mediaPlayer.setOnEndOfMedia(() ->{
            if (!isLooped){
                if (songNumber < songs.size()-1){
                    changeSong(songNumber+1);
                } else changeSong(0);
            } else {
                mediaPlayer.seek(Duration.millis(0));
            }
        });
    }

    public void refreshSongList() throws SQLException {

        conn.connect();

        ArrayList<File> newSongList = conn.updateMusicList(songs);

        conn.disconnect();

        if (!songs.isEmpty()){
            for (File song : newSongList) {
                Label newSong = new Label(song.getName().substring(0,song.getName().lastIndexOf('.')));

                newSong.setCursor(Cursor.HAND);

                newSong.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY){
                        changeSong(songs.indexOf(song));
                    }
                    else {

                        if (songs.indexOf(song) != songNumber){
                            Alert alert = new Alert(AlertType.CONFIRMATION, "Delete Selected Song?", ButtonType.YES, ButtonType.CANCEL);
                            alert.showAndWait();

                            if (alert.getResult() == ButtonType.YES) {
                                deleteSong(song);
                            }
                        }
                    }
                });

                vbSongs.getChildren().add(newSong);
            }
        }

        else {
            lbSongTitle.setText("Sem Músicas disponíveis");
        }
    }
}