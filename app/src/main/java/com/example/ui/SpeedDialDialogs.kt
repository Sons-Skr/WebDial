package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.SpeedDial

@Composable
fun AddSpeedDialDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, url: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var url by remember { mutableStateOf("") }
  var urlError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.BookmarkAdd,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = "Add Speed Dial",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Name option with bottom text
        Column {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Name") },
            placeholder = { Text("e.g. Wikipedia") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_dialog_name_input"),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "Enter website name",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.padding(start = 6.dp)
          )
        }

        // Website option with bottom text
        Column {
          OutlinedTextField(
            value = url,
            onValueChange = {
              url = it
              urlError = false
            },
            label = { Text("Website") },
            placeholder = { Text("e.g. wikipedia.org") },
            singleLine = true,
            isError = urlError,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("add_dialog_url_input"),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Uri,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                if (url.trim().isEmpty()) {
                  urlError = true
                } else {
                  onConfirm(title, url)
                }
              }
            )
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = if (urlError) "Please enter a valid website address" else "Enter website URL",
            style = MaterialTheme.typography.bodySmall,
            color = if (urlError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.padding(start = 6.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (url.trim().isEmpty()) {
            urlError = true
          } else {
            onConfirm(title, url)
          }
        },
        modifier = Modifier.testTag("add_dialog_confirm_btn")
      ) {
        Text("Add")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("add_dialog_cancel_btn")
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun RenameSpeedDialDialog(
  speedDial: SpeedDial,
  onDismiss: () -> Unit,
  onConfirm: (speedDialId: String, newTitle: String) -> Unit
) {
  var title by remember { mutableStateOf(speedDial.title) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.DriveFileRenameOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = "Rename",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Name") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rename_dialog_input"),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = { onConfirm(speedDial.id, title) }
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Enter new website name",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          modifier = Modifier.padding(start = 6.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(speedDial.id, title) },
        modifier = Modifier.testTag("rename_dialog_confirm_btn")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("rename_dialog_cancel_btn")
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun ChangeWebsiteDialog(
  speedDial: SpeedDial,
  onDismiss: () -> Unit,
  onConfirm: (speedDialId: String, newUrl: String) -> Unit
) {
  var url by remember { mutableStateOf(speedDial.url) }
  var urlError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Language,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = "Change Website",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = url,
          onValueChange = {
            url = it
            urlError = false
          },
          label = { Text("Website") },
          placeholder = { Text("https://example.com") },
          singleLine = true,
          isError = urlError,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("change_website_dialog_input"),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(
            onDone = {
              if (url.trim().isEmpty()) {
                urlError = true
              } else {
                onConfirm(speedDial.id, url)
              }
            }
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (urlError) "URL cannot be empty" else "Enter new website URL",
          style = MaterialTheme.typography.bodySmall,
          color = if (urlError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
          modifier = Modifier.padding(start = 6.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (url.trim().isEmpty()) {
            urlError = true
          } else {
            onConfirm(speedDial.id, url)
          }
        },
        modifier = Modifier.testTag("change_website_dialog_confirm_btn")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("change_website_dialog_cancel_btn")
      ) {
        Text("Cancel")
      }
    }
  )
}
