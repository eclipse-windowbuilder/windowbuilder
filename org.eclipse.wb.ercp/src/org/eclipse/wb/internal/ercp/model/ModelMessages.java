package org.eclipse.wb.internal.ercp.model;

import org.eclipse.osgi.util.NLS;

public class ModelMessages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.ercp.model.ModelMessages"; //$NON-NLS-1$
  public static String ErcpToolkitDescription_toolkitName;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, ModelMessages.class);
  }

  private ModelMessages() {
  }
}
