package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.servermanager.util.PaperVersion;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.LoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class CreateServerPage extends WebinterfacePage {
	
	public CreateServerPage() {
		super("Create Server", "/sm/create", true);
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addLayoutOptions(new GridLayout("1fr"));
		s.addTitle("Create a new server");
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			WebinterfaceInputField id = new WebinterfaceInputField("ID");
			els.add(id);
			
			WebinterfaceInputField name = new WebinterfaceInputField("Name");
			els.add(name);
			
			boolean modernOnly = VelocityBase.getForwardingMode() == VelocityForwardingMode.MODERN;
			WebinterfaceSelect version = new WebinterfaceSelect();
			for(PaperVersion v : PaperVersion.getVersions()) {
				if(modernOnly && !v.supportsModernForwarding()) continue;
				version.addOption("Paper " + v.getVersion(), v.name());
			}
			if(modernOnly) version.addOption("Enable legacy forwarding for more options", null, false, false);
			els.add(version);
			
			WebinterfaceSelect javaVersion = new WebinterfaceSelect();
			for(JavaVersion v : JavaVersion.getJavaVersions()) {
				javaVersion.addOption(v.getName(), v.getID());
			}
			els.add(javaVersion);
			
			WebinterfaceButton b = new WebinterfaceButton("Install");
			ObjectValue v = new ObjectValue();
			v.put("id", new ElementValue(id));
			v.put("name", new ElementValue(name));
			v.put("version", new ElementValue(version));
			v.put("javaVersion", new ElementValue(javaVersion));
			b.setOnClickAction(MultiAction.of(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "createServer", v)
					.onSuccess(new RedirectAction("/"))
					.onError(LoadingScreenAction.hide())));
			els.add(b);
			
			return els;
		});
		
		addSection(s);
	}

}
