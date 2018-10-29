package resource;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public class Shader {
	
	private int programID;
	
	public Shader(){}
	
	public void compile(String vertexSource, String fragmentSource, String geometrySource) throws Exception{
		programID = glCreateProgram();
		if(programID == 0)
			throw new Exception("Could not create shader");
		
		int vertexID = 0, fragmentID = 0, geometryID = 0;
		vertexID = createShader(loadShader(vertexSource), GL_VERTEX_SHADER);
		fragmentID = createShader(loadShader(fragmentSource), GL_FRAGMENT_SHADER);
		if(geometryID != NULL)
			geometryID = createShader(loadShader(geometrySource), GL_GEOMETRY_SHADER);
		
		glLinkProgram(programID);
		if(glGetProgrami(programID, GL_LINK_STATUS) == NULL)
			throw new Exception("Error linking shader code " + glGetShaderInfoLog(programID, 1024));
		
		glValidateProgram(programID);
		if(glGetProgrami(programID, GL_VALIDATE_STATUS) == NULL)
			throw new Exception("Error validating shader code " + glGetShaderInfoLog(programID, 1024));
		
		glDeleteShader(vertexID);
		glDeleteShader(fragmentID);
		if(geometryID != NULL)
			glDeleteShader(geometryID);
	}
	
	public void setFloat(String uniformName, float value){
		glUniform1f(glGetUniformLocation(programID, uniformName), value);
	}
	
	public void setInteger(String uniformName, int value){
		glUniform1i(glGetUniformLocation(programID, uniformName), value);
	}
	
	public void setVector2f(String uniformName, float x, float y){
		glUniform2f(glGetUniformLocation(programID, uniformName), x, y);
	}
	
	public void setVector2f(String uniformName, Vector2f value){
		glUniform2f(glGetUniformLocation(programID, uniformName), value.x, value.y);
	}
	
	public void setVector3f(String uniformName, float x, float y, float z){
		glUniform3f(glGetUniformLocation(programID, uniformName), x, y, z);
	}
	
	public void setVector3f(String uniformName, Vector3f value){
		glUniform3f(glGetUniformLocation(programID, uniformName), value.x, value.y, value.z);
	}
	
	public void setVector4f(String uniformName, float x, float y, float z, float w){
		glUniform4f(glGetUniformLocation(programID, uniformName), x, y, z, w);
	}
	
	public void setVector4f(String uniformName, Vector4f value){
		glUniform4f(glGetUniformLocation(programID, uniformName), value.x, value.y, value.z, value.w);
	}
	
	public void setMatrix4f(String uniformName, Matrix4f value){
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		value.get(fb);
		glUniformMatrix4fv(glGetUniformLocation(programID, uniformName), false, fb);
	}
	
	public void use(){
		glUseProgram(programID);
	}
	
	private int createShader(String shaderCode, int shaderType) throws Exception {
		int shaderID = glCreateShader(shaderType);
		if(shaderID == 0)
			throw new Exception("Error creating shader , code: " + shaderID);
		
		glShaderSource(shaderID, shaderCode);
		glCompileShader(shaderID);
		
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == NULL)
			throw new Exception("Error compiling shader, code: " + glGetShaderInfoLog(shaderID, 1024));
		
		glAttachShader(programID, shaderID);
		
		return shaderID;
	}
	
	private String loadShader(String shaderFile){
		StringBuilder shaderSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("shaders\\" + shaderFile + ".txt"));
			String line;
			while((line = reader.readLine()) != null){
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return shaderSource.toString();
	}

	public int getProgramID() {
		return programID;
	}
	
}
