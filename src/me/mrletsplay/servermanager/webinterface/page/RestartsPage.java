package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.util.ScheduledRestart;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ConfirmAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceElementGroup;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class RestartsPage extends WebinterfacePage {
	
	public RestartsPage() {
		super("Scheduled Restarts", "/sm/restarts");
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection sc = new WebinterfacePageSection();
		sc.addTitle("Restarts");
		
		WebinterfaceElementGroup grpC = new WebinterfaceElementGroup();
		grpC.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		
		WebinterfaceInputField cronTime = new WebinterfaceInputField("Time in UNIX cron format (see crontab.guru)");
		cronTime.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
		grpC.addElement(cronTime);
		
		WebinterfaceButton create = new WebinterfaceButton("Create");
		create.setOnClickAction(new SendJSAction("server-manager", "addRestart", new ElementValue(cronTime)).onSuccess(new ReloadPageAction()));
		grpC.addElement(create);
		
		sc.addElement(grpC);
		
		addSection(sc);
		
		addDynamicSections(() -> {
			List<WebinterfacePageSection> scs = new ArrayList<>();
			
			for(ScheduledRestart restart : ScheduledRestart.getRestarts()) {
				String cronString = restart.getCron().asString();
				
				List<String> servers = restart.getServers();
				WebinterfacePageSection s = new WebinterfacePageSection();
				
				s.addTitle(ScheduledRestart.CRON_DESCRIPTOR.describe(restart.getCron()) + " (" + cronString + ")");
				
				WebinterfaceElementGroup grpI = new WebinterfaceElementGroup();
				grpI.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("min-content", "auto", "min-content", "min-content", "min-content"));
				
				WebinterfaceTitleText tt = new WebinterfaceTitleText("Servers");
				tt.addLayoutOptions(DefaultLayoutOption.LEFTBOUND, DefaultLayoutOption.CENTER_VERTICALLY, DefaultLayoutOption.FULL_WIDTH);
				grpI.addElement(tt);
				
				if(servers.isEmpty()) {
					grpI.addElement(WebinterfaceText.builder()
							.text("(none)")
							.fullWidth()
							.leftbound()
							.create());
				}
				
				for(String server : servers) {
					MinecraftServer sr = ServerManager.getServer(server);
					String name = sr == null ? "(Invalid Server)" : sr.getName();
					grpI.addElement(WebinterfaceText.builder()
						.text(name)
						.noLineBreaks()
						.leftbound()
						.create());
					
					grpI.addElement(new WebinterfaceVerticalSpacer("0px"));
					
					ObjectValue v = new ObjectValue();
					v.put("restart", new StringValue(cronString));
					v.put("server", new StringValue(server));
					
					WebinterfaceButton up = new WebinterfaceButton("Up");
					up.setOnClickAction(new SendJSAction("server-manager", "moveRestartServerUp", v).onSuccess(new ReloadPageAction()));
					grpI.addElement(up);
					
					WebinterfaceButton down = new WebinterfaceButton("Down");
					down.setOnClickAction(new SendJSAction("server-manager", "moveRestartServerDown", v).onSuccess(new ReloadPageAction()));
					grpI.addElement(down);
					
					WebinterfaceButton remove = new WebinterfaceButton("X");
					remove.setOnClickAction(new SendJSAction("server-manager", "removeServerFromRestart", v).onSuccess(new ReloadPageAction()));
					grpI.addElement(remove);
				}
				
				s.addElement(grpI);
				
				WebinterfaceElementGroup grpH = new WebinterfaceElementGroup();
				grpH.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				
				WebinterfaceSelect addS = new WebinterfaceSelect();
				addS.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
				for(MinecraftServer server : ServerManager.getServers()) {
					if(!servers.contains(server.getID())) addS.addOption(server.getName(), server.getID());
				}
				grpH.addElement(addS);
				
				WebinterfaceButton addB = new WebinterfaceButton("Add");
				ObjectValue v = new ObjectValue();
				v.put("restart", new StringValue(cronString));
				v.put("server", new ElementValue(addS));
				addB.setOnClickAction(new SendJSAction("server-manager", "addServerToRestart", v).onSuccess(new ReloadPageAction()));
				grpH.addElement(addB);
				
				grpH.addElement(new WebinterfaceVerticalSpacer("30px"));
				
				WebinterfaceButton delete = new WebinterfaceButton("Delete");
				delete.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				delete.setOnClickAction(new ConfirmAction(new SendJSAction("server-manager", "removeRestart", new StringValue(cronString)).onSuccess(new ReloadPageAction())));
				grpH.addElement(delete);
				
				s.addElement(grpH);
				
				scs.add(s);
			}
			
			return scs;
		});
	}

}
