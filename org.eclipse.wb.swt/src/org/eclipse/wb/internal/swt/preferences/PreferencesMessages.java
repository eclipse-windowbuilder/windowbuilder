package org.eclipse.wb.internal.swt.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferencesMessages extends NLS {
  private static final String BUNDLE_NAME =
      "org.eclipse.wb.internal.swt.preferences.PreferencesMessages"; //$NON-NLS-1$
  public static String GridLayoutPreferencePage_doAlignRight;
  public static String GridLayoutPreferencePage_useGrab;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
  }

  private PreferencesMessages() {
  }
}
