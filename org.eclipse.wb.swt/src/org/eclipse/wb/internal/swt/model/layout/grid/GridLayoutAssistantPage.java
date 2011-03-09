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
import org.eclipse.swt.widgets.Label;

/**
 * Layout assistant for {@link org.eclipse.swt.layout.GridLayout}.
 * 
 * @author lobas_av
 * @coverage swt.assistant
 */
public final class GridLayoutAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridLayoutAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
    GridLayoutFactory.create(this).spacingV(1).noMargins();
    {
      Composite topComposite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(topComposite).columns(4);
      GridDataFactory.create(topComposite).fillH().grabH();
      // columns properties
      {
        addIntegerProperty(topComposite, "numColumns", "Number of columns:", 1);
        new Label(topComposite, SWT.NONE).setText(" ");
        addBooleanProperty(topComposite, "makeColumnsEqualWidth", "Make columns equal width");
      }
    }
    {
      Composite groupComposite = new Composite(this, SWT.NONE);
      GridLayoutFactory.create(groupComposite).columns(2);
      GridDataFactory.create(groupComposite).fill().grab();
      // margin for sides
      {
        Group group =
            addIntegerProperties(groupComposite, "Margins for sides", new String[][]{
                new String[]{"marginLeft", "Left:"},
                new String[]{"marginTop", "Top:"},
                new String[]{"marginRight", "Right:"},
                new String[]{"marginBottom", "Bottom:"}});
        GridDataFactory.create(group).fillV().spanV(2);
      }
      // margins
      {
        Group group =
            addIntegerProperties(groupComposite, "Margins", new String[][]{
                new String[]{"marginWidth", "Margin Width:"},
                new String[]{"marginHeight", "Margin Height:"}});
        GridDataFactory.create(group).fillH().grabH();
      }
      // spacing
      {
        Group group =
            addIntegerProperties(groupComposite, "Spacing", new String[][]{
                new String[]{"horizontalSpacing", "Horizontal spacing:"},
                new String[]{"verticalSpacing", "Vertical spacing:"}});
        GridDataFactory.create(group).fillH().grabH();
      }
    }
  }
}