package gui.spirefly;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private Button btMute;
    @FXML
    private ImageView imvCover;
    @FXML
    private VBox vbSongs;
    @FXML
    private Label lbImgTitle;
    private ArrayList<File> songs;
    private boolean isPLayin;
    private Media media;
    private MediaPlayer mediaPlayer;
    private int songNumber;

    @FXML
    public void initialize() {

        FileManager manager = new FileManager();
        songs = manager.listFiles(new File("C:\\Spirefly\\music"));

        manager.createDirectory("C:\\Spirefly\\music");

        btPlay.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.PLAY));
        btMute.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP));

        if (!songs.isEmpty()){
            for (File song : songs) {
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
            changeSong(songNumber);
        }
        else {
            lbSongTitle.setText("Sem Músicas disponíveis");
        }

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolher imagem de capa");
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null){
            imvCover.setImage(new Image(selectedFile.toURI().toString()));
        }
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

        Map<String,Object> audioMap = new HashMap<>();

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
            if (songNumber < songs.size()-1){
                songNumber++;
            } else songNumber =0;
            changeSong(songNumber);
        });
    }
}