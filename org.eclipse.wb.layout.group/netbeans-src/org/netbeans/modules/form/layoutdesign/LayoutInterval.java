/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General Public License
 * Version 2 only ("GPL") or the Common Development and Distribution License("CDDL") (collectively,
 * the "License"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP.
 * See the License for the specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header Notice in each file and
 * include the License file at nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this particular file
 * as subject to the "Classpath" exception as provided by Sun in the GPL Version 2 section of the
 * License file that accompanied this code. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software is Sun
 * Microsystems, Inc. Portions Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the GPL Version 2,
 * indicate your decision by adding "[Contributor] elects to include this software in this
 * distribution under the [CDDL or GPL Version 2] license." If you do not indicate a single choice
 * of license, a recipient has the option to distribute your version of this file under either the
 * CDDL, the GPL Version 2 or to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
 * option applies only if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.layoutdesign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tomas Pavek
 */
public final class LayoutInterval implements LayoutConstants {
  static final int ATTRIBUTE_FILL = 1;
  static final int ATTRIBUTE_FORMER_FILL = 2;
  static final int ATTR_CLOSED_GROUP = 32;
  // attributes denoting intervals with different size behavior in design time
  static final int ATTR_DESIGN_CONTAINER_GAP = 4;
  static final int ATTR_DESIGN_RESIZING = 8;
  static final int ATTR_DESIGN_SUPPRESSED_RESIZING = 16;
  // attributes used during aligning
  static final int ATTR_ALIGN_PRE = 64;
  static final int ATTR_ALIGN_POST = 128;
  static final int ATTR_FORCED_DEFAULT = 256;
  static final int DESIGN_ATTRS = ATTR_DESIGN_CONTAINER_GAP
      | ATTR_DESIGN_RESIZING
      | ATTR_DESIGN_SUPPRESSED_RESIZING
      | ATTR_ALIGN_PRE
      | ATTR_ALIGN_POST;
  static final int ATTR_PERSISTENT_MASK = ATTRIBUTE_FILL
      | ATTRIBUTE_FORMER_FILL
      | ATTR_CLOSED_GROUP;
  // type of the interval - SINGLE, SEQUENTIAL, PARALLEL
  private final int type;
  // additional attributes set on the interval as bit flags
  private int attributes;
  // alignment of the interval (if in a parallel group)
  private int alignment = DEFAULT;
  // parent interval (group )
  private LayoutInterval parentInterval;
  // internall alignment of a group (if this is a parallel group)
  private int groupAlignment = LEADING;
  // contained sub-intervals (if this is a group)
  private List<LayoutInterval> subIntervals;
  // associated LayoutComponent (if any)
  private LayoutComponent layoutComponent;
  // type of padding (default gap; if this is a preferred gap)
  private PaddingType paddingType;
  private String[] paddingDefComps; // 2 components, needed for INDENT gap
  // minimum, preferred, and maximum size definitions
  private int minSize;
  private int prefSize;
  private int maxSize;
  // current position and size of the interval in the visual representation
  private LayoutRegion currentSpace;

  // -----
  // setup methods - each setter should be called max. once after creation,
  // other changes should be done via LayoutModel to be fired and recorded
  // for undo/redo
  public LayoutInterval(int type) {
    this.type = type;
    minSize = NOT_EXPLICITLY_DEFINED;
    prefSize = NOT_EXPLICITLY_DEFINED;
    if (type == SEQUENTIAL || type == PARALLEL) {
      subIntervals = new ArrayList<LayoutInterval>();
      maxSize = NOT_EXPLICITLY_DEFINED; // group can resize by default
    } else {
      assert type == SINGLE;
      maxSize = USE_PREFERRED_SIZE;
    }
  }

  public void setAlignment(int alignment) {
    this.alignment = alignment;
  }

  public void setGroupAlignment(int alignment) {
    assert alignment != DEFAULT && type == PARALLEL;
    groupAlignment = alignment;
  }

  void setComponent(LayoutComponent comp) {
    layoutComponent = comp;
  }

  void setMinimumSize(int size) {
    // for groups we expect only two states - shrinking suppressed/allowed
    assert isSingle() || size == USE_PREFERRED_SIZE || size == NOT_EXPLICITLY_DEFINED;
    minSize = size;
  }

  public void setPreferredSize(int size) {
    assert size != USE_PREFERRED_SIZE && isSingle() || size == NOT_EXPLICITLY_DEFINED; // groups should not have explicit size
    prefSize = size;
  }

  public void setMaximumSize(int size) {
    // for single interval the max size must be defined
    // for groups only two states - growing suppressed or allowed
    assert isSingle()
        && size != NOT_EXPLICITLY_DEFINED
        || isGroup()
        && (size == USE_PREFERRED_SIZE || size == NOT_EXPLICITLY_DEFINED);
    maxSize = size;
  }

  void setSize(int size) {
    setMinimumSize(size);
    setPreferredSize(size);
    setMaximumSize(size);
  }

  public void setSizes(int min, int pref, int max) {
    setMinimumSize(min);
    setPreferredSize(pref);
    setMaximumSize(max);
  }

  int getMinimumSize() {
    return minSize;
  }

  int getPreferredSize() {
    return prefSize;
  }

  int getMaximumSize() {
    return maxSize;
  }

  public void setPaddingType(PaddingType type) {
    paddingType = type;
  }

  String[] getPaddingDefComponents() {
    return paddingDefComps;
  }

  void setPaddingDefComponents(String compId1, String compId2) {
    if (compId1 == null) {
      paddingDefComps = null;
    } else {
      paddingDefComps = new String[]{compId1, compId2};
    }
  }

  // ---------
  // public methods
  /**
   * Returns the type of the structure of the interval. It can be a single interval or a group with
   * sub-intervals arranged either sequentially or parallelly.
   * 
   * @return type of the interval: SINGLE, SEQUENTIAL, or PARALLEL
   */
  public int getType() {
    return type;
  }

  /**
   * Returns alignment of the interval within a parallel group. If the interval is not part of a
   * parallel group, the alignment is meaningless.
   * 
   * @return alignment of the interval within a parallel group (LEADING, TRAILING, CENTER, or
   *         BASELINE); DEFAULT if in a sequential group
   */
  public int getAlignment() {
    return alignment == DEFAULT && parentInterval != null && parentInterval.isParallel()
        ? parentInterval.getGroupAlignment()
        : alignment;
  }

  /**
   * Returns the common alignment of sub-intervals within a group (makes sense only for a parallel
   * group).
   * 
   * @return alignment of the group (LEADING, TRAILING, CENTER, or BASELINE)
   */
  public int getGroupAlignment() {
    return groupAlignment;
  }

  /**
   * Returns the minimum size of the interval. Instead of a specific size it may return also one of
   * the constants NOT_EXPLICITLY_DEFINED or USE_PREFERRED_SIZE.
   * 
   * @param designTime
   *          if true, size for design time layout is returned (design time behavior may be
   *          different in terms of resizing)
   * @return minimum interval size, or one of the constants: NOT_EXPLICITLY_DEFINED or
   *         USE_PREFERRED_SIZE
   */
  public int getMinimumSize(boolean designTime) {
    if (!designTime) {
      return minSize;
    }
    if (hasAttribute(ATTR_DESIGN_SUPPRESSED_RESIZING)) {
      assert !hasAttribute(ATTR_DESIGN_RESIZING);
      return USE_PREFERRED_SIZE;
    }
    if (hasAttribute(ATTR_DESIGN_RESIZING)) {
      return isEmptySpace() && getPreferredSize(designTime) != 0 ? NOT_EXPLICITLY_DEFINED : 0;
    }
    return minSize;
  }

  /**
   * Returns the preferred size of the interval. If no specific size was set, it returns
   * NOT_EXPLICITLY_DEFINED constant.
   * 
   * @param designTime
   *          if true, size for design time layout is returned (design time behavior may be
   *          different in terms of resizing)
   * @return preferred size of the interval, or NOT_EXPLICITLY_DEFINED constant
   */
  public int getPreferredSize(boolean designTime) {
    return prefSize;
  }

  /**
   * Returns the maximum size of the interval. Instead of a specific size it may return also one of
   * the constants NOT_EXPLICITLY_DEFINED or USE_PREFERRED_SIZE.
   * 
   * @param designTime
   *          if true, size for design time layout is returned (design time behavior may be
   *          different in terms of resizing)
   * @return maximum interval size, or one of the constants: NOT_EXPLICITLY_DEFINED or
   *         USE_PREFERRED_SIZE
   */
  public int getMaximumSize(boolean designTime) {
    if (!designTime) {
      return maxSize;
    }
    if (hasAttribute(ATTR_DESIGN_SUPPRESSED_RESIZING)) {
      assert !hasAttribute(ATTR_DESIGN_RESIZING);
      return USE_PREFERRED_SIZE;
    }
    if (hasAttribute(ATTR_DESIGN_RESIZING)) {
      return Short.MAX_VALUE;
    }
    return maxSize;
  }

  /**
   * Returns number of sub-intervals of this interval.
   * 
   * @return number of sub-intervals of this interval, 0 if it is not a group
   */
  public int getSubIntervalCount() {
    return subIntervals != null ? subIntervals.size() : 0;
  }

  /**
   * Returns an iterator of sub-intervals.
   * 
   * @return iterator of sub-intervals, empty if there are no sub-intervals
   */
  public Iterator<LayoutInterval> getSubIntervals() {
    return subIntervals != null ? subIntervals.iterator() : Collections.EMPTY_LIST.iterator();
  }

  /**
   * If this interval represents a component's width or height, this methods returns the component.
   * 
   * @return LayoutComponent instance representing the associated component. Null if this interval
   *         does not represent a component.
   */
  public LayoutComponent getComponent() {
    return layoutComponent;
  }

  // helper methods (redundant - based on derived information)
  public boolean isParallel() {
    return type == PARALLEL;
  }

  public boolean isSequential() {
    return type == SEQUENTIAL;
  }

  /**
   * Returns whether this interval defines a lyout component.
   * 
   * @return true if this interval represents a layout component, false otherwise
   */
  public boolean isComponent() {
    return layoutComponent != null;
  }

  /**
   * Returns whether this interval defines an "empty" space (gap) in the layout, not including nor
   * being able to include any component.
   * 
   * @return true if this is a single interval not representing a component, false otherwise
   */
  public boolean isEmptySpace() {
    return type == SINGLE && layoutComponent == null;
  }

  public boolean isDefaultPadding(boolean designTime) {
    return isEmptySpace()
        && (getMinimumSize(designTime) == NOT_EXPLICITLY_DEFINED || getPreferredSize(designTime) == NOT_EXPLICITLY_DEFINED);
  }

  public PaddingType getPaddingType() {
    return paddingType;
  }

  public boolean isSingle() {
    return type == SINGLE;
  }

  /**
   * Returns whether this interval represents a group structure that can have have sub-intervals.
   * 
   * @return whether this interval is a group, either sequential or parallel
   */
  public boolean isGroup() {
    return type == SEQUENTIAL || type == PARALLEL;
  }

  /**
   * @return whether the interval is allowed to grow (according to its definition); if allowed, the
   *         real growing possibility may still depend on the associated component
   */
  //    public boolean isAllowedToGrow() {
  //        return maxSize != USE_PREFERRED_SIZE
  //               && (prefSize == NOT_EXPLICITLY_DEFINED
  //                   || maxSize == NOT_EXPLICITLY_DEFINED
  //                   || maxSize > prefSize);
  //    }
  /**
   * @return whether the interval is allowed to shrink (according to its definition); if allowed,
   *         the real growing possibility may still depend on the associated component
   */
  //    public boolean isAllowedToShrink() {
  //        return minSize != USE_PREFERRED_SIZE
  //               && (prefSize == NOT_EXPLICITLY_DEFINED
  //                   || minSize == NOT_EXPLICITLY_DEFINED
  //                   || minSize < prefSize);
  //    }
  // end of public methods
  // -----
  boolean hasAttribute(int attr) {
    return (attributes & attr) == attr;
  }

  void setAttribute(int attr) {
    attributes |= attr;
  }

  void unsetAttribute(int attr) {
    attributes &= ~attr;
  }

  /**
   * Sets attributes of the layout interval. Should be used by persistence manager only!
   * 
   * @param attrs
   *          attributes.
   */
  void setAttributes(int attrs) {
    attributes = attrs;
  }

  /**
   * Returns attributes of this layout interval. You should use <code>hasAttribute()</code> when you
   * are interested in one particular attribute.
   */
  int getAttributes() {
    return attributes;
  }

  /**
   * @return the value of the alignment field of the interval - unlike getAlignment() it does not
   *         ask the parent if not set (DEFAULT)
   */
  int getRawAlignment() {
    return alignment;
  }

  // -----
  public LayoutInterval getParent() {
    return parentInterval;
  }

  public int add(LayoutInterval interval, int index) {
    if (getParent() == interval) {
      throw new IllegalArgumentException("Cannot add parent as a sub-interval!"); // NOI18N
    }
    if (interval.isComponent()) { // Issue 118562
      LayoutComponent comp = interval.getComponent();
      for (LayoutInterval subInterval : subIntervals) {
        if (subInterval.isComponent() && comp.equals(subInterval.getComponent())) {
          if (System.getProperty("netbeans.ignore.issue118562") == null) { // NOI18N
            throw new IllegalArgumentException("Cannot add a component into a group twice!"); // NOI18N
          } else {
            return -1;
          }
        }
      }
    }
    if (index < 0) {
      index = subIntervals.size();
    }
    subIntervals.add(index, interval);
    interval.parentInterval = this;
    return index;
  }

  int remove(LayoutInterval interval) {
    int index = subIntervals.indexOf(interval);
    if (index >= 0) {
      subIntervals.remove(index);
      interval.parentInterval = null;
    }
    return index;
  }

  LayoutInterval remove(int index) {
    LayoutInterval interval = subIntervals.get(index);
    subIntervals.remove(index);
    interval.parentInterval = null;
    return interval;
  }

  LayoutInterval getSubInterval(int index) {
    return subIntervals != null ? subIntervals.get(index) : null;
  }

  int indexOf(LayoutInterval interval) {
    return subIntervals != null ? subIntervals.indexOf(interval) : -1;
  }

  public boolean isParentOf(LayoutInterval interval) {
    if (isGroup()) {
      do {
        interval = interval.getParent();
        if (interval == this) {
          return true;
        }
      } while (interval != null);
    }
    return false;
  }

  public LayoutInterval getRoot() {
    return LayoutInterval.getRoot(this);
  }

  // -----
  // current state of the layout - current position and size of layout
  // interval kept to be available quickly for the layout designer
  LayoutRegion getCurrentSpace() {
    assert !isEmptySpace(); // [temporary - nobody should be interested in gap positions]
    if (currentSpace == null) {
      currentSpace = new LayoutRegion();
    }
    return currentSpace;
  }

  void setCurrentSpace(LayoutRegion space) {
    currentSpace = space;
  }

  // -----
  // static helper methods
  /**
   * @return the closest parent interval that matches given type
   */
  static LayoutInterval getFirstParent(LayoutInterval interval, int type) {
    LayoutInterval parent = interval.getParent();
    while (parent != null && parent.getType() != type) {
      parent = parent.getParent();
    }
    return parent;
  }

  static LayoutInterval getRoot(LayoutInterval interval) {
    while (interval.getParent() != null) {
      interval = interval.getParent();
    }
    //        assert interval.isParallel();
    return interval;
  }

  /**
   * Finds common parent of the given intervals.
   * 
   * @param intervals
   *          intervals whose parent should be found.
   * @return common parent of the given intervals.
   */
  static LayoutInterval getCommonParent(LayoutInterval[] intervals) {
    assert intervals != null && intervals.length > 0;
    LayoutInterval parent = intervals[0].getParent();
    for (int i = 1; i < intervals.length; i++) {
      parent = getCommonParent(parent, intervals[i]);
    }
    return parent;
  }

  /**
   * Finds common parent of two given intervals. In case one interval is parent of the other then
   * this interval is returned directly, not its parent.
   * 
   * @param interval1
   *          interval whose parent should be found.
   * @param interval2
   *          interval whose parent should be found.
   * @return common parent of two given intervals.
   */
  static LayoutInterval getCommonParent(LayoutInterval interval1, LayoutInterval interval2) {
    // Find all parents of given intervals
    Iterator parents1 = parentsOfInterval(interval1).iterator();
    Iterator parents2 = parentsOfInterval(interval2).iterator();
    LayoutInterval parent1 = (LayoutInterval) parents1.next();
    LayoutInterval parent2 = (LayoutInterval) parents2.next();
    assert parent1 == parent2;
    // Candidate for the common parent
    LayoutInterval parent = null;
    while (parent1 == parent2) {
      parent = parent1;
      if (parents1.hasNext()) {
        parent1 = (LayoutInterval) parents1.next();
      } else {
        break;
      }
      if (parents2.hasNext()) {
        parent2 = (LayoutInterval) parents2.next();
      } else {
        break;
      }
    }
    return parent;
  }

  /**
   * Calculates all parents of the given interval.
   * 
   * @param interval
   *          interval whose parents should be found.
   * @return <code>List</code> of <code>LayoutInterval</code> objects that are parents of the given
   *         interval. The root is the first in the list; the interval itelf is also included - at
   *         the end.
   */
  private static List<LayoutInterval> parentsOfInterval(LayoutInterval interval) {
    List<LayoutInterval> parents = new LinkedList<LayoutInterval>();
    while (interval != null) {
      parents.add(0, interval);
      interval = interval.getParent();
    }
    return parents;
  }

  static int getCount(LayoutInterval group, int alignment, boolean nonEmpty) {
    int n = 0;
    Iterator it = group.getSubIntervals();
    while (it.hasNext()) {
      LayoutInterval li = (LayoutInterval) it.next();
      if ((group.isSequential()
          || alignment == LayoutRegion.ALL_POINTS
          || li.getAlignment() == alignment || wantResize(li))
          && (!nonEmpty || !li.isEmptySpace())) { // count in
        n++;
      }
    }
    return n;
  }

  static LayoutInterval getDirectNeighbor(LayoutInterval interval, int alignment, boolean nonEmpty) {
    LayoutInterval parent = interval.getParent();
    if (parent == null || parent.isParallel()) {
      return null;
    }
    LayoutInterval neighbor = null;
    int d = alignment == LEADING ? -1 : 1;
    int n = parent.getSubIntervalCount();
    int index = parent.indexOf(interval) + d;
    while (index >= 0 && index < n && neighbor == null) {
      LayoutInterval li = parent.getSubInterval(index);
      index += d;
      if (!nonEmpty || !li.isEmptySpace()) {
        neighbor = li;
      }
    }
    return neighbor;
  }

  /**
   * @param alignment
   *          direction in which the neighbor is looked for (LEADING or TRAILING)
   * @param nonEmpty
   *          true if empty spaces (gaps) should be skipped
   * @param outOfParent
   *          true if can go up (out of the first sequential parent) for an indirect neighbor
   * @param aligned
   *          true if the indirect neighbor must be in contact with the given interval
   */
  static LayoutInterval getNeighbor(LayoutInterval interval,
      int alignment,
      boolean nonEmpty,
      boolean outOfParent,
      boolean aligned) {
    assert alignment == LEADING || alignment == TRAILING;
    LayoutInterval neighbor = null;
    LayoutInterval parent = interval;
    int d = alignment == LEADING ? -1 : 1;
    do {
      do { // find sequential parent first
        interval = parent;
        parent = interval.getParent();
        if (aligned
            && parent != null
            && parent.isParallel()
            && !isAlignedAtBorder(interval, alignment)) { // interval not aligned in parent
          parent = null;
        }
      } while (parent != null && parent.isParallel());
      if (parent != null) { // look for the neighbor in the sequence
        neighbor = getDirectNeighbor(interval, alignment, nonEmpty);
      }
    } while (neighbor == null && parent != null && outOfParent);
    return neighbor;
  }

  static LayoutInterval getNeighbor(LayoutInterval interval, int parentType, int alignment) {
    assert alignment == LEADING || alignment == TRAILING;
    LayoutInterval sibling = null;
    LayoutInterval parent = interval;
    do {
      do {
        interval = parent;
        parent = parent.getParent();
      } while (parent != null && parent.getType() != parentType);
      if (parent != null) {
        List subs = parent.subIntervals;
        int index = subs.indexOf(interval);
        if (alignment == LEADING && index > 0) {
          sibling = (LayoutInterval) subs.get(index - 1);
        } else if (alignment == TRAILING && index + 1 < subs.size()) {
          sibling = (LayoutInterval) subs.get(index + 1);
        }
      }
    } while (parent != null && sibling == null);
    return sibling;
  }

  static boolean startsWithEmptySpace(LayoutInterval interval, int alignment) {
    assert alignment == LEADING || alignment == TRAILING;
    if (interval.isSingle()) {
      return interval.isEmptySpace();
    }
    if (interval.isSequential()) {
      int index = alignment == LEADING ? 0 : interval.getSubIntervalCount() - 1;
      return startsWithEmptySpace(interval.getSubInterval(index), alignment);
    } else { // parallel group
      for (Iterator it = interval.getSubIntervals(); it.hasNext();) {
        LayoutInterval li = (LayoutInterval) it.next();
        if (startsWithEmptySpace(li, alignment)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks whether an interval is permanently aligned to its parent at given border. (The asked
   * relation is hard, always maintained by the layout.) For a sequential parent the interval is
   * aligned if it is first or last. For parallel parent the interval must have the given alignment
   * in the group, or be resizing.
   */
  static boolean isAlignedAtBorder(LayoutInterval interval, int alignment) {
    if (alignment != LEADING && alignment != TRAILING) {
      return false;
    }
    LayoutInterval parent = interval.getParent();
    if (parent == null) {
      return false;
    }
    if (parent.isSequential()) {
      int index = alignment == LEADING ? 0 : parent.getSubIntervalCount() - 1;
      return interval == parent.getSubInterval(index);
    } else { // parallel parent
      return interval.getAlignment() == alignment || wantResize(interval);
    }
  }

  /**
   * Checks whether an interval is permanently aligned with a given parent interval - need not be
   * the direct parent. This is a multi-level version of the other (simple) isAlignedAtBorder
   * method.
   */
  static boolean isAlignedAtBorder(LayoutInterval interval, LayoutInterval parent, int alignment) {
    do {
      if (!isAlignedAtBorder(interval, alignment)) {
        return false;
      }
      interval = interval.getParent();
    } while (interval != parent);
    return true;
  }

  /**
   * Checks whether given interval is placed at border side of its parent. Cares about the current
   * visual situation only - the place may change if the alignment is not backed by the layout
   * structure. Note this method requires the current visual state (positions) of the relevant
   * intervals to be up-to-date.
   */
  static boolean isPlacedAtBorder(LayoutInterval interval, int dimension, int alignment) {
    if (alignment != LEADING && alignment != TRAILING) {
      return false;
    }
    LayoutInterval parent = interval.getParent();
    if (parent == null) {
      return false;
    }
    if (interval.isEmptySpace()) {
      if (parent.isSequential()) {
        int index = alignment == LEADING ? 0 : parent.getSubIntervalCount() - 1;
        return interval == parent.getSubInterval(index);
      } else { // gap in parallel parent
        return true;
      }
    } else { // check visual position
      return LayoutRegion.distance(
          interval.getCurrentSpace(),
          parent.getCurrentSpace(),
          dimension,
          alignment,
          alignment) == 0;
    }
  }

  /**
   * Checks whether an interval is placed at border side of given parent (need not be the direct
   * parent). This is a multi-level version of the simpler isPlacededAtBorder method. Note this
   * method requires the current visual state (positions) of the relevant intervals to be
   * up-to-date.
   */
  static boolean isPlacedAtBorder(LayoutInterval interval,
      LayoutInterval parent,
      int dimension,
      int alignment) {
    if (alignment != LEADING && alignment != TRAILING) {
      return false;
    }
    if (interval.isEmptySpace()) {
      LayoutInterval p = interval.getParent();
      if (p.isSequential()) {
        int index = alignment == LEADING ? 0 : p.getSubIntervalCount() - 1;
        if (interval != p.getSubInterval(index)) {
          return false;
        }
      }
      if (p == parent) {
        return true;
      }
      interval = p;
    }
    return LayoutRegion.distance(
        interval.getCurrentSpace(),
        parent.getCurrentSpace(),
        dimension,
        alignment,
        alignment) == 0 && parent.isParentOf(interval);
  }

  // [to be replaced by separate methods like isAlignedAtBorder, isPlacedBorder, isLastInterval]
  static boolean isBorderInterval(LayoutInterval interval, int alignment, boolean attached) {
    LayoutInterval parent = interval.getParent();
    if (parent != null && (alignment == LEADING || alignment == TRAILING)) {
      if (parent.isSequential()) {
        int index = alignment == LEADING ? 0 : parent.getSubIntervalCount() - 1;
        while (index >= 0 && index < parent.getSubIntervalCount()) {
          LayoutInterval li = parent.getSubInterval(index);
          if (li == interval) {
            return true;
          } else if (attached || !li.isEmptySpace()) {
            return false;
          }
          index += alignment == LEADING ? 1 : -1;
        }
      } else {
        return !attached || interval.getAlignment() == alignment || wantResize(interval);
      }
      //                if (interval.getAlignment() == alignment) {
      //                return interval.getCurrentSpace().positions[dimension][alignment]
      //                       == parent.getCurrentSpace().positions[dimension][alignment];
    }
    return false;
  }

  static boolean isClosedGroup(LayoutInterval group, int alignment) {
    assert group.isParallel();
    if (group.hasAttribute(ATTR_CLOSED_GROUP)
        || group.getGroupAlignment() == CENTER
        || group.getGroupAlignment() == BASELINE) {
      return true;
    }
    Iterator it = group.getSubIntervals();
    while (it.hasNext()) {
      LayoutInterval li = (LayoutInterval) it.next();
      if (li.getAlignment() == alignment || wantResize(li)) {
        return true;
      }
    }
    return false;
  }

  static boolean isExplicitlyClosedGroup(LayoutInterval group) {
    return group.hasAttribute(ATTR_CLOSED_GROUP);
  }

  static boolean isDefaultPadding(LayoutInterval interval) {
    return interval.isEmptySpace()
        && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED || interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED);
  }

  static boolean isFixedDefaultPadding(LayoutInterval interval) {
    return interval.isEmptySpace()
        && (interval.getMinimumSize() == NOT_EXPLICITLY_DEFINED || interval.getMinimumSize() == USE_PREFERRED_SIZE)
        && interval.getPreferredSize() == NOT_EXPLICITLY_DEFINED
        && (interval.getMaximumSize() == NOT_EXPLICITLY_DEFINED || interval.getMaximumSize() == USE_PREFERRED_SIZE);
  }

  /**
   * @return whether given interval is allowed to resize (not defined as fixed)
   */
  static boolean canResize(LayoutInterval interval) {
    // [don't care about shrinking, assuming min possibly not defined - is it ok?]
    int max = interval.getMaximumSize();
    int pref = interval.getPreferredSize();
    assert interval.isGroup() || max != NOT_EXPLICITLY_DEFINED;
    return max != pref && max != USE_PREFERRED_SIZE || max == NOT_EXPLICITLY_DEFINED;
  }

  /**
   * Finds out whether given interval would resize if allowed (given more space by its parent).
   * 
   * @return whether given interval would resize if given opportunity
   */
  static boolean wantResize(LayoutInterval interval) {
    return canResize(interval) && (!interval.isGroup() || contentWantResize(interval));
  }

  /**
   * Finds out whether given interval would resize if allowed (given more space by its parent). This
   * method also considers resizing of the whole layout (some parent of the interval could block the
   * resizing).
   * 
   * @return whether given interval would resize if given opportunity
   */
  static boolean wantResizeInLayout(LayoutInterval interval) {
    if (!wantResize(interval)) {
      return false;
    }
    while (interval.getParent() != null) {
      interval = interval.getParent();
      if (!canResize(interval)) {
        return false;
      }
    }
    return true;
  }

  static boolean contentWantResize(LayoutInterval group) {
    boolean subres = false;
    Iterator it = group.getSubIntervals();
    while (it.hasNext()) {
      if (wantResize((LayoutInterval) it.next())) {
        subres = true;
        break;
      }
    }
    return subres;
  }

  static int getIntervalCurrentSize(LayoutInterval interval, int dimension) {
    if (!interval.isEmptySpace()) {
      return interval.getCurrentSpace().size(dimension);
    }
    int posL;
    int posT;
    LayoutInterval parent = interval.getParent();
    if (parent.isSequential()) {
      int index = parent.indexOf(interval);
      posL =
          index > 0
              ? parent.getSubInterval(index - 1).getCurrentSpace().positions[dimension][TRAILING]
              : parent.getCurrentSpace().positions[dimension][LEADING];
      posT =
          index + 1 < parent.getSubIntervalCount()
              ? parent.getSubInterval(index + 1).getCurrentSpace().positions[dimension][LEADING]
              : parent.getCurrentSpace().positions[dimension][TRAILING];
    } else {
      posL = parent.getCurrentSpace().positions[dimension][LEADING];
      posT = parent.getCurrentSpace().positions[dimension][TRAILING];
    }
    return posT - posL;
  }

  /**
   * Computes effective alignment of an interval in its parent. In case of a sequential parent, the
   * effective interval alignment depends on other intervals and their resizability. E.g. if a
   * preceding interval is resizing then the interval is effectivelly "pushed" to the trailing end.
   * If there are no other intervals resizing then the parent alignment is returned. If there are
   * resizing intervals on both sides, or the interval itself is resizing, then the there is no
   * (positive) effective alignment.
   * 
   * @return LEADING, TRAILING, or DEFAULT
   */
  static int getEffectiveAlignment(LayoutInterval interval) {
    LayoutInterval parent = interval.getParent();
    if (parent.isParallel()) {
      return interval.getAlignment();
    }
    if (LayoutInterval.wantResize(interval)) {
      return DEFAULT;
    }
    boolean before = true;
    boolean leadingFixed = true;
    boolean trailingFixed = true;
    Iterator it = parent.getSubIntervals();
    do {
      LayoutInterval li = (LayoutInterval) it.next();
      if (li == interval) {
        before = false;
      } else if (LayoutInterval.wantResize(li)) {
        if (before) {
          leadingFixed = false;
        } else {
          trailingFixed = false;
        }
      }
    } while (it.hasNext());
    if (leadingFixed && !trailingFixed) {
      return LEADING;
    }
    if (!leadingFixed && trailingFixed) {
      return TRAILING;
    }
    if (leadingFixed && trailingFixed) {
      return parent.getAlignment();
    }
    return DEFAULT; // !leadingFixed && !trailingFixed
  }

  /**
   * Computes effective alignment of given interval's edge in its direct parent. In case of a
   * sequential parent, the effective interval alignment depends on other intervals and their
   * resizability.
   * 
   * @return effective alignment within parent, or DEFAULT in case of ambiguous alignment in
   *         sequential parent
   */
  static int getEffectiveAlignment(LayoutInterval interval, int edge) {
    assert edge == LEADING || edge == TRAILING;
    boolean wantResize = LayoutInterval.wantResize(interval);
    LayoutInterval parent = interval.getParent();
    if (parent.isParallel()) {
      return wantResize ? edge : interval.getAlignment();
    }
    int n = parent.getSubIntervalCount();
    int i = edge == LEADING ? 0 : n - 1;
    int d = edge == LEADING ? 1 : -1;
    boolean before = true;
    boolean beforeFixed = true;
    boolean afterFixed = true;
    while (i >= 0 && i < n) {
      LayoutInterval li = parent.getSubInterval(i);
      if (li == interval) {
        before = false;
      } else if (LayoutInterval.wantResize(li)) {
        if (before) {
          beforeFixed = false;
        } else {
          afterFixed = false;
        }
      }
      i += d;
    }
    if (beforeFixed && !afterFixed) {
      return edge;
    }
    if (!beforeFixed && afterFixed) {
      return edge ^ 1;
    }
    if (beforeFixed && afterFixed) {
      if (wantResize) {
        return edge;
      } else {
        int parentAlignment = parent.getAlignment();
        if (parentAlignment == LEADING || parentAlignment == TRAILING) {
          return parentAlignment;
        }
      }
    }
    return DEFAULT;
  }

  /**
   * Computes effective alignment of an interval's edge relatively to given parent.
   * 
   * @return effective alignment within parent, or DEFAULT in case of ambiguous alignment in
   *         sequential parent
   */
  static int getEffectiveAlignmentInParent(LayoutInterval interval, LayoutInterval parent, int edge) {
    assert parent.isParentOf(interval);
    int alignment = edge;
    do {
      alignment = getEffectiveAlignment(interval, alignment);
      interval = interval.getParent();
      if (alignment != LEADING && alignment != TRAILING) {
        while (interval != parent) {
          if (getEffectiveAlignment(interval) != alignment) {
            return DEFAULT;
          }
          interval = interval.getParent();
        }
      }
    } while (interval != parent);
    return alignment;
  }

  /**
   * Creates clone of the given interval. Doesn't clone content of groups, nor it sets
   * LayoutComponent. Just the type, alignments and sizes are copied.
   * 
   * @param interval
   *          interval to be cloned.
   * @param clone
   *          interval that should contain cloned data. Can be <code>null</code>.
   * @return shallow clone of the interval.
   */
  static LayoutInterval cloneInterval(LayoutInterval interval, LayoutInterval clone) {
    clone = clone == null ? new LayoutInterval(interval.getType()) : clone;
    clone.setAlignment(interval.getAlignment());
    clone.setAttributes(interval.getAttributes() & ATTR_PERSISTENT_MASK);
    if (interval.getType() == PARALLEL) {
      clone.setGroupAlignment(interval.getGroupAlignment());
    }
    clone.setSizes(
        interval.getMinimumSize(),
        interval.getPreferredSize(),
        interval.getMaximumSize());
    if (isDefaultPadding(interval)) {
      clone.setPaddingType(interval.getPaddingType());
    }
    return clone;
  }
}
