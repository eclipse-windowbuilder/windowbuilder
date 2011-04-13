package org.eclipse.wb.internal.discovery.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.discovery.core.messages"; //$NON-NLS-1$
  public static String WBToolkit_fromProvider;
  public static String WBToolkit_fromProviderLicense;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
