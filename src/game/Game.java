package game;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import particles.ParticleGenerator;
import postProcessing.PostProcessor;
import resourceManager.ResourceManager;
import sprites.SpriteRenderer;

public class Game {
	
	public static enum GameState{GAME_ACTIVE, GAME_MENU, GAME_WIN}
	// epresents the four possible (collision) directions
	public static enum Direction{UP, RIGHT, DOWN,LEFT}
	
	private static Random rand = new Random();
	
	private final Vector2f PLAYER_SIZE = new Vector2f(100, 20);
	private final float PLAYER_VELOCITY = 500.0f;
	private final Vector2f INITIAL_BALL_VELOCITY = new Vector2f(100.0f, -350.0f);
	private final float BALL_RADIUS = 12.5f;
	
	private GameState state;
	private int width;
	private int height;
	private boolean[] keys;
	private SpriteRenderer renderer;
	private List<GameLevel> levels;
	private int level;
	private GameObject player;
	private BallObject ball;
	private ParticleGenerator particles;
	private PostProcessor effects;
	private float shakeTime = 0;
	private List<PowerUp> powerUps;
	
	public Game(int width, int height){
		this.width = width;
		this.height = height;
		keys  = new boolean[1024];
		state = GameState.GAME_ACTIVE;
		
	}
	
	public void init(){
		ResourceManager.loadShader("spriteVertex", "spriteFragment", null, "sprite");
		ResourceManager.loadShader("particleVertex", "particleFragment", null, "particle");
		ResourceManager.loadShader("postProcessingVertex", "postProcessingFragment", null, "postProcessing");
		
		Matrix4f projection = new Matrix4f();
		projection.ortho(0, (float)width, (float)height, 0, -1.0f, 1.0f);
		
		ResourceManager.getShader("sprite").use();
		ResourceManager.getShader("sprite").setInteger("image", 0);
		ResourceManager.getShader("sprite").setMatrix4f("projection", projection);
		
		ResourceManager.getShader("particle").use();
		ResourceManager.getShader("particle").setInteger("image", 0);
		ResourceManager.getShader("particle").setMatrix4f("projection", projection);
		// load all textures
		ResourceManager.loadTexture("awesomeface", true, "face");
		ResourceManager.loadTexture("background", false, "background");
		ResourceManager.loadTexture("block", false, "block");
		ResourceManager.loadTexture("block_solid", false, "block_solid");
		ResourceManager.loadTexture("paddle", true, "paddle");

		ResourceManager.loadTexture("powerup_speed", true, "powerup_speed");
		ResourceManager.loadTexture("powerup_sticky", true, "powerup_sticky");
		ResourceManager.loadTexture("powerup_increase", true, "powerup_increase");
		ResourceManager.loadTexture("powerup_confuse", true, "powerup_confuse");
		ResourceManager.loadTexture("powerup_chaos", true, "powerup_chaos");
		ResourceManager.loadTexture("powerup_passthrough", true, "powerup_passthrough");
		powerUps = new ArrayList<PowerUp>();
		
		renderer = new SpriteRenderer(ResourceManager.getShader("sprite"));
		particles = new ParticleGenerator(ResourceManager.getShader("particle"), ResourceManager.getTexture("particle"), 100);
		effects = new PostProcessor(ResourceManager.getShader("postProcessing"), width, height);
		
		levels = new ArrayList<GameLevel>();
		GameLevel one = new GameLevel();   one.load("one", width, (int)(height * 0.5f));
		GameLevel two = new GameLevel();   two.load("two", width, (int)(height * 0.5f));
		GameLevel three = new GameLevel(); three.load("three", width, (int)(height * 0.5f));
		GameLevel four = new GameLevel();  four.load("four", width, (int)(height * 0.5f));
		levels.add(one);
		levels.add(two);
		levels.add(three);
		levels.add(four);
		level = 0;
		
		Vector2f playerPos = new Vector2f(width / 2 - PLAYER_SIZE.x /2, height - PLAYER_SIZE.y);
		player = new GameObject(playerPos, PLAYER_SIZE, ResourceManager.getTexture("paddle"));
		
		Vector2f ballPos = new Vector2f(PLAYER_SIZE.x / 2 - BALL_RADIUS, -BALL_RADIUS * 2);
		ballPos.add(playerPos);
		ball = new BallObject(ballPos, BALL_RADIUS, INITIAL_BALL_VELOCITY, ResourceManager.getTexture("face"));
		
	}
	
