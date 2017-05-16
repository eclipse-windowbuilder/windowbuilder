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
package org.eclipse.wb.internal.core.nls.ui;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import java.beans.PropertyChangeListener;

/**
 * Dialog for asking new strings source parameters.
 * <p>
 * Dialog layout is different if there is one or more than one NLS source choice.
 *
 * @author scheglov_ke
 * @author Jaime Wren
 * @coverage core.nls.ui
 */
public class NewSourceDialog extends TitleAreaDialog {
  private static final String USUAL_MESSAGE = Messages.NewSourceDialog_message;
  private static final String ONLY_ONE_SOURCE_MESSAGE = Messages.NewSourceDialog_messageOneSource;
  private final JavaInfo m_root;
  private boolean m_hasOneSource;
  ////////////////////////////////////////////////////////////////////////////
  //
  // UI objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private Group m_containerComposite;
  private StackLayout m_containerCompositeLayout;
  private Button m_okButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewSourceDialog(Shell parentShell, JavaInfo root) {
    super(parentShell);
    m_root = root;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    // configure title area
    setTitle(Messages.NewSourceDialog_title);
    setMessage(USUAL_MESSAGE);
    setTitleImage(DesignerPlugin.getImage("nls/add_source.gif"));
    // configure container
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    container.setLayout(new GridLayout());
    // create parts of GUI
    createTypeGroup(container);
    createPropertiesGroup(container);
    // fill for one source
    if (m_hasOneSource) {
      createContentsForOneSource();
    }
    // done
    return area;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: types
  //
  ////////////////////////////////////////////////////////////////////////////
  private SourceDescription[] m_sourceDescriptions;
  private AbstractSourceNewComposite m_sourceComposites[];
  private SourceDescription m_currentSourceDescription;
  private AbstractSourceNewComposite m_currentSourceComposite;
  private SourceViewer m_sampleViewer;

  /**
   * Creates types composite, prepares source classes and composites.
   */
  private void createTypeGroup(final Composite container) {
    // fill typeGroup with SourceDescription's
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        createDescriptionsControls(container);
      }
    });
    // create sample viewer
    {
      Group exampleGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(exampleGroup).grabH().fillH();
      exampleGroup.setLayout(new GridLayout());
      exampleGroup.setText(Messages.NewSourceDialog_exampleGroup);
      //
      m_sampleViewer = JdtUiUtils.createJavaSourceViewer(exampleGroup, SWT.BORDER);
      GridDataFactory.create(m_sampleViewer.getControl()).grab().fill().hintVC(4);
    }
  }

  /**
   * Fills given parent with {@link SourceDescription} radio {@link Button}'s.
   */
  private void createDescriptionsControls(Composite container) throws Exception {
    // prepare arrays for sources
    m_sourceDescriptions = NlsSupport.getSourceDescriptions(m_root);
    m_sourceComposites = new AbstractSourceNewComposite[m_sourceDescriptions.length];
    // check, may be only one source
    m_hasOneSource = m_sourceDescriptions.length == 1;
    if (m_hasOneSource) {
      return;
    }
    // several sources, create selection group
    Group typeGroup = new Group(container, SWT.NONE);
    GridDataFactory.create(typeGroup).grabH().fill();
    typeGroup.setLayout(new GridLayout());
    typeGroup.setText(Messages.NewSourceDialog_sourceTypesGroup);
    // create radio buttons for possible sources
    for (int i = 0; i < m_sourceDescriptions.length; i++) {
      SourceDescription sourceDescription = m_sourceDescriptions[i];
      if (sourceDescription.getNewCompositeClass() == null) {
        continue;
      }
      // prepare title
      String title = sourceDescription.getTitle();
      // create button for current class
      {
        Button sourceButton = new Button(typeGroup, SWT.RADIO);
        sourceButton.setText(title);
        // add selection listener
        final int index = i;
        sourceButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            ExecutionUtils.runLog(new RunnableEx() {
              public void run() throws Exception {
                populateForSourceIndex(index);
              }
            });
          }
        });
      }
    }
  }

  /**
   * In the case where there is just one source and no radio buttons were generated, this method
   * populates the contents of the dialog.
   */
  private void createContentsForOneSource() {
    m_currentSourceDescription = m_sourceDescriptions[0];
    if (m_currentSourceDescription.getNewCompositeClass() == null) {
      return;
    }
    // prepare one-source case title and dialog message
    String title = m_currentSourceDescription.getTitle();
    setTitle(title);
    // show source in async, because Dialog itself manages size during create
    ExecutionUtils.runAsync(new RunnableEx() {
      public void run() throws Exception {
        populateForSourceIndex(0);
      }
    });
  }

  private void populateForSourceIndex(int index) throws Exception {
    m_currentSourceDescription = m_sourceDescriptions[index];
    // ensure that we have composite for this source class
    m_currentSourceComposite = m_sourceComposites[index];
    if (m_currentSourceComposite == null) {
      // create new composite
      m_currentSourceComposite =
          m_currentSourceDescription.createNewComposite(m_containerComposite, m_root);
      m_currentSourceComposite.addPropertyChangeListener(m_propertyChangeListener);
      // store composite
      m_sourceComposites[index] = m_currentSourceComposite;
    }
    // show composite for selected source type
    m_containerCompositeLayout.topControl = m_currentSourceComposite;
    m_containerComposite.layout();
    // show sample for select source type
    if (m_sampleViewer != null) {
      String sample = m_currentSourceComposite.getSample();
      JdtUiUtils.setJavaSourceForViewer(m_sampleViewer, sample);
    }
    // change size
    {
      Point p = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
      getShell().setSize(p.x, p.y);
    }
    // validate selected composite
    validateCurrentSourceComposite();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createPropertiesGroup(Composite parent) {
    m_containerComposite = new Group(parent, SWT.NONE);
    GridDataFactory.create(m_containerComposite).grab().fill();
    {
      m_containerCompositeLayout = new StackLayout();
      m_containerComposite.setLayout(m_containerCompositeLayout);
    }
    m_containerComposite.setText(Messages.NewSourceDialog_sourceProperties);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // New source properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyChangeListener m_propertyChangeListener = new PropertyChangeListener() {
    public void propertyChange(java.beans.PropertyChangeEvent e) {
      validateCurrentSourceComposite();
    }
  };

  private void validateCurrentSourceComposite() {
    IStatus status = m_currentSourceComposite.getStatus();
    if (m_hasOneSource) {
      StatusUtils.applyToTitleAreaDialog(this, status, ONLY_ONE_SOURCE_MESSAGE);
    } else {
      StatusUtils.applyToTitleAreaDialog(this, status, USUAL_MESSAGE);
    }
    if (m_okButton != null) {
      m_okButton.setEnabled(status.getSeverity() < IStatus.ERROR);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.NewSourceDialog_shellTitle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    {
      m_okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
      m_okButton.setEnabled(false);
    }
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected void okPressed() {
    try {
      m_newSourceDescription = m_currentSourceDescription;
      m_newSourceParameters = m_currentSourceComposite.createParametersObject();
      m_newEditableSource = m_currentSourceComposite.createEditableSource(m_newSourceParameters);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      return;
    }
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Result access
  //
  ////////////////////////////////////////////////////////////////////////////
  private IEditableSource m_newEditableSource;
  private SourceDescription m_newSourceDescription;
  private Object m_newSourceParameters;

  /**
   * @return created {@link IEditableSource}.
   */
  public IEditableSource getNewEditableSource() {
    return m_newEditableSource;
  }

  /**
   * @return the selected {@link SourceDescription}.
   */
  public SourceDescription getNewSourceDescription() {
    return m_newSourceDescription;
  }

  /**
   * @return parameters that needed by {@link AbstractSource} to create its objects.
   */
  public Object getNewSourceParameters() {
    return m_newSourceParameters;
  }
}
