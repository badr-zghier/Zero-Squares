package square;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.stb.STBImage;
import util.Time;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
    private final int width;
    private final int height;
    private  long glfwWindow;
    private final String title;
    private static Window window = null;
    public float r;
    public float g;
    public float b;
    private final float a;
    private boolean fadeToBlack = false;
    private static Scene currentScene;
    private Window() {
        this.width = 800;
        this.height = 600;
        this.title = "Zero Squares";
        r = 1;
        b = 1;
        g = 1;
        a = 1;
    }
    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public static void changeScene(int newScene) {
        switch (newScene){
            case 0:
                currentScene = new LevelEditorScene();
                //currentScene.init()
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                assert false : "Unknown Scene (" + newScene + ")" ;
                break;
        }
    }
    public void showIntroScreen() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        int textureID = loadTexture();

        float introDuration = 3.0f;
        long startTime = System.currentTimeMillis();

        while ((System.currentTimeMillis() - startTime) / 1000.0f < introDuration) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderLogo(textureID);

            // Calculate loading line progress
            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0f;
            float progress = elapsedTime / introDuration;

            // Render the loading line
            renderLoadingLine(progress);

            glfwSwapBuffers(glfwWindow);
            glfwPollEvents();
        }

        glDeleteTextures(textureID);
    }
    private int loadTexture() {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer image = STBImage.stbi_load("C:\\Zero-Squares\\zerosquares\\src\\assets\\logo\\intro.png", width, height, channels, 4);
        if (image != null) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            STBImage.stbi_image_free(image);
        } else {
            throw new RuntimeException("Failed to load texture file: " + "C:\\Zero-Squares\\zerosquares\\src\\assets\\logo\\intro.png");
        }

        return textureID;
    }
    private void renderLogo(int textureID) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(-1.0f, 1.0f);
        glTexCoord2f(1, 0); glVertex2f(1.0f, 1.0f);
        glTexCoord2f(1, 1); glVertex2f(1.0f, -1.0f);
        glTexCoord2f(0, 1); glVertex2f(-1.0f, -1.0f);
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }
    private void renderLoadingLine(float progress) {
        glColor3f(0.0f, 1.0f, 0.0f); // Set color for the loading line

        float startX = -0.8f;
        float endX = startX + progress * 1.6f; // Line grows based on progress
        float lineY = -0.8f;

        glBegin(GL_LINES);
        glVertex2f(startX, lineY); // Starting point of the line
        glVertex2f(endX, lineY);   // Ending point based on progress
        glEnd();

        glColor3f(1.0f, 1.0f, 1.0f); // Reset color to default
    }
    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        showIntroScreen();
        loop();

        // Free memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public void init() {
        //TIP Set up an Error call back
        // same like doing System.err.println("error");
        GLFWErrorCallback.createPrint(System.err).set();

        // Init GLFW
        if(!glfwInit()) {
            throw new IllegalStateException("Unable to Initialize GLFW");
        }
        // Config GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        // Get the primary monitor and its video mode
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vidmode != null;
        // Create the window
        glfwWindow = glfwCreateWindow(vidmode.width(), vidmode.height(), title, glfwGetPrimaryMonitor(), NULL);
        if(glfwWindow == NULL) {
            throw new IllegalStateException("Failed to Create the GLFW Window");
        }

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.


        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(glfwWindow, pWidth, pHeight);

            // Get the resolution of the primary monitor
            //GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window

            glfwSetWindowPos(
                    glfwWindow,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically


        // Mouse Event Listeners
        glfwSetCursorPosCallback(glfwWindow,MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow,MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow,MouseListener::mouseScrollCallback);
        // Key Event Listeners
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        // make the openGL context Current
        glfwMakeContextCurrent(glfwWindow);
        // Enable v-sync (buffer swapping)
        glfwSwapInterval(1);
        // make the window visible
        glfwShowWindow(glfwWindow);

        // chose a scene to start with
        Window.changeScene(0);

    }

    public void loop() {

        float beginTime = Time.getTime();
        float endTime;
        float dt = -1.0f;
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(glfwWindow) ) {
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            // Set the clear color

            glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            glClearColor(r, g, b, a);



            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            if (KeyListener.isKeyPressed(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(glfwWindow,true);

            }

            if(dt >= 0) {
                currentScene.update(dt);
            }

            glfwSwapBuffers(glfwWindow); // swap the color buffers

            endTime = Time.getTime();
            // dt (delta time has the looped time )
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

}

