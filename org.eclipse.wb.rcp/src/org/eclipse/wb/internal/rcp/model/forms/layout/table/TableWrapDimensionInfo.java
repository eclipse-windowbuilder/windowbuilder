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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.ui.forms.widgets.TableWrapData;

import java.util.List;

/**
 * Abstract dimension in {@link ITableWrapLayout_Info<C>}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public abstract class TableWrapDimensionInfo<C extends IControlInfo> {
  protected final ITableWrapLayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableWrapDimensionInfo(ITableWrapLayoutInfo<C> layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of this {@link TableWrapDimensionInfo}.
   */
  public abstract int getIndex();

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
   * @return the <code>grab</code> state for given {@link ITableWrapDataInfo}.
   */
  protected abstract boolean getGrab(ITableWrapDataInfo layoutData);

  /**
   * Sets the <code>grab</code> state for given {@link ITableWrapDataInfo}.
   */
  protected abstract void setGrab(ITableWrapDataInfo layoutData, boolean grab) throws Exception;

  /**
   * @return <code>true</code>, if at least one control has grab.
   */
  public final boolean getGrab() {
    final boolean result[] = new boolean[]{false};
    processControls(new ILayoutDataProcessor<C>() {
      public void process(C control, ITableWrapDataInfo layoutData) throws Exception {
        result[0] |= getGrab(layoutData);
      }
    });
    return result[0];
  }

  /**
   * Sets the <code>grab</code> state for all controls that start in this dimension.
   */
  public final void setGrab(final boolean grab) {
    processControls(new ILayoutDataProcessor<C>() {
      public void process(C control, ITableWrapDataInfo layoutData) throws Exception {
        setGrab(layoutData, grab);
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
   * @return the title of given alignment from {@link TableWrapData}.
   */
  protected abstract String getAlignmentTitle(int alignment);

  /**
   * @return the <code>alignment</code> for given {@link ITableWrapDataInfo}.
   */
  protected abstract int getAlignment(ITableWrapDataInfo layoutData);

  /**
   * Sets the <code>alignment</code> for given {@link ITableWrapDataInfo}.
   */
  protected abstract void setAlignment(ITableWrapDataInfo layoutData, int alignment)
      throws Exception;

  /**
   * @return common alignment, if it is same for all controls, or <code>null</code>.
   */
  public final Integer getAlignment() {
    final boolean first[] = new boolean[]{true};
    final Integer result[] = new Integer[]{null};
    processControls(new ILayoutDataProcessor<C>() {
      public void process(C control, ITableWrapDataInfo layoutData) throws Exception {
        int alignment = getAlignment(layoutData);
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
    processControls(new ILayoutDataProcessor<C>() {
      public void process(C control, ITableWrapDataInfo layoutData) throws Exception {
        setAlignment(layoutData, alignment);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes all controls that start in this dimension.
   */
  public final void delete() {
    processControls(new ILayoutDataProcessor<C>() {
      public void process(C control, ITableWrapDataInfo layoutData) throws Exception {
        control.delete();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Processor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processor for processing {@link ITableWrapDataInfo} for each {@link IControlInfo}.
   * 
   * @author scheglov_ke
   */
  protected interface ILayoutDataProcessor<C extends IControlInfo> {
    void process(C control, ITableWrapDataInfo layoutData) throws Exception;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Processing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processes {@link ITableWrapDataInfo} for each {@link C} using given
   * {@link ILayoutDataProcessor}.
   */
  private void processControls(final ILayoutDataProcessor<C> processor) {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        List<C> toProcess = getControlsToProcess();
        for (C control : toProcess) {
          ITableWrapDataInfo layoutData = m_layout.getTableWrapData2(control);
          if (shouldProcessThisControl(layoutData)) {
            processor.process(control, layoutData);
          }
        }
      }

      private List<C> getControlsToProcess() {
        List<C> toProcess = Lists.newArrayList();
        for (C control : m_layout.getControls()) {
          if (!m_layout.isFiller(control)) {
            ITableWrapDataInfo gridData = m_layout.getTableWrapData2(control);
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
   * @return <code>true</code> if {@link IControlInfo} with given {@link ITableWrapDataInfo} should
   *         be processed using
   *         {@link #processControls(TableWrapDimensionInfo.ILayoutDataProcessor)}.
   */
  protected abstract boolean shouldProcessThisControl(ITableWrapDataInfo layoutData);
}
