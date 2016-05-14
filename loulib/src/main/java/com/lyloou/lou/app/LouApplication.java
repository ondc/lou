package com.lyloou.lou.app;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.lyloou.lou.other.CrashHandler;
import com.lyloou.lou.util.Uapk;
import com.lyloou.lou.util.Ustring;

import java.util.ArrayList;

/**
 * TtcApplication完成了以下工作：
 * · 根据运行环境确定是否启用日志（需要使用Ulog）(原理是判断apk是否是通过export的方式进行得到的)；
 * · 是否跳过欢迎界面，在需要跳过的地方对SKIP_WELCOME进行判断和设值； （参照小米运动手环app的做法）
 * · 初始化崩溃日志的信息（需要提供有效日志路径，才会进行异常托管）；
 * · activity的管理；（需要结合TtcActivity使用，才能通过LouApplication进行管理）
 *
 * @author Lou
 */
public abstract class LouApplication extends Application {
    public static boolean DEBUG;
    public static boolean SKIP_WELCOME;

    @Override
    @CallSuper
    public void onCreate() {
        SKIP_WELCOME = false;
        DEBUG = Uapk.isApkDebugable(this);

        // 根据路径的有效性，判断是否记录日志
        String crashLogPath = crashLogPath();
        if (!TextUtils.isEmpty(crashLogPath) && Ustring.isValidPath(crashLogPath)) {
            CrashHandler crash = CrashHandler.getInstance();
            crash.init(getApplicationContext());
            crash.setCrashDir(crashLogPath);
        }

        super.onCreate();
    }

    /**
     * 设置崩溃日志的路径（例如：return "/Crash/CrashLog"，不包括文件名，文件名会根据日期信息自动生成）；
     * 路径不为空并且须有效，否则将不进行异常托管；
     * 注意：需要权限支持；
     *
     * @return 有效的路径字符串（不包括文件名）
     */
    protected abstract String crashLogPath();

    //-----------------Activity管理；
    private static final ArrayList<Activity> sActivities = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        sActivities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        if (sActivities.contains(activity)) {
            sActivities.remove(activity);
        }
    }

    public static void finishAll() {
        for (Activity activity : sActivities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
    //~~~~~~~~~~~~~~
}
