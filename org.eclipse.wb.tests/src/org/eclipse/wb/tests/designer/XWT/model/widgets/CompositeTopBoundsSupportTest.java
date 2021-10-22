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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Test for {@link CompositeTopBoundsSupport}.
 *
 * @author scheglov_ke
 */
public class CompositeTopBoundsSupportTest extends XwtModelTest {
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
  // show()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setSize() throws Exception {
    ControlInfo shell = parse("<Shell/>");
    shell.refresh();
    // default size
    assertEquals(new Dimension(450, 300), shell.getBounds().getSize());
    // set new size
    shell.getTopBoundsSupport().setSize(500, 400);
    shell.refresh();
    assertEquals(new Dimension(500, 400), shell.getBounds().getSize());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // show()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_show_closeUsingESC() throws Exception {
    final ControlInfo shell = parse("<Shell text='My Shell'/>");
    shell.refresh();
    // show
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        shell.getTopBoundsSupport().show();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        Shell wrapperShell = context.useShell("My Shell");
        {
          assertEquals(450, wrapperShell.getSize().x);
          assertEquals(300, wrapperShell.getSize().y);
        }
        new EventSender(wrapperShell).keyDown(SWT.ESC);
      }
    });
  }

  /**
   * Generic {@link Composite} should be wrapped into {@link Shell} to show it.
   */
  public void test_show_notShell() throws Exception {
    final CompositeInfo composite = parse("<Composite/>");
    composite.refresh();
    // show
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        composite.getTopBoundsSupport().show();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        Shell wrapperShell = context.useShell("Wrapper Shell");
        {
          assertEquals(450, composite.getControl().getSize().x);
          assertEquals(300, composite.getControl().getSize().y);
        }
        new EventSender(wrapperShell).keyDown(SWT.ESC);
      }
    });
  }
}
