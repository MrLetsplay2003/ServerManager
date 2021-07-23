package me.mrletsplay.servermanager.webinterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import me.mrletsplay.mrcore.http.HttpGet;
import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.SetupHelper;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.servermanager.util.PaperVersion;
import me.mrletsplay.servermanager.util.ScheduledRestart;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfaceSettingsPage;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceActionHandler;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceHandler;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceRequestEvent;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceResponse;

public class RequestHandler implements WebinterfaceActionHandler {
	
	private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_-]{1,32}");
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "setSetting")
	public WebinterfaceResponse setSetting(WebinterfaceRequestEvent event) {
		return WebinterfaceSettingsPage.handleSetSettingRequest(Webinterface.getConfig(), event);
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "installVelocity")
	public WebinterfaceResponse installVelocity(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String portStr = v.getString("port");
		if(portStr.isBlank()) return WebinterfaceResponse.error("Port must be set");
		int port;
		try {
			port = Integer.parseInt(portStr);
		}catch(NumberFormatException e) {
			return WebinterfaceResponse.error("Invalid port");
		}
		SetupHelper.installVelocity(port);
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "setVelocityForwardingMode")
	public WebinterfaceResponse setVelocityForwardingMode(WebinterfaceRequestEvent event) {
		String mode = event.getRequestData().getString("value");
		
		VelocityForwardingMode m;
		try {
			m = VelocityForwardingMode.valueOf(mode);
		}catch(IllegalArgumentException e) {
			return WebinterfaceResponse.error("Invalid forwarding mode");
		}
		
		if(m == VelocityForwardingMode.MODERN && ServerManager.getServers().stream().anyMatch(s -> !s.getVersion().supportsModernForwarding())) {
			return WebinterfaceResponse.error("Can't enable modern forwarding: Server(s) too old");
		}
		
		VelocityBase.setForwardingMode(m);
		for(MinecraftServer server : ServerManager.getServers()) {
			server.updateForwardingMode(m);
		}
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "setVelocityAutostart")
	public WebinterfaceResponse setVelocityAutostart(WebinterfaceRequestEvent event) {
		boolean autostart = event.getRequestData().getBoolean("value");
		
		VelocityBase.setAutostart(autostart);
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "reloadEverything")
	public WebinterfaceResponse reloadEverything(WebinterfaceRequestEvent event) {
		ServerManager.fullReload();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "shutdownEverything")
	public WebinterfaceResponse shutdownEverything(WebinterfaceRequestEvent event) {
		if(VelocityBase.isRunning()) VelocityBase.stop();
		for(MinecraftServer server : ServerManager.getServers()) {
			if(server.isRunning()) server.stop();
		}
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "createServer")
	public WebinterfaceResponse createServer(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String id = v.getString("id");
		String name = v.getString("name");
		String version = v.getString("version");
		String jVersion = v.getString("javaVersion");
		if(id.isBlank() || name.isBlank()) return WebinterfaceResponse.error("Both id and name must be set");
		if(!ID_PATTERN.matcher(id).matches()) return WebinterfaceResponse.error("ID must match " + ID_PATTERN.pattern());
		PaperVersion papV;
		try {
			papV = PaperVersion.valueOf(version);
		}catch(IllegalArgumentException e) {
			return WebinterfaceResponse.error("Invalid version");
		}
		if(ServerManager.getServer(id) != null || id.equals("base")) return WebinterfaceResponse.error("Server already exists");
		JavaVersion javaVersion = JavaVersion.getJavaVersion(jVersion);
		if(javaVersion == null) return WebinterfaceResponse.error("Invalid Java version");
		SetupHelper.createNewServer(false, id, name, papV, javaVersion);
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "getConsoleLog")
	public WebinterfaceResponse getConsoleLog(WebinterfaceRequestEvent event) {
		String server = event.getRequestData().getString("server");
		
		if(server.equals("base")) {
			JSONObject o = new JSONObject();
			o.put("log", VelocityBase.isRunning() ? new JSONArray(VelocityBase.getVelocityProcess().getLogHistory()) : null);
			return WebinterfaceResponse.success(o);
		}
		
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		JSONObject o = new JSONObject();
		o.put("log", s.isRunning() ? new JSONArray(s.getProcess().getLogHistory()) : null);
		return WebinterfaceResponse.success(o);
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "startServer")
	public WebinterfaceResponse startServer(WebinterfaceRequestEvent event) {
		String server = event.getRequestData().getString("value");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		s.start();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "stopServer")
	public WebinterfaceResponse stopServer(WebinterfaceRequestEvent event) {
		String server = event.getRequestData().getString("value");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		s.stop();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "executeConsoleCommand")
	public WebinterfaceResponse executeConsoleCommand(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String server = v.getString("server");
		String command = v.getString("command");
		
		if(server.equals("base")) {
			if(!VelocityBase.isRunning()) return WebinterfaceResponse.error("Velocity is not running");
			if(command.isBlank()) return WebinterfaceResponse.success();
			VelocityBase.getVelocityProcess().sendLine(command);
			return WebinterfaceResponse.success();
		}
		
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		if(!s.isRunning()) return WebinterfaceResponse.error("Server is not running");
		if(command.isBlank()) return WebinterfaceResponse.success();
		s.getProcess().sendLine(command);
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "startVelocity")
	public WebinterfaceResponse startVelocity(WebinterfaceRequestEvent event) {
		VelocityBase.start();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "stopVelocity")
	public WebinterfaceResponse stopVelocity(WebinterfaceRequestEvent event) {
		VelocityBase.stop();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "updateServerVersion")
	public WebinterfaceResponse updateServerVersion(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String server = v.getString("server");
		String version = v.getString("version");
		
		PaperVersion papV;
		try {
			papV = PaperVersion.valueOf(version);
		}catch(IllegalArgumentException e) {
			return WebinterfaceResponse.error("Invalid version");
		}
		
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		
		boolean running = s.isRunning();
		if(running) s.stop();
		
		File paperJar = new File(s.getServerFolder(), "paper.jar");
		String latestPaperURL = PaperAPI.getLatestBuildURL(papV.getVersion());
		try {
			new HttpGet(latestPaperURL).execute().transferTo(paperJar);
		} catch (IOException e) {
			throw new FriendlyException("Failed to download Paper", e);
		}
		
		s.getMetadata().setVersion(papV.getVersion());
		s.saveMetadata();
		
		if(running) s.start();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "updateServerJavaVersion")
	public WebinterfaceResponse updateServerJavaVersion(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String server = v.getString("server");
		String version = v.getString("javaVersion");
		JavaVersion jV = JavaVersion.getJavaVersion(version);
		if(jV == null) return WebinterfaceResponse.error("Invalid Java version");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		s.getMetadata().setJavaVersion(version);
		s.saveMetadata();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "updateServerMemory")
	public WebinterfaceResponse updateServerMemory(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String server = v.getString("server");
		String memory = v.getString("memory");
		int memoryMB;
		try {
			memoryMB = Integer.parseInt(memory);
		}catch(NumberFormatException e) {
			return WebinterfaceResponse.error("Invalid number");
		}
		if(memoryMB <= 0) return WebinterfaceResponse.error("Invalid amount of memory");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		s.getMetadata().setMemoryLimitMB(memoryMB);
		s.saveMetadata();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "updateServerAutostart")
	public WebinterfaceResponse updateServerAutostart(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String server = v.getString("server");
		boolean autostart = v.getBoolean("autostart");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		s.getMetadata().setAutostart(autostart);
		s.saveMetadata();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "deleteServer")
	public WebinterfaceResponse deleteServer(WebinterfaceRequestEvent event) {
		String server = event.getRequestData().getString("value");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		if(s.isRunning()) s.stop();
		IOUtils.deleteFile(s.getServerFolder());
		ServerManager.removeServer(s);
		CommentedFileConfig c = VelocityBase.loadVelocityConfig();
		c.remove("servers." + server);
		c.save();
		c.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "addJavaVersion")
	public WebinterfaceResponse addJavaVersion(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String id = v.getString("id");
		String name = v.getString("name");
		String javaPath = v.getString("javaPath");
		if(JavaVersion.getJavaVersion(id) != null) return WebinterfaceResponse.error("A Java version with that ID already exists");
		if(!ID_PATTERN.matcher(id).matches()) return WebinterfaceResponse.error("ID must match " + ID_PATTERN.pattern());
		File javaFile = new File(javaPath);
		if(!javaFile.exists() || !javaFile.isFile()) return WebinterfaceResponse.error("The Java path must point to the Java binary");
		JavaVersion.addJavaVersion(new JavaVersion(id, name, javaPath));
		ServerManager.saveJavaVersions();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "removeJavaVersion")
	public WebinterfaceResponse removeJavaVersion(WebinterfaceRequestEvent event) {
		String id = event.getRequestData().getString("value");
		JavaVersion v = JavaVersion.getJavaVersion(id);
		if(v == null || id.equals(JavaVersion.SYSTEM.getID())) return WebinterfaceResponse.error("Invalid Java version");
		JavaVersion.removeJavaVersion(v);
		ServerManager.saveJavaVersions();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "addHostname")
	public WebinterfaceResponse addHostname(WebinterfaceRequestEvent event) {
		String hostname = event.getRequestData().getString("value");
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		if(config.contains(Arrays.asList("forced-hosts", hostname))) return WebinterfaceResponse.error("Hostname already exists");
		config.set(Arrays.asList("forced-hosts", hostname), new ArrayList<>());
		config.save();
		config.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "removeHostname")
	public WebinterfaceResponse removeHostname(WebinterfaceRequestEvent event) {
		String hostname = event.getRequestData().getString("value");
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		if(!config.contains(Arrays.asList("forced-hosts", hostname))) return WebinterfaceResponse.error("Hostname doesn't exist");
		config.remove(Arrays.asList("forced-hosts", hostname));
		config.save();
		config.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "addServerToHostname")
	public WebinterfaceResponse addServerToHostname(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String hostname = value.getString("hostname");
		String server = value.getString("server");
		
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		if(!config.contains(Arrays.asList("forced-hosts", hostname))) return WebinterfaceResponse.error("Hostname doesn't exist");
		
		if(ServerManager.getServer(server) == null) return WebinterfaceResponse.error("Invalid server");
		
		List<String> servers = config.get(Arrays.asList("forced-hosts", hostname));
		if(servers.contains(server)) return WebinterfaceResponse.error("Server already added");
		
		servers.add(server);
		config.set(Arrays.asList("forced-hosts", hostname), servers);
		config.save();
		config.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "removeServerFromHostname")
	public WebinterfaceResponse removeServerFromHostname(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String hostname = value.getString("hostname");
		String server = value.getString("server");
		
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		if(!config.contains(Arrays.asList("forced-hosts", hostname))) return WebinterfaceResponse.error("Hostname doesn't exist");
		
		List<String> servers = config.get(Arrays.asList("forced-hosts", hostname));
		if(!servers.contains(server)) return WebinterfaceResponse.error("Server already removed");
		
		servers.remove(server);
		config.set(Arrays.asList("forced-hosts", hostname), servers);
		config.save();
		config.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "moveHostnameServerUp")
	public WebinterfaceResponse moveHostnameServerUp(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String hostname = value.getString("hostname");
		String server = value.getString("server");
		
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		if(!config.contains(Arrays.asList("forced-hosts", hostname))) return WebinterfaceResponse.error("Hostname doesn't exist");
		
		List<String> servers = config.get(Arrays.asList("forced-hosts", hostname));
		if(!servers.contains(server)) return WebinterfaceResponse.error("Server not added");
		
		int idx = servers.indexOf(server);
		if(idx == 0) return WebinterfaceResponse.error("Server is already at the top");
		Collections.swap(servers, idx, idx - 1);
		config.set(Arrays.asList("forced-hosts", hostname), servers);
		config.save();
		config.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "moveHostnameServerDown")
	public WebinterfaceResponse moveHostnameServerDown(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String hostname = value.getString("hostname");
		String server = value.getString("server");
		
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		if(!config.contains(Arrays.asList("forced-hosts", hostname))) return WebinterfaceResponse.error("Hostname doesn't exist");
		
		List<String> servers = config.get(Arrays.asList("forced-hosts", hostname));
		if(!servers.contains(server)) return WebinterfaceResponse.error("Server not added");
		
		int idx = servers.indexOf(server);
		if(idx == servers.size() - 1) return WebinterfaceResponse.error("Server is already at the top");
		Collections.swap(servers, idx, idx + 1);
		config.set(Arrays.asList("forced-hosts", hostname), servers);
		config.save();
		config.close();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "addRestart")
	public WebinterfaceResponse addRestart(WebinterfaceRequestEvent event) {
		String cronString = event.getRequestData().getString("value");
		String normalizedCronString;
		try {
			normalizedCronString  = ScheduledRestart.CRON_PARSER.parse(cronString).validate().asString();
		}catch(IllegalArgumentException e) {
			return WebinterfaceResponse.error("Invalid cron format");
		}
		if(ScheduledRestart.getRestart(normalizedCronString) != null) return WebinterfaceResponse.error("Restart already exists");
		ScheduledRestart.addRestart(ScheduledRestart.fromCronString(normalizedCronString));
		ServerManager.saveRestarts();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "removeRestart")
	public WebinterfaceResponse removeRestart(WebinterfaceRequestEvent event) {
		String cronString = event.getRequestData().getString("value");
		ScheduledRestart r = ScheduledRestart.getRestart(cronString);
		if(r == null) return WebinterfaceResponse.error("Restart doesn't exist");
		ScheduledRestart.removeRestart(r);
		ServerManager.saveRestarts();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "addServerToRestart")
	public WebinterfaceResponse addServerToRestart(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String cronString = value.getString("restart");
		String server = value.getString("server");

		ScheduledRestart r = ScheduledRestart.getRestart(cronString);
		if(r == null) return WebinterfaceResponse.error("Restart doesn't exist");
		
		if(r.getServers().contains(server)) return WebinterfaceResponse.error("Server already added");
		
		if(ServerManager.getServer(server) == null) return WebinterfaceResponse.error("Invalid server");
		
		r.getServers().add(server);
		ServerManager.saveRestarts();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "removeServerFromRestart")
	public WebinterfaceResponse removeServerFromRestart(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String cronString = value.getString("restart");
		String server = value.getString("server");
		
		ScheduledRestart r = ScheduledRestart.getRestart(cronString);
		if(r == null) return WebinterfaceResponse.error("Restart doesn't exist");
		
		if(!r.getServers().contains(server)) return WebinterfaceResponse.error("Server already removed");
		
		r.getServers().remove(server);
		ServerManager.saveRestarts();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "moveRestartServerUp")
	public WebinterfaceResponse moveRestartServerUp(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String cronString = value.getString("restart");
		String server = value.getString("server");

		ScheduledRestart r = ScheduledRestart.getRestart(cronString);
		if(r == null) return WebinterfaceResponse.error("Restart doesn't exist");
		
		if(!r.getServers().contains(server)) return WebinterfaceResponse.error("Server not added");
		
		int idx = r.getServers().indexOf(server);
		if(idx == 0) return WebinterfaceResponse.error("Server is already at the top");
		Collections.swap(r.getServers(), idx, idx - 1);
		ServerManager.saveRestarts();
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "moveRestartServerDown")
	public WebinterfaceResponse moveRestartServerDown(WebinterfaceRequestEvent event) {
		JSONObject value = event.getRequestData().getJSONObject("value");
		String cronString = value.getString("restart");
		String server = value.getString("server");

		ScheduledRestart r = ScheduledRestart.getRestart(cronString);
		if(r == null) return WebinterfaceResponse.error("Restart doesn't exist");
		
		if(!r.getServers().contains(server)) return WebinterfaceResponse.error("Server not added");
		
		int idx = r.getServers().indexOf(server);
		if(idx == r.getServers().size() - 1) return WebinterfaceResponse.error("Server is already at the top");
		Collections.swap(r.getServers(), idx, idx + 1);
		ServerManager.saveRestarts();
		return WebinterfaceResponse.success();
	}

}
