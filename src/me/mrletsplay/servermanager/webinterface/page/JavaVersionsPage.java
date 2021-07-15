package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.process.JavaVersion;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ConfirmAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class JavaVersionsPage extends WebinterfacePage {
	
	public JavaVersionsPage() {
		super("Java Versions", "/sm/java-versions");
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("min-content", "auto"));
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			for(JavaVersion version : JavaVersion.getJavaVersions()) {
				WebinterfaceTitleText h = new WebinterfaceTitleText(version.getName() + " (" + version.getID() + ")");
				h.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, DefaultLayoutOption.CENTER_VERTICALLY, DefaultLayoutOption.LEFTBOUND);
				els.add(h);
				
				WebinterfaceTitleText tt = new WebinterfaceTitleText("Java Path");
				tt.getStyle().setProperty("white-space", "nowrap");
				els.add(tt);
				
				WebinterfaceText onOff = new WebinterfaceText(version.getJavaPath());
				onOff.addLayoutOptions(DefaultLayoutOption.LEFTBOUND);
				els.add(onOff);
				
				if(!version.isSystemDefault()) {
					WebinterfaceButton delete = new WebinterfaceButton("Remove");
					delete.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
					delete.setOnClickAction(new ConfirmAction(new SendJSAction("server-manager", "removeJavaVersion", new StringValue(version.getID())).onSuccess(new ReloadPageAction())));
					els.add(delete);
				}
				
				els.add(new WebinterfaceVerticalSpacer("30px"));
			}
			
			WebinterfaceButton add = new WebinterfaceButton("Add Java version");
			add.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
			add.setOnClickAction(new RedirectAction("/sm/add-java-version"));
			els.add(add);
			
			return els;
		});
		
		addSection(s);
	}

}
