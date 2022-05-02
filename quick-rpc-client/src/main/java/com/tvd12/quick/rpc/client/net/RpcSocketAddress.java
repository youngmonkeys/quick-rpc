package com.tvd12.quick.rpc.client.net;

import com.tvd12.ezyfox.util.EzyEquals;
import com.tvd12.ezyfox.util.EzyHashCodes;
import lombok.Getter;

@Getter
public class RpcSocketAddress {

    protected final String host;
    protected final int port;
    protected static final int DEFAULT_PORT = 3005;

    public RpcSocketAddress(String host, int port) {
        this.host = host;
        this.port = port > 0 ? port : DEFAULT_PORT;
    }

    @Override
    public boolean equals(Object obj) {
        return new EzyEquals<RpcSocketAddress>()
            .function(t -> t.host)
            .function(t -> t.port)
            .isEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return new EzyHashCodes()
            .append(host)
            .append(port)
            .toHashCode();
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
