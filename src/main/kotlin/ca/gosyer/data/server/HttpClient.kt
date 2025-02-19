/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.build.BuildConfig
import ca.gosyer.data.server.model.Auth
import ca.gosyer.data.server.model.Proxy
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.auth.providers.BasicAuthCredentials
import io.ktor.client.features.auth.providers.DigestAuthCredentials
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.auth.providers.digest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets
import io.ktor.http.URLBuilder
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Provider
import io.ktor.client.features.auth.Auth as AuthFeature

typealias Http = HttpClient

internal class HttpProvider @Inject constructor(
    private val serverPreferences: ServerPreferences
) : Provider<Http> {
    override fun get(): Http {
        return HttpClient(OkHttp) {
            engine {
                proxy = when (serverPreferences.proxy().get()) {
                    Proxy.NO_PROXY -> ProxyConfig.NO_PROXY
                    Proxy.HTTP_PROXY -> ProxyBuilder.http(
                        URLBuilder(
                            host = serverPreferences.proxyHttpHost().get(),
                            port = serverPreferences.proxyHttpPort().get()
                        ).build()
                    )
                    Proxy.SOCKS_PROXY -> ProxyBuilder.socks(
                        serverPreferences.proxySocksHost().get(),
                        serverPreferences.proxySocksPort().get()
                    )
                }
            }
            when (serverPreferences.auth().get()) {
                Auth.NONE -> Unit
                Auth.BASIC -> install(AuthFeature) {
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                serverPreferences.authUsername().get(),
                                serverPreferences.authPassword().get()
                            )
                        }
                    }
                }
                Auth.DIGEST -> install(AuthFeature) {
                    digest {
                        credentials {
                            DigestAuthCredentials(
                                serverPreferences.authUsername().get(),
                                serverPreferences.authPassword().get()
                            )
                        }
                    }
                }
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json {
                        isLenient = false
                        ignoreUnknownKeys = true
                        allowSpecialFloatingPointValues = true
                        useArrayPolymorphism = false
                    }
                )
            }
            install(WebSockets)
            install(Logging) {
                level = if (BuildConfig.DEBUG) {
                    LogLevel.HEADERS
                } else {
                    LogLevel.INFO
                }
            }
        }
    }
}
