package org.openrndr.android

import org.openrndr.draw.*
import org.openrndr.internal.Driver
import org.openrndr.internal.DriverVersionGL
import org.openrndr.internal.FontMapManager
import org.openrndr.internal.ResourceThread
import org.openrndr.internal.ShaderGenerators
import org.openrndr.color.ColorRGBa
import java.nio.Buffer
import org.lwjgl.opengles.GLES32.*
import org.openrndr.internal.DriverTypeGL
import org.openrndr.internal.glcommon.ShaderGeneratorsGLCommon
import org.openrndr.math.Matrix33
import org.openrndr.math.Matrix44
import org.openrndr.shape.Rectangle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.min
import io.github.oshai.kotlinlogging.KotlinLogging
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL40C
import org.lwjgl.opengl.GL42C
import org.lwjgl.opengl.KHRBlendEquationAdvanced
import java.net.URL

private val logger = KotlinLogging.logger {}

class DriverGLES(val version: DriverVersionGL) : Driver {
    private val executionQueue = mutableListOf<() -> Unit>()

    fun executeOnMainThread(f: () -> Unit) {
        synchronized(executionQueue) {
            executionQueue.add(f)
        }
    }

    fun processMainThreadExecutables() {
        synchronized(executionQueue) {
            for (runnable in executionQueue) {
                try {
                    runnable()
                } catch (e: Throwable) {
                    throw e
                }
            }
            executionQueue.clear()
        }
    }

    data class Capabilities(
        val programUniform: Boolean,
        val textureStorage: Boolean,
        val textureMultisampleStorage: Boolean,
        val compute: Boolean,
    )

    val capabilities = Capabilities(
        programUniform = version.isAtLeast(DriverVersionGL.GL_VERSION_4_1, DriverVersionGL.GLES_VERSION_3_1),
        textureStorage = version.isAtLeast(DriverVersionGL.GL_VERSION_4_1, DriverVersionGL.GLES_VERSION_3_0),
        textureMultisampleStorage = version.isAtLeast(DriverVersionGL.GL_VERSION_4_3, DriverVersionGL.GLES_VERSION_3_1),
        compute = version.isAtLeast(DriverVersionGL.GL_VERSION_4_3, DriverVersionGL.GLES_VERSION_3_1)
    )

