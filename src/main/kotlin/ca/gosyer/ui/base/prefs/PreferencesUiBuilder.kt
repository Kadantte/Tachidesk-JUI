/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.prefs

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.ColorPickerDialog
import ca.gosyer.ui.base.components.ScrollableColumn

@Composable
fun PreferencesScrollableColumn(
    modifier: Modifier = Modifier,
    content: @Composable PreferenceScope.() -> Unit
) {
    Box {
        ScrollableColumn(modifier) {
            val scope = PreferenceScope()
            scope.content()
        }
    }
}

class PreferenceScope {
    @Composable
    fun <Key> ChoicePref(
        preference: PreferenceMutableStateFlow<Key>,
        choices: Map<Key, String>,
        title: String,
        subtitle: String? = null
    ) {
        Pref(
            title = title,
            subtitle = if (subtitle == null) choices[preference.value] else null,
            onClick = {
                ChoiceDialog(
                    items = choices.toList(),
                    selected = preference.value,
                    title = title,
                    onSelected = { selected ->
                        preference.value = selected
                    }
                )
            }
        )
    }

    @Composable
    fun ColorPref(
        preference: PreferenceMutableStateFlow<Color>,
        title: String,
        subtitle: String? = null,
        unsetColor: Color = Color.Unspecified
    ) {
        val initialColor = preference.value.takeOrElse { unsetColor }
        Pref(
            title = title,
            subtitle = subtitle,
            onClick = {
                    ColorPickerDialog(
                        title = title,
                        onSelected = {
                            preference.value = it
                        },
                        initialColor = initialColor
                    )
            },
            onLongClick = { preference.value = Color.Unspecified },
            action = {
                if (preference.value != Color.Unspecified || unsetColor != Color.Unspecified) {
                    val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color = initialColor)
                            .border(BorderStroke(1.dp, borderColor), CircleShape)
                    )
                }
            }
        )
    }

    private fun <T> ChoiceDialog(
        items: List<Pair<T, String>>,
        selected: T?,
        onDismissRequest: () -> Unit = {},
        onSelected: (T) -> Unit,
        title: String,
        buttons: @Composable (AppWindow) -> Unit = {  }
    ) {
        WindowDialog(onDismissRequest = onDismissRequest, buttons = buttons, title = title, content = {
            LazyColumn {
                items(items) { (value, text) ->
                    Row(
                        modifier = Modifier.requiredHeight(48.dp).fillMaxWidth().clickable(
                            onClick = {
                                onSelected(value)
                                it.close()
                            }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = value == selected,
                            onClick = {
                                onSelected(value)
                                it.close()
                            },
                        )
                        Text(text = text, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }
        })
    }
}

@Composable
fun Pref(
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    val height = if (subtitle != null) 72.dp else 56.dp

    Row(
        modifier = Modifier.fillMaxWidth().requiredHeight(height)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                modifier = Modifier.padding(horizontal = 16.dp).size(24.dp),
                tint = MaterialTheme.colors.primary,
                contentDescription = null
            )
        }
        Column(Modifier.padding(horizontal = 16.dp).weight(1f)) {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
        if (action != null) {
            Box(Modifier.widthIn(min = 56.dp)) {
                action()
            }
        }
    }
}

@Composable
fun SwitchPref(
    preference: PreferenceMutableStateFlow<Boolean>,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Pref(
        title = title,
        subtitle = subtitle,
        icon = icon,
        action = { Switch(checked = preference.value, onCheckedChange = null) },
        onClick = { preference.value = !preference.value }
    )
}

