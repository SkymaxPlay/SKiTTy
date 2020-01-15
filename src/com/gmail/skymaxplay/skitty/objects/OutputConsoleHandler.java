package com.gmail.skymaxplay.skitty.objects;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class OutputConsoleHandler extends OutputStream {

    private TextArea output;

    public OutputConsoleHandler(TextArea textArea) {
        this.output = textArea;
    }

    @Override
    public void write(int i) throws IOException {
        Platform.runLater(() -> this.output.appendText(String.valueOf((char) i)));
    }
}
