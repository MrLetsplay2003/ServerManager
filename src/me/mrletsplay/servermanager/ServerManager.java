package me.mrletsplay.servermanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.meta.MetadataHelper;
import me.mrletsplay.servermanager.server.meta.ServerMetadata;
import me.mrletsplay.servermanager.webinterface.RequestHandler;
import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.servermanager.webinterface.page.ConsolePage;
import me.mrletsplay.servermanager.webinterface.page.CreateServerPage;
import me.mrletsplay.servermanager.webinterface.page.JavaVersionsPage;
import me.mrletsplay.servermanager.webinterface.page.OverviewPage;
import me.mrletsplay.servermanager.webinterface.page.ServerSettingsPage;
import me.mrletsplay.servermanager.webinterface.page.SettingsPage;
import me.mrletsplay.servermanager.webinterface.page.SetupVelocityPage;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.config.DefaultSettings;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageCategory;

public class ServerManager {
	
	private static List<MinecraftServer> servers = new ArrayList<>();
	
	public static void main(String[] args) {
		DefaultSettings.HOME_PAGE_PATH.setDefaultValue("/sm/overview");
		
		Webinterface.start();
		Webinterface.getConfig().registerSettings(ServerManagerSettings.INSTANCE);
		Webinterface.registerActionHandler(new RequestHandler());
		
		WebinterfacePageCategory generalCategory = Webinterface.createCategory("Server Manager");
		generalCategory.addPage(new OverviewPage());
		generalCategory.addPage(new JavaVersionsPage());
		generalCategory.addPage(new SettingsPage());
		generalCategory.addPage(new CreateServerPage());
		generalCategory.addPage(new SetupVelocityPage());
		generalCategory.addPage(new ConsolePage());
		generalCategory.addPage(new ServerSettingsPage());
		
		loadServers();
	}
	
	private static void loadServers() {
		File serversFolder = new File(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVERS_PATH));
		if(!serversFolder.exists() || !serversFolder.isDirectory()) return;
		for(File s : serversFolder.listFiles()) {
			if(!s.isDirectory()) continue;
			File metaFile = new File(s, "server-manager.json");
			if(!metaFile.exists()) continue;
			ServerMetadata d = MetadataHelper.loadMetadata(metaFile);
			servers.add(new MinecraftServer(s, d));
		}
	}
	
	public static void addServer(MinecraftServer server) {
		servers.add(server);
	}
	
	public static MinecraftServer getServer(String serverID) {
		return servers.stream()
				.filter(s -> s.getID().equals(serverID))
				.findFirst().orElse(null);
	}
	
	public static List<MinecraftServer> getServers() {
		return servers;
	}

}
