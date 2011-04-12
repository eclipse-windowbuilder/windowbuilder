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
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;

/**
 * Layout assistant for {@link ColumnLayoutData}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class ColumnLayoutDataAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnLayoutDataAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).columns(2);
    // width/height hints
    {
      Group group =
          addIntegerProperties(
              this,
              ModelMessages.ColumnLayoutDataAssistantPage_sizeGroup,
              new String[][]{
                  {"widthHint", ModelMessages.ColumnLayoutDataAssistantPage_widthHint},
                  {"heightHint", ModelMessages.ColumnLayoutDataAssistantPage_heightHint}},
              new int[]{SWT.DEFAULT, SWT.DEFAULT});
      GridDataFactory.create(group).fillV();
    }
    // horizontal alignment
    {
      Group orientationGroup =
          addChoiceProperty(
              this,
              "horizontalAlignment",
              ModelMessages.ColumnLayoutDataAssistantPage_alignmentGroup,
              new Object[][]{
                  new Object[]{
                      ModelMessages.ColumnLayoutDataAssistantPage_alignmentLeft,
                      ColumnLayoutData.LEFT},
                  new Object[]{
                      ModelMessages.ColumnLayoutDataAssistantPage_alignmentCenter,
                      ColumnLayoutData.CENTER},
                  new Object[]{
                      ModelMessages.ColumnLayoutDataAssistantPage_alignmentRight,
                      ColumnLayoutData.RIGHT},
                  new Object[]{
                      ModelMessages.ColumnLayoutDataAssistantPage_alignmentFill,
                      ColumnLayoutData.FILL},});
      GridDataFactory.create(orientationGroup).fillV();
    }
  }
}