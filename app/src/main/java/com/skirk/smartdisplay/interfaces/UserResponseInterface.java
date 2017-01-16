package com.skirk.smartdisplay.interfaces;

import com.skirk.smartdisplay.POJO.UserResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by skirk on 1/16/17.
 */

public interface UserResponseInterface {

    @FormUrlEncoded
    @POST("addUser.php")
    Call<UserResponse> createUser(
            @Field("first_name") String first,
            @Field("last_name") String last,
            @Field("image") String image,
            @Field("password") String password,
            @Field("email") String email
    );

}
