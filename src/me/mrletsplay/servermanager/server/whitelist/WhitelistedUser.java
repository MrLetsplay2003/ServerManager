package me.mrletsplay.servermanager.server.whitelist;

public class WhitelistedUser {
	
	private String uuid;
	private String name;
	
	public WhitelistedUser(String uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public String getName() {
		return name;
	}
	
}
