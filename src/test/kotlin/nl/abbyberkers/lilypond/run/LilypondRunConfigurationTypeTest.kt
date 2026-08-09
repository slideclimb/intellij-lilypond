package nl.abbyberkers.lilypond.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LilypondRunConfigurationTypeTest : BasePlatformTestCase() {
    /**
     * The id is stored in every saved run configuration, so changing it must be a deliberate act.
     */
    fun testIdIsStable() {
        assertEquals("LilypondRunConfiguration", LilypondRunConfigurationType.instance.id)
    }

    fun testTypeIsItsOwnFactory() {
        val type = LilypondRunConfigurationType.instance
        assertEquals(listOf(type), type.configurationFactories.toList())
    }
}
