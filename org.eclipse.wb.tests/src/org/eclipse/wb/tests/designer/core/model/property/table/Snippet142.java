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
package org.eclipse.wb.tests.designer.core.model.property.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Snippet142 {
	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		final Button button = new Button(shell, SWT.NONE);
		button.setSize(100, 100);
		button.setText("Click");
		shell.pack();
		shell.open();
		button.addListener(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(Event e) {
				System.out.println("enter: " + e);
			}
		});
		button.addListener(SWT.MouseExit, new Listener() {
			@Override
			public void handleEvent(Event e) {
				System.out.println("exit: " + e);
			}
		});
		button.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event e) {
				System.out.println("Mouse Down (button: " + e.button + " x: " + e.x + " y: " + e.y + ")");
			}
		});
		final Point pt = display.map(shell, null, 50, 50);
		new Thread() {
			Event event;

			@Override
			public void run() {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
				{
					event = new Event();
					event.type = SWT.MouseMove;
					event.x = pt.x;
					event.y = pt.y;
					display.post(event);
					try {
						Thread.sleep(3);
					} catch (InterruptedException e) {
					}
				}
				/*{
         event.type = SWT.MouseDown;
         event.button = 1;
         display.post(event);
         try {
         Thread.sleep(3);
         } catch (InterruptedException e) {
         }
         event.type = SWT.MouseUp;
         display.post(event);
         }*/
				{
					event = new Event();
					event.type = SWT.MouseMove;
					event.x = 10;
					event.y = 10;
					display.post(event);
					try {
						Thread.sleep(3);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}