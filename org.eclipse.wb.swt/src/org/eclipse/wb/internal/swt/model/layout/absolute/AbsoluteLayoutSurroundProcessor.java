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
package org.eclipse.wb.internal.swt.model.layout.absolute;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundProcessor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Point;

import java.util.List;

/**
 * {@link ISurroundProcessor} that places enclosing {@link ControlInfo}'s into same visual positions
 * as they were before enclosing. It works only if source {@link CompositeInfo} has
 * {@link AbsoluteLayoutInfo} and sets also {@link AbsoluteLayoutInfo} on target
 * {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class AbsoluteLayoutSurroundProcessor
    implements
      ISurroundProcessor<CompositeInfo, ControlInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Object INSTANCE = new AbsoluteLayoutSurroundProcessor();

  private AbsoluteLayoutSurroundProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ISurroundProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean filter(CompositeInfo sourceContainer, CompositeInfo targetContainer)
      throws Exception {
    String targetClassName = targetContainer.getDescription().getComponentClass().getName();
    boolean isComposite = targetClassName.equals("org.eclipse.swt.widgets.Composite");
    boolean isGroup = targetClassName.equals("org.eclipse.swt.widgets.Group");
    return sourceContainer.hasLayout()
        && sourceContainer.getLayout() instanceof AbsoluteLayoutInfo
        && (isComposite || isGroup);
  }

  @Override
  public void move(CompositeInfo sourceContainer,
      CompositeInfo targetContainer,
      List<ControlInfo> components) throws Exception {
    // prepare absolute layout for target
    AbsoluteLayoutInfo targetLayout = (AbsoluteLayoutInfo) targetContainer.getLayout();
    // prepare expanded bounds for "targetContainer"
    Point locationOffset;
    {
      Rectangle targetBounds =
          (Rectangle) targetContainer.getArbitraryValue(AbsoluteLayoutSurroundSupport.BOUNDS_KEY);
      targetBounds.expand(targetContainer.getClientAreaInsets2());
      // prepare offset for components
      locationOffset = targetBounds.getLocation().getNegated();
      // set for "targetContainer" expanded bounds
      AbsoluteLayoutInfo sourceLayout = (AbsoluteLayoutInfo) sourceContainer.getLayout();
      sourceLayout.commandChangeBounds(
          targetContainer,
          targetBounds.getLocation(),
          targetBounds.getSize());
    }
    // move components
    for (ControlInfo component : components) {
      Rectangle bounds = component.getModelBounds().getTranslated(locationOffset);
      targetLayout.command_MOVE(component, null);
      targetLayout.commandChangeBounds(component, bounds.getLocation(), bounds.getSize());
    }
  }
}
