package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;

public class ServerSettingsPage extends WebinterfacePage {
	
	public ServerSettingsPage() {
		super("Server Settings", "/sm/server-settings", true);
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addTitle("Server settings");
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			String serverID = ctx.getClientHeader().getPath().getQueryParameterValue("server");
			if(serverID == null) return els;
			
			MinecraftServer server = ServerManager.getServer(serverID);
			if(server == null) return els;
			
			String serverVersion = server.getVersion();
			
			WebinterfaceSelect ver = new WebinterfaceSelect();
			ver.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			for(String v : PaperAPI.getPaperVersions()) {
				ver.addOption("Paper " + v, v, v.equals(serverVersion));
			}
			els.add(ver);
			
			WebinterfaceButton upd = new WebinterfaceButton("Update");
			ObjectValue v = new ObjectValue();
			v.put("server", new StringValue(serverID));
			v.put("version", new ElementValue(ver));
			upd.setOnClickAction(new SendJSAction("server-manager", "updateServerVersion", v).onSuccess(new ReloadPageAction()));
			els.add(upd);
			
			WebinterfaceInputField mem = new WebinterfaceInputField();
			ObjectValue v2 = new ObjectValue();
			v2.put("server", new StringValue(serverID));
			v2.put("memory", new ElementValue(mem));
			mem.setOnChangeAction(new SendJSAction("server-manager", "updateServerMemory", v2).onSuccess(new ReloadPageAction()));
			els.add(mem);
			
			return els;
		});
		addSection(s);
	}

}
