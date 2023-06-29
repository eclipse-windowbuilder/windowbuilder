package org.eclipse.wb.internal.rcp.databinding.emf;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wb.internal.rcp.databinding.emf.Messages"; //$NON-NLS-1$
	public static String EmfObserveTypeContainer_argumentNotFound;
	public static String EmfObserveTypeContainer_masterObservableArgumentNotFound;
	public static String EmfObserveTypeContainer_masterObservableNotFound;
	public static String EmfPropertiesCodeSupport_argumentNotFound;
	public static String EmfPropertiesCodeSupport_beanPropertyNotFound;
	public static String GlobalObservableFactory_for25Button;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
