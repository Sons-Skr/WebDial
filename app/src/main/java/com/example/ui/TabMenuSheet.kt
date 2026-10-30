package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.BrowserTab

@Composable
fun TabMenuSheet(
  isOpen: Boolean,
  tabs: List<BrowserTab>,
  activeTabId: String,
  onSelectTab: (String) -> Unit,
  onCloseTab: (String) -> Unit,
  onAddNewTab: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (!isOpen) return

  val listState = rememberLazyListState()

  // Always scroll to the newest tab on the right side when tabs change
  LaunchedEffect(tabs.size) {
    if (tabs.isNotEmpty()) {
      listState.animateScrollToItem(tabs.size - 1)
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Backdrop above sheet that dismisses the tab menu when tapped
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.45f))
        .clickable { onDismiss() }
        .testTag("tab_menu_backdrop")
    )

    // Half screen sheet placed directly above the toolbar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      tonalElevation = 8.dp,
      shadowElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.55f) // Half screen
        .align(Alignment.BottomCenter)
        .testTag("tab_menu_sheet")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 10.dp, bottom = 10.dp)
      ) {
        // Drag handle indicator
        Box(
          modifier = Modifier
            .size(width = 36.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Top Header of Tab Menu
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Tabs",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${tabs.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
              )
            }
          }

          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("tab_menu_done_btn")
          ) {
            Text("Done", fontWeight = FontWeight.SemiBold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical tab cards scrollable left and right
        // - Single tab is centered horizontally
        // - Multiple tabs: old tabs on left side, new tabs on right side
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          LazyRow(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .testTag("tabs_horizontal_grid"),
            contentPadding = if (tabs.size == 1) {
              PaddingValues(horizontal = 18.dp, vertical = 4.dp)
            } else {
              PaddingValues(start = 18.dp, end = 24.dp, top = 4.dp, bottom = 4.dp)
            },
            horizontalArrangement = if (tabs.size == 1) {
              Arrangement.Center // Center single tab horizontally
            } else {
              Arrangement.spacedBy(14.dp) // Old tabs on left, new tabs on right
            }
          ) {
            items(
              items = tabs,
              key = { it.id }
            ) { tab ->
              TabCard(
                tab = tab,
                isActive = tab.id == activeTabId,
                onSelect = { onSelectTab(tab.id) },
                onClose = { onCloseTab(tab.id) }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Center: + (Add Tab) Icon Button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
          contentAlignment = Alignment.Center
        ) {
          FloatingActionButton(
            onClick = onAddNewTab,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(3.dp),
            modifier = Modifier
              .size(48.dp)
              .testTag("add_new_tab_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add new tab",
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Vertical tab card:
 * - Top: Web name + Close button on the right
 * - Body: Web preview container
 */
@Composable
fun TabCard(
  tab: BrowserTab,
  isActive: Boolean,
  onSelect: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val cardShape = RoundedCornerShape(16.dp)
  val borderColor = if (isActive) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  }

  Surface(
    shape = cardShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    tonalElevation = if (isActive) 4.dp else 1.dp,
    modifier = modifier
      .width(156.dp)
      .fillMaxHeight()
      .clip(cardShape)
      .border(
        width = if (isActive) 2.dp else 1.dp,
        color = borderColor,
        shape = cardShape
      )
      .clickable { onSelect() }
      .testTag("tab_card_${tab.id}")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top of the tab: Web Name on left, Close button on right
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            if (isActive) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            }
          )
          .padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Web icon + name
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (tab.isHome) {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(15.dp)
            )
          } else {
            SubcomposeAsyncImage(
              model = ImageRequest.Builder(context)
                .data(tab.faviconUrl)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
              contentDescription = null,
              modifier = Modifier
                .size(15.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Fit,
              error = {
                Icon(
                  imageVector = AppIcons.Language,
                  contentDescription = null,
                  modifier = Modifier.size(15.dp)
                )
              }
            )
          }

          Text(
            text = if (tab.isHome) "Home" else tab.title.ifBlank { tab.domain },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Close button on the right
        IconButton(
          onClick = onClose,
          modifier = Modifier
            .size(32.dp)
            .testTag("close_tab_btn_${tab.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close tab",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Body preview of the tab
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(MaterialTheme.colorScheme.surface)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        if (tab.isHome) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Speed Dials",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            SubcomposeAsyncImage(
              model = ImageRequest.Builder(context)
                .data(tab.faviconUrl)
                .crossfade(true)
                .build(),
              contentDescription = null,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Fit,
              error = {
                Icon(
                  imageVector = AppIcons.Language,
                  contentDescription = null,
                  modifier = Modifier.size(32.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = tab.domain,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}
