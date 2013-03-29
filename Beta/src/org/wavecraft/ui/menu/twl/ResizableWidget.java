package org.wavecraft.ui.menu.twl;

import de.matthiasmann.twl.Widget;

public interface ResizableWidget {
	public void resize(int w, int h);
	public Widget asWidget();
	public String getPathToThemeFile();
}
