package com.xujiaji.todo.module.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xujiaji.happybubble.BubbleDialog;
import com.xujiaji.happybubble.Util;
import com.xujiaji.todo.R;
import com.xujiaji.todo.base.App;
import com.xujiaji.todo.base.BaseActivity;
import com.xujiaji.todo.dialog.OperatingGuideDialog;
import com.xujiaji.todo.helper.BubbleCreator;
import com.xujiaji.todo.helper.EmptyViewHelper;
import com.xujiaji.todo.helper.PrefHelper;
import com.xujiaji.todo.helper.ToolbarHelper;
import com.xujiaji.todo.module.about.AboutActivity;
import com.xujiaji.todo.module.login.LoginDialogActivity;
import com.xujiaji.todo.module.post.PostFragment;
import com.xujiaji.todo.repository.bean.TodoTypeBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends  BaseActivity<MainPresenter> implements MainContract.View, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mRefresh;
    private RecyclerView mTodoListView;
    private TodoAdapter mTodoAdapter;
    private PostFragment mPostFragment;
    private FrameLayout mFragmentContainerView;
    private FloatingActionButton mFab;
    private BubbleDialog mBubbleDialog;
    private View mBubbleDialogView;
    private BubbleDialog mContentBubbleDialog;
    private TextView mContentBubbleDialogText;
    private View mHeadView;
    private TextView mHeadViewText;
    private int mCategory;
    private final SparseArray<TodoTypeBean> mTypeBeanMap = new SparseArray<>();

    @Override
    public int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onPresenterCircle(MainPresenter presenter) {
        try {
            presenter.checkAppUpdate(this);
        } catch (Exception ignored) {}
    }

    @Override
    public void onInitCircle() {
        super.onInitCircle();
        mPostFragment = new PostFragment();

        mFragmentContainerView = findViewById(R.id.fragmentContainer);
        mTodoListView          = findViewById(R.id.todoListView);
        mRefresh               = findViewById(R.id.refresh);
        mFab                   = findViewById(R.id.fab);

        mTodoListView.setAdapter(mTodoAdapter = new TodoAdapter(presenter));
        EmptyViewHelper.initEmpty(mTodoListView);

        mTodoAdapter.addHeaderView(mHeadView = LayoutInflater.from(this).inflate(R.layout.layout_home_head, mTodoListView, false));
        mHeadViewText = mHeadView.findViewById(R.id.tvCategory);

        ToolbarHelper.initPaddingTopDiffBar(mHeadView);

        View view = getLayoutInflater().inflate(R.layout.layout_content, null);
        mContentBubbleDialogText = view.findViewById(R.id.tvContent);
        mContentBubbleDialog = new BubbleDialog(this)
                .addContentView(view)
                .setRelativeOffset(-16)
                .setBubbleLayout(BubbleCreator.get(this))
                .setPosition(BubbleDialog.Position.TOP, BubbleDialog.Position.BOTTOM);


        mRefresh.setColorSchemeResources(R.color.colorAccent, R.color.green_500, R.color.purple_500, R.color.grey_500);
        mRefresh.setRefreshing(true);
        onRefresh();
    }

    @Override
    public void onListenerCircle() {
        super.onListenerCircle();
        mRefresh.setOnRefreshListener(this);
        mHeadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseTodoCategory();
            }
        });
        mTodoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MultiItemEntity entity = mTodoAdapter.getData().get(position);
                if (entity.getItemType() == TodoAdapter.TYPE_TODO) {
                    TodoTypeBean.TodoListBean.TodoBean todoBean = (TodoTypeBean.TodoListBean.TodoBean) entity;
                    final String content = todoBean.getContent();
                    if (!TextUtils.isEmpty(content)) {
                        mContentBubbleDialogText.setText(content);
                        mContentBubbleDialog.setClickedView(view);
                        mContentBubbleDialog.show();
                    }
                }

            }
        });

        mTodoAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                MultiItemEntity entity = mTodoAdapter.getData().get(position);
                if (entity.getItemType() == TodoAdapter.TYPE_TODO) {
                    showLongClickDialog(view, position, (TodoTypeBean.TodoListBean.TodoBean) entity);
                    return true;
                }
                return false;
            }
        });
    }



    public void onClickHomeFab(View view) {
        if (App.Login.isOK()) {
            showEnterPost(null);
        } else {
            LoginDialogActivity.launch(this);
        }
    }

    private void showEnterPost(TodoTypeBean.TodoListBean.TodoBean todoBean) {
        mFragmentContainerView.setVisibility(View.VISIBLE);
        mFab.hide();
        PostFragment fragment = (PostFragment) getSupportFragmentManager().findFragmentByTag("PostFragment");
        if (fragment == null) {
            if (todoBean != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("todo_bean", todoBean);
                mPostFragment.setArguments(bundle);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, mPostFragment, "PostFragment")
                    .commit();
        } else {
            if (todoBean != null) {
                fragment.handleArgument(todoBean);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(fragment)
                    .commit();
        }
    }

    @Override
    public void displayList(TodoTypeBean todoTypeBean) {
        mRefresh.setRefreshing(false);

        mTypeBeanMap.put(todoTypeBean.getType(), todoTypeBean);
        List<MultiItemEntity> src = new ArrayList<>();
        src.add(todoTypeBean);
        todoTypeBean.setTitle(getString(R.string.ing));
        todoTypeBean.setSubItems(todoTypeBean.getTodoList());
        for (TodoTypeBean.TodoListBean todoListBean : todoTypeBean.getTodoList()) {
            todoListBean.setSubItems(todoListBean.getTodoList());
        }

        TodoTypeBean doneBean = new TodoTypeBean();
        doneBean.setTitle(getString(R.string.finished));
        doneBean.setType(todoTypeBean.getType());
        doneBean.setDoneList(todoTypeBean.getDoneList());
        doneBean.setSubItems(todoTypeBean.getDoneList());
        src.add(doneBean);
        for (TodoTypeBean.TodoListBean todoListBean : doneBean.getDoneList()) {
            todoListBean.setSubItems(todoListBean.getTodoList());
        }

        mTodoAdapter.setNewData(src);
        mTodoAdapter.expandAll();

        if (todoTypeBean.getTodoList().size() > 0) {
            mTodoListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int num = PrefHelper.getInt("guide_long_click_num");
                    if (num > 2) return;
                    View view = mTodoAdapter.getViewByPosition(mTodoListView, 3, R.id.text);
                    new OperatingGuideDialog(MainActivity.this)
                            .setText(getString(R.string.long_click_can_edit_or_delete))
                            .setClickedView(view)
                            .setOffsetY(Util.dpToPx(MainActivity.this, -8))
                            .show();
                    PrefHelper.set("guide_long_click_num", num + 1);
                }
            }, 400L);
        }
    }

    @Override
    public void showChooseTodoCategory() {
        if (mBubbleDialog == null) {
            mBubbleDialogView = LayoutInflater.from(this).inflate(R.layout.layout_choose_category, null);
            mBubbleDialog = new BubbleDialog(this)
                    .setPosition(BubbleDialog.Position.BOTTOM)
                    .addContentView(mBubbleDialogView)
                    .setRelativeOffset(-24)
                    .setBubbleLayout(BubbleCreator.get(this));
            if (mBubbleDialog.getWindow() != null)
                mBubbleDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            mBubbleDialogView.findViewById(R.id.btnAbout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideChooseTodoCategory();
                    AboutActivity.launch(MainActivity.this);
                }
            });
            mBubbleDialogView.findViewById(R.id.btnAbout).setVisibility(View.VISIBLE);
        }

        RadioGroup group = mBubbleDialogView.findViewById(R.id.rgGroup);
        switch (mCategory) {
            case MainContract.CATEGORY_USE_ONE:
                group.check(R.id.rbUseOne);
            break;
            case MainContract.CATEGORY_WORK:
                group.check(R.id.rbWork);
                break;
            case MainContract.CATEGORY_LEARN:
                group.check(R.id.rbLearn);
                break;
            case MainContract.CATEGORY_LIFE:
                group.check(R.id.rbLife);
                break;
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                hideChooseTodoCategory();
                switch (checkedId) {
                    case R.id.rbUseOne:
                        onRefresh(MainContract.CATEGORY_USE_ONE);
                        break;
                    case R.id.rbWork:
                        onRefresh(MainContract.CATEGORY_WORK);
                        break;
                    case R.id.rbLearn:
                        onRefresh(MainContract.CATEGORY_LEARN);
                        break;
                    case R.id.rbLife:
                        onRefresh(MainContract.CATEGORY_LIFE);
                        break;
                }
            }
        });
        mBubbleDialog.setClickedView(mHeadView);
        mBubbleDialog.show();
    }

    @Override
    public void hideChooseTodoCategory() {
        mBubbleDialog.dismiss();
    }

    @Override
    public void showDeleteTip(final int position, final TodoTypeBean.TodoListBean.TodoBean todoBean) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(todoBean.getTitle())
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTodoAdapter.remove(position);
                        presenter.requestDelTodo(todoBean.getId());
                    }
                })
                .show();
    }

    @Override
    public void showLongClickDialog(android.view.View clickView, final int position, final TodoTypeBean.TodoListBean.TodoBean todoBean) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.layout_choose_todo_operate, null);
        final BubbleDialog bubbleDialog = new BubbleDialog(this)
                .setClickedView(clickView)
                .addContentView(contentView)
                .setRelativeOffset(-16)
                .setBubbleLayout(BubbleCreator.get(this))
                .setPosition(BubbleDialog.Position.TOP, BubbleDialog.Position.BOTTOM);
        contentView.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bubbleDialog.dismiss();
                showDeleteTip(position, todoBean);
            }
        });

        contentView.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bubbleDialog.dismiss();
                showEnterPost(todoBean);
            }
        });

        bubbleDialog.show();
    }

    @Override
    public void guideLogin() {
        new OperatingGuideDialog(this).setText(getString(R.string.click_this_can_login)).setClickedView(mFab).show();
    }

    @Override
    public void onRefresh() {
        presenter.requestTodo(mCategory, mRefresh);
    }

    public void onRefresh(int category) {
        mRefresh.setRefreshing(true);
        mCategory = category;
        switch (mCategory) {
            case MainContract.CATEGORY_USE_ONE:
                mHeadViewText.setText(R.string.use_one);
                break;
            case MainContract.CATEGORY_WORK:
                mHeadViewText.setText(R.string.work);
                break;
            case MainContract.CATEGORY_LEARN:
                mHeadViewText.setText(R.string.learn);
                break;
            case MainContract.CATEGORY_LIFE:
                mHeadViewText.setText(R.string.life);
                break;
        }
        onRefresh();
    }

    @Override
    public void onBackPressed() {
        if (mPostFragment.isVisible()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mPostFragment)
                    .commit();
            mFab.show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginDialogActivity.ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mRefresh.setRefreshing(true);
            onRefresh();
        }
    }
}
