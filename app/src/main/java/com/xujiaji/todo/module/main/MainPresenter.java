package com.xujiaji.todo.module.main;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.xujiaji.todo.BuildConfig;
import com.xujiaji.todo.R;
import com.xujiaji.todo.base.App;
import com.xujiaji.todo.base.BasePresenter;
import com.xujiaji.todo.helper.PrefHelper;
import com.xujiaji.todo.helper.ToastHelper;
import com.xujiaji.todo.repository.bean.DailyBean;
import com.xujiaji.todo.repository.bean.Result;
import com.xujiaji.todo.repository.bean.TodoTypeBean;
import com.xujiaji.todo.repository.remote.DataCallbackImp;
import com.xujiaji.todo.repository.remote.Net;
import com.xujiaji.todo.util.FileUtil;
import com.xujiaji.todo.util.UpdateAppHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * author: xujiaji
 * created on: 2018/10/10 10:06
 * description:
 */
public class MainPresenter extends BasePresenter<MainContract.View,MainModel> implements MainContract.Presenter {
    @Override
    public void requestTodo(int type, SwipeRefreshLayout refreshLayout) {
        model.catTodo(type, this, new DataCallbackImp<Result<TodoTypeBean>>(refreshLayout) {
            @Override
            public void success(Result<TodoTypeBean> bean) {
                sortListByDate(bean.getData().getDoneList());
                sortListByDate(bean.getData().getTodoList());
                for (TodoTypeBean.TodoListBean todoListBean: bean.getData().getTodoList()) {
                    Collections.sort(todoListBean.getTodoList());
                }
                view.displayList(bean.getData());
            }

            @Override
            public void fail(int code, String msg) {
                super.fail(code, msg);
                if (code == Net.NO_LOGIN) {
                    view.guideLogin();
                }
            }
        });
    }

    private void sortListByDate(List<TodoTypeBean.TodoListBean> listBeans) {
        Collections.sort(listBeans, new Comparator<TodoTypeBean.TodoListBean>() {
            @Override
            public int compare(TodoTypeBean.TodoListBean o1, TodoTypeBean.TodoListBean o2) {
                return o1.getDate() == o2.getDate() ? 0 : o1.getDate() > o2.getDate() ? -1 : 1;
            }
        });

    }

    @Override
    public void requestUpdateTodo(TodoTypeBean.TodoListBean.TodoBean todoBean) {
        model.catUpdateTodo(todoBean, this, new DataCallbackImp<Result>() {
            @Override
            public void success(Result bean) {
            }
        });
    }

    @Override
    public void requestDelTodo(int id) {
        model.catDelTodo(id, this, new DataCallbackImp<Result>() {
            @Override
            public void success(Result bean) {
                ToastHelper.success(App.getInstance().getString(R.string.success_delete));
            }
        });
    }

    @Override
    public void requestDaily() {
        final Gson gson = new Gson();
        final String key = MainModel.dailyFormat.format(new Date());
        String dailyData = PrefHelper.getString("daily" + key);
        // 如果本地有这天的数据，那么就不必去请求了
        if (TextUtils.isEmpty(dailyData)) {
            model.catDailyList(this, new DataCallbackImp<Result<Map<String, DailyBean>>>() {
                @Override
                public void success(Result<Map<String, DailyBean>> bean) {
                    boolean save = false;
                    Map<String, DailyBean> map = bean.getData();
                    DailyBean db = map.get(key); // 获取今天的
                    for (String k: map.keySet()) {
                        if (key.equals(k)) {
                            save = true;
                        }
                        if (save) {
                            PrefHelper.set("daily" + k, gson.toJson(map.get(k)));
                        }

                        if (db == null) {
                            db = map.get(k);
                        }
                    }

                    if (db != null) {
                        view.displayDaily(db);
                    }

                }
            });
        } else {
            view.displayDaily(gson.fromJson(dailyData, DailyBean.class));
        }
    }

    @Override
    public void checkAppUpdate(final Activity activity) {
        new UpdateAppManager
                .Builder()
                //必须设置，当前Activity
                .setActivity(activity)
                //必须设置，实现httpManager接口的对象
                .setHttpManager(new UpdateAppHttpUtil())
                //必须设置，更新地址
                .setUpdateUrl(BuildConfig.UPDATE_VERSION_URL)
                .setThemeColor(ContextCompat.getColor(activity, R.color.colorAccent))
                //设置apk下砸路径，默认是在下载到sd卡下/Download/1.0.0/test.apk
                .setTargetPath(FileUtil.getDiskCacheDir(activity).getAbsolutePath())
                .build()
                //检测是否有新版本
                .checkNewApp(new UpdateCallback() {
                    /**
                     * 解析json,自定义协议
                     *
                     * @param json 服务器返回的json
                     * @return UpdateAppBean
                     */
                    @Override
                    protected UpdateAppBean parseJson(String json) {
                        UpdateAppBean updateAppBean = new UpdateAppBean();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            updateAppBean
                                    //（必须）是否更新Yes,No
                                    .setUpdate(jsonObject.optInt("version_code") > BuildConfig.VERSION_CODE ? "Yes" : "No")
                                    //（必须）新版本号，
                                    .setNewVersion(jsonObject.getString("version_name"))
                                    //（必须）下载地址
                                    .setApkFileUrl(jsonObject.getString("apk_url"))
                                    //测试下载路径是重定向路径
//                                    .setApkFileUrl("http://openbox.mobilem.360.cn/index/d/sid/3282847")
//                                    .setUpdateDefDialogTitle(String.format("AppUpdate 是否升级到%s版本？", newVersion))
                                    .setUpdateLog(jsonObject.getString("update_info"))
                                    //大小，不设置不显示大小，可以不设置
                                    .setTargetSize(jsonObject.getString("file_size"))
                                    //是否强制更新，可以不设置
                                    .setConstraint(jsonObject.getBoolean("constraint"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return updateAppBean;
                    }

                    @Override
                    protected void hasNewApp(UpdateAppBean updateApp, UpdateAppManager updateAppManager) {
                        updateAppManager.showDialogFragment();
                    }

                    /**
                     * 网络请求之前
                     */
                    @Override
                    public void onBefore() {

                    }

                    /**
                     * 网路请求之后
                     */
                    @Override
                    public void onAfter() {

                    }

                    /**
                     * 没有新版本
                     */
                    @Override
                    public void noNewApp(String error) {

                    }
                });

    }
}
