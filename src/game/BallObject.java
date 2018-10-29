package game;

import org.joml.Vector2f;
import org.joml.Vector3f;

import resource.Texture2D;

public class BallObject extends GameObject {
	
	public float radius;
	private boolean stuck;
	private boolean sticky;
	private boolean passThrough;
	
	public BallObject(){
		super();
	}
	
	public BallObject(Vector2f position, float radius, Vector2f velocity, Texture2D sprite){
		super(position, new Vector2f(radius * 2, radius * 2), sprite, new Vector3f(1), velocity);
		this.radius = radius;
		this.stuck = true;
	}
	
	public Vector2f move(float dt, int windowWidth){
		if(!stuck){
			// move the ball
			position.x += velocity.x * dt;
			position.y += velocity.y * dt;
			// then check for outside window bounds and if so reverse velocity and restore at correct position
			if(position.x <= 0.0f){
				velocity.x = -velocity.x;
				position.x = 0;
			}
			else if(position.x + size.x >= windowWidth){
				velocity.x = -velocity.x;
				position.x = windowWidth - size.x;
			}
			
			if(position.y <= 0.0f){
				velocity.y = -velocity.y;
				position.y = 0;
			}
		}
		
		return position;
	}
	
	public void reset(Vector2f position, Vector2f velocity){
		this.position = position;
		this.velocity = velocity;
		this.stuck = true;
	}

	public boolean isStuck() {
		return stuck;
	}

	public void setStuck(boolean stuck) {
		this.stuck = stuck;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public boolean isPassThrough() {
		return passThrough;
	}

	public void setPassThrough(boolean passThrough) {
		this.passThrough = passThrough;
	}
	
}



















