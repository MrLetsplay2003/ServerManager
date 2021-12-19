package me.mrletsplay.servermanager.webinterface.page;

import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class SetupVelocityPage extends WebinterfacePage {
	
	public SetupVelocityPage() {
		super("Setup Velocity", "/sm/setup-velocity", true);
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addLayoutOptions(new GridLayout("1fr"));
		
		WebinterfaceInputField port = new WebinterfaceInputField("Port");
		s.addElement(port);

		WebinterfaceSelect ver = new WebinterfaceSelect();
		ver.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
		for(String version : PaperAPI.getVelocityVersions()) {
			ver.addOption("Velocity " + version, version);
		}
		s.addElement(ver);
		
		WebinterfaceButton b = new WebinterfaceButton("Install");
		ObjectValue v = new ObjectValue();
		v.put("port", new ElementValue(port));
		b.setOnClickAction(new SendJSAction("server-manager", "installVelocity", v).onSuccess(new RedirectAction("/")));
		s.addElement(b);
		
		addSection(s);
	}
	
	@Override
	public void createContent() {
		if(VelocityBase.isInstalled()) {
			HttpRequestContext c = HttpRequestContext.getCurrentContext();
			c.getServerHeader().setStatusCode(HttpStatusCodes.FOUND_302);
			c.getServerHeader().getFields().setFieldValue("Location", "/");
			return;
		}
		
		super.createContent();
	}
	
}
