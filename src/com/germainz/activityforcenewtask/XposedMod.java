package com.germainz.activityforcenewtask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.lang.reflect.Method;

import static de.robv.android.xposed.XposedHelpers.findMethodExact;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class XposedMod implements IXposedHookZygoteInit {

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        final SettingsHelper settingsHelper = new SettingsHelper();
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (settingsHelper.isModDisabled())
                    return;
                Intent intent = (Intent) param.args[0];
                String className = param.thisObject.getClass().getName();
                settingsHelper.reload();
                if (settingsHelper.isBlacklisted(className))
                    return;
                XposedBridge.log("activityforcenewtask Class: " + className);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };
        Method startActivity = findMethodExact(Activity.class, "startActivity", Intent.class, Bundle.class);
        XposedBridge.hookMethod(startActivity, hook);
    }
}
