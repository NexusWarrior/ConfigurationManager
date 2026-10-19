package ru.soulways.configurationmanager

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Renders a dependency graph to PNG using only Java2D. */
object GraphPngRenderer {
    /** Saves a deterministic circular graph layout to the requested file. */
    fun render(graph: DependencyGraph, outputFile: Path) {
        val layout = createLayout(graph.nodes.sorted())
        val dimension = imageDimension(graph.nodes.size)
        val image = BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            configure(graphics)
            drawBackground(graphics, dimension)
            drawEdges(graphics, graph, layout)
            drawNodes(graphics, layout)
        } finally {
            graphics.dispose()
        }
        if (!ImageIO.write(image, "png", outputFile.toFile())) {
            throw RepositoryException("PNG image writer is unavailable")
        }
    }

    private fun createLayout(packageNames: List<String>): Map<String, Point2D.Double> {
        val dimension = imageDimension(packageNames.size)
        val center = dimension / COORDINATE_DIVISOR
        val radius = max(MIN_RADIUS, center - IMAGE_MARGIN - NODE_WIDTH / COORDINATE_DIVISOR)
        return packageNames.mapIndexed { index, packageName ->
            val angle = START_ANGLE + FULL_CIRCLE * index / max(packageNames.size, 1)
            val point = Point2D.Double(
                center + radius * cos(angle),
                center + radius * sin(angle),
            )
            packageName to point
        }.toMap()
    }

    private fun imageDimension(nodeCount: Int): Int {
        val circumference = max(nodeCount, 1) * NODE_SPACING
        val radius = max(MIN_RADIUS, circumference / FULL_CIRCLE)
        val requested = ceil(radius * COORDINATE_DIVISOR + IMAGE_MARGIN * COORDINATE_DIVISOR)
        return requested.toInt().coerceIn(MIN_IMAGE_SIZE, MAX_IMAGE_SIZE)
    }

    private fun configure(graphics: Graphics2D) {
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON,
        )
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE)
        graphics.stroke = BasicStroke(EDGE_WIDTH)
    }

    private fun drawBackground(graphics: Graphics2D, dimension: Int) {
        graphics.color = BACKGROUND_COLOR
        graphics.fillRect(0, 0, dimension, dimension)
    }

    private fun drawEdges(
        graphics: Graphics2D,
        graph: DependencyGraph,
        layout: Map<String, Point2D.Double>,
    ) {
        graphics.color = EDGE_COLOR
        graph.adjacency.forEach { (packageName, dependencies) ->
            dependencies.forEach { dependency ->
                drawArrow(graphics, layout.getValue(packageName), layout.getValue(dependency))
            }
        }
    }

    private fun drawArrow(graphics: Graphics2D, source: Point2D.Double, target: Point2D.Double) {
        val directionX = target.x - source.x
        val directionY = target.y - source.y
        val length = sqrt(directionX * directionX + directionY * directionY)
        if (length <= MIN_DIRECTION) return
        val unitX = directionX / length
        val unitY = directionY / length
        val sourceDistance = borderDistance(unitX, unitY)
        val targetDistance = borderDistance(unitX, unitY)
        val start = Point2D.Double(
            source.x + unitX * sourceDistance,
            source.y + unitY * sourceDistance,
        )
        val end = Point2D.Double(
            target.x - unitX * targetDistance,
            target.y - unitY * targetDistance,
        )
        graphics.draw(Line2D.Double(start, end))
        drawArrowHead(graphics, end, unitX, unitY)
    }

    private fun borderDistance(unitX: Double, unitY: Double): Double {
        val horizontal = axisBorderDistance(NODE_WIDTH, unitX)
        val vertical = axisBorderDistance(NODE_HEIGHT, unitY)
        return min(horizontal, vertical)
    }

    private fun axisBorderDistance(size: Double, direction: Double): Double {
        return if (abs(direction) > MIN_DIRECTION) {
            size / COORDINATE_DIVISOR / abs(direction)
        } else {
            MAX_DISTANCE
        }
    }

    private fun drawArrowHead(
        graphics: Graphics2D,
        end: Point2D.Double,
        unitX: Double,
        unitY: Double,
    ) {
        val baseX = end.x - unitX * ARROW_LENGTH
        val baseY = end.y - unitY * ARROW_LENGTH
        val perpendicularX = -unitY * ARROW_WIDTH
        val perpendicularY = unitX * ARROW_WIDTH
        val xCoordinates = intArrayOf(end.x.toInt(), (baseX + perpendicularX).toInt(),
            (baseX - perpendicularX).toInt())
        val yCoordinates = intArrayOf(end.y.toInt(), (baseY + perpendicularY).toInt(),
            (baseY - perpendicularY).toInt())
        graphics.fillPolygon(xCoordinates, yCoordinates, ARROW_POINTS)
    }

    private fun drawNodes(graphics: Graphics2D, layout: Map<String, Point2D.Double>) {
        layout.forEach { (packageName, center) ->
            val left = (center.x - NODE_WIDTH / 2).toInt()
            val top = (center.y - NODE_HEIGHT / 2).toInt()
            graphics.color = NODE_COLOR
            graphics.fillRoundRect(left, top, NODE_WIDTH.toInt(), NODE_HEIGHT.toInt(), ARC, ARC)
            graphics.color = NODE_BORDER_COLOR
            graphics.drawRoundRect(left, top, NODE_WIDTH.toInt(), NODE_HEIGHT.toInt(), ARC, ARC)
            drawLabel(graphics, packageName, center)
        }
    }

    private fun drawLabel(graphics: Graphics2D, value: String, center: Point2D.Double) {
        val label = ellipsize(value)
        val metrics = graphics.fontMetrics
        val x = (center.x - metrics.stringWidth(label) / COORDINATE_DIVISOR).toInt()
        val y = (center.y + (metrics.ascent - metrics.descent) / COORDINATE_DIVISOR).toInt()
        graphics.color = TEXT_COLOR
        graphics.drawString(label, x, y)
    }

    private fun ellipsize(value: String): String {
        return if (value.length <= MAX_LABEL_LENGTH) {
            value
        } else {
            value.take(MAX_LABEL_LENGTH - ELLIPSIS.length) + ELLIPSIS
        }
    }

    private const val MIN_IMAGE_SIZE = 640
    private const val MAX_IMAGE_SIZE = 3_000
    private const val FONT_SIZE = 16
    private const val ARC = 16
    private const val ARROW_POINTS = 3
    private const val MAX_LABEL_LENGTH = 24
    private const val ELLIPSIS = "..."
    private const val EDGE_WIDTH = 2.0f
    private const val NODE_WIDTH = 220.0
    private const val NODE_HEIGHT = 56.0
    private const val NODE_SPACING = 260.0
    private const val MIN_RADIUS = 180.0
    private const val IMAGE_MARGIN = 80.0
    private const val COORDINATE_DIVISOR = 2.0
    private const val FULL_CIRCLE = PI * COORDINATE_DIVISOR
    private const val START_ANGLE = -PI / COORDINATE_DIVISOR
    private const val MIN_DIRECTION = 0.000_001
    private const val MAX_DISTANCE = Double.MAX_VALUE
    private const val ARROW_LENGTH = 14.0
    private const val ARROW_WIDTH = 6.0
    private val BACKGROUND_COLOR = Color(248, 250, 252)
    private val EDGE_COLOR = Color(71, 85, 105)
    private val NODE_COLOR = Color(219, 234, 254)
    private val NODE_BORDER_COLOR = Color(37, 99, 235)
    private val TEXT_COLOR = Color(15, 23, 42)
}
