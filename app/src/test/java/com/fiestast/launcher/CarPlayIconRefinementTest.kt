package com.fiestast.launcher

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.fiestast.launcher.android.zlink.AndroidZLinkService
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.navigation.NavRoutes
import com.fiestast.launcher.ui.components.DockItems
import com.fiestast.launcher.ui.icons.AppleCarPlayIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CarPlayIconRefinementTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testAppleCarPlayIconAttributes() {
        assertNotNull(AppleCarPlayIcon)
        assertEquals("AppleCarPlay", AppleCarPlayIcon.name)
        assertEquals(24.dp, AppleCarPlayIcon.defaultWidth)
        assertEquals(24.dp, AppleCarPlayIcon.defaultHeight)
        assertEquals(24f, AppleCarPlayIcon.viewportWidth)
        assertEquals(24f, AppleCarPlayIcon.viewportHeight)
    }

    @Test
    fun testDockItemsContainsZLinkWithCarPlayIcon() {
        val zlinkDockItem = DockItems.find { it.route == NavRoutes.ZLINK }
        assertNotNull("ZLink dock item must exist in DockItems", zlinkDockItem)
        assertEquals("ZLink", zlinkDockItem!!.title)
        assertEquals("dock_zlink", zlinkDockItem.testTag)
        assertEquals(AppleCarPlayIcon, zlinkDockItem.icon)
    }

    @Test
    fun testDockItemsCountAndOrderPreserved() {
        assertEquals(8, DockItems.size)
        val expectedRoutes = listOf(
            NavRoutes.HOME,
            NavRoutes.NAVIGATION,
            NavRoutes.MUSIC,
            NavRoutes.PHONE,
            NavRoutes.RADIO,
            NavRoutes.APPS,
            NavRoutes.SETTINGS,
            NavRoutes.ZLINK
        )
        assertEquals(expectedRoutes, DockItems.map { it.route })
    }

    @Test
    fun testZLinkLaunchBehaviorPreservedWhenNotInstalled() {
        val zlinkService = AndroidZLinkService(context)
        assertFalse(zlinkService.isInstalled.value)
        assertTrue(zlinkService.status.value is ServiceStatus.Unavailable)
        // Calling launchZLink when not installed must safely return false without crashing
        val launched = zlinkService.launchZLink()
        assertFalse(launched)
    }
}
