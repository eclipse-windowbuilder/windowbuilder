package org.eclipse.wb.internal.core.databinding.xml;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wb.internal.core.databinding.xml.messages"; //$NON-NLS-1$
	public static String BindingXmlPage_errorMessage;
	public static String BindingXmlPage_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
