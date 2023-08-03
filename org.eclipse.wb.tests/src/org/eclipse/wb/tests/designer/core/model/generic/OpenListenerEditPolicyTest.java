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

import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.editor.multi.MultiMode;
import org.eclipse.wb.internal.core.gef.policy.OpenListenerEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link OpenListenerEditPolicy}.
 *
 * @author scheglov_ke
 */
public class OpenListenerEditPolicyTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_newHandler() throws Exception {
		openContainer(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		// add "refresh" broadcast listener
		final AtomicBoolean refreshFlag = new AtomicBoolean();
		button.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				refreshFlag.set(true);
			}
		});
		// double click on "button"
		canvas.doubleClick(button);
		// refresh was done
		assertTrue(refreshFlag.get());
		// "Source" page is active
		{
			MultiMode multiMode = (MultiMode) m_designerEditor.getMultiMode();
			waitEventLoop(10);
			// isActive() fails on Linux because the widget isn't updated in time...
			// assertTrue(multiMode.getSourcePage().isActive());
			assertTrue(multiMode.isSourceActive());
		}
		// source changes
		assertEditor(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    button.addActionListener(new ActionListener() {",
				"      public void actionPerformed(ActionEvent e) {",
				"      }",
				"    });",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * When we open existing event handler, would be good to about refresh cycle.
	 * <p>
	 * http://www.eclipse.org/forums/index.php/t/217349/
	 */
	@Test
	public void test_existingHandler() throws Exception {
		openContainer(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    button.addActionListener(new ActionListener() {",
				"      public void actionPerformed(ActionEvent e) {",
				"      }",
				"    });",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		// add "refresh" broadcast listener
		final AtomicBoolean refreshFlag = new AtomicBoolean();
		button.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				refreshFlag.set(true);
			}
		});
		// double click on "button"
		canvas.doubleClick(button);
		// refresh was not required
		assertFalse(refreshFlag.get());
	}
}
