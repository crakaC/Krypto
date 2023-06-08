import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class SignAndVerify {
    companion object {
        const val SignatureAlgorithm = "SHA256withECDSA"
    }

    private val keyPair = KeyPairGenerator.getInstance("EC").run {
        initialize(ECGenParameterSpec("secp256r1"))
        generateKeyPair()
    }

    fun sign(message: String): EcSignature {
        val signature = Signature.getInstance(SignatureAlgorithm).run {
            initSign(keyPair.private)
            update(message.toByteArray())
            sign()
        }
        println("signature: ${signature.toHex()}")
        return parseSignature(signature)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun verify(message: String, signature: EcSignature): Boolean {
        val derSignature = encodeToASN1(signature)
        return Signature.getInstance(SignatureAlgorithm).run {
            initVerify(keyPair.public)
            update(message.toByteArray())
            verify(derSignature)
        }
    }

    data class EcSignature(val r: String, val s: String)

    // https://stackoverflow.com/questions/48530316/what-is-the-output-format-of-the-sha256withecdsa-signature-algorithmを参考にしつつ自力でパースしてみる
    // https://www.rfc-editor.org/rfc/rfc3279#section-2.2.3
    private fun parseSignature(der: ByteArray): EcSignature {
        ByteArrayInputStream(der).use { input ->
            // 先頭2バイトは "30 xx(残りの長さ)"固定なので捨てて問題ない
            input.skip(2)
            val r = input.readInteger()
            val s = input.readInteger()
            return EcSignature(r.toBase64String(), s.toBase64String())
        }
    }

    private fun ByteArrayInputStream.readInteger(): ByteArray {
        assert(read() == 0x02) { "type is not integer" }
        val length = read()
        return readNBytes(length)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encodeToASN1(signature: EcSignature): ByteArray {
        val r = Base64.decode(signature.r)
        val s = Base64.decode(signature.s)
        val buffer = ByteBuffer.allocate(6 + r.size + s.size)
            .put(0x30.toByte())
            .put((4 + r.size + s.size).toByte())
            .put(0x02.toByte())
            .put(r.size.toByte())
            .put(r)
            .put(0x02.toByte())
            .put(s.size.toByte())
            .put(s)
        return buffer.array()
    }
}