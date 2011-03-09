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

import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.property.converter.DoubleConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

import org.apache.commons.lang.StringUtils;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Entry point for operations with {@link DimensionInfo}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public abstract class DimensionOperations<T extends DimensionInfo> {
  protected final AbstractGridBagLayoutInfo m_layout;
  protected final AstEditor m_editor;
  private final String m_sizeField;
  private final String m_weightField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionOperations(AbstractGridBagLayoutInfo layout, String sizeField, String weightField) {
    m_layout = layout;
    m_editor = layout.getEditor();
    m_sizeField = sizeField;
    m_weightField = weightField;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link DimensionInfo} has not components in it.
   */
  public abstract boolean isEmpty(int index);

  /**
   * @return the {@link ClipboardCommand} for copying dimensions information.
   */
  public ClipboardCommand getClipboardCommand() {
    LinkedList<T> dimensions = getDimensions();
    final boolean forColumns = this instanceof DimensionOperationsColumn;
    final int count = dimensions.size();
    final int[] sizeArray = new int[count];
    final double[] weightArray = new double[count];
    for (int i = 0; i < count; i++) {
      T dimension = dimensions.get(i);
      sizeArray[i] = dimension.getSize();
      weightArray[i] = dimension.getWeight();
    }
    // create command
    return new ComponentClipboardCommand<ContainerInfo>() {
      private static final long serialVersionUID = 0L;

      @Override
      @SuppressWarnings("unchecked")
      protected void execute(ContainerInfo container) throws Exception {
        AbstractGridBagLayoutInfo layout = (AbstractGridBagLayoutInfo) container.getLayout();
        DimensionOperations<T> operations =
            (DimensionOperations<T>) (forColumns
                ? layout.getColumnOperations()
                : layout.getRowOperations());
        operations.prepare(count - 1, false);
        LinkedList<T> dimensions = operations.getDimensions();
        for (int i = 0; i < count; i++) {
          T dimension = dimensions.get(i);
          dimension.setSize(sizeArray[i]);
          dimension.setWeight(weightArray[i]);
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Inserts new empty {@link DimensionInfo} before given index.
   */
  public abstract T insert(int index) throws Exception;

  /**
   * Deletes {@link DimensionInfo} with given index and all components located in it.
   */
  public final void delete(final int index) throws Exception {
    // delete components, update constraints
    m_layout.visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        int location = getLocation(constraints);
        int size = getSize(constraints);
        if (location == index) {
          component.delete();
        } else if (location > index) {
          setLocation(constraints, location - 1);
        } else if (location + size > index) {
          setSize(constraints, size - 1);
        }
      }
    });
    // remove dimension
    {
      getDimensions().remove(index);
      removeFieldArrayElement(m_sizeField, index);
      removeFieldArrayElement(m_weightField, index);
    }
    // fix gaps
    m_layout.ensureGapInsets();
  }

  /**
   * Deletes {@link ComponentInfo}'s located in {@link DimensionInfo} with given index, but does not
   * delete {@link DimensionInfo} itself.
   */
  public final void clear(final int index) throws Exception {
    m_layout.visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        if (getLocation(constraints) == index) {
          component.delete();
        }
      }
    });
  }

  /**
   * Splits {@link DimensionInfo} with given index, i.e. adds new {@link DimensionInfo} with same
   * size/width and spans all components to both {@link DimensionInfo}.
   */
  public final void split(final int index) throws Exception {
    // insert dimension
    insert(index + 1);
    // copy size/weight
    {
      List<T> dimensions = getDimensions();
      T dimension = dimensions.get(index);
      T newDimension = dimensions.get(index + 1);
      newDimension.setSize(dimension.getSize());
      newDimension.setWeight(dimension.getWeight());
    }
    // span components
    m_layout.visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        int location = getLocation(constraints);
        int size = getSize(constraints);
        if (location + size - 1 == index) {
          setSize(constraints, size + 1);
        }
      }
    });
  }

  /**
   * Moves {@link DimensionInfo} with given index into target index.
   */
  public void move(int index, final int targetIndex) throws Exception {
    insert(targetIndex);
    final int sourceIndex = targetIndex < index ? index + 1 : index;
    // transfer size/weight into newly inserted dimension
    exchangeFieldArrayElement(m_sizeField, sourceIndex, targetIndex);
    exchangeFieldArrayElement(m_weightField, sourceIndex, targetIndex);
    // exchange dimensions
    {
      List<T> dimensions = getDimensions();
      T sourceDimension = dimensions.get(sourceIndex);
      T targetDimension = dimensions.get(targetIndex);
      dimensions.set(sourceIndex, targetDimension);
      dimensions.set(targetIndex, sourceDimension);
    }
    // move components
    m_layout.visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        if (getLocation(constraints) == sourceIndex) {
          moveComponent(component, constraints, targetIndex);
          setSize(constraints, 1);
        }
      }
    });
    // delete old dimension
    delete(sourceIndex);
    m_layout.ensureGapInsets();
  }

  /**
   * If there are components that span multiple dimensions, and no other "real" components in these
   * dimensions, then removes these excess dimensions.
   */
  public final void normalizeSpanning() throws Exception {
    LinkedList<T> dimensions = getDimensions();
    // prepare filled dimensions
    final boolean[] filledDimensions = new boolean[dimensions.size()];
    m_layout.visitComponents(new IComponentVisitor() {
      public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
          throws Exception {
        int location = getLocation(constraints);
        filledDimensions[location] = true;
      }
    });
    // remove empty dimensions
    for (int index = dimensions.size() - 1; index >= 0; index--) {
      if (!filledDimensions[index]) {
        delete(index);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Cuts dimensions to the size rendered by {@link GridBagLayout}.<br>
   * We don't analyze that there are empty columns/rows ourselves. Instead we just allow
   * {@link GridBagLayout} to this for us and obey it - remove {@link DimensionInfo}'s and cut
   * "weight" arrays.
   * 
   * @return <code>true</code> if any dimension was removed.
   */
  boolean cutToGrid(int renderedSize) throws Exception {
    LinkedList<T> dimensions = getDimensions();
    // cut dimensions
    boolean removedAnyDimension = false;
    while (dimensions.size() > renderedSize) {
      dimensions.removeLast();
      removedAnyDimension = true;
    }
    // cut "weight"
    {
      ArrayInitializer initializer = getFieldArrayInitializer(m_weightField);
      if (initializer != null) {
        while (initializer.expressions().size() > renderedSize) {
          m_editor.removeArrayElement(initializer, initializer.expressions().size() - 1);
        }
      }
    }
    //
    return removedAnyDimension;
  }

  /**
   * Ensures that {@link DimensionInfo} with given index exists in {@link AbstractGridBagLayoutInfo}
   * , may be inserts/appends new {@link DimensionInfo}'s.
   */
  void prepare(final int index, boolean insert) throws Exception {
    // prepare dimensions
    {
      List<T> dimensions = getDimensions();
      if (insert) {
        addNewDimension(dimensions, index);
      } else {
        while (index >= dimensions.size()) {
          addNewDimension(dimensions, dimensions.size());
        }
      }
    }
    // move components
    if (insert) {
      m_layout.visitComponents(new IComponentVisitor() {
        public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
            throws Exception {
          int location = getLocation(constraints);
          int size = getSize(constraints);
          if (location >= index) {
            setLocation(constraints, location + 1);
          } else if (location + size > index) {
            setSize(constraints, size + 1);
          }
        }
      });
    }
  }

  /**
   * Adds new {@link DimensionInfo} into collection, may be updates also size/weight arrays.
   */
  private void addNewDimension(List<T> dimensions, int index) throws Exception {
    T dimension = newDimension();
    dimensions.add(index, dimension);
    // update size/weight arrays
    insertFieldArrayElement(m_sizeField, index, "0");
    insertFieldArrayElement(m_weightField, index, "0.0");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the all {@link DimensionInfo}'s.
   */
  protected abstract LinkedList<T> getDimensions();

  /**
   * @return the new {@link DimensionInfo} instance.
   */
  protected abstract T newDimension();

  /**
   * Moves {@link ComponentInfo} into given location.
   */
  protected abstract void moveComponent(ComponentInfo component,
      AbstractGridBagConstraintsInfo constraints,
      int location) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal GridBagConstraintsInfo operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the x/y location.
   */
  protected abstract int getLocation(AbstractGridBagConstraintsInfo constraints);

  /**
   * @return the width/height size.
   */
  protected abstract int getSize(AbstractGridBagConstraintsInfo constraints);

  /**
   * Sets the x/y location.
   */
  protected abstract void setLocation(AbstractGridBagConstraintsInfo constraints, int location)
      throws Exception;

  /**
   * Sets the width/height size.
   */
  protected abstract void setSize(AbstractGridBagConstraintsInfo constraints, int size)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size/weight arrays operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the (optional, may be <code>null</code>) element of minimum size field.
   */
  Expression getSizeFieldElement(int index) {
    return getFieldArrayElement(index, m_sizeField);
  }

  /**
   * @return the (optional, may be <code>null</code>) element of weight field.
   */
  Expression getWeightFieldElement(int index) {
    return getFieldArrayElement(index, m_weightField);
  }

  /**
   * Sets the minimum size of {@link DimensionInfo}.
   */
  void setSizeFieldElement(int index, int value) throws Exception {
    String source = IntegerConverter.INSTANCE.toJavaSource(m_layout, value);
    setFieldArrayElement(m_sizeField, "int", "0", index, source);
  }

  /**
   * Sets the weight of {@link DimensionInfo}.
   */
  void setWeightFieldElement(int index, double value) throws Exception {
    String source = DoubleConverter.INSTANCE.toJavaSource(m_layout, value);
    setFieldArrayElement(m_weightField, "double", "0.0", index, source);
  }

  /**
   * Sets the value for element of {@link ArrayInitializer} assigned to field.<br>
   * If there are no assignment to this field, adds assignment.
   */
  private void setFieldArrayElement(String fieldName,
      String typeName,
      String emptyElementSource,
      int index,
      String elementSource) throws Exception {
    ArrayInitializer initializer = getFieldArrayInitializer(fieldName);
    // ensure assignment
    if (initializer == null) {
      String initializerSource =
          StringUtils.repeat(", " + emptyElementSource, getDimensions().size()).substring(2);
      String fieldSource = MessageFormat.format("new {0}[]'{'{1}'}'", typeName, initializerSource);
      m_layout.addFieldAssignment(fieldName, fieldSource);
      initializer = getFieldArrayInitializer(fieldName);
    }
    // replace element
    m_editor.replaceExpression(DomGenerics.expressions(initializer).get(index), elementSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field arrays operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ArrayInitializer} of (optional) field assignment, such as
   *         {@link GridBagLayout#columnWidths}, or <code>null</code>, if no assignments to this
   *         field exists.
   */
  private ArrayInitializer getFieldArrayInitializer(String fieldName) {
    Assignment assignment = m_layout.getFieldAssignment(fieldName);
    if (assignment != null && assignment.getRightHandSide() instanceof ArrayCreation) {
      ArrayCreation arrayCreation = (ArrayCreation) assignment.getRightHandSide();
      return arrayCreation.getInitializer();
    }
    return null;
  }

  /**
   * @return the element of {@link ArrayInitializer} of (optional) field assignment, such as
   *         {@link GridBagLayout#columnWidths}, or <code>null</code>, if no assignments to this
   *         field exists, or given index is greater than size of {@link ArrayInitializer}.
   */
  private Expression getFieldArrayElement(int index, String fieldName) {
    ArrayInitializer initializer = getFieldArrayInitializer(fieldName);
    if (initializer != null && initializer.expressions().size() > index) {
      return DomGenerics.expressions(initializer).get(index);
    }
    return null;
  }

  /**
   * Inserts new element into (optional) field assignment, such as
   * {@link GridBagLayout#columnWidths}. Does nothing, if no assignments to this field exists.
   */
  private void insertFieldArrayElement(String fieldName, int index, String source) throws Exception {
    ArrayInitializer arrayInitializer = getFieldArrayInitializer(fieldName);
    if (arrayInitializer != null) {
      while (arrayInitializer.expressions().size() < index) {
        int i = arrayInitializer.expressions().size();
        m_editor.addArrayElement(arrayInitializer, i, source);
      }
      m_editor.addArrayElement(arrayInitializer, index, source);
    }
  }

  /**
   * Removes element from field assignment, such as {@link GridBagLayout#columnWidths}. Does
   * nothing, if no assignments to this field exists.
   */
  private void removeFieldArrayElement(String fieldName, int index) throws Exception {
    ArrayInitializer arrayInitializer = getFieldArrayInitializer(fieldName);
    if (arrayInitializer != null) {
      m_editor.removeArrayElement(arrayInitializer, index);
    }
  }

  /**
   * Exchanges two elements of field assignment, such as {@link GridBagLayout#columnWidths}. Does
   * nothing, if no assignments to this field exists.
   */
  private void exchangeFieldArrayElement(String fieldName, int index_1, int index_2)
      throws Exception {
    ArrayInitializer arrayInitializer = getFieldArrayInitializer(fieldName);
    if (arrayInitializer != null) {
      m_editor.exchangeArrayElements(arrayInitializer, index_1, index_2);
    }
  }
}
