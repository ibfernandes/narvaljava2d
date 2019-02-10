package engine.graphic;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;

import engine.utilities.BufferUtilities;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class Shader {
	private int id;

	public Shader use() {
		glUseProgram(this.id);
		return this;
	}

	public int getId() {
		return id;
	}

	/**
	 * Compile all vertex, fragment and geometry source code into a linked program.
	 * Geometry is optional.
	 * 
	 * @param vertexSource
	 * @param fragmentSource
	 * @param geometrySource
	 */
	public void compile(String vertexSource, String fragmentSource, String geometrySource) {
		int vertexShader, geometryShader = 0, fragmentShader;

		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexSource);
		glCompileShader(vertexShader);
		checkCompileErrors(vertexShader, "VERTEX");

		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentSource);
		glCompileShader(fragmentShader);
		checkCompileErrors(fragmentShader, "FRAGMENT");

		if (geometrySource != null) {
			geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
			glShaderSource(geometryShader, geometrySource);
			glCompileShader(geometryShader);
			checkCompileErrors(fragmentShader, "GEOMETRY");
		}

		id = glCreateProgram();
		glAttachShader(id, vertexShader);
		if (geometrySource != null)
			glAttachShader(id, geometryShader);
		glAttachShader(id, fragmentShader);
		glLinkProgram(id);
		checkCompileErrors(id, "PROGRAM");

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		if (geometrySource != null)
			glDeleteShader(geometryShader);
	}

	/**
	 * Checks and print compiling ERRORS.
	 * 
	 * @param object
	 * @param type
	 */
	private void checkCompileErrors(int object, String type) {
		int success;
		String log;

		if (type != "PROGRAM") {
			success = glGetShaderi(object, GL_COMPILE_STATUS);

			if (success == 0) {
				log = glGetShaderInfoLog(object);
				System.err.println(log);
			}

		} else {
			success = glGetProgrami(object, GL_LINK_STATUS);

			if (success == 0) {
				log = glGetProgramInfoLog(object);
				System.err.println(log);
			}

		}
	}

	public void setFloat(String name, float value) {
		glUniform1f(glGetUniformLocation(id, name), value);
	}

	public void setInteger(String name, int value) {
		glUniform1i(glGetUniformLocation(id, name), value);
	}

	public void setVec2(String name, float x, float y) {
		glUniform2f(glGetUniformLocation(id, name), x, y);
	}

	public void setVec2(String name, Vec2 pos) {
		glUniform2f(glGetUniformLocation(id, name), pos.x, pos.y);
	}

	public void setVec3(String name, float x, float y, float z) {
		glUniform3f(glGetUniformLocation(id, name), x, y, z);
	}

	public void setVec3(String name, Vec3 pos) {
		glUniform3f(glGetUniformLocation(id, name), pos.x, pos.y, pos.z);
	}

	public void setVec4(String name, float x, float y, float z, float w) {
		glUniform4f(glGetUniformLocation(id, name), x, y, z, w);
	}

	public void setVec4(String name, Vec4 pos) {
		glUniform4f(glGetUniformLocation(id, name), pos.x, pos.y, pos.z, pos.w);
	}

	public void setMat4(String name, FloatBuffer bf) {
		glUniformMatrix4fv(glGetUniformLocation(id, name), false, bf);
	}

	public void setPointLight(int index, Vec3 pos, Vec3 ambientColor, Vec3 lightColor, float constant, float linear,
			float quadratic) {
		glUniform3f(glGetUniformLocation(id, "pointLights[" + index + "].position"), pos.x, pos.y, pos.z);
		glUniform3f(glGetUniformLocation(id, "pointLights[" + index + "].ambient"), ambientColor.x, ambientColor.y,
				ambientColor.z);
		glUniform3f(glGetUniformLocation(id, "pointLights[" + index + "].lightColor"), lightColor.x, lightColor.y,
				lightColor.z);
		glUniform1f(glGetUniformLocation(id, "pointLights[" + index + "].constant"), constant);
		glUniform1f(glGetUniformLocation(id, "pointLights[" + index + "].linear"), linear);
		glUniform1f(glGetUniformLocation(id, "pointLights[" + index + "].quadratic"), quadratic);
	}

}
