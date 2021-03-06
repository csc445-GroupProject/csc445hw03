package edu.oswego.cs.ytsync.client.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class ConnectDialog extends Dialog<Map<String, String>> {
    private static final ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);

    public ConnectDialog() {
        super();

        getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);
        setTitle("Connect to Server");

        VBox mainBox = new VBox();
        mainBox.setPadding(new Insets(10, 75, 10, 10));
        mainBox.setSpacing(10);

        HBox serverBox = new HBox();
        serverBox.setSpacing(10);

        TextField username = new TextField();
        username.setPromptText("Username");
        TextField server = new TextField();
        server.setText("pi.cs.oswego.edu");
        server.setPromptText("Server");
        TextField port = new TextField();
        port.setMinWidth(40);
        port.setText("2706");
        port.setPromptText("Port");
        port.prefWidthProperty().bind(serverBox.widthProperty().divide(3));

        serverBox.getChildren().addAll(server, port);
        mainBox.getChildren().addAll(username, serverBox);

        getDialogPane().setContent(mainBox);

        Node connectButton = getDialogPane().lookupButton(connectButtonType);
        connectButton.disableProperty().bind(username.textProperty().isEmpty()
                .or(server.textProperty().isEmpty())
                .or(port.textProperty().isEmpty()));

        Platform.runLater(username::requestFocus);

        setResultConverter(type -> {
            if(type == connectButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("username", username.getText().trim());
                result.put("server", server.getText().trim());
                result.put("port", port.getText().trim());
                return result;
            }
            return null;
        });
    }
}
