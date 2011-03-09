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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.MigLayout.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.DimConstraint;
import net.miginfocom.layout.IDEUtil;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Description for column {@link DimConstraint} in {@link MigLayout}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class MigColumnInfo extends MigDimensionInfo {
  public enum Alignment {
    UNKNOWN, DEFAULT, LEFT, CENTER, RIGHT, FILL, LEADING, TRAILING;
    /**
     * @return the small image (5x9) to display current alignment to user.
     */
    public Image getSmallImage() {
      return Activator.getImage("alignment/h/small/" + name().toLowerCase() + ".gif");
    }

    /**
     * @return the big image (16x16) to display for user in menu.
     */
    public ImageDescriptor getMenuImage() {
      return Activator.getImageDescriptor("alignment/h/menu/" + name().toLowerCase() + ".gif");
    }

    /**
     * @return the text to use for menu item or toolbar tooltip.
     */
    public String getText() {
      return StringUtils.capitalize(name().toLowerCase());
    }
  }

  public static final Alignment[] ALIGNMENTS_TO_SET = new Alignment[]{
      Alignment.DEFAULT,
      Alignment.LEFT,
      Alignment.CENTER,
      Alignment.RIGHT,
      Alignment.FILL,
      Alignment.LEADING,
      Alignment.TRAILING};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MigColumnInfo(MigLayoutInfo layout) {
    super(layout, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void delete() throws Exception {
    m_layout.deleteColumn(getIndex());
  }

  @Override
  public List<? extends MigDimensionInfo> getSiblings() {
    return m_layout.getColumns();
  }

  @Override
  protected DimConstraint fetchConstraint() {
    int index = getIndex();
    AC colSpecs = (AC) ReflectionUtils.getFieldObject(m_layout.getObject(), "colSpecs");
    if (index < colSpecs.getCount()) {
      return colSpecs.getConstaints()[index];
    } else {
      return createDefaultConstraint();
    }
  }

  @Override
  protected DimConstraint createDefaultConstraint() {
    return ConstraintParser.parseColumnConstraints("[]").getConstaints()[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the default {@link Alignment} for components in this {@link MigColumnInfo}.
   */
  public Alignment getAlignment(boolean resolveDefault) {
    return getAlignment(m_constraint, true, resolveDefault);
  }

  /**
   * Sets default {@link Alignment} for components in this {@link MigColumnInfo}.
   */
  public void setAlignment(Alignment alignment) {
    setAlignment(m_constraint, alignment, true);
  }

  /**
   * @return the {@link Alignment} enum converted from alignment of {@link DimConstraint}.
   */
  static Alignment getAlignment(DimConstraint constraint, boolean forColumn, boolean resolveDefault) {
    if (forColumn && constraint.isFill()) {
      return Alignment.FILL;
    }
    if (!forColumn && constraint.getGrow() != null) {
      return Alignment.FILL;
    }
    // convert to enum
    UnitValue alignment =
        resolveDefault ? constraint.getAlignOrDefault(true) : constraint.getAlign();
    if (alignment == null) {
      return Alignment.DEFAULT;
    } else if (alignment == IDEUtil.LEFT) {
      return Alignment.LEFT;
    } else if (alignment == IDEUtil.CENTER) {
      return Alignment.CENTER;
    } else if (alignment == IDEUtil.RIGHT) {
      return Alignment.RIGHT;
    } else if (alignment == IDEUtil.LEADING) {
      return Alignment.LEADING;
    } else if (alignment == IDEUtil.TRAILING) {
      return Alignment.TRAILING;
    }
    // unknown
    return Alignment.UNKNOWN;
  }

  /**
   * Updates {@link DimConstraint} with {@link Alignment} enum value.
   */
  static void setAlignment(DimConstraint constraint, Alignment alignment, boolean forColumn) {
    Assert.isNotNull(alignment);
    // set default alignment
    if (alignment == Alignment.DEFAULT) {
      constraint.setAlign(null);
      if (forColumn) {
        constraint.setFill(false);
      } else {
        constraint.setGrow(null);
      }
      return;
    }
    // special support for FILL
    if (alignment == Alignment.FILL) {
      if (forColumn) {
        constraint.setFill(true);
      } else {
        constraint.setGrow(100f);
      }
      constraint.setAlign(null);
      return;
    }
    // prepare alignment as String
    UnitValue alignmentValue;
    switch (alignment) {
      case LEFT :
        alignmentValue = IDEUtil.LEFT;
        break;
      case CENTER :
        alignmentValue = IDEUtil.CENTER;
        break;
      case RIGHT :
        alignmentValue = IDEUtil.RIGHT;
        break;
      case LEADING :
        alignmentValue = IDEUtil.LEADING;
        break;
      case TRAILING :
        alignmentValue = IDEUtil.TRAILING;
        break;
      default :
        throw new IllegalArgumentException("Can not set UNKNOWN alignment.");
    }
    // parse alignment, update constraint
    constraint.setAlign(alignmentValue);
    if (!forColumn) {
      constraint.setGrow(null);
    } else {
      constraint.setFill(false);
    }
  }
}
