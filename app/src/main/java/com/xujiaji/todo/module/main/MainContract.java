package com.xujiaji.todo.module.main;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.xujiaji.todo.base.PresenterLife;
import com.xujiaji.todo.repository.bean.DailyBean;
import com.xujiaji.todo.repository.bean.Result;
import com.xujiaji.todo.repository.bean.TodoTypeBean;
import com.xujiaji.todo.repository.remote.DataCallback;

import java.util.List;
import java.util.Map;

import io.xujiaji.xmvp.contracts.XContract;

/**
 * author: xujiaji
 * created on: 2018/10/10 10:06
 * description:
 */
public class MainContract {
    public static final int CATEGORY_USE_ONE = 0;
    public static final int CATEGORY_WORK = 1;
    public static final int CATEGORY_LEARN = 2;
    public static final int CATEGORY_LIFE = 3;

    /**
     * 既紧急又重要
     */
    public static final int PRIORITY_URGENT_IMPORTANT = 1;

    /**
     * 重要不紧急
     */
    public static final int PRIORITY_IMPORTANT_NOTURGENT = 2;

    /**
     * 紧急不重要
     */
    public static final int PRIORITY_URGENT_NOTIMPORTANT = 3;

    /**
     * 不紧急不重要
     */
    public static final int PRIORITY_NOTURGENT_NOTIMPORTANT = 0;

    interface View extends XContract.View {
        void displayList(TodoTypeBean todoTypeBean);
        void showChooseTodoCategory();
        void hideChooseTodoCategory();
        void showDeleteTip(int position, TodoTypeBean.TodoListBean.TodoBean todoBean);
        void showLongClickDialog(android.view.View clickView, final int position, final TodoTypeBean.TodoListBean.TodoBean todoBean);
        void guideLogin();
        void displayDaily(DailyBean dailyBean);
    }

    interface Presenter extends XContract.Presenter {
        void requestTodo(int type, SwipeRefreshLayout refreshLayout);
        void requestUpdateTodo(TodoTypeBean.TodoListBean.TodoBean todoBean);
        void requestDelTodo(int id);

        void requestDaily();

        void checkAppUpdate(Activity activity);
    }

    public interface Model extends XContract.Model {
        void catTodo(int type, PresenterLife presenterLife, DataCallback<Result<TodoTypeBean>> callback);
        void catUpdateTodo(TodoTypeBean.TodoListBean.TodoBean todoBean, PresenterLife presenterLife, DataCallback<Result> callback);
        void catDelTodo(int id, PresenterLife presenterLife, DataCallback<Result> callback);
        void catDailyList(String dailyUrl, PresenterLife presenterLife, DataCallback<Result<Map<String, DailyBean>>> callback);
    }
}
