package com.uc.caffeine.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uc.caffeine.LocalAppScaffoldPadding

@Composable
fun CaffeineScreenScaffold(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    headerBottomSpacing: Dp = 16.dp,
    titleTrailing: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.(bottomPadding: Dp) -> Unit
) {
    val appPadding = LocalAppScaffoldPadding.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(
                start = contentPadding.calculateLeftPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateRightPadding(LocalLayoutDirection.current),
                top = contentPadding.calculateTopPadding()
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                titleTrailing?.invoke()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        if (headerBottomSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(headerBottomSpacing))
        }

        content(appPadding.calculateBottomPadding())
    }
}
