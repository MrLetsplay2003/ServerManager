package me.mrletsplay.servermanager.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
	
	private static final Map<Integer, String> MESSAGE_TIMES = new LinkedHashMap<>();
	
	static {
		MESSAGE_TIMES.put(600, "in 10 minutes");
		MESSAGE_TIMES.put(300, "in 5 minutes");
		MESSAGE_TIMES.put(60, "in 1 minute");
		MESSAGE_TIMES.put(30, "in 30 seconds");
		MESSAGE_TIMES.put(10, "in 10 seconds");
		MESSAGE_TIMES.put(9, "in 9 seconds");
		MESSAGE_TIMES.put(8, "in 8 seconds");
		MESSAGE_TIMES.put(7, "in 7 seconds");
		MESSAGE_TIMES.put(6, "in 6 seconds");
		MESSAGE_TIMES.put(5, "in 5 seconds");
		MESSAGE_TIMES.put(4, "in 4 seconds");
		MESSAGE_TIMES.put(3, "in 3 seconds");
		MESSAGE_TIMES.put(2, "in 2 seconds");
		MESSAGE_TIMES.put(1, "in 1 second");
	}
	
	private Cron cron;
	private List<String> servers;
	private boolean announce;
	private ZonedDateTime nextExecution;
	
	public ScheduledRestart(Cron cron, List<String> servers, boolean announce) {
		this.cron = cron;
		this.servers = servers;
		this.announce = announce;
		this.nextExecution = ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now()).get();
		if(announce) {
			this.nextExecution = this.nextExecution.minus(10, ChronoUnit.MINUTES);
		}
	}
	
	public Cron getCron() {
		return cron;
	}
	
	public List<String> getServers() {
		return servers;
	}
	
	public boolean isAnnounce() {
		return announce;
	}
	
	public ZonedDateTime getNextExecution() {
		return nextExecution;
	}
	
	public void run() {
		ZonedDateTime now = ZonedDateTime.now();
		if(announce) {
			now = now.plusMinutes(10);
		}
		
		nextExecution = ExecutionTime.forCron(cron).nextExecution(now).get();
		if(announce) {
			nextExecution = nextExecution.minus(10, ChronoUnit.MINUTES);
		}
		
		List<MinecraftServer> ss = servers.stream()
				.map(ServerManager::getServer)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		if(announce) {
			List<Map.Entry<Integer, String>> msgs = new ArrayList<>(MESSAGE_TIMES.entrySet());
			Map.Entry<Integer, String> msg = msgs.remove(0);
			while(true) {
				int t = msg.getKey();
				for(MinecraftServer server : ss) {
					if(server.isRunning()) server.showTitle("§cRestart", msg.getValue());
				}
				if(msgs.isEmpty()) {
					try {
						Thread.sleep(t * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}
				msg = msgs.remove(0);
				try {
					Thread.sleep((t - msg.getKey()) * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		for(MinecraftServer server : ss) {
			if(server.isRunning()) {
				server.stop();
				server.start();
			}
		}
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("cron", cron.asString());
		o.put("servers", new JSONArray(servers));
		o.put("announce", announce);
		return o;
	}

	public static ScheduledRestart fromCron(Cron cron, boolean announce) {
		return new ScheduledRestart(cron, new ArrayList<>(), announce);
	}
	
	public static ScheduledRestart fromJSON(JSONObject o) {
		try {
			Cron c = CRON_PARSER.parse(o.getString("cron")).validate();
			boolean announce = o.getBoolean("announce");
			return new ScheduledRestart(c, new ArrayList<>(o.getJSONArray("servers").stream()
					.map(s -> (String) s)
					.collect(Collectors.toList())),
					announce);
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
	
	public static void clearRestarts() {
		SCHEDULED_RESTARTS.clear();
	}
	
}
