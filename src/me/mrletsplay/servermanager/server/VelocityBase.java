package me.mrletsplay.servermanager.server;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;

import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.process.JavaProcess;
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
	
	public static boolean isInstalled() {
		return getVelocityJarFile().exists();
	}
	
	public static void start() {
		if(isRunning()) throw new FriendlyException("Velocity is already running");
		velocityProcess = JavaProcess.startProcess(getVelocityJarFile(), getFolder(), 512, null);
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