package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lightweight vector icons to replace heavy material-icons-extended dependency,
 * keeping the APK ultra-lite and fast on low-end devices.
 */
object AppIcons {
  val BookmarkAdd: ImageVector by lazy {
    ImageVector.Builder(
      name = "BookmarkAdd",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(17f, 11f)
        verticalLineToRelative(2f)
        horizontalLineToRelative(-2f)
        verticalLineToRelative(2f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(2f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(-2f)
        horizontalLineToRelative(2f)
        verticalLineToRelative(-2f)
        horizontalLineToRelative(-2f)
        verticalLineToRelative(-2f)
        close()
        moveTo(17f, 3f)
        lineTo(7f, 3f)
        curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
        verticalLineToRelative(16f)
        lineToRelative(7f, -3f)
        lineToRelative(7f, 3f)
        verticalLineToRelative(-7f)
        curveToRelative(-0.33f, 0.07f, -0.66f, 0.1f, -1f, 0.1f)
        curveToRelative(-2.21f, 0f, -4f, -1.79f, -4f, -4f)
        curveToRelative(0f, -1.45f, 0.78f, -2.7f, 1.94f, -3.39f)
        curveTo(18.61f, 3.28f, 17.84f, 3f, 17f, 3f)
        close()
      }
    }.build()
  }

  val Language: ImageVector by lazy {
    ImageVector.Builder(
      name = "Language",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(11.99f, 2f)
        curveTo(6.47f, 2f, 2f, 6.48f, 2f, 12f)
        reflectiveCurveToRelative(4.47f, 10f, 9.99f, 10f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        reflectiveCurveTo(17.52f, 2f, 11.99f, 2f)
        close()
        moveTo(17.92f, 7f)
        horizontalLineToRelative(-2.54f)
        curveToRelative(-0.27f, -1.39f, -0.73f, -2.67f, -1.34f, -3.76f)
        curveTo(15.75f, 4.09f, 17.06f, 5.37f, 17.92f, 7f)
        close()
        moveTo(12f, 4.04f)
        curveToRelative(0.83f, 1.2f, 1.48f, 2.53f, 1.91f, 3.96f)
        horizontalLineToRelative(-3.82f)
        curveToRelative(0.43f, -1.43f, 1.08f, -2.76f, 1.91f, -3.96f)
        close()
        moveTo(4.26f, 14f)
        curveTo(4.1f, 13.36f, 4f, 12.69f, 4f, 12f)
        reflectiveCurveToRelative(0.1f, -1.36f, 0.26f, -2f)
        horizontalLineToRelative(3.38f)
        curveToRelative(-0.08f, 0.66f, -0.14f, 1.32f, -0.14f, 2f)
        reflectiveCurveToRelative(0.06f, 1.34f, 0.14f, 2f)
        lineTo(4.26f, 14f)
        close()
        moveTo(5.08f, 16f)
        horizontalLineToRelative(2.81f)
        curveToRelative(0.36f, 1.43f, 0.95f, 2.73f, 1.72f, 3.82f)
        curveTo(7.68f, 18.99f, 6.17f, 17.7f, 5.08f, 16f)
        close()
        moveTo(7.89f, 8f)
        lineTo(5.08f, 8f)
        curveToRelative(1.09f, -1.7f, 2.6f, -2.99f, 4.53f, -3.82f)
        curveToRelative(-0.77f, 1.1f, -1.36f, 2.39f, -1.72f, 3.82f)
        close()
        moveTo(12f, 19.96f)
        curveToRelative(-0.83f, -1.2f, -1.48f, -2.53f, -1.91f, -3.96f)
        horizontalLineToRelative(3.82f)
        curveToRelative(-0.43f, 1.43f, -1.08f, 2.76f, -1.91f, 3.96f)
        close()
        moveTo(14.34f, 14f)
        lineTo(9.66f, 14f)
        curveToRelative(-0.09f, -0.66f, -0.16f, -1.32f, -0.16f, -2f)
        reflectiveCurveToRelative(0.07f, -1.35f, 0.16f, -2f)
        horizontalLineToRelative(4.68f)
        curveToRelative(0.09f, 0.65f, 0.16f, 1.32f, 0.16f, 2f)
        reflectiveCurveToRelative(-0.07f, 1.34f, -0.16f, 2f)
        close()
        moveTo(14.39f, 19.82f)
        curveToRelative(0.77f, -1.1f, 1.36f, -2.39f, 1.72f, -3.82f)
        horizontalLineToRelative(2.81f)
        curveToRelative(-1.09f, 1.71f, -2.6f, 2.99f, -4.53f, 3.82f)
        close()
        moveTo(16.36f, 14f)
        curveToRelative(0.08f, -0.66f, 0.14f, -1.32f, 0.14f, -2f)
        reflectiveCurveToRelative(-0.06f, -1.34f, -0.14f, -2f)
        horizontalLineToRelative(3.38f)
        curveToRelative(0.16f, 0.64f, 0.26f, 1.31f, 0.26f, 2f)
        reflectiveCurveToRelative(-0.1f, 1.36f, -0.26f, 2f)
        horizontalLineToRelative(-3.38f)
        close()
      }
    }.build()
  }

  val NavigateNext: ImageVector by lazy {
    ImageVector.Builder(
      name = "NavigateNext",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(10f, 6f)
        lineTo(8.59f, 7.41f)
        lineTo(13.17f, 12f)
        lineToRelative(-4.58f, 4.59f)
        lineTo(10f, 18f)
        lineToRelative(6f, -6f)
        close()
      }
    }.build()
  }
}
