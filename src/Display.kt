import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

object Display {
	var windowID: Long = 0
	val sync = Sync()

	var targetFPS = 60

	fun init(fps: Int, width: Int, height: Int, title: String) {
		this.targetFPS = fps

		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set()

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

		// Configure GLFW
		GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable

		// Create the window
		windowID = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
		if (windowID == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		GLFW.glfwSetKeyCallback(
			windowID
		) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) GLFW.glfwSetWindowShouldClose(
				window,
				true
			) // We will detect this in the rendering loop
		}
		MemoryStack.stackPush().let { stack ->
			val pWidth = stack.mallocInt(1) // int*
			val pHeight = stack.mallocInt(1) // int*

			// Get the window size passed to glfwCreateWindow
			GLFW.glfwGetWindowSize(windowID, pWidth, pHeight)

			// Get the resolution of the primary monitor
			val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

			// Center the window
			GLFW.glfwSetWindowPos(
				windowID,
				(vidmode!!.width() - pWidth[0]) / 2,
				(vidmode.height() - pHeight[0]) / 2
			)
		}

		// Make the OpenGL context current
		GLFW.glfwMakeContextCurrent(windowID)
		// Enable v-sync
		GLFW.glfwSwapInterval(1)

		// Make the window visible
		GLFW.glfwShowWindow(windowID)

		GL.createCapabilities()
	}

	fun destroy() {
		// Free the window callbacks and destroy the window
		Callbacks.glfwFreeCallbacks(windowID)
		GLFW.glfwDestroyWindow(windowID)

		// Terminate GLFW and free the error callback
		GLFW.glfwTerminate()
		GLFW.glfwSetErrorCallback(null)!!.free()
	}

	var delta: Float = 0f
	var fps: Int = 0
	private var lastFrameTime: Long = 0L

	fun render(f: () -> Unit) {
		glClearColor(0.2f, 0.3f, 0.8f, 0.0f)

		glEnable(GL_DEPTH_TEST)

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!GLFW.glfwWindowShouldClose(windowID)) {
			glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

			f()

			sync.sync(targetFPS)

			val currentFrameTime: Long = System.nanoTime()
			delta = (currentFrameTime - lastFrameTime) / 1000f
			lastFrameTime = currentFrameTime

			fps = (1 / delta).toInt()

			GLFW.glfwSwapBuffers(windowID)
			GLFW.glfwPollEvents()
		}
	}
}