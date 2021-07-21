package me.mrletsplay.servermanager.server.meta;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.util.PaperVersion;

public class ServerMetadata implements JSONConvertible {
	
	@JSONValue
	private String id;
	
	@JSONValue
	private String name;
	
	@JSONValue
	private String version;
	
	@JSONValue
	private String javaVersion = JavaVersion.SYSTEM.getID();
	
	@JSONValue
	private int memoryLimitMiB = 1024;
	
	@JSONValue
	private boolean autostart = false;
	
	@JSONConstructor
	private ServerMetadata() {}

	public ServerMetadata(String id, String name, String version, JavaVersion javaVersion) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.javaVersion = javaVersion.getID();
	}

	public String getID() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}
	
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}
	
	public String getJavaVersion() {
		return javaVersion;
	}
	
	public void setMemoryLimitMB(int memoryLimitMB) {
		this.memoryLimitMiB = memoryLimitMB;
	}

	public int getMemoryLimitMiB() {
		return memoryLimitMiB;
	}
	
	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}
	
	public boolean isAutostart() {
		return autostart;
	}
	
	public boolean isValid() {
		return id != null
				&& name != null
				&& version != null
				&& javaVersion != null
				&& memoryLimitMiB > 0
				&& PaperVersion.getByVersion(getVersion()) != null;
	}
	
}
