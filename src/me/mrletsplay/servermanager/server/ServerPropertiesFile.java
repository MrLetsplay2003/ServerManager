package me.mrletsplay.servermanager.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class ServerPropertiesFile {
	
	private File file;
	private Map<String, String> values;
	
	public ServerPropertiesFile(File file) {
		this.file = file;
		this.values = new LinkedHashMap<>();
		try {
			List<String> lines = Files.readAllLines(file.toPath());
			for(String line : lines) {
				if(line.trim().startsWith("#")) continue;
				String[] spl = line.trim().split("=", 2);
				values.put(spl[0], spl[1]);
			}
		} catch (IOException e) {
			throw new FriendlyException("Failed to load server properties", e);
		}
	}
	
	public ServerPropertiesFile set(String key, String value) {
		values.put(key, value);
		return this;
	}
	
	public String get(String key) {
		return values.get(key);
	}
	
	public void save() {
		try(BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
			for(Map.Entry<String, String> entry : values.entrySet()) {
				w.write(entry.getKey() + "=" + entry.getValue());
				w.newLine();
			}
		}catch(IOException e) {
			throw new FriendlyException("Failed to save server properties", e);
		}
	}

}
