package com.pantrixui.site.tools.render

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import com.pantrixui.site.core.designsystem.SiteTheme
import com.pantrixui.site.core.model.ShareImage
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.system.exitProcess

/**
 * Draws the share images `--manifest` lists, a JSON list of [ShareImage], into `--out`: each its [ShareCard], in one
 * scene whose card changes, so the site's typefaces are read once.
 */
fun main(args: Array<String>) {
    val options = args.toList().chunked(2).associate { it.first() to it.last() }
    val images = Json.decodeFromString(
        ListSerializer(ShareImage.serializer()),
        File(options.getValue("--manifest")).readText()
    )
    val out = File(options.getValue("--out"))
    var current by mutableStateOf(images.first())
    val scene = ImageComposeScene(ShareImage.WIDTH, ShareImage.HEIGHT, Density(1f)) {
        SiteTheme(dark = false, language = current.language) { ShareCard(current) }
    }
    var time = 0L
    // The typefaces are read while the first frames are drawn: the first card waits for them.
    repeat(WARM_UP) {
        scene.render(time)
        time += FRAME
        Thread.sleep(WAIT)
    }
    images.forEach { image ->
        current = image
        scene.render(time)
        time += FRAME
        val picture = scene.render(time)
        time += FRAME
        val bytes = checkNotNull(picture.encodeToData(EncodedImageFormat.PNG)) { "${image.file} was not encoded" }
        File(out, image.file).apply { parentFile.mkdirs() }.writeBytes(bytes.bytes)
    }
    scene.close()
    println("Drew ${images.size} share images to $out")
    // The desktop's toolkit keeps threads of its own: the program ends here, its work done.
    exitProcess(0)
}

private const val WARM_UP = 20
private const val FRAME = 16_000_000L
private const val WAIT = 50L