    override val properties: DriverProperties by lazy {
        DriverProperties(
            maxRenderTargetSamples = glGetInteger(GL_MAX_SAMPLES),
            maxTextureSamples = glGetInteger(GL_MAX_SAMPLES),
            maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE),
        )
    }

    override val contextID: Long
        get() = 0

    override val shaderLanguage: ShaderLanguage
        get() = GLSL(version.glslVersion)

    override fun createComputeStyleManager(session: Session?): ComputeStyleManager {
        TODO("Not yet implemented")
    }

    override val fontImageMapManager: FontMapManager
        get() = TODO("Not yet implemented")
    override val fontVectorMapManager: FontMapManager
        get() = TODO("Not yet implemented")
    override val shaderGenerators: ShaderGenerators
        get() = ShaderGeneratorsGLCommon()
    override val activeRenderTarget: RenderTarget
        get() = TODO("Not yet implemented")

    override fun shaderConfiguration(type: ShaderType): String = """
        #version ${version.glslVersion}
        #define OR_IN_OUT
        ${
        if (type == ShaderType.FRAGMENT) {
            """#extension GL_KHR_blend_equation_advanced : enable
            |#ifdef GL_KHR_blend_equation_advanced
            |layout(blend_support_all_equations) out;
            |#endif
            |
        """.trimMargin()
        } else ""
    }

        ${
        when (version.type) {
            DriverTypeGL.GL -> "#define OR_GL"
            DriverTypeGL.GLES -> """#define OR_GLES
          |precision highp float;
          |precision highp sampler2DArray;
          |precision highp image2DArray;
          |precision highp imageCube;
          |precision highp imageCubeArray;
          |${
                if (version >= DriverVersionGL.GLES_VERSION_3_1) {
                    "precision highp image2D; precision highp image3D;"
                } else {
                    ""
                }
            }
      """.trimMargin()
        }
    }
    """.trimIndent()

    override fun internalShaderResource(resourceId: String): String {
        TODO("Not yet implemented")
    }

    override fun createShader(
        vsCode: String,
        tcsCode: String?,
        tesCode: String?,
        gsCode: String?,
        fsCode: String,
        name: String,
        session: Session?
    ): Shader {
        logger.trace {
            "creating shader:\n${vsCode}\n${fsCode}"
        }
        val vertexShader = VertexShaderGLES.fromString(vsCode, name)
        val fragmentShader = FragmentShaderGLES.fromString(fsCode, name)

        synchronized(this) {
            return ShaderGLES.create(
                vertexShader,
                fragmentShader,
                name,
                session
            )
        }
    }

    override fun createComputeShader(code: String, name: String, session: Session?): ComputeShader {
        TODO("Not yet implemented")
    }

    override fun createAtomicCounterBuffer(counterCount: Int, session: Session?): AtomicCounterBuffer {
        TODO("Not yet implemented")
    }

    override fun createColorBuffer(
        width: Int,
        height: Int,
        contentScale: Double,
        format: ColorFormat,
        type: ColorType,
        multisample: BufferMultisample,
        levels: Int,
        session: Session?
    ): ColorBuffer {
        TODO("Not yet implemented")
    }

    override fun createDepthBuffer(
        width: Int,
        height: Int,
        format: DepthFormat,
        multisample: BufferMultisample,
        session: Session?
    ): DepthBuffer {
        TODO("Not yet implemented")
    }

    override fun createRenderTarget(
        width: Int,
        height: Int,
        contentScale: Double,
        multisample: BufferMultisample,
        session: Session?
    ): RenderTarget {
        TODO("Not yet implemented")
    }

    override fun createCubemap(
        width: Int,
        format: ColorFormat,
        type: ColorType,
        levels: Int,
        session: Session?
    ): Cubemap {
        TODO("Not yet implemented")
    }

    override fun createArrayCubemap(
        width: Int,
        layers: Int,
        format: ColorFormat,
        type: ColorType,
        levels: Int,
        session: Session?
    ): ArrayCubemap {
        TODO("Not yet implemented")
    }

    override fun createVolumeTexture(
        width: Int,
        height: Int,
        depth: Int,
        format: ColorFormat,
        type: ColorType,
        levels: Int,
        session: Session?
    ): VolumeTexture {
        TODO("Not yet implemented")
    }

    override fun createArrayTexture(
        width: Int,
        height: Int,
        layers: Int,
        format: ColorFormat,
        type: ColorType,
        levels: Int,
        session: Session?
    ): ArrayTexture {
        TODO("Not yet implemented")
    }

    override fun createBufferTexture(elementCount: Int, format: ColorFormat, type: ColorType, session: Session?): BufferTexture {
        TODO("Not yet implemented")
    }

    override fun createDynamicVertexBuffer(format: VertexFormat, vertexCount: Int, session: Session?): VertexBuffer {
        synchronized(this) {
            val vertexBuffer = VertexBufferGLES.createDynamic(format, vertexCount, session)
            session?.track(vertexBuffer)
            return vertexBuffer
        }
    }

    override fun createStaticVertexBuffer(format: VertexFormat, buffer: Buffer, session: Session?): VertexBuffer {
        TODO("Not yet implemented")
    }

    override fun createDynamicIndexBuffer(elementCount: Int, type: IndexType, session: Session?): IndexBuffer {
        synchronized(this) {
            val indexBuffer = IndexBufferGLES.create(elementCount, type, session)
            session?.track(indexBuffer)
            return indexBuffer
        }
    }

    override fun createShaderStorageBuffer(format: ShaderStorageFormat, session: Session?): ShaderStorageBuffer {
        TODO("Not yet implemented")
    }

    override fun createShadeStyleManager(
        name: String,
        vsGenerator: (ShadeStructure) -> String,
        tcsGenerator: ((ShadeStructure) -> String)?,
        tesGenerator: ((ShadeStructure) -> String)?,
        gsGenerator: ((ShadeStructure) -> String)?,
        fsGenerator: (ShadeStructure) -> String,
        session: Session?
    ): ShadeStyleManager {
        TODO("Not yet implemented")
    }

    override fun createDrawThread(session: Session?): DrawThread {
        TODO("Not yet implemented")
    }

    override fun createResourceThread(session: Session?, f: () -> Unit): ResourceThread {
        TODO("Not yet implemented")
    }

    override fun clear(color: ColorRGBa) {
        glClearColor(color.r.toFloat(), color.g.toFloat(), color.b.toFloat(), color.alpha.toFloat())
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }

    private val vaos = mutableMapOf<ShaderVertexDescription, Int>()
    private val defaultVAO: Int
        get() {
            val vaos = IntArray(1)
            glGenVertexArrays(vaos)
            return vaos[0]
        }


    override fun drawVertexBuffer(
        shader: Shader,
        vertexBuffers: List<VertexBuffer>,
        drawPrimitive: DrawPrimitive,
        vertexOffset: Int,
        vertexCount: Int,
        verticesPerPatch: Int
    ) {
        debugGLErrors {
            "a pre-existing GL error occurred before Driver.drawVertexBuffer "
        }

        shader as ShaderGLES
        // -- find or create a VAO for our shader + vertex buffers combination
        val shaderVertexDescription = ShaderVertexDescription(
            contextID,
            shader.programObject,
            IntArray(vertexBuffers.size) { (vertexBuffers[it] as VertexBufferGLES).buffer },
            IntArray(0)
        )

        val vao = vaos.getOrPut(shaderVertexDescription) {
            logger.debug {
                "[context=$contextID] creating new VAO for hash $shaderVertexDescription"
            }

            val arrays = IntArray(1)
            synchronized(this) {
                glGenVertexArrays(arrays)
                glBindVertexArray(arrays[0])
                setupFormat(vertexBuffers, emptyList(), shader)
                glBindVertexArray(defaultVAO)
            }
            arrays[0]
        }
        glBindVertexArray(vao)
        debugGLErrors {
            when (it) {
                GL_INVALID_OPERATION -> "array ($vao) is not zero or the name of a vertex array object previously returned from a call to glGenVertexArrays"
                else -> "unknown error $it"
            }
        }

        logger.trace { "drawing vertex buffer with $drawPrimitive(${drawPrimitive.glType()}) and $vertexCount vertices with vertexOffset $vertexOffset " }
        glDrawArrays(drawPrimitive.glType(), vertexOffset, vertexCount)

        debugGLErrors {
            when (it) {
                GL_INVALID_ENUM -> "mode ($drawPrimitive) is not an accepted value."
                GL_INVALID_VALUE -> "count ($vertexCount) is negative."
                GL_INVALID_OPERATION -> "a non-zero buffer object name is bound to an enabled array and the buffer object's data store is currently mapped."
                else -> null
            }
        }
        // -- restore defaultVAO binding
        glBindVertexArray(defaultVAO)
    }

    override fun drawIndexedVertexBuffer(
        shader: Shader,
        indexBuffer: IndexBuffer,
        vertexBuffers: List<VertexBuffer>,
        drawPrimitive: DrawPrimitive,
        indexOffset: Int,
        indexCount: Int,
        verticesPerPatch: Int
    ) {

        shader as ShaderGLES
        indexBuffer as IndexBufferGLES

        // -- find or create a VAO for our shader + vertex buffers combination
        val shaderVertexDescription =
            ShaderVertexDescription(
                contextID,
                shader.programObject,
                IntArray(vertexBuffers.size) { (vertexBuffers[it] as VertexBufferGLES).buffer },
                IntArray(0)
            )

        val vao =
            vaos.getOrPut(shaderVertexDescription) {
                logger.debug {
                    "creating new VAO for hash $shaderVertexDescription"
                }
                val arrays = IntArray(1)
                synchronized(this) {
                    glGenVertexArrays(arrays)
                    glBindVertexArray(arrays[0])
                    setupFormat(vertexBuffers, emptyList(), shader)
                    glBindVertexArray(defaultVAO)
                }
                arrays[0]
            }


        glBindVertexArray(vao)


        //logger.trace { "drawing vertex buffer with $drawPrimitive(${drawPrimitive.glType()}) and $indexCount indices with indexOffset $indexOffset " }
        indexBuffer.bind()


        glDrawElements(drawPrimitive.glType(), indexCount, indexBuffer.type.glType(), indexOffset.toLong())


        debugGLErrors {
            when (it) {
                GL_INVALID_ENUM -> "mode ($drawPrimitive) is not an accepted value."
                GL_INVALID_VALUE -> "count ($indexCount) is negative."
                GL_INVALID_OPERATION -> "a non-zero buffer object name is bound to an enabled array and the buffer object's data store is currently mapped."
                else -> null
            }
        }

        // -- restore defaultVAO binding
        glBindVertexArray(defaultVAO)
    }

    override fun drawInstances(
        shader: Shader,
        vertexBuffers: List<VertexBuffer>,
        instanceAttributes: List<VertexBuffer>,
        drawPrimitive: DrawPrimitive,
        vertexOffset: Int,
        vertexCount: Int,
        instanceOffset: Int,
        instanceCount: Int,
        verticesPerPatch: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun drawIndexedInstances(
        shader: Shader,
        indexBuffer: IndexBuffer,
        vertexBuffers: List<VertexBuffer>,
        instanceAttributes: List<VertexBuffer>,
        drawPrimitive: DrawPrimitive,
        indexOffset: Int,
        indexCount: Int,
        instanceOffset: Int,
        instanceCount: Int,
        verticesPerPatch: Int
    ) {
        TODO("Not yet implemented")
    }

    private var dirty = true
    private var cached = DrawStyle()

    override fun setState(drawStyle: DrawStyle) {
        if (dirty || cached.clip != drawStyle.clip) {
            if (drawStyle.clip != null) {
                drawStyle.clip?.let { it: Rectangle ->
                    val target = RenderTarget.active
                    glEnable(GL_SCISSOR_TEST)
                    glScissor(
                        (it.x * target.contentScale).toInt(),
                        (target.height * target.contentScale - it.y * target.contentScale - it.height * target.contentScale).toInt(),
                        (it.width * target.contentScale).toInt(),
                        (it.height * target.contentScale).toInt()
                    )

                }
            } else {
                glDisable(GL_SCISSOR_TEST)
            }
            cached.clip = drawStyle.clip
        }

        if (dirty || cached.channelWriteMask != drawStyle.channelWriteMask) {
            glColorMask(
                drawStyle.channelWriteMask.red,
                drawStyle.channelWriteMask.green,
                drawStyle.channelWriteMask.blue,
                drawStyle.channelWriteMask.alpha
            )
            cached.channelWriteMask = drawStyle.channelWriteMask
        }

        if (dirty || cached.depthWrite != drawStyle.depthWrite) {
            when (drawStyle.depthWrite) {
                true -> glDepthMask(true)
                false -> glDepthMask(false)
            }
            glEnable(GL_DEPTH_TEST)
            debugGLErrors()
            cached.depthWrite = drawStyle.depthWrite
        }

        if (dirty || cached.stencil != drawStyle.stencil || cached.backStencil != drawStyle.backStencil || cached.frontStencil != drawStyle.frontStencil) {
            if (drawStyle.frontStencil === drawStyle.backStencil) {
                if (drawStyle.stencil.stencilTest == StencilTest.DISABLED) {
                    glDisable(GL_STENCIL_TEST)
                } else {
                    glEnable(GL_STENCIL_TEST)
                    glStencilFuncSeparate(
                        GL_FRONT_AND_BACK,
                        glStencilTest(drawStyle.stencil.stencilTest),
                        drawStyle.stencil.stencilTestReference,
                        drawStyle.stencil.stencilTestMask
                    )
                    debugGLErrors()
                    glStencilOpSeparate(
                        GL_FRONT_AND_BACK,
                        glStencilOp(drawStyle.stencil.stencilFailOperation),
                        glStencilOp(drawStyle.stencil.depthFailOperation),
                        glStencilOp(drawStyle.stencil.depthPassOperation)
                    )
                    debugGLErrors()
                    glStencilMaskSeparate(GL_FRONT_AND_BACK, drawStyle.stencil.stencilWriteMask)
                    debugGLErrors()
                }
            } else {
                require(drawStyle.frontStencil.stencilTest != StencilTest.DISABLED)
                require(drawStyle.backStencil.stencilTest != StencilTest.DISABLED)
                glEnable(GL_STENCIL_TEST)
                glStencilFuncSeparate(
                    GL_FRONT,
                    glStencilTest(drawStyle.frontStencil.stencilTest),
                    drawStyle.frontStencil.stencilTestReference,
                    drawStyle.frontStencil.stencilTestMask
                )
                glStencilFuncSeparate(
                    GL_BACK,
                    glStencilTest(drawStyle.backStencil.stencilTest),
                    drawStyle.backStencil.stencilTestReference,
                    drawStyle.backStencil.stencilTestMask
                )
                glStencilOpSeparate(
                    GL_FRONT,
                    glStencilOp(drawStyle.frontStencil.stencilFailOperation),
                    glStencilOp(drawStyle.frontStencil.depthFailOperation),
                    glStencilOp(drawStyle.frontStencil.depthPassOperation)
                )
                glStencilOpSeparate(
                    GL_BACK,
                    glStencilOp(drawStyle.backStencil.stencilFailOperation),
                    glStencilOp(drawStyle.backStencil.depthFailOperation),
                    glStencilOp(drawStyle.backStencil.depthPassOperation)
                )
                glStencilMaskSeparate(GL_FRONT, drawStyle.frontStencil.stencilWriteMask)
                glStencilMaskSeparate(GL_BACK, drawStyle.backStencil.stencilWriteMask)
            }
            cached.stencil = drawStyle.stencil.copy()
            cached.frontStencil = drawStyle.frontStencil.copy()
            cached.backStencil = drawStyle.backStencil.copy()
        }

        if (dirty || cached.blendMode != drawStyle.blendMode) {

            fun setAdvancedEq(eq: Int) {
                glEnable(GL_BLEND)
                glBlendEquation(eq)
                glBlendFunc(GL_ONE, GL_ONE)
            }

            when (drawStyle.blendMode) {
                BlendMode.OVER -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_FUNC_ADD)
                    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
                }

                BlendMode.BLEND -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_FUNC_ADD)
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                }

                BlendMode.ADD -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_FUNC_ADD)
                    glBlendFunc(GL_ONE, GL_ONE)
                }

                BlendMode.REPLACE -> {
                    glDisable(GL_BLEND)
                }

                BlendMode.SUBTRACT -> {
                    glEnable(GL_BLEND)
                    glBlendEquationSeparate(GL_FUNC_REVERSE_SUBTRACT, GL_FUNC_ADD)
                    glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ONE)
                }

                BlendMode.MULTIPLY -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_FUNC_ADD)
                    glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA)
                }

                BlendMode.REMOVE -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_FUNC_ADD)
                    glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA)
                }

                BlendMode.MIN -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_MIN)
                    glBlendFunc(GL_ONE, GL_ONE)
                }

                BlendMode.MAX -> {
                    glEnable(GL_BLEND)
                    glBlendEquation(GL_MAX)
                    glBlendFunc(GL_ONE, GL_ONE)
                }

                BlendMode.SCREEN -> setAdvancedEq(GL_SCREEN_KHR)
                BlendMode.OVERLAY -> setAdvancedEq(GL_OVERLAY_KHR)
                BlendMode.DARKEN -> setAdvancedEq(GL_DARKEN_KHR)
                BlendMode.LIGHTEN -> setAdvancedEq(GL_LIGHTEN_KHR)
                BlendMode.COLOR_DODGE -> setAdvancedEq(GL_COLORDODGE_KHR)
                BlendMode.COLOR_BURN -> setAdvancedEq(GL_COLORBURN_KHR)
                BlendMode.HARD_LIGHT -> setAdvancedEq(GL_HARDLIGHT_KHR)
                BlendMode.SOFT_LIGHT -> setAdvancedEq(GL_SOFTLIGHT_KHR)
                BlendMode.DIFFERENCE -> setAdvancedEq(GL_DIFFERENCE_KHR)
                BlendMode.EXCLUSION -> setAdvancedEq(GL_EXCLUSION_KHR)
                BlendMode.HSL_HUE -> setAdvancedEq(GL_HSL_HUE_KHR)
                BlendMode.HSL_SATURATION -> setAdvancedEq(GL_HSL_SATURATION_KHR)
                BlendMode.HSL_COLOR -> setAdvancedEq(GL_HSL_COLOR_KHR)
                BlendMode.HSL_LUMINOSITY -> setAdvancedEq(GL_HSL_LUMINOSITY_KHR)
            }
            cached.blendMode = drawStyle.blendMode
        }
        if (dirty || cached.alphaToCoverage != drawStyle.alphaToCoverage) {
            if (drawStyle.alphaToCoverage) {
                glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE)
                glDisable(GL_BLEND)
            } else {
                glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE)
            }
            cached.alphaToCoverage = drawStyle.alphaToCoverage
        }

        if (dirty || cached.depthTestPass != drawStyle.depthTestPass) {
            when (drawStyle.depthTestPass) {
                DepthTestPass.ALWAYS -> {
                    glDepthFunc(GL_ALWAYS)
                }

                DepthTestPass.GREATER -> {
                    glDepthFunc(GL_GREATER)
                }

                DepthTestPass.GREATER_OR_EQUAL -> {
                    glDepthFunc(GL_GEQUAL)
                }

                DepthTestPass.LESS -> {
                    glDepthFunc(GL_LESS)
                }

                DepthTestPass.LESS_OR_EQUAL -> {
                    glDepthFunc(GL_LEQUAL)
                }

                DepthTestPass.EQUAL -> {
                    glDepthFunc(GL_EQUAL)
                }

                DepthTestPass.NEVER -> {
                    glDepthFunc(GL_NEVER)
                }
            }
            debugGLErrors()
            cached.depthTestPass = drawStyle.depthTestPass
        }

        if (dirty || cached.cullTestPass != drawStyle.cullTestPass) {
            when (drawStyle.cullTestPass) {
                CullTestPass.ALWAYS -> {
                    glDisable(GL_CULL_FACE)
                }

                CullTestPass.FRONT -> {
                    glEnable(GL_CULL_FACE)
                    glCullFace(GL_BACK)
                }

                CullTestPass.BACK -> {
                    glEnable(GL_CULL_FACE)
                    glCullFace(GL_FRONT)
                }

                CullTestPass.NEVER -> {
                    glEnable(GL_CULL_FACE)
                    glCullFace(GL_FRONT_AND_BACK)
                }
            }
            cached.cullTestPass = drawStyle.cullTestPass
        }
        dirty = false
        debugGLErrors()
    }

    override fun destroyContext(context: Long) {
        TODO("Not yet implemented")
    }

    override fun finish() {
        glFinish()
    }

    private fun setupFormat(
        vertexBuffer: List<VertexBuffer>,
        instanceAttributes: List<VertexBuffer>,
        shader: ShaderGLES
    ) {
        run {
            debugGLErrors()

            val scalarVectorTypes = setOf(
                VertexElementType.UINT8,
                VertexElementType.VECTOR2_UINT8,
                VertexElementType.VECTOR3_UINT8,
                VertexElementType.VECTOR4_UINT8,
                VertexElementType.INT8,
                VertexElementType.VECTOR2_INT8,
                VertexElementType.VECTOR3_INT8,
                VertexElementType.VECTOR4_INT8,
                VertexElementType.UINT16,
                VertexElementType.VECTOR2_UINT16,
                VertexElementType.VECTOR3_UINT16,
                VertexElementType.VECTOR4_UINT16,
                VertexElementType.INT16,
                VertexElementType.VECTOR2_INT16,
                VertexElementType.VECTOR3_INT16,
                VertexElementType.VECTOR4_INT16,
                VertexElementType.UINT32,
                VertexElementType.VECTOR2_UINT32,
                VertexElementType.VECTOR3_UINT32,
                VertexElementType.VECTOR4_UINT32,
                VertexElementType.INT32,
                VertexElementType.VECTOR2_INT32,
                VertexElementType.VECTOR3_INT32,
                VertexElementType.VECTOR4_INT32,
                VertexElementType.FLOAT32,
                VertexElementType.VECTOR2_FLOAT32,
                VertexElementType.VECTOR3_FLOAT32,
                VertexElementType.VECTOR4_FLOAT32
            )

            fun setupBuffer(buffer: VertexBufferGLES, divisor: Int = 0) {
                val prefix = if (divisor == 0) "a" else "i"
                var attributeBindings = 0

                glBindBuffer(GL_ARRAY_BUFFER, buffer.buffer)
                val format = buffer.vertexFormat
                for (item in format.items) {
                    // skip over padding attributes
                    if (item.attribute == "_") {
                        continue
                    }

                    val attributeIndex = shader.attributeIndex("${prefix}_${item.attribute}")
                    if (attributeIndex != -1) {
                        when (item.type) {
                            in scalarVectorTypes -> {
                                for (i in 0 until item.arraySize) {
                                    glEnableVertexAttribArray(attributeIndex + i)
                                    debugGLErrors {
                                        when (it) {
                                            GL_INVALID_OPERATION -> "no vertex array object is bound"
                                            GL_INVALID_VALUE -> "index ($attributeIndex) is greater than or equal to GL_MAX_VERTEX_ATTRIBS"
                                            else -> null
                                        }
                                    }
                                    val glType = item.type.glType()

                                    if (glType == GL_FLOAT) {
                                        glVertexAttribPointer(
                                            attributeIndex + i,
                                            item.type.componentCount,
                                            glType,
                                            false,
                                            format.size,
                                            item.offset.toLong() + i * item.type.sizeInBytes
                                        )
                                    } else {
                                        glVertexAttribIPointer(
                                            attributeIndex + i,
                                            item.type.componentCount,
                                            glType,
                                            format.size,
                                            item.offset.toLong() + i * item.type.sizeInBytes
                                        )

                                    }
                                    debugGLErrors {
                                        when (it) {
                                            GL_INVALID_VALUE -> "index ($attributeIndex) is greater than or equal to GL_MAX_VERTEX_ATTRIBS"
                                            else -> null
                                        }
                                    }
                                    glVertexAttribDivisor(attributeIndex, divisor)
                                    attributeBindings++
                                }
                            }

                            VertexElementType.MATRIX44_FLOAT32 -> {
                                for (i in 0 until item.arraySize) {
                                    for (column in 0 until 4) {
                                        glEnableVertexAttribArray(attributeIndex + column + i * 4)
                                        debugGLErrors()

                                        glVertexAttribPointer(
                                            attributeIndex + column + i * 4,
                                            4,
                                            item.type.glType(),
                                            false,
                                            format.size,
                                            item.offset.toLong() + column * 16 + i * 64
                                        )
                                        debugGLErrors()

                                        glVertexAttribDivisor(attributeIndex + column + i * 4, divisor)
                                        debugGLErrors()
                                        attributeBindings++
                                    }
                                }
                            }

                            VertexElementType.MATRIX33_FLOAT32 -> {
                                for (i in 0 until item.arraySize) {
                                    for (column in 0 until 3) {
                                        glEnableVertexAttribArray(attributeIndex + column + i * 3)
                                        debugGLErrors()

                                        glVertexAttribPointer(
                                            attributeIndex + column + i * 3,
                                            3,
                                            item.type.glType(),
                                            false,
                                            format.size,
                                            item.offset.toLong() + column * 12 + i * 48
                                        )
                                        debugGLErrors()

                                        glVertexAttribDivisor(attributeIndex + column + i * 3, divisor)
                                        debugGLErrors()
                                        attributeBindings++
                                    }
                                }
                            }

                            else -> {
                                TODO("implement support for ${item.type}")
                            }
                        }
                    }
                }

                if (attributeBindings > 16) {
                    throw RuntimeException("Maximum vertex attributes exceeded $attributeBindings (limit is 16)")
                }
            }
            vertexBuffer.forEach {
                require(!(it as VertexBufferGLES).isDestroyed)
                setupBuffer(it, 0)
            }

            instanceAttributes.forEach {
                setupBuffer(it as VertexBufferGLES, 1)
            }
        }
    }
}

