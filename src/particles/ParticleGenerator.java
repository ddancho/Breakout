package particles;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import game.GameObject;
import resource.Shader;
import resource.Texture2D;

public class ParticleGenerator {
	
	// state
	private List<Particle> particles;
	private int amount;
	// render state
	private Shader shader;
	private Texture2D texture;
	private int VAO;
	private int lastUsedParticle = 0; // Stores the index of the last particle used (for quick access to next dead particle)
	private int floatSize = 4;
	
	
	public ParticleGenerator(Shader shader, Texture2D texture, int amount){
		this.shader = shader;
		this.texture = texture;
		this.amount = amount;
		this.particles = new ArrayList<Particle>();
		
		init();
	}
	
	public void update(float dt, GameObject object, int newParticles, Vector2f offset){
		// add new particles
		for(int i = 0; i < newParticles; i++){
			int unusedParticle = firstUnusedParticle();
			respawnParticle(particles.get(unusedParticle), object, offset);
		}
		
		// update all particles
		for(int i = 0; i < amount; i++){
			Particle p = particles.get(i);
			// reduce life
			p.life -= dt;
			if(p.life > 0){
				// particle is a live , update
				p.position.x -= p.velocity.x * dt;
				p.position.y -= p.velocity.y * dt;
				p.color.z -= dt * 2.5f;
			}
		}
	}
	
	public void draw(){
		// use additive blendingto give it a 'glow' effect
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		shader.use();
		for(Particle particle:particles){
			if(particle.life > 0){
				shader.setVector2f("offset", particle.position);
				shader.setVector4f("color", particle.color);
				
				glActiveTexture(GL_TEXTURE0);
				texture.bind();
				
				glBindVertexArray(VAO);
				glDrawArrays(GL_TRIANGLES, 0, 6);
				glBindVertexArray(0);
			}
		}
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void init(){
		int vbo;
		float[] particleQuad = new float[]{
				0.0f, 1.0f, 0.0f, 1.0f,
		        1.0f, 0.0f, 1.0f, 0.0f,
		        0.0f, 0.0f, 0.0f, 0.0f,

		        0.0f, 1.0f, 0.0f, 1.0f,
		        1.0f, 1.0f, 1.0f, 1.0f,
		        1.0f, 0.0f, 1.0f, 0.0f
		};
		
		FloatBuffer fb = BufferUtils.createFloatBuffer(24);
		fb.put(particleQuad).flip();
		
		VAO = glGenVertexArrays();
		vbo = glGenBuffers();
				
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
		
		glBindVertexArray(VAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * floatSize, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		for(int i = 0; i < amount; i++ ){
			particles.add(new Particle());
		}
	}
	
	private int firstUnusedParticle(){
		// First search from last used particle, this will usually return almost instantly
		for(int i = lastUsedParticle; i < amount; i++){
			if(particles.get(i).life <= 0){
				lastUsedParticle = i;
				return i;
			}
		}
		
		// Otherwise, do a linear search
		for(int i = 0; i < lastUsedParticle; i++){
			if(particles.get(i).life <= 0){
				lastUsedParticle = i;
				return i;
			}
		}
		
		// All particles are taken, override the first one (note that if it repeatedly hits this case, more particles should be reserved)
		lastUsedParticle = 0;
		
		return 0;
	}
	
	private void respawnParticle(Particle particle, GameObject object, Vector2f offset){
		Random rand = new Random();
		float random = ((rand.nextFloat() % 100) - 50) / 10.0f;	
		float rColor = 0.5f + ((rand.nextFloat() % 100) / 100.0f);
		
		particle.position.x = object.position.x  + offset.x;
		particle.position.y = object.position.y  + offset.y;
		
		particle.color = new Vector4f(rColor, rColor, rColor, 1.0f);
		
		particle.life = 1.0f;
		
		particle.velocity.x = object.velocity.x * 0.1f;
		particle.velocity.y = object.velocity.y * 0.1f;
	}
}



















