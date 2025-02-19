/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models.sourcepreference

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("EditTextPreference")
data class EditTextPreference(
    override val props: EditTextProps
) : SourcePreference() {
    @Serializable
    data class EditTextProps(
        override val key: String,
        override val title: String?,
        override val summary: String?,
        override val currentValue: String?,
        override val defaultValue: String?,
        override val defaultValueType: String,
        val dialogTitle: String?,
        val dialogMessage: String?,
        val text: String?
    ) : Props<String?>
}