internal fun debugGLErrors(context: () -> String = { "after unknown" }) {
    var error = glGetError()
    if (error != GL_NO_ERROR) {
        val errorTexts = mutableListOf<String>()
        while (error != GL_NO_ERROR) {
            val errorText = when (error) {
                GL_INVALID_ENUM -> "invalid enum"
                GL_INVALID_VALUE -> "invalid value"
                GL_INVALID_OPERATION -> "invalid operation"
                GL_INVALID_FRAMEBUFFER_OPERATION -> "invalid framebuffer operation"
                GL_OUT_OF_MEMORY -> "out of memory"
                else -> "unknown error"
            }
            errorTexts.add(errorText)
            error = glGetError()
        }
        logger.error { "gl error ${context()}: ${errorTexts.joinToString(", ")}" }
    }
}

class VertexShaderGLES(val shaderObject: Int, val name: String) {
    companion object {
        fun fromString(code: String, name: String): VertexShaderGLES {
            val shaderObject = glCreateShader(GL_VERTEX_SHADER)
            glShaderSource(shaderObject, code)
            glCompileShader(shaderObject)

            val compileStatus = IntArray(1)
            glGetShaderiv(shaderObject, GL_COMPILE_STATUS, compileStatus)
            if (compileStatus[0] != GL_TRUE) {
                val log = glGetShaderInfoLog(shaderObject)
                throw Exception("vertex shader compilation failed ($name): $log")
            }
            return VertexShaderGLES(shaderObject, name)
        }
    }
}

