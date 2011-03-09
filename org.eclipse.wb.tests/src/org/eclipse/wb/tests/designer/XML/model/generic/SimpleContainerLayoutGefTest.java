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
package org.eclipse.wb.tests.designer.XML.model.generic;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;

import java.util.List;

/**
 * Tests for "simple container" support, such as {@link SimpleContainer} interface.
 * 
 * @author scheglov_ke
 */
public class SimpleContainerLayoutGefTest extends SimpleContainerAbstractGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final class MyLayout_Info extends LayoutInfo {
    public MyLayout_Info(EditorContext context,
        ComponentDescription description,
        CreationSupport creationSupport) throws Exception {
      super(context, description, creationSupport);
    }

    public List<ObjectInfo> getSimpleContainerChildren() {
      return getComposite().getChildren();
    }

    public void command_CREATE(Object component) throws Exception {
      XmlObjectUtils.add((XmlObjectInfo) component, Associations.direct(), getComposite(), null);
    }

    public void command_ADD(Object component) throws Exception {
      XmlObjectUtils.move((XmlObjectInfo) component, Associations.direct(), getComposite(), null);
    }
  }

  @Override
  protected void prepareSimplePanel() throws Exception {
    SimpleContainerModelTest.prepareSimplePanel_classes();
    setFileContentSrc(
        "test/MyLayout.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <x-model class='" + MyLayout_Info.class.getName() + "'/>",
            "  <parameters>",
            "    <parameter name='simpleContainer'>true</parameter>",
            "    <parameter name='simpleContainer.component'>org.eclipse.swt.widgets.Control</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
  }
}
