import math.Matrix4f
import math.Vector3f
import math.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.*
import java.io.File
import kotlin.system.exitProcess

object Shader {
	val vertex = "vert.glsl"
	val fragment = "frag.glsl"

	val programID: Int
	val vertexID: Int
	val fragmentID: Int

	val uniforms = mutableMapOf<String, Int>()

	init {
		vertexID = loadShader(vertex, GL_VERTEX_SHADER)
		fragmentID = loadShader(fragment, GL_FRAGMENT_SHADER)

		programID = glCreateProgram()
		glAttachShader(programID, vertexID)
		glAttachShader(programID, fragmentID)

		bindAttribute(0, "position")
		bindAttribute(1, "texture_coords")

		for (u in arrayOf("transform_mat", "projection_mat", "view_matrix")) {
			uniforms[u] = glGetUniformLocation(programID, u)
		}

		glLinkProgram(programID)
		glValidateProgram(programID)
	}

	fun setFloatUniform(uniform: String, value: Float) {
		glUniform1f(uniforms.getValue(uniform), value)
	}

	fun setIntUniform(uniform: String, value: Int) {
		glUniform1i(uniforms.getValue(uniform), value)
	}

	fun setVectorUniform(uniform: String, vector: Vector3f) {
		glUniform3f(uniforms.getValue(uniform), vector.x, vector.y, vector.z)
	}

	fun setVectorUniform(uniform: String, vector: Vector4f) {
		glUniform4f(uniforms.getValue(uniform), vector.x, vector.y, vector.z, vector.w)
	}

	fun setBooleanUniform(uniform: String, value: Boolean) {
		glUniform1f(uniforms.getValue(uniform), if (value) 1f else 0f)
	}

	private val matrixBuffer = BufferUtils.createFloatBuffer(16)

	fun setMatrixUniform(uniform: String, matrix: Matrix4f) {
		matrix.store(matrixBuffer)
		matrixBuffer.flip()
		glUniformMatrix4fv(uniforms.getValue(uniform), false, matrixBuffer)
	}

	fun start() {
		glUseProgram(programID)
	}

	fun stop() {
		glUseProgram(0)
	}

	fun destroy() {
		stop()
		glDetachShader(programID, vertexID)
		glDetachShader(programID, fragmentID)
		glDeleteShader(vertexID)
		glDeleteShader(fragmentID)
		glDeleteProgram(programID)
	}

	fun bindAttribute(attribute: Int, variableName: String) {
		glBindAttribLocation(programID, attribute, variableName)
	}

	private fun loadShader(path: String, type: Int): Int {
		println("Loading shader: $path")
		val source = File(path).readText()
		val id = glCreateShader(type)
		glShaderSource(id, source)
		glCompileShader(id)

		if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
			println(glGetShaderInfoLog(id, 500))
			println("Could not compile shader.")
			exitProcess(-1)
		}

		return id
	}
}