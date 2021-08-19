package me.mrletsplay.servermanager.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.process.JavaProcess;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.meta.MetadataHelper;
import me.mrletsplay.servermanager.server.meta.ServerMetadata;
import me.mrletsplay.servermanager.server.whitelist.ServerWhitelist;
import me.mrletsplay.servermanager.util.FileHelper;
import me.mrletsplay.servermanager.util.PaperVersion;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;

public class MinecraftServer {
	
	private static final List<String> AIKARS_FLAGS = Arrays.asList(
			"-XX:+UseG1GC",
			"-XX:+ParallelRefProcEnabled",
			"-XX:MaxGCPauseMillis=200",
			"-XX:+UnlockExperimentalVMOptions",
			"-XX:+DisableExplicitGC",
			"-XX:+AlwaysPreTouch",
			"-XX:G1NewSizePercent=30",
			"-XX:G1MaxNewSizePercent=40",
			"-XX:G1HeapRegionSize=8M",
			"-XX:G1ReservePercent=20",
			"-XX:G1HeapWastePercent=5",
			"-XX:G1MixedGCCountTarget=4",
			"-XX:InitiatingHeapOccupancyPercent=15",
			"-XX:G1MixedGCLiveThresholdPercent=90",
			"-XX:G1RSetUpdatingPauseTimePercent=5",
			"-XX:SurvivorRatio=32",
			"-XX:+PerfDisableSharedMem",
			"-XX:MaxTenuringThreshold=1",
			"-Dusing.aikars.flags=https://mcflags.emc.gs",
			"-Daikars.new.flags=true"
		);
	
	private File serverFolder;

	private ServerMetadata metadata;
	
	private int port = -1;
	
	private JavaProcess process;
	
	public MinecraftServer(File serverFolder, ServerMetadata metadata) {
		this.serverFolder = serverFolder;
		this.metadata = metadata;
	}

	public String getID() {
		return metadata.getID();
	}

	public String getName() {
		return metadata.getName();
	}
	
	public String getRawVersion() {
		return metadata.getVersion();
	}
	
	public PaperVersion getVersion() {
		return PaperVersion.getByVersion(getRawVersion());
	}
	
	public void saveMetadata() {
		MetadataHelper.saveMetadata(FileHelper.getMetadataFile(serverFolder), metadata);
	}
	
	public ServerMetadata getMetadata() {
		return metadata;
	}

	public File getServerFolder() {
		return serverFolder;
	}
	
	public int getPort() {
		if(port == -1) loadPort();
		return port;
	}
	
	private void loadPort() {
		this.port = Integer.parseInt(loadServerProperties().get("server-port"));
	}
	
	public ServerPropertiesFile loadServerProperties() {
		return new ServerPropertiesFile(FileHelper.getServerPropertiesFile(serverFolder));
	}
	
	public ServerWhitelist loadWhitelist() {
		return new ServerWhitelist(FileHelper.getWhitelistFile(serverFolder));
	}
	
	public Map<String, Object> loadPaperConfig() {
		try {
			Map<String, Object> map = new Yaml().load(new FileReader(FileHelper.getPaperConfigFile(serverFolder)));
			return map;
		} catch (FileNotFoundException e) {
			throw new FriendlyException("Failed to load paper config", e);
		}
	}
	
	public void savePaperConfig(Map<String, Object> map) {
		IOUtils.writeBytes(FileHelper.getPaperConfigFile(serverFolder), new Yaml().dumpAs(map, Tag.MAP, FlowStyle.BLOCK).getBytes(StandardCharsets.UTF_8));
	}
	
	public Map<String, Object> loadSpigotConfig() {
		try {
			Map<String, Object> map = new Yaml().load(new FileReader(FileHelper.getSpigotConfigFile(serverFolder)));
			return map;
		} catch (FileNotFoundException e) {
			throw new FriendlyException("Failed to load paper config", e);
		}
	}
	
