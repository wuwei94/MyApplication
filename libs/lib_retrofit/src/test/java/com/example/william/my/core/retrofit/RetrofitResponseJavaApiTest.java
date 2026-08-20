package com.example.william.my.core.retrofit;

import static org.junit.Assert.assertEquals;

import com.example.william.my.core.retrofit.response.RetrofitResponse;

import org.junit.Test;

public class RetrofitResponseJavaApiTest {

    @Test
    public void factoriesAreAvailableAsJavaStaticMethods() {
        assertEquals(RetrofitResponse.LOADING, RetrofitResponse.loading().getCode());
        assertEquals(RetrofitResponse.SUCCESS, RetrofitResponse.success().getCode());
        assertEquals(RetrofitResponse.ERROR, RetrofitResponse.error("failed").getCode());
        assertEquals(10, RetrofitResponse.of(10, "custom", null).getCode());
    }
}
