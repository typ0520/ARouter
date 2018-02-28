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
import java.net.URISyntaxException;
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
                String pattern = mappingInfo.getName();
                Map<String, String> params = mapping.getPathMatcher().extractUriTemplateVariables(mappingInfo.getPatternsCondition().getFirstPattern(),uri.getPath());
                Map<String, String> resultMap = new HashMap<>(TextUtils.splitQueryParameters(uri));
                resultMap.putAll(params);

                String newQueryString = joinQueryString(resultMap);
                return transformUrl(uri,pattern,newQueryString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    private Uri transformUrl(Uri uri, String pattern, String newQueryString) throws URISyntaxException {
        URI u = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),uri.getPort(),uri.getPath(),newQueryString,uri.getFragment());
        String str = u.toString();
        int index = str.indexOf(u.getPath());
        StringBuilder sb = new StringBuilder();
        if (index > 0) {
            sb.append(str.substring(0,index));
        }
        sb.append(pattern);
        sb.append(str.substring(index + u.getPath().length()));
        return Uri.parse(sb.toString());
    }

    private synchronized void syncRoutes() {
        if (routesSize != WarehouseProxy.getRoutes().size()) {
            mapping.cleanup();
            Map<String, RouteMeta> routes = new HashMap<>(WarehouseProxy.getRoutes());
            for (Map.Entry<String, RouteMeta> entry : routes.entrySet()) {
                if (entry.getKey().contains("/{") && entry.getKey().contains("}")) {
                    mapping.registerMapping(RequestMappingInfo.paths(entry.getKey()).mappingName(entry.getKey()).build());
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
}