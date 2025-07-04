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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildrenGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildrenTree;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test for {@link DefaultObjectPresentation}.
 *
 * @author scheglov_ke
 */
public class DefaultObjectPresentationTest extends DesignerTestCase {
	/**
	 * Test for {@link IObjectPresentation#getIcon()}.
	 */
	@Test
	public void test_getIcon() throws Exception {
		ObjectInfo parent = new TestObjectInfo();
		assertNull(parent.getPresentation().getIcon());
	}

	/**
	 * Test for {@link IObjectPresentation#getChildrenTree()}.
	 */
	@Test
	public void test_getChildrenTree() throws Exception {
		ObjectInfo parent = new TestObjectInfo();
		ObjectInfo child_1 = new TestObjectInfo("1");
		final ObjectInfo child_2 = new TestObjectInfo("2");
		ObjectInfo child_3 = new TestObjectInfo("3");
		parent.addChild(child_1);
		parent.addChild(child_2);
		parent.addChild(child_3);
		// filter out "child_2" from "tree children"
		parent.addBroadcastListener(new ObjectInfoChildTree() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object == child_2) {
					visible[0] = false;
				}
			}
		});
		// check "tree children"
		List<ObjectInfo> children = parent.getPresentation().getChildrenTree();
		assertEquals(2, children.size());
		assertSame(child_1, children.get(0));
		assertSame(child_3, children.get(1));
	}

	/**
	 * Test for {@link IObjectPresentation#getChildrenTree()}.
	 * <p>
	 * Using {@link ObjectInfoChildrenTree}.
	 */
	@Test
	public void test_getChildrenTree_childrenBroadcast() throws Exception {
		ObjectInfo parent = new TestObjectInfo();
		final ObjectInfo child_1 = new TestObjectInfo("1");
		final ObjectInfo child_2 = new TestObjectInfo("2");
		final ObjectInfo child_3 = new TestObjectInfo("3");
		parent.addChild(child_1);
		parent.addChild(child_2);
		parent.addChild(child_3);
		// move "child_1" to the end
		parent.addBroadcastListener(new ObjectInfoChildrenTree() {
			@Override
			public void invoke(ObjectInfo p, List<ObjectInfo> children) throws Exception {
				children.remove(child_1);
				children.add(child_1);
			}
		});
		// check "tree children"
		List<ObjectInfo> children = parent.getPresentation().getChildrenTree();
		Assertions.assertThat(children).containsExactly(child_2, child_3, child_1);
	}

	/**
	 * Test for {@link IObjectPresentation#getChildrenGraphical()}.
	 */
	@Test
	public void test_getChildrenGraphical() throws Exception {
		ObjectInfo parent = new TestObjectInfo();
		ObjectInfo child_1 = new TestObjectInfo("1");
		final ObjectInfo child_2 = new TestObjectInfo("2");
		ObjectInfo child_3 = new TestObjectInfo("3");
		parent.addChild(child_1);
		parent.addChild(child_2);
		parent.addChild(child_3);
		// filter out "child_2" from "graphical children"
		parent.addBroadcastListener(new ObjectInfoChildGraphical() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object == child_2) {
					visible[0] = false;
				}
			}
		});
		// check "graphical children"
		List<ObjectInfo> children = parent.getPresentation().getChildrenGraphical();
		assertEquals(2, children.size());
		assertSame(child_1, children.get(0));
		assertSame(child_3, children.get(1));
	}

	/**
	 * Test for {@link IObjectPresentation#getChildrenGraphical()}.
	 * <p>
	 * Using {@link ObjectInfoChildrenGraphical}.
	 */
	@Test
	public void test_getChildrenGraphical_childrenBroadcast() throws Exception {
		ObjectInfo parent = new TestObjectInfo();
		final ObjectInfo child_1 = new TestObjectInfo("1");
		final ObjectInfo child_2 = new TestObjectInfo("2");
		final ObjectInfo child_3 = new TestObjectInfo("3");
		parent.addChild(child_1);
		parent.addChild(child_2);
		parent.addChild(child_3);
		// move "child_1" to the end
		parent.addBroadcastListener(new ObjectInfoChildrenGraphical() {
			@Override
			public void invoke(List<ObjectInfo> children) throws Exception {
				children.remove(child_1);
				children.add(child_1);
			}
		});
		// check "graphical children"
		List<ObjectInfo> children = parent.getPresentation().getChildrenGraphical();
		Assertions.assertThat(children).containsExactly(child_2, child_3, child_1);
	}
}
