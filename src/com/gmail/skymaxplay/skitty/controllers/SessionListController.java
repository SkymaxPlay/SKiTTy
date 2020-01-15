package com.gmail.skymaxplay.skitty.controllers;

import com.gmail.skymaxplay.skitty.SKiTTY;
import com.gmail.skymaxplay.skitty.enums.SessionType;
import com.gmail.skymaxplay.skitty.managers.SessionManager;
import com.gmail.skymaxplay.skitty.objects.OutputConsoleHandler;
import com.gmail.skymaxplay.skitty.objects.SessionInfo;
import com.gmail.skymaxplay.skitty.utils.RegeditUtils;
import com.gmail.skymaxplay.skitty.utils.SessionUtils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

public class SessionListController implements Initializable {

    private final Background focusBackground = new Background(new BackgroundFill(Color.LIGHTCYAN, CornerRadii.EMPTY, Insets.EMPTY));
    private final Background unfocusBackground = new Background(new BackgroundFill(null, CornerRadii.EMPTY, Insets.EMPTY));

    @FXML
    public Button create;
    @FXML
    public Button remove;
    @FXML
    public Button save;
    @FXML
    public ScrollPane sessionList;

    @FXML
    public TextField name;
    @FXML
    public TextField host;
    @FXML
    public TextField port;
    @FXML
    public TextField user;
    @FXML
    public PasswordField password;

    @FXML
    public Button connect;

    private VBox sessionsBox;
    private HBox selectedBox;

    public void onRemoveSession(ActionEvent event) {
        if (selectedBox == null) return;
        SessionInfo sessionInfo = SessionManager.getLoadedSessionsInfo().get(selectedBox);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Usunięcie sesji");
        confirm.setHeaderText(sessionInfo.getName());
        confirm.setContentText("Czy na pewno chcesz usunąć tą sesję?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.get() == ButtonType.CANCEL) return;
        RegeditUtils.removeSession(sessionInfo);
        ((VBox) selectedBox.getParent()).getChildren().remove(selectedBox);
        SessionManager.getLoadedSessionsInfo().remove(selectedBox);

        getConnectionFields().forEach(f -> f.setText(null));
        remove.setDisable(true);
        save.setDisable(true);
    }

    public void onSaveSession(ActionEvent event) {
        if (!validateConnectionData()) return;
        if (name.getText().length() == 0) return;

        int parsedPort = Integer.parseInt(port.getText());

        SessionInfo session = SessionUtils.getSessionByName(name.getText());
        if (session != null) {
            session.setAddress(host.getText());
            session.setPort(parsedPort);
            session.setUser(user.getText());
            session.setPassword(password.getText());
            System.out.println("session not null");
        } else {
            session = new SessionInfo(name.getText(), host.getText(), parsedPort, user.getText(), password.getText(), SessionType.SSH, false);
            renderSesionBox(session);
        }

        // TODO: dodac zapis putty po formPutty
        RegeditUtils.saveSKiTTySession(session);
    }

    public void onCreateSession(ActionEvent event) {
        selectedBox = null;
        remove.setDisable(true);
        save.setDisable(true);
        getConnectionFields().forEach(f -> f.setText(null));
    }

    private List<TextInputControl> getConnectionFields() {
        return Arrays.asList(name, host, port, user, password);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect.setDisable(true);
        remove.setDisable(true);
        save.setDisable(true);
        getConnectionFields().forEach(f -> f.textProperty().addListener(e -> onEditConnectionFileds()));

        List<SessionInfo> loadedSessions = new ArrayList<>();
        for (String session : Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, RegeditUtils.PUTTY_KEY)) {
            loadedSessions.add(RegeditUtils.loadPuttySession(session));
        }

        for (String session : Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, RegeditUtils.SKITTY_KEY)) {
            SessionInfo e = RegeditUtils.loadSKiTTySession(session);
            if (e != null) loadedSessions.add(e);
        }

