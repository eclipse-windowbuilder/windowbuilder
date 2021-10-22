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
package org.eclipse.wb.internal.swt.model.jface;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.WrapperMethodInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.model.util.ExposeComponentSupport;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Model for SWT wrappers.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage swt.model.jface
 */
public class WrapperInfo extends WrapperMethodInfo {
  private final WrapperInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperInfo(AstEditor editor,
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
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          ExposeComponentSupport.contribute(m_this, manager, ModelMessages.WrapperInfo_exposeViewer);
          RenameConvertSupport.contribute(objects, manager);
        }
      }
    });
    StylePropertyEditor.configureContributeActions(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    StylePropertyEditor.addStyleProperties(properties);
    return properties;
  }
}
