/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.host

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore

sealed class ServerHostPreference<T : Any> {
    protected abstract val propertyName: String
    private val propertyPrefix
        get() = "${argPrefix}$propertyName="

    protected abstract val defaultValue: T
    protected abstract val serverValue: T
    private fun validate(value: T): Boolean {
        return value != serverValue
    }

    fun getProperty(): String? {
        val preference = preference().get().takeIf(::validate)
        return if (preference != null) {
            propertyPrefix + preference.toString()
        } else null
    }

    protected abstract val preferenceStore: PreferenceStore
    abstract fun preference(): Preference<T>

    companion object {
        const val argPrefix = "-Dsuwayomi.tachidesk.config.server."
    }

    open class StringServerHostPreference(
        override val preferenceStore: PreferenceStore,
        override val propertyName: String,
        override val defaultValue: String,
        override val serverValue: String = defaultValue
    ) : ServerHostPreference<String>() {
        override fun preference(): Preference<String> {
            return preferenceStore.getString(propertyName, defaultValue)
        }
    }
    open class IntServerHostPreference(
        override val preferenceStore: PreferenceStore,
        override val propertyName: String,
        override val defaultValue: Int,
        override val serverValue: Int = defaultValue
    ) : ServerHostPreference<Int>() {
        override fun preference(): Preference<Int> {
            return preferenceStore.getInt(propertyName, defaultValue)
        }
    }
    open class BooleanServerHostPreference(
        override val preferenceStore: PreferenceStore,
        override val propertyName: String,
        override val defaultValue: Boolean,
        override val serverValue: Boolean = defaultValue
    ) : ServerHostPreference<Boolean>() {
        override fun preference(): Preference<Boolean> {
            return preferenceStore.getBoolean(propertyName, defaultValue)
        }
    }

    class IP(preferenceStore: PreferenceStore) : StringServerHostPreference(
        preferenceStore,
        "ip",
        "0.0.0.0"
    )
    class Port(override val preferenceStore: PreferenceStore) : IntServerHostPreference(
        preferenceStore,
        "port",
        4567
    )

    // Proxy
    class SocksProxyEnabled(preferenceStore: PreferenceStore) : BooleanServerHostPreference(
        preferenceStore,
        "socksProxyEnabled",
        false
    )
    class SocksProxyHost(preferenceStore: PreferenceStore) : StringServerHostPreference(
        preferenceStore,
        "socksProxyHost",
        ""
    )
    class SocksProxyPort(override val preferenceStore: PreferenceStore) : IntServerHostPreference(
        preferenceStore,
        "socksProxyPort",
        0
    )

    // Misc
    class DebugLogsEnabled(preferenceStore: PreferenceStore) : BooleanServerHostPreference(
        preferenceStore,
        "debugLogsEnabled",
        false
    )

    class SystemTrayEnabled(preferenceStore: PreferenceStore) : BooleanServerHostPreference(
        preferenceStore,
        "systemTrayEnabled",
        true
    )

    // WebUI
    class WebUIEnabled(preferenceStore: PreferenceStore) : BooleanServerHostPreference(
        preferenceStore,
        "webUIEnabled",
        false,
        true
    )

    class OpenInBrowserEnabled(preferenceStore: PreferenceStore) : BooleanServerHostPreference(
        preferenceStore,
        "initialOpenInBrowserEnabled",
        false,
        true
    )

    // Authentication
    class BasicAuthEnabled(preferenceStore: PreferenceStore) : BooleanServerHostPreference(
        preferenceStore,
        "basicAuthEnabled",
        false
    )
    class BasicAuthUsername(preferenceStore: PreferenceStore) : StringServerHostPreference(
        preferenceStore,
        "basicAuthUsername",
        ""
    )
    class BasicAuthPassword(preferenceStore: PreferenceStore) : StringServerHostPreference(
        preferenceStore,
        "basicAuthPassword",
        ""
    )
}
