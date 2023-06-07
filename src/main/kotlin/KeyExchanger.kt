import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class KeyExchanger {
    private val ecKeyPairGenerator = KeyPairGenerator.getInstance("EC").apply {
        initialize(ECGenParameterSpec("secp256r1"))
    }
    private val ecKeyFactory = KeyFactory.getInstance("EC")
    private val ecdhKeyAgreement = KeyAgreement.getInstance("ECDH")
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    private val keyPair = ecKeyPairGenerator.generateKeyPair()
    val encodedPublicKey: ByteArray = keyPair.public.encoded
    private var sharedKey: SecretKey? = null

    fun generateSharedSecret(receivedPublicKey: ByteArray) {
        val pubKey = ecKeyFactory.generatePublic(X509EncodedKeySpec(receivedPublicKey))
        val sharedSecret = ecdhKeyAgreement.run {
            init(keyPair.private)
            doPhase(pubKey, true)
            generateSecret()
        }
        val hash = MessageDigest.getInstance("SHA-256").digest(sharedSecret)
        assert(hash.size == 32)
        sharedKey = SecretKeySpec(hash, "AES")
    }

    fun encrypt(message: String): String {
        val encrypted = cipher.run {
            init(Cipher.ENCRYPT_MODE, sharedKey)
            doFinal(message.toByteArray())
        }
        return (cipher.iv + encrypted).toBase64String()
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(message: String): String {
        val decoded = Base64.decode(message)
        val iv = decoded.sliceArray(0 until 12)
        val data = decoded.sliceArray(12 until decoded.size)
        return cipher.run {
            init(Cipher.DECRYPT_MODE, sharedKey, GCMParameterSpec(128, iv))
            doFinal(data)
        }.toString(Charsets.UTF_8)
    }
}