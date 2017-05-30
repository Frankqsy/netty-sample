package org.daisy.stevin.netty.spring;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Created by shaoyang.qi on 2017/5/30.
 */
public class NettyJspWriter extends JspWriter {

    private final HttpServletResponse response;

    private PrintWriter targetWriter;


    public NettyJspWriter(HttpServletResponse response) {
        this(response, null);
    }

    public NettyJspWriter(Writer targetWriter) {
        this(null, targetWriter);
    }


    public NettyJspWriter(HttpServletResponse response, Writer targetWriter) {
        super(DEFAULT_BUFFER, true);
        this.response = (response != null ? response : new NettyHttpServletResponse());
        if (targetWriter instanceof PrintWriter) {
            this.targetWriter = (PrintWriter) targetWriter;
        } else if (targetWriter != null) {
            this.targetWriter = new PrintWriter(targetWriter);
        }
    }

    /**
     * Lazily initialize the target Writer.
     */
    protected PrintWriter getTargetWriter() throws IOException {
        if (this.targetWriter == null) {
            this.targetWriter = this.response.getWriter();
        }
        return this.targetWriter;
    }


    @Override
    public void clear() throws IOException {
        if (this.response.isCommitted()) {
            throw new IOException("Response already committed");
        }
        this.response.resetBuffer();
    }

    @Override
    public void clearBuffer() throws IOException {
    }

    @Override
    public void flush() throws IOException {
        this.response.flushBuffer();
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public int getRemaining() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void newLine() throws IOException {
        getTargetWriter().println();
    }

    @Override
    public void write(char value[], int offset, int length) throws IOException {
        getTargetWriter().write(value, offset, length);
    }

    @Override
    public void print(boolean value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(char value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(char[] value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(double value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(float value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(int value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(long value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(Object value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(String value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void println() throws IOException {
        getTargetWriter().println();
    }

    @Override
    public void println(boolean value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(char value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(char[] value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(double value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(float value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(int value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(long value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(Object value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(String value) throws IOException {
        getTargetWriter().println(value);
    }

}
