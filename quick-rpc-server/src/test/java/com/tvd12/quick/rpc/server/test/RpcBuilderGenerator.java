package com.tvd12.quick.rpc.server.test;

import com.tvd12.ezyfox.tool.EzyBuilderCreator;
import com.tvd12.quick.rpc.server.setting.QuickRpcSettings;

public class RpcBuilderGenerator {

    public static void main(String[] args) throws Exception {
        System.out.println(new EzyBuilderCreator()
            .create(QuickRpcSettings.class));
    }
}
