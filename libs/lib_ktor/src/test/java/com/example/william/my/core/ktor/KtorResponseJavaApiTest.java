package com.example.william.my.core.ktor;

import com.example.william.my.core.ktor.response.KtorResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KtorResponseJavaApiTest {

    @Test
    public void factoriesAreAvailableAsJavaStaticMethods() {
        assertEquals(KtorResponse.LOADING, KtorResponse.loading().getCode());
        assertEquals(KtorResponse.SUCCESS, KtorResponse.success().getCode());
        assertEquals(KtorResponse.ERROR, KtorResponse.error("failed").getCode());
        assertEquals(10, KtorResponse.of(10, "custom", null).getCode());
    }
}
