package io.en1s0o.common.security;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;

/**
 * AESSecurity
 *
 * @author En1s0o
 */
@Slf4j
@SuppressWarnings("unused")
public class AESSecurity extends BaseSecurity {

    public static final String KEY_ALGORITHM = "AES";

    // 加解密算法/模式/填充方式
    // ECB 模式只用密钥即可对数据进行加密解密，CBC 模式需要添加一个参数 iv
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";

    public static Key toKey(String password) {
        return new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);
    }

    public static Key toKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    // 生成 iv
    public static AlgorithmParameters makeIv(String ivStr) throws Exception {
        // iv 为一个 16 字节的数组
        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) 0x00);

        // 填充我们的数据
        if (ivStr != null && !ivStr.isEmpty()) {
            byte[] data = ivStr.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(data, 0, iv, 0, Math.min(data.length, iv.length));
        }

        AlgorithmParameters params = AlgorithmParameters.getInstance(KEY_ALGORITHM);
        params.init(new IvParameterSpec(iv));
        return params;
    }

    // 加密
    public static byte[] encrypt(byte[] data, byte[] keyBytes) throws Exception {
        Key key = toKey(keyBytes);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    // 加密，CBC 模式需要添加一个参数 iv
    public static byte[] encrypt(byte[] data, byte[] keyBytes, AlgorithmParameters ivParams) throws Exception {
        Key key = toKey(keyBytes);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        return cipher.doFinal(data);
    }

    // 解密
    public static byte[] decrypt(byte[] encryptedData, byte[] keyBytes) throws Exception {
        Key key = toKey(keyBytes);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    // 解密，CBC 模式需要添加一个参数 iv
    public static byte[] decrypt(byte[] encryptedData, byte[] keyBytes, AlgorithmParameters ivParams) throws Exception {
        Key key = toKey(keyBytes);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        return cipher.doFinal(encryptedData);
    }

    public static void main(String[] args) {
        String content = "unionman123";
        String password = "xresmgr-20190311";
        log.info("Content: {}", content);
        log.info("Password: {}", password);

        try {
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            byte[] key = password.getBytes(StandardCharsets.UTF_8);
            // CBC 模式需要添加一个参数 iv
            AlgorithmParameters ivParams = makeIv(password);

            byte[] encryptedData = encrypt(data, key);
            log.info("Encrypted: {}", Base64.toBase64String(encryptedData));

            byte[] decryptedData = decrypt(encryptedData, key);
            log.info("decryptedData: {}", new String(decryptedData, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
