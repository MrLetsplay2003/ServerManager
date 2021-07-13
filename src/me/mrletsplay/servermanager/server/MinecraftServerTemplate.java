package me.mrletsplay.servermanager.server;

public class MinecraftServerTemplate {
	
	private String id;
	private String path;
	
	public MinecraftServerTemplate(String id, String path) {
		this.id = id;
		this.path = path;
	}
	
	public String getId() {
		return id;
	}

	public String getPath() {
		return path;
	}
	
}
