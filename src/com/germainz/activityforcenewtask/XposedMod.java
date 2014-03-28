package com.germainz.activityforcenewtask;

import android.content.Context;
import android.content.Intent;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

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
                Intent intent = (Intent) getObjectField(param.thisObject, "intent");

                String intentAction = intent.getAction();
                // If the intent is not a known safe intent (as in, the launching app does not expect
                // data back, so it's safe to run in a new task,) ignore it straight away.
                if (intentAction == null || shouldIgnore(intentAction))
                    return;
                // Get the activity component that's about to be launched so we can compare that
                // against our whitelist.
                Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
                Context context;
                if (activityThread != null)
                    context = (Context) callMethod(activityThread, "getSystemContext");
                else
                    context = (Context) getStaticObjectField(findClass("android.app.ActivityThread", null), "mSystemContext");
                String componentName = intent.resolveActivity(context.getPackageManager()).flattenToString();
                // Log if necessary.
                if (settingsHelper.isLogEnabled()) {
                    context.sendBroadcast(new Intent(Common.INTENT_LOG).putExtra(Common.INTENT_COMPONENT_EXTRA, componentName));
                    XposedBridge.log("activityforcenewtask componentString: " + componentName);
                }
                // We shouldn't modify the intent's flag unless the component is whitelisted.
                boolean isListed = settingsHelper.isListed(componentName);
                if (!isListed)
                    return;
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };

        Class ActivityRecord = findClass("com.android.server.am.ActivityRecord", null);
        XposedBridge.hookAllConstructors(ActivityRecord, hook);
    }

    boolean shouldIgnore(String action) {
        for (int i = 0; i < INTENT_ACTIONS.length; i++) {
            if (action.equals(INTENT_ACTIONS[i]))
                return false;
        }
        return true;
    }
}
