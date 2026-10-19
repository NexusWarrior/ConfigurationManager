package ru.soulways.configurationmanager

import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

class GraphPngRendererTest {
    @Test
    fun `creates a readable PNG image`() {
        val graph = DependencyGraph(
            mapOf(
                "A" to setOf("B", "C"),
                "B" to setOf("C"),
                "C" to emptySet(),
            ),
        )
        val output = Files.createTempFile("dependency-graph", ".png")

        GraphPngRenderer.render(graph, output)

        val image = ImageIO.read(output.toFile())
        assertTrue(Files.size(output) > EMPTY_FILE_SIZE)
        assertTrue(image.width > EMPTY_IMAGE_SIZE)
        assertTrue(image.height > EMPTY_IMAGE_SIZE)
    }

    private companion object {
        const val EMPTY_FILE_SIZE = 0L
        const val EMPTY_IMAGE_SIZE = 0
    }
}
