package sprites;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import resource.Shader;
import resource.Texture2D;

public class SpriteRenderer {
	
	Shader shader;
	int quadVAO;
	int floatSize = 4;
	
	public SpriteRenderer(Shader shader){
		this.shader = shader;
		initRenderData();
	}
	
	public void drawSprite(Texture2D texture, Vector2f position, Vector2f size, float rotate, Vector3f color){
		shader.use();
		
		Matrix4f model = new Matrix4f();
		// translate first
		model.translate(position.x, position.y, 0);
		
		// move origin of rotation to center of quad
		model.translate(0.5f * size.x, 0.5f * size.y, 0);
		// rotate
		model.rotate((float) Math.toRadians(rotate), new Vector3f(0, 0, 1));
		// move origin back
		model.translate(-0.5f * size.x, -0.5f * size.y, 0);
		
		// scale
		model.scale(size.x, size.y, 1);
		
		shader.setMatrix4f("model", model);
		shader.setVector3f("spriteColor", color);
		
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
	
	public void clear(){
		glDeleteVertexArrays(quadVAO);
	}
	
	private void initRenderData(){
		int vbo = 0;
		float vertices[] = { 
			        // Pos      // Tex
			        0.0f, 1.0f, 0.0f, 1.0f,
			        1.0f, 0.0f, 1.0f, 0.0f,
			        0.0f, 0.0f, 0.0f, 0.0f, 

			        0.0f, 1.0f, 0.0f, 1.0f,
			        1.0f, 1.0f, 1.0f, 1.0f,
			        1.0f, 0.0f, 1.0f, 0.0f
			    };
		
		FloatBuffer fb = BufferUtils.createFloatBuffer(24);
		fb.put(vertices).flip();
		
		quadVAO = glGenVertexArrays();
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
		
		glBindVertexArray(quadVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * floatSize, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
}

























