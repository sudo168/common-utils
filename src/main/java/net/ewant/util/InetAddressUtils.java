package net.ewant.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by huangzh on 2018/10/30.
 */
public class InetAddressUtils {

    private InetAddressUtils() {
    }

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_STD_PATTERN =
            Pattern.compile(
                    "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
            Pattern.compile(
                    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    private static boolean isIPv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    private static boolean isIPv6HexCompressedAddress(final String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

    public static boolean isIPAddress(final String input) {
        return isIPv4Address(input) || isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

    public static List<String> getIpByDomain(final String domain){
        try {
            InetAddress[] ips= InetAddress.getAllByName("www.baidu.com");
            if(ips != null){
                List<String> ipList = new ArrayList<>();
                for(InetAddress ip: ips){
                    ipList.add(ip.getHostAddress());
                }
                return ipList;
            }
        } catch (UnknownHostException e) {}
        return null;
    }
}
