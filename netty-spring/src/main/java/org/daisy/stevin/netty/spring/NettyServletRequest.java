package org.daisy.stevin.netty.spring;

import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.security.Principal;
import java.util.*;

public class NettyServletRequest implements HttpServletRequest {
    private static final String FORM = "application/x-www-form-urlencoded";
    private final String requestURI;
    private final String queryString;
    private final Map<String, String[]> parameters;
    private final ServletContext servletContext;
    private final String method;
    private final byte[] contentArray;
    private final String contentType;
    private final HttpSession httpSession;

    private Map<String, Object> attributes = new HashMap<>();
    private ServletInputStream servletInputStream;
    private BufferedReader reader;
    private Map<String, List<Object>> header = new HashMap<>();
    private AsyncContext asyncContext;
    private String characterEncoding = "UTF-8";

    public NettyServletRequest(String uri, ServletContext servletContext, String method, byte[] contentArray, String contentType, HttpSession httpSession) {
        Objects.requireNonNull(uri, () -> "Request URI cannot be null");
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        this.requestURI = decoder.path();
        this.queryString = uri.substring(this.requestURI.length());

        Map<String, List<String>> params = decoder.parameters();
        parameters = new HashMap<>(params.size());
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> list = entry.getValue();
            parameters.put(entry.getKey(), list.toArray(new String[list.size()]));
        }

        this.servletContext = servletContext;
        this.method = method;
        this.contentType = contentType;
        this.httpSession = httpSession;
        this.contentArray = contentArray;
        tryInitParameters();
    }

    private void tryInitParameters() {
        if (this.contentType == null || this.contentArray == null) {
            return;
        }
        String[] split = contentType.split(";");
        boolean isForm = false;
        for (String s : split) {
            if (FORM.equals(s)) {
                isForm = true;
                break;
            }
        }
        if (isForm) {
            String content = new String(contentArray);
            QueryStringDecoder decoder = new QueryStringDecoder(content, false);
            Map<String, List<String>> params = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                List<String> list = entry.getValue();
                parameters.put(entry.getKey(), list.toArray(new String[list.size()]));
            }
        }
    }

    @Override
    public Object getAttribute(String name) {
        return name == null ? null : attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new Vector<>(attributes.keySet()).elements();
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        return this.contentArray == null ? -1 : this.contentArray.length;
    }

    @Override
    public long getContentLengthLong() {
        return this.contentArray == null ? -1 : this.contentArray.length;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (contentArray != null && reader == null && servletInputStream == null) {
            servletInputStream = new NettyServletInputStream(contentArray);
        }
        return servletInputStream;
    }

    @Override
    public String getParameter(String name) {
        if (name == null) {
            return null;
        }
        String[] values = parameters.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector<>(parameters.keySet()).elements();
    }

    @Override
    public String[] getParameterValues(String name) {
        return name == null ? null : parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public String getProtocol() {
        return HttpVersion.HTTP_1_1.text();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "getServerName";
    }

    @Override
    public int getServerPort() {
        return 8080;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (contentArray != null && servletInputStream == null && reader == null) {
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contentArray)));
        }
        return reader;
    }

    @Override
    public String getRemoteAddr() {
        return "getRemoteAddr";
    }

    @Override
    public String getRemoteHost() {
        return "getRemoteHost";
    }

    @Override
    public void setAttribute(String name, Object o) {
        if (name == null) {
            return;
        }
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return new Vector<>(Arrays.asList(getLocale())).elements();
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return String.format("getRealPath %s", path);
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "getLocalName";
    }

    @Override
    public String getLocalAddr() {
        return "getLocalAddr";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (asyncContext == null) {
            asyncContext = new NettyAsyncContext(servletRequest, servletResponse);
        }
        return asyncContext;
    }

    @Override
    public boolean isAsyncStarted() {
        if (asyncContext == null) {
            return false;
        }
        // first time it will not have ASYNC_REQUEST_URI attribute
        return getAttribute(AsyncContext.ASYNC_REQUEST_URI) == null;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return asyncContext == null ? null : DispatcherType.ASYNC;
    }

    @Override
    public String getAuthType() {
        return "getAuthType";
    }

    @Override
    public Cookie[] getCookies() {
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        List<Object> headers = this.header.get(name);
        return headers == null ? -1 : (long) headers.get(0);
    }

    @Override
    public String getHeader(String name) {
        Enumeration<String> headers = getHeaders(name);
        return headers != null && headers.hasMoreElements() ? headers.nextElement() : null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> list = new ArrayList<>();
        List<Object> headers = this.header.get(name);
        if (headers == null) {
            return new Vector<String>().elements();
        }
        for (Object obj : headers) {
            list.add(obj.toString());
        }
        return new Vector<>(list).elements();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new Vector<>(this.header.keySet()).elements();
    }

    @Override
    public int getIntHeader(String name) {
        List<Object> headers = this.header.get(name);
        return headers == null ? -1 : (int) headers.get(0);
    }

    public void addHeader(String name, Object value) {
        if (name == null) {
            return;
        }
        List<Object> values = this.header.computeIfAbsent(name, (key) -> new ArrayList<>());
        values.add(value);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (create) {
            return getSession();
        }
        return httpSession;
    }

    @Override
    public HttpSession getSession() {
        return httpSession;
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

}
