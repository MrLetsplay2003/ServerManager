package me.mrletsplay.servermanager.server.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.misc.FriendlyException;

public class MetadataHelper {
	
	public static ServerMetadata loadMetadata(File metaFile) {
		try (FileInputStream fIn = new FileInputStream(metaFile)){
			return JSONConverter.decodeObject(new JSONObject(new String(IOUtils.readAllBytes(fIn), StandardCharsets.UTF_8)), ServerMetadata.class);
		} catch (IOException e) {
			throw new FriendlyException("Failed to load server metadata", e);
		}
	}
	
	public static void saveMetadata(File metaFile, ServerMetadata metadata) {
		IOUtils.createFile(metaFile);
		IOUtils.writeBytes(metaFile, metadata.toJSON(false).toFancyString().getBytes(StandardCharsets.UTF_8));
	}

}
