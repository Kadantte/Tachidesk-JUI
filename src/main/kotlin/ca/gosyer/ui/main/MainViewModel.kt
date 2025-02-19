/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.ui.base.vm.ViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    uiPreferences: UiPreferences
) : ViewModel() {
    val startScreen = uiPreferences.startScreen().get()
}
