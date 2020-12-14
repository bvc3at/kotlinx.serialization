package kotlinx.serialization.protobuf.schema

import kotlinx.serialization.serializer
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

private const val SCHEMAS_DIRECTORY_PATH = "formats/protobuf/jvmTest/resources"

// Regenerate all proto file for tests.
private fun main() {
    regenerateAllProtoFiles()
}

private fun regenerateAllProtoFiles() {
    generateProtoFile(GenerationTest.ScalarHolder::class)
    generateProtoFile(GenerationTest.FieldNumberClass::class)
    generateProtoFile(GenerationTest.SerialNameClass::class)
    generateProtoFile(GenerationTest.OptionsClass::class, mapOf("java_package" to "api.proto", "java_outer_classname" to "Outer"))
    generateProtoFile(GenerationTest.ListClass::class)
    generateProtoFile(GenerationTest.MapClass::class)
    generateProtoFile(GenerationTest.OptionalClass::class)
    generateProtoFile(GenerationTest.ContextualHolder::class)
    generateProtoFile(GenerationTest.AbstractHolder::class)
    generateProtoFile(GenerationTest.SealedHolder::class)
}

private inline fun <reified T : Any> generateProtoFile(
    clazz: KClass<T>,
    options: Map<String, String> = emptyMap()
) {
    val filePath = "$SCHEMAS_DIRECTORY_PATH/${clazz.simpleName}.proto"
    val file = File(filePath)
    val schema = generateProto2Schema(listOf(serializer(typeOf<T>()).descriptor), TARGET_PACKAGE, options)
    file.writeText(schema, StandardCharsets.UTF_8)
}
