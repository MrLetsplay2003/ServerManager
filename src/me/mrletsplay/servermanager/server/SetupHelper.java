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
import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.process.JavaProcess;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.meta.MetadataHelper;
import me.mrletsplay.servermanager.server.meta.ServerMetadata;
import me.mrletsplay.servermanager.util.FileHelper;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.servermanager.util.PaperVersion;
import me.mrletsplay.servermanager.util.SetupException;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;
import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;

public class SetupHelper {
	
	public static void installVelocity(String version, int port) throws SetupException {
		if(VelocityBase.isInstalled()) return;

		File velocityFolder = VelocityBase.getFolder();
		if(velocityFolder.exists() && (!velocityFolder.isDirectory() || velocityFolder.list().length > 0)) throw new SetupException("Velocity base folder is not empty");
		
		downloadVelocity(version);
		
		File velocityJar = VelocityBase.getVelocityJarFile();
		
		// Start velocity process and immediately shut it down again to create configuration files
		JavaProcess velocityProcess = JavaProcess.startProcess(JavaVersion.SYSTEM, velocityJar, velocityFolder, 512, null);
		velocityProcess.sendLine("shutdown");
		try {
			velocityProcess.getProcess().waitFor();
		} catch (InterruptedException e) {
			throw new SetupException(e);
		}
		
		if(!VelocityBase.getVelocityConfigFile().exists()) {
			throw new SetupException("Can't find Velocity config file (Did it crash?)");
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
	
	public static void downloadVelocity(String version) throws SetupException {
		File velocityJar = VelocityBase.getVelocityJarFile();
		IOUtils.createFile(velocityJar);
		
		try {
			String velLink = PaperAPI.getVelocityBuildURL(version);
			if(velLink == null) throw new SetupException("Invalid Velocity version");
			new HttpGet(velLink).execute().transferTo(velocityJar);
		} catch (IOException e) {
			throw new SetupException("Failed to download Velocity", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static MinecraftServer createNewServer(boolean useTemplate, String id, String name, PaperVersion version, JavaVersion javaVersion) throws SetupException {
		List<Integer> ports = ServerManager.getServers().stream()
				.map(s -> s.getPort())
				.collect(Collectors.toList());
		
		int port = IntStream.range(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVER_MIN_PORT), Webinterface.getConfig().getSetting(ServerManagerSettings.SERVER_MAX_PORT) + 1)
			.filter(i -> !ports.contains(i))
			.findFirst().orElse(-1);
		
		if(port == -1) throw new SetupException("No free port");
		
		File serverFolder = new File(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVERS_PATH), id);
		serverFolder.mkdirs();
		
		if(useTemplate) {
			File templateFolder = new File(Webinterface.getConfig().getSetting(ServerManagerSettings.TEMPLATE_PATH));
			if(templateFolder.exists() && templateFolder.isDirectory()) {
				// TODO: copy files from template...
			}
		}
		
		IOUtils.writeBytes(FileHelper.getEULAFile(serverFolder), "eula=true".getBytes(StandardCharsets.UTF_8));
		
		File paperJar = FileHelper.getServerJarFile(serverFolder);
		String latestPaperURL = PaperAPI.getLatestBuildURL(version.getVersion());
		try {
			new HttpGet(latestPaperURL).execute().transferTo(paperJar);
		} catch (IOException e) {
			throw new SetupException("Failed to download Paper", e);
		}
		
		// Start velocity process and immediately shut it down again to create configuration files
		JavaProcess paperProcess = JavaProcess.startProcess(javaVersion, paperJar, serverFolder, 1024, null, "nogui");
		paperProcess.sendLine("stop");
		try {
			paperProcess.getProcess().waitFor();
		} catch (InterruptedException e) {
			throw new SetupException(e);
		}
		
		CommentedFileConfig velocityConfig = VelocityBase.loadVelocityConfig();
		
		String forwardingSecret = velocityConfig.get("forwarding-secret");
		
		velocityConfig.set("servers." + id, "127.0.0.1:" + port);
		velocityConfig.save();
		velocityConfig.close();
		
		ServerMetadata m = new ServerMetadata(id, name, version.getVersion(), javaVersion);
		MinecraftServer server = new MinecraftServer(serverFolder, m);
		
		if(!FileHelper.getServerPropertiesFile(serverFolder).exists() || !FileHelper.getPaperConfigFile(serverFolder).exists()) {
			throw new SetupException("Failed to find server.properties or paper.yml (Did the server crash during setup?)");
		}
		
		server.loadServerProperties()
			.set("server-port", String.valueOf(port))
			.set("server-ip", "127.0.0.1")
			.set("online-mode", "false")
			.save();
		
		MetadataHelper.saveMetadata(new File(serverFolder, "server-manager.json"), m);
		
		if(version.supportsModernForwarding()) {
			Map<String, Object> paper = server.loadPaperConfig();
			Map<String, Object> settings = (Map<String, Object>) paper.get("settings");
			Map<String, Object> velocitySupport = (Map<String, Object>) settings.get("velocity-support");
			
			velocitySupport.put("enabled", VelocityBase.getForwardingMode() == VelocityForwardingMode.MODERN); // Only enable if using modern forwarding
			velocitySupport.put("secret", forwardingSecret);
			velocitySupport.put("online-mode", true);
			server.savePaperConfig(paper);
		}
		
		ServerManager.addServer(server);
		return server;
	}
	
}
