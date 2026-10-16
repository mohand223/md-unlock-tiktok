package com.mdalrefai.ttunlock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * MD Unlock TikTok - فاتح تيك توك للمناطق المحجوبة.
 * صُنع بواسطة: مهند الرفاعي (@m.alrefai_515)
 */
public class Hook implements IXposedHookLoadPackage {

    private static final String[] TIKTOK = {
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically.go"
    };

    private static final String COUNTRY  = "us";
    private static final String OPERATOR = "310260";
    private static final String CARRIER  = "T-Mobile";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        boolean isTikTok = false;
        for (String p : TIKTOK) {
            if (p.equals(lpparam.packageName)) { isTikTok = true; break; }
        }
        if (!isTikTok) return;

        Class<?> tm;
        try {
            tm = Class.forName("android.telephony.TelephonyManager", false, lpparam.classLoader);
        } catch (Throwable t) {
            return;
        }

        hook(tm, "getSimCountryIso",      COUNTRY);
        hook(tm, "getNetworkCountryIso",  COUNTRY);
        hook(tm, "getSimOperator",        OPERATOR);
        hook(tm, "getNetworkOperator",    OPERATOR);
        hook(tm, "getSimOperatorName",    CARRIER);
        hook(tm, "getNetworkOperatorName", CARRIER);
    }

    private void hook(Class<?> cls, String method, final String value) {
        try {
            XposedBridge.hookAllMethods(cls, method, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return value;
                }
            });
        } catch (Throwable ignored) {
        }
    }
}
