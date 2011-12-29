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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.HorizontalAlignment;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.VerticalAlignment;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.swt.widgets.Composite;

import java.awt.GridLayout;
import java.util.List;

/**
 * Model for {@link GridLayout}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class GridLayoutInfo extends GenericFlowLayoutInfo {
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
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    // GridLayout uses "columns" only when "rows == 0"
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void setPropertyExpression(GenericPropertyImpl property,
          String[] source,
          Object[] value,
          boolean[] shouldSet) throws Exception {
        if (property.getJavaInfo() == GridLayoutInfo.this && property.getTitle().equals("columns")) {
          getPropertyByTitle("rows").setValue(0);
        }
      }
    });
    // alignment support
    new LayoutAssistantSupport(this) {
      @Override
      protected AbstractAssistantPage createLayoutPage(Composite parent) {
        return new GridLayoutAssistantPage(parent, m_layout);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void onSet() throws Exception {
    super.onSet();
    GridLayoutConverter.convert(getContainer(), this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage general layout data. 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void storeLayoutData(ComponentInfo component) throws Exception {
    if (isManagedObject(component)) {
      GeneralLayoutData generalLayoutData = new GeneralLayoutData();
      {
        // calculate cell
        List<ComponentInfo> components = getComponents();
        int rowCount = (Integer) ReflectionUtils.invokeMethod(getObject(), "getRows()");
        int colCount;
        if (rowCount > 0) {
          colCount = (components.size() - 1) / rowCount + 1;
        } else {
          colCount = (Integer) ReflectionUtils.invokeMethod(getObject(), "getColumns()");
        }
        int index = components.indexOf(component);
        generalLayoutData.gridX = index % colCount;
        generalLayoutData.gridY = index / colCount;
      }
      generalLayoutData.spanX = 1;
      generalLayoutData.spanY = 1;
      generalLayoutData.horizontalGrab = null;
      generalLayoutData.verticalGrab = null;
      // alignments
      generalLayoutData.horizontalAlignment = HorizontalAlignment.FILL;
      generalLayoutData.verticalAlignment = VerticalAlignment.FILL;
      generalLayoutData.putToInfo(component);
    }
  }
}
