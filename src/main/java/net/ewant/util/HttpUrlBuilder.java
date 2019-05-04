package net.ewant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangzh on 2018/10/17.
 */
public class HttpUrlBuilder {

    protected static final Logger LOGGER = LoggerFactory.getLogger(HttpUrlBuilder.class);

    /** Pattern to detect unprintable ASCII characters. */
    private static final Pattern NON_PRINTABLE = Pattern.compile("[\\x00-\\x1F\\x7F]+");

    public static String buildUrl(final String url, String... keyValuePair){
        Map<String, String> parameters = new HashMap<>();
        if(keyValuePair != null){
            int length = keyValuePair.length;
            if(length % 2 == 0){
                for (int i = 0; i < length; i++) {
                    parameters.put(keyValuePair[i], keyValuePair[++i]);
                }
            }
        }
        return buildUrl(url, parameters);
    }

    public static String buildUrl(final String url, final Map<String, String> parameters){
        final StringBuilder builder = new StringBuilder();

        boolean isFirst = true;
        final String[] fragmentSplit = sanitizeUrl(url).split("#");

        builder.append(fragmentSplit[0]);

        if(parameters != null && !parameters.isEmpty()){
            for (final Map.Entry<String, String> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    if (isFirst) {
                        builder.append(url.contains("?") ? "&" : "?");
                        isFirst = false;
                    } else {
                        builder.append('&');
                    }
                    builder.append(entry.getKey());
                    builder.append('=');

                    try {
                        builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    } catch (final Exception e) {
                        builder.append(entry.getValue());
                    }
                }
            }
        }

        if (fragmentSplit.length > 1) {
            builder.append('#');
            builder.append(fragmentSplit[1]);
        }
        return builder.toString();
    }

    private static String sanitizeUrl(final String url) {
        final Matcher m = NON_PRINTABLE.matcher(url);
        final StringBuffer sb = new StringBuffer(url.length());
        boolean hasNonPrintable = false;
        while (m.find()) {
            m.appendReplacement(sb, " ");
            hasNonPrintable = true;
        }
        m.appendTail(sb);
        if (hasNonPrintable) {
            LOGGER.warn("The following redirect URL has been sanitized and may be sign of attack:\n{}", url);
        }
        return sb.toString();
    }
}
