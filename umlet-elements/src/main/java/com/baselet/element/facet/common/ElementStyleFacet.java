package com.baselet.element.facet.common;

import java.util.Locale;

import com.baselet.control.enums.ElementStyle;
import com.baselet.element.facet.FirstRunKeyValueFacet;
import com.baselet.element.facet.PropertiesParserState;

public class ElementStyleFacet extends FirstRunKeyValueFacet {

	private static final ValueInfo NORESIZE_VALUE = new ValueInfo(ElementStyle.NORESIZE, "disable manual resizing");
	private static final ValueInfo WORDWRAP_VALUE = new ValueInfo(ElementStyle.WORDWRAP, "wrap lines at the end of the line");
	private static final ValueInfo AUTORESIZE_VALUE = new ValueInfo(ElementStyle.AUTORESIZE, "resizes element as text grows");

	private static final String MANUAL = "manual";
	private static final ValueInfo MANUAL_VALUE = new ValueInfo(MANUAL, "no automatic resizing, element size is set by hand");

	public static final ElementStyleFacet INSTANCE = new ElementStyleFacet(AUTORESIZE_VALUE, WORDWRAP_VALUE, NORESIZE_VALUE, MANUAL_VALUE);
	public static final ElementStyleFacet INSTANCE_AUTORESIZEONLY = new ElementStyleFacet(AUTORESIZE_VALUE);

	private final ValueInfo[] valueInfo;

	private ElementStyleFacet(ValueInfo... valueInfo) {
		this.valueInfo = valueInfo;
	}

	@Override
	public KeyValue getKeyValue() {
		return new KeyValue("style", valueInfo);
	}

	@Override
	public void handleValue(String value, PropertiesParserState state) {
		if (MANUAL.equalsIgnoreCase(value.trim())) {
			state.setElementStyle(ElementStyle.SIMPLE); // manual is the readable name of the plain style without any automatic resizing
		}
		else {
			state.setElementStyle(ElementStyle.valueOf(value.toUpperCase(Locale.ENGLISH)));
		}
	}

}
