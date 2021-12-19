package me.mrletsplay.servermanager.webinterface.page;

import java.util.ArrayList;
import java.util.List;

import me.mrletsplay.servermanager.server.VelocityBase;
import me.mrletsplay.servermanager.util.PaperAPI;
import me.mrletsplay.servermanager.util.VelocityForwardingMode;
import me.mrletsplay.webinterfaceapi.http.HttpStatusCodes;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.LoadingScreenAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.CheckboxValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceCheckBox;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceElementGroup;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class VelocitySettingsPage extends WebinterfacePage {
	
	public VelocitySettingsPage() {
		super("Velocity Settings", "/sm/velocity-settings", true);
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addTitle("Velocity Settings");
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			WebinterfaceSelect forw = new WebinterfaceSelect();
			forw.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			VelocityForwardingMode current = VelocityBase.getForwardingMode();
			for(VelocityForwardingMode m : VelocityForwardingMode.values()) {
				forw.addOption(m.getFriendlyName(), m.name(), m == current);
			}
			els.add(forw);
			
			WebinterfaceButton forwConfirm = new WebinterfaceButton("Confirm");
			forwConfirm.setOnClickAction(new SendJSAction("server-manager", "setVelocityForwardingMode", new ElementValue(forw)).onSuccess(new ReloadPageAction()));
			els.add(forwConfirm);
			
			WebinterfaceElementGroup chbGrp = new WebinterfaceElementGroup();
			chbGrp.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("50px", "auto"));
			
			WebinterfaceCheckBox autostart = new WebinterfaceCheckBox(VelocityBase.isAutostart());
			autostart.setOnChangeAction(new SendJSAction("server-manager", "setVelocityAutostart", new CheckboxValue(autostart)).onError(new ReloadPageAction()));
			chbGrp.addElement(autostart);
			
			chbGrp.addElement(WebinterfaceText.builder()
					.text("Autostart")
					.leftboundText()
					.create());
			
			els.add(chbGrp);
			
			els.add(new WebinterfaceVerticalSpacer("30px"));
			
			els.add(WebinterfaceTitleText.builder()
					.text("Danger Zone")
					.fullWidth()
					.leftboundText()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text("Update Velocity version")
					.fullWidth()
					.leftboundText()
					.create());

			WebinterfaceSelect ver = new WebinterfaceSelect();
			ver.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			for(String version : PaperAPI.getVelocityVersions()) {
				ver.addOption("Velocity " + version, version);
			}
			els.add(ver);
			
			ObjectValue val = new ObjectValue();
			val.put("version", new ElementValue(ver));
			
			WebinterfaceButton update = WebinterfaceButton.builder()
					.text("Update")
					.onClick(MultiAction.of(LoadingScreenAction.show(), new SendJSAction("server-manager", "updateVelocity", val).onSuccess(LoadingScreenAction.hide()).onError(LoadingScreenAction.hide())))
					.create();
			els.add(update);
			
			return els;
		});
		
		addSection(s);
	}
	
	@Override
	public void createContent() {
		if(!VelocityBase.isInstalled()) {
			HttpRequestContext c = HttpRequestContext.getCurrentContext();
			c.getServerHeader().setStatusCode(HttpStatusCodes.FOUND_302);
			c.getServerHeader().getFields().setFieldValue("Location", "/");
			return;
		}
		
		super.createContent();
	}
	
}
