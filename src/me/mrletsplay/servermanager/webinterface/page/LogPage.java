package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.webinterface.LogElement;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;

public class LogPage extends WebinterfacePage {
	
	public LogPage() {
		super("Log", "/sm/log", true);
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			String logID = ctx.getClientHeader().getPath().getQueryParameterValue("log");
			
			String[] spl = logID.split(":", 2);
			String serverID = spl[0];
			String log = spl[1];
			
			MinecraftServer server = ServerManager.getServer(serverID);
			if(server == null) return els;
			
			if(!server.getLogFiles().contains(log)) return els;
			
			List<String> logLines = server.loadLog(log);
			
			LogElement c = new LogElement();
			c.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
			c.setID("console-area");
			
			c.setText(logLines.stream().collect(Collectors.joining("\n")));
			
			els.add(c);
			return els;
		});
		
		addSection(s);
	}

}
