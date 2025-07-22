package org.openrndr.android.demo

import org.openrndr.Program
import org.openrndr.android.OpenrndrActivity
import org.openrndr.color.ColorRGBa
import org.openrndr.shape.Circle

class MainActivity : OpenrndrActivity() {
    override fun program(): Program {
        return object : Program() {
            override fun draw() {
                drawer.clear(ColorRGBa.BLACK)
                drawer.fill = ColorRGBa.WHITE
                drawer.circle(Circle(width / 2.0, height / 2.0, 100.0))
            }
        }
    }
}
