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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.text.MessageFormat;

/**
 * Model for {@link GridBagConstraints}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.model.layout
 */
public final class GridBagConstraintsInfo extends AbstractGridBagConstraintsInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagConstraintsInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void getCurrentObjectFields(boolean init) throws Exception {
    GridBagConstraints constraints;
    if (init) {
      constraints = (GridBagConstraints) getObject();
      // location
      x = constraints.gridx;
      y = constraints.gridy;
      width = constraints.gridwidth;
      height = constraints.gridheight;
    } else {
      Component component = ((ComponentInfo) getParent()).getComponent();
      // prepare GridBagLayout
      GridBagLayout gridBagLayout;
      {
        LayoutManager layout = component.getParent().getLayout();
        if (!(layout instanceof GridBagLayout)) {
          return;
        }
        gridBagLayout = (GridBagLayout) layout;
      }
      // get constraints
      constraints =
          (GridBagConstraints) ReflectionUtils.invokeMethod(
              gridBagLayout,
              "lookupConstraints(java.awt.Component)",
              component);
      // location
      x = ReflectionUtils.getFieldInt(constraints, "tempX");
      y = ReflectionUtils.getFieldInt(constraints, "tempY");
      width = ReflectionUtils.getFieldInt(constraints, "tempWidth");
      height = ReflectionUtils.getFieldInt(constraints, "tempHeight");
    }
    // fetch fields
    insets = CoordinateUtils.get(constraints.insets);
    anchor = constraints.anchor;
    fill = constraints.fill;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: location
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void materializeLocation() throws Exception {
    GridBagConstraints constraints = (GridBagConstraints) getObject();
    if (constraints.gridy == GridBagConstraints.RELATIVE) {
      constraints.gridy = y;
      setY(y);
    }
    if (constraints.gridx == GridBagConstraints.RELATIVE) {
      constraints.gridx = x;
      setX(x);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access: alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ColumnInfo.Alignment getHorizontalAlignment() {
    return getHorizontalAlignment(fill, anchor);
  }

  public static ColumnInfo.Alignment getHorizontalAlignment(int fill, int anchor) {
    if (fill == GridBagConstraints.BOTH || fill == GridBagConstraints.HORIZONTAL) {
      return ColumnInfo.Alignment.FILL;
    }
    for (AlignmentInfoEx alignment : ALIGNMENTS) {
      if (alignment.equals(anchor)) {
        return alignment.hAlignment;
      }
    }
    throw new IllegalArgumentException(MessageFormat.format(
        "Unknown combination of fill/anchor: {0} {1}",
        fill,
        anchor));
  }

  @Override
  public RowInfo.Alignment getVerticalAlignment() {
    return getVerticalAlignment(fill, anchor);
  }

  public static RowInfo.Alignment getVerticalAlignment(int fill, int anchor) {
    if (fill == GridBagConstraints.BOTH || fill == GridBagConstraints.VERTICAL) {
      return RowInfo.Alignment.FILL;
    }
    for (AlignmentInfoEx alignment : ALIGNMENTS) {
      if (alignment.equals(anchor)) {
        return alignment.vAlignment;
      }
    }
    throw new IllegalArgumentException(MessageFormat.format(
        "Unknown combination of fill/anchor: {0} {1}",
        fill,
        anchor));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AlignmentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static final class AlignmentInfoEx extends AlignmentInfo {
    int anchorValue;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AlignmentInfoEx(String alignmentString, final String fill, final String anchor) {
      // some fields not exists in Java6, so return Integer.MIN_VALUE as anchor.
      super(alignmentString, fill, anchor);
      anchorValue = ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Integer>() {
        public Integer runObject() throws Exception {
          return ReflectionUtils.getFieldInt(GridBagConstraints.class, anchor);
        }
      }, Integer.MIN_VALUE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean equals(int anchor) {
      return anchorValue == anchor;
    }
  }

  private static final AlignmentInfoEx[] ALIGNMENTS = {
      /* NORTH */
      new AlignmentInfoEx("LT", "NONE", "NORTHWEST"),
      new AlignmentInfoEx("CT", "NONE", "NORTH"),
      new AlignmentInfoEx("RT", "NONE", "NORTHEAST"),
      new AlignmentInfoEx("FT", "HORIZONTAL", "NORTH"),
      /* CENTER */
      new AlignmentInfoEx("LC", "NONE", "WEST"),
      new AlignmentInfoEx("CC", "NONE", "CENTER"),
      new AlignmentInfoEx("RC", "NONE", "EAST"),
      new AlignmentInfoEx("FC", "HORIZONTAL", "CENTER"),
      /* SOUTH */
      new AlignmentInfoEx("LB", "NONE", "SOUTHWEST"),
      new AlignmentInfoEx("CB", "NONE", "SOUTH"),
      new AlignmentInfoEx("RB", "NONE", "SOUTHEAST"),
      new AlignmentInfoEx("FB", "HORIZONTAL", "SOUTH"),
      /* FILL */
      new AlignmentInfoEx("LF", "VERTICAL", "WEST"),
      new AlignmentInfoEx("CF", "VERTICAL", "CENTER"),
      new AlignmentInfoEx("RF", "VERTICAL", "EAST"),
      new AlignmentInfoEx("FF", "BOTH", "CENTER"),
      /* PAGE_START */
      new AlignmentInfoEx("LT", "NONE", "FIRST_LINE_START"),
      new AlignmentInfoEx("CT", "NONE", "PAGE_START"),
      new AlignmentInfoEx("RT", "NONE", "FIRST_LINE_END"),
      new AlignmentInfoEx("FT", "HORIZONTAL", "PAGE_START"),
      /* PAGE_END */
      new AlignmentInfoEx("LB", "NONE", "PAGE_END"),
      new AlignmentInfoEx("CB", "NONE", "LAST_LINE_START"),
      new AlignmentInfoEx("RB", "NONE", "LAST_LINE_END"),
      new AlignmentInfoEx("FB", "HORIZONTAL", "PAGE_END"),
      /* PAGE_CENTER */
      new AlignmentInfoEx("LC", "NONE", "LINE_START"),
      new AlignmentInfoEx("RC", "NONE", "LINE_END"),
      /* BASELINE: s */
      new AlignmentInfoEx("Ls", "NONE", "BASELINE_LEADING"),
      new AlignmentInfoEx("Cs", "NONE", "BASELINE"),
      new AlignmentInfoEx("Rs", "NONE", "BASELINE_TRAILING"),
      new AlignmentInfoEx("Fs", "HORIZONTAL", "BASELINE"),
      /* ABOVE_BASELINE: a */
      new AlignmentInfoEx("La", "NONE", "ABOVE_BASELINE_LEADING"),
      new AlignmentInfoEx("Ca", "NONE", "ABOVE_BASELINE"),
      new AlignmentInfoEx("Ra", "NONE", "ABOVE_BASELINE_TRAILING"),
      new AlignmentInfoEx("Fa", "HORIZONTAL", "ABOVE_BASELINE"),
      /* BELOW_BASELINE: b */
      new AlignmentInfoEx("Lb", "NONE", "BELOW_BASELINE_LEADING"),
      new AlignmentInfoEx("Cb", "NONE", "BELOW_BASELINE"),
      new AlignmentInfoEx("Rb", "NONE", "BELOW_BASELINE_TRAILING"),
      new AlignmentInfoEx("Fb", "HORIZONTAL", "BELOW_BASELINE"),};

  @Override
  protected AlignmentInfo[] getAlignments() {
    return ALIGNMENTS;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source utils 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String newInstanceSourceLong() {
    return "new java.awt.GridBagConstraints("
        + "java.awt.GridBagConstraints.RELATIVE, "
        + "java.awt.GridBagConstraints.RELATIVE, "
        + "1, 1, 0.0, 0.0, "
        + "java.awt.GridBagConstraints.CENTER, "
        + "java.awt.GridBagConstraints.NONE, "
        + "new java.awt.Insets(0, 0, 0, 0), 0, 0)";
  }

  @Override
  public String newInstanceSourceShort() {
    return "new java.awt.GridBagConstraints()";
  }
}
