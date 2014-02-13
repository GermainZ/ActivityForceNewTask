package com.germainz.activityforcenewtask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.lang.reflect.Method;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
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
                settingsHelper.reload();
                if (settingsHelper.isModDisabled())
                    return;
                Intent intent = (Intent) param.args[0];
                // Get the activity component that's about to be launched
                Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
                Context context = (Context) callMethod(activityThread, "getSystemContext");
                ComponentName componentName = intent.resolveActivity(context.getPackageManager());
                String componentString = componentName.flattenToString();
                // If the component is in the blacklist, do nothing
                if (settingsHelper.isBlacklisted(componentString))
                    return;
                XposedBridge.log("activityforcenewtask componentString: " + componentString);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };
        Method startActivity = findMethodExact(Activity.class, "startActivity", Intent.class, Bundle.class);
        XposedBridge.hookMethod(startActivity, hook);
    }
}
