package me.mrletsplay.servermanager.util;

import java.io.File;

import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;

public class FileHelper {
	
	public static File getServerFolder(String serverID) {
		return new File(Webinterface.getConfig().getSetting(ServerManagerSettings.SERVERS_PATH), serverID);
	}
	
	public static File getMetadataFile(File serverFolder) {
		return new File(serverFolder, "server-manager.json");
	}
	
	public static File getPaperConfigFile(File serverFolder) {
		return new File(serverFolder, "paper.yml");
	}
	
	public static File getSpigotConfigFile(File serverFolder) {
		return new File(serverFolder, "spigot.yml");
	}
	
	public static File getServerPropertiesFile(File serverFolder) {
		return new File(serverFolder, "server.properties");
	}
	
	public static File getServerJarFile(File serverFolder) {
		return new File(serverFolder, "paper.jar");
	}
	
	public static File getEULAFile(File serverFolder) {
		return new File(serverFolder, "eula.txt");
	}

}
