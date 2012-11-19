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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import swingintegration.example.EmbeddedSwingComposite2;

/**
 * The dialog for editing {@link SpinnerModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class SpinnerModelDialog extends AbstractValidationTitleAreaDialog {
  private final SpinnerModel m_model;
  private String m_source;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SpinnerModelDialog(Shell parentShell, String shellText, SpinnerModel model) {
    super(parentShell,
        Activator.getDefault(),
        shellText,
        ModelMessages.SpinnerModelDialog_title,
        null,
        ModelMessages.SpinnerModelDialog_message);
    m_model = model;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the source of {@link SpinnerModel} that represents user selections.
   */
  public String getSource() {
    return m_source;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<AbstractSpinnerComposite> m_composites = Lists.newArrayList();
  private TabFolder m_tabFolder;
  private JSpinner m_spinner;

  //private Combo m_typeCombo;
  @Override
  protected void createControls(Composite container) {
    GridLayoutFactory.create(container);
    m_tabFolder = new TabFolder(container, SWT.NONE);
    GridDataFactory.create(m_tabFolder).grab().fill();
    // add composites
    {
      m_composites.add(new ListSpinnerComposite(m_tabFolder, this));
      m_composites.add(new NumberSpinnerComposite(m_tabFolder, this));
      m_composites.add(new DateSpinnerComposite(m_tabFolder, this));
    }
    // create tab for each spinner composite
    for (AbstractSpinnerComposite composite : m_composites) {
      // create tab
      TabItem tabItem = new TabItem(m_tabFolder, SWT.NONE);
      tabItem.setControl(composite);
      tabItem.setText(composite.getTitle());
      // select tab
      if (composite.setModel(m_model)) {
        m_tabFolder.setSelection(tabItem);
      }
    }
    // preview
    createPreviewComposite(container);
  }

  /**
   * Creates {@link Composite} with {@link JSpinner} for preview.
   */
  private void createPreviewComposite(Composite parent) {
    Group previewGroup = new Group(parent, SWT.NONE);
    GridDataFactory.create(previewGroup).fill();
    GridLayoutFactory.create(previewGroup).columns(2);
    previewGroup.setText(ModelMessages.SpinnerModelDialog_preview);
    // hint
    {
      Label label = new Label(previewGroup, SWT.NONE);
      GridDataFactory.create(label).spanH(2);
      label.setText(ModelMessages.SpinnerModelDialog_hint);
    }
    {
      Label label = new Label(previewGroup, SWT.NONE);
      configureColumn_1(label);
      label.setText(ModelMessages.SpinnerModelDialog_test);
    }
    {
      // two clicks needed to focus AWT component, see:
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6523306
      EmbeddedSwingComposite2 spinnerComposite =
          new EmbeddedSwingComposite2(previewGroup, SWT.NONE) {
            @Override
            protected JComponent createSwingComponent() {
              m_spinner = new JSpinner();
              return m_spinner;
            }
          };
      spinnerComposite.populate();
      GridDataFactory.create(spinnerComposite).grab().fill();
    }
  }

  @Override
  protected void okPressed() {
    try {
      AbstractSpinnerComposite spinnerComposite = getSelectedSpinnerComposite();
      m_source = spinnerComposite.getSource();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      return;
    }
    // close dialog
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() throws Exception {
    AbstractSpinnerComposite spinnerComposite = getSelectedSpinnerComposite();
    // validate
    {
      String message = spinnerComposite.validate();
      if (message != null) {
        m_spinner.setEnabled(false);
        return message;
      }
    }
    // configure spinner
    {
      m_spinner.setEnabled(true);
      m_spinner.setModel(spinnerComposite.getModel());
    }
    // OK
    return null;
  }

  /**
   * @return the selected {@link AbstractSpinnerComposite}.
   */
  private AbstractSpinnerComposite getSelectedSpinnerComposite() {
    int index = m_tabFolder.getSelectionIndex();
    AbstractSpinnerComposite spinnerComposite = m_composites.get(index);
    return spinnerComposite;
  }

  /**
   * Sets the standard {@link GridData} for column <code>1</code> of {@link SpinnerModelDialog}.
   */
  static GridDataFactory configureColumn_1(Control control) {
    return GridDataFactory.create(control).hintHC(15);
  }

  /**
   * Sets the standard {@link GridData} for column <code>2</code> of {@link SpinnerModelDialog}.
   */
  static GridDataFactory configureColumn_2(Control control) {
    return GridDataFactory.create(control).hintHC(20);
  }
}
