package com.qcloud.cos.common_utils;

import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.UnknownException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class CommonPathUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CommonPathUtils.class);
    private static final String PATH_DELIMITER = "/";

    public static String encodeRemotePath(String urlPath) throws AbstractCosException {
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathSegment : urlPath.split(PATH_DELIMITER)) {
            if (!pathSegment.isEmpty()) {
                try {
                    pathBuilder.append(PATH_DELIMITER).append(URLEncoder.encode(pathSegment, "UTF-8").replace(Marker.ANY_NON_NULL_MARKER, "%20"));
                } catch (UnsupportedEncodingException e) {
                    String errMsg = "Unsupported ecnode exception:" + e.toString();
                    LOG.error(errMsg);
                    throw new UnknownException(errMsg);
                }
            }
        }
        if (urlPath.endsWith(PATH_DELIMITER)) {
            pathBuilder.append(PATH_DELIMITER);
        }
        return pathBuilder.toString();
    }
}
