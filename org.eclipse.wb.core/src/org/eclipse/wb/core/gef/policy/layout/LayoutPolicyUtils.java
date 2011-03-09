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
package org.eclipse.wb.core.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import java.util.List;

/**
 * Contains utilities for {@link LayoutEditPolicy}'s.
 * 
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class LayoutPolicyUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutEditPolicy creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link LayoutEditPolicy} for given model.
   */
  public static LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    // try to create policy
    List<ILayoutEditPolicyFactory> factories =
        ExternalFactoriesHelper.getElementsInstances(
            ILayoutEditPolicyFactory.class,
            "org.eclipse.wb.core.editPolicyFactories",
            "factory");
    for (ILayoutEditPolicyFactory factory : factories) {
      LayoutEditPolicy layoutEditPolicy = factory.createLayoutEditPolicy(context, model);
      if (layoutEditPolicy != null) {
        return layoutEditPolicy;
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Side figures utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks that selection edit policy should display side figures for given edit part.
   */
  public static boolean shouldShowSideFigures(String figuresMode, EditPart part) {
    return part.getSelected() == EditPart.SELECTED_PRIMARY;
    // TODO implement full check
    /*boolean showFiguresAll = IDesignerPrefConstants.V_ALIGNMENT_FIGURES_ALL.equals(figuresMode);
    if (showFiguresAll) {
    	return true;
    }
    //
    boolean isSelected = part.getSelected() != EditPart.SELECTED_NONE;
    boolean isPrimarySelected = part.getSelected() == EditPart.SELECTED_PRIMARY;
    boolean showFiguresSelected = IDesignerPrefConstants.V_ALIGNMENT_FIGURES_SELECTED.equals(figuresMode);
    boolean showFiguresPrimarySelected = IDesignerPrefConstants.V_ALIGNMENT_FIGURES_PRIMARY_SELECTED.equals(figuresMode);
    return (showFiguresSelected && isSelected) || (showFiguresPrimarySelected && isPrimarySelected);*/
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Command} that performs generic {@link JavaInfo} paste operation.
   */
  public static <T extends JavaInfo> Command getPasteCommand(final JavaInfo existingHierarchyObject,
      final PasteRequest request,
      final Class<T> componentClass,
      final IPasteProcessor<T> processor) {
    @SuppressWarnings("unchecked")
    final List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Command>() {
      public Command runObject() throws Exception {
        // prepare models
        final List<JavaInfo> components;
        {
          components = Lists.newArrayList();
          for (JavaInfoMemento memento : mementos) {
            JavaInfo javaInfo = memento.create(existingHierarchyObject);
            if (componentClass.isAssignableFrom(javaInfo.getClass())) {
              components.add(javaInfo);
            } else {
              return null;
            }
          }
          // set objects for selection
          request.setObjects(components);
        }
        // create command
        return new EditCommand(existingHierarchyObject) {
          @Override
          @SuppressWarnings("unchecked")
          protected void executeEdit() throws Exception {
            for (int i = 0; i < components.size(); i++) {
              processor.process((T) components.get(i));
              mementos.get(i).apply();
            }
          }
        };
      }
    }, null);
  }

  /**
   * Performs some concrete operation during {@link JavaInfo} pasting.
   */
  public interface IPasteProcessor<T extends JavaInfo> {
    /**
     * Performs some action for given {@link JavaInfo} (in most case - adds given component).
     */
    void process(T component) throws Exception;
  }
}
