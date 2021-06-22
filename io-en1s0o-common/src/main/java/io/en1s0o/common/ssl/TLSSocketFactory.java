package io.en1s0o.common.ssl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * TLSSocketFactory
 *
 * @author En1s0o
 */
public class TLSSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;
    private final String[] enabledProtocols;

    private void enabledProtocols(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(enabledProtocols);
        }
    }

    public TLSSocketFactory(SSLSocketFactory delegate, String[] enabledProtocols) {
        this.delegate = delegate;
        this.enabledProtocols = enabledProtocols;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket ssl = delegate.createSocket();
        enabledProtocols(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        Socket ssl = delegate.createSocket(socket, host, port, autoClose);
        enabledProtocols(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket ssl = delegate.createSocket(host, port);
        enabledProtocols(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        Socket ssl = delegate.createSocket(host, port, localHost, localPort);
        enabledProtocols(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket ssl = delegate.createSocket(host, port);
        enabledProtocols(ssl);
        return ssl;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket ssl = delegate.createSocket(address, port, localAddress, localPort);
        enabledProtocols(ssl);
        return ssl;
    }

}
