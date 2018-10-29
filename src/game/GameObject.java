package game;

import org.joml.Vector2f;
import org.joml.Vector3f;

import resource.Texture2D;
import sprites.SpriteRenderer;

public class GameObject {
	
	public Vector2f position;
	public Vector2f size;
	public Vector2f velocity;
	
	private Texture2D sprite;
	private Vector3f color;	
	private float rotation;
	private boolean isSolid;
	private boolean destroyed;
	
	public GameObject(){
		sprite = new Texture2D();
		color = new Vector3f(1);
		position = new Vector2f(0, 0);
		size = new Vector2f(1, 1);
		velocity = new Vector2f(0);
		rotation = 0;
		isSolid = false;
		destroyed = false;
	}
	
	public GameObject(Vector2f position, Vector2f size, Texture2D sprite){
		this.position = position;
		this.size = size;
		this.sprite = sprite;
		this.rotation = 0;
		this.color = new Vector3f(1);
	}
	
	public GameObject(Vector2f position, Vector2f size, Texture2D sprite, Vector3f color){
		this.position = position;
		this.size = size;
		this.sprite = sprite;
		this.color = color;
		this.rotation = 0;
	}
	
	public GameObject(Vector2f position, Vector2f size, Texture2D sprite, Vector3f color, Vector2f velocity){
		this.position = position;
		this.size = size;
		this.sprite = sprite;
		this.color = color;
		this.velocity = velocity;
		this.rotation = 0;
	}
	
	public void draw(SpriteRenderer renderer){
		renderer.drawSprite(this.sprite, this.position, this.size, this.rotation, this.color);
	}

	public boolean isSolid() {
		return isSolid;
	}

	public void setSolid(boolean isSolid) {
		this.isSolid = isSolid;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}

	public void setPosition(float posX, float posY) {
		this.position.x = posX;
		this.position.y = posY;
	}

	public void setSize(Vector2f size) {
		this.size = size;
	}

	public Vector3f getColor() {
		return color;
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}
	
}









