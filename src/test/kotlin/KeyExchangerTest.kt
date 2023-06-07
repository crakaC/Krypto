import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class KeyExchangerTest : StringSpec({
    "key exchange and crypto" {
        checkAll<String>(1_000) { text ->
            val alice = KeyExchanger()
            val bob = KeyExchanger()
            alice.generateSharedSecret(bob.encodedPublicKey)
            bob.generateSharedSecret(alice.encodedPublicKey)

            val message = alice.encrypt(text)
            val decrypted = bob.decrypt(message)
            decrypted shouldBe text
        }
    }
})