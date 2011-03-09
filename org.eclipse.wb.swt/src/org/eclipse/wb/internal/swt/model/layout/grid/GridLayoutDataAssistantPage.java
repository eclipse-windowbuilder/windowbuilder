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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.GridData}.
 * 
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class GridLayoutDataAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridLayoutDataAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(3);
    {
      Group composite = new Group(this, SWT.NONE);
      composite.setText("Alignment");
      GridLayoutFactory.create(composite).columns(2);
      GridDataFactory.create(composite).fill().grab().spanH(2).spanV(2);
      // Horizontal alignment & grab
      {
        Group horizontalOrientationGroup =
            addChoiceProperty(composite, "horizontalAlignment", "Horizontal", new Object[][]{
                new Object[]{"Left", SWT.LEFT},
                new Object[]{"Center", SWT.CENTER},
                new Object[]{"Right", SWT.RIGHT},
                new Object[]{"Fill", SWT.FILL}});
        //
        addBooleanProperty(horizontalOrientationGroup, "grabExcessHorizontalSpace", "Grab");
        GridDataFactory.create(horizontalOrientationGroup).alignHC().fillV().grab();
      }
      // Vertical alignment & grab
      {
        Group verticalOrientationGroup =
            addChoiceProperty(composite, "verticalAlignment", "Vertical", new Object[][]{
                new Object[]{"Top", SWT.TOP},
                new Object[]{"Center", SWT.CENTER},
                new Object[]{"Bottom", SWT.BOTTOM},
                new Object[]{"Fill", SWT.FILL}});
        //
        addBooleanProperty(verticalOrientationGroup, "grabExcessVerticalSpace", "Grab");
        GridDataFactory.create(verticalOrientationGroup).alignHC().fillV().grab();
      }
    }
    // Hints
    {
      Group group =
          addIntegerProperties(this, "Hints", new String[][]{
              {"widthHint", "Width Hint:"},
              {"heightHint", "Height Hint:"}}, new int[]{SWT.DEFAULT, SWT.DEFAULT});
      GridDataFactory.create(group).fillH().fillV();
    }
    // Spanning
    {
      Group group =
          addIntegerProperties(this, "Spanning", new String[][]{
              {"horizontalSpan", "Column Span:"},
              {"verticalSpan", "Row Span:"}}, new int[]{1, 1});
      GridDataFactory.create(group).fillH().fillV();
    }
    // Minimum
    {
      addIntegerProperties(this, "Minimum", new String[][]{
          {"minimumWidth", "Width:"},
          {"minimumHeight", "Height:"}});
    }
    // Exclude
    {
      addBooleanProperty(this, "exclude", "Exclude");
    }
    // Indents
    {
      Group group =
          addIntegerProperties(this, "Indents", new String[][]{
              {"horizontalIndent", "hIndent:"},
              {"verticalIndent", "vIndent:"}});
      GridDataFactory.create(group).fillH().fillV();
    }
  }
}