import java.math.BigInteger
import java.security.SecureRandom

fun main() {
    val key = measureTime("gen") { generateKeyPair(4096) }
    println(key)
    val plain = 1234567890L
    val encrypted = measureTime("encrypt") { encrypt(plain.toBigInteger(), key) }
    val decrypted = measureTime("decrypt") { decrypt(encrypted, key) }
    println("plain: $plain")
    println("encrypted: $encrypted")
    println("decrypted: $decrypted")
}

data class RSAKey(
    val N: BigInteger,
    val E: BigInteger,
    val D: BigInteger
) {
    override fun toString(): String {
        return """
            N = $N,
            E = $E,
            D = $D
        """.trimIndent()
    }
}

fun generateKeyPair(keySize: Int): RSAKey {
    val e = 65537.toBigInteger() // だいたいこれが決め打ちらしい。
    // 鍵長が偶数のときはこうやるとうまいこといくらしい
    val minValue = if (keySize.and(1) == 0) getSqrt(keySize) else BigInteger.ZERO
    val random = SecureRandom()
    val lp = (keySize + 1).shr(1)
    val lq = keySize - lp
    val pqDiffSize = lp - 100
    while (true) {
        var tmp: BigInteger? = null
        for (i in 0 until 10 * lp) {
            tmp = BigInteger.probablePrime(lp, random)
            if (
                tmp > minValue &&
                (tmp - BigInteger.ONE).isRelativePrime(e)
            ) {
                println("p is found in loop($i)")
                break
            }
        }
        val p = tmp ?: throw RuntimeException("Cannot find prime P")

        for (i in 0 until 20 * lq) {
            tmp = BigInteger.probablePrime(lq, random)
            if (
                tmp > minValue &&
                (p.minus(tmp).abs() > BigInteger.TWO.pow(pqDiffSize)) &&
                (tmp - BigInteger.ONE).isRelativePrime(e)
            ) {
                println("q is found in loop($i)")
                break
            }
        }
        val q = tmp ?: throw RuntimeException("Cannot find prime Q")
        val n = p * q
        if (n.bitLength() != keySize) {
            println("keySize is not match")
            continue
        }
        return createKeyPair(n, e, p, q)
    }
}

fun getSqrt(keySize: Int): BigInteger {
    return BigInteger.TWO.pow(keySize - 1).sqrt()
}

fun encrypt(message: BigInteger, key: RSAKey): BigInteger {
    return message.modPow(key.E, key.N)
}

fun decrypt(message: BigInteger, key: RSAKey): BigInteger {
    return message.modPow(key.D, key.N)
}

fun Int.toBigInteger(): BigInteger = BigInteger.valueOf(toLong())

fun BigInteger.isRelativePrime(other: BigInteger): Boolean {
    return gcd(other) == BigInteger.ONE
}

fun createKeyPair(n: BigInteger, e: BigInteger, p: BigInteger, q: BigInteger): RSAKey {
    val p1 = p - BigInteger.ONE
    val q1 = q - BigInteger.ONE

    val lcm = (p1 * q1) / p1.gcd(q1)

    val d = e.modInverse(lcm)
    if (d <= BigInteger.TWO.pow(p.bitLength())) {
        throw RuntimeException("something wrong!")
    }
    return RSAKey(
        N = n, E = e, D = d
    )
}

inline fun <T> measureTime(tag: String = "", crossinline block: () -> T): T {
    val start = System.nanoTime()
    val res = block()
    val elapsed = System.nanoTime() - start
    println("$tag: ${elapsed.toDouble() / 1_000_000} ms!")
    return res
}