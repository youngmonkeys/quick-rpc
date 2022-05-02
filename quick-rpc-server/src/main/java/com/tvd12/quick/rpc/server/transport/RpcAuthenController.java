package com.tvd12.quick.rpc.server.transport;

import com.tvd12.ezyfox.io.EzyStrings;
import com.tvd12.ezyfoxserver.constant.EzyLoginError;
import com.tvd12.ezyfoxserver.context.EzyPluginContext;
import com.tvd12.ezyfoxserver.controller.EzyAbstractPluginEventController;
import com.tvd12.ezyfoxserver.event.EzyUserLoginEvent;
import com.tvd12.ezyfoxserver.exception.EzyLoginErrorException;
import com.tvd12.ezyfoxserver.setting.EzyAdminSetting;
import com.tvd12.ezyfoxserver.setting.EzyAdminsSetting;
import com.tvd12.ezyfoxserver.setting.EzySettings;

public class RpcAuthenController
    extends EzyAbstractPluginEventController<EzyUserLoginEvent> {

    @Override
    public void handle(EzyPluginContext ctx, EzyUserLoginEvent event) {
        EzySettings settings = ctx.getParent()
            .getParent()
            .getServer()
            .getSettings();
        EzyAdminsSetting adminsSetting = settings.getAdmins();
        if (EzyStrings.isNoContent(event.getUsername())) {
            throw new EzyLoginErrorException(EzyLoginError.INVALID_USERNAME);
        }
        if (EzyStrings.isNoContent(event.getPassword())) {
            throw new EzyLoginErrorException(EzyLoginError.INVALID_PASSWORD);
        }
        EzyAdminSetting adminSetting = adminsSetting.getAdminByName(event.getUsername());
        if (adminSetting == null) {
            throw new EzyLoginErrorException(EzyLoginError.INVALID_USERNAME);
        }
        if (!adminSetting.getPassword().equals(event.getPassword())) {
            throw new EzyLoginErrorException(EzyLoginError.INVALID_PASSWORD);
        }
    }
}
