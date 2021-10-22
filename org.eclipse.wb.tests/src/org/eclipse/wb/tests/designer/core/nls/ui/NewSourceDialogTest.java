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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.bundle.pure.direct.DirectSourceNewComposite;
import org.eclipse.wb.internal.core.nls.ui.NewSourceDialog;
import org.eclipse.wb.internal.core.nls.ui.PropertiesComposite;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

/**
 * Tests for {@link NewSourceDialog}.
 *
 * @author scheglov_ke
 */
public class NewSourceDialogTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Open {@link NewSourceDialog} from {@link PropertiesComposite}.
   */
  public void test_openDialog() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    final NewSourceDialog newSourceDialog = new NewSourceDialog(null, frame);
    //
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        newSourceDialog.open();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        assertNotNull(context.getButtonByText("Classic Eclipse messages class"));
        assertNotNull(context.getButtonByText("Modern Eclipse messages class"));
        assertNotNull(context.getButtonByText("Direct ResourceBundle usage"));
        assertNotNull(context.getButtonByText("ResourceBundle in field"));
        // close dialog
        context.clickButton("Cancel");
      }
    });
  }

  /**
   * Test for {@link DirectSourceNewComposite}.
   */
  public void test_DirectSource() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    final NewSourceDialog newSourceDialog = new NewSourceDialog(null, frame);
    //
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        newSourceDialog.open();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        Button okButton = context.getButtonByText("OK");
        context.selectButton("Direct ResourceBundle usage");
        try {
          {
            StyledText styledText = context.findFirstWidget(StyledText.class);
            assertEquals(
                "button.setText( ResourceBundle.getBundle(\"full.bundle.name\").getString(\"some.key\") );",
                styledText.getText());
          }
          // source folder
          {
            Text sourceFolderText = (Text) context.getControlAfterLabel("Source folder:");
            assertEquals("TestProject/src", sourceFolderText.getText());
            // set bad folder - "OK" button disabled
            sourceFolderText.setText("no-such-folder");
            sourceFolderText.notifyListeners(SWT.Modify, null);
            assertFalse(okButton.getEnabled());
            // restore good folder - "OK" button enabled
            sourceFolderText.setText("TestProject/src");
            sourceFolderText.notifyListeners(SWT.Modify, null);
            assertTrue(okButton.getEnabled());
          }
          // package
          {
            Text packageText = (Text) context.getControlAfterLabel("Package:");
            assertEquals("test", packageText.getText());
            // set bad - "OK" button disabled
            packageText.setText("no-such-package");
            packageText.notifyListeners(SWT.Modify, null);
            assertFalse(okButton.getEnabled());
            // restore good - "OK" button enabled
            packageText.setText("test");
            packageText.notifyListeners(SWT.Modify, null);
            assertTrue(okButton.getEnabled());
          }
          // properties file
          {
            Text fileText = (Text) context.getControlAfterLabel("Property file name:");
            assertEquals("messages.properties", fileText.getText());
            // set bad - "OK" button disabled
            fileText.setText("bad-file-name");
            fileText.notifyListeners(SWT.Modify, null);
            assertFalse(okButton.getEnabled());
            // restore good - "OK" button enabled
            fileText.setText("messages.properties");
            fileText.notifyListeners(SWT.Modify, null);
            assertTrue(okButton.getEnabled());
          }
        } finally {
          // close dialog
          context.clickButton("OK");
        }
      }
    });
    // result
    /*System.out.println(newSourceDialog.getNewSourceDescription());
    System.out.println(newSourceDialog.getNewEditableSource());
    System.out.println(newSourceDialog.getNewSourceParameters());*/
  }
}
