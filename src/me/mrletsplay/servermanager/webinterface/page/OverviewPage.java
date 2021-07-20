package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ConfirmAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceElementGroup;
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
		s.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("1fr", "1fr"));
		s.addTitle("Velocity");
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			if(!VelocityBase.isInstalled()) {
				WebinterfaceButton setupV = new WebinterfaceButton("Setup Velocity Base");
				setupV.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				setupV.setOnClickAction(new RedirectAction("/sm/setup-velocity"));
				els.add(setupV);
			}else {
				
				WebinterfaceElementGroup infoGroup = new WebinterfaceElementGroup();
				infoGroup.addLayoutOptions(new GridLayout("min-content", "auto"));
				
				infoGroup.addElement(new WebinterfaceTitleText("Running"));
				
				WebinterfaceText onOff = new WebinterfaceText(VelocityBase.isRunning() ? "Yes" : "No");
				onOff.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(onOff);
				
				els.add(infoGroup);
				
				WebinterfaceElementGroup btnGroup = new WebinterfaceElementGroup();
				btnGroup.addLayoutOptions(new GridLayout("1fr", "1fr"));
				
				WebinterfaceButton console = new WebinterfaceButton("Open Console");
				console.setOnClickAction(new RedirectAction("/sm/console?server=base"));
				btnGroup.addElement(console);
				
				WebinterfaceButton velocitySettings = new WebinterfaceButton("Settings");
				velocitySettings.setOnClickAction(new RedirectAction("/sm/velocity-settings"));
				btnGroup.addElement(velocitySettings);
				
				WebinterfaceButton start = new WebinterfaceButton("Start Velocity");
				start.setOnClickAction(new SendJSAction("server-manager", "startVelocity", null).onSuccess(new ReloadPageAction()));
				btnGroup.addElement(start);
				
				WebinterfaceButton shutdown = new WebinterfaceButton("Shutdown");
				shutdown.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "stopVelocity", null).onSuccess(new ReloadPageAction())));
				btnGroup.addElement(shutdown);
				
				WebinterfaceButton createServer = new WebinterfaceButton("Create Server");
				createServer.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				createServer.setOnClickAction(new RedirectAction("/sm/create"));
				btnGroup.addElement(createServer);
				
				els.add(btnGroup);
			}
			
			return els;
		});
		
		addSection(s);
		
		addDynamicSections(() -> {
			List<WebinterfacePageSection> ss = new ArrayList<>();
			
			for(MinecraftServer server : ServerManager.getServers()) {
				WebinterfacePageSection sc = new WebinterfacePageSection();
				sc.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("1fr", "1fr"));
				sc.addTitle(server.getName() + " (" + server.getID() + ")");
				
				WebinterfaceElementGroup infoGroup = new WebinterfaceElementGroup();
				infoGroup.addLayoutOptions(new GridLayout("min-content", "auto"));
				
				WebinterfaceTitleText onOffT = new WebinterfaceTitleText("Running");
				onOffT.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(onOffT);
				
				WebinterfaceText onOff = new WebinterfaceText(server.isRunning() ? "Yes" : "No");
				onOff.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(onOff);
				
				WebinterfaceTitleText verT = new WebinterfaceTitleText("Version");
				verT.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(verT);
				
				WebinterfaceText ver = new WebinterfaceText(server.getVersion());
				ver.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(ver);
				
				WebinterfaceTitleText memT = new WebinterfaceTitleText("Memory Limit");
				memT.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				memT.getStyle().setProperty("white-space", "nowrap");
				infoGroup.addElement(memT);
				
				WebinterfaceText mem = new WebinterfaceText(server.getMetadata().getMemoryLimitMiB() + " MiB");
				mem.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(mem);
				
				WebinterfaceTitleText portT = new WebinterfaceTitleText("Port");
				portT.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(portT);
				
				WebinterfaceText port = new WebinterfaceText(String.valueOf(server.getPort()));
				port.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				infoGroup.addElement(port);

				WebinterfaceTitleText jVerT = new WebinterfaceTitleText("Java Version");
				jVerT.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				jVerT.getStyle().setProperty("white-space", "nowrap");
				infoGroup.addElement(jVerT);
				
				JavaVersion j = server.getJavaVersion();
				infoGroup.addElement(WebinterfaceText.builder()
						.text(j == null ? "[MISSING]" : j.getName())
						.leftbound()
						.noLineBreaks()
						.create());
				
				sc.addElement(infoGroup);
				
				WebinterfaceElementGroup btnGroup = new WebinterfaceElementGroup();
				btnGroup.addLayoutOptions(new GridLayout("1fr", "1fr"));
				
				WebinterfaceButton console = new WebinterfaceButton("Open Console");
				console.setOnClickAction(new RedirectAction("/sm/console?server=" + server.getID()));
				btnGroup.addElement(console);
				
				WebinterfaceButton serverSettings = new WebinterfaceButton("Settings");
				serverSettings.setOnClickAction(new RedirectAction("/sm/server-settings?server=" + server.getID()));
				btnGroup.addElement(serverSettings);
				
				WebinterfaceButton start = new WebinterfaceButton("Start Server");
				start.setOnClickAction(new SendJSAction("server-manager", "startServer", new StringValue(server.getID())).onSuccess(new ReloadPageAction()));
				btnGroup.addElement(start);
				
				WebinterfaceButton shutdown = new WebinterfaceButton("Shutdown");
				shutdown.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "stopServer", new StringValue(server.getID())).onSuccess(new ReloadPageAction())));
				btnGroup.addElement(shutdown);
				
				btnGroup.addElement(new WebinterfaceVerticalSpacer("30px"));
				
				WebinterfaceButton delete = new WebinterfaceButton("Delete");
				delete.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				delete.setOnClickAction(new ConfirmAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "deleteServer", new StringValue(server.getID())).onSuccess(new ReloadPageAction()))));
				btnGroup.addElement(delete);
				
				sc.addElement(btnGroup);
				
				ss.add(sc);
			}
			
			return ss;
		});
	}

}
