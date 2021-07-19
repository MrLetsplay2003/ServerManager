package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import me.mrletsplay.servermanager.ServerManager;
import me.mrletsplay.servermanager.server.MinecraftServer;
import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceElementGroup;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class HostnamesPage extends WebinterfacePage {
	
	@SuppressWarnings("unchecked")
	public HostnamesPage() {
		super("Hostnames", "/sm/hostnames");
		getContainerStyle().setProperty("max-width", "900px");
		
		addDynamicSections(() -> {
			List<WebinterfacePageSection> scs = new ArrayList<>();
			
			CommentedFileConfig config = VelocityBase.loadVelocityConfig();
			
			CommentedConfig forcedHosts = config.get("forced-hosts");
			for(Map.Entry<String, Object> host : forcedHosts.valueMap().entrySet()) {
				List<String> servers = (List<String>) host.getValue();
				WebinterfacePageSection s = new WebinterfacePageSection();
				s.addTitle(host.getKey());
				
//				WebinterfaceButton settings = new WebinterfaceButton("Settings");
//				settings.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
//				s.addElement(settings);
				
				WebinterfaceElementGroup grpI = new WebinterfaceElementGroup();
				grpI.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("min-content", "auto"));
				
				WebinterfaceTitleText tt = new WebinterfaceTitleText("Servers");
				tt.addLayoutOptions(DefaultLayoutOption.LEFTBOUND, DefaultLayoutOption.CENTER_VERTICALLY);
				grpI.addElement(tt);
				
				grpI.addElement(WebinterfaceText.builder()
						.text(servers.isEmpty() ? "(none)" : servers.stream().collect(Collectors.joining(", ")))
						.leftbound()
						.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
						.create());
				
				s.addElement(grpI);
				
				WebinterfaceElementGroup grpH = new WebinterfaceElementGroup();
				grpH.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				
				WebinterfaceSelect addS = new WebinterfaceSelect();
				addS.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
				for(MinecraftServer server : ServerManager.getServers()) {
					if(!servers.contains(server.getID())) addS.addOption(server.getName(), server.getID());
				}
				grpH.addElement(addS);
				
				WebinterfaceButton addB = new WebinterfaceButton("Add");
				grpH.addElement(addB);
				
				WebinterfaceButton delete = new WebinterfaceButton("Delete");
				delete.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				grpH.addElement(delete);
				
				s.addElement(grpH);
				
				s.addElement(new WebinterfaceVerticalSpacer("30px"));
				
				WebinterfaceElementGroup grpC = new WebinterfaceElementGroup();
				grpC.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				
				WebinterfaceInputField createName = new WebinterfaceInputField("Hostname");
				createName.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
				grpC.addElement(createName);
				
				WebinterfaceButton create = new WebinterfaceButton("Create");
				grpC.addElement(create);
				
				s.addElement(grpC);
				
				scs.add(s);
			}
			
			return scs;
		});
	}

}