class FragmentShaderGLES(val shaderObject: Int, val name: String) {
    companion object {
        fun fromString(code: String, name: String): FragmentShaderGLES {
            val shaderObject = glCreateShader(GL_FRAGMENT_SHADER)
            glShaderSource(shaderObject, code)
            glCompileShader(shaderObject)

            val compileStatus = IntArray(1)
            glGetShaderiv(shaderObject, GL_COMPILE_STATUS, compileStatus)
            if (compileStatus[0] != GL_TRUE) {
                val log = glGetShaderInfoLog(shaderObject)
                throw Exception("fragment shader compilation failed ($name): $log")
            }
            return FragmentShaderGLES(shaderObject, name)
        }
    }
}

class ShaderGLES(
    val programObject: Int,
    val name: String,
    val vs: VertexShaderGLES,
    val fs: FragmentShaderGLES,
    override val session: Session?
) : Shader {
    companion object {
        fun create(
            vs: VertexShaderGLES,
            fs: FragmentShaderGLES,
            name: String,
            session: Session?
        ): ShaderGLES {
            val programObject = glCreateProgram()
            glAttachShader(programObject, vs.shaderObject)
            glAttachShader(programObject, fs.shaderObject)
            glLinkProgram(programObject)

            val linkStatus = IntArray(1)
            glGetProgramiv(programObject, GL_LINK_STATUS, linkStatus)
            if (linkStatus[0] != GL_TRUE) {
                val log = glGetProgramInfoLog(programObject)
                throw Exception("shader link failed ($name): $log")
            }
            return ShaderGLES(programObject, name, vs, fs, session)
        }
    }

    override fun begin() {
        glUseProgram(programObject)
    }

    override fun end() {
        glUseProgram(0)
    }

    override fun destroy() {
        glDeleteProgram(programObject)
    }

    override fun hasUniform(name: String): Boolean {
        return glGetUniformLocation(programObject, name) != -1
    }

    override fun uniform(name: String, value: Matrix44) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            val fa = value.toFloatArray()
            glUniformMatrix4fv(location, 1, false, fa)
        }
    }

    override fun uniform(name: String, value: Matrix33) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            val fa = value.toFloatArray()
            glUniformMatrix3fv(location, 1, false, fa)
        }
    }

    override fun uniform(name: String, value: ColorRGBa) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform4f(location, value.r.toFloat(), value.g.toFloat(), value.b.toFloat(), value.alpha.toFloat())
        }
    }

    override fun uniform(name: String, value: Vector4) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform4f(location, value.x.toFloat(), value.y.toFloat(), value.z.toFloat(), value.w.toFloat())
        }
    }

    override fun uniform(name: String, value: Vector3) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform3f(location, value.x.toFloat(), value.y.toFloat(), value.z.toFloat())
        }
    }

    override fun uniform(name: String, value: Vector2) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform2f(location, value.x.toFloat(), value.y.toFloat())
        }
    }

    override fun uniform(name: String, value: Float) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform1f(location, value)
        }
    }

    override fun uniform(name: String, value: Double) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform1f(location, value.toFloat())
        }
    }

    override fun uniform(name: String, value: Int) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform1i(location, value)
        }
    }

    override fun uniform(name: String, value: Boolean) {
        val location = glGetUniformLocation(programObject, name)
        if (location != -1) {
            glUniform1i(location, if (value) 1 else 0)
        }
    }

    override fun <T> uniform(name: String, value: T) {
        TODO("Not yet implemented")
    }

    fun attributeIndex(name: String): Int {
        return glGetAttribLocation(programObject, name)
    }
}

