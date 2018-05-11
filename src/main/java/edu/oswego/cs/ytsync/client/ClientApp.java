package edu.oswego.cs.ytsync.client;


import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import edu.oswego.cs.ytsync.client.components.ConnectDialog;
import edu.oswego.cs.ytsync.common.ConnectPacket;
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
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientApp extends Application {
    private TextField chatField;
    private TextArea chatArea;
    private ChatServer chatServer;
    String username;
    Socket server;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        new ConnectDialog().showAndWait().ifPresent(m -> {
            username = m.get("username");
            String serverName = m.get("server");

            int port = Integer.parseInt(m.get("port"));

            try {
                server = new Socket(serverName, port);
            } catch (IOException e) {
                throw new RuntimeException();
            }

            chatField = new TextField();
            chatArea = new TextArea();
            VBox.setVgrow(chatArea, Priority.ALWAYS);
            chatArea.setEditable(false);
            HBox.setHgrow(chatField, Priority.ALWAYS);
            Button sendButton = new Button("Send");
            HBox inputBox = new HBox(10, chatField, sendButton);

            VBox root = new VBox(10, chatArea, inputBox);
            root.setPadding(new Insets(10, 10, 10, 10));
            primaryStage.setScene(new Scene(root));
            chatField.requestFocus();
            primaryStage.show();
        });
    }
}