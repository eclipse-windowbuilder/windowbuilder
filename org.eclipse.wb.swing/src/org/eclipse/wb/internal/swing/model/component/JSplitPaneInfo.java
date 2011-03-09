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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;

import java.awt.Component;
import java.util.List;

import javax.swing.JSplitPane;

/**
 * Model for {@link JSplitPane}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JSplitPaneInfo extends AbstractPositionContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JSplitPaneInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if position is empty.
   */
  public boolean isEmptyPosition(boolean left) {
    JSplitPane splitPane = (JSplitPane) getObject();
    if (left) {
      Component component = splitPane.getLeftComponent();
      return getChildByObject(component) == null;
    } else {
      Component component = splitPane.getRightComponent();
      return getChildByObject(component) == null;
    }
  }

  /**
   * @return the {@link Rectangle} for left/right position.
   */
  public Rectangle getPositionRectangle(boolean left) {
    JSplitPane splitPane = (JSplitPane) getObject();
    if (left) {
      Component component = splitPane.getLeftComponent();
      if (component != null) {
        return CoordinateUtils.get(component.getBounds());
      }
    } else {
      Component component = splitPane.getRightComponent();
      if (component != null) {
        return CoordinateUtils.get(component.getBounds());
      }
    }
    return new Rectangle();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ComponentInfo} in left/right position.
   */
  public void command_CREATE(ComponentInfo component, boolean left) throws Exception {
    command_CREATE(component, get_setMethodName(left));
  }

  /**
   * Moves child {@link ComponentInfo} to left/right position.
   */
  public void command_MOVE(ComponentInfo component, boolean left) throws Exception {
    command_MOVE(component, get_setMethodName(left));
  }

  /**
   * Reparents {@link ComponentInfo} to left/right position.
   */
  public void command_ADD(ComponentInfo component, boolean left) throws Exception {
    command_ADD(component, get_setMethodName(left));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addCommands(List<ClipboardCommand> commands) throws Exception {
    super.clipboardCopy_addCommands(commands);
    {
      JSplitPane splitPane = (JSplitPane) getObject();
      clipboard_addComponent(commands, splitPane.getLeftComponent(), true);
      clipboard_addComponent(commands, splitPane.getRightComponent(), false);
    }
  }

  private void clipboard_addComponent(List<ClipboardCommand> commands,
      Component componentObject,
      final boolean left) throws Exception {
    ComponentInfo component = (ComponentInfo) getChildByObject(componentObject);
    if (component != null) {
      commands.add(new ContainerClipboardCommand<JSplitPaneInfo>(component) {
        private static final long serialVersionUID = 0L;

        @Override
        protected void add(JSplitPaneInfo container, ComponentInfo component) throws Exception {
          container.command_CREATE(component, left);
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
   * @return the name of "set" method for left/right position.
   */
  private String get_setMethodName(boolean left) {
    return left ? "setLeftComponent" : "setRightComponent";
  }
}
