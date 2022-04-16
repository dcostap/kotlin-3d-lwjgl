import java.io.File

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
		} else if (parts[0] == "vn") {
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