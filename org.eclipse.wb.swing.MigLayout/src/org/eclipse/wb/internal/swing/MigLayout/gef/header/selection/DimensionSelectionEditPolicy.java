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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.selection;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.selection.ResizeHintFigure.SizeElement;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import net.miginfocom.layout.UnitValue;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Abstract {@link SelectionEditPolicy} for {@link DimensionHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
abstract class DimensionSelectionEditPolicy<T extends MigDimensionInfo>
    extends
      AbstractHeaderSelectionEditPolicy {
  protected static final String REQ_RESIZE = "resize"; //$NON-NLS-1$

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // move handle
    {
      MoveHandle moveHandle = new MoveHandle(getHost(), new HeaderMoveHandleLocator());
      moveHandle.setForeground(IColorConstants.red);
      handles.add(moveHandle);
    }
    //
    return handles;
  }

  @Override
  protected List<Handle> createStaticHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(createResizeHandle());
    return handles;
  }

  /**
   * @return the {@link Handle} for resizing.
   */
  protected abstract Handle createResizeHandle();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the host {@link DimensionHeaderEditPart}.
   */
  @SuppressWarnings("unchecked")
  private DimensionHeaderEditPart<T> getHostHeader() {
    return (DimensionHeaderEditPart<T>) getHost();
  }

  /**
   * @return the host {@link MigLayoutInfo}.
   */
  protected final MigLayoutInfo getLayout() {
    return getHostHeader().getLayout();
  }

  /**
   * @return the host {@link MigDimensionInfo}.
   */
  protected final T getDimension() {
    return getHostHeader().getDimension();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private SizeElement m_resizeSizeElement;
  private String m_resizeSizeUnit;
  private Figure m_lineFeedback;
  private ResizeHintFigure m_feedback;
  private ChangeBoundsRequest m_lastResizeRequest;
  protected Command m_resizeCommand;

  @Override
  public boolean understandsRequest(Request request) {
    return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
  }

  @Override
  public Command getCommand(Request request) {
    if (!getLayout().canChangeDimensions()) {
      return null;
    }
    // use such "indirect" command because when we press Ctrl and _don't_ move mouse after
    // this, we will show correct feedback text (without hint), and set correct m_resizeCommand,
    // but GEF already asked command and will not ask it again
    return new Command() {
      @Override
      public void execute() throws Exception {
        getHost().getViewer().getEditDomain().executeCommand(m_resizeCommand);
      }
    };
  }

  @Override
  public void showSourceFeedback(Request request) {
    ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
    m_resizeCommand = null;
    // line feedback
    {
      // create feedback
      if (m_lineFeedback == null) {
        m_lineFeedback = new Figure();
        LineBorder border = new LineBorder(IColorConstants.red, 2);
        m_lineFeedback.setBorder(border);
        addFeedback(m_lineFeedback);
      }
      // prepare feedback bounds
      Rectangle bounds;
      {
        Figure hostFigure = getHostFigure();
        bounds = changeBoundsRequest.getTransformedRectangle(hostFigure.getBounds());
        FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
      }
      // show feedback
      m_lineFeedback.setBounds(bounds);
    }
    // text feedback
    {
      Layer feedbackLayer = getMainLayer(IEditPartViewer.TOP_LAYER);
      // add feedback
      if (m_feedback == null) {
        m_feedback = new ResizeHintFigure();
        feedbackLayer.add(m_feedback);
        // get initial values
        prepareDefaultResizeElements();
      }
      // set feedback bounds
      {
        Point mouseLocation = changeBoundsRequest.getLocation().getCopy();
        Point feedbackLocation = getTextFeedbackLocation(mouseLocation);
        FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
        m_feedback.setLocation(feedbackLocation);
      }
      // set text
      m_lastResizeRequest = changeBoundsRequest;
      updateFeedbackText(changeBoundsRequest);
    }
  }

  /**
   * Initializes {@link #m_resizeSizeElement} and {@link #m_resizeSizeUnit}.
   */
  private void prepareDefaultResizeElements() {
    m_resizeSizeElement = SizeElement.PREF;
    m_resizeSizeUnit = StringUtils.EMPTY;
    // prepare default size element to resize
    UnitValue resizeValue;
    {
      T dimension = getDimension();
      {
        resizeValue = dimension.getPreferredSize();
        m_resizeSizeElement = SizeElement.PREF;
      }
      if (resizeValue == null) {
        resizeValue = dimension.getMinimumSize();
        if (resizeValue != null) {
          m_resizeSizeElement = SizeElement.MIN;
        }
      }
      if (resizeValue == null) {
        resizeValue = dimension.getMaximumSize();
        if (resizeValue != null) {
          m_resizeSizeElement = SizeElement.MAX;
        }
      }
    }
    // use existing unit
    if (resizeValue != null) {
      m_resizeSizeUnit = resizeValue.getUnitString();
    }
  }

  /**
   * Updates the feedback text according to the {@link #m_lastResizeRequest} and size element/unit.
   */
  private void updateFeedbackText(Request request) {
    // prepare size
    final String sizeString;
    {
      int pixels = getPixelSize(m_lastResizeRequest.getSizeDelta());
      sizeString = getDimension().toUnitString(pixels, m_resizeSizeUnit);
    }
    // show text
    {
      String text;
      switch (m_resizeSizeElement) {
        case MIN :
          text = "min := "; //$NON-NLS-1$
          break;
        case PREF :
          text = "pref := "; //$NON-NLS-1$
          break;
        case MAX :
          text = "max := "; //$NON-NLS-1$
          break;
        default :
          text = ""; //$NON-NLS-1$
      }
      m_feedback.setText(text + sizeString);
    }
    // set command
    final MigLayoutInfo layout = getLayout();
    m_resizeCommand = new EditCommand(layout) {
      @Override
      protected void executeEdit() throws Exception {
        T dimension = getDimension();
        switch (m_resizeSizeElement) {
          case MIN :
            dimension.setMinimumSize(sizeString);
            break;
          case PREF :
            dimension.setPreferredSize(sizeString);
            break;
          case MAX :
            dimension.setMaximumSize(sizeString);
            break;
          default :
            return;
        }
        layout.writeDimensions();
      }
    };
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    removeFeedback(m_lineFeedback);
    m_lineFeedback = null;
    //
    FigureUtils.removeFigure(m_feedback);
    m_feedback = null;
  }

  @Override
  public void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      // special key for resize feedback
      if (m_feedback != null) {
        char c = keyRequest.getCharacter();
        {
          SizeElement newSizeElement = ResizeHintFigure.getNewSizeElement(c);
          if (newSizeElement != null) {
            m_resizeSizeElement = newSizeElement;
            updateFeedbackText(request);
          }
        }
        {
          String newSizeUnit = ResizeHintFigure.getNewSizeUnit(c);
          if (newSizeUnit != null) {
            m_resizeSizeUnit = newSizeUnit;
            updateFeedbackText(request);
          }
        }
      }
      // keyboard based alignment
      if (keyRequest.isPressed()) {
        char c = keyRequest.getCharacter();
        if (c == 'g') {
          flipGrow();
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize: abstract feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the location of text feedback (with size hint).
   */
  protected abstract Point getTextFeedbackLocation(Point mouseLocation);

  /**
   * @return the size of host {@link EditPart} in pixels, taking into account given resize delta.
   */
  protected abstract int getPixelSize(Dimension resizeDelta);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move location
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ILocator} to place handle directly on header.
   */
  private class HeaderMoveHandleLocator implements ILocator {
    public void relocate(Figure target) {
      Figure reference = getHostFigure();
      Rectangle bounds = reference.getBounds().getCopy();
      FigureUtils.translateFigureToFigure(reference, target, bounds);
      target.setBounds(bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set/unset the "grow" flag.
   */
  private void flipGrow() {
    final MigLayoutInfo layout = getLayout();
    ExecutionUtils.run(layout, new RunnableEx() {
      public void run() throws Exception {
        getDimension().flipGrow();
        layout.writeDimensions();
      }
    });
  }
}
