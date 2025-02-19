/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.build.BuildConfig
import ca.gosyer.util.lang.withIOContext
import ca.gosyer.util.system.CKLogger
import ca.gosyer.util.system.userDataDir
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File.pathSeparatorChar
import java.io.IOException
import java.io.Reader
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarInputStream
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isExecutable
import kotlin.io.path.name
import kotlin.io.path.outputStream

@OptIn(DelicateCoroutinesApi::class)
class ServerService @Inject constructor(
    serverPreferences: ServerPreferences,
    private val serverHostPreferences: ServerHostPreferences
) {
    private val restartServerFlow = MutableSharedFlow<Unit>()
    private val host = serverPreferences.host().stateIn(GlobalScope)
    private val _initialized = MutableStateFlow(
        if (host.value) {
            ServerResult.STARTING
        } else {
            ServerResult.UNUSED
        }
    )
    val initialized = _initialized.asStateFlow()
    private var process: Process? = null

    fun startAnyway() {
        _initialized.value = ServerResult.UNUSED
    }

    fun restartServer() {
        GlobalScope.launch { restartServerFlow.emit(Unit) }
    }

    @Throws(IOException::class)
    private fun copyJar(jarFile: Path) {
        javaClass.getResourceAsStream("/Tachidesk.jar")?.buffered()?.use { input ->
            jarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun getJavaFromPath(javaPath: Path): String? {
        val javaExeFile = javaPath.resolve("java.exe")
        val javaUnixFile = javaPath.resolve("java")
        return when {
            javaExeFile.exists() && javaExeFile.isExecutable() -> javaExeFile.absolutePathString()
            javaUnixFile.exists() && javaUnixFile.isExecutable() -> javaUnixFile.absolutePathString()
            else -> null
        }
    }

    private fun getRuntimeJava(): String? {
        return System.getProperty("java.home")?.let { getJavaFromPath(Path(it).resolve("bin")) }
    }

    private fun getPossibleJava(): String? {
        return System.getProperty("java.library.path")?.split(pathSeparatorChar)
            .orEmpty()
            .asSequence()
            .mapNotNull {
                val file = Path(it)
                if (file.absolutePathString().contains("java") || file.absolutePathString().contains("jdk")) {
                    if (file.name.equals("bin", true)) {
                        file
                    } else file.resolve("bin")
                } else null
            }
            .mapNotNull { getJavaFromPath(it) }
            .firstOrNull()
    }

    init {
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                process?.destroy()
                process = null
            }
        )

        merge(restartServerFlow, host).mapLatest {
            process?.destroy()
            process?.waitFor()
            _initialized.value = if (host.value) {
                ServerResult.STARTING
            } else {
                ServerResult.UNUSED
                return@mapLatest
            }
            val handler = CoroutineExceptionHandler { _, throwable ->
                error(throwable) { "Error launching Tachidesk.jar" }
                if (_initialized.value == ServerResult.STARTING || _initialized.value == ServerResult.STARTED) {
                    _initialized.value = ServerResult.FAILED
                }
            }
            GlobalScope.launch(handler) {
                val jarFile = userDataDir.also { it.createDirectories() }.resolve("Tachidesk.jar")
                if (!jarFile.exists()) {
                    info { "Copying server to resources" }
                    withIOContext { copyJar(jarFile) }
                } else {
                    try {
                        val jarVersion = withIOContext {
                            JarInputStream(jarFile.inputStream()).use { jar ->
                                jar.manifest?.mainAttributes?.getValue(Attributes.Name.IMPLEMENTATION_VERSION)?.toIntOrNull()
                            }
                        }

                        if (jarVersion != BuildConfig.SERVER_CODE) {
                            info { "Updating server file from resources" }
                            withIOContext { copyJar(jarFile) }
                        }
                    } catch (e: IOException) {
                        error(e) {
                            "Error accessing server jar, cannot update server, ${BuildConfig.NAME} may not work properly"
                        }
                    }
                }

                val javaPath = getRuntimeJava() ?: getPossibleJava() ?: "java"
                info { "Starting server with $javaPath" }
                val properties = serverHostPreferences.properties()
                info { "Using server properties:\n" + properties.joinToString(separator = "\n") }

                withIOContext {
                    val reader: Reader
                    process = ProcessBuilder(javaPath, *properties, "-jar", jarFile.absolutePathString())
                        .redirectErrorStream(true)
                        .start()
                        .also {
                            reader = it.inputStream.reader()
                        }
                    info { "Server started successfully" }
                    val logger = KotlinLogging.logger("Server")
                    reader.useLines { lines ->
                        lines.forEach {
                            if (_initialized.value == ServerResult.STARTING) {
                                if (it.contains("Javalin started")) {
                                    _initialized.value = ServerResult.STARTED
                                } else if (it.contains("Javalin has stopped")) {
                                    _initialized.value = ServerResult.FAILED
                                }
                            }
                            logger.info { it }
                        }
                    }
                    if (_initialized.value == ServerResult.STARTING) {
                        _initialized.value = ServerResult.FAILED
                    }
                    info { "Server closed" }
                    val exitVal = process?.waitFor()
                    info { "Process exitValue: $exitVal" }
                    process = null
                }
            }
        }.launchIn(GlobalScope)
    }

    enum class ServerResult {
        UNUSED,
        STARTING,
        STARTED,
        FAILED;
    }

    private companion object : CKLogger({})
}
