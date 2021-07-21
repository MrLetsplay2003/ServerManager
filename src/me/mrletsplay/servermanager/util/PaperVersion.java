package me.mrletsplay.servermanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PaperVersion {
	
	PAPER_1_8_8("1.8.8"),
	PAPER_1_9_4("1.9.4"),
	PAPER_1_10_2("1.10.2"),
	PAPER_1_11_2("1.11.2"),
	PAPER_1_12_2("1.12.2"),
	PAPER_1_13_2("1.13.2"),
	PAPER_1_14_4("1.14.4"),
	PAPER_1_15_2("1.15.2"),
	PAPER_1_16_5("1.16.5"),
	PAPER_1_17_1("1.17.1"),
	;
	
	private final String version;

	private PaperVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public boolean supportsModernForwarding() {
		return ordinal() >= PAPER_1_13_2.ordinal();
	}
	
	public static PaperVersion getByVersion(String version) {
		return Arrays.stream(values())
				.filter(v -> v.getVersion().equals(version))
				.findFirst().orElse(null);
	}
	
	public static List<PaperVersion> getVersions() {
		List<PaperVersion> vs = new ArrayList<>(Arrays.asList(values()));
		Collections.reverse(vs);
		return vs;
	}

}
