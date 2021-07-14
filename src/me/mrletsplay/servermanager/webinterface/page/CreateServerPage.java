package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.HideLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class CreateServerPage extends WebinterfacePage {
	
	public CreateServerPage() {
		super("Create Server", "/sm/create", true);
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addLayoutOptions(new GridLayout("1fr"));
		s.addTitle("Create a new server");
		
		WebinterfaceInputField id = new WebinterfaceInputField("ID");
		s.addElement(id);
		
		WebinterfaceInputField name = new WebinterfaceInputField("Name");
		s.addElement(name);
		
		WebinterfaceSelect version = new WebinterfaceSelect();
		List<String> versions = new ArrayList<>(PaperAPI.getPaperVersions());
		Collections.reverse(versions);
		for(String v : versions) {
			version.addOption("Paper " + v, v);
		}
		s.addElement(version);
		
		WebinterfaceSelect javaVersion = new WebinterfaceSelect();
		for(JavaVersion v : JavaVersion.getJavaVersions()) {
			javaVersion.addOption(v.getName(), v.getID());
		}
		s.addElement(javaVersion);
		
		WebinterfaceButton b = new WebinterfaceButton("Install");
		ObjectValue v = new ObjectValue();
		v.put("id", new ElementValue(id));
		v.put("name", new ElementValue(name));
		v.put("version", new ElementValue(version));
		v.put("javaVersion", new ElementValue(javaVersion));
		b.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "createServer", v)
				.onSuccess(new RedirectAction("/"))
				.onError(new HideLoadingScreenAction())));
		s.addElement(b);
		
		addSection(s);
	}

}
