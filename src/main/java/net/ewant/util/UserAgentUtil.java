package net.ewant.util;

import com.decibel.uasparser.OnlineUpdater;
import com.decibel.uasparser.UASparser;
import com.decibel.uasparser.UserAgentInfo;
import org.apache.http.protocol.HTTP;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2019/1/10.
 */
public class UserAgentUtil {

    private static Pattern MAC_VERSION_REG = Pattern.compile("OS ([0-9_]+)");

    private static UASparser parser;

    static {
        try {
            parser = new UASparser(OnlineUpdater.getVendoredInputStream());
        } catch (IOException e) {
        }
    }

    public static UserAgentInfo parse(HttpServletRequest request){
        try {
            /*System.out.println("操作系统名称："+userAgentInfo.getOsFamily());
            System.out.println("操作系统："+userAgentInfo.getOsName());
            System.out.println("浏览器名称："+userAgentInfo.getUaFamily());
            System.out.println("浏览器版本："+userAgentInfo.getBrowserVersionInfo());
            System.out.println("设备类型："+userAgentInfo.getDeviceType());
            System.out.println("浏览器:"+userAgentInfo.getUaName());
            System.out.println("类型："+userAgentInfo.getType());*/
            return parser.parse(request.getHeader(HTTP.USER_AGENT));
        } catch (IOException e) {
        }
        return null;
    }

