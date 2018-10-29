package postProcessing;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import resource.Shader;
import resource.Texture2D;

public class PostProcessor {
	
	private int MSFBO;	//  MSFBO = Multisampled FBO
	private int FBO;	//	FBO is regular, used for blitting MS color-buffer to texture
	private int RBO;	//  RBO is used for multisampled color buffer
	private int VAO;
	private int floatSize = 4;
	
	private Shader postProcessingShader;
	private Texture2D texture;
	private IntBuffer width;
	private IntBuffer height;
	public boolean confuse;
	public boolean chaos;
	public boolean shake;
	
	public PostProcessor(Shader shader, int width, int height){
		this.postProcessingShader = shader;
		this.width = BufferUtils.createIntBuffer(1).put(width);
		this.height = BufferUtils.createIntBuffer(1).put(height);
		this.texture = new Texture2D();
		confuse = false;
		chaos = false;
		shake = false;
		
		// Initialize renderbuffer/framebuffer object
		MSFBO = glGenFramebuffers();
		FBO = glGenFramebuffers();
		RBO = glGenRenderbuffers();
		
		// Initialize renderbuffer storage with a multisampled color buffer (don't need a depth/stencil buffer)
		glBindFramebuffer(GL_FRAMEBUFFER, MSFBO);
		glBindRenderbuffer(GL_RENDERBUFFER, RBO);
		// Allocate storage for render buffer object
		glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_RGB, width, height);
		// Attach MS render buffer object to framebuffer
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, RBO);
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.out.println("ERROR::POSTPROCESSOR: Failed to initialize MSFBO");
		
		// Also initialize the FBO/texture to blit multisampled color-buffer to
		// used for shader operations (for postprocessing effects)
		glBindFramebuffer(GL_FRAMEBUFFER, FBO);
		this.texture.generate(this.width, this.height, null);
		// Attach texture to framebuffer as its color attachment
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getTextureID(), 0);
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.out.println("ERROR::POSTPROCESSOR: Failed to initialize FBO");
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		// Initialize render data and uniforms
		initRenderData();
		postProcessingShader.use();
		postProcessingShader.setInteger("scene", 0);
		float offset = 1.0f / 300.0f;
		float[] offsets = new float[] {
			 -offset,  offset  ,  // top-left
	          0.0f,    offset  ,  // top-center
	          offset,  offset  ,  // top-right
	         -offset,  0.0f    ,  // center-left
	          0.0f,    0.0f    ,  // center-center
	          offset,  0.0f    ,  // center - right
	         -offset, -offset  ,  // bottom-left
	          0.0f,   -offset  ,  // bottom-center
	          offset, -offset     // bottom-right    
		};
		
		FloatBuffer fb = BufferUtils.createFloatBuffer(9 * 2);
		fb.put(offsets).flip();
		glUniform2fv(glGetUniformLocation(postProcessingShader.getProgramID(), "offsets"), fb);
		fb.clear();
		
		int[] edge_kernel = new int[] {
				 -1, -1, -1,
			     -1,  8, -1,
			     -1, -1, -1
		};
		
		IntBuffer ib = BufferUtils.createIntBuffer(9);
		ib.put(edge_kernel).flip();
		glUniform1iv(glGetUniformLocation(postProcessingShader.getProgramID(), "edge_kernel"), ib);
		
		float[] blur_kernel = new float[] {
				 1.0f / 16, 2.0f / 16, 1.0f / 16,
			     2.0f / 16, 4.0f / 16, 2.0f / 16,
			     1.0f / 16, 2.0f / 16, 1.0f / 16
		};
		fb.put(blur_kernel).flip();
		glUniform1fv(glGetUniformLocation(postProcessingShader.getProgramID(), "blur_kernel"), fb);
		
	}
	
	public void beginRender(){
		glBindFramebuffer(GL_FRAMEBUFFER, MSFBO);
		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT);
	}
	
	public void endRender(){
		// Now resolve multisampled color-buffer into intermediate FBO to store to texture
		glBindFramebuffer(GL_READ_FRAMEBUFFER, MSFBO);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, FBO);
		glBlitFramebuffer(0, 0, width.get(0), height.get(0), 0, 0, width.get(0), height.get(0), GL_COLOR_BUFFER_BIT, GL_NEAREST);
		// Binds both READ and WRITE framebuffer to default framebuffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void render(float time){
		postProcessingShader.use();
		postProcessingShader.setFloat("time", time);
		postProcessingShader.setInteger("confuse", this.confuse ? 1 : 0);
		postProcessingShader.setInteger("chaos", this.chaos ? 1 : 0);
		postProcessingShader.setInteger("shake", this.shake ? 1 : 0);
		
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		glBindVertexArray(VAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
	
	private void initRenderData(){
		int vbo = 0;
		float vertices[] = { 
				 // Pos        // Tex
		        -1.0f, -1.0f, 0.0f, 0.0f,
		         1.0f,  1.0f, 1.0f, 1.0f,
		        -1.0f,  1.0f, 0.0f, 1.0f,

		        -1.0f, -1.0f, 0.0f, 0.0f,
		         1.0f, -1.0f, 1.0f, 0.0f,
		         1.0f,  1.0f, 1.0f, 1.0f
			    };
		
		FloatBuffer fb = BufferUtils.createFloatBuffer(24);
		fb.put(vertices).flip();
		
		VAO = glGenVertexArrays();
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
		
		glBindVertexArray(VAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * floatSize, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
}



















