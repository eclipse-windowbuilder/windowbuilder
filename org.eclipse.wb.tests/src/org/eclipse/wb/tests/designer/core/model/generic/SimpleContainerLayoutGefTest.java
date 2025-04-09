/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

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
		public MyLayout_Info(AstEditor editor,
				ComponentDescription description,
				CreationSupport creationSupport) throws Exception {
			super(editor, description, creationSupport);
		}

		public List<ObjectInfo> getSimpleContainerChildren() {
			return getContainer().getChildren();
		}

		public void command_CREATE(Object component) throws Exception {
			JavaInfoUtils.add((JavaInfo) component, getAssociationObject(), getContainer(), null);
		}

		public void command_ADD(Object component) throws Exception {
			JavaInfoUtils.move((JavaInfo) component, getAssociationObject(), getContainer(), null);
		}

		private AssociationObject getAssociationObject() {
			return AssociationObjects.invocationChild("%parent%.setContent(%child%)", true);
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
						"  <model class='" + MyLayout_Info.class.getName() + "'/>",
						"  <parameters>",
						"    <parameter name='simpleContainer'>true</parameter>",
						"    <parameter name='simpleContainer.association'>%parent%.setContent(%child%)</parameter>",
						"    <parameter name='simpleContainer.component'>java.awt.Component</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
	}
}
