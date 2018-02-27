package com.alibaba.android.arouter.core;

import com.alibaba.android.arouter.facade.model.RouteMeta;
import java.util.Map;

/**
 * Created by tong on 2018/2/27.
 */
public class WarehouseProxy {
    public static Map<String, RouteMeta> getRoutes() {
        return Warehouse.routes;
    }
}
