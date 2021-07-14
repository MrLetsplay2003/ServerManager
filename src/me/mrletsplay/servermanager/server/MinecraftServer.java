package me.mrletsplay.servermanager.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.process.JavaProcess;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.meta.ServerMetadata;

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
	
	public String getVersion() {
		return metadata.getVersion();
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
		return new ServerPropertiesFile(new File(serverFolder, "server.properties"));
	}
	
	public Map<String, Object> loadPaperConfig() {
		try {
			Map<String, Object> map = new Yaml().load(new FileReader(new File(serverFolder, "paper.yml")));
			return map;
		} catch (FileNotFoundException e) {
			throw new FriendlyException("Failed to load paper config", e);
		}
	}
	
	public void savePaperConfig(Map<String, Object> map) {
		IOUtils.writeBytes(new File(serverFolder, "paper.yml"), new Yaml().dumpAs(map, Tag.MAP, FlowStyle.BLOCK).getBytes(StandardCharsets.UTF_8));
	}
	
	public void start() {
		if(isRunning()) return;
		process = JavaProcess.startProcess(JavaVersion.SYSTEM, new File(serverFolder, "paper.jar"), serverFolder, 2048, AIKARS_FLAGS, "nogui");
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
	
}
