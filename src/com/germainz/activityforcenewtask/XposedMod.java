package com.germainz.activityforcenewtask;

import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XposedMod implements IXposedHookZygoteInit {

    private final static SettingsHelper settingsHelper = new SettingsHelper();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                settingsHelper.reload();
                if (settingsHelper.isModDisabled())
                    return;
                Intent intent = (Intent) XposedHelpers.getObjectField(param.thisObject, "intent");

                // The launching app does not expect data back. It's safe to run the activity in a
                // new task.
                int requestCode = getIntField(param.thisObject, "requestCode");
                if (requestCode != -1)
                    return;

                // The intent already has FLAG_ACTIVITY_NEW_TASK set, no need to do anything.
                if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) == Intent.FLAG_ACTIVITY_NEW_TASK)
                    return;

                String intentAction = intent.getAction();
                // If the intent is not a known safe intent (as in, the launching app does not expect
                // data back, so it's safe to run in a new task,) ignore it straight away.
                if (intentAction == null)
                    return;

                // If the app is launching one of its own activities, we shouldn't open it in a new task.
                int uid = ((ActivityInfo) getObjectField(param.thisObject, "info")).applicationInfo.uid;
                if (getIntField(param.thisObject, "launchedFromUid") == uid)
                    return;

                ComponentName componentName = (ComponentName) getObjectField(param.thisObject, "realActivity");
                String componentNameString = componentName.flattenToString();
                // Log if necessary.
                if (settingsHelper.isLogEnabled()) {
                    // Get context
                    Context context = AndroidAppHelper.currentApplication();

                    if (context != null)
                        context.sendBroadcast(new Intent(Common.INTENT_LOG).putExtra(Common.INTENT_COMPONENT_EXTRA, componentNameString));
                    else
                        XposedBridge.log("activityforcenewtask: couldn't get context.");
                    XposedBridge.log("activityforcenewtask: componentString: " + componentNameString);
                }

                // If the blacklist is used and the component is in the blacklist, or if the
                // whitelist is used and the component isn't whitelisted, we shouldn't modify
                // the intent's flags.
                boolean isListed = settingsHelper.isListed(componentNameString);
                String listType = settingsHelper.getListType();
                if ((listType.equals(Common.PREF_BLACKLIST) && isListed) ||
                        (listType.equals(Common.PREF_WHITELIST) && !isListed))
                    return;

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        };

        Class ActivityRecord = findClass("com.android.server.am.ActivityRecord", null);
        XposedBridge.hookAllConstructors(ActivityRecord, hook);
    }

}
