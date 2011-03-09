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
package org.eclipse.wb.internal.rcp.model.jface.viewers;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;

import org.eclipse.jface.viewers.ComboViewer;

/**
 * Model for any JFace {@link org.eclipse.jface.viewers.ComboViewer}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.model.jface.viewers
 */
public final class ComboViewerInfo extends ViewerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboViewerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    // tweak for Control method
    {
      ComboViewer viewer = (ComboViewer) object;
      if (viewer.getControl() instanceof org.eclipse.swt.custom.CCombo) {
        getWrapper().setControlMethodName("getCCombo");
      }
    }
    // continue
    super.setObject(object);
  }
}
