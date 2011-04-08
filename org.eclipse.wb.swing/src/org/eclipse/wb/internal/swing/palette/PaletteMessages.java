package org.eclipse.wb.internal.swing.palette;

import org.eclipse.osgi.util.NLS;

public class PaletteMessages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.swing.palette.PaletteMessages"; //$NON-NLS-1$
  public static String AbsoluteLayoutEntryInfo_description;
  public static String AbsoluteLayoutEntryInfo_name;
  public static String ActionExternalEntryInfo_description;
  public static String ActionExternalEntryInfo_name;
  public static String ActionNewEntryInfo_description;
  public static String ActionNewEntryInfo_name;
  public static String SwingPaletteEntryInfo_description;
  public static String SwingPaletteEntryInfo_name;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, PaletteMessages.class);
  }

  private PaletteMessages() {
  }
}
