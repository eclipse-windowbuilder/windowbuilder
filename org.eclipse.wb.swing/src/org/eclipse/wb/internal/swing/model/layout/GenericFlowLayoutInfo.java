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

import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.List;

/**
 * Model for "flow based" {@link LayoutManager}, such as {@link FlowLayout}, {@link GridLayout}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public abstract class GenericFlowLayoutInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericFlowLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link ComponentInfo}.
   */
  public final void add(ComponentInfo componentInfo, ComponentInfo nextComponentInfo)
      throws Exception {
    add(componentInfo, null, nextComponentInfo);
  }

  /**
   * Moves given {@link ComponentInfo}.
   */
  public final void move(ComponentInfo componentInfo, ComponentInfo nextComponentInfo)
      throws Exception {
    move(componentInfo, null, nextComponentInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addComponentCommands(ComponentInfo component,
      List<ClipboardCommand> commands) throws Exception {
    super.clipboardCopy_addComponentCommands(component, commands);
    commands.add(new LayoutClipboardCommand<GenericFlowLayoutInfo>(component) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(GenericFlowLayoutInfo layout, ComponentInfo component) throws Exception {
        layout.add(component, null);
      }
    });
  }
}
