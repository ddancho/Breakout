package resource;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class Texture2D {
	
	private int textureID;
	private IntBuffer width;
	private IntBuffer height;
	private int internalFormat;
	private int imageFormat;
	private int wrapS;
	private int wrapT;
	private int filterMin;
	private int filterMax;
	
	public Texture2D(){
		width = BufferUtils.createIntBuffer(1).put(0);
		height = BufferUtils.createIntBuffer(1).put(0);
		internalFormat = GL_RGB;
		imageFormat = GL_RGB;
		wrapS = GL_REPEAT;
		wrapT = GL_REPEAT;
		filterMin = GL_LINEAR;
		filterMax = GL_LINEAR;
		
		textureID = glGenTextures();
	}
	
	public void generate(IntBuffer width, IntBuffer height, ByteBuffer data){
		this.width = width;
		this.height = height;
		
		glBindTexture(GL_TEXTURE_2D, textureID);
		
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, this.width.get(0), this.height.get(0), 0, imageFormat, GL_UNSIGNED_BYTE, data);		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMin);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMax);
		
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void bind(){
		glBindTexture(GL_TEXTURE_2D, textureID);
	}

	public void setInternalFormat(int internalFormat) {
		this.internalFormat = internalFormat;
	}

	public void setImageFormat(int imageFormat) {
		this.imageFormat = imageFormat;
	}

	public int getTextureID() {
		return textureID;
	}
	
}














