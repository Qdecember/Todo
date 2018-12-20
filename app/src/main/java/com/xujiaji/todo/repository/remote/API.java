package com.xujiaji.todo.repository.remote;

import com.xujiaji.todo.repository.bean.DailyBean;
import com.xujiaji.todo.repository.bean.LicenseBean;
import com.xujiaji.todo.repository.bean.Result;
import com.xujiaji.todo.repository.bean.TodoTypeBean;
import com.xujiaji.todo.repository.bean.UserBean;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * author: xujiaji
 * created on: 2018/10/9 18:25
 * description:
 */
public interface API {
    /**
     * 登录
     */
    @POST("user/login")
    @FormUrlEncoded
    Call<Result<UserBean>> postLogin(@Field("username") String username, @Field("password") String password);

    /**
     * 注册
     */
    @POST("user/register")
    @FormUrlEncoded
    Call<Result<UserBean>> postRegister(@Field("username") String username, @Field("password") String password, @Field("repassword") String repassword);

    /**
     * 清单
     */
    @GET("/lg/todo/list/{type}/json")
    Call<Result<TodoTypeBean>> getTodoList(@Path("type") int type);

    /**
     * 更新清单
     */
    @POST("lg/todo/update/{id}/json")
    @FormUrlEncoded
    Call<Result> postUpdateTodo(@Path("id")               int id,
                                @Field("title")     String title,
                                @Field("content") String content,
                                @Field("date")       String date,
                                @Field("status")      int status,
                                @Field("type")          int type,
                                @Field("priority")  int priority);

    /**
     * 新增一个todo
     */
    @POST("lg/todo/add/json")
    @FormUrlEncoded
    Call<Result> postAddTodo(
            @Field("title")     String title,
            @Field("content") String content,
            @Field("date")       String date,
            @Field("type")          int type,
            @Field("priority")  int priority);

    /**
     * 删除一个todo
     */
    @POST("lg/todo/delete/{id}/json")
    Call<Result> postDelTodo(@Path("id") int id);

    /**
     *  获取项目中使用的开源库信息
     */
    @GET
    Call<Result<List<LicenseBean>>> getLicenses(@Url String url);

    /**
     * 获取每日
     */
    @GET
    Call<Result<Map<String, DailyBean>>> getDailyList(@Url String url);
}
