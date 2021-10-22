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
package org.eclipse.wb.internal.rcp.model.layout.grid;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Model for {@link GridLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public final class GridLayoutInfo
    extends
      org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    replaceGridLayout();
    super.refresh_afterCreate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replace
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces standard {@link GridLayout} and {@link GridData} with our {@link GridLayout2} and
   * {@link GridData2}.
   */
  private void replaceGridLayout() throws Exception {
    Composite composite = (Composite) getComposite().getObject();
    // update GridLayout
    {
      GridLayout2 newGridLayout = GridLayout2.replaceGridLayout(composite);
      setObject(newGridLayout);
    }
    // force layout() to recalculate "design" fields
    composite.layout();
    // update GridDataInfo's
    for (ControlInfo controlInfo : getControls()) {
      Control control = (Control) controlInfo.getObject();
      GridData2 gridDataObject = GridLayout2.getLayoutData2(control);
      if (gridDataObject != null) {
        GridDataInfo gridDataInfo = getGridData(controlInfo);
        gridDataInfo.setObject(gridDataObject);
      }
    }
  }
}
