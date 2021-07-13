package me.mrletsplay.servermanager.util.meta;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class ServerMetadata implements JSONConvertible {
	
	@JSONValue
	private String id;
	
	@JSONValue
	private String name;
	
	@JSONValue
	private String version;
	
	@JSONValue
	private int memoryLimitMB = 1024;
	
	@JSONConstructor
	private ServerMetadata() {}

	public ServerMetadata(String id, String name, String version) {
		this.id = id;
		this.name = name;
		this.version = version;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public int getMemoryLimitMB() {
		return memoryLimitMB;
	}
	
}
