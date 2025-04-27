package nep.timeline.fuck_shown;

import java.lang.reflect.Method;

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
 
            Class<?> clazz = XposedHelpers.findClassIfExists("com.oplus.systemui.statusbar.notification.interruption.OplusKeyguardNotificationVisibilityProviderExImpl", classLoader);
            if (clazz == null) {
                XposedBridge.log("[Fuck-Shown] YOUR DEVICE IS UNSUPPORTED!");
                return;
            }

            try {
                Method method = clazz.getDeclaredMethod("shouldHideNotificationInLockState", XposedHelpers.findClass("com.android.systemui.statusbar.notification.collection.NotificationEntry", classLoader));
                XposedBridge.log("[Fuck-Shown] Your device is OxygenOS!");
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        try {
                            boolean isLockDeadState = XposedHelpers.getStaticBooleanField(XposedHelpers.findClass("com.oplus.systemui.keyguard.lockdead.LockDeadUtil", classLoader), "isLockDeadState");
                            if (isLockDeadState) {
                                param.setResult(true);
                                return;
                            }

                            Object notificationKeyguardHelper = XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "notificationKeyguardHelper"), "get");
                            boolean isLockScreenNotificationEnable = (boolean) XposedHelpers.callMethod(notificationKeyguardHelper, "isLockScreenNotificationEnable");
                            if (!isLockScreenNotificationEnable) {
                                param.setResult(true);
                                return;
                            }

                            param.setResult(false);
                        } catch (Throwable throwable) {
                            XposedBridge.log(throwable);
                        }
                    }
                });
            } catch (NoSuchMethodException e) {
                XposedBridge.log("[Fuck-Shown] Your device is ColorOS!");
                XposedHelpers.findAndHookMethod(clazz, "shouldHideNotification", "com.android.systemui.statusbar.notification.collection.NotificationEntry", new XC_MethodHook() {
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
                        } catch (Throwable throwable) {
                            XposedBridge.log(throwable);
                        }
                    }
                });
            }
        }
    }
}
