package com.bi_side.CodeSnap.api.web;

import com.bi_side.CodeSnap.config.aop.SkipApiResponse;
import com.bi_side.CodeSnap.config.util.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestApiController {

    @GetMapping("/test")
    public ApiResponse<List<String>> test01() {
        ApiResponse<List<String>> response = new ApiResponse<>();
        List<String> test = new ArrayList<>();
        test.add("test01");
        test.add("test02");

        response.setData(test);

        return response;
    }

    @GetMapping("/test02")
    public Object test02() {
        HashMap<String,String> obj01 = new HashMap<>();
        obj01.put("test01","test01");
        HashMap<String,String> obj02 = new HashMap<>();
        obj02.put("test02","test02");
        obj02.put("test02_1","test02_1");

        List<Map<String,String>> returnObj = new ArrayList<>();
        returnObj.add(obj01);
        returnObj.add(obj02);

        return returnObj;
    }

    @GetMapping("/test-exception")
    public Object test03() throws Exception {
        throw new Exception("TEST");
    }

    @SkipApiResponse
    @GetMapping("/test-skip")
    public String test04() throws Exception {
        return "test";
    }
}
