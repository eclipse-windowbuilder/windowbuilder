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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.swt.graphics.Image;

/**
 * Test for {@link ObjectsLabelProvider}.
 *
 * @author scheglov_ke
 */
public class ObjectsLabelProviderTest extends DesignerTestCase {
	private static final Image DEF_ICON = SwtResourceManager.getImage(
			Object.class,
			"/javax/swing/plaf/basic/icons/JavaCup16.png");
	private static final Image DOWN_ICON = SwtResourceManager.getImage(
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
	public void test_default() throws Exception {
		TestObjectInfo theObject = new MyObjectInfo();
		// do checks
		assertSame(DEF_ICON, ObjectsLabelProvider.INSTANCE.getImage(theObject));
		assertSame(DEF_TEXT, ObjectsLabelProvider.INSTANCE.getText(theObject));
	}

	/**
	 * Test for decorated icon/text.
	 */
	public void test_decorateImageText() throws Exception {
		TestObjectInfo theObject = new MyObjectInfo();
		theObject.addBroadcastListener(new ObjectInfoPresentationDecorateIcon() {
			@Override
			public void invoke(ObjectInfo object, Image[] icon) throws Exception {
				icon[0] =
						SwtResourceManager.decorateImage(icon[0], DOWN_ICON, SwtResourceManager.BOTTOM_RIGHT);
			}
		});
		theObject.addBroadcastListener(new ObjectInfoPresentationDecorateText() {
			@Override
			public void invoke(ObjectInfo object, String[] text) throws Exception {
				text[0] = "A: " + text[0] + " :B";
			}
		});
		// do checks
		assertNotSame(DEF_ICON, ObjectsLabelProvider.INSTANCE.getImage(theObject));
		assertEquals("A: " + DEF_TEXT + " :B", ObjectsLabelProvider.INSTANCE.getText(theObject));
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
				public Image getIcon() throws Exception {
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
