package me.mrletsplay.servermanager.webinterface.page;

import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.HideLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;

public class ShutdownPage extends WebinterfacePage {
	
	public ShutdownPage() {
		super("Shutdown", "/sm/shutdown");
		setIcon("mdi:power");
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addTitle("Shutdown");
		
		WebinterfaceButton reload = new WebinterfaceButton("Reload everything");
		reload.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		reload.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "reloadEverything", null).onSuccess(new RedirectAction("/")).onError(new HideLoadingScreenAction())));
		s.addElement(reload);
		
		WebinterfaceButton shutdown = new WebinterfaceButton("Shutdown everything");
		shutdown.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		shutdown.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "shutdownEverything", null).onSuccess(new RedirectAction("/")).onError(new HideLoadingScreenAction())));
		s.addElement(shutdown);
		
		addSection(s);
	}

}
