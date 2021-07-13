package me.mrletsplay.servermanager.webinterface.page;

import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
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
		
		WebinterfaceInputField id = new WebinterfaceInputField("ID");
		s.addElement(id);
		
		WebinterfaceInputField name = new WebinterfaceInputField("Name");
		s.addElement(name);
		
		WebinterfaceSelect version = new WebinterfaceSelect();
		for(String v : PaperAPI.getPaperVersions()) {
			version.addOption("Paper " + v, v);
		}
		s.addElement(version);
		
		WebinterfaceButton b = new WebinterfaceButton("Install");
		ObjectValue v = new ObjectValue();
		v.put("id", new ElementValue(id));
		v.put("name", new ElementValue(name));
		v.put("version", new ElementValue(version));
		b.setOnClickAction(new SendJSAction("server-manager", "createServer", v).onSuccess(new RedirectAction("/")));
		s.addElement(b);
		
		addSection(s);
	}

}
