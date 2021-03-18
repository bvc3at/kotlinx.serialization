package kotlinx.serialization.protobuf.schema

import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal const val TARGET_PACKAGE = "kotlinx.serialization.protobuf.schema.generator"

class GenerationTest {

    @Serializable
    class ScalarHolder(
        val int: Int,
        @ProtoType(ProtoIntegerType.SIGNED)
        val intSigned: Int,
        @ProtoType(ProtoIntegerType.FIXED)
        val intFixed: Int,
        @ProtoType(ProtoIntegerType.DEFAULT)
        val intDefault: Int,

        val long: Long,
        @ProtoType(ProtoIntegerType.SIGNED)
        val longSigned: Long,
        @ProtoType(ProtoIntegerType.FIXED)
        val longFixed: Long,
        @ProtoType(ProtoIntegerType.DEFAULT)
        val longDefault: Int,

        val flag: Boolean,
        val byteArray: ByteArray,
        val boxedByteArray: Array<Byte?>,
        val text: String,
        val float: Float,
        val double: Double
    )

    @Serializable
    class FieldNumberClass(
        val a: Int,
        @ProtoNumber(5)
        val b: Int,
        @ProtoNumber(3)
        val c: Int
    )

    @Serializable
    @SerialName("my serial name")
    class SerialNameClass(
        val original: Int,
        @SerialName("enum field")
        val b: SerialNameEnum

    )

    @Serializable
    enum class SerialNameEnum {
        FIRST,

        @SerialName("overridden-name-of-enum!")
        SECOND
    }

    @Serializable
    data class OptionsClass(val i: Int)

    @Serializable
    class ListClass(
        val intList: List<Int>,
        val intArray: IntArray,
        val boxedIntArray: Array<Int?>,
        val messageList: List<OptionsClass>,
        val enumList: List<SerialNameEnum>
    )

    @Serializable
    class MapClass(
        val scalarMap: Map<Int, Float>,
        val bytesMap: Map<Int, List<Byte>>,
        val messageMap: Map<String, OptionsClass>,
        val enumMap: Map<Boolean, SerialNameEnum>
    )

    @Serializable
    data class OptionalClass(
        val requiredInt: Int,
        val optionalInt: Int = 5,
        val nullableInt: Int?,
        val nullableOptionalInt: Int? = 10
    )

    @Serializable
    data class ContextualHolder(
        @Contextual val value: Int
    )

    @Serializable
    abstract class AbstractClass(val int: Int)

    @Serializable
    data class AbstractHolder(@Polymorphic val abs: AbstractClass)

    @Serializable
    sealed class SealedClass {
        @Serializable
        data class Impl1(val int: Int) : SealedClass()

        @Serializable
        data class Impl2(val long: Long) : SealedClass()
    }

    @Serializable
    data class SealedHolder(val sealed: SealedClass)

    @Test
    fun testIllegalPackageNames() {
        val descriptors = listOf(OptionsClass.serializer().descriptor)
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, "", emptyMap()) }
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, ".", emptyMap()) }
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, ".first.dot", emptyMap()) }
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, "ended.with.dot.", emptyMap()) }
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, "first._underscore", emptyMap()) }
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, "first.1digit", emptyMap()) }
        assertFailsWith(IllegalArgumentException::class) { generateProto2Schema(descriptors, "illegal.sym+bol", emptyMap()) }
    }

    @Test
    fun testValidPackageNames() {
        val descriptors = listOf(OptionsClass.serializer().descriptor)
        generateProto2Schema(descriptors, "singleIdent", emptyMap())
        generateProto2Schema(descriptors, "double.ident", emptyMap())
        generateProto2Schema(descriptors, "with.digits0123", emptyMap())
        generateProto2Schema(descriptors, "with.underscore_", emptyMap())
    }

    @Test
    fun test() {
        assertProtoForType(ScalarHolder::class)
        assertProtoForType(FieldNumberClass::class)
        assertProtoForType(SerialNameClass::class)
        assertProtoForType(OptionsClass::class, mapOf("java_package" to "api.proto", "java_outer_classname" to "Outer"))
        assertProtoForType(ListClass::class)
        assertProtoForType(MapClass::class)
        assertProtoForType(OptionalClass::class)
        assertProtoForType(ContextualHolder::class)
        assertProtoForType(AbstractHolder::class)
        assertProtoForType(SealedHolder::class)
    }

    private inline fun <reified T : Any> assertProtoForType(
        clazz: KClass<T>,
        options: Map<String, String> = emptyMap()
    ) {
        val schema = clazz.java.getResourceAsStream("/${clazz.simpleName}.proto")
            .readBytes().toString(Charsets.UTF_8)
            .replace("\r\n", "\n") // fixme when compiled on windows, the file contains line \r\n breaks
        assertEquals(schema, generateProto2Schema(listOf(serializer(typeOf<T>()).descriptor), TARGET_PACKAGE, options))
    }
}
