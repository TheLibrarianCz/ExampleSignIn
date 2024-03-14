package cz.smycka.example.network

import java.math.BigInteger
import java.security.MessageDigest
import javax.inject.Inject

/**
 * SHA-1 implementation of [Encoder].
 */
class SHA1Encoder @Inject constructor() : Encoder {

    override fun encode(input: String): String {
        val sha1MessageDigest: MessageDigest = MessageDigest.getInstance("SHA-1")
        val digestedPassword: ByteArray =
            sha1MessageDigest.digest(input.toByteArray(Charsets.UTF_8))
        val signUm = BigInteger(1, digestedPassword)
        val hashText = signUm.toString(16)
        return fill32bit(hashText)
    }

    private fun fill32bit(hash: String): String {
        return if (hash.length < 32) {
            fill32bit("0$hash")
        } else {
            hash
        }
    }
}

interface Encoder {
    fun encode(input: String): String
}