    public static UserAgent getUserAgent(HttpServletRequest request) {
        String uAgent = request.getHeader(HTTP.USER_AGENT);
        UserAgent userAgent = getUserAgent(uAgent);
        if (userAgent != null) {
            String mac = request.getParameter("mac");//用户自定义
            userAgent.setClientMAC(mac);
            userAgent.setAppVersion(request.getParameter("appVersion"));// 用户自定义参数
            userAgent.setClientIP(IPUtils.getRequestIP(request));
            userAgent.setUserAgent(uAgent);
        }
        return userAgent;
    }
    /**
     * 用途：根据客户端 User Agent Strings 判断其浏览器、操作平台
     * if 判断的先后次序：
     * 根据设备的用户使用量降序排列，这样对于大多数用户来说可以少判断几次即可拿到结果：
     *  >>操作系统:Windows > 苹果 > 安卓 > Linux > ...
     *  >>Browser:Chrome > FF > IE > ...
     * @param userAgent
     * @return
     */
    private static UserAgent getUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() == 0) {
            return null;
        }

        if (userAgent.contains("Windows")) {//主流应用靠前  注意 Windows Phone OS
            /**
             * ******************
             * 台式机 Windows 系列
             * ******************
             * Windows NT 6.4 / 10.0   -   Windows 10
             * Windows NT 6.3   -   Windows 8.1
             * Windows NT 6.2   -   Windows 8
             * Windows NT 6.1   -   Windows 7
             * Windows NT 6.0   -   Windows Vista
             * Windows NT 5.2   -   Windows Server 2003; Windows XP x64 Edition
             * Windows NT 5.1   -   Windows XP
             * Windows NT 5.01  -   Windows 2000, Service Pack 1 (SP1)
             * Windows NT 5.0   -   Windows 2000
             * Windows NT 4.0   -   Microsoft Windows NT 4.0
             * Windows 98; Win 9x 4.90  -   Windows Millennium Edition (Windows Me)
             * Windows 98   -   Windows 98
             * Windows 95   -   Windows 95
             * Windows CE   -   Windows CE
             * 判断依据:http://msdn.microsoft.com/en-us/library/ms537503(v=vs.85).aspx
             */
            boolean containsWin64 = userAgent.contains("Windows NT 6.4");
            if (containsWin64 || userAgent.contains("Windows NT 10.0")) {//Windows 10
                return judgeBrowser(userAgent, "Windows", "10.0" , containsWin64 ? "6.4" : "10.0");//判断浏览器
            }else if (userAgent.contains("Windows NT 6.3")) {//Windows 8.1
                return judgeBrowser(userAgent, "Windows", "8.1" , "6.3");//判断浏览器
            }else if (userAgent.contains("Windows NT 6.2")) {//Windows 8
                return judgeBrowser(userAgent, "Windows", "8" , "6.2");//判断浏览器
            } else if (userAgent.contains("Windows NT 6.1")) {//Windows 7
                return judgeBrowser(userAgent, "Windows", "7" , "6.1");
            } else if (userAgent.contains("Windows NT 6.0")) {//Windows Vista
                return judgeBrowser(userAgent, "Windows", "Vista" , "6.0");
            } else if (userAgent.contains("Windows NT 5.2")) {//Windows XP x64 Edition
                return judgeBrowser(userAgent, "Windows", "XP" , "5.2 x64 Edition");
            } else if (userAgent.contains("Windows NT 5.1")) {//Windows XP
                return judgeBrowser(userAgent, "Windows", "XP" , "5.1");
            } else if (userAgent.contains("Windows NT 5.01")) {//Windows 2000, Service Pack 1 (SP1)
                return judgeBrowser(userAgent, "Windows", "2000" , "5.01 SP1");
            } else if (userAgent.contains("Windows NT 5.0")) {//Windows 2000
                return judgeBrowser(userAgent, "Windows", "2000" , "5.0");
            } else if (userAgent.contains("Windows NT 4.0")) {//Microsoft Windows NT 4.0
                return judgeBrowser(userAgent, "Windows", "NT 4.0" , "4.0");
            } else if (userAgent.contains("Windows 98; Win 9x 4.90")) {//Windows Millennium Edition (Windows Me)
                return judgeBrowser(userAgent, "Windows", "ME" , "9x 4.90");
            } else if (userAgent.contains("Windows 98")) {//Windows 98
                return judgeBrowser(userAgent, "Windows", "98" , "98");
            } else if (userAgent.contains("Windows 95")) {//Windows 95
                return judgeBrowser(userAgent, "Windows", "95" , "95");
            } else if (userAgent.contains("Windows CE")) {//Windows CE
                return judgeBrowser(userAgent, "Windows", "CE" , "CE");
            } else if (userAgent.contains("Windows Phone OS")) {//Windows Phone OS
                return judgeBrowser(userAgent, "Windows", "Windows Phone" , "Windows Phone");
            }
        } else if (userAgent.contains("Mac OS X") || userAgent.contains("iOS") || userAgent.contains("iPhone")) {
            /**
             * ********
             * 苹果系列
             * ********
             * iPod -       Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_1 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8G4 Safari/6533.18.5
             * iPad -       Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10
             * iPad2 -      Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3
             * iPhone 4 -   Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7
             * iPhone 5 -   Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3
             * 判断依据:http://www.useragentstring.com/pages/Safari/
             * 参考:http://stackoverflow.com/questions/7825873/what-is-the-ios-5-0-user-agent-string
             * 参考:http://stackoverflow.com/questions/3105555/what-is-the-iphone-4-user-agent
             */
            String version = "";
            Matcher matcher = MAC_VERSION_REG.matcher(userAgent);
            if(matcher.find()){
                version = matcher.group(1).replace("_",".");
            }
            if (userAgent.contains("iPod")) {
                return judgeBrowser(userAgent,"iOS", "iPod" , version);//判断浏览器
            } else if (userAgent.contains("iPad")) {
                return judgeBrowser(userAgent, "iOS","iPad" , version);//判断浏览器
            } else if (userAgent.contains("iPhone")) {
                return judgeBrowser(userAgent, "iOS","iPhone" , version);//判断浏览器
            } else {
                return judgeBrowser(userAgent, "Mac OS", "Mac OS" , version);//判断浏览器
            }
        } else if (userAgent.contains("Linux") || userAgent.contains("Android")) {
            /**
             * Mozilla/5.0 (Linux; U; Android 5.0.2; Redmi Note 3 MIUI/V7.2.5.0.LHNCNDA)
             * Mozilla/5.0 (Linux; Android 6.0; HUAWEI NXT-AL10 Build/HUAWEINXT-AL10; wv)
             * Mozilla/5.0 (Linux; U; Android 4.1.2; zh-cn; GT-I8190N Build/JZO54K)
             */
            String uAgent = userAgent.substring(userAgent.indexOf("(")+1, userAgent.indexOf(")"));
            String[] info = uAgent.split(";");
            String version = null;
            String series = null;
            int length = info.length;
            for (int i = 0; i < length; i++) {
                String data = info[i];
                if(data.contains("Android")){
                    version = data.trim().replace("Android", "").trim();
                    if (i+1 < length) {
                        String s = info[i+1];
                        if (s.contains("zh-") || s.contains("en-")) {
                            if (i+2 < length) {
                                series = info[i+2];
                            }
                        }else{
                            series = s;
                        }
                    }
                    break;
                }
            }
            return judgeBrowser(userAgent, "Android", series , version);//判断浏览器
        }
        return judgeBrowser(userAgent, userAgent,"" , "");//判断浏览器 ;
    }

    /**
     * 用途：根据客户端 User Agent Strings 判断其浏览器
     * if 判断的先后次序：
     * 根据浏览器的用户使用量降序排列，这样对于大多数用户来说可以少判断几次即可拿到结果：
     * 显示浏览器使用的主流渲染引擎有：Gecko、WebKit、Presto(opera)、Trident(IE)、KHTML、Tasman等，格式为：渲染引擎/版本信息
     *
     * Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0  火狐
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36  谷歌
     * 		PC端chrome浏览器的判断标准是chrome字段，chrome后面的数字为版本号；
     * 		移动端的chrome浏览器判断”android“、”linux“、”mobile safari“等字段，version后面的数字为版本号。
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12860.10 Safari/537.36  UC
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36   360（谷歌内核 ）
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2   Safari
     * 		由于Chrome及Nokia’s Series 60 browser也使用WebKit内核，因此Safari浏览器的判断必须是：包含safari字段，同时不包含chrome等信息，
     * 		确定 后”version/“后面的数字即为版本号。在以上条件下包含Mobile字段的即为移动设备上的Safari浏览器。
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 BIDUBrowser/7.6 Safari/537.36  百度
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Maxthon/4.4.8.1000 Chrome/30.0.1599.101 Safari/537.36  傲游
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36 OPR/37.0.2178.54 (Edition Baidu)  opera
     * Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11   opera
     * Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko LBBROWSER  猎豹
     * Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko Core/1.47.516.400 QQBrowser/9.4.8113.400  QQ
     * Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13F69 MicroMessenger/6.3.16 NetType/WIFI Language/zh_CN  微信
     * Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0 搜狗 （SE）
     * 		搜狗浏览器的判断标准是”SE“、”MetaSr“字段，版本号为SE后面的数字。
     * Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; TencentTraveler 4.0) 腾讯TT
     * 		腾讯浏览器的判断标准是”TencentTraveler“或者”QQBrowser“，TencentTraveler或QQBrowser后面的数字为版本号。
     * Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; The World) 世界之窗（The World） 3.x
     * Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; InfoPath.2; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; 360SE)  360(标准)
     * UCWEB7.0.2.37/28/999  手机UC

     * 【IE 系列】IEMobile
     * Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)
     * Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 6.1; WOW64; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)
     * Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)
     * Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E)
     * Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)
     * Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/7.0)
     * Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko  IE11  rv:10.0  表示IE11版本
     *
     * 		遨游、世界之窗、360浏览器、腾讯浏览器以及搜狗浏览器、Avant、Green Browser均采用IE的内核，因此IE浏览器判断的标准是”MSIE“字段，
     * 		MSIE字段后面的数字为版本号，但同时还需要判断不包 含”Maxthon“、”The world“、”360SE“、”TencentTraveler“、”SE“、”Avant“等字段（Green Browser没有明显标识）。
     * 		移动设备还需要判断IEMobile+版本号。
     * @param userAgent:user agent
     * @param platformType:平台
     * @param platformSeries:系列
     * @param platformVersion:版本
     * @return
     */
    private static UserAgent judgeBrowser(String userAgent, String platformType, String platformSeries, String platformVersion) {
        if (userAgent.contains("MicroMessenger")) {
            String temp = userAgent.substring(userAgent.indexOf("MicroMessenger/") + 15);
            String micMsgVersion = null;
            if (temp.indexOf(" ") < 0) {//temp形如"24.0.1295.0"
                micMsgVersion = temp;
            } else {//temp形如"24.0.1295.0 Safari/537.15"
                micMsgVersion = temp.substring(0, temp.indexOf(" "));
            }
            return new UserAgent("MicroMessenger", micMsgVersion, platformType, platformSeries, platformVersion);
        }else if (userAgent.contains("Chrome")) {
            /**
             * ***********
             * Chrome 系列
             * ***********
             * Chrome 24.0.1295.0   -   Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.15 (KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15
             * Chrome 24.0.1292.0   -   Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.14 (KHTML, like Gecko) Chrome/24.0.1292.0 Safari/537.14
             * Chrome 24.0.1290.1   -   Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.13 (KHTML, like Gecko) Chrome/24.0.1290.1 Safari/537.13
             * 判断依据:http://www.useragentstring.com/pages/Chrome/
             */
            String temp = userAgent.substring(userAgent.indexOf("Chrome/") + 7);//拿到User Agent String "Chrome/" 之后的字符串,结果形如"24.0.1295.0 Safari/537.15"或"24.0.1295.0"
            String chromeVersion = null;
            if (temp.indexOf(" ") < 0) {//temp形如"24.0.1295.0"
                chromeVersion = temp;
            } else {//temp形如"24.0.1295.0 Safari/537.15"
                chromeVersion = temp.substring(0, temp.indexOf(" "));
            }
            return new UserAgent("Chrome", chromeVersion, platformType, platformSeries, platformVersion);
        } else if (userAgent.contains("Firefox")) {
            /**
             * *******
             * FF 系列
             * *******
             * Firefox 16.0.1   -   Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1
             * Firefox 15.0a2   -   Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15.0a2
             * Firefox 15.0.2   -   Mozilla/5.0 (Windows NT 6.2; WOW64; rv:15.0) Gecko/20120910144328 Firefox/15.0.2
             * 判断依据:http://www.useragentstring.com/pages/Firefox/
             */
            String temp = userAgent.substring(userAgent.indexOf("Firefox/") + 8);//拿到User Agent String "Firefox/" 之后的字符串,结果形如"16.0.1 Gecko/20121011"或"16.0.1"
            String ffVersion = null;
            if (temp.indexOf(" ") < 0) {//temp形如"16.0.1"
                ffVersion = temp;
            } else {//temp形如"16.0.1 Gecko/20121011"
                ffVersion = temp.substring(0, temp.indexOf(" "));
            }
            return new UserAgent("Firefox", ffVersion, platformType, platformSeries, platformVersion);
        } else if (userAgent.contains("MSIE")) {
            /**
             * *******
             * IE 系列
             * *******
             * MSIE 10.0 -  Internet Explorer 10
             * MSIE 9.0 -   Internet Explorer 9
             * MSIE 8.0 -   Internet Explorer 8 or IE8 Compatibility View/Browser Mode
             * MSIE 7.0 -   Windows Internet Explorer 7 or IE7 Compatibility View/Browser Mode
             * MSIE 6.0 -   Microsoft Internet Explorer 6
             * MSIE 5.5 -   Microsoft Internet Explorer 5.5
             * 判断依据:http://msdn.microsoft.com/en-us/library/ms537503(v=vs.85).aspx
             */
            if (userAgent.contains("MSIE 10.0")) {//Internet Explorer 10
                return new UserAgent("Internet Explorer", "10", platformType, platformSeries, platformVersion);
            } else if (userAgent.contains("MSIE 9.0")) {//Internet Explorer 9
                return new UserAgent("Internet Explorer", "9", platformType, platformSeries, platformVersion);
            } else if (userAgent.contains("MSIE 8.0")) {//Internet Explorer 8
                return new UserAgent("Internet Explorer", "8", platformType, platformSeries, platformVersion);
            } else if (userAgent.contains("MSIE 7.0")) {//Internet Explorer 7
                return new UserAgent("Internet Explorer", "7", platformType, platformSeries, platformVersion);
            } else if (userAgent.contains("MSIE 6.0")) {//Internet Explorer 6
                return new UserAgent("Internet Explorer", "6", platformType, platformSeries, platformVersion);
            } else if (userAgent.contains("MSIE 5.5")) {//Internet Explorer 5.5
                return new UserAgent("Internet Explorer", "5.5", platformType, platformSeries, platformVersion);
            }
        }else if(userAgent.contains("Safari") && userAgent.contains("Version")){
            String temp = userAgent.substring(userAgent.indexOf("Version/") + 8);//拿到User Agent String "Version/" 之后的字符串,结果形如"24.0.1295.0 Safari/537.15"或"24.0.1295.0"
            String chromeVersion = null;
            if (temp.indexOf(" ") < 0) {//temp形如"24.0.1295.0"
                chromeVersion = temp;
            } else {//temp形如"24.0.1295.0 Safari/537.15"
                chromeVersion = temp.substring(0, temp.indexOf(" "));
            }
            return new UserAgent("Safari", chromeVersion, platformType, platformSeries, platformVersion);
        }else if(userAgent.contains("Opera") && userAgent.contains("Version")){
            String temp = userAgent.substring(userAgent.indexOf("Version/") + 8);//拿到User Agent String "Version/" 之后的字符串,结果形如"24.0.1295.0 Safari/537.15"或"24.0.1295.0"
            String chromeVersion = null;
            if (temp.indexOf(" ") < 0) {//temp形如"24.0.1295.0"
                chromeVersion = temp;
            } else {//temp形如"24.0.1295.0 Safari/537.15"
                chromeVersion = temp.substring(0, temp.indexOf(" "));
            }
            return new UserAgent("Opera", chromeVersion, platformType, platformSeries, platformVersion);
        } else {//暂时支持以上几个主流.其它浏览器,待续...
            return new UserAgent("", "", platformType, platformSeries, platformVersion);
        }
        return new UserAgent("", "", platformType, platformSeries, platformVersion);
    }

    public static class UserAgent {

        private Long uaId;//id
        private String clientIP;//客户端IP
        private String clientMAC;//客户端mac地址
        private String loginUser;//登录用户ID
        private Integer ipTimes;//此用户在当前IP登录的次数
        private Integer macTimes;//此用户在当前设备登录的次数
        private Integer status;//状态
        private String browserType;//浏览器类型
        private String browserVersion;//浏览器版本
        private String platformType;//平台类型
        private String platformSeries;//平台系列
        private String platformVersion;//平台版本
        private String appVersion;//app版本
        private String userAgent;//完整信息
        private Date createTime;
        private Date updateTime;

        private boolean isAndroid;
        private boolean isIOS;
        private boolean isWindows;
        private boolean isMacOs;
        private boolean isLinux;

        public UserAgent(){}

        public UserAgent(String browserType, String browserVersion,
                         String platformType, String platformSeries, String platformVersion){
            this.browserType = browserType;
            this.browserVersion = browserVersion;
            this.platformType = platformType;
            this.platformSeries = platformSeries;
            this.platformVersion = platformVersion;
            this.isAndroid = "Android".equals(platformType);// 多4次判断了...
            this.isIOS = "iOS".equals(platformType);
            this.isWindows = "Windows".equals(platformType);
            this.isMacOs = "Mac OS".equals(platformType);
            this.isLinux = "Linux".equals(platformType);
        }

        public Long getUaId() {
            return uaId;
        }

        public void setUaId(Long uaId) {
            this.uaId = uaId;
        }

        public String getClientIP() {
            return clientIP;
        }

        public void setClientIP(String clientIP) {
            this.clientIP = clientIP;
        }

        public String getClientMAC() {
            return clientMAC;
        }

        public void setClientMAC(String clientMAC) {
            this.clientMAC = clientMAC;
        }

        public String getLoginUser() {
            return loginUser;
        }

        public void setLoginUser(String loginUser) {
            this.loginUser = loginUser;
        }

        public Integer getIpTimes() {
            return ipTimes;
        }

        public void setIpTimes(Integer ipTimes) {
            this.ipTimes = ipTimes;
        }

        public Integer getMacTimes() {
            return macTimes;
        }

        public void setMacTimes(Integer macTimes) {
            this.macTimes = macTimes;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getBrowserType() {
            return browserType;
        }

        public void setBrowserType(String browserType) {
            this.browserType = browserType;
        }

        public String getBrowserVersion() {
            return browserVersion;
        }

        public void setBrowserVersion(String browserVersion) {
            this.browserVersion = browserVersion;
        }

        public String getPlatformType() {
            return platformType;
        }

        public void setPlatformType(String platformType) {
            this.platformType = platformType;
        }

        public String getPlatformSeries() {
            return platformSeries;
        }

        public void setPlatformSeries(String platformSeries) {
            this.platformSeries = platformSeries;
        }

        public String getPlatformVersion() {
            return platformVersion;
        }

        public void setPlatformVersion(String platformVersion) {
            this.platformVersion = platformVersion;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        public boolean isAndroid() {
            return isAndroid;
        }

        public boolean isIOS() {
            return isIOS;
        }

        public boolean isWindows() {
            return isWindows;
        }

        public boolean isMacOs() {
            return isMacOs;
        }

        public boolean isLinux() {
            return isLinux;
        }
    }
}
