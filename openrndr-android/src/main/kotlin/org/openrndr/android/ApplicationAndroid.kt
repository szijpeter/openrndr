package org.openrndr.android

import org.openrndr.*
import org.openrndr.draw.Drawer
import org.openrndr.draw.Session
import org.openrndr.math.Vector2
import kotlinx.coroutines.runBlocking
import org.openrndr.internal.Driver
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.VertexBuffer
import org.openrndr.draw.IndexBuffer

class ApplicationAndroid(override var program: Program, override var configuration: Configuration) : Application() {
    var width: Int = 0
    var height: Int = 0

    override var presentationMode: PresentationMode
        get() = TODO("Not yet implemented")
        set(value) {}
    override var windowContentScale: Double
        get() = 1.0
        set(value) {}
    override var windowSize: Vector2
        get() = Vector2(width.toDouble(), height.toDouble())
        set(value) {}
    override var windowPosition: Vector2
        get() = TODO("Not yet implemented")
        set(value) {}
    override var windowResizable: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var windowMultisample: WindowMultisample
        get() = TODO("Not yet implemented")
        set(value) {}
    override var cursorPosition: Vector2
        get() = TODO("Not yet implemented")
        set(value) {}
    override var cursorVisible: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var cursorHideMode: MouseCursorHideMode
        get() = TODO("Not yet implemented")
        set(value) {}
    override var cursorType: CursorType
        get() = TODO("Not yet implemented")
        set(value) {}
    override val pointers: List<Pointer>
        get() = TODO("Not yet implemented")
    override var clipboardContents: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val seconds: Double
        get() = System.currentTimeMillis() / 1000.0
    override var windowTitle: String
        get() = ""
        set(value) {}

    override fun exit() {
        TODO("Not yet implemented")
    }

    override suspend fun setup() {
        program.driver = Driver.instance
        program.drawer = Drawer(Driver.instance)
        program.application = this
        program.width = width
        program.height = height
        runBlocking {
            program.setup()
        }
    }

    fun draw() {
        program.drawImpl()
    }

    override fun loop() {
        // The loop is driven by the Android Choreographer, so we don't need to do anything here.
    }

    override fun requestDraw() {
        // TODO: implement
    }

    override fun requestFocus() {
        // TODO: implement
    }

    override fun createChildWindow(configuration: WindowConfiguration, program: Program): ApplicationWindow {
        TODO("Not yet implemented")
    }
}
