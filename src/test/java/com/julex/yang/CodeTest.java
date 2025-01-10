package com.julex.yang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class CodeTest {

    private final OkHttpClient okHttpClient;

    public CodeTest() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .build();
    }

    private String reqStr   = "{\n" +
            "  \"body\": {\n" +
            "    \"bankCode\": \"2013\",\n" +
            "    \"channel\": \"cms\",\n" +
            "    \"group\": \"汇丰信用证查询\",\n" +
            "    \"seqNo\": \"TEST19800408080741\",\n" +
            "    \"taskName\": \"汇丰信用证查询_汇总\",\n" +
            "    \"pageNumber\":\"1\",\n" +
            "    \"pageSize\":\"100\",\n" +
            "    \"hsbcAccountCountry\": \"HK\",\n" +
            "    \"hsbcInstitutionCode\": \"HSBC\",\n" +
            "    \"hsbcAccountNumbers\": \"502689300095\"\n" +
            "  },\n" +
            "  \"head\": {\n" +
            "    \"channel\": \"cms\",\n" +
            "    \"seqNo\": \"API20170919104644\",\n" +
            "    \"service\": \"qBSmr\"\n" +
            "  }\n" +
            "}";

    /**
     * 通过callable实现并发请求
     *
     */
    @Test
    public void doReqByCallable() throws  InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(500); // 创建一个线程池，最多3个线程
        List<Future<String>> futures = new ArrayList<>(); // 用于保存每个任务的 Future

        for (int i = 0; i < 100; i++) {
            String uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
            //callable实现
            /*Callable<String> callable = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "";
                }
            };*/

            Future<String> future = executorService.submit(
                    () -> {
                        System.out.println(uid);
                        JSONObject jsonObject = JSON.parseObject(reqStr);
                        jsonObject.getJSONObject("body").put("seqNo", uid);
                        jsonObject.getJSONObject("head").put("seqNo", uid);
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        RequestBody requestBody = RequestBody.create(com.alibaba.fastjson.JSON.toJSONString(jsonObject), JSON);

                        Request request = new Request.Builder()
                                .url("http://10.0.124.111:9005/bedc/inf.do")
                                .post(requestBody)
                                .build();
                        try (Response response = okHttpClient.newCall(request).execute()) {
                            if (response.isSuccessful() && response.body() != null) {
                                System.out.println(uid + ":" + "请求成功");
                                return uid + ":" + "请求成功";
                            } else {
                                System.out.println(uid + ":" + "请求失败" + response.code());
                                return uid + "请求失败" + ":" + response.code();
                            }
                        } catch (IOException e) {
                            System.out.println(uid + ":" + "请求失败" + e.getMessage());
                            return uid + "请求失败" + ":" + e.getMessage();
                        }
                    });
            //}).get();//get方法会阻塞请求

            futures.add(future);
        }
        executorService.shutdown(); // 关闭线程池
        // 等待所有任务执行完毕并获取结果
        for (Future<String> future : futures) {
            try {
                System.out.println(future.get());  // 获取并打印每个任务的返回值
            } catch (ExecutionException e) {
                e.printStackTrace(); // 捕获任务执行中的异常
            }
        }
    }

    /**
     * 通过runnable实现并发
     */
    @Test
    public void doReqByRunable()  {



        int n = 100;
        CountDownLatch latch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            String uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
            //runable实现
            /*Runnable runnable = new Runnable() {
                @Override
                public void run() {

                }
            };*/

            new Thread(() -> {
                System.out.println(uid);
                JSONObject jsonObject = JSON.parseObject(reqStr);
                jsonObject.getJSONObject("body").put("seqNo", uid);
                jsonObject.getJSONObject("head").put("seqNo", uid);
                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(com.alibaba.fastjson.JSON.toJSONString(jsonObject), JSON);

                Request request = new Request.Builder()
                        .url("http://10.0.124.111:9005/bedc/inf.do")
                        .post(requestBody)
                        .build();
                //同步请求
                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        System.out.println(uid + ":" + "请求成功");
                        latch.countDown();
                    } else {
                        System.out.println(uid + ":" + "请求失败" + response.code());
                        latch.countDown();
                    }
                } catch (IOException e) {
                    System.out.println(uid + ":" + "请求失败" + e.getMessage());
                    latch.countDown();
                }
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /***
     * 异步发起http请求
     */
    @Test
    public void doReq() {

        int n = 2000;
        CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            String uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
            System.out.println(uid);
            JSONObject jsonObject = JSON.parseObject(reqStr);
            jsonObject.getJSONObject("body").put("seqNo", uid);
            jsonObject.getJSONObject("head").put("seqNo", uid);
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(com.alibaba.fastjson.JSON.toJSONString(jsonObject), JSON);

            Request request = new Request.Builder()
                    .url("http://10.0.124.111:9005/bedc/inf.do")
                    .post(requestBody)
                    .build();
            //同步请求
            /*try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println(i + ":" + uid + ":" + "请求成功");
                } else {
                    System.out.println(i + ":" + uid + "请求失败" + ":" + response.code());
                }
            } catch (IOException e) {
                System.out.println(i + ":" + uid + "请求失败" + ":" + e.getMessage());
            }*/

            //异步请求
            // 创建 CountDownLatch，初始值为 1
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    if (response.isSuccessful()) {
                        System.out.println("异步请求发送成功" + response.body());
                    } else {
                        System.out.println("Unexpected code: " + response.code());
                    }
                    latch.countDown(); // 释放锁
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println("异步请求发送失败" + e.getMessage());
                    latch.countDown(); // 释放锁
                }
            });

        }
        // 主线程等待异步请求完成
        try {
            latch.await(); // 阻塞，直到 latch 的计数为 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
