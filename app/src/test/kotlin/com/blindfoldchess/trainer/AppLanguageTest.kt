package com.blindfoldchess.trainer

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {

    @Test
    fun `fromTag maps french and english tags`() {
        assertEquals(AppLanguage.French, AppLanguage.fromTag("fr-FR"))
        assertEquals(AppLanguage.French, AppLanguage.fromTag("fr"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en-US"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en"))
    }

    @Test
    fun `defaultForDevice follows the JVM locale language`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.FRANCE)
            assertEquals(AppLanguage.French, AppLanguage.defaultForDevice())
            Locale.setDefault(Locale.US)
            assertEquals(AppLanguage.English, AppLanguage.defaultForDevice())
            Locale.setDefault(Locale.GERMANY)
            assertEquals(AppLanguage.English, AppLanguage.defaultForDevice())
        } finally {
            Locale.setDefault(previous)
        }
    }
}
