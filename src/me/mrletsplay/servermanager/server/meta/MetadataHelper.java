package me.mrletsplay.servermanager.server.meta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class MetadataHelper {
	
	public static ServerMetadata loadMetadata(File metaFile) {
		try {
			return JSONConverter.decodeObject(new JSONObject(Files.readString(metaFile.toPath())), ServerMetadata.class);
		} catch (IOException e) {
			throw new FriendlyException("Failed to load server metadata", e);
		}
	}
	
	public static void saveMetadata(File metaFile, ServerMetadata metadata) {
		try {
			Files.writeString(metaFile.toPath(), metadata.toJSON(false).toFancyString(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new FriendlyException("Failed to save server metadata", e);
		}
	}

}
