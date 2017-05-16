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
/**
 *
 */
package org.eclipse.wb.internal.core.editor.multi;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Helper for highlighting lines visited during rendering.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public class VisitedLinesHighlighter implements IPainter, LineBackgroundListener {
  private boolean m_shouldHighlight;
  private Color m_color;
  private final List<Position> m_linePositions = Lists.newArrayList();
  private final ISourceViewer m_sourceViewer;
  private final IDocument m_document;
  private final StyledText m_textWidget;
  private final ITextViewerExtension2 m_extension2;
  private final ITextViewerExtension5 m_extension5;
  private IPaintPositionManager m_positionManager;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VisitedLinesHighlighter(ISourceViewer sourceViewer) {
    m_sourceViewer = sourceViewer;
    m_document = m_sourceViewer.getDocument();
    m_textWidget = sourceViewer.getTextWidget();
    m_extension2 = (ITextViewerExtension2) sourceViewer;
    m_extension5 = (ITextViewerExtension5) m_sourceViewer;
    m_extension2.addPainter(this);
    m_textWidget.addLineBackgroundListener(this);
    //
    trackPreferences();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences tracking
  //
  ////////////////////////////////////////////////////////////////////////////
  IPropertyChangeListener m_preferenceListener = new IPropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent event) {
      trackPreferences_getCurrentValues();
      m_textWidget.redraw();
    }
  };

  private void trackPreferences() {
    trackPreferences_getCurrentValues();
    DesignerPlugin.getPreferences().addPropertyChangeListener(m_preferenceListener);
  }

  private void trackPreferences_getCurrentValues() {
    IPreferenceStore preferences = DesignerPlugin.getPreferences();
    m_shouldHighlight = preferences.getBoolean(IPreferenceConstants.P_HIGHLIGHT_VISITED);
    RGB rgb =
        PreferenceConverter.getColor(preferences, IPreferenceConstants.P_HIGHLIGHT_VISITED_COLOR);
    m_color = SwtResourceManager.getColor(rgb);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setVisitedNodes(Collection<ASTNode> nodes) throws Exception {
    // unmanage previous positions
    for (Position position : m_linePositions) {
      m_positionManager.unmanagePosition(position);
    }
    // prepare lines
    Set<Integer> lines = Sets.newHashSet();
    for (ASTNode node : nodes) {
      int line = m_document.getLineOfOffset(node.getStartPosition());
      lines.add(line);
    }
    // create new positions
    m_linePositions.clear();
    for (Integer line : lines) {
      int lineOffset = m_document.getLineOffset(line);
      Position position = new Position(lineOffset);
      m_linePositions.add(position);
      m_positionManager.managePosition(position);
    }
    // paint
    ExecutionUtils.runAsync(new RunnableEx() {
      public void run() throws Exception {
        m_textWidget.redraw();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPainter
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setPositionManager(IPaintPositionManager manager) {
    m_positionManager = manager;
  }

  public void paint(int reason) {
  }

  public void deactivate(boolean redraw) {
  }

  public void dispose() {
    DesignerPlugin.getPreferences().removePropertyChangeListener(m_preferenceListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LineBackgroundListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void lineGetBackground(LineBackgroundEvent event) {
    if (!m_shouldHighlight) {
      return;
    }
    // prepare "model" line offset
    int lineOffset = event.lineOffset;
    lineOffset = m_extension5.widgetOffset2ModelOffset(lineOffset);
    // if already not default background, such as "current line", then keep it as is
    if (event.lineBackground != null) {
      Color defaultBackground = m_textWidget.getBackground();
      if (!event.lineBackground.equals(defaultBackground)) {
        return;
      }
    }
    // try to find position for given line
    for (Position position : m_linePositions) {
      if (!position.isDeleted()) {
        if (position.getOffset() == lineOffset) {
          event.lineBackground = m_color;
          break;
        }
      }
    }
  }
}