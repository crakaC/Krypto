import io.kotest.core.spec.style.StringSpec
import io.kotest.property.forAll

class SignAndVerifyTest : StringSpec({
    "signing and verify" {
        forAll<String>(1_000) { message ->
            val sut = SignAndVerify()
            val signature = sut.sign(message)
            sut.verify(message, signature)
        }
    }
})