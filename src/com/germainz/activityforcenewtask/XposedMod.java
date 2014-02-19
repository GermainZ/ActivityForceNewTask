package com.germainz.activityforcenewtask;

import android.app.Activity;
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

    private final static SettingsHelper settingsHelper = new SettingsHelper();
    // Safe intents that can be run in a new task (the calling app does not expect a return value.)
    private final static String[] INTENT_ACTIONS = {"android.intent.action.MAIN", "android.intent.action.VIEW",
            "android.intent.action.EDIT", "android.intent.action.ATTACH_DATA", "android.intent.action.SEND",
            "android.intent.action.SENDTO", "android.intent.action.WEB_SEARCH"};
    private final static int INTENT_ACTIONS_LENGTH = INTENT_ACTIONS.length;

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
                // data back, so it's safe to run in a new task,) ignore it straight away.
                if (intentAction == null || shouldIgnore(intentAction))
                    return;
                String listType = settingsHelper.getListType();
                XposedBridge.log("listType: " + listType);
                if (listType != null) {
                    // Get the activity component that's about to be launched so we can compare that
                    // against our blacklist/whitelist.
                    Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
                    Context context = (Context) callMethod(activityThread, "getSystemContext");
                    String componentName = intent.resolveActivity(context.getPackageManager()).flattenToString();
                    // Log if necessary.
                    if (settingsHelper.isLogEnabled()) {
                        context.sendBroadcast(new Intent(Common.INTENT_LOG).putExtra(Common.INTENT_COMPONENT_EXTRA, componentName));
                        XposedBridge.log("activityforcenewtask componentString: " + componentName);
                    }
                    // If the blacklist is used and the component is in the blacklist, or if the
                    // whitelist is used and the component isn't whitelisted, we shouldn't modify
                    // the intent's flags.
                    boolean isListed = settingsHelper.isListed(componentName, listType);
                    if ((listType.equals(Common.PREF_BLACKLIST) && isListed) ||
                            (listType.equals(Common.PREF_WHITELIST) && !isListed))
                        return;
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };
        Method startActivity = findMethodExact(Activity.class, "startActivity", Intent.class, Bundle.class);
        XposedBridge.hookMethod(startActivity, hook);
    }

    boolean shouldIgnore(String action) {
        for (int i = 0; i < INTENT_ACTIONS_LENGTH; i++) {
            if (action.equals(INTENT_ACTIONS[i]))
                return false;
        }
        return true;
    }
}
