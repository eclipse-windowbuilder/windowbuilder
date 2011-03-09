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

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundProcessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import java.util.List;

/**
 * {@link ISurroundProcessor} that places enclosing {@link ComponentInfo}'s into same relative
 * cells, as they were before enclosing. It works only if source {@link ContainerInfo} has
 * {@link GridBagLayoutInfo} and sets also {@link GridBagLayoutInfo} on target {@link ContainerInfo}
 * .
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class GridBagLayoutSurroundProcessor
    implements
      ISurroundProcessor<ContainerInfo, ComponentInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Object INSTANCE = new GridBagLayoutSurroundProcessor();

  private GridBagLayoutSurroundProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ISurroundProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean filter(ContainerInfo sourceContainer, ContainerInfo targetContainer)
      throws Exception {
    String targetClassName = targetContainer.getDescription().getComponentClass().getName();
    boolean isJPanel = targetClassName.equals("javax.swing.JPanel");
    return sourceContainer.hasLayout()
        && sourceContainer.getLayout() instanceof GridBagLayoutInfo
        && isJPanel;
  }

  public void move(ContainerInfo sourceContainer,
      ContainerInfo targetContainer,
      List<ComponentInfo> components) throws Exception {
    // set GridBagLayoutInfo for target
    GridBagLayoutInfo targetLayout;
    {
      targetLayout =
          (GridBagLayoutInfo) JavaInfoUtils.createJavaInfo(
              targetContainer.getEditor(),
              "java.awt.GridBagLayout",
              new ConstructorCreationSupport());
      targetContainer.setLayout(targetLayout);
    }
    // configure target layout
    Point locationOffset;
    {
      Rectangle targetBounds =
          (Rectangle) targetContainer.getArbitraryValue(GridBagLayoutSurroundSupport.CELLS_KEY);
      GridBagLayoutInfo sourceLayout = (GridBagLayoutInfo) sourceContainer.getLayout();
      // copy columns
      copyDimensions(
          targetLayout.getColumnOperations(),
          sourceLayout.getColumnOperations(),
          targetBounds.x,
          targetBounds.right());
      // copy rows
      copyDimensions(
          targetLayout.getRowOperations(),
          sourceLayout.getRowOperations(),
          targetBounds.y,
          targetBounds.bottom());
      // offset for components to move
      locationOffset = targetBounds.getLocation().getNegated();
    }
    // move components
    for (ComponentInfo component : components) {
      GridBagConstraintsInfo oldConstraints = GridBagLayoutInfo.getConstraintsFor(component);
      // move component
      {
        Rectangle cells = GridBagLayoutSurroundSupport.getCells(component);
        cells = cells.getTranslated(locationOffset);
        targetLayout.command_MOVE(component, cells.x, false, cells.y, false);
      }
      // update constraints
      {
        GridBagConstraintsInfo constraints = GridBagLayoutInfo.getConstraintsFor(component);
        constraints.setWidth(oldConstraints.width);
        constraints.setHeight(oldConstraints.height);
        constraints.setAlignment(
            oldConstraints.getHorizontalAlignment(),
            oldConstraints.getVerticalAlignment());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copies {@link DimensionInfo}'s from source to target {@link DimensionOperations}.
   */
  private static <T extends DimensionInfo> void copyDimensions(DimensionOperations<T> targetOperations,
      DimensionOperations<T> sourceOperations,
      int beginIndex,
      int endIndex) throws Exception {
    for (int sourceIndex = beginIndex; sourceIndex < endIndex; sourceIndex++) {
      T sourceDimension = sourceOperations.getDimensions().get(sourceIndex);
      // prepare targetDimension
      T targetDimension;
      {
        int targetIndex = sourceIndex - beginIndex;
        targetOperations.prepare(targetIndex, false);
        targetDimension = targetOperations.getDimensions().get(targetIndex);
      }
      // copy properties
      targetDimension.setSize(sourceDimension.getSize());
      targetDimension.setWeight(sourceDimension.getWeight());
    }
  }
}
