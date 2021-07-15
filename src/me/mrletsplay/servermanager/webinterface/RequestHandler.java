package me.mrletsplay.servermanager.webinterface;

import java.io.File;
import java.io.IOException;
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
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "createServer")
	public WebinterfaceResponse createServer(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String id = v.getString("id");
		String name = v.getString("name");
		String version = v.getString("version");
		String jVersion = v.getString("javaVersion");
		if(id.isBlank() || name.isBlank()) return WebinterfaceResponse.error("Both id and name must be set");
		if(!ID_PATTERN.matcher(id).matches()) return WebinterfaceResponse.error("ID must match " + ID_PATTERN.pattern());
		JavaVersion javaVersion = JavaVersion.getJavaVersion(jVersion);
		if(javaVersion == null) return WebinterfaceResponse.error("Invalid Java version");
		SetupHelper.createNewServer(false, id, name, version, javaVersion);
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
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		
		boolean running = s.isRunning();
		if(running) s.stop();
		
		File paperJar = new File(s.getServerFolder(), "paper.jar");
		String latestPaperURL = PaperAPI.getLatestBuildURL(version);
		try {
			new HttpGet(latestPaperURL).execute().transferTo(paperJar);
		} catch (IOException e) {
			throw new FriendlyException("Failed to download Paper", e);
		}
		
		s.getMetadata().setVersion(version);
		s.saveMetadata();
		
		if(running) s.start();
		
		return WebinterfaceResponse.success();
	}
	
	@WebinterfaceHandler(requestTarget = "server-manager", requestTypes = "updateServerJavaVersion")
	public WebinterfaceResponse updateServerJavaVersion(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String server = v.getString("server");
		String version = v.getString("javaVersion");
		MinecraftServer s = ServerManager.getServer(server);
		if(s == null) return WebinterfaceResponse.error("Invalid server");
		s.getMetadata().setJavaVersion(version);
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
		ServerManager.getServers().remove(s);
		CommentedFileConfig c = VelocityBase.loadVelocityConfig();
		c.remove("servers." + server);
		c.save();
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

}
