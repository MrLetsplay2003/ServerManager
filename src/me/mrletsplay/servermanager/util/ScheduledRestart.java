package me.mrletsplay.servermanager.util;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.server.MinecraftServer;

public class ScheduledRestart {
	
	public static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
	public static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);
	public static final CronDescriptor CRON_DESCRIPTOR = CronDescriptor.instance(Locale.US);
	
	private static final List<ScheduledRestart> SCHEDULED_RESTARTS = new ArrayList<>();
	
	private Cron cron;
	private List<String> servers;
	private ZonedDateTime nextExecution;
	
	public ScheduledRestart(Cron cron, List<String> servers) {
		this.cron = cron;
		this.servers = servers;
		this.nextExecution = ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now()).get();
	}
	
	public Cron getCron() {
		return cron;
	}
	
	public List<String> getServers() {
		return servers;
	}
	
	public ZonedDateTime getNextExecution() {
		return nextExecution;
	}
	
	public void run() {
		ZonedDateTime now = ZonedDateTime.now();
		
		for(String s : servers) {
			MinecraftServer server = ServerManager.getServer(s);
			if(server == null) continue;
			if(server.isRunning()) {
				server.stop();
				server.start();
			}
		}
		
		nextExecution = ExecutionTime.forCron(cron).nextExecution(now).get();
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("cron", cron.asString());
		o.put("servers", new JSONArray(servers));
		return o;
	}

	public static ScheduledRestart fromCronString(String cronString) {
		return new ScheduledRestart(CRON_PARSER.parse(cronString), new ArrayList<>());
	}
	
	public static ScheduledRestart fromJSON(JSONObject o) {
		try {
			Cron c = CRON_PARSER.parse(o.getString("cron")).validate();
			return new ScheduledRestart(c, new ArrayList<>(o.getJSONArray("servers").stream()
					.map(s -> (String) s)
					.collect(Collectors.toList())));
		}catch(IllegalArgumentException e) {
			throw new FriendlyException("Failed to load scheduled restarts: Invalid cron format: " + o.getString("cron"), e);
		}
	}
	
	public static List<ScheduledRestart> getRestarts() {
		return SCHEDULED_RESTARTS;
	}
	
	public static void addRestart(ScheduledRestart restart) {
		SCHEDULED_RESTARTS.add(restart);
	}
	
	public static ScheduledRestart getRestart(String cronString) {
		return SCHEDULED_RESTARTS.stream()
				.filter(r -> r.getCron().asString().equals(cronString))
				.findFirst().orElse(null);
	}
	
	public static void removeRestart(ScheduledRestart restart) {
		SCHEDULED_RESTARTS.remove(restart);
	}
	
}
