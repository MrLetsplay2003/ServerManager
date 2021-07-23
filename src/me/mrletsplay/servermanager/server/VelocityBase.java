package me.mrletsplay.servermanager.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.process.JavaProcess;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;
import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;

public class VelocityBase {
	
	private static JavaProcess velocityProcess;
	
	public static File getFolder() {
		return new File(Webinterface.getConfig().getSetting(ServerManagerSettings.VELOCITY_BASE_PATH));
	}
	
	public static File getVelocityJarFile() {
		return new File(getFolder(), "velocity.jar");
	}
	
	public static File getVelocityConfigFile() {
		return new File(getFolder(), "velocity.toml");
	}
	
	public static CommentedFileConfig loadVelocityConfig() {
		CommentedFileConfig config = CommentedFileConfig
				.builder(VelocityBase.getVelocityConfigFile())
				.onFileNotFound(FileNotFoundAction.THROW_ERROR)
				.build();
		config.load();
		return config;
	}
	
	public static VelocityForwardingMode getForwardingMode() {
		CommentedFileConfig c = loadVelocityConfig();
		return VelocityForwardingMode.getByRaw(c.get("player-info-forwarding-mode"));
	}
	
	public static void setForwardingMode(VelocityForwardingMode mode) {
		CommentedFileConfig c = loadVelocityConfig();
		c.set("player-info-forwarding-mode", mode.getRaw());
		c.save();
		c.close();
	}
	
	private static JSONObject loadVelocitySettings() {
		try {
			File f = new File(Webinterface.getConfigurationDirectory(), "velocity.json");
			if(!f.exists()) return new JSONObject();
			return new JSONObject(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new FriendlyException("Failed to load Java versions", e);
		}
	}
	
	private static void saveVelocitySettings(JSONObject settings) {
		IOUtils.writeBytes(new File(Webinterface.getConfigurationDirectory(), "velocity.json"), settings.toFancyString().getBytes(StandardCharsets.UTF_8));
	}
	
	public static boolean isAutostart() {
		return loadVelocitySettings().optBoolean("autostart").orElse(false);
	}
	
	public static void setAutostart(boolean autostart) {
		JSONObject s = loadVelocitySettings();
		s.put("autostart", autostart);
		saveVelocitySettings(s);
	}
	
	public static boolean isInstalled() {
		return getVelocityJarFile().exists();
	}
	
	public static void start() {
		if(isRunning()) throw new FriendlyException("Velocity is already running");
		velocityProcess = JavaProcess.startProcess(JavaVersion.SYSTEM, getVelocityJarFile(), getFolder(), 512, null);
		velocityProcess.setOnStopped(() -> {
			velocityProcess = null;
		});
	}
	
	public static JavaProcess getVelocityProcess() {
		return velocityProcess;
	}
	 
	public static void stop() {
		if(!isRunning()) return;
		velocityProcess.sendLine("shutdown");
		velocityProcess.waitForOrKill(30, TimeUnit.SECONDS);
		velocityProcess = null;
	}
	
	public static boolean isRunning() {
		return velocityProcess != null;
	}

}