	public void saveSpigotConfig(Map<String, Object> map) {
		IOUtils.writeBytes(FileHelper.getSpigotConfigFile(serverFolder), new Yaml().dumpAs(map, Tag.MAP, FlowStyle.BLOCK).getBytes(StandardCharsets.UTF_8));
	}
	
	@SuppressWarnings("unchecked")
	public void updateForwardingMode(VelocityForwardingMode mode) {
		if(getVersion().supportsModernForwarding()) {
			Map<String, Object> paper = loadPaperConfig();
			Map<String, Object> paperSettings = (Map<String, Object>) paper.get("settings");
			Map<String, Object> velocitySupport = (Map<String, Object>) paperSettings.get("velocity-support");
			velocitySupport.put("enabled", mode == VelocityForwardingMode.MODERN);
			savePaperConfig(paper);
		}
		
		Map<String, Object> spigot = loadSpigotConfig();
		Map<String, Object> spigotSettings = (Map<String, Object>) spigot.get("settings");
		spigotSettings.put("bungeecord", mode == VelocityForwardingMode.LEGACY);
		saveSpigotConfig(spigot);
	}
	
	public JavaVersion getJavaVersion() {
		return JavaVersion.getJavaVersion(metadata.getJavaVersion());
	}
	
	public boolean hasJavaVersion() {
		return getJavaVersion() != null;
	}
	
	public List<String> loadLatestLog() {
		try {
			File latestLogFile = new File(serverFolder, "logs/latest.log");
			if(!latestLogFile.exists()) return Collections.emptyList();
			return Files.readAllLines(latestLogFile.toPath());
		} catch (IOException e) {
			throw new FriendlyException("Failed to load latest log");
		}
	}
	
	public List<String> getLogFiles() {
		File logsFolder = new File(serverFolder, "logs");
		if(!logsFolder.exists()) return Collections.emptyList();
		return Arrays.asList(logsFolder.list((dir, n) -> n.endsWith(".log") || n.endsWith(".log.gz")));
	}
	
	public List<String> loadLog(String logName) {
		try {
			File logFile = new File(serverFolder, "logs/" + logName);
			if(!logFile.exists()) return Collections.emptyList();
			if(!logFile.toPath().normalize().startsWith(serverFolder.toPath())) return Collections.emptyList();
			if(logFile.getName().endsWith(".log")) {
				return Files.readAllLines(logFile.toPath());
			}else if(logFile.getName().endsWith(".log.gz")) {
				try(BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile))))) {
					return r.lines().collect(Collectors.toList());
				}
			}else {
				return Collections.emptyList();
			}
		} catch (IOException e) {
			throw new FriendlyException("Failed to load latest log");
		}
	}
	
	public void start() {
		if(isRunning()) return;
		JavaVersion j = getJavaVersion();
		if(j == null) return;
		process = JavaProcess.startProcess(j, FileHelper.getServerJarFile(serverFolder), serverFolder, metadata.getMemoryLimitMiB(), AIKARS_FLAGS, "nogui");
		process.setOnStopped(() -> {
			process = null;
		});
	}
	
	public void stop() {
		if(!isRunning()) return;
		process.sendLine("stop");
		process.waitForOrKill(120, TimeUnit.SECONDS);
		process = null;
	}
	
	public boolean isRunning() {
		return process != null;
	}
	
	public JavaProcess getProcess() {
		return process;
	}
	
	public void showTitle(String title, String subtitle) {
		if(!isRunning()) throw new FriendlyException("Server not running");
		if(getVersion().ordinal() <= PaperVersion.PAPER_1_12_2.ordinal()) {
			getProcess().sendLine("title @a title \"" + title.replace("\"", "\\\"") + "\"");
			if(subtitle != null) getProcess().sendLine("title @a subtitle \"" + subtitle.replace("\"", "\\\"") + "\"");
		}else {
			JSONObject o = new JSONObject();
			o.put("text", title);
			getProcess().sendLine("title @a title " + o.toString());
			
			if(subtitle != null) {
				JSONObject o2 = new JSONObject();
				o2.put("text", subtitle);
				getProcess().sendLine("title @a subtitle " + o2.toString());
			}
		}
	}
	
}
