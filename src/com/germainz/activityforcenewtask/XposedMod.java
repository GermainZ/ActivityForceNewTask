package com.germainz.activityforcenewtask;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposedMod implements IXposedHookZygoteInit {

    private final static SettingsHelper settingsHelper = new SettingsHelper();
    // Safe intents that can be run in a new task (the calling app does not expect a return value.)
    private final static String[] INTENT_ACTIONS = {"android.intent.action.MAIN", "android.intent.action.VIEW",
            "android.intent.action.EDIT", "android.intent.action.ATTACH_DATA", "android.intent.action.SEND",
            "android.intent.action.SENDTO", "android.intent.action.WEB_SEARCH"};

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                settingsHelper.reload();
                if (settingsHelper.isModDisabled())
                    return;
                Intent intent = (Intent) XposedHelpers.getObjectField(param.thisObject, "intent");

                // The intent already has FLAG_ACTIVITY_NEW_TASK set, no need to do anything.
                if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == Intent.FLAG_ACTIVITY_NEW_TASK)
                    return;

                String intentAction = intent.getAction();
                // If the intent is not a known safe intent (as in, the launching app does not expect
                // data back, so it's safe to run in a new task,) ignore it straight away.
                if (intentAction == null || shouldIgnore(intentAction))
                    return;

                // Get the activity component that's about to be launched so we can compare that
                // against our whitelist.
                Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
                Context context = (Context) callMethod(activityThread, "getSystemContext");
                ComponentName componentName = intent.resolveActivity(context.getPackageManager());

                // If the app is launching one of its own activities, we shouldn't open it in a new task.
                if (componentName.getPackageName().equals(AndroidAppHelper.currentPackageName()))
                    return;

                String componentNameString = componentName.flattenToString();
                // Log if necessary.
                if (settingsHelper.isLogEnabled()) {
                    context.sendBroadcast(new Intent(Common.INTENT_LOG).putExtra(Common.INTENT_COMPONENT_EXTRA, componentNameString));
                    XposedBridge.log("activityforcenewtask componentString: " + componentNameString);
                }

                // If the blacklist is used and the component is in the blacklist, or if the
                // whitelist is used and the component isn't whitelisted, we shouldn't modify
                // the intent's flags.
                String listType = settingsHelper.getListType();
                boolean isListed = settingsHelper.isListed(componentNameString, listType);
                if ((listType.equals(Common.PREF_BLACKLIST) && isListed) ||
                        (listType.equals(Common.PREF_WHITELIST) && !isListed))
                    return;

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };

        Class ActivityRecord = findClass("com.android.server.am.ActivityRecord", null);
        XposedBridge.hookAllConstructors(ActivityRecord, hook);
    }

    boolean shouldIgnore(String action) {
        for (String INTENT_ACTION : INTENT_ACTIONS) {
            if (action.equals(INTENT_ACTION))
                return false;
        }
        return true;
    }

}
