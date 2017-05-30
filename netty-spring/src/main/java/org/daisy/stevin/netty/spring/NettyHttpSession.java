package org.daisy.stevin.netty.spring;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import java.util.*;

public class NettyHttpSession implements HttpSession {
    private final long creationTime = System.currentTimeMillis();
    private Map<String, Object> attributes = new HashMap<>();
    private ServletContext servletContext;
    private static int nextId = 1;
    private String id;
    private boolean invalid = false;
    private long lastAccessedTime = System.currentTimeMillis();
    private boolean isNew = true;
    private int maxInactiveInterval;

    public NettyHttpSession(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public long getCreationTime() {
        assertIsValid();
        return this.creationTime;
    }

    private void assertIsValid() {
        if (isInvalid()) {
            throw new IllegalStateException("The session has already been invalidated");
        }
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String changeSessionId() {
        this.id = Integer.toString(nextId++);
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public long getLastAccessedTime() {
        assertIsValid();
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @SuppressWarnings("deprecation")
    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public Object getAttribute(String name) {
        assertIsValid();
        return name == null ? null : this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        assertIsValid();
        return new Vector<>(attributes.keySet()).elements();
    }

    @Override
    public String[] getValueNames() {
        assertIsValid();
        return this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        assertIsValid();
        if (name == null) {
            return;
        }
        if (value == null) {
            removeAttribute(name);
            return;
        }
        this.attributes.put(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        assertIsValid();
        if (name == null) {
            return;
        }
        this.attributes.remove(name);
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        assertIsValid();
        this.invalid = true;
        clearAttributes();
    }

    public void clearAttributes() {
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

}
