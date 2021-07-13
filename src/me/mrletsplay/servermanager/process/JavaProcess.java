package me.mrletsplay.servermanager.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class JavaProcess {
	
	private File jarFile;
	private Process process;
	private BufferedReader reader;
	private List<String> logHistory;
	private Runnable onStopped;
	
	private JavaProcess(File jarFile, Process process) {
		this.jarFile = jarFile;
		this.process = process;
		this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		this.logHistory = new ArrayList<>();
		
		new Thread(() -> {
			try {
				String line;
				while(this.process != null && this.process.isAlive() && (line = reader.readLine()) != null) {
					synchronized(logHistory) {
						logHistory.add(line);
						if(logHistory.size() > 500) logHistory.remove(0);
					}
				}
				
				try {
					if(process != null) process.waitFor(5, TimeUnit.SECONDS); // Wait for process to exit
				} catch (InterruptedException e) {
					this.process = null;
					if(onStopped != null) onStopped.run();
					return;
				}
				
				if(this.process != null && !this.process.isAlive()) {
					this.process = null;
					if(onStopped != null) onStopped.run();
				}
			}catch(IOException e) {
				throw new FriendlyException(e);
			}
		}).start();
	}
	
	public File getJarFile() {
		return jarFile;
	}
	
	public Process getProcess() {
		return process;
	}
	
	public BufferedReader getReader() {
		return reader;
	}
	
	public List<String> getLogHistory() {
		return new ArrayList<>(logHistory);
	}
	
	public void send(String command) {
		try {
			process.getOutputStream().write(command.getBytes());
			process.getOutputStream().flush();
		} catch (IOException e) {
			throw new FriendlyException("Failed to send command", e);
		}
	}
	
	public void sendLine(String command) {
		synchronized (logHistory) {
			logHistory.add("> " + command);
		}
		send(command + System.lineSeparator());
	}
	
	public void setOnStopped(Runnable onStopped) {
		this.onStopped = onStopped;
	}
	
	public void waitForOrKill(long timeout, TimeUnit unit) {
		try {
			process.waitFor(timeout, unit);
		} catch (InterruptedException e) {
			throw new FriendlyException(e);
		}
		
		if(process.isAlive()) process.destroyForcibly();
		process = null;
		if(onStopped != null) onStopped.run();
	}
	
	public static JavaProcess startProcess(File jarFile, File workingDir, int memoryLimitMB, List<String> javaOptions, String... arguments) {
		List<String> command = new ArrayList<>();
		command.add("java");
		
		if(javaOptions != null) command.addAll(javaOptions);
		command.add("-Xmx" + memoryLimitMB + "M");
		command.add("-jar");
		command.add(jarFile.getAbsolutePath());
		command.addAll(Arrays.asList(arguments));
		
		try {
			Process p = new ProcessBuilder(command)
					.directory(workingDir)
					.start();
			return new JavaProcess(jarFile, p);
		} catch (IOException e) {
			throw new FriendlyException(e);
		}
	}
	
}
