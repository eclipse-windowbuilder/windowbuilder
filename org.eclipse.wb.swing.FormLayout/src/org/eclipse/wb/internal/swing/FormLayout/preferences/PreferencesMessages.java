package org.eclipse.wb.internal.swing.FormLayout.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferencesMessages extends NLS {
  private static final String BUNDLE_NAME =
      "org.eclipse.wb.internal.swing.FormLayout.preferences.PreferencesMessages"; //$NON-NLS-1$
  public static String FormLayoutPreferencePage_rightAlignment;
  public static String FormLayoutPreferencePage_useGrab;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
  }

  private PreferencesMessages() {
  }
}
