package com.alibaba.android.arouter.demo;

import android.content.Context;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.service.DegradeService;

/**
 * Created by tong on 2018/2/26.
 */
@Route(path = "/xxx/xxx")
public class DegradeServiceImpl implements DegradeService {
    @Override
    public void onLost(Context context, Postcard postcard) {
        // do something.

        //syncRoutes();
        System.out.println();
    }

    @Override
    public void init(Context context) {

    }
}