/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;

import org.junit.Test;

/**
 * Test for {@link ObjectsLabelProvider}.
 *
 * @author scheglov_ke
 */
public class ObjectsLabelProviderTest extends DesignerTestCase {
	private static final ImageDescriptor DEF_ICON = ImageDescriptor.createFromFile(
			Object.class,
			"/javax/swing/plaf/basic/icons/JavaCup16.png");
	private static final ImageDescriptor DOWN_ICON = ImageDescriptor.createFromFile(
			Object.class,
			"/javax/swing/plaf/metal/icons/sortDown.png");
	private static final String DEF_TEXT = "theObject";

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for default icon/text, no any decorators.
	 */
	@Test
	public void test_default() throws Exception {
		TestObjectInfo theObject = new MyObjectInfo();
		// do checks
		assertSame(DEF_ICON, ObjectInfo.getImageDescriptor(theObject));
		assertSame(DEF_TEXT, ObjectInfo.getText(theObject));
	}

	/**
	 * Test for decorated icon/text.
	 */
	@Test
	public void test_decorateImageText() throws Exception {
		TestObjectInfo theObject = new MyObjectInfo();
		theObject.addBroadcastListener(new ObjectInfoPresentationDecorateIcon() {
			@Override
			public void invoke(ObjectInfo object, ImageDescriptor[] icon) throws Exception {
				icon[0] = new DecorationOverlayIcon(icon[0], DOWN_ICON, IDecoration.BOTTOM_RIGHT);
			}
		});
		theObject.addBroadcastListener(new ObjectInfoPresentationDecorateText() {
			@Override
			public void invoke(ObjectInfo object, String[] text) throws Exception {
				text[0] = "A: " + text[0] + " :B";
			}
		});
		// do checks
		assertNotSame(DEF_ICON, ObjectInfo.getImageDescriptor(theObject));
		assertEquals("A: " + DEF_TEXT + " :B", ObjectInfo.getText(theObject));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class MyObjectInfo extends TestObjectInfo {
		@Override
		public IObjectPresentation getPresentation() {
			return new DefaultObjectPresentation(this) {
				@Override
				public ImageDescriptor getIcon() throws Exception {
					return DEF_ICON;
				}

				@Override
				public String getText() throws Exception {
					return DEF_TEXT;
				}
			};
		}
	}
}
