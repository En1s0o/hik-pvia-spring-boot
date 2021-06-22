package io.en1s0o.common.ssl;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * SSLHelper
 * <p>
 * SSL 助手
 *
 * @author En1s0o
 */
@Slf4j
public class SSLHelper {

    static class KeyAndTrustManagers {

        final KeyManager[] keyManagers;
        final TrustManager[] trustManagers;

        KeyAndTrustManagers(KeyManager[] keyManagers, TrustManager[] trustManagers) {
            this.keyManagers = keyManagers;
            this.trustManagers = trustManagers;
        }

    }

    /**
     * 不安全的 TrustManager
     * <p>
     * 不校验服务器证书
     */
    static class UnsafeTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // 可以保存服务器发过来的证书，是一种导出证书的方法
            // for (int i = 0; i < chain.length; i++) {
            //     saveCertificate(chain[i], "unsafe_cert_" + i + ".cer");
            //     savePem(chain[i], "unsafe_cert_" + i + ".pem");
            // }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

    }

    /**
     * 安全的 TrustManager
     * <p>
     * 校验服务器证书
     */
    static class SafeTrustManager implements X509TrustManager {

        final List<X509KeyManager> keyManagers;
        final List<X509TrustManager> trustManagers;
        final X509Certificate[] certificates;

        SafeTrustManager(KeyAndTrustManagers keyAndTrustManagers,
                         List<? extends Certificate> certificates) {
            this.keyManagers = new ArrayList<>();
            for (KeyManager keyManager : keyAndTrustManagers.keyManagers) {
                if (keyManager instanceof X509KeyManager) {
                    keyManagers.add((X509KeyManager) keyManager);
                }
            }

            this.trustManagers = new ArrayList<>();
            for (TrustManager trustManager : keyAndTrustManagers.trustManagers) {
                if (trustManager instanceof X509TrustManager) {
                    trustManagers.add((X509TrustManager) trustManager);
                }
            }

            List<X509Certificate> certs = new ArrayList<>();
            for (Certificate certificate : certificates) {
                if (certificate instanceof X509Certificate) {
                    certs.add((X509Certificate) certificate);
                }
            }
            this.certificates = certs.toArray(new X509Certificate[0]);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            CertificateException exception = null;
            for (X509TrustManager trustManager : trustManagers) {
                try {
                    trustManager.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    exception = e;
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return certificates;
        }

    }

    private static Collection<? extends Certificate> systemCertificates()
            throws NoSuchAlgorithmException, KeyStoreException {
        // 获取系统的证书
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers != null && trustManagers.length > 0) {
            for (TrustManager trustManager : trustManagers) {
                if (trustManager instanceof X509TrustManager) {
                    X509Certificate[] certs = ((X509TrustManager) trustManager).getAcceptedIssuers();
                    return certs == null ? Collections.emptyList() : Arrays.asList(certs);
                }
            }
        }
        return Collections.emptyList();
    }

    private static Collection<? extends Certificate> userCertificates(InputStream is)
            throws GeneralSecurityException {
        // 从输入流获取证书
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(is);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }
        return certificates;
    }

    private static Collection<? extends Certificate> userCertificates(String fileName)
            throws GeneralSecurityException, IOException {
        try (InputStream is = new FileInputStream(fileName)) {
            return userCertificates(is);
        }
    }

    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // By convention, 'null' creates an empty key store.
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static KeyAndTrustManagers trustManagerForCertificates(Collection<Certificate> certificates)
            throws GeneralSecurityException {
        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return new KeyAndTrustManagers(
                keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers());
    }

    private final List<Certificate> certificates;
    private final KeyAndTrustManagers keyAndTrustManagers;
    private final X509TrustManager trustManager;

    /**
     * 校验服务器证书，只加载系统证书
     *
     * @throws GeneralSecurityException 加载证书失败
     */
    public SSLHelper() throws GeneralSecurityException {
        this(false);
    }

    /**
     * 根据 {@code unsafe} 确定是否需要加载系统证书
     *
     * @param unsafe true 表示不校验服务器证书，false 表示加载服务器证书
     * @throws GeneralSecurityException 加载证书失败
     */
    public SSLHelper(boolean unsafe) throws GeneralSecurityException {
        certificates = new ArrayList<>();
        if (unsafe) {
            trustManager = new UnsafeTrustManager();
            keyAndTrustManagers = new KeyAndTrustManagers(null, new TrustManager[]{trustManager});
        } else {
            certificates.addAll(systemCertificates());
            keyAndTrustManagers = trustManagerForCertificates(certificates);
            trustManager = new SafeTrustManager(keyAndTrustManagers, certificates);
        }
    }

    /**
     * 校验服务器证书，加载外部证书 和 系统证书
     *
     * @param fileNames 外部证书文件路径集合
     * @throws GeneralSecurityException 加载证书失败
     * @throws IOException              加载证书失败
     */
    public SSLHelper(List<String> fileNames) throws GeneralSecurityException, IOException {
        certificates = new ArrayList<>();
        // 加载 PEM 证书
        if (fileNames != null && !fileNames.isEmpty()) {
            for (String fileName : fileNames) {
                certificates.addAll(userCertificates(fileName));
            }
        }
        // 加载系统证书
        certificates.addAll(systemCertificates());
        keyAndTrustManagers = trustManagerForCertificates(certificates);
        trustManager = new SafeTrustManager(keyAndTrustManagers, certificates);
    }

    /**
     * 校验服务器证书，加载外部证书 和 系统证书
     *
     * @param is 外部证书输入流。可以把多个 PEM 证书制作成一个输入流，例如：
     *           <code>
     *           new Buffer()
     *           .writeUtf8(comodoRsaCertificationAuthority)
     *           .writeUtf8(entrustRootCertificateAuthority)
     *           .writeUtf8(letsEncryptCertificateAuthority)
     *           .inputStream();
     *           </code>
     * @throws GeneralSecurityException 加载证书失败
     */
    public SSLHelper(InputStream is) throws GeneralSecurityException {
        certificates = new ArrayList<>();
        // 加载 PEM 证书
        certificates.addAll(userCertificates(is));
        // 加载系统证书
        certificates.addAll(systemCertificates());
        keyAndTrustManagers = trustManagerForCertificates(certificates);
        trustManager = new SafeTrustManager(keyAndTrustManagers, certificates);
    }

    public X509TrustManager getX509TrustManager() {
        return trustManager;
    }

    /**
     * 获取 {@link SSLSocketFactory}，默认：开启 SSLv3, TLSv1, TLSv1.1, TLSv1.2
     *
     * @return {@link SSLSocketFactory}
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return getSSLSocketFactory(new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"});
    }

    /**
     * 获取 {@link SSLSocketFactory}，指定开启的协议
     *
     * @param enabledProtocols 开启的协议
     * @return {@link SSLSocketFactory}
     */
    public SSLSocketFactory getSSLSocketFactory(String[] enabledProtocols) {
        // Install the all-trusting trust manager
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyAndTrustManagers.keyManagers,
                    keyAndTrustManagers.trustManagers,
                    new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            return new TLSSocketFactory(sslContext.getSocketFactory(), enabledProtocols);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> {
            // needs verify?
            return true;
        };
    }

    /**
     * 保存证书
     *
     * @param certificate 证书
     * @param fileName    导出的文件名
     */
    public static void saveCertificate(Certificate certificate, String fileName) {
        // 可以通过命令将证书转为 pem 格式，savePem 实现相同的功能
        // openssl x509 -inform der -in xxx.cer -out xxx.pem
        try (OutputStream os = new FileOutputStream(fileName)) {
            os.write(certificate.getEncoded());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 保存证书为 pem 格式
     *
     * @param certificate 证书
     * @param fileName    导出的文件名
     */
    public static void savePem(Certificate certificate, String fileName) {
        // 将证书保存为 pem 格式，这是 java 常用格式
        try (JcaPEMWriter writer = new JcaPEMWriter(new FileWriter(fileName))) {
            writer.writeObject(certificate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
