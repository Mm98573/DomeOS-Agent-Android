package com.domeos.agent.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 加密工具类
 * 使用 Android Keystore 系统存储主密钥，AES-GCM 加密敏感数据
 */
@Singleton
class EncryptionUtil @Inject constructor(
    private val context: Context
) {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val KEY_ALIAS = "domeos_master_key"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val IV_LENGTH = 12 // GCM 推荐 12 字节 IV

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    private val masterKey: SecretKey by lazy {
        generateOrLoadKey()
    }

    /** 生成或加载主密钥 */
    private fun generateOrLoadKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(false)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    /**
     * 加密字符串
     * @param plaintext 明文
     * @return Base64 编码的密文 (IV + 密文 + 认证标签)
     */
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        // 组合 IV + 密文
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * 解密字符串
     * @param encrypted Base64 编码的密文
     * @return 明文
     */
    fun decrypt(encrypted: String): String {
        val combined = Base64.decode(encrypted, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, IV_LENGTH)
        val ciphertext = combined.copyOfRange(IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, StandardCharsets.UTF_8)
    }

    /** 检查是否已初始化密钥 */
    fun isInitialized(): Boolean = keyStore.containsAlias(KEY_ALIAS)

    /** 删除主密钥（用于重置） */
    fun deleteKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }
}

/** 简单的混淆工具（不依赖 Keystore，用于非敏感数据） */
object ObfuscationUtil {

    private const val XOR_KEY = 0x5A.toByte()

    fun obfuscate(input: String): String {
        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        val result = ByteArray(bytes.size)
        for (i in bytes.indices) {
            result[i] = (bytes[i] xor XOR_KEY).toByte()
        }
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    fun deobfuscate(input: String): String {
        val bytes = Base64.decode(input, Base64.NO_WRAP)
        val result = ByteArray(bytes.size)
        for (i in bytes.indices) {
            result[i] = (bytes[i] xor XOR_KEY).toByte()
        }
        return String(result, StandardCharsets.UTF_8)
    }
}