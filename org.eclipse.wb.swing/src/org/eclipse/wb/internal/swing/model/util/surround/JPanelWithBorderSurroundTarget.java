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
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.swt.graphics.Image;

import java.util.List;

import javax.swing.JPanel;

/**
 * {@link ISurroundTarget} that uses {@link JPanel} as target container.
 * 
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public final class JPanelWithBorderSurroundTarget
    extends
      ISurroundTarget<ContainerInfo, ComponentInfo> {
  private static final String CLASS_NAME = "javax.swing.JPanel";

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
    return CLASS_NAME + " (border)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ContainerInfo createContainer(AstEditor editor) throws Exception {
    return (ContainerInfo) JavaInfoUtils.createJavaInfo(
        editor,
        CLASS_NAME,
        new ConstructorCreationSupport());
  }

  @Override
  public void afterContainerAdd(ContainerInfo container, List<ComponentInfo> components)
      throws Exception {
    container.addMethodInvocation(
        "setBorder(javax.swing.border.Border)",
        "new javax.swing.border.TitledBorder(null, "
            + "\"JPanel title\", "
            + "javax.swing.border.TitledBorder.LEADING, "
            + "javax.swing.border.TitledBorder.TOP, "
            + "null, null)");
  }

  @Override
  public void move(ContainerInfo container, ComponentInfo component) throws Exception {
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) container.getLayout();
    flowLayout.move(component, null);
  }
}
