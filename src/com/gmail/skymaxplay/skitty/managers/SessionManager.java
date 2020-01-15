package com.gmail.skymaxplay.skitty.managers;

import com.gmail.skymaxplay.skitty.objects.SessionInfo;
import com.jcraft.jsch.JSch;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.util.HashMap;

public class SessionManager {

    @Getter
    private static HashMap<HBox, SessionInfo> loadedSessionsInfo = new HashMap<>();

    public static final JSch SJCH = new JSch();

}
