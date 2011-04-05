package org.eclipse.wb.internal.swing.MigLayout.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferencesMessages extends NLS {
  private static final String BUNDLE_NAME =
      "org.eclipse.wb.internal.swing.MigLayout.preferences.PreferencesMessages"; //$NON-NLS-1$
  public static String MigLayoutPreferencePage_autoAlign;
  public static String MigLayoutPreferencePage_autoGrab;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
  }

  private PreferencesMessages() {
  }
}
