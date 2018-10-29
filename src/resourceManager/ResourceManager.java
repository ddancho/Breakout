package resourceManager;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.stb.STBImage.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import java.util.HashMap;
import java.util.Map;

import resource.Shader;
import resource.Texture2D;

public class ResourceManager {
	
	private static Map<String, Shader> shaders = new HashMap<String, Shader>();
	private static Map<String, Texture2D> textures = new HashMap<String, Texture2D>();
	
	public static Shader loadShader(String vertexShaderFile, String fragmentShaderFile, String geometryShaderFile, String name){
		shaders.put(name, loadShaderFromFile(vertexShaderFile, fragmentShaderFile, geometryShaderFile));
		
		return shaders.get(name);
	}
	
	public static Shader getShader(String name){
		return shaders.get(name);
	}
	
	public static Texture2D loadTexture(String file, boolean alpha, String name){
		textures.put(name, loadTextureFromFile(file, alpha));
		
		return textures.get(name);
	}
	
	public static Texture2D getTexture(String name){
		return textures.get(name);
	}
	
	public static void clear(){
		for(Texture2D texture:textures.values())
			glDeleteTextures(texture.getTextureID());
		
		for(Shader shader:shaders.values())
			glDeleteProgram(shader.getProgramID());
	}
	
	private static Shader loadShaderFromFile(String vertexShaderFile, String fragmentShaderFile, String geometryShaderFile){
		Shader shader = new Shader();
		
		try {
			shader.compile(vertexShaderFile, fragmentShaderFile, geometryShaderFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return shader;
	}
	
	private static Texture2D loadTextureFromFile(String file, boolean alpha){
		Texture2D texture = new Texture2D();
		if(alpha){
			texture.setInternalFormat(GL_RGBA);
			texture.setImageFormat(GL_RGBA);			
		}
		
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		
		ByteBuffer buffer = stbi_load("res\\" + file + ".png", width, height, comp, alpha ? 4 : 3);
		texture.generate(width, height, buffer);
		stbi_image_free(buffer);
		
		return texture;
	}
	
}









