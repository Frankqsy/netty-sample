package org.daisy.stevin.netty.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.WebUtils;

import javax.activation.FileTypeMap;
import javax.servlet.*;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.descriptor.JspConfigDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class NettyServletContext implements ServletContext {
    private final Log logger = LogFactory.getLog(getClass());
    /**
     * Default Servlet name used by Tomcat, Jetty, JBoss, and GlassFish: {@value}.
     */
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";
    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";
    private String servletContextName = this.getClass().getSimpleName();
    private Map<String, ServletRegistration> servletRegistrations = new HashMap<>();
    private Map<String, String> initParameters = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();
    private final ResourceLoader resourceLoader;
    private final String resourceBasePath;
    private String contextPath = "";
    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
    private final Map<String, ServletContext> contexts = new HashMap<String, ServletContext>();
    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<String, RequestDispatcher>();
    private final Set<String> declaredRoles = new LinkedHashSet<String>();

    public NettyServletContext() {
        this("", null);
    }

    public NettyServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    public NettyServletContext(ResourceLoader resourceLoader) {
        this("", resourceLoader);
    }

    public NettyServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");

        // Use JVM temp dir as ServletContext temp dir.
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
        }

        registerNamedDispatcher(this.defaultServletName, new NettyRequestDispatcher(this.defaultServletName));
    }

    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        Assert.notNull(requestDispatcher, "RequestDispatcher must not be null");
        this.namedRequestDispatchers.put(name, requestDispatcher);
    }

    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = (contextPath != null ? contextPath : "");
    }

    @Override
    public ServletContext getContext(String uripath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    @Override
    public int getMajorVersion() {
        return 3;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 3;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(file);
        return ("application/octet-stream".equals(mimeType) ? null : mimeType);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        Resource resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
        try {
            File file = resource.getFile();
            String[] fileList = file.list();
            if (ObjectUtils.isEmpty(fileList)) {
                return null;
            }
            Set<String> resourcePaths = new LinkedHashSet<String>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                if (resource.createRelative(fileEntry).getFile().isDirectory()) {
                    resultPath += "/";
                }
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        } catch (IOException ex) {
            logger.warn("Couldn't get resource paths for " + resource, ex);
            return null;
        }
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getURL();
        } catch (MalformedURLException ex) {
            throw ex;
        } catch (IOException ex) {
            logger.warn("Couldn't get URL for " + resource, ex);
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getInputStream();
        } catch (IOException ex) {
            logger.warn("Couldn't open InputStream for " + resource, ex);
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
        }
        return new NettyRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return this.namedRequestDispatchers.get(name);
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getServletNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String msg) {
        logger.info(msg);
    }

    @Override
    public void log(Exception exception, String msg) {
        logger.info(msg, exception);
    }

    @Override
    public void log(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        try {
            return resource.getFile().getAbsolutePath();
        } catch (IOException ex) {
            logger.warn("Couldn't determine real path of resource " + resource, ex);
            return null;
        }
    }

    @Override
    public String getServerInfo() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getInitParameter(String name) {
        return name == null ? null : initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return new Vector<>(initParameters.keySet()).elements();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (name == null || initParameters.containsKey(name)) {
            return false;
        }
        initParameters.put(name, value);
        return true;
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
    public void setAttribute(String name, Object object) {
        if (name == null) {
            return;
        }
        if (object == null) {
            removeAttribute(name);
            return;
        }
        attributes.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        if (name == null) {
            return;
        }
        attributes.remove(name);
    }
    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }
    @Override
    public String getServletContextName() {
        return this.servletContextName;
    }

    @Override
    public Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return servletName == null ? null : servletRegistrations.get(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return new HashMap<>(servletRegistrations);
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void declareRoles(String... roleNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVirtualServerName() {
        throw new UnsupportedOperationException();
    }
    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

}
