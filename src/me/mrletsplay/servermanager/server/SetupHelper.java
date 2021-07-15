package me.mrletsplay.servermanager.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import me.mrletsplay.mrcore.http.HttpGet;
import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.process.JavaProcess;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.meta.MetadataHelper;
import me.mrletsplay.servermanager.server.meta.ServerMetadata;
import me.mrletsplay.servermanager.util.FileHelper;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;

public class SetupHelper {
	
	private static final String
		LATEST_VELOCITY_URL = "https://versions.velocitypowered.com/download/latest";
	
	public static void installVelocity(int port) {
		if(VelocityBase.isInstalled()) return;
		
		File velocityFolder = VelocityBase.getFolder();
		if(velocityFolder.exists() && (!velocityFolder.isDirectory() || velocityFolder.list().length > 0)) throw new FriendlyException("Velocity base folder is not empty");
		
		File velocityJar = VelocityBase.getVelocityJarFile();
		IOUtils.createFile(velocityJar);
		
		try {
			new HttpGet(LATEST_VELOCITY_URL).execute().transferTo(velocityJar);
		} catch (IOException e) {
			throw new FriendlyException("Failed to download Velocity", e);
		}
		
		// Start velocity process and immediately shut it down again to create configuration files
		JavaProcess velocityProcess = JavaProcess.startProcess(JavaVersion.SYSTEM, velocityJar, velocityFolder, 512, null);
		velocityProcess.sendLine("shutdown");
		try {
			velocityProcess.getProcess().waitFor();
		} catch (InterruptedException e) {
			throw new FriendlyException(e);
		}
		
		CommentedFileConfig config = VelocityBase.loadVelocityConfig();
		
		// Apply settings
		config.set("bind", "0.0.0.0:" + port);
		config.set("player-info-forwarding-mode", "modern");
		
		// Clear servers and forced hosts
		CommentedConfig vs = config.get("servers");
		vs.clear();
		CommentedConfig fh = config.get("forced-hosts");
		fh.clear();
		
		config.save();
		config.close();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static MinecraftServer createNewServer(boolean useTemplate, String id, String name, String version, JavaVersion javaVersion) {
		List<Integer> ports = ServerManager.getServers().stream()
				.map(s -> s.getPort())
				.collect(Collectors.toList());
		
		int port = IntStream.range(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVER_MIN_PORT), Webinterface.getConfig().getSetting(ServerManagerSettings.SERVER_MAX_PORT) + 1)
			.filter(i -> !ports.contains(i))
			.findFirst().orElse(-1);
		
		if(port == -1) throw new FriendlyException("No free port");
		
		File serverFolder = new File(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVERS_PATH), id);
		serverFolder.mkdirs();
		
		if(useTemplate) {
			File templateFolder = new File(Webinterface.getConfig().getSetting(ServerManagerSettings.TEMPLATE_PATH));
			if(templateFolder.exists() && templateFolder.isDirectory()) {
				// copy files from template...
			}
		}
		
		IOUtils.writeBytes(FileHelper.getEULAFile(serverFolder), "eula=true".getBytes(StandardCharsets.UTF_8));
		
		File paperJar = FileHelper.getServerJarFile(serverFolder);
		String latestPaperURL = PaperAPI.getLatestBuildURL(version);
		try {
			new HttpGet(latestPaperURL).execute().transferTo(paperJar);
		} catch (IOException e) {
			throw new FriendlyException("Failed to download Paper", e);
		}
		
		// Start velocity process and immediately shut it down again to create configuration files
		JavaProcess paperProcess = JavaProcess.startProcess(javaVersion, paperJar, serverFolder, 1024, null, "nogui");
		paperProcess.sendLine("stop");
		try {
			paperProcess.getProcess().waitFor();
		} catch (InterruptedException e) {
			throw new FriendlyException(e);
		}
		
		CommentedFileConfig velocityConfig = VelocityBase.loadVelocityConfig();
		
		String forwardingSecret = velocityConfig.get("forwarding-secret");
		
		velocityConfig.set("servers." + id, "127.0.0.1:" + port);
		velocityConfig.save();
		velocityConfig.close();
		
		ServerMetadata m = new ServerMetadata(id, name, version, javaVersion);
		MinecraftServer server = new MinecraftServer(serverFolder, m);
		server.loadServerProperties()
			.set("server-port", String.valueOf(port))
			.set("server-ip", "127.0.0.1")
			.set("online-mode", "false")
			.save();
		
		MetadataHelper.saveMetadata(new File(serverFolder, "server-manager.json"), m);
		
		Map<String, Object> paper = server.loadPaperConfig();
		Map<String, Object> settings = (Map<String, Object>) paper.get("settings");
		Map<String, Object> velocitySupport = (Map<String, Object>) settings.get("velocity-support");
		// TODO: doesn't work for MC < 1.12/1.13
		velocitySupport.put("enabled", true);
		velocitySupport.put("secret", forwardingSecret);
		velocitySupport.put("online-mode", true);
		server.savePaperConfig(paper);
		
		ServerManager.addServer(server);
		return server;
	}
	
}
