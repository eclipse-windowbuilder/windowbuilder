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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.layout.GridData;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract dimension in {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public abstract class GridDimensionInfo<C extends IControlInfo> {
  protected final IGridLayoutInfo<C> m_layout;
  protected int m_index;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridDimensionInfo(IGridLayoutInfo<C> layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of this {@link GridDimensionInfo}.
   */
  public final int getIndex() {
    return m_index;
  }

  /**
   * Sets the index of this {@link GridDimensionInfo}, cached for speed.
   */
  public final void setIndex(int index) {
    m_index = index;
  }

  /**
   * @return the string to display.
   */
  public final String getTitle() throws Exception {
    String tooltip = "";
    Integer alignmentValue = getAlignment();
    if (alignmentValue != null) {
      tooltip = getAlignmentTitle(alignmentValue.intValue());
    }
    if (getGrab()) {
      if (tooltip.length() != 0) {
        tooltip += ", ";
      }
      tooltip += "grab";
    }
    return tooltip;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grab
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>grab</code> state for given {@link IGridDataInfo}.
   */
  protected abstract boolean getGrab(IGridDataInfo gridData);

  /**
   * Sets the <code>grab</code> state for given {@link IGridDataInfo}.
   */
  protected abstract void setGrab(IGridDataInfo gridData, boolean grab) throws Exception;

  /**
   * @return <code>true</code>, if at least one control has grab.
   */
  public final boolean getGrab() {
    final boolean result[] = new boolean[]{false};
    processControls(new IGridDataProcessor<C>() {
      public void process(C control, IGridDataInfo gridData) throws Exception {
        result[0] |= getGrab(gridData);
      }
    });
    return result[0];
  }

  /**
   * Sets the <code>grab</code> state for all controls that start in this dimension.
   */
  public final void setGrab(final boolean grab) {
    processControls(new IGridDataProcessor<C>() {
      public void process(C control, IGridDataInfo gridData) throws Exception {
        setGrab(gridData, grab);
      }
    });
  }

  /**
   * Flips the <code>grab</code> state.
   */
  public final void flipGrab() {
    setGrab(!getGrab());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the title of given alignment from {@link GridData}.
   */
  protected abstract String getAlignmentTitle(int alignment);

  /**
   * @return the <code>alignment</code> for given {@link IGridDataInfo}.
   */
  protected abstract int getAlignment(IGridDataInfo gridData);

  /**
   * Sets the <code>alignment</code> for given {@link IGridDataInfo}.
   */
  protected abstract void setAlignment(IGridDataInfo gridData, int alignment) throws Exception;

  /**
   * @return common alignment, if it is same for all controls, or <code>null</code>.
   */
  public final Integer getAlignment() {
    final boolean first[] = new boolean[]{true};
    final Integer result[] = new Integer[]{null};
    processControls(new IGridDataProcessor<C>() {
      public void process(C control, IGridDataInfo gridData) throws Exception {
        int alignment = getAlignment(gridData);
        if (first[0]) {
          result[0] = alignment;
        } else if (result[0] != null) {
          if (result[0].intValue() != alignment) {
            result[0] = null;
          }
        }
        first[0] = false;
      }
    });
    return result[0];
  }

  /**
   * Sets alignment for all controls that start in this dimension.
   */
  public final void setAlignment(final int alignment) {
    processControls(new IGridDataProcessor<C>() {
      public void process(C control, IGridDataInfo gridData) throws Exception {
        setAlignment(gridData, alignment);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes this dimension.
   */
  public abstract void delete() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Processor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processor for processing {@link IGridDataInfo} for each {@link IControlInfo}.
   */
  protected interface IGridDataProcessor<C extends IControlInfo> {
    void process(C control, IGridDataInfo gridData) throws Exception;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Processing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processes {@link IGridDataInfo} for each {@link IControlInfo} using given
   * {@link IGridDataProcessor}.
   */
  private void processControls(final IGridDataProcessor<C> processor) {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        List<C> toProcess = getControlsToProcess();
        for (C control : toProcess) {
          IGridDataInfo gridData = m_layout.getGridData2(control);
          processor.process(control, gridData);
        }
      }

      private List<C> getControlsToProcess() {
        List<C> toProcess = new ArrayList<>();
        for (C control : m_layout.getControls()) {
          if (!m_layout.isFiller(control)) {
            IGridDataInfo gridData = m_layout.getGridData2(control);
            if (shouldProcessThisControl(gridData)) {
              toProcess.add(control);
            }
          }
        }
        return toProcess;
      }
    });
  }

  /**
   * @return <code>true</code> if {@link IControlInfo} with given {@link IGridDataInfo} should be
   *         processed using {@link #processControls(GridDimensionInfo.IGridDataProcessor)}.
   */
  protected abstract boolean shouldProcessThisControl(IGridDataInfo gridData);
}
