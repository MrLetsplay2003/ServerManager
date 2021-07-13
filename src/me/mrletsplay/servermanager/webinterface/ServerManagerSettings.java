package me.mrletsplay.servermanager.webinterface;

import me.mrletsplay.webinterfaceapi.webinterface.config.setting.AutoSetting;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.AutoSettings;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.SettingsCategory;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.impl.IntSetting;
import me.mrletsplay.webinterfaceapi.webinterface.config.setting.impl.StringSetting;

public class ServerManagerSettings implements AutoSettings {
	
	public static ServerManagerSettings INSTANCE = new ServerManagerSettings();
	
	@AutoSetting
	private static SettingsCategory velocity = new SettingsCategory("Velocity");
	
	@AutoSetting
	private static SettingsCategory template = new SettingsCategory("Template");
	
	@AutoSetting
	private static SettingsCategory servers = new SettingsCategory("Servers");
	
	public static final StringSetting
		VELOCITY_BASE_PATH = velocity.addString("velocity.base-path", "/home/velocity/base", "Velocity Base Path", "Path for the Velocity proxy"),
		TEMPLATE_PATH = template.addString("template.path", "/home/velocity/template", "Server Template Path"),
		SERVERS_PATH = servers.addString("servers.path", "/home/velocity/servers", "Servers folder", "Folder in which new servers are created");
	
	public static final IntSetting
		SERVER_MIN_PORT = servers.addInt("servers.min-port", 25566, "Minimum Server Port"),
		SERVER_MAX_PORT = servers.addInt("servers.max-port", 26000, "Maximum Server Port");
	

}
