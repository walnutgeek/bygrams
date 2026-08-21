package com.walnutgeek.bygrams.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the two things about the launcher icon that break silently.
 *
 * Only the centre 72dp of the 108dp adaptive canvas is ever drawn, and Pixel's
 * launcher masks that to a circle. Art that strays outside the safe radius gets its
 * corners sliced off — and nothing in the build says so. The first version of this
 * icon was drawn to the full canvas and would have lost the corners of the scale body.
 *
 * The themed-icon layer has the matching trap: launchers tint every opaque pixel, so
 * the "42g" readout only stays readable because it is genuine transparency in a single
 * compound path. Anyone "simplifying" it into a terracotta rectangle painted over the
 * body would see it vanish in themed mode only.
 */
class LauncherIconTest {

    // Gradle runs unit tests with the module directory (app/) as the working directory.
    private val res = File("src/main/res")

    private val centreX = 54.0
    private val centreY = 54.0
    private val safeRadius = 34.0

    private fun vector(name: String) = File(res, "drawable/$name.xml").readText()

    private fun pathData(xml: String): String =
        Regex("""android:pathData="([^"]+)"""").find(xml)?.groupValues?.get(1)
            ?: error("no pathData found")

    /**
     * Every coordinate in the path, control points included.
     *
     * This has to respect command arity rather than just pairing off numbers: the
     * generator emits H and V shorthand, which take a *single* argument. Blindly
     * chunking into pairs desynchronises everything after the first H and reports a
     * radius that is simply wrong.
     */
    private fun points(d: String): List<Pair<Double, Double>> {
        val tokens = Regex("""[A-Za-z]|-?\d+(?:\.\d+)?(?:[eE]-?\d+)?""").findAll(d)
            .map { it.value }.toList()
        val out = mutableListOf<Pair<Double, Double>>()
        var x = 0.0
        var y = 0.0
        var i = 0
        var cmd = ' '
        while (i < tokens.size) {
            val t = tokens[i]
            if (t.length == 1 && t[0].isLetter()) {
                cmd = t[0]
                i++
                if (cmd == 'Z' || cmd == 'z') continue
            }
            require(cmd.isUpperCase()) { "relative command '$cmd' not handled" }
            fun num(): Double = tokens[i++].toDouble()
            when (cmd) {
                'M', 'L', 'T' -> { x = num(); y = num() }
                'H' -> x = num()
                'V' -> y = num()
                'C' -> {
                    val a = num() to num(); val b = num() to num()
                    out += a; out += b; x = num(); y = num()
                }
                'Q', 'S' -> {
                    val a = num() to num()
                    out += a; x = num(); y = num()
                }
                else -> error("unhandled path command '$cmd'")
            }
            out += x to y
        }
        assertTrue("path data should yield coordinates", out.isNotEmpty())
        return out
    }

    private fun maxRadius(d: String): Double =
        points(d).maxOf { (x, y) ->
            Math.hypot(x - centreX, y - centreY)
        }

    @Test
    fun `foreground art stays inside the adaptive safe zone`() {
        val r = maxRadius(pathData(vector("ic_launcher_foreground")))
        assertTrue(
            "Foreground art reaches radius %.2f but the circular mask safe zone is %.2f — "
                .format(r, safeRadius) + "corners would be clipped on Pixel launchers.",
            r <= safeRadius
        )
    }

    @Test
    fun `monochrome art stays inside the adaptive safe zone`() {
        val r = maxRadius(pathData(vector("ic_launcher_monochrome")))
        assertTrue(
            "Monochrome art reaches radius %.2f, safe zone is %.2f.".format(r, safeRadius),
            r <= safeRadius
        )
    }

    @Test
    fun `monochrome and foreground share the same geometry`() {
        assertTrue(
            "The themed layer must be the same shape as the foreground; if they drift, " +
                "themed icons stop matching the real one.",
            pathData(vector("ic_launcher_monochrome")) == pathData(vector("ic_launcher_foreground"))
        )
    }

    @Test
    fun `readout is a hole, not a painted shape`() {
        // One path, many subpaths: the glyphs are subtracted from the body rather than
        // drawn on top of it. Multiple <path> elements would mean someone painted them.
        val xml = vector("ic_launcher_monochrome")
        val pathCount = Regex("""<path\b""").findAll(xml).count()
        assertTrue("themed layer must be a single compound path, found $pathCount", pathCount == 1)
        val subpaths = pathData(xml).count { it == 'M' || it == 'm' }
        assertTrue("expected the readout and groove as subpaths, found $subpaths", subpaths >= 5)
    }

    @Test
    fun `adaptive icon declares all three layers`() {
        for (name in listOf("ic_launcher", "ic_launcher_round")) {
            val xml = File(res, "mipmap-anydpi-v26/$name.xml").readText()
            for (layer in listOf("background", "foreground", "monochrome")) {
                assertTrue("$name.xml is missing <$layer>", xml.contains("<$layer"))
            }
        }
    }
}