        sessionsBox = new VBox();
        sessionsBox.setSpacing(10);
        loadedSessions.forEach(this::renderSesionBox);
        sessionList.setContent(sessionsBox);
    }

    public void onConnect(ActionEvent event) {
        if (!validateConnectionData()) return;

        TextArea textArea = new TextArea();
        TextField commandField = new TextField();
        HBox box = new HBox(commandField);
        box.setMaxHeight(25);
        box.setPadding(new Insets(5, 5, 5, 5));
        SplitPane splitPane = new SplitPane(textArea, box);
        splitPane.setOrientation(Orientation.VERTICAL);
        textArea.setEditable(false);
        textArea.appendText("[SKiTTy] Connecting...\n");
        Tab tab = new Tab(name.getText() == null ? user.getText() + "@" + host.getText() : name.getText(), splitPane);
        MainController mainController = SKiTTY.getInstance().getMainController();
        mainController.tabPane.getTabs().add(tab);
        mainController.tabPane.getSelectionModel().select(tab);

        commandField.setMinWidth(mainController.tabPane.getScene().getWindow().getWidth() - 30);
        mainController.tabPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            commandField.setMinWidth(newVal.intValue() - 10);
        });

        ((Stage) connect.getScene().getWindow()).close();

        /** **************************************** */

        Session session;

        try {
            session = SessionManager.SJCH.getSession(user.getText(), host.getText(), Integer.parseInt(port.getText()));
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
        } catch (JSchException e) {
            textArea.appendText("[SKiTTy] Error while connecting! Message: " + e.getMessage());
            return;
        }

        session.setPassword(password.getText());

        OutputConsoleHandler out = new OutputConsoleHandler(textArea);
        PrintStream ps = new PrintStream(out, true);

        Channel channel;
        try {
            session.connect(3000);

            channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("dumb");
            channel.connect();
            channel.setOutputStream(ps); // works

            commandField.setOnKeyPressed((e) -> {
                BufferedWriter bw;
                String text = commandField.getText();
                if (e.getCode() == KeyCode.ENTER && text != null) {
                    System.out.println("exec: " + text);
                    try {
                        bw = new BufferedWriter(new OutputStreamWriter(channel.getOutputStream()));
                        bw.write(text + "\n");
                        bw.flush();
                        bw.close();
                        commandField.setText(null);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            textArea.appendText("[SKiTTy] Error while connecting! Message: " + e.getMessage());
            return;
        }

        tab.onClosedProperty().addListener(e -> {
            try {
                ps.close();
                out.close();
                if (channel.isConnected()) channel.disconnect();
                if (session.isConnected()) session.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        SKiTTY.getInstance().getPrimaryStage().requestFocus();
    }

    private boolean validateConnectionData() {
        if (getConnectionFields().stream().anyMatch(f -> f.getText() == null || f.getText().isEmpty())) return false;
        try {
            Integer.parseInt(port.getText());
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private void renderSesionBox(SessionInfo sessionInfo) {
        HBox box = new HBox();
        box.setPadding(new Insets(5, 5, 5, 5));
        Label sessionTitle = new Label(sessionInfo.getName());
        box.getChildren().add(sessionTitle);
        sessionsBox.getChildren().add(box);
        box.setMinWidth(sessionsBox.getWidth());
        SessionManager.getLoadedSessionsInfo().put(box, sessionInfo);
        box.setOnMouseClicked((event) -> {
            remove.setDisable(false);
            save.setDisable(false);
            box.requestFocus();
            name.setText(sessionInfo.getName());
            host.setText(sessionInfo.getAddress());
            port.setText(Integer.toString(sessionInfo.getPort()));
            user.setText(sessionInfo.getUser());
            password.setText(sessionInfo.getPassword());
            selectedBox = box;
        });

        box.backgroundProperty().bind(Bindings.when(box.focusedProperty()).then(focusBackground).otherwise(unfocusBackground));
    }

    private void tryEnableConnectionButton() {
        if (validateConnectionData()) {
            if (save.isDisabled()) save.setDisable(false);
            connect.setDisable(false);
        } else connect.setDisable(true);
    }

    public void onEditConnectionFileds() {
        tryEnableConnectionButton();
    }
}
