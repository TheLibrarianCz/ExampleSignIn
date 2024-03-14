package cz.smycka.example.network

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SHA1EncoderTest(private val input: String, private val expected: String) {

    @Test
    fun testEncodingSha1() {
        assertEquals(expected, SHA1Encoder().encode(input))
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(
            name = "verify that \"{0}\", is properly encoded to \"{1}\""
        )
        fun getTestSetData(): List<Array<String>> = listOf(
            arrayOf("thereisnospoon", "d0b95db10e92e2943bd371c564facebb5ed846e3"),
            arrayOf("", "da39a3ee5e6b4b0d3255bfef95601890afd80709"),
            arrayOf("test", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
        )
    }
}
