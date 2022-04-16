import math.Matrix4f
import math.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import java.util.stream.Collectors
import java.util.stream.Collectors.groupingBy

fun main(args: Array<String>) {
	Display.init(60, 500, 500, "You see it all in 3D")

	val mesh = loadObjFile("mario.obj")

	val mesh2 = Mesh(
		indices = intArrayOf(0, 1, 2),
		vertices = floatArrayOf(
			0f, 0f, 0f,
			0f, 0.5f, 0f,
			0.5f, 0.5f, 0f,
		),
		texture = null, textureCoords = null, normals = floatArrayOf(), name = "cube"
	)

	val entities = mapOf(
		mesh to Entity(mesh),
//		mesh2 to Entity(mesh2)
	)

	Display.render {
		Shader.start()

		for ((mesh, ent) in entities) {
			Shader.setMatrixUniform(
				"transform_mat",
				createTransformationMatrix(ent.position, ent.rotation.x, ent.rotation.y, ent.rotation.z, 1f)
			)
			mesh.draw()
		}

		println(Display.fps)
		Shader.stop()

		entities.values.forEach { e -> e.position.z += Display.delta * 0.5f }
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

class Entity(val mesh: Mesh) {
	val position = Vector3f()
	val rotation = Vector3f()
	val scale = Vector3f()
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

		texture?.let {
			glActiveTexture(GL_TEXTURE)
			glBindTexture(GL_TEXTURE_2D, texture.id)
		}

		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0L)
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