fun Matrix44.toFloatArray(): FloatArray = floatArrayOf(
    c0r0.toFloat(), c0r1.toFloat(), c0r2.toFloat(), c0r3.toFloat(),
    c1r0.toFloat(), c1r1.toFloat(), c1r2.toFloat(), c1r3.toFloat(),
    c2r0.toFloat(), c2r1.toFloat(), c2r2.toFloat(), c2r3.toFloat(),
    c3r0.toFloat(), c3r1.toFloat(), c3r2.toFloat(), c3r3.toFloat()
)

fun Matrix33.toFloatArray(): FloatArray = floatArrayOf(
    c0r0.toFloat(), c0r1.toFloat(), c0r2.toFloat(),
    c1r0.toFloat(), c1r1.toFloat(), c1r2.toFloat(),
    c2r0.toFloat(), c2r1.toFloat(), c2r2.toFloat()
)

data class ShaderVertexDescription(
    val context: Long,
    val shader: Int,
    val vertexBuffers: IntArray,
    val instanceAttributeBuffers: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShaderVertexDescription

        if (context != other.context) return false
        if (shader != other.shader) return false
        if (!vertexBuffers.contentEquals(other.vertexBuffers)) return false
        if (!instanceAttributeBuffers.contentEquals(other.instanceAttributeBuffers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        result = 31 * result + shader
        result = 31 * result + vertexBuffers.contentHashCode()
        result = 31 * result + instanceAttributeBuffers.contentHashCode()
        return result
    }
}

class VertexBufferGLES(
    val buffer: Int,
    override val vertexFormat: VertexFormat,
    override val vertexCount: Int,
    override val session: Session?
) : VertexBuffer {
    companion object {
        fun createDynamic(vertexFormat: VertexFormat, vertexCount: Int, session: Session?): VertexBufferGLES {
            val buffers = IntArray(1)
            glGenBuffers(buffers)
            val buffer = buffers[0]
            glBindBuffer(GL_ARRAY_BUFFER, buffer)
            glBufferData(GL_ARRAY_BUFFER, (vertexFormat.size * vertexCount).toLong(), GL_DYNAMIC_DRAW)
            return VertexBufferGLES(buffer, vertexFormat, vertexCount, session)
        }
    }

    override fun write(source: Buffer, sourceOffset: Int, writeOffset: Int, writeSize: Int) {
        source as ByteBuffer
        source.position(sourceOffset)
        source.limit(sourceOffset + writeSize)
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        glBufferSubData(GL_ARRAY_BUFFER, writeOffset.toLong(), source)
    }

    override fun read(dest: Buffer, sourceOffset: Int, writeOffset: Int, writeSize: Int) {
        TODO("Not yet implemented")
    }

    override fun destroy() {
        glDeleteBuffers(intArrayOf(buffer))
    }

    override val isDestroyed: Boolean
        get() = TODO("Not yet implemented")
}

class IndexBufferGLES(val buffer: Int, override val type: IndexType, override val elementCount: Int, override val session: Session?) :
    IndexBuffer {
    companion object {
        fun create(elementCount: Int, type: IndexType, session: Session?): IndexBufferGLES {
            val buffers = IntArray(1)
            glGenBuffers(buffers)
            val buffer = buffers[0]
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, (elementCount * type.sizeInBytes).toLong(), GL_DYNAMIC_DRAW)
            return IndexBufferGLES(buffer, type, elementCount, session)
        }
    }

    override fun write(source: Buffer, sourceOffset: Int, writeOffset: Int, writeSize: Int) {
        source as ByteBuffer
        source.position(sourceOffset)
        source.limit(sourceOffset + writeSize)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer)
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, writeOffset.toLong(), source)
    }

    override fun read(dest: Buffer, sourceOffset: Int, writeOffset: Int, writeSize: Int) {
        TODO("Not yet implemented")
    }

    override fun destroy() {
        glDeleteBuffers(intArrayOf(buffer))
    }

    override val isDestroyed: Boolean
        get() = TODO("Not yet implemented")

    fun bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer)
    }
}


