package org.daisy.stevin.netty.spring;

import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NettyHttpServletRequest implements HttpServletRequest {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    public static final String DEFAULT_PROTOCOL = HTTP;
    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";
    public static final String DEFAULT_SERVER_NAME = "localhost";
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";
    public static final String DEFAULT_REMOTE_HOST = "localhost";
    private static final String HOST_HEADER = "Host";
    private static final String CHARSET_PREFIX = "charset=";
    private static final String FORM = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private final Map<String, String[]> parameters = new LinkedHashMap<String, String[]>();
    private final ServletContext servletContext;
    private final String method;
    private String requestURI;
    private String queryString;
    private byte[] contentArray;
    private String contentType;
    private HttpSession httpSession;

    private String characterEncoding = "UTF-8";
    private String scheme = DEFAULT_PROTOCOL;
    private String serverName = DEFAULT_SERVER_NAME;
    private int serverPort = DEFAULT_SERVER_PORT;
    private String remoteAddr = DEFAULT_REMOTE_ADDR;
    private String remoteHost = DEFAULT_REMOTE_HOST;
    private final List<Locale> locales = new LinkedList<Locale>();
    private boolean secure = false;
    private int remotePort = DEFAULT_SERVER_PORT;
    private String localName = DEFAULT_SERVER_NAME;
    private String localAddr = DEFAULT_SERVER_ADDR;
    private int localPort = DEFAULT_SERVER_PORT;
    private boolean asyncSupported = false;
    private String authType;
    private Cookie[] cookies;
    private String pathInfo;
    private String contextPath = "";
    private String remoteUser;
    private final Set<String> userRoles = new HashSet<String>();
    private Principal userPrincipal;
    private String requestedSessionId;
    private String servletPath = "";
    private HttpSession session;
    private boolean requestedSessionIdValid = true;
    private boolean requestedSessionIdFromCookie = true;
    private boolean requestedSessionIdFromURL = false;
    private final MultiValueMap<String, Part> parts = new LinkedMultiValueMap<String, Part>();

    private Map<String, Object> attributes = new HashMap<>();
    private ServletInputStream servletInputStream;
    private BufferedReader reader;
    private Map<String, NettyHeaderValueHolder> headers = new HashMap<>();
    private AsyncContext asyncContext;

    public NettyHttpServletRequest() {
        this(null, "", "");
    }


    public NettyHttpServletRequest(String method, String requestURI) {
        this(null, method, requestURI);
    }


    public NettyHttpServletRequest(ServletContext servletContext) {
        this(servletContext, "", "");
    }


    public NettyHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
        this.servletContext = (servletContext != null ? servletContext : new NettyServletContext());
        this.method = method;
        this.requestURI = requestURI;
        this.locales.add(Locale.ENGLISH);
    }

    public NettyHttpServletRequest(String uri, ServletContext servletContext, String method, byte[] contentArray, String contentType, HttpSession httpSession) {
        this(servletContext, method, "");
        Objects.requireNonNull(uri, () -> "Request URI cannot be null");
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        this.requestURI = decoder.path();
        this.queryString = uri.substring(this.requestURI.length());

        Map<String, List<String>> params = decoder.parameters();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> list = entry.getValue();
            parameters.put(entry.getKey(), list.toArray(new String[list.size()]));
        }

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
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }

    @Override
    public int getContentLength() {
        return this.contentArray == null ? -1 : this.contentArray.length;
    }

    @Override
    public long getContentLengthLong() {
        return this.contentArray == null ? -1 : this.contentArray.length;
    }

    private void updateContentTypeHeader() {
        if (StringUtils.hasLength(this.contentType)) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) &&
                    StringUtils.hasLength(this.characterEncoding)) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                if (mediaType.getCharset() != null) {
                    this.characterEncoding = mediaType.getCharset().name();
                }
            } catch (Exception ex) {
                // Try to get charset value anyway
                int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
                if (charsetIndex != -1) {
                    this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                }
            }
            updateContentTypeHeader();
        }
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
        return this.scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getServerName() {
        String host = getHeader(HOST_HEADER);
        if (host != null) {
            host = host.trim();
            if (host.startsWith("[")) {
                host = host.substring(1, host.indexOf(']'));
            } else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }
            return host;
        }

        // else
        return this.serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int getServerPort() {
        String host = getHeader(HOST_HEADER);
        if (host != null) {
            host = host.trim();
            int idx;
            if (host.startsWith("[")) {
                idx = host.indexOf(':', host.indexOf(']'));
            } else {
                idx = host.indexOf(':');
            }
            if (idx != -1) {
                return Integer.parseInt(host.substring(idx + 1));
            }
        }

        // else
        return this.serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
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
        return this.remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public void setAttribute(String name, Object o) {
        if (o != null) {
            this.attributes.put(name, o);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (name != null) {
            this.attributes.remove(name);
        }
    }

    @Override
    public Locale getLocale() {
        return this.locales.get(0);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    @Override
    public boolean isSecure() {
        return (this.secure || HTTPS.equalsIgnoreCase(this.scheme));
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new NettyRequestDispatcher(path);
    }

    @Override
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
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
        if (!this.asyncSupported) {
            throw new IllegalStateException("Async not supported");
        }
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
        return this.asyncSupported;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
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
        return this.authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    public void setCookies(Cookie... cookies) {
        this.cookies = cookies;
    }

    @Override
    public long getDateHeader(String name) {
        NettyHeaderValueHolder header = this.headers.get(name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Date) {
            return ((Date) value).getTime();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return parseDateHeader(name, (String) value);
        } else if (value != null) {
            throw new IllegalArgumentException(
                    "Value for header '" + name + "' is not a Date, Number, or String: " + value);
        } else {
            return -1L;
        }
    }

    private long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            } catch (ParseException ex) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    @Override
    public String getHeader(String name) {
        Enumeration<String> headers = getHeaders(name);
        return headers != null && headers.hasMoreElements() ? headers.nextElement() : null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        NettyHeaderValueHolder header = NettyHeaderValueHolder.getByName(this.headers, name);
        return Collections.enumeration(header != null ? header.getStringValues() : new LinkedList<String>());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return new Vector<>(this.headers.keySet()).elements();
    }

    @Override
    public int getIntHeader(String name) {
        NettyHeaderValueHolder header = NettyHeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value != null) {
            throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
        } else {
            return -1;
        }
    }

    public void addHeader(String name, Object value) {
        if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name) && !this.headers.containsKey(CONTENT_TYPE_HEADER)) {
            setContentType(value.toString());
        } else {
            doAddHeaderValue(name, value, false);
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        NettyHeaderValueHolder header = NettyHeaderValueHolder.getByName(this.headers, name);
        if (header == null || replace) {
            header = new NettyHeaderValueHolder();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addValues((Collection) value);
        } else if (value.getClass().isArray()) {
            header.addValueArray(value);
        } else {
            header.addValue(value);
        }
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || (this.servletContext instanceof NettyServletContext &&
                ((NettyServletContext) this.servletContext).getDeclaredRoles().contains(role)));
    }

    public void addUserRole(String role) {
        this.userRoles.add(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer(this.scheme).append("://").append(this.serverName);

        if (this.serverPort > 0
                && ((HTTP.equalsIgnoreCase(this.scheme) && this.serverPort != DEFAULT_SERVER_PORT) || (HTTPS.equalsIgnoreCase(this.scheme) && this.serverPort != 443))) {
            url.append(':').append(this.serverPort);
        }

        if (StringUtils.hasText(getRequestURI())) {
            url.append(getRequestURI());
        }

        return url;
    }

    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (this.session instanceof NettyHttpSession && ((NettyHttpSession) this.session).isInvalid()) {
            this.session = null;
        }
        // Create new session if necessary.
        if (this.session == null && create) {
            this.session = new NettyHttpSession(this.servletContext);
        }
        return this.session;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    public void setSession(HttpSession session) {
        this.session = session;
        if (session instanceof NettyHttpSession) {
            NettyHttpSession nettySession = ((NettyHttpSession) session);
            nettySession.access();
        }
    }

    @Override
    public String changeSessionId() {
        if (this.session instanceof NettyHttpSession) {
            return ((NettyHttpSession) session).changeSessionId();
        }
        return this.session.getId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
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
        this.userPrincipal = null;
        this.remoteUser = null;
        this.authType = null;
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        List<Part> result = new LinkedList<Part>();
        for (List<Part> list : this.parts.values()) {
            result.addAll(list);
        }
        return result;
    }

    public void addPart(Part part) {
        this.parts.add(part.getName(), part);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return this.parts.getFirst(name);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

}
