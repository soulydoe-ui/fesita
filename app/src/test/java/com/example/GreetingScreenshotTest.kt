package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.fiestast.launcher.android.clock.ClockData
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.components.TopStatusBar
import com.fiestast.launcher.ui.theme.FiestaSTTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      FiestaSTTheme {
        TopStatusBar(
          clockData = ClockData(timeFormatted = "12:00", amPm = "PM", dateFormatted = "Fiesta ST"),
          driverMode = DriverMode.SPORT,
          bluetoothStatus = ServiceStatus.Available("Ready"),
          zlinkStatus = ServiceStatus.Unavailable("Not installed")
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
