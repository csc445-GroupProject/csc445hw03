package edu.oswego.cs.ytsync.client;


import edu.oswego.cs.ytsync.client.components.ConnectDialog;
import edu.oswego.cs.ytsync.common.raft.RaftMessageBuffer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientApp extends Application {
    String username;
    Socket server;
    private TextField chatField;
    private TextArea chatArea;
    private ChatServer chatServer;

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        new ConnectDialog().showAndWait().ifPresent(m -> {
            username = m.get("username");
            String addr = m.get("server");

            int port = Integer.parseInt(m.get("port"));

            chatServer = new ChatServer(addr, port, new ArrayList<>(), chatArea);

            new Thread(chatServer).start();

            try {
                server = new Socket(addr, port);
            } catch (IOException e) {
                throw new RuntimeException();
            }

            new Thread(() -> {
                RaftMessageBuffer buffer = new RaftMessageBuffer();
                try {
                    InputStream in = server.getInputStream();
                    byte[] bytes = new byte[16384];

                    while (true) {
                        int available = in.available();
                        if (available > 0) {
                            int size = in.read(bytes);
                            buffer.addToBuffer(bytes, size);

                            if (buffer.hasNext()) {
                                List<String> hostnames = new ArrayList<>();

                                System.out.printf("\nNEW HOST LIST\n");

                                while (buffer.hasNext()) {
                                    hostnames.addAll(buffer.next().getHostnames());
                                }

                                for (String h : hostnames) {
                                    System.out.printf("%s\n", h);
                                }

                                chatServer.getUpdatedClientListFromServer(hostnames);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            chatField = new TextField();
            chatArea = new TextArea();
            VBox.setVgrow(chatArea, Priority.ALWAYS);
            chatArea.setEditable(false);
            HBox.setHgrow(chatField, Priority.ALWAYS);
            Button sendButton = new Button("Send");
            HBox inputBox = new HBox(10, chatField, sendButton);

            sendButton.setOnMouseClicked(me -> {
                sendInput();
            });

            chatField.setOnKeyPressed(ke -> {
                if (ke.getCode() == KeyCode.ENTER)
                    sendInput();
            });

            VBox root = new VBox(10, chatArea, inputBox);
            root.setPadding(new Insets(10, 10, 10, 10));
            primaryStage.setScene(new Scene(root));
            chatField.requestFocus();
            primaryStage.show();
        });
    }

    private void sendInput() {
        String message = chatField.getText().trim();
        chatServer.getNewMessageFromConnectedClient(username, message);
        chatField.clear();
    }
}