package org.eclipse.wb.internal.rcp.swing2swt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wb.internal.rcp.swing2swt.Messages"; //$NON-NLS-1$
	public static String BorderLayoutAssistantPage_horizontalGap;
	public static String BorderLayoutAssistantPage_verticalGap;
	public static String BorderLayout_center;
	public static String BorderLayout_east;
	public static String BorderLayout_north;
	public static String BorderLayout_south;
	public static String BorderLayout_west;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
