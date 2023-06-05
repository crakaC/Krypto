import java.math.BigInteger
import java.security.SecureRandom

data class ExchangedKey(
    val a: BigInteger,
    val b: BigInteger,
    val key: BigInteger
) {
    override fun toString(): String {
        return """
            a: $a,
            b: $b,
            key: $key
        """.trimIndent()
    }
}

fun dhKeyExchange(keyLength: Int): ExchangedKey {
    val random = SecureRandom()
    val g = BigInteger.TWO
    val p = BigInteger.probablePrime(keyLength, random)
    val a = BigInteger(keyLength / 2, random)
    val e = g.modPow(a, p)
    val b = BigInteger(keyLength / 2, random)
    val f = g.modPow(b, p)

    val keyA = f.modPow(a, p)
    val keyB = e.modPow(b, p)
    val key = g.modPow(a * b, p)

    return ExchangedKey(a = keyA, b = keyB, key = key)
}