package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MarioViewModel
import com.example.ui.PowerupType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Super Mario Classic", appName)
  }

  @Test
  fun `test mario sizing height changes`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = MarioViewModel(context as android.app.Application)
    
    // Default size is SMALL
    assertEquals("SMALL", viewModel.marioSize.value)
    assertEquals(22f, viewModel.getMarioHeight())
    
    // Change to BIG
    viewModel.marioSize.value = "BIG"
    assertEquals(38f, viewModel.getMarioHeight())
    
    // Change to FIRE
    viewModel.marioSize.value = "FIRE"
    assertEquals(38f, viewModel.getMarioHeight())
  }

  @Test
  fun `test starman state activation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = MarioViewModel(context as android.app.Application)
    
    assertFalse(viewModel.isStarman.value)
    
    // Activate starman
    viewModel.isStarman.value = true
    viewModel.starmanTimer.value = 150
    
    assertTrue(viewModel.isStarman.value)
    assertEquals(150, viewModel.starmanTimer.value)
  }

  @Test
  fun `test score state bounds`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = MarioViewModel(context as android.app.Application)
    
    viewModel.score.value = 0
    viewModel.score.value += 100
    assertEquals(100, viewModel.score.value)
  }
}
