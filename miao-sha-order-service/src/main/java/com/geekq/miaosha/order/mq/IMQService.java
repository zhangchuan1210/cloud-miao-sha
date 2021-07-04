package com.geekq.miaosha.order.mq;

import com.alibaba.fastjson.JSONObject;

public interface IMQService {
    String send(Object... paramsJson);
    String receive(Object... paramsJson);

}
