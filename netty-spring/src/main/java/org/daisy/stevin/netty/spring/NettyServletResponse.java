package org.daisy.stevin.netty.spring;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class NettyServletResponse implements HttpServletResponse {
    private int status = SC_OK;
    private Map<String, List<Object>> header = new HashMap<>();
    private String contentType = "text/html;charset=UTF-8";
    private boolean committed = false;
    private NettyServletOutputStream servletOutputStream;
    private String charset;
    private int bufferSize;
    private Integer errorStatusCode;
    private String errorMessage;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @Override
    public String getCharacterEncoding() {
        return charset;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void closeStreams() {
        if (servletOutputStream != null) {
            try {
                servletOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            servletOutputStream = new NettyServletOutputStream();
            stringWriter = servletOutputStream.getStringWriter();
        }
        return servletOutputStream;
    }

    public StringWriter getStringWriter() {
        return stringWriter;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (stringWriter == null) {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
        }
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentLengthLong(long len) {
        List<Object> col = new ArrayList<>();
        col.add(len);
        this.header.put("Content-Length", col);
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void flushBuffer() throws IOException {
        closeStreams();
        committed = true;
    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return committed;

    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return name == null ? false : this.header.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public String encodeUrl(String url) {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.errorStatusCode = sc;
        this.errorMessage = msg;
        setStatus(this.errorStatusCode);
    }

    public boolean isError() {
        return errorStatusCode != null;
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, null);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(SC_FOUND);
        setHeader("Location", location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        List<Object> col = new ArrayList<>();
        col.add(date);
        this.header.put(name, col);
    }

    @Override
    public void addDateHeader(String name, long date) {
        Collection<Object> col = this.header.get(name);
        if (col == null) {
            List<Object> list = new ArrayList<>();
            this.header.put(name, list);
            col = list;
        }
        col.add(date);
    }

    @Override
    public void setHeader(String name, String value) {
        List<Object> col = new ArrayList<>();
        col.add(value);
        this.header.put(name, col);
    }

    @Override
    public void addHeader(String name, String value) {
        Collection<Object> col = this.header.get(name);
        if (col == null) {
            List<Object> list = new ArrayList<>();
            this.header.put(name, list);
            col = list;
        }
        col.add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        List<Object> col = new ArrayList<>();
        col.add(value);
        this.header.put(name, col);
    }

    @Override
    public void addIntHeader(String name, int value) {
        Collection<Object> col = this.header.get(name);
        if (col == null) {
            List<Object> list = new ArrayList<>();
            this.header.put(name, list);
            col = list;
        }
        col.add(value);
    }

    @Override
    public void setStatus(int sc) {
        setStatus(sc, null);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getHeader(String name) {
        Collection<String> headers = getHeaders(name);
        if (headers != null && headers.iterator().hasNext()) {
            return headers.iterator().next();
        }
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> list = new ArrayList<>();
        Collection<Object> headers = this.header.get(name);
        if (headers != null) {
            for (Object obj : headers) {
                list.add(obj.toString());
            }
        }
        return list;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return new ArrayList<>(this.header.keySet());
    }

}
