package com.tvd12.quick.rpc.server.manager;

import com.tvd12.quick.rpc.server.entity.RpcSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcSessionManager {

    protected final Map<Object, RpcSession> sessions;

    public RpcSessionManager() {
        this.sessions = new ConcurrentHashMap<>();
    }

    public void addSession(RpcSession session) {
        this.sessions.put(session.getKey(), session);
    }

    public RpcSession removeSession(Object key) {
        return this.sessions.remove(key);
    }

    public RpcSession getSession(Object key) {
        return sessions.get(key);
    }

    public List<RpcSession> getSessionList() {
        return new ArrayList<>(sessions.values());
    }

    public void getSessions(List<RpcSession> buffer) {
        buffer.addAll(sessions.values());
    }

    public int getSessionCount() {
        return sessions.size();
    }
}
