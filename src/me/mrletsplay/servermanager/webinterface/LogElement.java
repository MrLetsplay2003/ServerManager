package me.mrletsplay.servermanager.webinterface;

import me.mrletsplay.webinterfaceapi.html.HtmlElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.AbstractWebinterfacePageElement;

public class LogElement extends AbstractWebinterfacePageElement {
	
	private String text;
	
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public HtmlElement createElement() {
		HtmlElement el = new HtmlElement("code");
		el.appendAttribute("style", "height: 80vh; font-family: DejaVu Sans Mono; text-align: left; width: calc(100% - 20px); display:block; overflow-y: scroll; border: 1px solid rgba(128,128,128,0.5); padding-left: 10px; padding-right: 10px; white-space: nowrap;");
		if(text != null) el.setText(text);
		return el;
	}

}
