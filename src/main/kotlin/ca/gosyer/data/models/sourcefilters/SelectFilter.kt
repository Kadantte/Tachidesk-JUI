/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Select")
data class SelectFilter(
    override val filter: SelectProps
) : SourceFilter() {
    @Serializable
    data class SelectProps(
        override val name: String,
        override val state: Int,
        val values: List<String>
    ) : Props<Int>
}
