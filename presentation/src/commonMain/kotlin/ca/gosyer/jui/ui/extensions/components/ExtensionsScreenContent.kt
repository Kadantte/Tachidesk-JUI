/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.extensions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.core.lang.getDisplayName
import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.extensions.ExtensionUI
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.image.ImageLoaderImage
import ca.gosyer.jui.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.listItemsMultiChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

@Composable
fun ExtensionsScreenContent(
    extensions: List<ExtensionUI>,
    isLoading: Boolean,
    query: String?,
    setQuery: (String) -> Unit,
    enabledLangs: Set<String>,
    availableLangs: List<String>,
    setEnabledLanguages: (Set<String>) -> Unit,
    installExtension: (Extension) -> Unit,
    updateExtension: (Extension) -> Unit,
    uninstallExtension: (Extension) -> Unit
) {
    val languageDialogState = rememberMaterialDialogState()
    Scaffold(
        topBar = {
            ExtensionsToolbar(
                query,
                setQuery,
                languageDialogState::show
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen()
        } else {
            val state = rememberLazyListState()

            Box(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(Modifier.fillMaxSize(), state) {
                    items(
                        extensions,
                        contentType = {
                            when (it) {
                                is ExtensionUI.Header -> "header"
                                is ExtensionUI.ExtensionItem -> "extension"
                            }
                        },
                        key = {
                            when (it) {
                                is ExtensionUI.Header -> it.header
                                is ExtensionUI.ExtensionItem -> it.extension.pkgName
                            }
                        }
                    ) {
                        when (it) {
                            is ExtensionUI.Header -> Text(
                                it.header,
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.animateItemPlacement()
                                    .padding(16.dp, 16.dp, 16.dp, 4.dp)
                            )
                            is ExtensionUI.ExtensionItem -> Column {
                                ExtensionItem(
                                    Modifier.animateItemPlacement(),
                                    it.extension,
                                    onInstallClicked = installExtension,
                                    onUpdateClicked = updateExtension,
                                    onUninstallClicked = uninstallExtension
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .scrollbarPadding(),
                    adapter = rememberScrollbarAdapter(state)
                )
            }
        }
    }
    LanguageDialog(languageDialogState, enabledLangs, availableLangs, setEnabledLanguages)
}

@Composable
fun ExtensionsToolbar(
    searchText: String?,
    search: (String) -> Unit,
    openLanguageDialog: () -> Unit
) {
    Toolbar(
        stringResource(MR.strings.location_extensions),
        searchText = searchText,
        search = search,
        actions = {
            getActionItems(openLanguageDialog)
        }
    )
}

@Composable
fun ExtensionItem(
    modifier: Modifier,
    extension: Extension,
    onInstallClicked: (Extension) -> Unit,
    onUpdateClicked: (Extension) -> Unit,
    onUninstallClicked: (Extension) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(end = 12.dp)
            .height(50.dp)
            .background(MaterialTheme.colors.background) then modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(4.dp))
            ImageLoaderImage(
                data = extension,
                contentDescription = extension.name,
                modifier = Modifier.size(50.dp),
                filterQuality = FilterQuality.Medium
            )
            Spacer(Modifier.width(8.dp))
            Column {
                val title = buildAnnotatedString {
                    append("${extension.name} ")
                    val mediumColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
                    withStyle(SpanStyle(fontSize = 12.sp, color = mediumColor)) { append("v${extension.versionName}") }
                }
                Text(title, fontSize = 18.sp, color = MaterialTheme.colors.onBackground)
                Row {
                    Text(extension.lang.toUpperCase(Locale.current), fontSize = 14.sp, color = MaterialTheme.colors.onBackground)
                    if (extension.isNsfw) {
                        Spacer(Modifier.width(4.dp))
                        Text("18+", fontSize = 14.sp, color = Color.Red)
                    }
                    if (extension.obsolete) {
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(MR.strings.obsolete), fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
        Button(
            {
                when {
                    extension.obsolete -> onUninstallClicked(extension)
                    extension.hasUpdate -> onUpdateClicked(extension)
                    extension.installed -> onUninstallClicked(extension)
                    else -> onInstallClicked(extension)
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                when {
                    extension.obsolete -> stringResource(MR.strings.action_uninstall)
                    extension.hasUpdate -> stringResource(MR.strings.action_update)
                    extension.installed -> stringResource(MR.strings.action_uninstall)
                    else -> stringResource(MR.strings.action_install)
                }
            )
        }
    }
}

@Composable
fun LanguageDialog(
    state: MaterialDialogState,
    enabledLangs: Set<String>,
    availableLangs: List<String>,
    setLangs: (Set<String>) -> Unit
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok))
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties()
    ) {
        title(BuildKonfig.NAME)

        Box {
            val locale = remember { Locale.current }
            val listState = rememberLazyListState()
            listItemsMultiChoice(
                list = availableLangs.map { lang ->
                    Locale(lang).getDisplayName(locale).ifBlank { lang.capitalize(Locale.current) }
                },
                state = listState,
                initialSelection = enabledLangs.mapNotNull { lang ->
                    availableLangs.indexOfFirst { it == lang }.takeUnless { it == -1 }
                }.toSet(),
                onCheckedChange = { indexes ->
                    setLangs(indexes.map { availableLangs[it] }.toSet())
                }
            )
            VerticalScrollbar(
                rememberScrollbarAdapter(listState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
            )
        }
    }
}

@Stable
@Composable
private fun getActionItems(
    openLanguageDialog: () -> Unit
): List<ActionItem> {
    return listOf(
        ActionItem(
            stringResource(MR.strings.enabled_languages),
            Icons.Rounded.Translate,
            doAction = openLanguageDialog
        )
    )
}
