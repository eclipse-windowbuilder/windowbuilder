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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.controls.Separator;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionTemplate;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeConstantInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ModelMessages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.jgoodies.forms.layout.Size;
import com.jgoodies.forms.layout.Sizes;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * The dialog for editing {@link FormDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
abstract class DimensionEditDialog<T extends FormDimensionInfo> extends ResizableDialog {
  private final FormLayoutInfo m_layout;
  private final List<T> m_dimensions;
  private T m_currentDimension;
  private T m_dimension;
  private final boolean m_horizontal;
  private final String m_dimensionName;
  private final DefaultAlignmentDescription[] m_alignments;
  private final UnitDescription[] m_units;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionEditDialog(Shell parentShell,
      FormLayoutInfo layout,
      List<T> dimensions,
      T dimension,
      String dimensionName,
      DefaultAlignmentDescription[] alignments,
      UnitDescription[] units) {
    super(parentShell, Activator.getDefault());
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    m_layout = layout;
    m_dimensions = dimensions;
    setEditDimension(dimension);
    m_horizontal = dimension instanceof FormColumnInfo;
    //
    m_dimensionName = dimensionName;
    m_alignments = alignments;
    m_units = units;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_indexText;
  private Button m_prevButton;
  private Button m_nextButton;
  //
  private Text m_specificationText;
  private Button[] m_alignmentButtons;
  private Combo m_templateCombo;
  private final Map<Size, Button> m_componentSizeToButton = Maps.newHashMap();
  // constant
  private ConstantSizeComposite m_constantSizeComposite;
  private Button m_constantSizeButton;
  // lower bound
  private Button m_lowerSizeButton;
  private ConstantSizeComposite m_lowerSizeComposite;
  // lower bound
  private Button m_upperSizeButton;
  private ConstantSizeComposite m_upperSizeComposite;
  // grow
  private Button m_noGrowButton;
  private Button m_growButton;
  private Spinner m_growSpinner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    createHeaderComposite(container);
    createAlignmentComposite(container);
    createSizeComposite(container);
    createResizeComposite(container);
    //
    showDimension();
    return container;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(MessageFormat.format(
        ModelMessages.DimensionEditDialog_dialogTitle,
        m_dimensionName));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int APPLY_ID = IDialogConstants.CLIENT_ID + 1;

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    if (m_layout != null) {
      createButton(parent, APPLY_ID, ModelMessages.DimensionEditDialog_applyButton, false);
    }
  }

  @Override
  protected void buttonPressed(int buttonId) {
    try {
      if (buttonId == IDialogConstants.OK_ID || buttonId == APPLY_ID) {
        applyChanges();
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    super.buttonPressed(buttonId);
  }

  /**
   * Saves current {@link FormLayoutInfo} changes into source and refreshes GUI.
   */
  private void applyChanges() throws Exception {
    m_currentDimension.assign(m_dimension);
    if (m_layout != null) {
      try {
        m_layout.startEdit();
        m_layout.writeDimensions();
      } finally {
        m_layout.endEdit();
      }
    }
  }

  /**
   * Sets the {@link FormDimensionInfo} to edit.
   */
  private void setEditDimension(T dimension) {
    try {
      // apply changes
      if (m_currentDimension != null && !m_currentDimension.equals2(m_dimension)) {
        // open dialog
        int dialogResult;
        {
          String title = ModelMessages.DimensionEditDialog_applyConfirmTitle;
          String message =
              MessageFormat.format(
                  ModelMessages.DimensionEditDialog_applyConfirmMessage,
                  m_dimensionName);
          MessageDialog dialog =
              new MessageDialog(getShell(),
                  title,
                  null,
                  message,
                  MessageDialog.QUESTION,
                  new String[]{
                      IDialogConstants.YES_LABEL,
                      IDialogConstants.NO_LABEL,
                      IDialogConstants.CANCEL_LABEL},
                  0);
          dialogResult = dialog.open();
        }
        // check cancel/yes
        if (dialogResult == 2) {
          return;
        } else if (dialogResult == 0) {
          applyChanges();
        }
      }
      // remember new dimension
      m_currentDimension = dimension;
      m_dimension = getCopy(dimension);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * @return the deep copy of {@link FormDimensionInfo}.
   */
  @SuppressWarnings("unchecked")
  private static <T extends FormDimensionInfo> T getCopy(T dimension) throws Exception {
    return (T) dimension.copy();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composites
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create the header displaying {@link Composite}.
   */
  private void createHeaderComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill();
    GridLayoutFactory.create(composite).noMargins().columns(4);
    // index
    {
      new Label(composite, SWT.NONE).setText(m_dimensionName + ":");
      // index
      {
        m_indexText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.create(m_indexText).grabH().fillH().indentHC(3);
      }
      // prev button
      {
        m_prevButton = new Button(composite, SWT.NONE);
        m_prevButton.setToolTipText(MessageFormat.format(
            ModelMessages.DimensionEditDialog_previousButton,
            m_dimensionName));
        m_prevButton.setImage(m_horizontal
            ? Activator.getImage("navigation/left.gif")
            : Activator.getImage("navigation/up.gif"));
        m_prevButton.addListener(SWT.Selection, new Listener() {
          @Override
          public void handleEvent(Event event) {
            int index = m_dimensions.indexOf(m_currentDimension);
            setEditDimension(m_dimensions.get(index - 1));
            showDimension();
          }
        });
      }
      // next button
      {
        m_nextButton = new Button(composite, SWT.NONE);
        m_nextButton.setToolTipText(MessageFormat.format(
            ModelMessages.DimensionEditDialog_nextButton,
            m_dimensionName));
        m_nextButton.setImage(m_horizontal
            ? Activator.getImage("navigation/right.gif")
            : Activator.getImage("navigation/down.gif"));
        m_nextButton.addListener(SWT.Selection, new Listener() {
          @Override
          public void handleEvent(Event event) {
            int index = m_dimensions.indexOf(m_currentDimension);
            setEditDimension(m_dimensions.get(index + 1));
            showDimension();
          }
        });
      }
    }
    // template
    {
      new Label(composite, SWT.NONE).setText(ModelMessages.DimensionEditDialog_template);
      //
      m_templateCombo = new Combo(composite, SWT.READ_ONLY);
      GridDataFactory.create(m_templateCombo).spanH(3).hintHC(40).grabH().fillH().indentHC(3);
      // fill templates
      final FormDimensionTemplate[] templates = m_dimension.getTemplates();
      m_templateCombo.setVisibleItemCount(templates.length);
      for (int i = 0; i < templates.length; i++) {
        FormDimensionTemplate template = templates[i];
        m_templateCombo.add(template.getTitle());
      }
      // add selection listener
      m_templateCombo.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          try {
            int index = m_templateCombo.getSelectionIndex();
            FormDimensionTemplate template = templates[index];
            m_dimension.setTemplate(template);
            //
            showDimension();
          } catch (Throwable e) {
          }
        }
      });
    }
    // specification
    {
      new Label(composite, SWT.NONE).setText(ModelMessages.DimensionEditDialog_specification);
      //
      m_specificationText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
      GridDataFactory.create(m_specificationText).spanH(3).grabH().fillH().indentHC(3);
    }
  }

  /**
   * Creates the alignment editing {@link Composite}.
   */
  private void createAlignmentComposite(Composite parent) {
    createSeparator(parent, ModelMessages.DimensionEditDialog_defaultAlignment);
    //
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill().indentHC(2);
    GridLayoutFactory.create(composite).noMargins().columns(m_alignments.length);
    //
    m_alignmentButtons = new Button[m_alignments.length];
    for (int i = 0; i < m_alignments.length; i++) {
      final DefaultAlignmentDescription description = m_alignments[i];
      // create radio button
      Button button = new Button(composite, SWT.RADIO);
      button.setText(description.getTitle());
      // add listener
      m_alignmentButtons[i] = button;
      button.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          m_dimension.setAlignment(description.getAlignment());
          showDimension();
        }
      });
    }
  }

  /**
   * Creates the size editing {@link Composite}.
   */
  private void createSizeComposite(Composite parent) {
    createSeparator(parent, ModelMessages.DimensionEditDialog_size);
    //
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill().indentHC(2);
    GridLayoutFactory.create(composite).noMargins();
    // default, preferred, minimum
    {
      Composite sizesComposite = new Composite(composite, SWT.NONE);
      GridDataFactory.create(sizesComposite).grabH().fill();
      GridLayoutFactory.create(sizesComposite).columns(3).noMargins();
      createComponentSizeButton(sizesComposite, Sizes.DEFAULT, "&default");
      createComponentSizeButton(sizesComposite, Sizes.PREFERRED, "&preferred");
      createComponentSizeButton(sizesComposite, Sizes.MINIMUM, "minim&um");
    }
    // constants
    {
      Composite constantsComposite = new Composite(composite, SWT.NONE);
      GridDataFactory.create(constantsComposite).grabH().fill();
      GridLayoutFactory.create(constantsComposite).columns(2).noMargins();
      // constant size
      {
        {
          m_constantSizeButton = new Button(constantsComposite, SWT.RADIO);
          m_constantSizeButton.setText(ModelMessages.DimensionEditDialog_18);
          GridDataFactory.create(m_constantSizeButton).hintHC(15);
          m_constantSizeButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
              FormSizeInfo size = m_dimension.getSize();
              size.setComponentSize(null);
              if (size.getConstantSize() == null) {
                size.setConstantSize(new FormSizeConstantInfo(50, m_units[0].getUnit()));
              }
              showDimension();
            }
          });
        }
        //
        m_constantSizeComposite = new ConstantSizeComposite(constantsComposite, SWT.NONE, m_units);
        m_constantSizeComposite.addListener(SWT.Selection, new Listener() {
          @Override
          public void handleEvent(Event event) {
            FormSizeInfo size = m_dimension.getSize();
            size.setConstantSize(m_constantSizeComposite.getConstantSize());
            showDimension();
          }
        });
      }
      // lower
      {
        {
          m_lowerSizeButton = new Button(constantsComposite, SWT.CHECK);
          m_lowerSizeButton.setText(ModelMessages.DimensionEditDialog_19);
          m_lowerSizeButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
              FormSizeInfo size = m_dimension.getSize();
              size.setLowerSize(m_lowerSizeButton.getSelection());
              if (size.getLowerSize() == null) {
                size.setLowerSize(new FormSizeConstantInfo(50, m_units[0].getUnit()));
              }
              showDimension();
            }
          });
        }
        //
        m_lowerSizeComposite = new ConstantSizeComposite(constantsComposite, SWT.NONE, m_units);
        m_lowerSizeComposite.addListener(SWT.Selection, new Listener() {
          @Override
          public void handleEvent(Event event) {
            FormSizeInfo size = m_dimension.getSize();
            size.setLowerSize(m_lowerSizeComposite.getConstantSize());
            showDimension();
          }
        });
      }
      // upper
      {
        {
          m_upperSizeButton = new Button(constantsComposite, SWT.CHECK);
          m_upperSizeButton.setText(ModelMessages.DimensionEditDialog_20);
          m_upperSizeButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
              FormSizeInfo size = m_dimension.getSize();
              size.setUpperSize(m_upperSizeButton.getSelection());
              if (size.getUpperSize() == null) {
                size.setUpperSize(new FormSizeConstantInfo(50, m_units[0].getUnit()));
              }
              showDimension();
            }
          });
        }
        //
        m_upperSizeComposite = new ConstantSizeComposite(constantsComposite, SWT.NONE, m_units);
        m_upperSizeComposite.addListener(SWT.Selection, new Listener() {
          @Override
          public void handleEvent(Event event) {
            FormSizeInfo size = m_dimension.getSize();
            size.setUpperSize(m_upperSizeComposite.getConstantSize());
            showDimension();
          }
        });
      }
    }
  }

  /**
   * Creates single radio button for given component size.
   */
  private void createComponentSizeButton(Composite parent, final Size componentSize, String text) {
    Button button = new Button(parent, SWT.RADIO);
    m_componentSizeToButton.put(componentSize, button);
    button.setText(text);
    button.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        m_dimension.getSize().setComponentSize(componentSize);
        showDimension();
      }
    });
  }

  /**
   * Creates the resize behavior editing {@link Composite}.
   */
  private void createResizeComposite(Composite parent) {
    createSeparator(parent, ModelMessages.DimensionEditDialog_resizeBehavior);
    //
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).grabH().fill().indentHC(2);
    GridLayoutFactory.create(composite).noMargins().columns(2);
    // none
    {
      m_noGrowButton = new Button(composite, SWT.RADIO);
      GridDataFactory.create(m_noGrowButton).spanH(2);
      m_noGrowButton.setText("&none");
      m_noGrowButton.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          m_dimension.setWeight(0.0);
          showDimension();
        }
      });
    }
    // grow
    {
      m_growButton = new Button(composite, SWT.RADIO);
      GridDataFactory.create(m_growButton).hintHC(15);
      m_growButton.setText("&grow");
      m_growButton.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          m_dimension.setWeight(1.0);
          showDimension();
        }
      });
      //
      m_growSpinner = new Spinner(composite, SWT.BORDER);
      GridDataFactory.create(m_growSpinner).hintHC(10);
      m_growSpinner.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          m_dimension.setWeight(m_growSpinner.getSelection());
          showDimension();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows the {@link FormDimensionInfo} in UI controls.
   */
  private void showDimension() {
    // header
    {
      // index
      {
        int index = 1 + m_dimensions.indexOf(m_currentDimension);
        m_indexText.setText("" + index);
        m_prevButton.setEnabled(index != 1);
        m_nextButton.setEnabled(index < m_dimensions.size());
      }
      // template
      {
        FormDimensionTemplate[] templates = m_dimension.getTemplates();
        m_templateCombo.deselectAll();
        for (int i = 0; i < templates.length; i++) {
          FormDimensionTemplate template = templates[i];
          if (m_dimension.isTemplate(template)) {
            m_templateCombo.select(i);
            break;
          }
        }
      }
      // specification
      m_specificationText.setText(m_dimension.getDisplayString());
    }
    // alignment
    for (int i = 0; i < m_alignments.length; i++) {
      DefaultAlignmentDescription description = m_alignments[i];
      Button button = m_alignmentButtons[i];
      button.setSelection(m_dimension.getAlignment() == description.getAlignment());
    }
    // size
    {
      FormSizeInfo size = m_dimension.getSize();
      boolean hasConstantSize = size.getComponentSize() == null;
      // component size
      for (Map.Entry<Size, Button> entry : m_componentSizeToButton.entrySet()) {
        Size componentSize = entry.getKey();
        Button button = entry.getValue();
        button.setSelection(componentSize == size.getComponentSize());
      }
      // constant size
      {
        UiUtils.changeControlEnable(m_constantSizeComposite, hasConstantSize);
        m_constantSizeButton.setSelection(hasConstantSize);
        m_constantSizeComposite.setConstantSize(size.getConstantSize());
      }
      // lower
      {
        m_lowerSizeButton.setEnabled(!hasConstantSize);
        m_lowerSizeButton.setSelection(size.hasLowerSize());
        UiUtils.changeControlEnable(m_lowerSizeComposite, size.hasLowerSize());
        m_lowerSizeComposite.setConstantSize(size.getLowerSize());
      }
      // upper
      {
        m_upperSizeButton.setEnabled(!hasConstantSize);
        m_upperSizeButton.setSelection(size.hasUpperSize());
        UiUtils.changeControlEnable(m_upperSizeComposite, size.hasUpperSize());
        m_upperSizeComposite.setConstantSize(size.getUpperSize());
      }
    }
    // grow
    if (m_dimension.hasGrow()) {
      m_noGrowButton.setSelection(false);
      m_growButton.setSelection(true);
      //
      m_growSpinner.setEnabled(true);
      int weight = Math.max((int) m_dimension.getWeight(), 1);
      if (m_growSpinner.getSelection() != weight) {
        m_growSpinner.setSelection(weight);
      }
    } else {
      m_noGrowButton.setSelection(true);
      m_growButton.setSelection(false);
      //
      m_growSpinner.setEnabled(false);
      m_growSpinner.setSelection(0);
    }
  }

  /**
   * Creates separator with given text.
   */
  private static void createSeparator(Composite parent, String text) {
    Separator separator = new Separator(parent, SWT.NONE);
    GridDataFactory.create(separator).grabH().fillH();
    separator.setText(text);
    separator.setForeground(separator.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
  }
}
