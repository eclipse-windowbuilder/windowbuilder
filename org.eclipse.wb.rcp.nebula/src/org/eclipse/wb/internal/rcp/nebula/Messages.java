package org.eclipse.wb.internal.rcp.nebula;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.rcp.nebula.Messages"; //$NON-NLS-1$
  public static String CollapsibleButtonEntryInfo_description;
  public static String CollapsibleButtonEntryInfo_name;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
