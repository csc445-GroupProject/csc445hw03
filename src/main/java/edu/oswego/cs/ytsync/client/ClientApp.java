package edu.oswego.cs.ytsync.client;


import edu.oswego.cs.ytsync.client.components.ConnectDialog;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Map;


public class ClientApp extends Application {
    private Client client;

    private WebView ytWebView;
    private TextField chatField;
    private TextArea chatArea;
    private TextField queueField;
    private ListView<String> queueView;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ytWebView = new WebView();
        ytWebView.setPrefWidth(564);
        ytWebView.setPrefHeight(344);
        ytWebView.getEngine().load(this.getClass().getClassLoader().getResource("html/ytembed.html").toString());

        VBox root = new VBox();
        root.setPadding(new Insets(20, 20, 20, 20));
        root.setSpacing(10);

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
        queueBox.prefHeightProperty().bind(ytWebView.heightProperty());

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
        Button submitButton = new Button("Submit");
        queueInputBox.getChildren().addAll(queueField, submitButton);

        queueView = new ListView<>();
        queueBox.getChildren().addAll(queueInputBox, queueView);
        upper.getChildren().addAll(ytWebView, queueBox);

        root.getChildren().addAll(upper, chatBox);

        Scene scene = new Scene(root, 960, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("YTSync");
        primaryStage.show();
        ConnectDialog connectDialog = new ConnectDialog();
        connectDialog.showAndWait().ifPresent(dialogMap -> {
            String username = dialogMap.get("username");
            client = new Client(username);
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
    }

    private void handleInput() {
        if(chatField.getText().startsWith("!seek ")) {
            double offset = Double.parseDouble(chatField.getText().substring(6));
            ytWebView.getEngine().executeScript(String.format("player.seekTo(%f)", offset));
        }
        chatArea.appendText(String.format("%s: %s\n", client.getUsername(), chatField.getText().trim()));
        chatField.clear();
    }
}