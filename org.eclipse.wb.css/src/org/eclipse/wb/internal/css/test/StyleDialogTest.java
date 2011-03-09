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
package org.eclipse.wb.internal.css.test;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.css.dialogs.style.StyleEditDialog;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.File;

public class StyleDialogTest {
  protected Shell shell;

  public static void main(String[] args) {
    try {
      StyleDialogTest window = new StyleDialogTest();
      window.open();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void open() {
    final Display display = Display.getDefault();
    createContents();
    shell.open();
    shell.layout();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  protected void createContents() {
    shell = new Shell();
    shell.setLayout(new GridLayout());
    shell.setSize(650, 500);
    shell.setLocation(500, 350);
    shell.setText("SWT Application");
    //
    {
      final Button button = new Button(shell, SWT.NONE);
      button.setText("button");
      button.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          try {
            testSemantics();
          } catch (Throwable ex) {
            ex.printStackTrace();
          }
        }
      });
    }
    /*{
    	long start = System.nanoTime();
    	for (int i = 0; i < 400; i++) {
    		Combo combo = new Combo(shell, SWT.NONE);
    		combo.setVisibleItemCount(20);
    	}
    	System.out.println("combo's time: " + (System.nanoTime() - start) / 1000000.0);
    }*/
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  private void testSemantics() throws Exception {
    String path = "C:/eclipsePL/workspace/org.eclipse.wb.css/src/org/eclipse/wb/css/test/test.css";
    String content = IOUtils2.readString(new File(path));
    CssEditContext context = new CssEditContext(new Document(content));
    CssDocument document = context.getCssDocument();
    CssRuleNode rule = document.getRule(0);
    //
    StyleEditDialog styleEditDialog = StyleEditDialog.get(shell);
    styleEditDialog.setRule(rule);
    if (styleEditDialog.open() != Window.OK) {
      return;
    }
    //
    System.out.println(context.getDocument().get());
    styleEditDialog.updateRule(rule);
    System.out.println(context.getDocument().get());
  }
}
