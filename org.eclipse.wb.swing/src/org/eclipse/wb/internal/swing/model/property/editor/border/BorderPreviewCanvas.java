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
package org.eclipse.wb.internal.swing.model.property.editor.border;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import swingintegration.example.EmbeddedSwingComposite2;

/**
 * {@link Composite} for displaying {@link Border}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderPreviewCanvas extends EmbeddedSwingComposite2 {
  private JPanel m_awtRoot;
  private JPanel m_emptyPanel;
  private JPanel m_filledPanel_1;
  private JPanel m_filledPanel_2;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderPreviewCanvas(Composite parent, int style) {
    super(parent, SWT.NONE);
    populate();
  }

  @Override
  protected JComponent createSwingComponent() {
    m_awtRoot = new JPanel();
    m_awtRoot.setLayout(new GridLayout(1, 0, 5, 0));
    m_awtRoot.setBackground(new Color(200, 225, 255));
    // empty JPanel
    {
      m_emptyPanel = new JPanel();
      m_awtRoot.add(m_emptyPanel);
    }
    // filled JPanel
    {
      m_filledPanel_1 = new JPanel();
      m_awtRoot.add(m_filledPanel_1);
      // fill JPanel
      m_filledPanel_1.setLayout(new GridLayout());
      m_filledPanel_1.add(new JButton("JButton"));
    }
    // filled JPanel
    {
      m_filledPanel_2 = new JPanel();
      m_awtRoot.add(m_filledPanel_2);
      m_filledPanel_2.setBackground(new Color(192, 192, 192));
      // fill JPanel
      m_filledPanel_2.setLayout(new GridLayout());
      m_filledPanel_2.add(new JButton("JButton"));
    }
    // OK, filled container
    return m_awtRoot;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Border ERROR_BORDER = new LineBorder(Color.RED, 5);

  /**
   * Sets the {@link Border} to display.
   */
  public void setBorder(final Border border) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          if (border != null) {
            Graphics graphics = m_emptyPanel.getGraphics();
            graphics.setClip(new Rectangle(0, 0, 0, 0));
            border.paintBorder(m_emptyPanel, graphics, 0, 0, 0, 0);
          }
          // OK, now we know, that this Border can be set on JPanel
          m_emptyPanel.setBorder(border);
          m_filledPanel_1.setBorder(border);
          m_filledPanel_2.setBorder(border);
          m_awtRoot.validate();
        } catch (Throwable e) {
          // oops...this Border can not be used on JPanel
          setBorder(ERROR_BORDER);
        }
      }
    });
  }
}
