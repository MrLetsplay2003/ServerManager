package me.mrletsplay.servermanager.webinterface.page;

import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.HideLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ShowLoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;

public class ShutdownPage extends WebinterfacePage {
	
	public ShutdownPage() {
		super("Shutdown", "/sm/shutdown");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		
		WebinterfaceButton shutdown = new WebinterfaceButton("Shutdown everything");
		shutdown.setOnClickAction(new MultiAction(new ShowLoadingScreenAction(), new SendJSAction("server-manager", "shutdownEverything", null).onSuccess(new RedirectAction("/")).onError(new HideLoadingScreenAction())));
		s.addElement(shutdown);
		
		addSection(s);
	}

}
