/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.components.DebugOverlayViewModel

@Composable
fun DebugOverlay() {
    val vm = viewModel<DebugOverlayViewModel>()
    val usedMemory by vm.usedMemoryFlow.collectAsState()
    Column {
        Text("$usedMemory/${vm.maxMemory}", color = Color.White)
    }
}
