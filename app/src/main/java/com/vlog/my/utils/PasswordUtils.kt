package com.vlog.my.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000 // Standard iteration count
    private const val KEY_LENGTH = 256    // Desired key length in bits
    private const val SALT_SIZE = 16      // Salt size in bytes

    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE)
        random.nextBytes(salt)
        return salt
    }

    fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    /**
     * Stores salt and hash as "salt_base64:hash_base64".
     */
    fun generateStoredPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        return "$saltB64:$hash"
    }

    /**
     * Verifies a password against a stored salt:hash string.
     * @param enteredPassword The password to verify.
     * @param storedSaltAndHash The string containing "salt_base64:hash_base64".
     * @return True if the password is correct, false otherwise.
     */
    fun verifyPassword(enteredPassword: String, storedSaltAndHash: String?): Boolean {
        if (storedSaltAndHash.isNullOrEmpty()) return false
        val parts = storedSaltAndHash.split(":")
        if (parts.size != 2) {
            // Log error or handle malformed stored hash
            System.err.println("Malformed stored salt and hash.")
            return false
        }
        val saltB64 = parts[0]
        val hashB64 = parts[1]

        return try {
            val salt = Base64.decode(saltB64, Base64.NO_WRAP)
            val newHashB64 = hashPassword(enteredPassword, salt)
            newHashB64 == hashB64
        } catch (e: IllegalArgumentException) {
            // Error decoding Base64 salt
            System.err.println("Error decoding Base64 salt: ${e.message}")
            false
        }
    }
}
