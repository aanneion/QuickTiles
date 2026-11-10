package com.flxholle.quicktiles.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Keeps the three places in sync that have to agree for a tile to work at all:
 *
 * 1. the switch in `root_preferences.xml`,
 * 2. the key to service mapping in [TileRegistry],
 * 3. the `<service>` declaration in `AndroidManifest.xml`.
 *
 * A tile that is in the settings screen but not in the registry has a switch that does nothing, a
 * tile in the registry without a manifest entry cannot be added to Quick Settings, and a tile in the
 * manifest without a registry entry is invisible in the app. All three are silent failures on the
 * device, which makes them worth a test.
 */
class TileRegistryConsistencyTest {

    /**
     * Switches in the settings screen that are not tiles.
     */
    private val nonTileSwitchKeys = setOf(
        "volume_panel_instant",
        "volume_panel_expanded",
        "tile_diagnostics_enabled",
    )

    private val namespace: String =
        TileRegistry::class.java.name.removeSuffix(".utils.TileRegistry")

    @Test
    fun `every tile switch of the settings screen has a service`() {
        val switchKeys = switchKeys()
        assertTrue("No switches found, the test could not read the preference screen",
            switchKeys.isNotEmpty())

        val registry = TileRegistry.preferenceServices()
        val unhandled = switchKeys
            .filter { it !in nonTileSwitchKeys }
            .filter { it !in registry.keys }

        assertEquals("Switches without a tile service: $unhandled", emptyList<String>(), unhandled)
    }

    @Test
    fun `every registered tile has a switch`() {
        val switchKeys = switchKeys()
        val missing = TileRegistry.preferenceServices().keys.filter { it !in switchKeys }

        assertEquals("Tiles without a switch in the settings screen: $missing",
            emptyList<String>(), missing)
    }

    @Test
    fun `every registered tile is declared in the manifest`() {
        val declared = manifestTileServices()
        assertTrue("No tile services found in the manifest, the test could not read it",
            declared.isNotEmpty())

        val missing = TileRegistry.preferenceServices()
            .filterValues { it.name !in declared }
            .keys

        assertEquals("Tiles without a service in AndroidManifest.xml: $missing",
            emptyList<String>(), missing)
    }

    @Test
    fun `every tile service in the manifest can be enabled in the app`() {
        val registered = TileRegistry.preferenceServices().values.map { it.name }.toSet()
        val orphans = manifestTileServices().filter { it !in registered }

        assertEquals("Tile services that cannot be enabled in the app: $orphans",
            emptyList<String>(), orphans)
    }

    private fun switchKeys(): List<String> {
        val document = parse(resourceFile("src/main/res/xml/root_preferences.xml"))
        val nodes = document.getElementsByTagName("SwitchPreferenceCompat")
        return (0 until nodes.length)
            .map { nodes.item(it) as Element }
            .map { it.getAttribute("android:key") }
            .filter { it.isNotEmpty() }
    }

    private fun manifestTileServices(): List<String> {
        val document = parse(resourceFile("src/main/AndroidManifest.xml"))
        val services = document.getElementsByTagName("service")
        val names = mutableListOf<String>()
        for (index in 0 until services.length) {
            val service = services.item(index) as Element
            val isTile = service.getElementsByTagName("action").let { actions ->
                (0 until actions.length).any { actionIndex ->
                    (actions.item(actionIndex) as Element).getAttribute("android:name") ==
                        "android.service.quicksettings.action.QS_TILE"
                }
            }
            if (!isTile) {
                continue
            }
            names.add(qualified(service.getAttribute("android:name")))
        }
        return names
    }

    private fun qualified(name: String): String =
        if (name.startsWith(".")) namespace + name else name

    private fun parse(file: File) =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

    /**
     * Unit tests run in the module directory, but tolerate being started from the repository root.
     */
    private fun resourceFile(relativePath: String): File {
        val fromModule = File(relativePath)
        if (fromModule.exists()) {
            return fromModule
        }
        val fromRoot = File("app/$relativePath")
        assertTrue("Could not find $relativePath", fromRoot.exists())
        return fromRoot
    }
}
