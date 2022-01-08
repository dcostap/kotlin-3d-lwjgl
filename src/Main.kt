
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import java.io.File

fun main(args: Array<String>) {
	Display.init(60, 500, 500, "You see it all in 3D")

	val mesh = Mesh(
		indices = intArrayOf(0, 1, 2),
		positions = floatArrayOf(
			0f, 0f, 0f,
			0f, 0.5f, 0f,
			0.5f, 0.5f, 0f,
		),
		texture = null, textureCoords = null
	)

	Display.render {
		mesh.draw()
	}
	Display.destroy()
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

class Mesh(val indices: IntArray, val positions: FloatArray, val texture: Texture?, val textureCoords: FloatArray?) {
	val vaoID: Int = glGenVertexArrays()

	val vertexCount get() = indices.size

	fun draw() {
		glBindVertexArray(this.vaoID)
		glEnableVertexAttribArray(0)
		glEnableVertexAttribArray(1)
		glActiveTexture(GL_TEXTURE)

		texture?.let {
			glBindTexture(GL_TEXTURE_2D, texture.id)
		}
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0)
		glDisableVertexAttribArray(0)
		glDisableVertexAttribArray(1)
		glBindVertexArray(0)
	}

	init {
		glBindVertexArray(vaoID)
		bindIndicesBuffer(indices)
		storeDataInAttributeList(0, 3, positions)

		textureCoords?.let {
			storeDataInAttributeList(1, 2, textureCoords)
		}
		glBindVertexArray(0) // unbind
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
