package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.HideLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowLoadingScreenAction;
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
			
			WebinterfaceButton upd = new WebinterfaceButton("Update Paper Version");
			ObjectValue v = new ObjectValue();
			v.put("server", new StringValue(serverID));
			v.put("version", new ElementValue(ver));
			upd.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "updateServerVersion", v).onSuccess(new ReloadPageAction()).onError(new HideLoadingScreenAction())));
			els.add(upd);
			
			WebinterfaceInputField mem = new WebinterfaceInputField(String.valueOf(server.getMetadata().getMemoryLimitMiB()));
			mem.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			els.add(mem);
			
			WebinterfaceButton updMem = new WebinterfaceButton("Update Memory");
			ObjectValue v2 = new ObjectValue();
			v2.put("server", new StringValue(serverID));
			v2.put("memory", new ElementValue(mem));
			updMem.setOnClickAction(new SendJSAction("server-manager", "updateServerMemory", v2).onSuccess(new ReloadPageAction()));
			els.add(updMem);
			
			String serverJavaVersion = server.getMetadata().getJavaVersion();
			
			WebinterfaceSelect jVer = new WebinterfaceSelect();
			jVer.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			for(JavaVersion jv : JavaVersion.getJavaVersions()) {
				jVer.addOption(jv.getName(), jv.getID(), jv.getID().equals(serverJavaVersion));
			}
			els.add(jVer);
			
			WebinterfaceButton updJ = new WebinterfaceButton("Update Java Version");
			ObjectValue v3 = new ObjectValue();
			v3.put("server", new StringValue(serverID));
			v3.put("javaVersion", new ElementValue(ver));
			updJ.setOnClickAction(new SendJSAction("server-manager", "updateServerJavaVersion", v3).onSuccess(new ReloadPageAction()));
			els.add(updJ);
			
			return els;
		});
		addSection(s);
	}

}
