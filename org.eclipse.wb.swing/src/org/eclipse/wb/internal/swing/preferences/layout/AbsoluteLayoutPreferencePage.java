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
package org.eclipse.wb.internal.swing.preferences.layout;

import org.eclipse.wb.core.controls.jface.preference.FieldLayoutPreferencePage;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.swing.preferences.Messages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Main {@link PreferencePage} for Swing Absolute Layout Support.
 * 
 * @author mitin_aa
 * @coverage swing.preferences.ui
 */
public final class AbsoluteLayoutPreferencePage extends FieldLayoutPreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  /**
   * @return The {@link IPreferenceStore} of Swing Toolkit Support plugin
   */
  @Override
  public IPreferenceStore getPreferenceStore() {
    return getToolkit().getPreferences();
  }

  /**
   * @return the {@link ToolkitDescription}.
   */
  private ToolkitDescription getToolkit() {
    return ToolkitProvider.DESCRIPTION;
  }

  @Override
  protected Control createPageContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridLayoutFactory.create(container);
    createBooleanFieldEditor(
        P_USE_FREE_MODE,
        Messages.AbsoluteLayoutPreferencePage_useFreeStyle,
        container);
    createBooleanFieldEditor(
        P_USE_JDK_LAYOUT_STYLE,
        Messages.AbsoluteLayoutPreferencePage_useLayoutStyle,
        container);
    {
      final Composite freeStylePropertiesComposite = new Composite(container, SWT.NONE);
      GridDataFactory.create(freeStylePropertiesComposite).fillH().alignVM();
      GridLayoutFactory.create(freeStylePropertiesComposite).marginsH(0).columns(3);
      {
        final Group componentGapsGroup = new Group(freeStylePropertiesComposite, SWT.NONE);
        GridLayoutFactory.create(componentGapsGroup);
        componentGapsGroup.setText(Messages.AbsoluteLayoutPreferencePage_componentGapsGroup);
        createIntegerFieldEditor(
            P_COMPONENT_GAP_LEFT,
            Messages.AbsoluteLayoutPreferencePage_left,
            componentGapsGroup,
            true);
        createIntegerFieldEditor(
            P_COMPONENT_GAP_RIGHT,
            Messages.AbsoluteLayoutPreferencePage_right,
            componentGapsGroup,
            true);
        createIntegerFieldEditor(
            P_COMPONENT_GAP_TOP,
            Messages.AbsoluteLayoutPreferencePage_top,
            componentGapsGroup,
            true);
        createIntegerFieldEditor(
            P_COMPONENT_GAP_BOTTOM,
            Messages.AbsoluteLayoutPreferencePage_bottom,
            componentGapsGroup,
            true);
      }
      {
        final Group containerGapsGroup = new Group(freeStylePropertiesComposite, SWT.NONE);
        GridLayoutFactory.create(containerGapsGroup);
        containerGapsGroup.setText(Messages.AbsoluteLayoutPreferencePage_containerGapsGroup);
        createIntegerFieldEditor(
            P_CONTAINER_GAP_LEFT,
            Messages.AbsoluteLayoutPreferencePage_left,
            containerGapsGroup,
            true);
        createIntegerFieldEditor(
            P_CONTAINER_GAP_RIGHT,
            Messages.AbsoluteLayoutPreferencePage_right,
            containerGapsGroup,
            true);
        createIntegerFieldEditor(
            P_CONTAINER_GAP_TOP,
            Messages.AbsoluteLayoutPreferencePage_top,
            containerGapsGroup,
            true);
        createIntegerFieldEditor(
            P_CONTAINER_GAP_BOTTOM,
            Messages.AbsoluteLayoutPreferencePage_bottom,
            containerGapsGroup,
            true);
      }
    }
    createBooleanFieldEditor(
        P_CREATION_FLOW,
        Messages.AbsoluteLayoutPreferencePage_applyGridFlow,
        container);
    createBooleanFieldEditor(
        P_USE_GRID,
        Messages.AbsoluteLayoutPreferencePage_useGridSnapping,
        container);
    {
      final Group gridGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(gridGroup).fillH().alignVM();
      gridGroup.setText(Messages.AbsoluteLayoutPreferencePage_gridGroup);
      GridLayoutFactory.create(gridGroup);
      createIntegerFieldEditor(
          P_GRID_STEP_X,
          Messages.AbsoluteLayoutPreferencePage_stepX,
          gridGroup,
          false);
      createIntegerFieldEditor(
          P_GRID_STEP_Y,
          Messages.AbsoluteLayoutPreferencePage_stepY,
          gridGroup,
          false);
      createBooleanFieldEditor(
          P_DISPLAY_GRID,
          Messages.AbsoluteLayoutPreferencePage_displayGrid,
          gridGroup);
    }
    createBooleanFieldEditor(
        P_DISPLAY_LOCATION_SIZE_HINTS,
        Messages.AbsoluteLayoutPreferencePage_displayHints,
        container);
    return container;
  }

  /**
   * Helper method Create {@link #BooleanFieldEditor} within own composite on specified parent
   */
  private void createBooleanFieldEditor(final String key, final String text, final Composite parent) {
    final Composite composite = new Composite(parent, SWT.NONE);
    addField(new BooleanFieldEditor(key, text, composite));
  }

  /**
   * Helper method Create {@link #IntegerFieldEditor} within own composite on specified parent
   * 
   * @param alignedRight
   *          if this parameter is true then align filed editor to the right
   */
  private void createIntegerFieldEditor(final String key,
      final String text,
      final Composite parent,
      boolean alignedRight) {
    final Composite composite = new Composite(parent, SWT.NONE);
    if (alignedRight) {
      GridDataFactory.create(composite).grabH().alignHR().alignVM();
    }
    // set min width of text control to 40
    IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(key, text, composite);
    Text textControl = integerFieldEditor.getTextControl(composite);
    GridDataFactory.modify(textControl).hintHC(5);
    addField(integerFieldEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //	IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}
