package com.gmail.skymaxplay.skitty.objects;

import com.gmail.skymaxplay.skitty.enums.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionInfo {

    private String name;
    private String address;
    private int port;
    private String user;
    private String password;
    private SessionType type;
    private boolean fromPutty;

    public String getRegeditName() {
        return name.replaceAll(" ", "%20");
    }
}