	public void processInput(float dt){
		if(state == GameState.GAME_ACTIVE){
			float velocity = PLAYER_VELOCITY * dt;
			
			if(keys[GLFW_KEY_A] || keys[GLFW_KEY_LEFT]){
				if(player.position.x >= 0){
					player.position.x -= velocity;
					if(ball.isStuck())
						ball.position.x -= velocity;
				}
			}
			if(keys[GLFW_KEY_D] || keys[GLFW_KEY_RIGHT]){
				if(player.position.x <= width - player.size.x){
					player.position.x += velocity;
					if(ball.isStuck())
						ball.position.x += velocity;
				}
			}
			if(keys[GLFW_KEY_SPACE])
				ball.setStuck(false);
		}
	}
	
	public void update(float dt){
		ball.move(dt, width);
		doCollisions();
		particles.update(dt, ball, 2, new Vector2f(BALL_RADIUS / 2));
		
		updatePowerUps(dt);
		
		if(shakeTime > 0.0f){
			shakeTime -= dt;
			if(shakeTime <= 0.0f)
				effects.shake = false;
		}
		
		if(ball.position.y >= height){
			resetLevel();
			resetPlayer();
		}
	}
	
	public void render(){
		if(state == GameState.GAME_ACTIVE){
			effects.beginRender();
			renderer.drawSprite(ResourceManager.getTexture("background"), new Vector2f(0, 0), new Vector2f(width, height),
								0.0f, new Vector3f(1));
			levels.get(level).draw(renderer);
			player.draw(renderer);
			for(PowerUp p:powerUps){
				if(!p.isDestroyed())
					p.draw(renderer);
			}
			//particles.draw();
			ball.draw(renderer);
			effects.endRender();
			effects.render((float) glfwGetTime());
		}
	}
	
	public void clear(){
		renderer.clear();
	}
	
	public void resetLevel(){
		if(level == 0)
			levels.get(level).load("one", width, (int)(height * 0.5f));
		else if(level == 1)
			levels.get(level).load("two", width, (int)(height * 0.5f));
		else if(level == 2)
			levels.get(level).load("three", width, (int)(height * 0.5f));
		else if(level == 3)
			levels.get(level).load("four", width, (int)(height * 0.5f));
	}
	
	public void resetPlayer(){
		player.setSize(PLAYER_SIZE);
		player.setPosition(width / 2 - PLAYER_SIZE.x /2, height - PLAYER_SIZE.y);
		Vector2f ballPos = new Vector2f(PLAYER_SIZE.x / 2 - BALL_RADIUS, -BALL_RADIUS * 2);
		ballPos.add(player.position);
		ball.reset(ballPos, INITIAL_BALL_VELOCITY);
		effects.chaos = effects.confuse = false;
		ball.setPassThrough(false);
		ball.setSticky(false);
		player.setColor(new Vector3f(1));
		ball.setColor(new Vector3f(1));
	}
	
	private void doCollisions(){
		for(GameObject box:levels.get(level).bricks){
			if(!box.isDestroyed()){
				Collision collision = checkCollision(ball, box);
				// if collision is true
				if(collision.isCollision()){
					if(!box.isSolid()){
						box.setDestroyed(true);
						spawnPowerUps(box);
					}
					else {
						shakeTime = 0.05f;
						effects.shake = true;
					}
						
					// collision resolution
					Direction dir = collision.getCollisionDirec();
					Vector2f diffVector = collision.getCollisionVec();
					// horizontal collision
					if(dir == Direction.LEFT || dir == Direction.RIGHT){
						ball.velocity.x = -ball.velocity.x;
						// relocate
						float penetration = ball.radius - Math.abs(diffVector.x);
						if(dir == Direction.LEFT)
							ball.position.x += penetration; // move ball to the right
						else
							ball.position.x -= penetration; // move ball to the left
					}
					// vertical collicion
					else {
						ball.velocity.y = -ball.velocity.y;
						// relocate
						float penetration = ball.radius - Math.abs(diffVector.y);
						if(dir == Direction.UP)
							ball.position.y -= penetration; // move ball to up
						else
							ball.position.y += penetration; // move ball to down
					}
				}
			}
		}	// for loop
		
		// Also check collisions on PowerUps and if so, activate them
		for(PowerUp powerUp:powerUps){
			// First check if powerup passed bottom edge, if so: keep as inactive and destroy
			if(powerUp.position.y >= height)
				powerUp.setDestroyed(true);
			
			if(checkCollision(player, powerUp)){
				activatePowerUp(powerUp);
				powerUp.setDestroyed(true);
				powerUp.activated = true;
			}
		}
		
		// check collisions for player pad (unless stuck)
		Collision result = checkCollision(ball, player);
		if(!ball.isStuck() && result.isCollision()){
			// Check where it hit the board, and change velocity based on where it hit the board
			float centerBoard = player.position.x + player.size.x / 2;
			float distance = (ball.position.x + ball.radius) - centerBoard;
			float percentage = distance / (player.size.x / 2);
			// Then move accordingly
			float strength = 2.0f;
			Vector2f oldVelocity = new Vector2f(ball.velocity);
			ball.velocity.x = INITIAL_BALL_VELOCITY.x * percentage * strength;
			ball.velocity.y = -ball.velocity.y;
			// Keep speed consistent over both axes (multiply by length of old velocity, so total strength is not changed)
			ball.velocity.normalize();
			ball.velocity.x *= oldVelocity.length();
			ball.velocity.y *= oldVelocity.length();
			// Fix sticky paddle
			ball.velocity.y = -1 * Math.abs(ball.velocity.y);
			
			// If Sticky powerup is activated, also stick ball to paddle once new velocity vectors were calculated
			ball.setStuck(ball.isSticky());
		}
	}
	
