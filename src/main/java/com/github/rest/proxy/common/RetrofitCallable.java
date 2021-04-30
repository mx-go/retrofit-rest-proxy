package com.github.rest.proxy.common;

import okhttp3.Request;
import retrofit2.Response;

/**
 * 请求回调
 * <p>
 * Create by max on 2021/04/28
 **/
public interface RetrofitCallable<R> {

    /**
     * 请求回调
     * 若 e!= null，这表示存在异常
     *
     * @param request  请求报文
     * @param response 响应报文
     * @param e        异常
     */
    void after(Request request, Response<R> response, Exception e);
}