private fun DrawPrimitive.glType(): Int {
    return when (this) {
        DrawPrimitive.TRIANGLES -> GL_TRIANGLES
        DrawPrimitive.TRIANGLE_FAN -> GL_TRIANGLE_FAN
        DrawPrimitive.POINTS -> GL_POINTS
        DrawPrimitive.LINES -> GL_LINES
        DrawPrimitive.LINE_STRIP -> GL_LINE_STRIP
        DrawPrimitive.LINE_LOOP -> GL_LINE_LOOP
        DrawPrimitive.TRIANGLE_STRIP -> GL_TRIANGLE_STRIP
        DrawPrimitive.PATCHES -> GL_PATCHES
    }
}

private fun VertexElementType.glType(): Int = when (this) {
    VertexElementType.UINT8, VertexElementType.VECTOR2_UINT8, VertexElementType.VECTOR3_UINT8, VertexElementType.VECTOR4_UINT8 -> GL_UNSIGNED_BYTE
    VertexElementType.UINT16, VertexElementType.VECTOR2_UINT16, VertexElementType.VECTOR3_UINT16, VertexElementType.VECTOR4_UINT16 -> GL_UNSIGNED_SHORT
    VertexElementType.UINT32, VertexElementType.VECTOR2_UINT32, VertexElementType.VECTOR3_UINT32, VertexElementType.VECTOR4_UINT32 -> GL_UNSIGNED_INT

    VertexElementType.INT8, VertexElementType.VECTOR2_INT8, VertexElementType.VECTOR3_INT8, VertexElementType.VECTOR4_INT8 -> GL_BYTE
    VertexElementType.INT16, VertexElementType.VECTOR2_INT16, VertexElementType.VECTOR3_INT16, VertexElementType.VECTOR4_INT16 -> GL_SHORT
    VertexElementType.INT32, VertexElementType.VECTOR2_INT32, VertexElementType.VECTOR3_INT32, VertexElementType.VECTOR4_INT32 -> GL_INT

    VertexElementType.FLOAT32 -> GL_FLOAT
    VertexElementType.MATRIX22_FLOAT32 -> GL_FLOAT
    VertexElementType.MATRIX33_FLOAT32 -> GL_FLOAT
    VertexElementType.MATRIX44_FLOAT32 -> GL_FLOAT
    VertexElementType.VECTOR2_FLOAT32 -> GL_FLOAT
    VertexElementType.VECTOR3_FLOAT32 -> GL_FLOAT
    VertexElementType.VECTOR4_FLOAT32 -> GL_FLOAT
}

