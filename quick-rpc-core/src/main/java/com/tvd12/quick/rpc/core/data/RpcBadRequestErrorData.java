package com.tvd12.quick.rpc.core.data;

import com.tvd12.ezyfox.binding.annotation.EzyArrayBinding;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@EzyArrayBinding
@AllArgsConstructor
public class RpcBadRequestErrorData {

    protected final int code;
    protected final String message;
}
