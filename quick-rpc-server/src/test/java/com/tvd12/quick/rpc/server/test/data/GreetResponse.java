package com.tvd12.quick.rpc.server.test.data;

import com.tvd12.ezyfox.binding.annotation.EzyObjectBinding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EzyObjectBinding(read = false)
public class GreetResponse {
    protected String message;
}