internal fun glStencilTest(test: StencilTest): Int {
    return when (test) {
        StencilTest.NEVER -> GL_NEVER
        StencilTest.ALWAYS -> GL_ALWAYS
        StencilTest.LESS -> GL_LESS
        StencilTest.LESS_OR_EQUAL -> GL_LEQUAL
        StencilTest.GREATER -> GL_GREATER
        StencilTest.GREATER_OR_EQUAL -> GL_GEQUAL
        StencilTest.EQUAL -> GL_EQUAL
        StencilTest.NOT_EQUAL -> GL_NOTEQUAL
        else -> throw RuntimeException("unsupported test: $test")
    }
}

internal fun glStencilOp(op: StencilOperation): Int {
    return when (op) {
        StencilOperation.KEEP -> GL_KEEP
        StencilOperation.DECREASE -> GL_DECR
        StencilOperation.DECREASE_WRAP -> GL_DECR_WRAP
        StencilOperation.INCREASE -> GL_INCR
        StencilOperation.INCREASE_WRAP -> GL_INCR_WRAP
        StencilOperation.ZERO -> GL_ZERO
        StencilOperation.INVERT -> GL_INVERT
        StencilOperation.REPLACE -> GL_REPLACE
        else -> throw RuntimeException("unsupported op")
    }
}

val Driver.Companion.glVersion
    get() = (instance as DriverGLES).version

val Driver.Companion.glType
    get() = (instance as DriverGLES).version.type

val Driver.Companion.capabilities
    get() = (instance as DriverGLES).capabilities

private fun IndexType.glType(): Int {
    return when (this) {
        IndexType.INT16 -> GL_UNSIGNED_SHORT
        IndexType.INT32 -> GL_UNSIGNED_INT
    }
}
