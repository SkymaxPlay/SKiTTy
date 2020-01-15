package com.gmail.skymaxplay.skitty.utils;

import com.gmail.skymaxplay.skitty.enums.SessionType;
import com.gmail.skymaxplay.skitty.objects.SessionInfo;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class RegeditUtils {
    public static final String PUTTY_KEY = "Software\\SimonTatham\\PuTTY\\Sessions";
    public static final String SKITTY_KEY = "Software\\SkymaxPlay\\SKiTTy\\Sessions";

    static {
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, SKITTY_KEY);
    }


    public static SessionInfo loadPuttySession(String name) {
        // Odczyt Username i Password do rejestru ze SKiTTy
        boolean extendedData = Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(name));
        return new SessionInfo(name.replaceAll("%20", " "), getPuttyField(name, "HostName"), Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, getPuttyKey(name), "PortNumber"), extendedData ? getSKiTTyField(name, "User") : "", extendedData ? getSKiTTyField(name, "Password") : "", SessionType.SSH, true);
    }

    public static SessionInfo loadSKiTTySession(String name) {
        if (Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, getPuttyKey(name))) return null; // jesli istnieje sesja w putty o tej samej nazwie to znaczy ze ta sesja bedzie posiadala tzw. extendedData czyli Username i Password wiec nie trzeba tej sesji ladowac
        return new SessionInfo(name.replaceAll("%20", " "), getSKiTTyField(name, "Address"), Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(name), "Port"), getSKiTTyField(name, "User"), getSKiTTyField(name, "Password"), SessionType.valueOf(getSKiTTyField(name, "Type")), false);
    }

    public static void saveSKiTTySession(SessionInfo info) {
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(info.getRegeditName()));
        if (!info.isFromPutty()) {
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(info.getRegeditName()), "Address", info.getAddress());
            Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(info.getRegeditName()), "Port", info.getPort());
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(info.getRegeditName()), "Type", info.getType().toString());
        }
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(info.getRegeditName()), "User", info.getUser());
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(info.getRegeditName()), "Password", info.getPassword()); // dodanie szyfrowania base np
    }

    public static void removeSession(SessionInfo info) {
        Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, info.isFromPutty() ? getPuttyKey(info.getRegeditName()) : getSKiTTyKey(info.getRegeditName()));
    }

    private static String getSKiTTyKey(String name) {
        return SKITTY_KEY + "\\" + name;
    }

    private static String getPuttyKey(String name) {
        return PUTTY_KEY + "\\" + name;
    }

    private static String getPuttyField(String name, String field) {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, getPuttyKey(name), field);
    }

    private static String getSKiTTyField(String name, String field) {
        return Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, getSKiTTyKey(name), field);
    }
}
