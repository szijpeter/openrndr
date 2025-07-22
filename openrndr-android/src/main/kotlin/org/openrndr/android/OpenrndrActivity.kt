package org.openrndr.android

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import org.openrndr.Configuration
import org.openrndr.Program
import org.openrndr.internal.Driver
import org.openrndr.internal.DriverVersionGL
import org.openrndr.internal.DriverTypeGL

abstract class OpenrndrActivity : Activity() {
    private lateinit var surfaceView: GLSurfaceView
    lateinit var application: ApplicationAndroid

    abstract fun program(): Program

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surfaceView = GLSurfaceView(this)
        surfaceView.setEGLContextClientVersion(3)
        surfaceView.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
                Driver.driver = DriverGLES(DriverVersionGL.GLES_VERSION_3_0)
                application = ApplicationAndroid(program(), Configuration())
                application.setup()
            }

            override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
                application.width = width
                application.height = height
            }

            override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
                application.draw()
            }
        })
        setContentView(surfaceView)
    }

    override fun onResume() {
        super.onResume()
        surfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        surfaceView.onPause()
    }
}
