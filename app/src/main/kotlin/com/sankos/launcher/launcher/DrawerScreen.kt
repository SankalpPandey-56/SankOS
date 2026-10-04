package com.sankos.launcher.launcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.sankos.launcher.data.model.AppEntry
import com.sankos.launcher.designsystem.icons.SankIcons
import com.sankos.launcher.designsystem.theme.InkElevated
import com.sankos.launcher.designsystem.theme.Paper
import com.sankos.launcher.designsystem.theme.PaperDim
import com.sankos.launcher.designsystem.theme.PaperFaint
import com.sankos.launcher.designsystem.theme.SankType
import com.sankos.launcher.designsystem.theme.sankAccent
import com.sankos.launcher.domain.ActionKind
import com.sankos.launcher.domain.AppRanker
import com.sankos.launcher.domain.SearchResult

/**
 * The intelligent app drawer.
 *
 * Empty query: ranked sections — FREQUENT (adaptive, category-aware) and
 * EVERYTHING. Any query switches to instant search with quick actions.
 */
@Composable
fun DrawerScreen(
    viewModel: LauncherViewModel,
    progressProvider: () -> Float,
    onClose: () -> Unit,
    onSankSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ranked by viewModel.ranked.collectAsState()
    val icons by viewModel.apps.collectAsState()
    val usageAccess by viewModel.usageAccess.collectAsState()

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val isSearching = query.isNotBlank()
    val searchResults = remember(query, icons) { viewModel.search(query) }

    val frequent = remember(ranked) {
        ranked.filter { it.score > AppRanker.FREQUENT_THRESHOLD }.take(6)
    }
    val everything = remember(ranked, frequent) {
        val frequentPkgs = frequent.mapTo(HashSet()) { it.app.packageName }
        ranked.filter { it.app.packageName !in frequentPkgs }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .background(com.sankos.launcher.designsystem.theme.Ink),
    ) {
        // header: title + collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 20.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("APPS", style = SankType.SectionLabel, color = PaperFaint)
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = SankIcons.Collapse,
                contentDescription = "Close drawer",
                tint = PaperDim,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { onClose() }
                    .padding(2.dp),
            )
        }

        Spacer(Modifier.height(10.dp))

        // search field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = SankIcons.Search,
                contentDescription = null,
                tint = PaperFaint,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = SankType.SearchInput.copy(color = Paper),
                cursorBrush = SolidColor(sankAccent()),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    // first result executes on done; otherwise just dismiss keyboard
                    (searchResults.firstOrNull() as? SearchResult.QuickAction)?.let { action ->
                        if (action.kind == ActionKind.SANK_SETTINGS) onSankSettings()
                        else viewModel.executeAction(context, action)
                    }
                }),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text("Search anything", style = SankType.SearchInput, color = PaperFaint)
                        }
                        inner()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .padding(vertical = 10.dp),
            )
            if (isSearching) {
                Icon(
                    imageVector = SankIcons.Collapse,
                    contentDescription = "Clear search",
                    tint = PaperFaint,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { query = "" }
                        .padding(2.dp),
                )
            }
        }

        // usage-access hint: honest, tiny, optional
        if (!usageAccess && !isSearching) {
            Text(
                text = "USAGE ACCESS OFF — RANKING IS ALPHABETICAL · TAP TO ENABLE",
                style = SankType.SectionLabel,
                color = PaperFaint,
                modifier = Modifier
                    .padding(start = 28.dp, top = 8.dp, end = 28.dp)
                    .clickable { viewModel.openUsageAccessSettings(context) },
            )
        }

        Spacer(Modifier.height(14.dp))

        // content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!isSearching) {
                if (frequent.isNotEmpty()) {
                    item(key = "header-frequent") {
                        SectionHeader("FREQUENT")
                    }
                    items(frequent, key = { "f-${it.app.packageName}" }) { hit ->
                        AppRow(app = hit.app, icons = icons.icons, onClick = {
                            viewModel.launchApp(context, hit.app)
                        })
                    }
                    item(key = "header-everything") { SectionHeader("EVERYTHING") }
                }
                items(everything, key = { "e-${it.app.packageName}" }) { hit ->
                    AppRow(app = hit.app, icons = icons.icons, onClick = {
                        viewModel.launchApp(context, hit.app)
                    })
                }
            } else {
                searchResults.forEach { result ->
                    when (result) {
                        is SearchResult.QuickAction -> item(key = "action-${result.kind}") {
                            QuickActionRow(
                                result = result,
                                onClick = {
                                    if (result.kind == ActionKind.SANK_SETTINGS) {
                                        onSankSettings()
                                    } else {
                                        viewModel.executeAction(context, result)
                                    }
                                },
                            )
                        }
                        is SearchResult.AppHit -> item(key = "hit-${result.app.packageName}") {
                            AppRow(app = result.app, icons = icons.icons, onClick = {
                                viewModel.launchApp(context, result.app)
                            })
                        }
                    }
                }
                if (searchResults.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "Nothing matches that",
                            style = SankType.Body,
                            color = PaperFaint,
                            modifier = Modifier.padding(start = 8.dp, top = 20.dp),
                        )
                    }
                }
            }
            item(key = "bottom-space") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = SankType.SectionLabel,
        color = PaperFaint,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun AppRow(
    app: AppEntry,
    icons: Map<String, android.graphics.drawable.Drawable>,
    onClick: () -> Unit,
) {
    val icon = icons[app.packageName]
    val bitmap = remember(icon) {
        runCatching { icon?.toBitmap(64, 64)?.asImageBitmap() }.getOrNull()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(InkElevated),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.size(26.dp))
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    style = SankType.Title,
                    color = PaperDim,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(text = app.label, style = SankType.Body, color = Paper, maxLines = 1)
    }
}

@Composable
private fun QuickActionRow(result: SearchResult.QuickAction, onClick: () -> Unit) {
    val accent = sankAccent()
    val icon = when (result.kind) {
        ActionKind.TIMER -> SankIcons.Timer
        ActionKind.DIAL -> SankIcons.Outbound
        ActionKind.SANK_SETTINGS -> SankIcons.Sliders
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(InkElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(text = result.title, style = SankType.Body, color = Paper, maxLines = 1)
            Text(text = result.subtitle, style = SankType.SectionLabel, color = PaperFaint)
        }
    }
}
