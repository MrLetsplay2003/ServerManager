package me.mrletsplay.servermanager.server.whitelist;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class ServerWhitelist {
	
	private File file;
	private List<WhitelistedUser> whitelistedUsers;
	
	public ServerWhitelist(File file) {
		this.file = file;
		this.whitelistedUsers = new ArrayList<>();
		try {
			String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			JSONArray arr = new JSONArray(json);
			for(Object o : arr) {
				JSONObject u = (JSONObject) o;
				whitelistedUsers.add(new WhitelistedUser(u.getString("uuid"), u.getString("name")));
			}
		}catch(IOException e) {
			throw new FriendlyException("Failed to load whitelist", e);
		}
	}
	
	public List<WhitelistedUser> getWhitelistedUsers() {
		return whitelistedUsers;
	}
	
	public void addWhitelistedUser(String uuid, String name) {
		whitelistedUsers.removeIf(u -> u.getUUID().equals(uuid) || u.getName().equals(name));
		whitelistedUsers.add(new WhitelistedUser(uuid, name));
	}
	
	public void save() {
		try {
			JSONArray arr = new JSONArray();
			whitelistedUsers.forEach(u -> {
				JSONObject o = new JSONObject();
				o.put("uuid", u.getUUID());
				o.put("name", u.getName());
				arr.add(o);
			});
			Files.write(file.toPath(), arr.toFancyString().toString().getBytes(StandardCharsets.UTF_8));
		}catch(IOException e) {
			throw new FriendlyException("Failed to save whitelist", e);
		}
	}

}
