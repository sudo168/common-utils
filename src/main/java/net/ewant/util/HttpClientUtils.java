package net.ewant.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Created by huangzh on 2018/10/16.
 */
public class HttpClientUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);

    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    public static String doGet(String url, String... headerPairs) {
        CloseableHttpResponse response = null;
        String result = "";
        long start = System.currentTimeMillis();
        String exceptionMessage = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 为httpGet实例设置配置
            httpGet.setConfig(getRequestConfig());
            // 设置请求头
            addHeaders(httpGet, headerPairs);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
            LOGGER.error("", e);
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        LOGGER.info("GET url[{}] tick time[{}ms] response[{}] exception[{}]", url, System.currentTimeMillis() - start, result, exceptionMessage);
        return result;
    }

    public static String doPostJson(String url, Map<String, Object> paramMap){
        return doPost(url, paramMap, CONTENT_TYPE_HEADER_NAME, ContentType.APPLICATION_JSON.getMimeType());
    }

    public static String doPost(String url, Map<String, Object> paramMap, String... headerPairs) {
        return doPostOrPut(new HttpPost(url), paramMap, headerPairs);
    }

    public static String doPut(String url, Map<String, Object> paramMap, String... headerPairs) {
        return doPostOrPut(new HttpPut(url), paramMap, headerPairs);
    }

    private static String doPostOrPut(HttpEntityEnclosingRequestBase request, Map<String, Object> paramMap, String... headerPairs) {
        // 创建httpClient实例
        CloseableHttpResponse httpResponse = null;
        String result = "";

        // 为请求实例设置配置
        request.setConfig(getRequestConfig());
        // 设置请求头
        addHeaders(request, headerPairs);
        Header[] contentTypes = request.getHeaders(CONTENT_TYPE_HEADER_NAME);
        boolean isJson = false;
        if(contentTypes == null || contentTypes.length == 0){
            String[] defaultHeaders = new String[]{CONTENT_TYPE_HEADER_NAME, ContentType.APPLICATION_FORM_URLENCODED.getMimeType()};
            addHeaders(request, defaultHeaders);
        }else{
            isJson = contentTypes[0].getValue().startsWith(ContentType.APPLICATION_JSON.getMimeType());
        }
        // 封装post请求参数
        if (null != paramMap && paramMap.size() > 0) {
            if(isJson){
                request.setEntity(new StringEntity(JSON.toJSONString(paramMap), ContentType.APPLICATION_JSON));
            }else{
                List<NameValuePair> nvps = new ArrayList<>();
                Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();
                Iterator<Map.Entry<String, Object>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> mapEntry = iterator.next();
                    nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
                }
                request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            }
        }
        long start = System.currentTimeMillis();
        String exceptionMessage = "";
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(request);
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
            LOGGER.error("",e);
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        LOGGER.info("POST url[{}] tick time[{}ms] response[{}] exception[{}]", request.getURI(), System.currentTimeMillis() - start, result, exceptionMessage);
        return result;
    }

    private static void addHeaders(HttpMessage httpMessage, String... headerPairs){
        if(headerPairs != null){
            int length = headerPairs.length;
            if(length % 2 != 0){
                LOGGER.warn("Notice: headerPairs must be an even number length.");
            }
            for(int i=0; i < length; i++){
                httpMessage.addHeader(headerPairs[i], headerPairs[++i]);
            }
        }
        if(httpMessage.getFirstHeader(HTTP.USER_AGENT) == null){
            httpMessage.addHeader(HTTP.USER_AGENT,"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36 Apache HttpClient");
        }
    }

    private static RequestConfig getRequestConfig(){
        return RequestConfig.custom().setConnectTimeout(10000)// 设置连接超时时间，单位毫秒
                .setConnectionRequestTimeout(2000)// 设置从connect Manager获取Connection 超时时间，单位毫秒
                .setSocketTimeout(15000)// 设置读取数据超时时间，单位毫秒
                .build();
    }

    private static CloseableHttpClient httpClient;
    private static PoolingHttpClientConnectionManager connectManager;
    static{
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(createIgnoreVerifySSL(), new IgnoreHostnameVerifier()))
                .build();
        connectManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connectManager.setMaxTotal(50);// 连接池总大小
        connectManager.setDefaultMaxPerRoute(50);// 每个domain请求路由使用的最大线程数

        httpClient = config(null, null).build();
    }

    private static HttpClientBuilder config(HttpRequestInterceptor requestInterceptor, HttpResponseInterceptor responseInterceptor){
        return HttpClients.custom()
                .setConnectionManager(connectManager)
                .addInterceptorFirst(requestInterceptor)
                .addInterceptorFirst(responseInterceptor);
    }

    public static HttpClient interceptor(HttpRequestInterceptor requestInterceptor, HttpResponseInterceptor responseInterceptor){
        httpClient = config(requestInterceptor, responseInterceptor).build();
        return httpClient;
    }

    private static SSLContext createIgnoreVerifySSL() {
        try {
            SSLContext sc = SSLContext.getInstance("SSLv3");

            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                        String paramString) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sc.init(null, new TrustManager[] { trustManager }, null);

            return sc;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static class IgnoreHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

}
