package gui.spirefly;

import connection.SQLConnection;
import filemanager.FileManager;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
    private ArrayList<File> songs;
    private boolean isPLayin;
    private boolean isLooped = false;
    private Media media;
    private MediaPlayer mediaPlayer;
    private int songNumber;
    private SQLConnection conn;
    private FileManager manager;

    @FXML
    public void initialize() {

        conn = new SQLConnection();
        songs = new ArrayList<File>();

        try {
            conn.connect();
            conn.createDb();
        } catch (SQLException e){
            e.printStackTrace();
        }

        refreshSongList();

        if (!songs.isEmpty()){
            changeSong(songNumber);
        }

        manager = new FileManager();

        btPlay.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PAUSE));
        btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));

        slVolume.setValue(50);

        slVolume.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(slVolume.getValue()*0.01);
            }
        });
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
        if (mediaPlayer != null) {
            if (songNumber < songs.size()-1){
                songNumber++;
            } else {
                songNumber = 0;
            }
            changeSong(songNumber);
        }
    }
    @FXML
    public void previous() {
        if (mediaPlayer != null){
            if (songNumber > 0 && songNumber <= songs.size()-1){
                songNumber--;
            } else {
                songNumber = songs.size()-1;
            }
            changeSong(songNumber);
        }
    }

    @FXML
    void mute() {
        if (mediaPlayer != null){
            mediaPlayer.setMute(!mediaPlayer.isMute());
            if (mediaPlayer.isMute()){
                btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_OFF));
            } else {
                btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));
            }
        }
    }

    @FXML
    void changeImvCover() {
        imvCover.setImage(new Image(manager.choose().toString()));
    }
    @FXML
    void setLoop(){
        if (!isLooped){
            imvLoopBt.setImage(new Image(String.valueOf(getClass().getResource("/assets/icons/player_icons/loop_on.png"))));
        } else {
            imvLoopBt.setImage(new Image(String.valueOf(getClass().getResource("/assets/icons/player_icons/loop_off.png"))));
        }
        isLooped = !isLooped;
    }
    @FXML
    void chooseSong(){
        File file = (File) manager.choose();
        if (file != null){
            conn.insertMusic(file.getName(),file.toString(),false);
        }
        refreshSongList();
    }

    public void changeSong(int index){
        if (mediaPlayer == null){
            media = new Media(songs.get(index).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
        }

        mediaPlayer.stop();
        mediaPlayer.dispose();
        media = new Media(songs.get(index).toURI().toString());

        mediaPlayer = new MediaPlayer(media);

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
                    Object value=ch.getValueAdded();

                    switch (key){
                        case "image":
                            imvCover.setImage((Image) value);
                            break;
                        case "artist":
                            lbImgTitle.setText(value.toString());
                        case "title":
                            lbSongTitle.setText(value.toString());
                            break;
                    }
                }
            }
        });

        lbSongTitle.setText(songs.get(songNumber).getName().replace(".mp3",""));
        lbImgTitle.setText(songs.get(songNumber).getName());
        imvCover.setImage(new Image(String.valueOf(getClass().getResource("/assets/icons/player_covers/default_2.png"))));

        mediaPlayer.play();

        mediaPlayer.currentTimeProperty().addListener(((observableValue, oldValue, newValue) -> {
            slProgress.setValue(newValue.toSeconds());
        }));

        autoplay();

        btPlay.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PAUSE));
        btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));

        isPLayin = true;
    }

    public void autoplay(){
        mediaPlayer.setOnEndOfMedia(() ->{
            if (!isLooped){
                if (songNumber < songs.size()-1){
                    songNumber++;
                } else songNumber =0;
            }
            changeSong(songNumber);
        });
    }

    public void refreshSongList(){

        ArrayList<File> newSongList = conn.updateMusicList(songs);

        if (!songs.isEmpty()){
            for (File song : newSongList) {
                Label newSong = new Label();

                newSong.getStyleClass().add("songItem");

                Text icon = GlyphsDude.createIcon(FontAwesomeIcon.MUSIC);
                icon.getStyleClass().add("custom-icon");

                newSong.setGraphic(icon);

                newSong.setOnMouseClicked(mouseEvent -> {
                    songNumber = songs.indexOf(song);
                    changeSong(songs.indexOf(song));
                });

                newSong.setText(" " + song.getName().replace(".mp3", ""));

                newSong.setMaxWidth(Double.MAX_VALUE);
                vbSongs.getChildren().add(newSong);
            }
        }
        else {
            lbSongTitle.setText("Sem Músicas disponíveis");
        }
    }
}