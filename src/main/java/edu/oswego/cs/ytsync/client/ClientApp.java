package edu.oswego.cs.ytsync.client;


import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import edu.oswego.cs.ytsync.client.components.ConnectDialog;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class ClientApp extends Application {
    private Client client;

    //private WebView ytWebView;
    private TextField chatField;
    private TextArea chatArea;
    private TextField queueField;
    private ListView<String> queueView;
    MediaPlayer player;
    MediaView playerView;
    private Slider volumeSlider;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        ytWebView = new WebView();
//        ytWebView.managedProperty().bind(ytWebView.visibleProperty());
//        ytWebView.setOnMouseClicked(me -> {
//            System.out.println(ytWebView.getEngine().executeScript("player.getCurrentTime()"));
//        });
//        ytWebView.setPrefWidth(644);
//        ytWebView.setPrefHeight(394);
//        ytWebView.getEngine().load(this.getClass().getClassLoader().getResource("html/ytembed.html").toString());

        playerView = new MediaView();
        playerView.managedProperty().bind(playerView.visibleProperty());
        HBox.setHgrow(playerView, Priority.ALWAYS);

        VBox root = new VBox();

        VBox uiBox = new VBox();
        VBox.setVgrow(uiBox, Priority.ALWAYS);
        uiBox.setPadding(new Insets(20, 20, 20, 20));
        uiBox.setSpacing(10);
        playerView.fitHeightProperty().bind(root.heightProperty().multiply(.50));

        //menu
        MenuBar menuBar = new MenuBar();
        Menu viewMenu = new Menu("View");
        CheckMenuItem toggleVideo = new CheckMenuItem("Show Video");
        toggleVideo.setSelected(true);
        viewMenu.getItems().addAll(toggleVideo);
        menuBar.getMenus().addAll(viewMenu);
        root.getChildren().add(menuBar);

        playerView.visibleProperty().bind(toggleVideo.selectedProperty());

        HBox upper = new HBox();
        upper.setSpacing(10);

        VBox chatBox = new VBox();
        chatBox.setSpacing(5);
        VBox.setVgrow(chatBox, Priority.ALWAYS);

        HBox chatInputBox = new HBox();
        chatInputBox.setSpacing(5);

        VBox queueBox = new VBox();
        queueBox.setSpacing(5);
        HBox.setHgrow(queueBox, Priority.ALWAYS);
        //queueBox.prefHeightProperty().bind(playerView.fitHeightProperty());

        HBox queueInputBox = new HBox();
        queueInputBox.setSpacing(5);

        chatField = new TextField();
        HBox.setHgrow(chatField, Priority.ALWAYS);
        Button sendButton = new Button("Send");
        chatInputBox.getChildren().addAll(chatField, sendButton);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.appendText("Welcome to chat!\n\n");
        VBox.setVgrow(chatArea, Priority.ALWAYS);
        chatBox.getChildren().addAll(chatArea, chatInputBox);

        queueField = new TextField();
        queueField.setPromptText("Enter a YouTube URL");
        HBox.setHgrow(queueField, Priority.ALWAYS);
        Button submitButton = new Button("Add");
        queueInputBox.getChildren().addAll(queueField, submitButton);

        queueView = new ListView<>();
        VBox.setVgrow(queueView, Priority.ALWAYS);
        queueBox.getChildren().addAll(queueInputBox, queueView);
        upper.getChildren().addAll(playerView, queueBox);

        uiBox.getChildren().addAll(upper, chatBox);
        root.getChildren().add(uiBox);

        Scene scene = new Scene(root, 960, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("YTSync");
        primaryStage.show();
        //player.play();
        ConnectDialog connectDialog = new ConnectDialog();
        connectDialog.showAndWait().ifPresent(dialogMap -> {
            String username = dialogMap.get("username");
            String server = dialogMap.get("server");
            int port = Integer.parseInt(dialogMap.get("port"));
            try {
                client = new Client(server, port, username);
                new Thread(client).start();
            } catch (IOException e) {
                Alert errAlert = new Alert(Alert.AlertType.ERROR, "Could not connect to server.");
                errAlert.showAndWait();
            }
        });

        sendButton.setOnAction(e -> {
            handleInput();
        });

        chatField.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case ENTER: {
                    handleInput();
                }
            }
        });

        submitButton.setOnAction(e -> {
            YoutubeDLRequest request = new YoutubeDLRequest(queueField.getText().trim());
            request.setOption("format", "mp4");
            request.setOption("get-url");
            try {
                if(player != null)
                    player.stop();
                YoutubeDLResponse response = YoutubeDL.execute(request);
                String url = response.getOut().trim();
                if(player != null)
                    player.dispose();
                player = new MediaPlayer(new Media(url));
                playerView.setMediaPlayer(player);
                player.play();
                player.setVolume(.5);
                chatArea.appendText("The Volume of this Video is: " + player.getVolume() + ".\n\n");
            } catch (YoutubeDLException e1) {
                e1.printStackTrace();
            }
        });
    }

    private void handleInput() {
        // seeks the video the number entered is the number of seconds the video will sync to from the beginning of the song
        if(chatField.getText().startsWith("!seek ")) {
            double offset = Double.parseDouble(chatField.getText().substring(6));
            player.seek(Duration.seconds(offset));
        }

        //toggles mute for the video
        if(chatField.getText().startsWith("!mute")) {
            if(player.isMute()) {
                player.setMute(false);
            } else {
                player.setMute(true);
            }
        }

        //controls the volume
        if(chatField.getText().startsWith("!volume ")) {
            double volume = Double.parseDouble(chatField.getText().substring(8));
            if(volume >= 0 && volume <= 1.0)
                player.setVolume(volume);
        }


        chatArea.appendText(String.format("User: %s\n", /*client.getUsername(), */chatField.getText().trim()));
        chatField.clear();
    }
}