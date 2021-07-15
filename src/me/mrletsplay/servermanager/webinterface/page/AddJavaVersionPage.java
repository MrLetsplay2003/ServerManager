package me.mrletsplay.servermanager.webinterface.page;

import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class AddJavaVersionPage extends WebinterfacePage {
	
	public AddJavaVersionPage() {
		super("Create Server", "/sm/add-java-version", true);
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addLayoutOptions(new GridLayout("1fr"));
		s.addTitle("Add a Java version");
		
		WebinterfaceInputField id = new WebinterfaceInputField("ID");
		s.addElement(id);
		
		WebinterfaceInputField name = new WebinterfaceInputField("Name");
		s.addElement(name);
		
		WebinterfaceInputField javaPath = new WebinterfaceInputField("Java Path");
		s.addElement(javaPath);
		
		WebinterfaceButton b = new WebinterfaceButton("Add");
		ObjectValue v = new ObjectValue();
		v.put("id", new ElementValue(id));
		v.put("name", new ElementValue(name));
		v.put("javaPath", new ElementValue(javaPath));
		b.setOnClickAction(new SendJSAction("server-manager", "addJavaVersion", v)
				.onSuccess(new RedirectAction("/sm/java-versions")));
		s.addElement(b);
		
		addSection(s);
	}

}
