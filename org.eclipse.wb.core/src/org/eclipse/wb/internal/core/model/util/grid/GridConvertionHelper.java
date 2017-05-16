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
package org.eclipse.wb.internal.core.model.util.grid;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Helper for converting absolute bounds into grid.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public class GridConvertionHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GridConvertionHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Building groups
  //
  ////////////////////////////////////////////////////////////////////////////
  public static List<ComponentGroup> buildGroups(List<? extends IAbstractComponentInfo> components,
      boolean horizontal) {
    List<ComponentInGroup> groupComponents = Lists.newArrayList();
    for (IAbstractComponentInfo component : components) {
      ComponentInGroup groupComponent =
          new ComponentInGroup(component, component.getModelBounds(), horizontal);
      groupComponents.add(groupComponent);
    }
    return buildGroups(groupComponents);
  }

  private static List<ComponentGroup> buildGroups(List<ComponentInGroup> components) {
    // sort by begins
    Collections.sort(components, new Comparator<ComponentInGroup>() {
      public int compare(ComponentInGroup o1, ComponentInGroup o2) {
        return o1.getMin() - o2.getMin();
      }
    });
    // create groups
    List<ComponentGroup> groups = Lists.newArrayList();
    for (ComponentInGroup component : components) {
      // create group for this component
      ComponentGroup group = new ComponentGroup();
      int value = component.getMin();
      for (ComponentInGroup component2 : components) {
        if (component2.contains(value)) {
          group.add(component2);
        }
      }
      groups.add(group);
    }
    // remove sub-groups
    for (Iterator<ComponentGroup> I = groups.iterator(); I.hasNext();) {
      ComponentGroup subSetGroup = I.next();
      if (isSubGroupInGroups(subSetGroup, groups)) {
        I.remove();
      }
    }
    //
    return groups;
  }

  private static boolean isSubGroupInGroups(ComponentGroup subSetGroup, List<ComponentGroup> groups) {
    for (ComponentGroup superSetGroup : groups) {
      if (superSetGroup != subSetGroup
          && superSetGroup.getComponents().containsAll(subSetGroup.getComponents())) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sorting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sorts given {@link List} of {@link ComponentGroup}'s by beginnings.
   */
  public static void sortGroups(final List<ComponentGroup> groups) {
    Collections.sort(groups, new Comparator<ComponentGroup>() {
      public int compare(ComponentGroup group_1, ComponentGroup group_2) {
        int value_1 = group_1.getMinOfBegins(groups);
        int value_2 = group_2.getMinOfBegins(groups);
        return value_1 - value_2;
      }
    });
  }

  /**
   * Sorts {@link ComponentGroup}'s by beginnings in given transposed groups.
   */
  public static void sortGroupsByTranspose(List<ComponentGroup> groups,
      List<ComponentGroup> t_groups) {
    for (ComponentGroup group : groups) {
      sortGroupByTranspose(group, t_groups);
    }
  }

  /**
   * Sorts {@link ComponentGroup} by beginnings in given transposed groups.
   */
  private static void sortGroupByTranspose(final ComponentGroup group,
      final List<ComponentGroup> t_groups) {
    Collections.sort(group.getComponents(), new Comparator<ComponentInGroup>() {
      public int compare(ComponentInGroup component_1, ComponentInGroup component_2) {
        return findTComponent(component_1).getMin() - findTComponent(component_2).getMin();
      }

      /**
       * @return the {@link ComponentInGroup} from <code>t_groups</code> for given
       *         {@link ComponentInGroup} from <code>group</code>.
       */
      private ComponentInGroup findTComponent(ComponentInGroup component) {
        for (ComponentGroup tGroup : t_groups) {
          for (ComponentInGroup tComponent : tGroup.getComponents()) {
            if (tComponent.equals(component)) {
              return tComponent;
            }
          }
        }
        // should not happen
        throw new IllegalStateException("Can not find 't' for component: " + component);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds and gaps
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets min/max for each {@link ComponentGroup} in {@link List}.<br>
   * Optionally uses parent size as "max" for last {@link ComponentGroup}.
   *
   * Also creates gap {@link ComponentGroup}'s.
   */
  public static void updateBoundsGaps(List<ComponentGroup> groups, boolean addGaps) {
    List<ComponentGroup> newGroups = Lists.newArrayList();
    //
    for (int i = 0; i < groups.size(); i++) {
      ComponentGroup group = groups.get(i);
      // update min/max
      group.m_min = group.getMinOfBegins(groups);
      group.m_max = group.getMaxOfEnds(groups);
      // add gap
      if (addGaps) {
        if (i == 0) {
          if (group.m_min > 0) {
            ComponentGroup gapGroup = new ComponentGroup();
            gapGroup.m_min = 0;
            gapGroup.m_max = group.m_min;
            newGroups.add(gapGroup);
          }
        } else {
          ComponentGroup prevGroup = groups.get(i - 1);
          // add gap between previous and this groups
          if (group.m_min - prevGroup.m_max > 0) {
            // prepare gap
            ComponentGroup gapGroup = new ComponentGroup();
            gapGroup.m_min = prevGroup.m_max;
            gapGroup.m_max = group.m_min;
            newGroups.add(gapGroup);
            // add spanned components
            for (ComponentInGroup component : prevGroup.getComponents()) {
              if (group.contains(component)) {
                gapGroup.add(component);
              }
            }
          }
        }
      }
      // add group
      newGroups.add(group);
    }
    // replace groups
    groups.clear();
    groups.addAll(newGroups);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location/size (in groups)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the first {@link ComponentGroup} that contains given component.
   */
  public static ComponentGroup getBeginForComponent(List<ComponentGroup> groups,
      ComponentInGroup component) {
    for (ComponentGroup group : groups) {
      if (group.contains(component)) {
        return group;
      }
    }
    // should not happen
    throw new IllegalStateException("Can not find group for component: " + component);
  }

  /**
   * @return the last {@link ComponentGroup} that contains given component.
   */
  public static ComponentGroup getEndForComponent(List<ComponentGroup> groups,
      ComponentInGroup component) {
    ComponentGroup lastGroup = null;
    for (ComponentGroup group : groups) {
      if (group.contains(component)) {
        lastGroup = group;
      }
    }
    Assert.isNotNull(lastGroup, "Can not find group for component: " + component);
    return lastGroup;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentInGroup
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Description for single {@link IAbstractComponentInfo} in some {@link ComponentGroup}.
   */
  public static final class ComponentInGroup {
    private final IAbstractComponentInfo m_component;
    private final int m_min;
    private final int m_max;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private ComponentInGroup(IAbstractComponentInfo component, Rectangle bounds, boolean horizontal) {
      m_component = component;
      int width = Math.max(1, bounds.width);
      int height = Math.max(1, bounds.height);
      if (horizontal) {
        m_min = bounds.x;
        m_max = bounds.x + width;
      } else {
        m_min = bounds.y;
        m_max = bounds.y + height;
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
      return m_component.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ComponentInGroup) {
        return m_component == ((ComponentInGroup) obj).m_component;
      }
      return false;
    }

    @Override
    public String toString() {
      return getComponentName() + "(" + m_min + "," + m_max + ")";
    }

    private String getComponentName() {
      try {
        return (String) ScriptUtils.evaluate("getVariableSupport().getComponentName()", m_component);
      } catch (Throwable e) {
        return m_component.toString();
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the underlying {@link IAbstractComponentInfo}.
     */
    public IAbstractComponentInfo getComponent() {
      return m_component;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Internal access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the minimal coordinate.
     */
    private int getMin() {
      return m_min;
    }

    /**
     * @return the maximal coordinate.
     */
    private int getMax() {
      return m_max;
    }

    /**
     * @return <code>true</code> if component interval contains given value.
     */
    private boolean contains(int value) {
      return m_min <= value && value < m_max;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentGroup
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Group of {@link ComponentInGroup}.
   *
   * @author scheglov_ke
   */
  public static class ComponentGroup {
    private final List<ComponentInGroup> m_components = Lists.newArrayList();
    private int m_min;
    private int m_max;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
      return m_components + "(" + m_min + "," + m_max + ")";
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the minimal coordinate.
     */
    public int getMin() {
      return m_min;
    }

    /**
     * @return the maximal coordinate.
     */
    public int getMax() {
      return m_max;
    }

    /**
     * @return the size of this {@link ComponentGroup}.
     */
    public int getSize() {
      return m_max - m_min;
    }

    /**
     * @return the {@link List} of {@link ComponentInGroup} that belongs to this group.
     */
    public List<ComponentInGroup> getComponents() {
      return m_components;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Internal access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Adds new {@link ComponentInGroup}.
     */
    private void add(ComponentInGroup component) {
      if (!contains(component)) {
        m_components.add(component);
      }
    }

    /**
     * @return the minimal coordinate for all components that begin in this group.
     */
    private int getMinOfBegins(List<ComponentGroup> groups) {
      int result = Integer.MAX_VALUE;
      for (ComponentInGroup component : m_components) {
        if (getBeginForComponent(groups, component) == this) {
          result = Math.min(result, component.getMin());
        }
      }
      return result;
    }

    /**
     * @return the maximal coordinate for all components that end in this group.
     */
    private int getMaxOfEnds(List<ComponentGroup> groups) {
      int result = Integer.MIN_VALUE;
      for (ComponentInGroup component : m_components) {
        if (getEndForComponent(groups, component) == this) {
          result = Math.max(result, component.getMax());
        }
      }
      return result;
    }

    /**
     * @return <code>true</code> if this group contains given component.
     */
    private boolean contains(ComponentInGroup component) {
      return m_components.indexOf(component) != -1;
    }
  }
}
