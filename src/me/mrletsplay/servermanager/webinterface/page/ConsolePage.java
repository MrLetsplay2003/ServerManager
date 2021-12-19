package me.mrletsplay.servermanager.webinterface.page;

import java.util.Collections;

import me.mrletsplay.servermanager.webinterface.LogElement;
import me.mrletsplay.webinterfaceapi.html.HtmlDocument;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.MultiAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SetValueAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceInputField;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;

public class ConsolePage extends WebinterfacePage {
	
	public ConsolePage() {
		super("Console", "/sm/console", true);
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		LogElement c = new LogElement();
		c.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		c.setID("console-area");
		s.addElement(c);
		WebinterfaceInputField i = new WebinterfaceInputField("Execute Console Command");
		ObjectValue v = new ObjectValue();
		v.put("server", new StringValue(() -> {
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			return ctx.getClientHeader().getPath().getQueryParameterValue("server");
		}));
		v.put("command", new ElementValue(i));
		i.setOnChangeAction(MultiAction.of(new SendJSAction("server-manager", "executeConsoleCommand", v), new SetValueAction(i, new StringValue(""))));
		i.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
		s.addElement(i);
		
		s.addDynamicElements(() -> {
			HtmlDocument doc = (HtmlDocument) HttpRequestContext.getCurrentContext().getProperty(CONTEXT_PROPERTY_DOCUMENT);
			doc.includeScript("/_internal/console-include.js", false, true);
			return Collections.emptyList();
		});
		
		addSection(s);
	}

}
