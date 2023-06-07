import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64String(): String {
    return Base64.encode(this)
}

fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }