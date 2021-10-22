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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

/**
 * Tests for "flow container" support for "layout manager", created by
 * {@link ILayoutEditPolicyFactory}.
 *
 * @author scheglov_ke
 */
public class FlowContainerLayoutGefTest extends FlowContainerAbstractGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final class MyLayout_Info extends LayoutInfo {
    public MyLayout_Info(AstEditor editor,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(editor, description, creationSupport);
    }

    public void command_CREATE(Object component, Object nextComponent) throws Exception {
      JavaInfoUtils.add(
          (JavaInfo) component,
          getAssociationObject(),
          getContainer(),
          (JavaInfo) nextComponent);
    }

    public void command_MOVE(Object component, Object nextComponent) throws Exception {
      JavaInfoUtils.move(
          (JavaInfo) component,
          getAssociationObject(),
          getContainer(),
          (JavaInfo) nextComponent);
    }

    private AssociationObject getAssociationObject() {
      return AssociationObjects.invocationChild("%parent%.add(%child%)", true);
    }
  }

  @Override
  protected void prepareFlowPanel() throws Exception {
    FlowContainerModelTest.prepareFlowPanel_classes();
    setFileContentSrc(
        "test/MyLayout.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <model class='" + MyLayout_Info.class.getName() + "'/>",
            "  <parameters>",
            "    <parameter name='flowContainer'>true</parameter>",
            "    <parameter name='flowContainer.horizontal'>true</parameter>",
            "    <parameter name='flowContainer.association'>%parent%.add(%child%)</parameter>",
            "    <parameter name='flowContainer.component'>java.awt.Component</parameter>",
            "    <parameter name='flowContainer.reference'>java.awt.Component</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }
}
