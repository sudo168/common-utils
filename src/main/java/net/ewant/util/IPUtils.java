package net.ewant.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by admin on 2018/11/12.
 */
public class IPUtils {
    public static String getRequestIP(HttpServletRequest request) {
        String ipAddress = null;
        // ipAddress = request.getRemoteAddr();
        ipAddress = request.getHeader("x-real-ip");//nginx 反向代理配置
        //当反向代理服务器配置forwarded_for:no时,为unknown
        if (isEmptyIP(ipAddress)) {
            ipAddress = request.getHeader("x-forwarded-for");
            if (isEmptyIP(ipAddress)) {
                ipAddress = request.getHeader("proxy-client-ip");
                if (isEmptyIP(ipAddress)) {
                    ipAddress = request.getHeader("wl-proxy-client-ip");
                }
            }
        }

        if (isEmptyIP(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                // 根据网卡取本机配置的IP
                ipAddress = getLocalHost();
            }

        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    private static boolean isEmptyIP(String ip){
        return ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip);
    }

    public static String getLocalHost(){
        String host = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            host = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            try {
                InetAddress inetAddress = null;
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements() && host == null) {
                    NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements() && host == null) {
                        inetAddress = (InetAddress) inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            host = inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e1) {
                host = "127.0.0.1";
            }
        }
        return host;
    }

    /**
     * 通过ip获取地理位置
     * @param ip
     * @return
     * @throws Exception
     */
    public static String getIpInfo(String ip) {

        String path = "http://ip.taobao.com/service/getIpInfo.php?ip=";
        String returnStr = HttpClientUtils.doGet(path + ip);

        if(returnStr != null){

            JSONObject returnJson = JSON.parseObject(returnStr);

            if("0".equals(returnJson.get("code").toString())){

                JSONObject json = returnJson.getJSONObject("data");

                StringBuffer buffer = new StringBuffer();
                buffer.append(" ");
                buffer.append(json.getString("country"));//国家
                buffer.append(" ");
                buffer.append(json.getString("area"));//地区 华东、华南、华中、华北 ...
                buffer.append(" ");
                buffer.append(json.getString("region"));//省份
                buffer.append(" ");
                buffer.append(json.getString("city"));//市区
                buffer.append(" ");
                buffer.append(json.getString("county"));//地区
                buffer.append(" ");
                buffer.append(json.getString("isp"));//ISP公司

                return buffer.toString().trim();

            }else{
                return "获取地址失败 : "+returnStr;
            }

        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(getIpInfo("183.14.31.198"));
    }
}
