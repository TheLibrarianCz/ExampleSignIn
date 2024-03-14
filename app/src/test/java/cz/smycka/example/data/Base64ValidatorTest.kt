package cz.smycka.example.data

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class Base64ValidatorTest(private val input: String, private val expected: Boolean) {

    @Test
    fun testValidation() {
        assert(Base64Validator().validate(input) == expected)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(
            name = " \"{0}\" as {1}"
        )
        fun getTestSetData() = listOf(
            arrayOf("82ffbccc9a291f63975943ee64c7bb34", true),
            arrayOf("sfddsf", false),
            arrayOf("f875eba085941cc78509bd3482dc0294", true),
            arrayOf("9e107d9d372bb6826bd81d3542a419d6", true),
            arrayOf("___|_____", false),
            arrayOf("", false),
            arrayOf("82ffbccc_a291f63975943ee64c7bb34", false)
        )
    }
}
