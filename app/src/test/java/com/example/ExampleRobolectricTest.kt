package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.doctor.CodeXDoctor
import com.example.core.fs.WorkspaceManager
import com.example.core.languages.LanguageRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CodeX Studio", appName)
  }

  @Test
  fun `language registry detects languages correctly`() {
    val py = LanguageRegistry.detectByExtension("script.py")
    assertEquals("Python", py.name)

    val kt = LanguageRegistry.detectByExtension("MainActivity.kt")
    assertEquals("Kotlin", kt.name)

    val cpp = LanguageRegistry.detectByExtension("engine.cpp")
    assertEquals("C / C++", cpp.name)
  }

  @Test
  fun `workspace manager creates starter project template`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val wm = WorkspaceManager(context)
    val proj = wm.createFromTemplate("python_cli", "TestProject")
    assertTrue(proj.exists())
    assertTrue(File(proj, "main.py").exists())
  }

  @Test
  fun `codex doctor returns system diagnostic checks`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val doctor = CodeXDoctor(context)
    val checks = doctor.runDiagnostics()
    assertTrue(checks.isNotEmpty())
    assertNotNull(checks.firstOrNull { it.title.contains("Android OS") })
  }
}

