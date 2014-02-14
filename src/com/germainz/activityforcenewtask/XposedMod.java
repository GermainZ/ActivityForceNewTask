package com.germainz.activityforcenewtask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

    final static SettingsHelper settingsHelper = new SettingsHelper();
    // Safe intents that can be run in a new task (the calling app does not expect a return value.)
    final static String[] intentActions = {"android.intent.action.MAIN", "android.intent.action.VIEW",
            "android.intent.action.EDIT", "android.intent.action.ATTACH_DATA", "android.intent.action.SEND",
            "android.intent.action.SENDTO", "android.intent.action.WEB_SEARCH"};

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                settingsHelper.reload();
                if (settingsHelper.isModDisabled())
                    return;
                Intent intent = (Intent) param.args[0];
                String intentAction = intent.getAction();
                // If the intent is not a known safe intent (as in, the launching app does not expect
                // data back, so it's safe to run in a new task,) ignore it
                if (intentAction == null || shouldIgnore(intentAction))
                    return;
                // Get the activity component that's about to be launched
                Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
                Context context = (Context) callMethod(activityThread, "getSystemContext");
                ComponentName componentName = intent.resolveActivity(context.getPackageManager());
                String componentString = componentName.flattenToString();
                // If the component is in the blacklist, do nothing
                if (settingsHelper.isBlacklisted(componentString))
                    return;
                // Log if necessary
                if (settingsHelper.isLogEnabled()) {
                    context.sendBroadcast(new Intent(Common.INTENT_LOG).putExtra("componentString", componentString));
                    XposedBridge.log("activityforcenewtask componentString: " + componentString);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };
        Method startActivity = findMethodExact(Activity.class, "startActivity", Intent.class, Bundle.class);
        XposedBridge.hookMethod(startActivity, hook);
    }

    boolean shouldIgnore(String action) {
        int i = intentActions.length;
        int j = 0;
        while (j < i) {
            if (action.equals(intentActions[j])) {
                return false;
            }
            j++;
        }
        return true;
    }
}
