/*
 * Copyright (c) 2019-2021, JetBrains s.r.o. and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. JetBrains designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact JetBrains, Na Hrebenech II 1718/10, Prague, 14000, Czech Republic
 * if you need additional information or have any questions.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package org.jetbrains.projector.server.service

import org.jetbrains.projector.awt.font.PFontManager
import org.jetbrains.projector.awt.service.FontProvider
import sun.font.Font2D
import sun.font.PhysicalFont
import java.awt.Font
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.properties.Delegates

object ProjectorFontProvider : FontProvider {

  var isAgent by Delegates.notNull<Boolean>()

  private val defaultRegularFont by lazy { loadFont(DEFAULT_R_NAME, DEFAULT_R_PATH) }
  private val defaultRegularItalicFont by lazy { loadFont(DEFAULT_RI_NAME, DEFAULT_RI_PATH) }
  private val defaultBoldFont by lazy { loadFont(DEFAULT_B_NAME, DEFAULT_B_PATH) }
  private val defaultBoldItalicFont by lazy { loadFont(DEFAULT_BI_NAME, DEFAULT_BI_PATH) }
  private val monoRegularFont by lazy { loadFont(MONO_R_NAME, MONO_R_PATH) }
  private val monoRegularItalicFont by lazy { loadFont(MONO_RI_NAME, MONO_RI_PATH) }
  private val monoBoldFont by lazy { loadFont(MONO_B_NAME, MONO_B_PATH) }
  private val monoBoldItalicFont by lazy { loadFont(MONO_BI_NAME, MONO_BI_PATH) }

  private val allInstalledFonts by lazy {
    fun Font2D.toFont() = Font(getFamilyName(null), style, DEFAULT_SIZE)

    listOf(
      defaultRegularFont,
      defaultRegularItalicFont,
      defaultBoldFont,
      defaultBoldItalicFont,
      monoRegularFont,
      monoRegularItalicFont,
      monoBoldFont,
      monoBoldItalicFont,
    ).map(Font2D::toFont)
  }

  override val installedFonts get() = allInstalledFonts

  override val defaultPhysicalFont: PhysicalFont get() = defaultRegularFont

  override val defaultPlatformFont: Array<String> get() = arrayOf(DEFAULT_FONT_NAME, DEFAULT_FONT_PATH)

  override fun findFont2D(name: String, style: Int, fallback: Int): Font2D {
    when (name) {
      DEFAULT_NAME, DEFAULT_R_NAME, DEFAULT_RI_NAME, DEFAULT_B_NAME, DEFAULT_BI_NAME -> return when (style) {
        Font.BOLD or Font.ITALIC -> defaultBoldItalicFont
        Font.BOLD -> defaultBoldFont
        Font.ITALIC -> defaultRegularItalicFont
        else -> defaultRegularFont
      }
      MONO_NAME, MONO_R_NAME, MONO_RI_NAME, MONO_B_NAME, MONO_BI_NAME -> return when (style) {
        Font.BOLD or Font.ITALIC -> monoBoldItalicFont
        Font.BOLD -> monoBoldFont
        Font.ITALIC -> monoRegularItalicFont
        else -> monoRegularFont
      }
    }

    return when (isMonospacedFont(name)) {
      true -> when (style) {
        Font.BOLD or Font.ITALIC -> monoBoldItalicFont
        Font.BOLD -> monoBoldFont
        Font.ITALIC -> monoRegularItalicFont
        else -> monoRegularFont
      }
      false -> when (style) {
        Font.BOLD or Font.ITALIC -> defaultBoldItalicFont
        Font.BOLD -> defaultBoldFont
        Font.ITALIC -> defaultRegularItalicFont
        else -> defaultRegularFont
      }
    }
  }

  private fun isMonospacedFont(name: String): Boolean {
    return "mono" in name.lowercase() || name.lowercase() == "menlo"
  }

  private fun loadFont(fontName: String, fontPath: String): PhysicalFont {
    val tempFile = File.createTempFile(fontName, ".ttf").apply {
      deleteOnExit()
    }

    val link = PFontManager::class.java.getResourceAsStream(fontPath)!!
    Files.copy(link, tempFile.absoluteFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    return PFontManager.createFont2D(tempFile, Font.TRUETYPE_FONT, false, false, null).single() as PhysicalFont
  }

  private const val DEFAULT_NAME = "Inter"

  private const val DEFAULT_R_NAME = "Default-R"
  private const val DEFAULT_R_PATH = "/fonts/Default-R.otf"

  private const val DEFAULT_RI_NAME = "Default-RI"
  private const val DEFAULT_RI_PATH = "/fonts/Default-RI.otf"

  private const val DEFAULT_B_NAME = "Default-B"
  private const val DEFAULT_B_PATH = "/fonts/Default-B.otf"

  private const val DEFAULT_BI_NAME = "Default-BI"
  private const val DEFAULT_BI_PATH = "/fonts/Default-BI.otf"


  private const val MONO_NAME = "Sarasa Mono J"

  private const val MONO_R_NAME = "Mono-R"
  private const val MONO_R_PATH = "/fonts/Mono-R.ttf"

  private const val MONO_RI_NAME = "Mono-RI"
  private const val MONO_RI_PATH = "/fonts/Mono-RI.ttf"

  private const val MONO_B_NAME = "Mono-B"
  private const val MONO_B_PATH = "/fonts/Mono-B.ttf"

  private const val MONO_BI_NAME = "Mono-BI"
  private const val MONO_BI_PATH = "/fonts/Mono-BI.ttf"

  private const val DEFAULT_FONT_NAME = DEFAULT_NAME
  private const val DEFAULT_FONT_PATH = DEFAULT_R_PATH

  private const val DEFAULT_SIZE = 12
}
