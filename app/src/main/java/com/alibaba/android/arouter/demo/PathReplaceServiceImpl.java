package com.alibaba.android.arouter.demo;

import android.content.Context;
import android.net.Uri;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.core.WarehouseProxy;
import com.alibaba.android.arouter.exception.HandlerException;
import com.alibaba.android.arouter.exception.NoRouteFoundException;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.model.RouteMeta;
import com.alibaba.android.arouter.facade.service.PathReplaceService;
import com.alibaba.android.arouter.utils.Consts;
import com.alibaba.android.arouter.utils.TextUtils;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import router.mapping.RequestMapping;
import router.mapping.RequestMappingInfo;

/**
 * Created by tong on 2018/2/27.
 */
@Route(path = "/xxx/xxx") // 必须标明注解
public class PathReplaceServiceImpl implements PathReplaceService {
    private volatile int routesSize = 0;
    private RequestMapping mapping = new RequestMapping();

    @Override
    public void init(Context context) {

    }

    @Override
    public String forString(String path) {
        return path;
    }

    @Override
    public Uri forUri(Uri uri) {
        try {
            LogisticsCenter.completion(new Postcard(uri.getPath(),extractGroup(uri.getPath())));
        } catch (NoRouteFoundException e) {

        }
        syncRoutes();
        try {
            RequestMappingInfo mappingInfo = mapping.lookupHandlerMethod(uri.getPath());
            if (mappingInfo != null) {
                String pattern = mappingInfo.getPatternsCondition().getFirstPattern();
                Map<String, String> params = mapping.getPathMatcher().extractUriTemplateVariables(pattern,uri.getPath());
                Map<String, String> resultMap = new HashMap<>(TextUtils.splitQueryParameters(uri));
                resultMap.putAll(params);

                String newQueryString = joinQueryString(resultMap);
                URI u = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),uri.getPort(),pattern,newQueryString,uri.getFragment());
                String str = u.toString().replace(u.getPath(), URLDecoder.decode(u.getPath(),"utf-8"));
                return Uri.parse(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    private synchronized void syncRoutes() {
        if (routesSize != WarehouseProxy.getRoutes().size()) {
            mapping.cleanup();
            Map<String, RouteMeta> routes = new HashMap<>(WarehouseProxy.getRoutes());
            for (Map.Entry<String, RouteMeta> entry : routes.entrySet()) {
                if (entry.getKey().contains("/{") && entry.getKey().contains("}")) {
                    mapping.registerMapping(RequestMappingInfo.paths(entry.getKey()).build());
                }
            }
            routesSize = routes.size();
        }
    }

    /**
     * Extract the default group from path.
     */
    private static String extractGroup(String path) {
        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new HandlerException(Consts.TAG + "Extract the default group failed! There's nothing between 2 '/'!");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String joinQueryString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

//    private String toString(String scheme,
//                            String opaquePart,
//                            String authority,
//                            String userInfo,
//                            String host,
//                            int port,
//                            String path,
//                            String query,
//                            String fragment)
//    {
//        StringBuffer sb = new StringBuffer();
//        if (scheme != null) {
//            sb.append(scheme);
//            sb.append(':');
//        }
//        appendSchemeSpecificPart(sb, opaquePart,
//                authority, userInfo, host, port,
//                path, query);
//        appendFragment(sb, fragment);
//        return sb.toString();
//    }
//
//    // Quote any characters in s that are not permitted
//    // by the given mask pair
//    //
//    private static String quote(String s, long lowMask, long highMask) {
//        int n = s.length();
//        StringBuffer sb = null;
//        boolean allowNonASCII = ((lowMask & L_ESCAPED) != 0);
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//            if (c < '\u0080') {
//                if (!match(c, lowMask, highMask)) {
//                    if (sb == null) {
//                        sb = new StringBuffer();
//                        sb.append(s.substring(0, i));
//                    }
//                    appendEscape(sb, (byte)c);
//                } else {
//                    if (sb != null)
//                        sb.append(c);
//                }
//            } else if (allowNonASCII
//                    && (Character.isSpaceChar(c)
//                    || Character.isISOControl(c))) {
//                if (sb == null) {
//                    sb = new StringBuffer();
//                    sb.append(s.substring(0, i));
//                }
//                appendEncoded(sb, c);
//            } else {
//                if (sb != null)
//                    sb.append(c);
//            }
//        }
//        return (sb == null) ? s : sb.toString();
//    }
//
//
//    private void appendFragment(StringBuffer sb, String fragment) {
//        if (fragment != null) {
//            sb.append('#');
//            sb.append(quote(fragment, L_URIC, H_URIC));
//        }
//    }
//
//    private void appendSchemeSpecificPart(StringBuffer sb,
//                                          String opaquePart,
//                                          String authority,
//                                          String userInfo,
//                                          String host,
//                                          int port,
//                                          String path,
//                                          String query)
//    {
//        if (opaquePart != null) {
//            /* check if SSP begins with an IPv6 address
//             * because we must not quote a literal IPv6 address
//             */
//            if (opaquePart.startsWith("//[")) {
//                int end =  opaquePart.indexOf("]");
//                if (end != -1 && opaquePart.indexOf(":")!=-1) {
//                    String doquote, dontquote;
//                    if (end == opaquePart.length()) {
//                        dontquote = opaquePart;
//                        doquote = "";
//                    } else {
//                        dontquote = opaquePart.substring(0,end+1);
//                        doquote = opaquePart.substring(end+1);
//                    }
//                    sb.append (dontquote);
//                    sb.append(quote(doquote, L_URIC, H_URIC));
//                }
//            } else {
//                sb.append(quote(opaquePart, L_URIC, H_URIC));
//            }
//        } else {
//            appendAuthority(sb, authority, userInfo, host, port);
//            if (path != null)
//                sb.append(quote(path, L_PATH, H_PATH));
//            if (query != null) {
//                sb.append('?');
//                sb.append(quote(query, L_URIC, H_URIC));
//            }
//        }
//    }
}