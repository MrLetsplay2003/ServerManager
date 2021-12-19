package me.mrletsplay.servermanager.util;

import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.http.HttpGet;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;

public class PaperAPI {
	
	public static List<String> getPaperVersions() {
		JSONObject obj = new HttpGet("https://papermc.io/api/v2/projects/paper").execute().asJSONObject();
		return obj.getJSONArray("versions").stream()
				.map(v -> (String) v)
				.collect(Collectors.toList());
	}
	
	public static String getLatestBuildURL(String version) {
		try {
			JSONObject obj = new HttpGet("https://papermc.io/api/v2/projects/paper/versions/" + version).execute().asJSONObject();
			JSONArray builds = obj.getJSONArray("builds");
			int latestBuild = builds.getInt(builds.size() - 1);
			return String.format("https://papermc.io/api/v2/projects/paper/versions/%s/builds/%s/downloads/paper-%s-%s.jar", version, latestBuild, version, latestBuild);
		}catch(IllegalStateException e) {
			return null;
		}
	}
	
	public static List<String> getVelocityVersions() {
		JSONObject obj = new HttpGet("https://papermc.io/api/v2/projects/velocity").execute().asJSONObject();
		return obj.getJSONArray("versions").stream()
				.map(v -> (String) v)
				.collect(Collectors.toList());
	}
	
	public static String getVelocityBuildURL(String version) {
		try {
			JSONObject obj = new HttpGet("https://papermc.io/api/v2/projects/velocity/versions/" + version).execute().asJSONObject();
			JSONArray builds = obj.getJSONArray("builds");
			int latestBuild = builds.getInt(builds.size() - 1);
			return String.format("https://papermc.io/api/v2/projects/velocity/versions/%s/builds/%s/downloads/velocity-%s-%s.jar", version, latestBuild, version, latestBuild);
		}catch(IllegalStateException e) {
			return null;
		}
	}

}
