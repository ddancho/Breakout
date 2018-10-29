package game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import resourceManager.ResourceManager;
import sprites.SpriteRenderer;

public class GameLevel {
	
	List<GameObject> bricks;
	
	public GameLevel(){
		bricks = new ArrayList<GameObject>();
	}
	
	public void load(String file, int levelWidth, int levelHeight){
		bricks.clear();
		ArrayList<ArrayList<Integer>> tileData = new ArrayList<ArrayList<Integer>>();				
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("levels\\" + file + ".txt"));
			String line;
			while((line = reader.readLine()) != null){
				String[] tokens = line.split(" ");
				ArrayList<Integer> row = new ArrayList<Integer>();
				for(String token:tokens){
					row.add(Integer.parseInt(token));
				}
				tileData.add(row);
			}
			reader.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(tileData.size() > 0)
			init(tileData, levelWidth, levelHeight);
		
	}
	
	public void draw(SpriteRenderer renderer){
		for(GameObject tile:bricks){
			if(!tile.isDestroyed())
				tile.draw(renderer);
		}
	}
	
	public boolean isCompleted(){
		for(GameObject tile:bricks){
			if(!tile.isSolid() && !tile.isDestroyed())
				return false;
		}
		return true;
	}
	
	private void init(ArrayList<ArrayList<Integer>> tileData, int levelWidth, int levelHeight){
		int height = tileData.size();
		int width = tileData.get(0).size();
		// get size of each brick
		float unitWidth = levelWidth / (float)width;
		float unitHeight = levelHeight / height;
		
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				
				// solid brick
				if(tileData.get(y).get(x) == 1){
					Vector2f position = new Vector2f(unitWidth * x, unitHeight * y);
					Vector2f size = new Vector2f(unitWidth, unitHeight);
					GameObject obj = new GameObject(position, size, ResourceManager.getTexture("block_solid"), new Vector3f(0.8f, 0.8f, 0.7f));
					obj.setSolid(true);
					bricks.add(obj);
				}
				// non solid brick
				else if(tileData.get(y).get(x) > 1){
					// set color based on level data
					Vector3f color = new Vector3f(1); // original white
					if(tileData.get(y).get(x) == 2)
						color = new Vector3f(0.2f, 0.6f, 1.0f);
					else if(tileData.get(y).get(x) == 3)
						color = new Vector3f(0.0f, 0.7f, 0.0f);
					else if(tileData.get(y).get(x) == 4)
						color = new Vector3f(0.8f, 0.8f, 0.4f);
					else if(tileData.get(y).get(x) == 5)
						color = new Vector3f(1.0f, 0.5f, 0.0f);
					
					Vector2f position = new Vector2f(unitWidth * x, unitHeight * y);
					Vector2f size = new Vector2f(unitWidth, unitHeight);
					bricks.add(new GameObject(position, size, ResourceManager.getTexture("block"), color));
				}
				
			}
		}
		
	}
	
}










