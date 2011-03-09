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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for {@link SashForm}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class SashFormInfo extends CompositeInfo implements ISashFormInfo<ControlInfo> {
  private final SashFormInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SashFormInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // when remove ControlInfo: remove weight
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child instanceof ControlInfo && parent == m_this) {
          removeWeight((ControlInfo) child);
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        if (child instanceof ControlInfo && oldParent == m_this && oldParent != newParent) {
          removeWeight((ControlInfo) child);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isHorizontal() {
    return ControlSupport.isStyle(getObject(), SWT.HORIZONTAL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(ControlInfo control, ControlInfo nextControl) throws Exception {
    ensureWeights();
    JavaInfoUtils.add(control, null, this, nextControl);
    addWeight(control);
  }

  public void command_MOVE(ControlInfo control, ControlInfo nextControl) throws Exception {
    ensureWeights();
    int oldIndex = getChildrenControls().indexOf(control);
    JavaInfoUtils.move(control, null, this, nextControl);
    // update weights
    if (oldIndex == -1) {
      addWeight(control);
    } else {
      moveWeight(control, oldIndex);
    }
  }

  public void command_RESIZE(ControlInfo control, int size) throws Exception {
    ensureWeights();
    List<ControlInfo> children = getChildrenControls();
    // prepare weights as current sizes
    int[] weights = new int[children.size()];
    for (int i = 0; i < children.size(); i++) {
      ControlInfo child = children.get(i);
      Rectangle bounds = child.getModelBounds();
      weights[i] = isHorizontal() ? bounds.width : bounds.height;
    }
    // update adjacent weights
    int index = children.indexOf(control);
    int sumWeight = weights[index] + weights[index + 1];
    weights[index + 1] = Math.max(0, sumWeight - size);
    weights[index] = sumWeight - weights[index + 1];
    // set weights
    setWeights(weights);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Weights operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds weight for {@link ControlInfo} that is already in children.
   */
  private void addWeight(ControlInfo newControl) throws Exception {
    ArrayInitializer weightsExpression = ensureWeights();
    int[] weights = getWeights(weightsExpression);
    // prepare weight for "newControl"
    int newWeight;
    if (weights.length == 0) {
      newWeight = 1;
    } else {
      newWeight = 0;
      for (int i = 0; i < weights.length; i++) {
        newWeight += weights[i];
      }
      newWeight /= weights.length;
    }
    // add weight
    int newIndex = getChildrenControls().indexOf(newControl);
    getEditor().addArrayElement(
        weightsExpression,
        newIndex,
        IntegerConverter.INSTANCE.toJavaSource(this, newWeight));
  }

  /**
   * Moves weight for {@link ControlInfo} that is already moved in children.
   */
  private void moveWeight(ControlInfo newControl, int oldIndex) throws Exception {
    ArrayInitializer weightsExpression = ensureWeights();
    int newIndex = getChildrenControls().indexOf(newControl);
    getEditor().moveArrayElement(weightsExpression, weightsExpression, oldIndex, newIndex);
  }

  /**
   * Removes weight for {@link ControlInfo} that is still in children.
   */
  private void removeWeight(ControlInfo control) throws Exception {
    MethodInvocation invocation = getMethodInvocation("setWeights(int[])");
    if (invocation != null) {
      ArrayInitializer weightsExpression =
          ((ArrayCreation) DomGenerics.arguments(invocation).get(0)).getInitializer();
      int index = getChildrenControls().indexOf(control);
      getEditor().removeArrayElement(weightsExpression, index);
    }
  }

  /**
   * Ensures that this {@link SashFormInfo} has "setWeights()" invocation.
   * 
   * @return the {@link ArrayInitializer} for weights.
   */
  private ArrayInitializer ensureWeights() throws Exception {
    MethodInvocation invocation = getMethodInvocation("setWeights(int[])");
    if (invocation == null) {
      String elementsSource;
      {
        elementsSource = StringUtils.repeat("1, ", getChildrenControls().size());
        elementsSource = StringUtils.removeEnd(elementsSource, ", ");
      }
      // add invocation
      String arraySource = "new int[] {" + elementsSource + "}";
      invocation = addMethodInvocation("setWeights(int[])", arraySource);
    }
    return ((ArrayCreation) DomGenerics.arguments(invocation).get(0)).getInitializer();
  }

  /**
   * @param weightsExpression
   *          the argument of {@link SashForm#setWeights(int[])} argument.
   * 
   * @return the current weights.
   */
  private int[] getWeights(ArrayInitializer weightsExpression) throws Exception {
    EvaluationContext context;
    {
      AstEditor editor = getEditor();
      EditorState state = EditorState.get(editor);
      ClassLoader editorLoader = state.getEditorLoader();
      context = new EvaluationContext(editorLoader, state.getFlowDescription());
    }
    // evaluate
    return (int[]) AstEvaluationEngine.evaluate(context, weightsExpression);
  }

  /**
   * Sets the weights for {@link SashForm#setWeights(int[])}.
   */
  private void setWeights(int[] weights) throws Exception {
    String arraySource;
    {
      arraySource = "new int[] {";
      for (int i = 0; i < weights.length; i++) {
        if (i > 0) {
          arraySource += ", ";
        }
        arraySource += weights[i];
      }
      arraySource += "}";
    }
    // replace "setWeight()" invocation
    removeMethodInvocations("setWeights(int[])");
    addMethodInvocation("setWeights(int[])", arraySource);
  }
}
