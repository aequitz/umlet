package com.baselet.element.settings;

import java.util.List;

import com.baselet.control.enums.AlignVertical;
import com.baselet.control.config.SharedConfig;
import com.baselet.control.enums.ElementStyle;
import com.baselet.element.facet.Facet;
import com.baselet.element.facet.Settings;

public abstract class SettingsManualresizeCenter extends Settings {

	@Override
	public ElementStyle getElementStyle() {
		return SharedConfig.getInstance().isAutofitShapes() ? ElementStyle.AUTORESIZE : ElementStyle.SIMPLE; // autofit is configurable in the options and can be overridden per element with style=manual|wordwrap|noresize
	}

	@Override
	protected List<Facet> createFacets() {
		return Settings.MANUALRESIZE;
	}

	@Override
	public AlignVertical getVAlign() {
		return AlignVertical.CENTER;
	}

}
