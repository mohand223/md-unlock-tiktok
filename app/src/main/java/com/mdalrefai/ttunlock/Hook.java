package com.mdalrefai.ttunlock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {

    private static final String[] TIKTOK = {
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically.go"
    };

    private static final String COUNTRY = "us";
    private static final String OPERATOR = "310260";
    private static final String CARRIER = "T-Mobile";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        boolean isTikTok = false;
        for (String p : TIKTOK) {
            if (p.equals(lpparam.packageName)) { isTikTok = true; break; }
        }
        if (!isTikTok) return;

        ClassLoader cl = lpparam.classLoader;

        blockSelfKill(cl);

        try {
            Class<?> tm = Class.forName("android.telephony.TelephonyManager", false, cl);
            replace(tm, "getSimCountryIso", COUNTRY);
            replace(tm, "getNetworkCountryIso", COUNTRY);
            replace(tm, "getSimOperator", OPERATOR);
            replace(tm, "getNetworkOperator", OPERATOR);
            replace(tm, "getSimOperatorName", CARRIER);
            replace(tm, "getNetworkOperatorName", CARRIER);
        } catch (Throwable ignored) {}

        try {
            Class<?> loc = Class.forName("java.util.Locale", false, cl);
            XposedBridge.hookAllMethods(loc, "getCountry", new XC_MethodReplacement() {
                @Override protected Object replaceHookedMethod(MethodHookParam p) { return "US"; }
            });
        } catch (Throwable ignored) {}
    }

    private void blockSelfKill(ClassLoader cl) {
        final int myPid = android.os.Process.myPid();

        final XC_MethodHook stopVoid = new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) { p.setResult(null); }
        };

        try {
            Class<?> sys = Class.forName("java.lang.System", false, cl);
            XposedBridge.hookAllMethods(sys, "exit", stopVoid);
        } catch (Throwable ignored) {}

        try {
            Class<?> rt = Class.forName("java.lang.Runtime", false, cl);
            XposedBridge.hookAllMethods(rt, "exit", stopVoid);
            XposedBridge.hookAllMethods(rt, "halt", stopVoid);
        } catch (Throwable ignored) {}

        try {
            Class<?> proc = Class.forName("android.os.Process", false, cl);
            XposedBridge.hookAllMethods(proc, "killProcess", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam p) {
                    try {
                        if (p.args != null && p.args.length > 0
                                && p.args[0] instanceof Integer
                                && ((Integer) p.args[0]) == myPid) {
                            p.setResult(null);
                        }
                    } catch (Throwable ignored) {}
                }
            });
        } catch (Throwable ignored) {}
    }

    private void replace(Class<?> cls, String method, final String value) {
        try {
            XposedBridge.hookAllMethods(cls, method, new XC_MethodReplacement() {
                @Override protected Object replaceHookedMethod(MethodHookParam p) { return value; }
            });
        } catch (Throwable ignored) {}
    }
}
