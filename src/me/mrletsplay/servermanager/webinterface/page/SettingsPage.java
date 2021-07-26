package me.mrletsplay.servermanager.webinterface.page;

import me.mrletsplay.servermanager.webinterface.ServerManagerSettings;
import me.mrletsplay.webinterfaceapi.webinterface.Webinterface;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfaceSettingsPage;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSettingsPane;

public class SettingsPage extends WebinterfaceSettingsPage {
	
	public SettingsPage() {
		super("Settings", "/sm/settings", new WebinterfaceSettingsPane(Webinterface.getConfig(), ServerManagerSettings.INSTANCE.getSettingsCategories(), "server-manager", "setSetting"));
		setIcon("mdi:cog");
	}

}
