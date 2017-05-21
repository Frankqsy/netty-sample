package org.daisy.stevin.netty.spring;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import java.io.IOException;

public class NettyServletInputStream extends ServletInputStream {
    private ByteBuf byteBuf;

    public NettyServletInputStream(byte[] content) {
        byteBuf = Unpooled.copiedBuffer(content);
    }

    @Override
    public boolean isFinished() {
        return byteBuf.readableBytes() <= 0;
    }

    @Override
    public boolean isReady() {
        return byteBuf.isReadable();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
        return byteBuf.isReadable() ? byteBuf.readByte() & 0xFF : -1;
    }

}
