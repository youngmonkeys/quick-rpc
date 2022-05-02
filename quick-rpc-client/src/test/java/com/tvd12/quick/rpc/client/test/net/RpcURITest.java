package com.tvd12.quick.rpc.client.test.net;

import com.tvd12.quick.rpc.client.net.RpcSocketAddress;
import com.tvd12.quick.rpc.client.net.RpcURI;
import com.tvd12.test.base.BaseTest;
import org.testng.annotations.Test;

public class RpcURITest extends BaseTest {

    @Test
    public void fullTest() {
        // given
        String str = "quickrpc://user:pass@localhost:3005";

        // when
        RpcURI uri = new RpcURI(str);

        // then
        assert uri.getUsername().equals("user");
        assert uri.getPassword().equals("pass");
        assert uri.getSocketAddress().equals(new RpcSocketAddress("localhost", 3005));
    }

    @Test
    public void hasNoPortTest() {
        // given
        String str = "quickrpc://user:pass@localhost";

        // when
        RpcURI uri = new RpcURI(str);

        // then
        assert uri.getUsername().equals("user");
        assert uri.getPassword().equals("pass");
        assert uri.getSocketAddress().equals(new RpcSocketAddress("localhost", 3005));
    }

    @Test
    public void hasNoUserInfoTest() {
        // given
        String str = "quickrpc://localhost:3005";

        // when
        RpcURI uri = new RpcURI(str);

        // then
        assert uri.getUsername() == null;
        assert uri.getPassword() == null;
        assert uri.getSocketAddress().equals(new RpcSocketAddress("localhost", 3005));
    }

    @Test
    public void hasNoUserPasswordTest() {
        // given
        String str = "qkrpc://user@localhost:3005";

        // when
        RpcURI uri = new RpcURI(str);

        // then
        assert uri.getUsername().equals("user");
        assert uri.getPassword() == null;
        assert uri.getSocketAddress().equals(new RpcSocketAddress("localhost", 3005));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void invalidSchemeTest() {
        // given
        String str = "http://user@localhost:3005";

        // when
        // then
        new RpcURI(str);
    }

}
