/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.errors.report.ErrorReport;
import org.eclipse.wb.internal.core.editor.errors.report.IReportSubmitter;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.StringAreaDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dialog to contact with support via http or e-mail.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public class ContactSupportDialog extends AbstractValidationTitleAreaDialog {
  // report
  private final ErrorReport m_errorReport;
  // GUI
  private Composite m_container;
  private BooleanDialogField m_screenshotsField;
  private Composite m_screenshotsComposite;
  private BooleanDialogField m_filesField;
  private Composite m_filesComposite;
  private BooleanDialogField m_projectField;
  private BooleanDialogField m_cuField;
  private StringDialogField m_summaryField;
  private StringAreaDialogField m_descriptionField;
  private ProgressMonitorPart m_progressMonitorPart;
  private StringDialogField m_nameField;
  private StringDialogField m_emailField;
  private final String m_exceptionTitle;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param exceptionTitle
   *          the possible title to show in the 'summary' field.
   * @param screenshot
   *          the {@link Image} of entire shell just before error. Can be <code>null</code> in case
   *          of parse error when no screenshot needed. Shouldn't be disposed here.
   * @param compilationUnit
   *          the compilation unit in which the problem occurred.
   * @param javaInfo
   *          the root {@link JavaInfo}.
   */
  public ContactSupportDialog(Shell parentShell,
      String exceptionTitle,
      Image screenshot,
      ErrorReport errorReport) {
    super(parentShell,
        DesignerPlugin.getDefault(),
        "Contact Support",
        "Please fill the fields below.",
        DesignerPlugin.getImage("actions/errors/support_banner.png"),
        "The information below is very important to fix a problem sooner.");
    m_exceptionTitle = exceptionTitle;
    m_errorReport = errorReport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createControls(Composite container) {
    m_container = container;
    final boolean addDefaultScreenshot = m_errorReport.hasDefaultScreenshot();
    GridLayoutFactory.create(container).columns(1);
    {
      // group for entering a problem descriptions
      Group descriptionGroup = new Group(container, SWT.NONE);
      descriptionGroup.setText("Problem description");
      GridDataFactory.create(descriptionGroup).grab().fill();
      GridLayoutFactory.create(descriptionGroup).columns(2);
      {
        m_summaryField = new StringDialogField();
        doCreateField(descriptionGroup, m_summaryField, "&Summary:", 60);
        m_summaryField.setText(m_exceptionTitle);
      }
      {
        m_descriptionField = new StringAreaDialogField(5);
        doCreateField(descriptionGroup, m_descriptionField, "&Description:", 60);
        final Text textControl = m_descriptionField.getTextControl(descriptionGroup);
        GridDataFactory.modify(textControl).grabV();
      }
    }
    {
      // group to add additional required and optional data (user info, hard&soft info, .log file and so on)
      Group additionalDataGroup = new Group(container, SWT.NONE);
      additionalDataGroup.setText("Additional data to include into report");
      GridDataFactory.create(additionalDataGroup).grabH().fillH();
      GridLayoutFactory.create(additionalDataGroup).columns(2);
      {
        // create first column composite
        Composite groupComposite1 = new Composite(additionalDataGroup, SWT.NONE);
        GridDataFactory.create(groupComposite1).fillH().alignVT();
        GridLayoutFactory.create(groupComposite1).columns(3).marginsH(0);
        {
          // user registration info and link to view it
          {
            BooleanDialogField userInfoField = new BooleanDialogField(true);
            doCreateBooleanField(groupComposite1, userInfoField, "Prod&uct info", 2);
            selectAndDisable(groupComposite1, userInfoField);
          }
          {
            Link link = new Link(groupComposite1, SWT.NONE);
            link.setText("<a>view...</a>");
            GridDataFactory.create(link);
            link.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                BrowserMessageDialog.openMessage(
                    getShell(),
                    "Product Info",
                    m_errorReport.getRegistrationInfo());
              }
            });
          }
        }
        {
          // Eclipse .log file
          BooleanDialogField logInfoField = new BooleanDialogField(true);
          doCreateBooleanField(groupComposite1, logInfoField, "&Log files (ex., Eclipse .log)", 3);
          selectAndDisable(groupComposite1, logInfoField);
        }
        {
          // screenshots and link to add it
          {
            m_screenshotsField = new BooleanDialogField(true);
            doCreateBooleanField(groupComposite1, m_screenshotsField, "Sc&reenshots"
                + (addDefaultScreenshot ? ":" : ""), 2);
            // don't disable this control when no default screenshot
            if (addDefaultScreenshot) {
              m_screenshotsField.setSelection(true);
            }
            m_screenshotsField.setDialogFieldListener(new IDialogFieldListener() {
              public void dialogFieldChanged(DialogField field) {
                final boolean fieldSelected = m_screenshotsField.getSelection();
                GridDataFactory.modify(m_screenshotsComposite).exclude(!fieldSelected);
                if (fieldSelected) {
                  if (!addScreenshot()) {
                    m_screenshotsField.setSelection(false);
                  }
                } else {
                  removeAllScreenshots();
                }
              }
            });
          }
          {
            final Link link = new Link(groupComposite1, SWT.NONE);
            link.setText("<a>add...</a>");
            GridDataFactory.create(link).hintHC(link.getText().length());
            link.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                if (addScreenshot()) {
                  m_screenshotsField.setSelection(true);
                }
              }
            });
          }
        }
        {
          m_screenshotsComposite = new Composite(groupComposite1, SWT.NONE);
          GridDataFactory.create(m_screenshotsComposite).spanH(3).exclude(!addDefaultScreenshot);
          GridLayoutFactory.create(m_screenshotsComposite).marginsH(10).marginsV(0).columns(2);
          {
            // default post-mortem screenshot
            if (addDefaultScreenshot) {
              selectAndDisable(
                  m_screenshotsComposite,
                  addDefaultScreenshotControl(m_screenshotsComposite));
            }
          }
        }
        {
          m_cuField = new BooleanDialogField(true);
          doCreateBooleanField(groupComposite1, m_cuField, "Source file at which error occurred", 3);
          m_cuField.setDialogFieldListener(new IDialogFieldListener() {
            public void dialogFieldChanged(DialogField field) {
              final boolean selected = m_cuField.getSelection();
              m_errorReport.setIncludeSourceFile(selected);
            }
          });
          if (!m_errorReport.hasSourceFile()) {
            m_cuField.setSelection(false);
            m_cuField.setEnabled(false);
          } else {
            m_cuField.setSelection(true);
          }
        }
        {
          m_projectField = new BooleanDialogField(true);
          doCreateBooleanField(groupComposite1, m_projectField, "Entire &project of error", 3);
          m_projectField.setDialogFieldListener(new IDialogFieldListener() {
            public void dialogFieldChanged(DialogField field) {
              final boolean selected = m_projectField.getSelection();
              if (selected) {
                // warn the user for huge size of report
                boolean dialogResult =
                    MessageDialog.openConfirm(
                        getShell(),
                        "Warning",
                        "Adding the entire project into problem report may cause of creating a very big report file. Press OK to continue.");
                if (dialogResult) {
                  m_errorReport.setIncludeProject(selected);
                  // we should set both "CU" and "project" mode
                  m_cuField.setSelection(true);
                  m_errorReport.setIncludeSourceFile(true);
                } else {
                  m_errorReport.setIncludeProject(false);
                  m_projectField.setSelection(false);
                }
              }
            }
          });
        }
      }
      // create column composite
      Composite groupComposite2 = new Composite(additionalDataGroup, SWT.NONE);
      GridDataFactory.create(groupComposite2).fillH().alignVT();
      GridLayoutFactory.create(groupComposite2).columns(3).marginsH(0);
      {
        // hardware and software summary and link to view it
        {
          BooleanDialogField compInfoField = new BooleanDialogField(true);
          doCreateBooleanField(groupComposite2, compInfoField, "So&ftware and hardware summary", 2);
          selectAndDisable(groupComposite2, compInfoField);
        }
        {
          Link link = new Link(groupComposite2, SWT.NONE);
          link.setText("<a>view...</a>");
          GridDataFactory.create(link);
          link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              BrowserMessageDialog.openMessage(
                  getShell(),
                  "Software and Hardware Info",
                  m_errorReport.getComputerInfo());
            }
          });
        }
        {
          // WindowBuilder preferences
          BooleanDialogField preferencesInfoField = new BooleanDialogField(true);
          doCreateBooleanField(
              groupComposite2,
              preferencesInfoField,
              "&WindowBuilder preferences",
              3);
          selectAndDisable(groupComposite2, preferencesInfoField);
        }
        {
          // filez ;) and link to add it
          {
            m_filesField = new BooleanDialogField(true);
            doCreateBooleanField(groupComposite2, m_filesField, "Fi&le attachments", 2);
            m_filesField.setDialogFieldListener(new IDialogFieldListener() {
              public void dialogFieldChanged(DialogField field) {
                final boolean fieldSelected = m_filesField.getSelection();
                GridDataFactory.modify(m_filesComposite).exclude(!fieldSelected);
                if (fieldSelected) {
                  if (!addFile()) {
                    m_filesField.setSelection(false);
                  }
                } else {
                  removeAllFiles();
                }
              }
            });
          }
          {
            Link link = new Link(groupComposite2, SWT.NONE);
            link.setText("<a>add...</a>");
            GridDataFactory.create(link);
            link.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                if (addFile()) {
                  m_filesField.setSelection(true);
                }
              }
            });
          }
          {
            m_filesComposite = new Composite(groupComposite2, SWT.NONE);
            GridDataFactory.create(m_filesComposite).spanH(3).exclude(true);
            GridLayoutFactory.create(m_filesComposite).marginsH(10).marginsV(0).columns(2);
            m_filesComposite.setVisible(false);
          }
        }
      }
      {
        // Privacy policy
        Link link = new Link(additionalDataGroup, SWT.NONE);
        link.setText("<a href=\"http://www.eclipse.org/legal/privacy.php\">Eclipse.org Privacy Policy</a>");
        GridDataFactory.create(link).alignHR().spanH(2);
        link.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            DesignerExceptionUtils.openBrowser("http://www.eclipse.org/legal/privacy.php");
          }
        });
      }
    }
    {
      Group optionsGroup = new Group(container, SWT.NONE);
      optionsGroup.setText("Contact options");
      GridDataFactory.create(optionsGroup).grabH().fillH();
      GridLayoutFactory.create(optionsGroup).columns(1);
      {
        m_nameField = new StringDialogField();
        doCreateField(optionsGroup, m_nameField, "Contact Name (optional):", 60);
        m_nameField.setText(m_errorReport.getName());
      }
      {
        m_emailField = new StringDialogField();
        doCreateField(
            optionsGroup,
            m_emailField,
            "Contact E-mail (never be shared with anyone):",
            60);
        m_emailField.setText(m_errorReport.getEmail());
        if (m_emailField.getText().length() == 0) {
          m_emailField.setText("<enter address here, if you want a response, or leave blank>");
        }
      }
      {
        Composite methodGroup = new Composite(optionsGroup, SWT.NONE);
        GridDataFactory.create(methodGroup).grabH().fillH();
        GridLayoutFactory.create(methodGroup).columns(2).noMargins();
        {
          Label label = new Label(methodGroup, SWT.NONE);
          label.setText("Ple&ase select:");
          GridDataFactory.create(label).spanH(2);
        }
        {
          Button webButton = new Button(methodGroup, SWT.RADIO);
          webButton.setText("Submit directly to web-site (recommended)");
          webButton.setSelection(true);
          webButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              m_errorReport.setSubmitter(IReportSubmitter.WEB);
            }
          });
        }
        {
          Link link = new Link(methodGroup, SWT.NONE);
          link.setText("<a>proxy settings...</a>");
          link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              ProxySettingDialog dialog = new ProxySettingDialog(getShell());
              dialog.open();
            }
          });
        }
        {
          Button mailButton = new Button(methodGroup, SWT.RADIO);
          mailButton.setText("Submit manually via email or forum");
          GridDataFactory.create(mailButton).spanH(2);
          mailButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              m_errorReport.setSubmitter(IReportSubmitter.MAIL);
            }
          });
        }
      }
    }
    {
      final GridLayout layout = new GridLayout();
      layout.marginHeight = 0;
      m_progressMonitorPart = new ProgressMonitorPart(container, layout);
      GridDataFactory.modify(m_progressMonitorPart).alignHF();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener handlers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens {@link FileDialog} for selecting screenshot file, then if selected adds screenshot
   * control onto screenshots composite, marks as selected and lays out dialog composite.
   */
  private boolean addScreenshot() {
    FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
    fileDialog.setFilterExtensions(new String[]{"*.png", "*.jpg", "*.bmp", "*.tiff", "*.tga"});
    String fileName = fileDialog.open();
    if (fileName != null) {
      if (!m_errorReport.hasScreenshot(fileName)) {
        m_errorReport.addScreenshot(fileName);
        m_screenshotsComposite.setVisible(true);
        GridDataFactory.modify(m_screenshotsComposite).exclude(false);
        addScreenshotControl(fileName);
      }
      return true;
    }
    return false;
  }

  /**
   * Removes all user screenshots from being sent list.
   */
  private void removeAllScreenshots() {
    removeAllControls(m_screenshotsComposite);
    m_errorReport.removeAllScreenshots();
    GridDataFactory.modify(m_screenshotsComposite).exclude(true);
    m_screenshotsComposite.setVisible(false);
    m_container.layout(true, true);
  }

  /**
   * Opens {@link FileDialog} for selecting file, then if selected adds file control onto files
   * composite, marks as selected and lays out dialog composite.
   */
  private boolean addFile() {
    FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
    fileDialog.setFilterExtensions(new String[]{"*.*"});
    String fileName = fileDialog.open();
    if (fileName != null) {
      if (!m_errorReport.hasFile(fileName)) {
        m_errorReport.addFile(fileName);
        m_filesComposite.setVisible(true);
        GridDataFactory.modify(m_filesComposite).exclude(false);
        addFileControl(fileName);
      }
      return true;
    }
    return false;
  }

  /**
   * Removes all files from being sent list.
   */
  private void removeAllFiles() {
    removeAllControls(m_filesComposite);
    m_errorReport.removeAllFiles();
    GridDataFactory.modify(m_filesComposite).exclude(true);
    m_filesComposite.setVisible(false);
    m_container.layout(true, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Marks given boolean field as selected and disables button control.
   */
  private void selectAndDisable(Composite parent, BooleanDialogField booleanField) {
    booleanField.setSelection(true);
    booleanField.getButtonControl(parent).setEnabled(false);
  }

  /**
   * Adds {@link BooleanDialogField} instance representing added screenshot.
   * 
   * @param filePath
   *          a full path to screenshot file.
   * @return the {@link BooleanDialogField} representing added screenshot.
   */
  private BooleanDialogField addScreenshotControl(final String filePath) {
    final BooleanDialogField dialogField = addControl(m_screenshotsComposite, filePath);
    dialogField.setDialogFieldListener(new IDialogFieldListener() {
      public void dialogFieldChanged(DialogField field) {
        m_errorReport.includeScreenshot(filePath, dialogField.getSelection());
      }
    });
    return dialogField;
  }

  /**
   * Adds {@link BooleanDialogField} instance representing added file.
   * 
   * @param filePath
   *          a full path to file.
   * @return the {@link BooleanDialogField} representing added file.
   */
  private BooleanDialogField addFileControl(final String filePath) {
    final BooleanDialogField dialogField = addControl(m_filesComposite, filePath);
    dialogField.setDialogFieldListener(new IDialogFieldListener() {
      public void dialogFieldChanged(DialogField field) {
        m_errorReport.includeFile(filePath, dialogField.getSelection());
      }
    });
    return dialogField;
  }

  /**
   * Creates {@link BooleanDialogField}, selects it and make container to do layout.
   */
  private BooleanDialogField addControl(Composite parent, String filePath) {
    final BooleanDialogField selectableField = new BooleanDialogField(true);
    selectableField.setSelection(true);
    doCreateBooleanField(parent, selectableField, FilenameUtils.getName(filePath), 2);
    m_container.layout(true, true);
    return selectableField;
  }

  /**
   * Deletes all entries from parent.
   */
  protected void removeAllControls(Composite parent) {
    for (Control control : parent.getChildren()) {
      control.dispose();
    }
  }

  /**
   * Adds {@link BooleanDialogField} instance representing default post-mortem screenshot.
   * 
   * @return the {@link BooleanDialogField} representing post-mortem screenshot.
   */
  private BooleanDialogField addDefaultScreenshotControl(Composite parent) {
    final BooleanDialogField screenShotField = new BooleanDialogField(true);
    doCreateBooleanField(parent, screenShotField, "Just before error", 2);
    return screenShotField;
  }

  /**
   * Configures given {@link DialogField} for specific of this dialog.
   */
  protected final void doCreateField(Composite fieldsContainer,
      DialogField dialogField,
      String labelText,
      int chars) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    DialogFieldUtils.fillControls(fieldsContainer, dialogField, 2, chars);
  }

  protected final void doCreateBooleanField(Composite fieldsContainer,
      DialogField dialogField,
      String labelText,
      int columns) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    dialogField.doFillIntoGrid(fieldsContainer, columns);
  }

  /**
   * Disposes the main dialog content, shows success messages.
   */
  private void showSuccessMessage() {
    // turn cancel to close
    Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
    cancelButton.setText(IDialogConstants.CLOSE_LABEL);
    // remove all dialog contents
    Composite containerParent = m_container.getParent();
    m_container.dispose();
    // prepare success contents
    Composite successComposite = new Composite(containerParent, SWT.NONE);
    GridDataFactory.create(successComposite).grab().fill();
    GridLayoutFactory.create(successComposite);
    {
      Label successLabel = new Label(successComposite, SWT.NONE);
      successLabel.setText("Operations done successfully, thank you for your feedback!");
      GridDataFactory.create(successLabel).grab().alignVB().alignHC();
    }
    {
      Label successLabel = new Label(successComposite, SWT.NONE);
      successLabel.setText("Press \"Close\" button.");
      GridDataFactory.create(successLabel).grab().alignVT().alignHC();
    }
    containerParent.layout(true, true);
    String message =
        m_errorReport.getSubmitter() == IReportSubmitter.WEB
            ? "Report submit success."
            : "Report prepared successfully.";
    setMessage(message, IMessageProvider.INFORMATION);
    setTitle("Success.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validate/OK
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() throws Exception {
    if (StringUtils.isEmpty(m_summaryField.getText())) {
      return "Please enter the problem summary.";
    } else if (StringUtils.isEmpty(m_descriptionField.getText())) {
      return "Please provide a description of the problem.";
    } else if (!isValidEmail(m_emailField.getText())) {
      return "Please provide a syntactically correct e-mail address or leave blank.";
    }
    return super.validate();
  }

  private static boolean isValidEmail(String email) {
    if (email == null || email.length() == 0) {
      return true; // allow empty
    }
    Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
    Matcher m = p.matcher(email);
    return m.matches();
  }

  @Override
  protected void okPressed() {
    Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
    Button okButton = getButton(IDialogConstants.OK_ID);
    m_progressMonitorPart.attachToCancelComponent(cancelButton);
    okButton.setEnabled(false);
    try {
      m_errorReport.setSummary(m_summaryField.getText());
      m_errorReport.setDescription(m_descriptionField.getText());
      m_errorReport.setName(m_nameField.getText());
      m_errorReport.setEmail(m_emailField.getText());
      m_errorReport.reportProblem(m_progressMonitorPart);
    } catch (final Throwable e) {
      okButton.setEnabled(true);
      // show error
      showError(e);
      // offer to send report by mail
      maybeUseMail();
      // exit
      return;
    } finally {
      // anyway done monitor
      m_progressMonitorPart.done();
      m_progressMonitorPart.removeFromCancelComponent(cancelButton);
    }
    showSuccessMessage();
  }

  private void maybeUseMail() {
    if (m_errorReport.getSubmitter() != IReportSubmitter.MAIL) {
      if (MessageDialog.openQuestion(
          DesignerPlugin.getShell(),
          "Question",
          "Would you like to send report by e-mail?")) {
        m_errorReport.setSubmitter(IReportSubmitter.MAIL);
        m_progressMonitorPart.done();
        try {
          m_errorReport.reportProblem(m_progressMonitorPart);
          showSuccessMessage();
        } catch (Throwable e) {
          // no luck
          showError(e);
        }
      }
    }
  }

  private void showError(final Throwable e) {
    final Status status =
        new Status(IStatus.ERROR,
            DesignerPlugin.getDefault().toString(),
            IStatus.ERROR,
            "Click \"Details\" for more info.",
            e);
    ErrorDialog dialog =
        new ErrorDialog(
            DesignerPlugin.getShell(),
            "Error sending report",
            "Sorry, there was an error sending report. Please try again or contact support directly by e-mail.",
            status, IStatus.ERROR) {
          private Clipboard clipboard;

          @Override
          protected List createDropDownList(Composite parent) {
            final List list = super.createDropDownList(parent);
            list.removeAll();
            // populate list using custom PrintWriter
            final PrintWriter printWriter = new PrintWriter(new Writer() {
              @Override
              public void write(char[] cbuf, int off, int len) throws IOException {
                if (len != 2 && !(cbuf[0] == '\r' || cbuf[0] == '\n')) {
                  list.add(StringUtils.replace(new String(cbuf, off, len), "\t", "    "));
                }
              }

              @Override
              public void flush() throws IOException {
              }

              @Override
              public void close() throws IOException {
              }
            });
            e.printStackTrace(printWriter);
            // install own context menu
            Menu menu = list.getMenu();
            menu.dispose();
            Menu copyMenu = new Menu(list);
            MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
            copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
            copyItem.addSelectionListener(new SelectionListener() {
              public void widgetSelected(@SuppressWarnings("hiding") SelectionEvent e) {
                copyList(list);
              }

              public void widgetDefaultSelected(@SuppressWarnings("hiding") SelectionEvent e) {
                copyList(list);
              }
            });
            list.setMenu(copyMenu);
            return list;
          }

          private void copyList(List list) {
            if (clipboard != null) {
              clipboard.dispose();
            }
            StringBuffer statusBuffer = new StringBuffer();
            for (int i = 0; i < list.getItemCount(); ++i) {
              statusBuffer.append(list.getItem(i));
              statusBuffer.append("\r\n");
            }
            clipboard = new Clipboard(list.getDisplay());
            clipboard.setContents(
                new Object[]{statusBuffer.toString()},
                new Transfer[]{TextTransfer.getInstance()});
          }

          @Override
          public boolean close() {
            if (clipboard != null) {
              clipboard.dispose();
            }
            return super.close();
          }
        };
    dialog.open();
  }
}
