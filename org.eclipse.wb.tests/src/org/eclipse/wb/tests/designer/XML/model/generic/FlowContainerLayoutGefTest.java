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

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;

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
		public MyLayout_Info(EditorContext context,
				ComponentDescription description,
				CreationSupport creationSupport) throws Exception {
			super(context, description, creationSupport);
		}

		public void command_CREATE(Object component, Object nextComponent) throws Exception {
			XmlObjectUtils.add(
					(XmlObjectInfo) component,
					Associations.direct(),
					getComposite(),
					(XmlObjectInfo) nextComponent);
		}

		public void command_MOVE(Object component, Object nextComponent) throws Exception {
			XmlObjectUtils.move(
					(XmlObjectInfo) component,
					Associations.direct(),
					getComposite(),
					(XmlObjectInfo) nextComponent);
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
						"    <parameter name='flowContainer.component'>org.eclipse.swt.widgets.Control</parameter>",
						"    <parameter name='flowContainer.reference'>org.eclipse.swt.widgets.Control</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
	}
}
