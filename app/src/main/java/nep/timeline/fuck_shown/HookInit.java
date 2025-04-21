package nep.timeline.fuck_shown;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookInit implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam packageParam) {
        if ("com.android.systemui".equals(packageParam.packageName)) {
            ClassLoader classLoader = packageParam.classLoader;
            XposedHelpers.findAndHookMethod("com.oplus.systemui.statusbar.notification.interruption.OplusKeyguardNotificationVisibilityProviderExImpl", classLoader, "shouldHideNotification", "com.android.systemui.statusbar.notification.collection.NotificationEntry", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        int currentOrUpcomingState = (int) XposedHelpers.callMethod(XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "statusBarStateController"), "get"), "getCurrentOrUpcomingState");
                        boolean mShowing = XposedHelpers.getBooleanField(XposedHelpers.getObjectField(param.thisObject, "keyguardStateController"), "mShowing");
                        if (!(currentOrUpcomingState == 1 || currentOrUpcomingState == 2) && !mShowing) {
                            param.setResult(false);
                            return;
                        }

                        Object notificationKeyguardHelper = XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "notificationKeyguardHelper"), "get");
                        boolean shouldShowOnKeyguard = (boolean) XposedHelpers.callMethod(notificationKeyguardHelper, "shouldShowOnKeyguard", XposedHelpers.callMethod(param.args[0], "getSbn"));
                        if (!shouldShowOnKeyguard) {
                            param.setResult(true);
                            return;
                        }

                        boolean isLockDeadState = XposedHelpers.getStaticBooleanField(XposedHelpers.findClass("com.oplus.systemui.keyguard.lockdead.LockDeadUtil", classLoader), "isLockDeadState");
                        if (isLockDeadState) {
                            param.setResult(true);
                            return;
                        }

                        int mLockScreenNotificationState = XposedHelpers.getIntField(notificationKeyguardHelper, "mLockScreenNotificationState");
                        if (mLockScreenNotificationState != 2 && mLockScreenNotificationState != 1) {
                            param.setResult(true);
                            return;
                        }

                        param.setResult(false);
                    } catch (Throwable e) {
                        XposedBridge.log(e);
                    }
                }
            });
        }
    }
}
