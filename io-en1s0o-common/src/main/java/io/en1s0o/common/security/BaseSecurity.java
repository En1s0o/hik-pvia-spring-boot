package io.en1s0o.common.security;

import java.security.Security;

/**
 * BaseSecurity
 *
 * @author En1s0o
 */
public class BaseSecurity {

    static {
        // 添加加密算法支持
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

}
