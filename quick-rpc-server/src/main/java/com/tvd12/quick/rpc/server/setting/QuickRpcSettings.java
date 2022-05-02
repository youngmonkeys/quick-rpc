package com.tvd12.quick.rpc.server.setting;

import com.tvd12.ezyfox.builder.EzyBuilder;
import lombok.Getter;

import java.util.Properties;

@Getter
public class QuickRpcSettings {

    protected final String username;
    protected final String password;
    protected final String host;
    protected final int port;

    public static final String HOST = "quickrpc.server.host";
    public static final String PORT = "quickrpc.server.port";
    public static final String USERNAME = "quickrpc.server.username";
    public static final String PASSWORD = "quickrpc.server.password";

    protected QuickRpcSettings(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.password = builder.password;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements EzyBuilder<QuickRpcSettings> {

        protected String host = "0.0.0.0";
        protected int port = 3005;
        protected String username = "admin";
        protected String password = "admin";
        protected Properties properties;

        public Builder host(String host) {
            if (host != null) {
                this.host = host;
            }
            return this;
        }

        public Builder port(String port) {
            if (port != null) {
                this.port = Integer.parseInt(port);
            }
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            if (username != null) {
                this.username = username;
            }
            return this;
        }

        public Builder password(String password) {
            if (password != null) {
                this.password = password;
            }
            return this;
        }

        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        @Override
        public QuickRpcSettings build() {
            if (properties != null) {
                host(properties.getProperty(HOST));
                port(properties.getProperty(PORT));
                username(properties.getProperty(USERNAME));
                password(properties.getProperty(PASSWORD));
            }
            return new QuickRpcSettings(this);
        }
    }
}