	private boolean shouldSpawn(int chance){
		int random = rand.nextInt() % chance;
		return random == 0;
	}
	
	private void spawnPowerUps(GameObject block){
		if(shouldSpawn(75))
			powerUps.add(new PowerUp("speed", new Vector3f(0.5f, 0.5f, 1.0f), 0.0f, block.position, ResourceManager.getTexture("powerup_speed")));
		if(shouldSpawn(75))
			powerUps.add(new PowerUp("sticky", new Vector3f(1.0f, 0.5f, 1.0f), 20.0f, block.position, ResourceManager.getTexture("powerup_sticky")));
		if(shouldSpawn(75))
			powerUps.add(new PowerUp("pass-through", new Vector3f(0.5f, 1.0f, 0.5f), 10.0f, block.position, ResourceManager.getTexture("powerup_passthrough")));
		if(shouldSpawn(75))
			powerUps.add(new PowerUp("pad-size-increase", new Vector3f(1.0f, 0.6f, 0.4f), 0.0f, block.position, ResourceManager.getTexture("powerup_increase")));
		// negative powerups
		if(shouldSpawn(15))
			powerUps.add(new PowerUp("confuse", new Vector3f(1.0f, 0.3f, 0.3f), 15.0f, block.position, ResourceManager.getTexture("powerup_confuse")));
		if(shouldSpawn(15))
			powerUps.add(new PowerUp("chaos", new Vector3f(0.9f, 0.25f, 0.25f), 15.0f, block.position, ResourceManager.getTexture("powerup_chaos")));
	}
	
	private void activatePowerUp(PowerUp powerUp){
		if(powerUp.type == "speed")
			ball.velocity.mul(1.2f);
		else if(powerUp.type == "sticky"){
			ball.setSticky(true);
			player.setColor(new Vector3f(1.0f, 0.5f, 1.0f));
		}
		else if(powerUp.type == "pass-through"){
			ball.setPassThrough(true);
			ball.setColor(new Vector3f(1.0f, 0.5f, 0.5f));
		}
		else if(powerUp.type == "pad-size-increase")
			player.size.x += 50;
		else if(powerUp.type == "confuse"){
			if(!effects.chaos)
				effects.confuse = true;
		}
		else if(powerUp.type == "chaos"){
			if(!effects.confuse)
				effects.chaos = true;
		}
	}
	
	private boolean isOtherPowerupActive(String type){
		for(PowerUp powerUp:powerUps){
			if(powerUp.activated == true){
				if(powerUp.type == type)
					return true;
			}
		}
		return false;
	}
	
