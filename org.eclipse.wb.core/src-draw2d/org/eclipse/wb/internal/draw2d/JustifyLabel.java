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
package org.eclipse.wb.internal.draw2d;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * A figure that can display justify text.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class JustifyLabel extends Figure {
  private String m_text = "";
  private int m_wrapChars = 50;
  private int m_lineWidth;
  private boolean m_isCharMode = true;
  private List<Line> m_lines = Collections.emptyList();
  private Dimension m_preferredSize = new Dimension();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JustifyLabel() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the desirable size for this label's text.
   */
  public Dimension getPreferredSize() {
    return m_preferredSize;
  }

  /**
   * Returns the text of the label.
   */
  public String getText() {
    return m_text;
  }

  /**
   * Sets the label's text.
   */
  public void setText(String text) {
    m_text = text;
    updateContent();
  }

  /**
   * Returns chars count for wrap one line.
   */
  public int getWrapChars() {
    return m_wrapChars;
  }

  /**
   * Sets chars count for wrap one line.
   */
  public void setWrapChars(int wrapChars) {
    m_wrapChars = wrapChars;
    m_lineWidth = 0;
    m_isCharMode = true;
    updateContent();
  }

  /**
   * Returns width in pixels for wrap one line.
   */
  public int getWrapPixels() {
    return m_lineWidth;
  }

  /**
   * Sets width in pixels for wrap one line.
   */
  public void setWrapPixels(int width) {
    m_lineWidth = width;
    m_wrapChars = 0;
    m_isCharMode = false;
    updateContent();
  }

  /**
   * @return <code>true</code> if figure work on char wrap mode.
   */
  public boolean isCharMode() {
    return m_isCharMode;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    for (Line line : m_lines) {
      float currentX = 0;
      for (int i = 0; i < line.words.length; i++) {
        graphics.drawString(line.words[i], Math.round(currentX), line.startY);
        currentX += line.wordsSeparatorWidth + line.widths[i];
      }
    }
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    updateContent();
  }

  @Override
  public void setBorder(Border border) {
    super.setBorder(border);
    GC gc = FigureUtils.createGC();
    try {
      // prepare metrics
      gc.setFont(getFont());
      FontMetrics fontMetrics = gc.getFontMetrics();
      int lineWidth = m_isCharMode ? fontMetrics.getAverageCharWidth() * m_wrapChars : m_lineWidth;
      int lineHeight = fontMetrics.getHeight();
      // calculate preferred size
      calculatePreferredSize(lineWidth, lineHeight);
    } finally {
      gc.dispose();
    }
  }

  private void updateContent() {
    m_lines = Lists.newArrayList();
    GC gc = FigureUtils.createGC();
    try {
      // prepare metrics
      gc.setFont(getFont());
      FontMetrics fontMetrics = gc.getFontMetrics();
      int lineWidth = m_isCharMode ? fontMetrics.getAverageCharWidth() * m_wrapChars : m_lineWidth;
      int lineHeight = fontMetrics.getHeight();
      int wordsSeparatorWidth = gc.getAdvanceWidth(' ');
      int textLength = m_text.length();
      //
      for (int lineIndex = 0, startIndex = 0; startIndex < textLength; lineIndex++) {
        // calculate text line
        int endIndex = m_text.indexOf('\n', startIndex);
        if (startIndex == endIndex) {
          startIndex++;
          continue;
        }
        if (endIndex == -1) {
          endIndex = textLength;
        }
        // separate text line to words
        String[] words = StringUtils.split(m_text.substring(startIndex, endIndex));
        int[] widths = new int[words.length];
        for (int i = 0; i < widths.length; i++) {
          widths[i] = gc.stringExtent(words[i]).x;
        }
        // calculate internal lines
        int calculateLineWidth = 0;
        int startWordIndex = 0;
        boolean isStartWordIndex = true;
        for (int i = 0; i < widths.length; i++) {
          int nextWidth = isStartWordIndex ? widths[i] : wordsSeparatorWidth + widths[i];
          isStartWordIndex = false;
          int delta = lineWidth - calculateLineWidth - nextWidth;
          if (delta > 0) {
            calculateLineWidth += nextWidth;
          } else if (delta == 0) {
            Line line = new Line(words, widths, startWordIndex, i + 1 - startWordIndex);
            line.wordsSeparatorWidth = wordsSeparatorWidth;
            line.startY = lineIndex * lineHeight;
            m_lines.add(line);
            // reset state
            lineIndex++;
            calculateLineWidth = 0;
            isStartWordIndex = true;
            startWordIndex = i + 1;
          } else {
            int length = i - startWordIndex;
            //
            if (widths[i] > lineWidth) {
              if (length == 0) {
                length = 1;
              } else {
                length++;
              }
              Line line = new Line(words, widths, startWordIndex, length);
              line.startY = lineIndex * lineHeight;
              m_lines.add(line);
              //
              int remainderWidth = lineWidth - calculateLineWidth;
              if (length > 1) {
                remainderWidth -= wordsSeparatorWidth;
              }
              //
              int lineLastIndex = line.widths.length - 1;
              String splitWord = line.words[lineLastIndex];
              String splitWordBeforePart = null;
              int splitWordBeforeWidth = 0;
              int splitCharCount = splitWord.length();
              while (splitCharCount > 0) {
                splitWordBeforePart = splitWord.substring(0, splitCharCount);
                splitWordBeforeWidth = gc.stringExtent(splitWordBeforePart).x;
                if (splitWordBeforeWidth <= remainderWidth) {
                  break;
                }
                splitCharCount--;
              }
              //
              line.words[lineLastIndex] = splitWordBeforePart;
              line.widths[lineLastIndex] = splitWordBeforeWidth;
              //
              words[i] = words[i].substring(splitCharCount);
              widths[i] = gc.stringExtent(words[i]).x;
              //
              line.calculateWordsSeparatorWidth(lineWidth);
              // reset state
              lineIndex++;
              calculateLineWidth = 0;
              isStartWordIndex = true;
              startWordIndex = i--;
            } else {
              if (length == 0) {
                length = 1;
              }
              Line line = new Line(words, widths, startWordIndex, length);
              line.calculateWordsSeparatorWidth(lineWidth);
              line.startY = lineIndex * lineHeight;
              m_lines.add(line);
              // reset state
              lineIndex++;
              calculateLineWidth = 0;
              isStartWordIndex = true;
              if (i == startWordIndex) {
                startWordIndex = i + 1;
              } else {
                startWordIndex = i--;
              }
            }
          }
        }
        //
        if (calculateLineWidth > 0) {
          Line line = new Line(words, widths, startWordIndex, widths.length - startWordIndex);
          line.wordsSeparatorWidth = wordsSeparatorWidth;
          line.startY = lineIndex * lineHeight;
          m_lines.add(line);
        }
        //
        startIndex = endIndex + 1;
      }
      // calculate preferred size
      calculatePreferredSize(lineWidth, lineHeight);
    } finally {
      gc.dispose();
    }
  }

  private void calculatePreferredSize(int lineWidth, int lineHeight) {
    m_preferredSize = new Dimension();
    if (!m_lines.isEmpty()) {
      int size = m_lines.size();
      Line line = m_lines.get(size - 1);
      if (size == 1) {
        int length = line.widths.length;
        for (int i = 0; i < length; i++) {
          m_preferredSize.width += line.widths[i];
        }
        m_preferredSize.width += Math.round(line.wordsSeparatorWidth * (length - 1));
      } else {
        m_preferredSize.width = lineWidth;
      }
      m_preferredSize.height = line.startY + lineHeight;
    }
    //
    Insets insets = getInsets();
    m_preferredSize.expand(insets.getWidth(), insets.getHeight());
  }

  /**
   * Internal information about one line.
   */
  private static final class Line {
    final String[] words;
    final int[] widths;
    int startY;
    float wordsSeparatorWidth;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Line(String[] words, int[] widths, int startIndex, int length) {
      // copy words
      this.words = new String[length];
      System.arraycopy(words, startIndex, this.words, 0, length);
      // copy widths
      this.widths = new int[length];
      System.arraycopy(widths, startIndex, this.widths, 0, length);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void calculateWordsSeparatorWidth(int lineWidth) {
      if (widths.length > 1) {
        float lineWordsSeparatorWidth = 0;
        for (int i = 0; i < widths.length; i++) {
          lineWordsSeparatorWidth += widths[i];
        }
        wordsSeparatorWidth = (lineWidth - lineWordsSeparatorWidth) / (widths.length - 1);
      }
    }
  }
}