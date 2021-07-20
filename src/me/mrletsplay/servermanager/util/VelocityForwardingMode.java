package me.mrletsplay.servermanager.util;

import java.util.Arrays;

public enum VelocityForwardingMode {
	
	NONE("none", "None"),
	LEGACY("legacy", "Legacy (1.12-)"),
	// BUNGEEGUARD("bungeeguard", "BungeeGuard"), TODO: currently unsupported
	MODERN("modern", "Modern (Paper 1.13+)");
	
	private final String
		raw,
		friendlyName;

	private VelocityForwardingMode(String raw, String friendlyName) {
		this.raw = raw;
		this.friendlyName = friendlyName;
	}
	
	public String getRaw() {
		return raw;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public static VelocityForwardingMode getByRaw(String raw) {
		return Arrays.stream(values())
				.filter(m -> m.getRaw().equals(raw))
				.findFirst().orElse(null);
	}

}
