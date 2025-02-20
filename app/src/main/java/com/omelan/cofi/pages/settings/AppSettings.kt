package com.omelan.cofi.pages.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.omelan.cofi.*
import com.omelan.cofi.R
import com.omelan.cofi.components.PiPAwareAppBar
import com.omelan.cofi.components.createAppBarBehavior
import com.omelan.cofi.ui.Spacing
import com.omelan.cofi.utils.checkPiPPermission
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AppSettings(
    goBack: () -> Unit,
    goToAbout: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val dataStore = DataStore(context)
    val isDingEnabled by dataStore.getStepChangeSetting()
        .collectAsState(initial = DING_DEFAULT_VALUE)
    val isPiPEnabled by dataStore.getPiPSetting().collectAsState(initial = PIP_DEFAULT_VALUE)
    val combineWeightState by dataStore.getWeightSetting()
        .collectAsState(initial = COMBINE_WEIGHT_DEFAULT_VALUE)
    var showCombineWeightDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val appBarBehavior = createAppBarBehavior()

    val hasPiPPermission = checkPiPPermission(context)
    fun enablePip() {
        coroutineScope.launch { dataStore.togglePipSetting() }
    }
    Scaffold(
        topBar = {
            PiPAwareAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = appBarBehavior,
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .nestedScroll(appBarBehavior.nestedScrollConnection)
                .fillMaxSize()
        ) {
            item {
                ListItem(
                    text = {
                        Text(text = stringResource(id = R.string.settings_pip_item))
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_picture_in_picture),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.settingsItemModifier(
                        onClick = { enablePip() },
                        enabled = hasPiPPermission
                    ),
                    trailing = {
                        Switch(
                            checked = isPiPEnabled,
                            onCheckedChange = { enablePip() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            ),
                            enabled = hasPiPPermission
                        )
                    }
                )
            }
            item {
                ListItem(
                    text = {
                        Text(text = stringResource(id = R.string.settings_ding_item))
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_sound),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.settingsItemModifier(
                        onClick = {
                            coroutineScope.launch {
                                dataStore.toggleStepChangeSound()
                            }
                        },
                    ),
                    trailing = {
                        Switch(
                            checked = isDingEnabled,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    dataStore.toggleStepChangeSound()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            )
                        )
                    }
                )
            }
            item {
                ListItem(
                    overlineText = {
                        Text(text = stringResource(id = R.string.settings_combine_weight_item))
                    },
                    text = {
                        Text(
                            text = stringResource(
                                stringToCombineWeight(combineWeightState).settingsStringId
                            ),
                            fontWeight = FontWeight.Light
                        )
                    },
                    icon = { Icon(Icons.Rounded.List, contentDescription = null) },
                    modifier = Modifier.settingsItemModifier(
                        onClick = { showCombineWeightDialog = true },
                    ),
                )
                if (showCombineWeightDialog) {
                    CombineWeightDialog(
                        dismiss = { showCombineWeightDialog = false },
                        selectCombineMethod = {
                            coroutineScope.launch {
                                dataStore.selectCombineMethod(it)
                                showCombineWeightDialog = false
                            }
                        },
                        combineWeightState = combineWeightState
                    )
                }
            }
            item {
                ListItem(
                    text = {
                        Text(text = stringResource(id = R.string.settings_bug_item))
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_bug_report),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.settingsItemModifier(
                        onClick = {
                            uriHandler.openUri("https://github.com/rozPierog/Cofi/issues")
                        }
                    ),
                )
            }
            item {
                ListItem(
                    text = {
                        Text(text = stringResource(id = R.string.settings_about_item))
                    },
                    icon = {
                        Icon(Icons.Rounded.Info, contentDescription = null)
                    },
                    modifier = Modifier.settingsItemModifier(onClick = goToAbout)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@Composable
fun CombineWeightDialog(
    dismiss: () -> Unit,
    selectCombineMethod: (CombineWeight) -> Unit,
    combineWeightState: String
) {
    Dialog(
        onDismissRequest = dismiss
    ) {
        Surface(
            shape = RoundedCornerShape(28.0.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(vertical = Spacing.big)
            ) {
                CombineWeight.values().forEach {
                    ListItem(
                        text = { Text(stringResource(id = it.settingsStringId)) },
                        modifier = Modifier.selectable(
                            selected = combineWeightState == it.name,
                            onClick = { selectCombineMethod(it) },
                        ),
                        icon = {
                            RadioButton(
                                selected = combineWeightState == it.name,
                                onClick = { selectCombineMethod(it) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.secondary,
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalMaterialApi
@Preview
@Composable
fun SettingsPagePreview() {
    AppSettings(goBack = { }, goToAbout = { })
}