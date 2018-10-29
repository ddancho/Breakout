package main;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import game.Game;
import game.Game.GameState;
import resourceManager.ResourceManager;

public class Main {
	
	private final static int SCREEN_WIDTH = 800;
	private final static int SCREEN_HEIGHT = 600;
	
	private static GLFWErrorCallback errorCallback;
	private static GLFWKeyCallback keyCallback;
	private static long window;
	
	private static Game breakout = new Game(SCREEN_WIDTH, SCREEN_HEIGHT);
	
	public static void main(String[] args) {
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
		
		if(glfwInit() != GLFW_TRUE)
			throw new IllegalStateException("Unable to initialize GLFW");
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		
		window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Breakout", NULL, NULL);
		if(window == NULL)
			throw new RuntimeException("Failed to create GLFW window");
		
		glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
			
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
					glfwSetWindowShouldClose(window, GL_TRUE);	
				
				if(key >= 0 && key < 1024){
					if(action == GLFW_PRESS)
						breakout.setKeysState(key, true);
					else if(action == GLFW_RELEASE)
						breakout.setKeysState(key, false);
				}
			}
		});
		
		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		glfwSetWindowPos(window, (vidMode.width() - SCREEN_WIDTH)/2, (vidMode.height() - SCREEN_HEIGHT)/2);
		
		glfwMakeContextCurrent(window);		
		
		glfwSwapInterval(1);
		
		glfwShowWindow(window);
		
		GL.createCapabilities();		
		
		glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		glEnable(GL_CULL_FACE);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		breakout.init();
		
		float deltaTime = 0;
		float lastFrame = 0;
		
		breakout.setState(GameState.GAME_ACTIVE);
		
		while(glfwWindowShouldClose(window) == GLFW_FALSE){			
			
			float currentFrame = (float) glfwGetTime();
			deltaTime = currentFrame - lastFrame;
			lastFrame = currentFrame;
			glfwPollEvents();
			
			breakout.processInput(deltaTime);
			
			breakout.update(deltaTime);
			
			//glClearColor(0, 0, 0, 1);
			glClear(GL_COLOR_BUFFER_BIT);
			
			breakout.render();
			
			glfwSwapBuffers(window);			
		}
		
		ResourceManager.clear();
		breakout.clear();
		
		glfwDestroyWindow(window);
		keyCallback.release();
		glfwTerminate();
		errorCallback.release();
	}

}
