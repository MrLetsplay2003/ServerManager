package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.servermanager.util.PaperVersion;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.LoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.CheckboxValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceCheckBox;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceElementGroup;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

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
			v3.put("javaVersion", new ElementValue(jVer));
			updJ.setOnClickAction(new SendJSAction("server-manager", "updateServerJavaVersion", v3).onSuccess(new ReloadPageAction()));
			els.add(updJ);
			
			WebinterfaceElementGroup chbGrp = new WebinterfaceElementGroup();
			chbGrp.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("50px", "auto"));
			
			WebinterfaceCheckBox autostart = new WebinterfaceCheckBox(server.getMetadata().isAutostart());
			ObjectValue v4 = new ObjectValue();
			v4.put("server", new StringValue(serverID));
			v4.put("autostart", new CheckboxValue(autostart));
			autostart.setOnChangeAction(new SendJSAction("server-manager", "updateServerAutostart", v4).onError(new ReloadPageAction()));
			chbGrp.addElement(autostart);
			
			chbGrp.addElement(WebinterfaceText.builder()
					.text("Autostart")
					.leftboundText()
					.create());
			
			els.add(chbGrp);
			
			WebinterfaceInputField whl = new WebinterfaceInputField("Username");
			whl.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			els.add(whl);
			
			WebinterfaceButton addWhl = new WebinterfaceButton("Add to Whitelist");
			ObjectValue v5 = new ObjectValue();
			v5.put("server", new StringValue(serverID));
			v5.put("user", new ElementValue(whl));
			addWhl.setOnClickAction(new SendJSAction("server-manager", "addToServerWhitelist", v5));
			els.add(addWhl);
			
			els.add(new WebinterfaceVerticalSpacer("30px"));
			
			els.add(WebinterfaceTitleText.builder()
					.text("Danger Zone")
					.fullWidth()
					.leftboundText()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text("Update server version")
					.fullWidth()
					.leftboundText()
					.create());
			
			PaperVersion serverVersion = server.getVersion();

			boolean modernOnly = VelocityBase.getForwardingMode() == VelocityForwardingMode.MODERN;
			WebinterfaceSelect ver = new WebinterfaceSelect();
			ver.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			for(PaperVersion v : PaperVersion.getVersions()) {
				if(modernOnly && !v.supportsModernForwarding()) continue;
				ver.addOption("Paper " + v.getVersion(), v.name(), v == serverVersion);
			}
			if(modernOnly) ver.addOption("Enable legacy forwarding for more options", null, false, false);
			els.add(ver);
			
			WebinterfaceButton upd = new WebinterfaceButton("Update");
			ObjectValue v = new ObjectValue();
			v.put("server", new StringValue(serverID));
			v.put("version", new ElementValue(ver));
			upd.setOnClickAction(MultiAction.of(LoadingScreenAction.show(), new SendJSAction("server-manager", "updateServerVersion", v).onSuccess(new ReloadPageAction()).onError(LoadingScreenAction.hide())));
			els.add(upd);
			
//			els.add(WebinterfaceText.builder()
//					.text("Update to any Paper version (not recommended)")
//					.fullWidth()
//					.leftboundText()
//					.create());
//
//			WebinterfaceSelect ver2 = new WebinterfaceSelect();
//			ver2.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
//			for(String version : PaperAPI.getPaperVersions()) {
//				ver2.addOption("Paper " + version, version);
//			}
//			els.add(ver2);
//			
//			WebinterfaceButton upd2 = new WebinterfaceButton("Update");
//			ObjectValue val = new ObjectValue();
//			val.put("server", new StringValue(serverID));
//			val.put("version", new ElementValue(ver2));
//			upd2.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "updateAnyServerVersion", val).onSuccess(new ReloadPageAction()).onError(new HideLoadingScreenAction())));
//			els.add(upd2);
			
			return els;
		});
		addSection(s);
	}

}
