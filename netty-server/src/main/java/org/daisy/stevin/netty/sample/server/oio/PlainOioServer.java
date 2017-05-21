package org.daisy.stevin.netty.sample.server.oio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class PlainOioServer {
    @SuppressWarnings("resource")
    public void serve(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        try {
            for (;;) {
                Socket clientSocket = socket.accept();
                dealConn(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dealConn(final Socket clientSocket) {
        System.out.println("Accepted connection from " + clientSocket);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream out;
                try {
                    out = clientSocket.getOutputStream();
                    out.write("Hi!\r\n".getBytes(Charset.forName("UTF-8")));
                    out.flush();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        // ignore on close
                    }
                }
            }
        }).start();
    }
}
