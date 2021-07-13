package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class OverviewPage extends WebinterfacePage {
	
	public OverviewPage() {
		super("Overview", "/sm/overview");
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("min-content", "auto"));
		s.addTitle("Velocity");
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			if(!VelocityBase.isInstalled()) {
				WebinterfaceButton setupV = new WebinterfaceButton("Setup Velocity Base");
				setupV.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				setupV.setOnClickAction(new RedirectAction("/sm/setup-velocity"));
				els.add(setupV);
			}else {
				els.add(new WebinterfaceTitleText("Running"));
				
				WebinterfaceText onOff = new WebinterfaceText(VelocityBase.isRunning() ? "Yes" : "No");
				onOff.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				els.add(onOff);
				
				WebinterfaceButton console = new WebinterfaceButton("Open Console");
				console.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				console.setOnClickAction(new RedirectAction("/sm/console?server=base"));
				els.add(console);
				
				WebinterfaceButton start = new WebinterfaceButton("Start Velocity");
				start.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				start.setOnClickAction(new SendJSAction("server-manager", "startVelocity", null).onSuccess(new ReloadPageAction()));
				els.add(start);
				
				WebinterfaceButton shutdown = new WebinterfaceButton("Shutdown");
				shutdown.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				shutdown.setOnClickAction(new SendJSAction("server-manager", "stopVelocity", null).onSuccess(new ReloadPageAction()));
				els.add(shutdown);
			}
			
			return els;
		});
		
		WebinterfaceButton createServer = new WebinterfaceButton("Create Server");
		createServer.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		createServer.setOnClickAction(new RedirectAction("/sm/create"));
		s.addElement(createServer);
		
		addSection(s);
		
		addDynamicSections(() -> {
			List<WebinterfacePageSection> ss = new ArrayList<>();
			
			for(MinecraftServer server : ServerManager.getServers()) {
				WebinterfacePageSection sc = new WebinterfacePageSection();
				sc.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("min-content", "auto"));
				sc.addTitle(server.getName() + " (" + server.getID() + ")");
				
				sc.addElement(new WebinterfaceTitleText("Running"));
				
				WebinterfaceText onOff = new WebinterfaceText(server.isRunning() ? "Yes" : "No");
				onOff.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				sc.addElement(onOff);
				
				sc.addElement(new WebinterfaceTitleText("Version"));
				
				WebinterfaceText ver = new WebinterfaceText(server.getVersion());
				ver.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				sc.addElement(ver);
				
				WebinterfaceTitleText memT = new WebinterfaceTitleText("Memory Limit");
				memT.getStyle().setProperty("white-space", "nowrap");
				sc.addElement(memT);
				
				WebinterfaceText mem = new WebinterfaceText(server.getMetadata().getMemoryLimitMB() + " MB");
				mem.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				sc.addElement(mem);
				
				sc.addElement(new WebinterfaceTitleText("Port"));
				
				WebinterfaceText port = new WebinterfaceText(String.valueOf(server.getPort()));
				port.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				sc.addElement(port);
				
				sc.addElement(new WebinterfaceVerticalSpacer("30px"));
				
				WebinterfaceButton console = new WebinterfaceButton("Open Console");
				console.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				console.setOnClickAction(new RedirectAction("/sm/console?server=" + server.getID()));
				sc.addElement(console);
				
				WebinterfaceButton start = new WebinterfaceButton("Start Server");
				start.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				start.setOnClickAction(new SendJSAction("server-manager", "startServer", new StringValue(server.getID())).onSuccess(new ReloadPageAction()));
				sc.addElement(start);
				
				WebinterfaceButton shutdown = new WebinterfaceButton("Shutdown");
				shutdown.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				shutdown.setOnClickAction(new SendJSAction("server-manager", "stopServer", new StringValue(server.getID())).onSuccess(new ReloadPageAction()));
				sc.addElement(shutdown);
				
				ss.add(sc);
			}
			
			return ss;
		});
	}

}
