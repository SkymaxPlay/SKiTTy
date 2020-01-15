package com.gmail.skymaxplay.skitty.utils;

import com.gmail.skymaxplay.skitty.managers.SessionManager;
import com.gmail.skymaxplay.skitty.objects.SessionInfo;

public class SessionUtils {

    public static SessionInfo getSessionByName(String name) {
        for (SessionInfo session : SessionManager.getLoadedSessionsInfo().values()) {
            if (session.getName().equals(name)) return session;
        }
        return null;
    }
}
