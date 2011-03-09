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
package org.eclipse.wb.internal.swing.model.util.surround;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;

import org.eclipse.swt.graphics.Image;

import javax.swing.JTabbedPane;

/**
 * {@link ISurroundTarget} that uses {@link JTabbedPane} as target container.
 * 
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public final class JTabbedPaneSurroundTarget
    extends
      ISurroundTarget<JTabbedPaneInfo, ComponentInfo> {
  private static final String CLASS_NAME = "javax.swing.JTabbedPane";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon(AstEditor editor) throws Exception {
    return ComponentDescriptionHelper.getDescription(editor, CLASS_NAME).getIcon();
  }

  @Override
  public String getText(AstEditor editor) throws Exception {
    return CLASS_NAME;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public JTabbedPaneInfo createContainer(AstEditor editor) throws Exception {
    return (JTabbedPaneInfo) JavaInfoUtils.createJavaInfo(
        editor,
        CLASS_NAME,
        new ConstructorCreationSupport());
  }

  @Override
  public void move(JTabbedPaneInfo container, ComponentInfo component) throws Exception {
    container.command_ADD(component, null);
  }
}
