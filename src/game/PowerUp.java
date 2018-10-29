package game;

import org.joml.Vector2f;
import org.joml.Vector3f;

import resource.Texture2D;

public class PowerUp extends GameObject {
	
	private static final Vector2f SIZE = new Vector2f(60, 20);
	private static final Vector2f VELOCITY = new Vector2f(0, 150);
	
	public String type;
	public float duration;
	public boolean activated;
	
	public PowerUp(String type, Vector3f color, float duration, Vector2f position, Texture2D texture){
		super(position, SIZE, texture, color, VELOCITY);
		this.type = type;
		this.duration = duration;
		this.activated = false;
	}
}
