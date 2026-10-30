package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.BrowserTab
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    // Fixed-height bottom sheet placed directly above the toolbar:
    // Tab poster height increased by 20% (to 238dp) and stays completely constant
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      tonalElevation = 8.dp,
      shadowElevation = 12.dp,
      modifier = Modifier
        .fillMaxWidth()
        .height(358.dp) // Accommodates 20% taller posters and moved-down add button
        .align(Alignment.BottomCenter)
        .testTag("tab_menu_sheet")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 8.dp, bottom = 8.dp)
      ) {
        // Drag handle indicator - No text above tabs
        Box(
          modifier = Modifier
            .padding(top = 4.dp, bottom = 10.dp)
            .size(width = 44.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            .align(Alignment.CenterHorizontally)
        )

        // Vertical tab posters scrollable left and right (20% increased poster height)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(246.dp)
        ) {
          LazyRow(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .testTag("tabs_horizontal_grid"),
            contentPadding = if (tabs.size == 1) {
              PaddingValues(horizontal = 20.dp, vertical = 2.dp)
            } else {
              PaddingValues(start = 20.dp, end = 26.dp, top = 2.dp, bottom = 2.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Center: + (Add Tab) Icon Button moved down a little
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
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
 * Vertical tab card / poster:
 * - Constant fixed size (178dp x 238dp, 20% increased poster height)
 * - Swipe up gesture to delete/close tab with smooth fly-out animation
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
  val cardShape = RoundedCornerShape(18.dp)
  val borderColor = if (isActive) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  }

  val coroutineScope = rememberCoroutineScope()
  val offsetY = remember(tab.id) { Animatable(0f) }
  val density = LocalDensity.current
  val swipeDismissThresholdPx = with(density) { 30.dp.toPx() } // Decreased by 50% (from 60dp to 30dp)
  var isDismissing by remember(tab.id) { mutableStateOf(false) }

  val dismissAlpha = (1f - (-offsetY.value / 200f)).coerceIn(0f, 1f)

  Surface(
    shape = cardShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    tonalElevation = if (isActive) 4.dp else 1.dp,
    modifier = modifier
      .width(178.dp)
      .height(238.dp) // 20% increased poster height (from 198dp to 238dp)
      .offset { IntOffset(0, offsetY.value.roundToInt().coerceAtMost(0)) }
      .alpha(dismissAlpha)
      .clip(cardShape)
      .border(
        width = if (isActive) 2.dp else 1.dp,
        color = borderColor,
        shape = cardShape
      )
      .pointerInput(tab.id) {
        detectVerticalDragGestures(
          onDragEnd = {
            if (!isDismissing) {
              if (-offsetY.value > swipeDismissThresholdPx) {
                isDismissing = true
                coroutineScope.launch {
                  offsetY.animateTo(
                    targetValue = -900f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                  )
                  onClose()
                }
              } else {
                coroutineScope.launch {
                  offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                      dampingRatio = Spring.DampingRatioMediumBouncy,
                      stiffness = Spring.StiffnessLow
                    )
                  )
                }
              }
            }
          },
          onDragCancel = {
            if (!isDismissing) {
              coroutineScope.launch {
                offsetY.animateTo(0f)
              }
            }
          },
          onVerticalDrag = { change, dragAmount ->
            if (!isDismissing) {
              // Only allow swiping up (negative delta or returning)
              if (dragAmount < 0 || offsetY.value < 0f) {
                change.consume()
                coroutineScope.launch {
                  val nextOffset = (offsetY.value + dragAmount).coerceAtMost(0f)
                  offsetY.snapTo(nextOffset)
                }
              }
            }
          }
        )
      }
      .clickable {
        if (offsetY.value == 0f && !isDismissing) {
          onSelect()
        }
      }
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
          .padding(start = 10.dp, end = 3.dp, top = 3.dp, bottom = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Web icon + name
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (tab.isHome) {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(17.dp)
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
                .size(17.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Fit,
              error = {
                Icon(
                  imageVector = AppIcons.Language,
                  contentDescription = null,
                  modifier = Modifier.size(17.dp)
                )
              }
            )
          }

          Text(
            text = if (tab.isHome) "Home" else tab.title.ifBlank { tab.domain },
            style = MaterialTheme.typography.bodySmall,
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
            .size(34.dp)
            .testTag("close_tab_btn_${tab.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close tab",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Body preview of the tab
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(MaterialTheme.colorScheme.surface)
          .padding(10.dp),
        contentAlignment = Alignment.Center
      ) {
        if (tab.isHome) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Speed Dials",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.5.sp,
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
                .size(46.dp)
                .clip(CircleShape),
              contentScale = ContentScale.Fit,
              error = {
                Icon(
                  imageVector = AppIcons.Language,
                  contentDescription = null,
                  modifier = Modifier.size(40.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = tab.domain,
              style = MaterialTheme.typography.bodySmall,
              fontSize = 12.5.sp,
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
