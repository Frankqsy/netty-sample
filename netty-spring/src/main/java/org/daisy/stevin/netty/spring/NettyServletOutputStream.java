package org.daisy.stevin.netty.spring;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import java.io.IOException;
import java.io.StringWriter;

public class NettyServletOutputStream extends ServletOutputStream {
    private StringWriter stringWriter;

    public NettyServletOutputStream() {
        this.stringWriter = new StringWriter();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int b) throws IOException {
        stringWriter.write(b);
    }

    public StringWriter getStringWriter() {
        return stringWriter;
    }

}
