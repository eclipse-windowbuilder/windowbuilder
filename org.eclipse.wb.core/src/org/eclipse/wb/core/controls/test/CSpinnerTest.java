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
package org.eclipse.wb.core.controls.test;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.core.controls.CSpinnerDeferredNotifier;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * Application for testing {@link CSpinner}.
 *
 * @author scheglov_ke
 * @coverage core.test
 */
public class CSpinnerTest {
  protected Shell shell;

  public static void main(String[] args) {
    try {
      CSpinnerTest window = new CSpinnerTest();
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
    shell.setSize(500, 375);
    shell.setLocation(700, 400);
    shell.setText("SWT Application");
    GridLayoutFactory.create(shell);
    //
    {
      final CSpinner spinner = new CSpinner(shell, SWT.NONE);
      GridDataFactory.create(spinner).hintHC(10);
      spinner.setSelection(-50);
      spinner.setMinimum(-15);
      spinner.setMaximum(100);
      // listener
      {
        final Label valueLabel = new Label(shell, SWT.NONE);
        spinner.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            updateValueLabel(event, valueLabel);
          }
        });
      }
      // deferred listener
      {
        final Label valueLabel = new Label(shell, SWT.NONE);
        new CSpinnerDeferredNotifier(spinner, 500, new Listener() {
          public void handleEvent(Event event) {
            updateValueLabel(event, valueLabel);
          }
        });
      }
    }
    {
      Group group = new Group(shell, SWT.NONE);
      GridLayoutFactory.create(group).columns(2);
      // "standard" Spinner, integer
      {
        new Label(group, SWT.NONE).setText("Standard Spinner:");
        Spinner spinner = new Spinner(group, SWT.BORDER);
        GridDataFactory.create(spinner).alignVM().hintHC(15);
      }
      // "standard" Spinner, setDigits()
      {
        new Label(group, SWT.NONE).setText("Standard Spinner, digits 2:");
        Spinner spinner = new Spinner(group, SWT.BORDER);
        GridDataFactory.create(spinner).alignVM().hintHC(15);
        spinner.setDigits(2);
      }
      // CSpinner
      {
        new Label(group, SWT.NONE).setText("CSpinner [-15,30]:");
        CSpinner spinner = new CSpinner(group, SWT.BORDER);
        GridDataFactory.create(spinner).alignVM().hintHC(15);
        spinner.setSelection(5);
        spinner.setMinimum(-15);
        spinner.setMaximum(30);
      }
      // CSpinner, digits 2
      {
        new Label(group, SWT.NONE).setText("CSpinner [-10.00, +20.00]:");
        CSpinner spinner = new CSpinner(group, SWT.BORDER);
        GridDataFactory.create(spinner).alignVM().hintHC(15);
        spinner.setSelection(5);
        spinner.setMinimum(-1000);
        spinner.setMaximum(+2000);
        spinner.setDigits(2);
      }
      // just Text
      {
        new Label(group, SWT.NONE).setText("Text:");
        Text text = new Text(group, SWT.BORDER);
        GridDataFactory.create(text).alignVM().hintHC(15);
      }
      // CSpinner
      {
        new Label(group, SWT.NONE).setText("CSpinner [-inf,+inf]:");
        CSpinner spinner = new CSpinner(group, SWT.BORDER);
        GridDataFactory.create(spinner).alignVM().hintHC(15);
      }
      // Button
      {
        new Label(group, SWT.NONE).setText("Button SWT.ARROW:");
        Button button = new Button(group, SWT.ARROW);
        GridDataFactory.create(button).alignVM();
      }
    }
  }

  private void updateValueLabel(Event event, final Label valueLabel) {
    valueLabel.setText("value: " + event.detail);
    valueLabel.setForeground(event.doit ? IColorConstants.black : IColorConstants.red);
    shell.layout();
  }
}
