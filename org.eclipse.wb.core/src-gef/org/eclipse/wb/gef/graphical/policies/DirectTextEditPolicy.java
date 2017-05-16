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
package org.eclipse.wb.gef.graphical.policies;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

/**
 * {@link GraphicalEditPolicy} that supports direct edit for some {@link String} using {@link Text}
 * widget.
 *
 * @author scheglov_ke
 * @coverage gef.graphical
 */
public abstract class DirectTextEditPolicy extends GraphicalEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text to edit.
   */
  protected abstract String getText();

  /**
   * Sets new text after edit.
   */
  protected abstract void setText(String text);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deactivate() {
    endEdit();
    super.deactivate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Direct edit
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_textWidget;
  private org.eclipse.swt.graphics.Point m_initialSize;
  private final Listener m_mouseDownFilter = new Listener() {
    public void handleEvent(Event event) {
      if (event.widget != m_textWidget) {
        commitEdit();
      }
    }
  };

  /**
   * Commits currently done text modifications.
   */
  private void commitEdit() {
    String text = m_textWidget.getText();
    endEdit();
    setText(text);
  }

  /**
   * Begins direct edit using {@link Text} widget.
   */
  protected final void beginEdit() {
    m_textWidget = new Text((Composite) getHost().getViewer().getControl(), SWT.BORDER);
    // initial text/location
    {
      String text = getText();
      text = StringUtils.defaultString(text);
      m_textWidget.setText(text);
      m_textWidget.selectAll();
      m_textWidget.setFocus();
    }
    // set initial location
    relocateTextWidget();
    // listeners
    m_textWidget.addListener(SWT.KeyDown, new Listener() {
      public void handleEvent(Event event) {
        if (event.keyCode == SWT.ESC) {
          endEdit();
        } else if (event.keyCode == SWT.CR) {
          commitEdit();
        }
      }
    });
    m_textWidget.addListener(SWT.Modify, new Listener() {
      public void handleEvent(Event event) {
        relocateTextWidget();
      }
    });
    m_textWidget.addListener(SWT.FocusOut, new Listener() {
      public void handleEvent(Event event) {
        commitEdit();
      }
    });
    m_textWidget.getDisplay().addFilter(SWT.MouseDown, m_mouseDownFilter);
  }

  /**
   * Ends current direct edit.
   */
  private void endEdit() {
    if (isEditing()) {
      m_textWidget.getDisplay().removeFilter(SWT.MouseDown, m_mouseDownFilter);
      m_textWidget.dispose();
      m_textWidget = null;
      m_initialSize = null;
      // restore focus
      Display.getCurrent().asyncExec(new Runnable() {
        public void run() {
          if (getHost().isActive()) {
            getHost().getViewer().getControl().setFocus();
          }
        }
      });
    }
  }

  /**
   * @return <code>true</code> if we in process of direct edit.
   */
  private boolean isEditing() {
    return m_textWidget != null;
  }

  /**
   * Updates location of {@link Text} widget in host {@link Figure}.
   */
  private void relocateTextWidget() {
    // prepare absolute bounds of host figure
    Rectangle hostBounds;
    {
      hostBounds = getHostFigure().getBounds().getCopy();
      FigureUtils.translateFigureToCanvas(getHostFigure().getParent(), hostBounds);
    }
    // prepare text size
    org.eclipse.swt.graphics.Point textSize;
    {
      textSize = m_textWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      if (m_initialSize != null) {
        textSize.x = Math.max(textSize.x, m_initialSize.x);
        textSize.y = Math.max(textSize.y, m_initialSize.y);
      } else {
        m_initialSize = textSize;
      }
    }
    // set bounds for Text
    {
      Point textLocation = getTextWidgetLocation(hostBounds, new Dimension(textSize.x, textSize.y));
      m_textWidget.setBounds(textLocation.x, textLocation.y, textSize.x, textSize.y);
    }
    // ensure that full text is visible
    {
      org.eclipse.swt.graphics.Point oldSelection = m_textWidget.getSelection();
      m_textWidget.setSelection(0);
      m_textWidget.setSelection(oldSelection);
    }
  }

  /**
   * @param hostBounds
   *          the absolute bounds of host {@link Figure}.
   * @param textSize
   *          the size of {@link Text} widget.
   *
   * @return the absolute location of {@link Text} widget.
   */
  protected abstract Point getTextWidgetLocation(Rectangle hostBounds, Dimension textSize);
}