	private void updatePowerUps(float dt){
		for(PowerUp powerUp:powerUps){
			powerUp.position.x += powerUp.velocity.x * dt;
			powerUp.position.y += powerUp.velocity.y * dt;
			
			if(powerUp.activated){
				powerUp.duration -= dt;
				if(powerUp.duration <= 0.0f){
					// Remove powerup from list (will later be removed)
					powerUp.activated = false;
					// Deactivate effects
					if(powerUp.type == "sticky"){
						if(!isOtherPowerupActive("sticky")){
							ball.setSticky(false);
							player.setColor(new Vector3f(1));
						}							
					}
					else if(powerUp.type == "pass-through"){
						if(!isOtherPowerupActive("pass-through")){
							ball.setPassThrough(false);
							ball.setColor(new Vector3f(1));
						}
					}
					else if(powerUp.type == "confuse"){
						if(!isOtherPowerupActive("confuse"))
							effects.confuse = false;
					}
					else if(powerUp.type == "chaos"){
						if(!isOtherPowerupActive("chaos"))
							effects.chaos = false;
					}
				}
			}			
		}
		//  Remove all PowerUps from vector that are destroyed AND !activated (thus either off the map or finished)
		for(int i = 0; i < powerUps.size(); i++){
			PowerUp p = powerUps.get(i);
			if(p.isDestroyed() && !p.activated)
				powerUps.remove(i);
		}
		
	}
	
	private class Collision{
		private boolean collision;
		private Direction collisionDirec;
		private Vector2f collisionVec;
		
		public Collision(boolean collision, Direction collisionDir, Vector2f collisionVec){
			this.collision = collision;
			this.collisionDirec = collisionDir;
			this.collisionVec = collisionVec;
		}

		public boolean isCollision() {
			return collision;
		}

		public Direction getCollisionDirec() {
			return collisionDirec;
		}

		public Vector2f getCollisionVec() {
			return collisionVec;
		}
	}
	
	//  AABB - AABB collision
	private boolean checkCollision(GameObject one, GameObject two){
		boolean collisionX = one.position.x + one.size.x >= two.position.x &&
							two.position.x + two.size.x >= one.position.x;
							
		boolean collisionY = one.position.y + one.size.y >= two.position.y &&
				two.position.y + two.size.y >= one.position.y;
				
		return collisionX && collisionY;
	}
	
	// AABB - circle collision
	private Collision checkCollision(BallObject one, GameObject two){
		// first get the center point of the ball(circle)
		Vector2f center = new Vector2f(one.position.x + one.radius, one.position.y + one.radius);
		// then calculate aabb info (center, half extents)
		Vector2f aabbHalfExtents = new Vector2f(two.size.x / 2, two.size.y /2);
		Vector2f aabbCenter = new Vector2f(two.position.x + aabbHalfExtents.x,
										   two.position.y + aabbHalfExtents.y);
		// get difference vector
		Vector2f difference = new Vector2f(center.x - aabbCenter.x, center.y - aabbCenter.y);
		Vector2f clamped = clamp(difference, new Vector2f(-aabbHalfExtents.x, -aabbHalfExtents.y), aabbHalfExtents);
		// add clamped value to AABB_center and we get the value of box closest to circle
		Vector2f closest = new Vector2f(aabbCenter.x + clamped.x, aabbCenter.y + clamped.y);
		// retrieve vector between center circle and closest point AABB and check if length <= radius
		difference.x = closest.x - center.x;
		difference.y = closest.y - center.y;
		
		 if(difference.length() < one.radius)
			 return new Collision(true, vectorDirection(difference), difference);
		 else
			 return new Collision(false, Direction.UP, new Vector2f(0, 0));
	}
	
	private Vector2f clamp(Vector2f value, Vector2f min, Vector2f max){
		Vector2f v = new Vector2f();
		v.x = Math.max(min.x, Math.min(max.x, value.x));
		v.y = Math.max(min.y, Math.min(max.y, value.y));
		return v;
	}
	
	// calculates which direction a vector is facing (N,E,S or W)
	private Direction vectorDirection(Vector2f target){
		List<Vector2f> compass = new ArrayList<Vector2f>();
		compass.add(new Vector2f(0.0f, 1.0f));	// up
		compass.add(new Vector2f(1.0f, 0.0f));	// right
		compass.add(new Vector2f(0.0f, -1.0f));	// down
		compass.add(new Vector2f(-1.0f, 0.0f));	// left
		
		float max = 0;
		int bestMatch = -1;
		
		for(int i=0; i<4; i++){
			float dotProduct = compass.get(i).dot(target.normalize());
			if(dotProduct > max){
				max = dotProduct;
				bestMatch = i;
			}
		}
		
		Direction d = null;
		
		switch(bestMatch){
		case 0:
			d = Direction.UP;
			break;
		case 1:
			d = Direction.RIGHT;
			break;
		case 2:
			d = Direction.DOWN;
			break;
		case 3:
			d = Direction.LEFT;
			break;
		}
		
		return d;
	}
	
	public void setKeysState(int key, boolean state){
		keys[key] = state;
	}
	
	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}
	
}
