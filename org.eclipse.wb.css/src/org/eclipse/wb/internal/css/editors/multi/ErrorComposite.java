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
package org.eclipse.wb.internal.css.editors.multi;

import org.eclipse.wb.internal.css.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Composite for showing error message while editing CSS file.
 * 
 * @author sablin_aa
 * @coverage CSS.editor
 */
public class ErrorComposite extends Composite {
  private StyledText m_text;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ErrorComposite(Composite parent, int style) {
    super(parent, style);
    GridLayout layout = new GridLayout();
    setLayout(layout);
    // create GUI elements
    {
      // title section
      Composite titleComposite = new Composite(this, SWT.NONE);
      titleComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      titleComposite.setLayout(new GridLayout());
      {
        Label label = new Label(titleComposite, SWT.NONE);
        label.setText(Messages.ErrorComposite_title);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      }
    }
    {
      // body section
      ScrolledComposite bodyComposite =
          new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      bodyComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      {
        m_text = new StyledText(bodyComposite, SWT.NONE);
        m_text.setEditable(false);
      }
      bodyComposite.setContent(m_text);
      bodyComposite.addListener(SWT.Resize, new Listener() {
        public void handleEvent(Event event) {
          onBodyResize();
        }
      });
    }
    {
      // actions section
      Composite buttonComposite = new Composite(this, SWT.NONE);
      buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    }
  }

  public void onBodyResize() {
    // resize text area
    Point computeSize = m_text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    Rectangle clientArea = m_text.getParent().getClientArea();
    m_text.setSize(
        computeSize.x > clientArea.width ? computeSize.x : clientArea.width,
        computeSize.y > clientArea.height ? computeSize.y : clientArea.height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setExeption(Throwable e) {
    ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(bufferStream));
    m_text.setText(bufferStream.toString());
    onBodyResize();
  }
}
