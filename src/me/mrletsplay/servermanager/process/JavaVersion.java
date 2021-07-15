package me.mrletsplay.servermanager.process;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class JavaVersion implements JSONConvertible {
	
	public static final JavaVersion SYSTEM = new JavaVersion("system", "System Default", "java");
	
	private static final List<JavaVersion> JAVA_VERSIONS = new ArrayList<>();
	
	static {
		JAVA_VERSIONS.add(SYSTEM);
	}
	
	@JSONValue
	private String id;
	
	@JSONValue
	private String name;
	
	@JSONValue
	private String javaPath;
	
	@JSONConstructor
	private JavaVersion() {}

	public JavaVersion(String id, String name, String javaPath) {
		this.id = id;
		this.name = name;
		this.javaPath = javaPath;
	}
	
	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getJavaPath() {
		return javaPath;
	}
	
	public boolean isSystemDefault() {
		return id.equals("system");
	}
	
	public static void addJavaVersion(JavaVersion version) {
		JAVA_VERSIONS.add(version);
	}
	
	public static void removeJavaVersion(JavaVersion version) {
		JAVA_VERSIONS.remove(version);
	}
	
	public static JavaVersion getJavaVersion(String id) {
		return JAVA_VERSIONS.stream()
			.filter(v -> v.getID().equals(id))
			.findFirst().orElse(null);
	}
	
	public static List<JavaVersion> getJavaVersions() {
		return JAVA_VERSIONS;
	}
	
}
