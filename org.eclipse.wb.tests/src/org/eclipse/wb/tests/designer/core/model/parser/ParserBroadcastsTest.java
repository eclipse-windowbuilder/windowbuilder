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
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.laf.ui.LafRootProcessor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import javax.swing.JTextField;

/**
 * During parsing we may use {@link IJavaInfoInitializationParticipator} to set "external" broadcast
 * listeners. And even any {@link JavaInfo} itself may add some broadcast listeners in
 * {@link JavaInfo#initialize()}. Note, that all these listeners are set for {@link AstEditor}
 * global {@link BroadcastSupport}, so they stay alive even if {@link JavaInfo} at the ends is not
 * bound to the <em>root</em> {@link JavaInfo}.
 * <p>
 * This is bad, because keeps in memory {@link JavaInfo}'s not bound to hierarchy, and for
 * "external" listeners, such as {@link LafRootProcessor} or
 * {@link DeviceSelectionJavaInfoParticipator}, causes displaying <em>more than one</em>
 * contribution item, because they see more than one root {@link JavaInfo}.
 * <p>
 * So, we should "evict" listeners for all {@link ObjectInfo}'s except root {@link JavaInfo}
 * selected in {@link JavaInfoParser}.
 *
 * @author scheglov_ke
 */
public class ParserBroadcastsTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noExternalListener() throws Exception {
    ContainerInfo panel =
        parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    Component notBoundComponentObject = new JTextField();",
        "    add(new JButton());",
        "  }",
        "}");
    panel.refresh();
    // extension is not added yet, so...
    assertEquals(0, MyParticipator.m_refreshCount);
  }

  /**
   * Test that any extra {@link IJavaInfoInitializationParticipator} for non-hierarchy components
   * are evicted.
   */
  public void test_extraListenersEvicted() throws Exception {
    addParticipatorExtension(MyParticipator.class.getName());
    try {
      ContainerInfo panel =
          parseContainer(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    Component notBoundComponentObject = new JTextField();",
          "    add(new JButton());",
          "  }",
          "}");
      // no "JTextField" in "panel" hierarchy
      panel.accept(new ObjectInfoVisitor() {
        @Override
        public void endVisit(ObjectInfo objectInfo) throws Exception {
          if (objectInfo instanceof JavaInfo) {
            JavaInfo javaInfo = (JavaInfo) objectInfo;
            assertNotSame(JTextField.class, javaInfo.getDescription().getComponentClass());
          }
        }
      });
      // no "refresh" yet, so...
      assertEquals(0, MyParticipator.m_refreshCount);
      // it is expected that there is ONE real root JavaInfo, so only one root refresh expected
      panel.refresh();
      assertEquals(1, MyParticipator.m_refreshCount);
    } finally {
      removeParticipatorExtension();
      MyParticipator.m_refreshCount = 0;
    }
  }

  /**
   * Test implementation of {@link IJavaInfoInitializationParticipator}.
   *
   * @author scheglov_ke
   */
  public static final class MyParticipator implements IJavaInfoInitializationParticipator {
    private static int m_refreshCount;

    public void process(final JavaInfo javaInfo) throws Exception {
      javaInfo.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed() throws Exception {
          if (javaInfo.isRoot()) {
            m_refreshCount++;
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dynamic IJavaInfoInitializationParticipator extension support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String POINT_ID = "org.eclipse.wb.core.java.javaInfoInitializationParticipators";

  /**
   * Adds dynamic {@link IJavaInfoInitializationParticipator} extension.
   *
   * @param className
   *          the name of {@link IJavaInfoInitializationParticipator} class.
   */
  private static void addParticipatorExtension(String className) throws Exception {
    String contribution = "  <participator class='" + className + "'/>";
    TestUtils.addDynamicExtension(POINT_ID, contribution);
  }

  /**
   * Removes dynamic {@link IJavaInfoInitializationParticipator} extension.
   */
  private static void removeParticipatorExtension() throws Exception {
    TestUtils.removeDynamicExtension(POINT_ID);
  }
}
