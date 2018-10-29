package particles;

import org.joml.Vector2f;
import org.joml.Vector4f;

public class Particle {
	
	Vector4f color;
	Vector2f position;
	Vector2f velocity;
	float life;
	
	public Particle(){
		this.color = new Vector4f(1.0f);
		this.position = new Vector2f(0);
		this.velocity = new Vector2f(0);
		this.life = 0;
	}
	
}
