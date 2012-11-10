package org.eclipse.wb.internal.core.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.eclipse.wb.internal.core.wizards.messages"; //$NON-NLS-1$
  public static String DesignerJavaProjectWizard_errorMessage;
  public static String DesignerJavaProjectWizard_errorTitle;
  public static String DesignerJavaProjectWizard_title;
  public static String DesignerNewElementWizard_errorMessage;
  public static String DesignerNewElementWizard_errorSeeLog;
  public static String DesignerNewElementWizard_errorTitle;
  public static String DesignerNewProjectCreationWizardPage_message;
  public static String DesignerNewProjectCreationWizardPage_removeErrorMessage;
  public static String DesignerNewProjectCreationWizardPage_removeErrorTitle;
  public static String DesignerNewProjectCreationWizardPage_removeProgress;
  public static String DesignerNewProjectCreationWizardPage_title;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
