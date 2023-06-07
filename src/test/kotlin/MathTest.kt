import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.random.Random

class MathTest {
    @ParameterizedTest
    @CsvSource(
        // a, b, lcm(a, b)
        "3, 4, 12",
        "3, 6, 6",
        "5, 5, 5",
        "10, 3, 30"
    )
    fun testLCM(a: Long, b: Long, expected: Long) {
        lcm(a, b) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        // a, b, gcd(a,b)
        "3, 4, 1",
        "3, 6, 3",
        "12, 5, 1",
        "12, 6, 6"
    )
    fun testGCD(a: Long, b: Long, expected: Long) {
        gcd(a, b) shouldBe expected
    }

    @Test
    fun testSort() {
        val array = Array(10) {
            Random.nextInt(0, 100)
        }
        val expected = array.toList().sorted()
        val actual = qsort(array.toList())
        actual shouldBe expected
    }

    @Test
    fun testDHKeyExchange() {
        val (a, b, key) = dhKeyExchange(1024)
        a shouldBe b
        a shouldBe key
    }
}

fun <T : Comparable<T>> qsort(array: List<T>): List<T> {
    if (array.size <= 1) return array
    val p = array.first()
    val (l, r) = array.drop(1).partition { it < p }
    return qsort(l) + p + qsort(r)
}


fun lcm(a: Long, b: Long): Long {
    return (a * b) / gcd(a, b)
}

fun gcd(a: Long, b: Long): Long {
    val x = minOf(a, b)
    val y = maxOf(a, b)
    val rem = y % x
    return if (rem == 0L) {
        x
    } else {
        gcd(rem, x)
    }
}