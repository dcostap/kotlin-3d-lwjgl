import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import java.io.File
import kotlin.system.exitProcess

object Shader {
	val vertex = "vert.glsl"
	val fragment = "frag.glsl"

	val programID: Int
	val vertexID: Int
	val fragmentID: Int

	init {
		vertexID = loadShader(vertex, GL_VERTEX_SHADER)
		fragmentID = loadShader(fragment, GL_FRAGMENT_SHADER)

		programID = glCreateProgram()
		glAttachShader(programID, vertexID)
		glAttachShader(programID, fragmentID)

		bindAttribute(0, "position")
		bindAttribute(1, "texture_coords")

		glLinkProgram(programID)
		glValidateProgram(programID)
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

fun main(args: Array<String>) {
	Display.init(60, 500, 500, "You see it all in 3D")

	val mesh = loadObjFile("mario.obj")
	Display.render {
		Shader.start()
		mesh.draw()
		Shader.stop()
	}
	Display.destroy()
	Shader.destroy()
}

class Texture(path: String) {
	val id = glGenTextures()

	init {
		glBindTexture(GL_TEXTURE_2D, id)
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST.toFloat())
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST.toFloat())

		val width = BufferUtils.createIntBuffer(1)
		val height = BufferUtils.createIntBuffer(1)
		val comp = BufferUtils.createIntBuffer(1)

		val data = stbi_load(path, width, height, comp, 4)

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data)
		stbi_image_free(data)
	}
}

class Mesh(
	val name: String,
	val indices: IntArray,
	val vertices: FloatArray,
	val normals: FloatArray,
	val texture: Texture?,
	val textureCoords: FloatArray?
) {
	val vaoID: Int = glGenVertexArrays()

	val vertexCount get() = indices.size

	init {
		glBindVertexArray(vaoID)
		bindIndicesBuffer(indices)
		storeDataInAttributeList(0, 3, vertices)
		storeDataInAttributeList(1, 3, normals)

		textureCoords?.let {
			storeDataInAttributeList(2, 2, textureCoords)
		}
		glBindVertexArray(0) // unbind
	}

	fun draw() {
		glBindVertexArray(this.vaoID)
		glEnableVertexAttribArray(0)
		glEnableVertexAttribArray(1)
		glEnableVertexAttribArray(2)
		glActiveTexture(GL_TEXTURE)

		texture?.let {
			glBindTexture(GL_TEXTURE_2D, texture.id)
		}
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0)
		glDisableVertexAttribArray(0)
		glDisableVertexAttribArray(1)
		glDisableVertexAttribArray(2)
		glBindVertexArray(0)
	}

	private fun storeDataInAttributeList(attributeNumber: Int, size: Int, data: FloatArray) {
		val vboID = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, vboID)

		val buffer = BufferUtils.createFloatBuffer(data.size)
		buffer.put(data)
		buffer.flip()

		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
		glVertexAttribPointer(attributeNumber, size, GL_FLOAT, false, 0, 0)
		glBindBuffer(GL_ARRAY_BUFFER, 0)
	}

	private fun bindIndicesBuffer(indices: IntArray) {
		val vboID = glGenBuffers()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID)

		val buffer = BufferUtils.createIntBuffer(indices.size)
		buffer.put(indices)
		buffer.flip()

		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
	}
}

val meshes = mutableMapOf<String, Mesh>()
val textures = mutableMapOf<String, Texture>()

fun loadObjFile(file: String): Mesh {
	val vertices = mutableListOf<Float>()
	val indices = mutableListOf<Int>()
	val normals = mutableListOf<Float>()

	val file = File(file)
	file.forEachLine { line ->
		val parts = line.split(" ")
		if (parts[0] == "v") {
			vertices.addAll(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat())
		} else if (parts[0] == "f") {
			indices.addAll(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
		}else if (parts[0] == "vn") {
			normals.addAll(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat())
		}
	}

	val mesh = Mesh(
		file.nameWithoutExtension, indices.toIntArray(), vertices.toFloatArray(), normals.toFloatArray(), null, null
	)
	meshes[file.nameWithoutExtension] = mesh

	return mesh
}

private fun <E> MutableList<E>.addAll(vararg values: E) {
	addAll(values)
}
