package com.example.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.SpeedDial
import com.example.data.SpeedDialRepository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedDialGrid(
  speedDials: List<SpeedDial>,
  contextMenuState: ContextMenuState,
  onSpeedDialClick: (SpeedDial) -> Unit,
  onSpeedDialLongClick: (SpeedDial) -> Unit,
  onDismissMenu: () -> Unit,
  onEditClick: (SpeedDial) -> Unit,
  onDeleteClick: (SpeedDial) -> Unit,
  onRenameClick: (SpeedDial) -> Unit,
  onChangeWebsiteClick: (SpeedDial) -> Unit,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(SpeedDialRepository.TOTAL_COLUMNS), // 5 columns
    modifier = modifier
      .fillMaxWidth()
      .testTag("speed_dial_grid"),
    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Auto-aligned speed dial items
    itemsIndexed(
      items = speedDials,
      key = { _, item -> item.id }
    ) { index, speedDial ->
      val isTargetOfMenu = when (contextMenuState) {
        is ContextMenuState.MainMenu -> contextMenuState.speedDial.id == speedDial.id
        is ContextMenuState.EditSubMenu -> contextMenuState.speedDial.id == speedDial.id
        ContextMenuState.None -> false
      }

      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
      ) {
        SpeedDialItem(
          speedDial = speedDial,
          onClick = { onSpeedDialClick(speedDial) },
          onLongClick = { onSpeedDialLongClick(speedDial) },
          modifier = Modifier.testTag("speed_dial_item_$index")
        )

        // Popup Menu anchored to this speed dial
        if (isTargetOfMenu) {
          when (contextMenuState) {
            is ContextMenuState.MainMenu -> {
              DropdownMenu(
                expanded = true,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.testTag("speed_dial_context_menu")
              ) {
                DropdownMenuItem(
                  text = {
                    Text(
                      text = "Edit",
                      fontWeight = FontWeight.SemiBold
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit speed dial",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  },
                  trailingIcon = {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                  },
                  onClick = { onEditClick(speedDial) },
                  modifier = Modifier.testTag("menu_option_edit")
                )

                HorizontalDivider()

                DropdownMenuItem(
                  text = {
                    Text(
                      text = "Delete",
                      color = MaterialTheme.colorScheme.error,
                      fontWeight = FontWeight.SemiBold
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete speed dial",
                      tint = MaterialTheme.colorScheme.error
                    )
                  },
                  onClick = { onDeleteClick(speedDial) },
                  modifier = Modifier.testTag("menu_option_delete")
                )
              }
            }

            is ContextMenuState.EditSubMenu -> {
              DropdownMenu(
                expanded = true,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.testTag("speed_dial_edit_submenu")
              ) {
                DropdownMenuItem(
                  text = {
                    Text(
                      text = "Edit: ${speedDial.title}",
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back",
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  onClick = { onSpeedDialLongClick(speedDial) },
                  modifier = Modifier.testTag("menu_option_edit_back")
                )

                HorizontalDivider()

                DropdownMenuItem(
                  text = { Text("Rename") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.DriveFileRenameOutline,
                      contentDescription = "Rename",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  },
                  onClick = { onRenameClick(speedDial) },
                  modifier = Modifier.testTag("menu_option_rename")
                )

                DropdownMenuItem(
                  text = { Text("Change website") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Language,
                      contentDescription = "Change website",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  },
                  onClick = { onChangeWebsiteClick(speedDial) },
                  modifier = Modifier.testTag("menu_option_change_website")
                )
              }
            }

            ContextMenuState.None -> Unit
          }
        }
      }
    }

    // Plus icon at the last of the speed dials to add new speed dials
    item(key = "plus_speed_dial_card") {
      PlusSpeedDialCard(
        onClick = onAddClick,
        modifier = Modifier.testTag("plus_speed_dial_card")
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedDialItem(
  speedDial: SpeedDial,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val cornerShape = RoundedCornerShape(14.dp) // Square with circular corners
  val brandColor = speedDial.brandColor

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.padding(vertical = 2.dp)
  ) {
    // Square icon container with background matching the icon/brand color
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .aspectRatio(1f)
        .clip(cornerShape)
        .background(brandColor.copy(alpha = 0.16f))
        .border(
          width = 1.2.dp,
          color = brandColor.copy(alpha = 0.45f),
          shape = cornerShape
        )
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick
        ),
      contentAlignment = Alignment.Center
    ) {
      SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
          .data(speedDial.faviconUrl)
          .crossfade(true)
          .diskCachePolicy(CachePolicy.ENABLED)
          .memoryCachePolicy(CachePolicy.ENABLED)
          .build(),
        contentDescription = "${speedDial.title} logo",
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Fit,
        loading = {
          InitialsBadge(title = speedDial.title, color = brandColor)
        },
        error = {
          InitialsBadge(title = speedDial.title, color = brandColor)
        }
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Label as web name
    Text(
      text = speedDial.title,
      style = MaterialTheme.typography.labelSmall,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun InitialsBadge(
  title: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  val initial = (title.firstOrNull() ?: 'W').uppercaseChar()

  Box(
    modifier = modifier
      .size(34.dp)
      .clip(CircleShape)
      .background(color),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initial.toString(),
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      textAlign = TextAlign.Center
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlusSpeedDialCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cornerShape = RoundedCornerShape(14.dp)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.padding(vertical = 2.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .aspectRatio(1f)
        .clip(cornerShape)
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        .border(
          width = 1.2.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
          shape = cornerShape
        )
        .combinedClickable(
          onClick = onClick,
          onLongClick = onClick
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add new speed dial",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(26.dp)
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Add",
      style = MaterialTheme.typography.labelSmall,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
  }
}
