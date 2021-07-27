package me.mrletsplay.servermanager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.servermanager.server.meta.MetadataHelper;
import me.mrletsplay.servermanager.server.meta.ServerMetadata;
import me.mrletsplay.servermanager.util.FileHelper;
import me.mrletsplay.servermanager.util.ScheduledRestart;
import me.mrletsplay.servermanager.webinterface.RequestHandler;
import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.servermanager.webinterface.page.AddJavaVersionPage;
import me.mrletsplay.servermanager.webinterface.page.ConsolePage;
import me.mrletsplay.servermanager.webinterface.page.CreateServerPage;
import me.mrletsplay.servermanager.webinterface.page.HostnamesPage;
import me.mrletsplay.servermanager.webinterface.page.JavaVersionsPage;
import me.mrletsplay.servermanager.webinterface.page.LogPage;
import me.mrletsplay.servermanager.webinterface.page.OverviewPage;
import me.mrletsplay.servermanager.webinterface.page.RestartsPage;
import me.mrletsplay.servermanager.webinterface.page.ServerSettingsPage;
import me.mrletsplay.servermanager.webinterface.page.SettingsPage;
import me.mrletsplay.servermanager.webinterface.page.SetupVelocityPage;
import me.mrletsplay.servermanager.webinterface.page.ShutdownPage;
import me.mrletsplay.servermanager.webinterface.page.VelocitySettingsPage;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.config.DefaultSettings;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageCategory;

public class ServerManager {
	
	private static List<MinecraftServer> servers = new ArrayList<>();
	private static ScheduledExecutorService executorService;
	
	public static void main(String[] args) {
		DefaultSettings.HOME_PAGE_PATH.setDefaultValue("/sm/overview");
		
		Webinterface.start();
		Webinterface.getConfig().registerSettings(ServerManagerSettings.INSTANCE);
		Webinterface.registerActionHandler(new RequestHandler());
		
		WebinterfacePageCategory generalCategory = Webinterface.createCategory("Server Manager");
		generalCategory.addPage(new OverviewPage());
		generalCategory.addPage(new JavaVersionsPage());
		generalCategory.addPage(new HostnamesPage());
		generalCategory.addPage(new RestartsPage());
		generalCategory.addPage(new SettingsPage());
		generalCategory.addPage(new ShutdownPage());
		
		// Invisible pages
		generalCategory.addPage(new CreateServerPage());
		generalCategory.addPage(new SetupVelocityPage());
		generalCategory.addPage(new ConsolePage());
		generalCategory.addPage(new LogPage());
		generalCategory.addPage(new ServerSettingsPage());
		generalCategory.addPage(new VelocitySettingsPage());
		generalCategory.addPage(new AddJavaVersionPage());
		
		executorService = Executors.newScheduledThreadPool(5);
		
		loadAndStart();
		
		executorService.scheduleAtFixedRate(() -> {
			try {
				for(ScheduledRestart r : ScheduledRestart.getRestarts()) {
					if(ZonedDateTime.now().isAfter(r.getNextExecution())) {
						Webinterface.getLogger().info("Running scheduled restart for: " + r.getServers().stream().collect(Collectors.joining(", ")));
						executorService.execute(() -> r.run());
						Webinterface.getLogger().info("Scheduled restart finished");
					}
				}
			}catch(Exception e) {
				Webinterface.getLogger().error("Failed to run scheduled restarts", e);
			}
		}, 1, 1, TimeUnit.MINUTES);
	}
	
	private static void loadAndStart() {
		loadJavaVersions();
		loadServers();
		loadRestarts();
		
		if(VelocityBase.isAutostart()) VelocityBase.start();
		
		for(MinecraftServer server : servers) {
			if(server.getMetadata().isAutostart()) server.start();
		}
	}
	
	private static void loadJavaVersions() {
		try {
			File f = new File(Webinterface.getConfigurationDirectory(), "java-versions.json");
			if(!f.exists()) return;
			new JSONArray(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)).stream()
				.map(o -> JSONConverter.decodeObject((JSONObject) o, JavaVersion.class))
				.forEach(JavaVersion::addJavaVersion);
		} catch (IOException e) {
			throw new FriendlyException("Failed to load Java versions", e);
		}
	}
	
	public static void saveJavaVersions() {
		IOUtils.writeBytes(new File(Webinterface.getConfigurationDirectory(), "java-versions.json"), JavaVersion.getJavaVersions().stream()
				.filter(v -> !v.isSystemDefault())
				.map(v -> v.toJSON(false))
				.collect(Collectors.toCollection(JSONArray::new)).toFancyString().getBytes(StandardCharsets.UTF_8));
	}
	
	private static void loadRestarts() {
		try {
			File f = new File(Webinterface.getConfigurationDirectory(), "restarts.json");
			if(!f.exists()) return;
			new JSONArray(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)).stream()
				.map(o -> ScheduledRestart.fromJSON((JSONObject) o))
				.forEach(ScheduledRestart::addRestart);
		} catch (IOException e) {
			throw new FriendlyException("Failed to load scheduled restarts", e);
		}
	}
	
	public static void saveRestarts() {
		IOUtils.writeBytes(new File(Webinterface.getConfigurationDirectory(), "restarts.json"), ScheduledRestart.getRestarts().stream()
				.map(v -> v.toJSON())
				.collect(Collectors.toCollection(JSONArray::new)).toFancyString().getBytes(StandardCharsets.UTF_8));
	}
	
	private static void loadServers() {
		File serversFolder = new File(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVERS_PATH));
		if(!serversFolder.exists() || !serversFolder.isDirectory()) return;
		for(File s : serversFolder.listFiles()) {
			if(!s.isDirectory()) continue;
			File metaFile = FileHelper.getMetadataFile(s);
			if(!metaFile.exists()) continue;
			ServerMetadata d = MetadataHelper.loadMetadata(metaFile);
			if(!d.isValid()) continue;
			servers.add(new MinecraftServer(s, d));
		}
		
		servers.sort(Comparator.comparing(m -> m.getID()));
	}
	
	public static void addServer(MinecraftServer server) {
		servers.add(server);
		servers.sort(Comparator.comparing(m -> m.getID()));
	}
	
	public static void removeServer(MinecraftServer server) {
		servers.remove(server);
	}
	
	public static MinecraftServer getServer(String serverID) {
		return servers.stream()
				.filter(s -> s.getID().equals(serverID))
				.findFirst().orElse(null);
	}
	
	public static List<MinecraftServer> getServers() {
		return servers;
	}
	
	public static void fullReload() {
		if(VelocityBase.isRunning()) VelocityBase.stop();
		for(MinecraftServer s : servers) {
			if(s.isRunning()) s.stop();
		}

		ScheduledRestart.clearRestarts();
		servers.clear();
		JavaVersion.clearJavaVersions();
		
		loadAndStart();
	}

}
