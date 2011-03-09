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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class serves as a library of various useful and well-defined operations on the layout model.
 * 
 * @author Tomas Pavek
 */
class LayoutOperations implements LayoutConstants {
  private final LayoutModel layoutModel;
  private final VisualMapper visualMapper;

  LayoutOperations(LayoutModel model, VisualMapper mapper) {
    layoutModel = model;
    visualMapper = mapper;
  }

  LayoutModel getModel() {
    return layoutModel;
  }

  VisualMapper getMapper() {
    return visualMapper;
  }

  // -----
  /**
   * Extracts surroundings of given interval (placed in a sequential group). Extracted intervals are
   * removed and go to the 'restLeading' and 'restTrailing' lists. Does not extract/remove the
   * interval itself.
   */
  int extract(LayoutInterval interval,
      int alignment,
      boolean closed,
      List<List> restLeading,
      List<List> restTrailing) {
    return extract(interval, interval, alignment, closed, restLeading, restTrailing);
  }

  int extract(LayoutInterval leading,
      LayoutInterval trailing,
      int alignment,
      boolean closed,
      List<List> restLeading,
      List<List> restTrailing) {
    LayoutInterval seq = leading.getParent();
    assert seq.isSequential();
    int leadingIndex = seq.indexOf(leading);
    int trailingIndex = seq.indexOf(trailing);
    int count = seq.getSubIntervalCount();
    int extractCount;
    if (closed) {
      extractCount = trailingIndex - leadingIndex + 1;
    } else if (alignment != LEADING && alignment != TRAILING) {
      extractCount = 1;
    } else {
      extractCount = alignment == LEADING ? count - leadingIndex : leadingIndex + 1;
    }
    if (extractCount < seq.getSubIntervalCount()) {
      List<Object/*Integer or LayoutInterval*/> toRemainL = null;
      List<Object/*Integer or LayoutInterval*/> toRemainT = null;
      int startIndex = alignment == LEADING ? leadingIndex : leadingIndex - extractCount + 1;
      int endIndex = alignment == LEADING ? trailingIndex + extractCount - 1 : trailingIndex;
      Iterator it = seq.getSubIntervals();
      for (int idx = 0; it.hasNext(); idx++) {
        LayoutInterval li = (LayoutInterval) it.next();
        if (idx < startIndex) {
          if (toRemainL == null) {
            toRemainL = new LinkedList<Object>();
            toRemainL.add(new Integer(LayoutInterval.getEffectiveAlignment(li)));
          }
          toRemainL.add(li);
        } else if (idx > endIndex) {
          if (toRemainT == null) {
            toRemainT = new LinkedList<Object>();
            toRemainT.add(new Integer(LayoutInterval.getEffectiveAlignment(li)));
          }
          toRemainT.add(li);
        }
      }
      if (toRemainL != null) {
        it = toRemainL.iterator();
        it.next();
        do {
          layoutModel.removeInterval((LayoutInterval) it.next());
        } while (it.hasNext());
        restLeading.add(toRemainL);
      }
      if (toRemainT != null) {
        it = toRemainT.iterator();
        it.next();
        do {
          layoutModel.removeInterval((LayoutInterval) it.next());
        } while (it.hasNext());
        restTrailing.add(toRemainT);
      }
    }
    return extractCount;
  }

  /**
   * Adds parallel content of a group specified in List to given sequence. Used to create a
   * remainder parallel group to a group of aligned intervals.
   * 
   * @param list
   *          the content of the group, output from 'extract' method
   * @param seq
   *          a sequential group where to add to
   * @param index
   *          the index in the sequence where to add
   * @param dimension
   * @param position
   *          the position of the remainder group relative to the main group (LEADING or TRAILING)
   *          // * @param mainAlignment effective alignment of the main group (LEADING or // *
   *          TRAILING or something else meaning not aligned)
   * @return parallel group if it has been created, or null
   */
  LayoutInterval addGroupContent(List<List> list,
      LayoutInterval seq,
      int index,
      int dimension,
      int position/*, int mainAlignment*/) {
    assert seq.isSequential() && (position == LEADING || position == TRAILING);
    boolean resizingFillGap = false;
    LayoutInterval commonGap = null;
    boolean onlyGaps = true;
    // Remove sequences just with one gap
    for (int i = list.size() - 1; i >= 0; i--) {
      List subList = list.get(i);
      assert subList.size() >= 2;
      if (subList.size() == 2) { // there is just one interval
        LayoutInterval li = (LayoutInterval) subList.get(1);
        if (li.isEmptySpace()) {
          if (commonGap == null || li.getPreferredSize() > commonGap.getPreferredSize()) {
            commonGap = li;
          }
          if (LayoutInterval.canResize(li)) {
            resizingFillGap = true;
          }
          list.remove(i);
        } else {
          onlyGaps = false;
        }
      } else {
        onlyGaps = false;
      }
    }
    if (onlyGaps) { // just one gap
      if (resizingFillGap && !LayoutInterval.canResize(commonGap)) {
        layoutModel.setIntervalSize(
            commonGap,
            NOT_EXPLICITLY_DEFINED,
            commonGap.getPreferredSize(),
            Short.MAX_VALUE);
      }
      insertGapIntoSequence(commonGap, seq, index, dimension);
      return null;
    }
    if (list.size() == 1) { // just one sequence
      List subList = list.get(0);
      for (int n = subList.size(), i = n - 1; i > 0; i--) { // skip alignment at 0
        LayoutInterval li = (LayoutInterval) subList.get(i);
        if (resizingFillGap
            && li.isEmptySpace()
            && !LayoutInterval.canResize(li)
            && (i == 1 && position == TRAILING || i == n - 1 && position == LEADING)) { // make the end gap resizing
          layoutModel.setIntervalSize(
              li,
              NOT_EXPLICITLY_DEFINED,
              li.getPreferredSize(),
              Short.MAX_VALUE);
        }
        if (i == 1 && li.isEmptySpace()) {
          insertGapIntoSequence(li, seq, index, dimension);
        } else {
          layoutModel.addInterval(li, seq, index);
        }
      }
      return null;
    }
    // create parallel group for multiple intervals/sequences
    LayoutInterval group = new LayoutInterval(PARALLEL);
    //        if (position == mainAlignment) {
    //            // [but this should eliminate resizability only for gaps...]
    //            group.setMinimumSize(USE_PREFERRED_SIZE);
    //            group.setMaximumSize(USE_PREFERRED_SIZE);
    //        }
    ////        group.setGroupAlignment(alignment);
    // fill the group
    for (Iterator it = list.iterator(); it.hasNext();) {
      List subList = (List) it.next();
      LayoutInterval interval;
      if (subList.size() == 2) { // there is just one interval - use it directly
        int alignment = ((Integer) subList.get(0)).intValue();
        interval = (LayoutInterval) subList.get(1);
        if (alignment == LEADING || alignment == TRAILING) {
          layoutModel.setIntervalAlignment(interval, alignment);
        }
      } else { // there are more intervals - create sequence
        interval = new LayoutInterval(SEQUENTIAL);
        int alignment = ((Integer) subList.get(0)).intValue();
        if (alignment == LEADING || alignment == TRAILING) {
          interval.setAlignment(alignment);
        }
        for (int i = 1, n = subList.size(); i < n; i++) {
          LayoutInterval li = (LayoutInterval) subList.get(i);
          if (resizingFillGap
              && li.isEmptySpace()
              && !LayoutInterval.canResize(li)
              && (i == 1 && position == TRAILING || i == n - 1 && position == LEADING)) { // make the end gap resizing
            layoutModel.setIntervalSize(
                li,
                NOT_EXPLICITLY_DEFINED,
                li.getPreferredSize(),
                Short.MAX_VALUE);
          }
          layoutModel.addInterval(li, interval, -1);
        }
      }
      layoutModel.addInterval(interval, group, -1);
    }
    layoutModel.addInterval(group, seq, index);
    return group;
  }

  /**
   * Adds 'interval' to 'target'. In case of 'interval' is a group, it is dismounted to individual
   * intervals if needed (e.g. if adding sequence to sequence), or if producing equal result with
   * less nesting (e.g. when adding parallel group to parallel group with same alignment). Also
   * redundant groups are canceled (containing just one interval).
   */
  boolean addContent(LayoutInterval interval, LayoutInterval target, int index) {
    if (interval.isGroup() && interval.getSubIntervalCount() == 1) {
      return addContent(layoutModel.removeInterval(interval, 0), target, index);
    }
    if (interval.isSequential() && target.isSequential()) {
      if (index < 0) {
        index = target.getSubIntervalCount();
      }
      while (interval.getSubIntervalCount() > 0) {
        LayoutInterval li = layoutModel.removeInterval(interval, 0);
        layoutModel.addInterval(li, target, index++);
      }
      return true;
    } else if (interval.isParallel() && target.isParallel()) {
      int align = interval.getAlignment();
      if (align == DEFAULT) {
        align = target.getGroupAlignment();
      }
      boolean sameAlign = true;
      Iterator it = interval.getSubIntervals();
      while (it.hasNext()) {
        LayoutInterval li = (LayoutInterval) it.next();
        if (LayoutInterval.wantResize(li)) { // will span over whole target group
          sameAlign = true;
          break;
        }
        if (li.getAlignment() != align) {
          sameAlign = false;
        }
      }
      if (sameAlign
          && (LayoutInterval.canResize(interval) || !LayoutInterval.canResize(target) || !LayoutInterval.wantResize(target))) { // can dismantle the group
        assert interval.getParent() == null;
        while (interval.getSubIntervalCount() > 0) {
          LayoutInterval li = interval.getSubInterval(0);
          if (li.getRawAlignment() == DEFAULT
              && interval.getGroupAlignment() != target.getGroupAlignment()) { // force alignment explicitly
            layoutModel.setIntervalAlignment(li, li.getAlignment());
          }
          layoutModel.removeInterval(li);
          layoutModel.addInterval(li, target, index);
          if (index >= 0) {
            index++;
          }
        }
        if (!LayoutInterval.canResize(interval) && LayoutInterval.canResize(target)) {
          suppressGroupResizing(target);
        }
        return true;
      } else { // need to add the group as a whole
        layoutModel.addInterval(interval, target, index);
      }
    } else {
      if (target.isSequential() && interval.getRawAlignment() != DEFAULT) {
        layoutModel.setIntervalAlignment(interval, DEFAULT);
      }
      layoutModel.addInterval(interval, target, index);
    }
    return false;
  }

  void resizeInterval(LayoutInterval interval, int size) {
    assert size >= 0 || size == NOT_EXPLICITLY_DEFINED;
    int min =
        interval.getMinimumSize() == interval.getPreferredSize()
            && interval.getMaximumSize() < Short.MAX_VALUE ? size : interval.getMinimumSize();
    int max =
        interval.getMaximumSize() == interval.getPreferredSize() ? (size == NOT_EXPLICITLY_DEFINED
            ? USE_PREFERRED_SIZE
            : size) : interval.getMaximumSize();
    layoutModel.setIntervalSize(interval, min, size, max);
  }

  void suppressGroupResizing(LayoutInterval group) {
    // don't for root group
    if (group.getParent() != null) {
      layoutModel.setIntervalSize(
          group,
          group.getMinimumSize(),
          group.getPreferredSize(),
          USE_PREFERRED_SIZE);
    }
  }

  void enableGroupResizing(LayoutInterval group) {
    layoutModel.setIntervalSize(
        group,
        group.getMinimumSize(),
        group.getPreferredSize(),
        NOT_EXPLICITLY_DEFINED);
  }

  void mergeParallelGroups(LayoutInterval group) {
    assert group.isParallel();
    if (!group.isParallel()) {
      return;
    }
    for (int i = group.getSubIntervalCount() - 1; i >= 0; i--) {
      LayoutInterval sub = group.getSubInterval(i);
      if (sub.isParallel()) {
        mergeParallelGroups(sub);
        dissolveRedundantGroup(sub);
      }
    }
  }

  /**
   * Dissolves given group to parent group in case it is redundant.
   * 
   * @return true if the group was dissolved
   */
  boolean dissolveRedundantGroup(LayoutInterval group) {
    LayoutInterval parent = group.getParent();
    if (parent == null) {
      return false;
    }
    boolean dissolve = false;
    if (group.getSubIntervalCount() == 1) {
      dissolve = true;
    } else if (group.isSequential() && parent.isSequential()) {
      dissolve = true;
    } else if (group.isParallel() && parent.isParallel()) {
      // check for compatible alignment and resizability
      int align = group.getAlignment();
      boolean sameAlign = true;
      boolean subResizing = false;
      Iterator it = group.getSubIntervals();
      while (it.hasNext()) {
        LayoutInterval li = (LayoutInterval) it.next();
        if (!subResizing && LayoutInterval.wantResize(li)) {
          subResizing = true;
        }
        if (li.getAlignment() != align && group.getSubIntervalCount() > 1) {
          sameAlign = false;
        }
      }
      boolean compatible;
      if (subResizing && (sameAlign || group.getGroupAlignment() != BASELINE)) {
        compatible = false;
        if (LayoutInterval.canResize(group) || !LayoutInterval.canResize(parent)) {
          it = parent.getSubIntervals();
          while (it.hasNext()) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (li != group && LayoutInterval.wantResize(li)) {
              compatible = true;
              break;
            }
          }
          if (!compatible) {
            LayoutInterval neighbor =
                LayoutInterval.getNeighbor(parent, group.getAlignment() ^ 1, false, true, true);
            if (neighbor != null
                && neighbor.isEmptySpace()
                && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED) { // default fixed padding means there is no space for
                                                                            // independent size change, so the subgroup can be merged
              compatible = true;
            }
          }
        }
      } else {
        compatible = sameAlign;
      }
      dissolve = compatible;
    }
    if (dissolve) { // the sub-group can be dissolved into parent group
      int index = layoutModel.removeInterval(group);
      while (group.getSubIntervalCount() > 0) {
        LayoutInterval li = group.getSubInterval(0);
        if (parent.isParallel()) { // moving to parallel group
          if (group.isParallel()) { // from parallel group
            if (li.getRawAlignment() == DEFAULT
                && group.getGroupAlignment() != parent.getGroupAlignment()) { // force alignment explicitly
              layoutModel.setIntervalAlignment(li, li.getAlignment());
            }
          } else { // from sequential group
            layoutModel.setIntervalAlignment(li, group.getRawAlignment());
          }
        } else { // moving to sequential group
          if (li.getRawAlignment() != DEFAULT) {
            layoutModel.setIntervalAlignment(li, DEFAULT);
          }
        }
        layoutModel.removeInterval(li);
        group.remove(li);
        layoutModel.addInterval(li, parent, index++);
      }
      return true;
    }
    return false;
  }

  /**
   * This method goes through a sequential group and moves each interval next to an open edge of a
   * parallel group into the group.
   * 
   * @param parent
   *          sequential group to process
   * @param dimension
   */
  void moveInsideSequential(LayoutInterval parent, int dimension) {
    assert parent.isSequential();
    if (!parent.isSequential()) {
      return;
    }
    int alignment = LEADING;
    do {
      LayoutInterval extend = findIntervalToExtend(parent, dimension, alignment);
      if (extend == null) {
        if (alignment == LEADING) {
          alignment = TRAILING;
          extend = findIntervalToExtend(parent, dimension, alignment);
        }
        if (extend == null) {
          break;
        }
      }
      LayoutInterval inGroup = extend.getParent(); // group to infiltrate
      LayoutInterval outGroup = inGroup;
      while (outGroup.getParent() != parent) {
        outGroup = outGroup.getParent();
      }
      int index = parent.indexOf(outGroup);
      int d = alignment == LEADING ? -1 : 1;
      // will the group remain open at the opposite edge?
      boolean commonEndingGap = true;
      for (int i = index - d, n = parent.getSubIntervalCount(); i >= 0 && i < n; i -= d) {
        LayoutInterval li = parent.getSubInterval(i);
        if ((!li.isEmptySpace() || i - d >= 0 && i - d < n) // ignore last gap
            && LayoutInterval.wantResize(li)) { // resizing interval will close the group
                                                // possibly need to separate the rest of the group not to be influenced
          LayoutInterval endGap = parent.getSubInterval(alignment == LEADING ? n - 1 : 0);
          if (endGap == null || endGap.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
            commonEndingGap = false;
            LayoutInterval closing = extend;
            int borderPos = parent.getCurrentSpace().positions[dimension][alignment ^ 1];
            do {
              LayoutInterval par = closing.getParent();
              if (par.isParallel()) {
                separateGroupContent(closing, borderPos, dimension, alignment ^ 1);
              }
              closing = par;
            } while (closing != outGroup);
          }
          break;
        }
      }
      int extendPos = extend.getCurrentSpace().positions[dimension][alignment ^ 1];
      if (!extend.isSequential()) {
        LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
        seq.setAlignment(extend.getAlignment());
        layoutModel.addInterval(seq, inGroup, layoutModel.removeInterval(extend));
        layoutModel.setIntervalAlignment(extend, DEFAULT);
        layoutModel.addInterval(extend, seq, 0);
        extend = seq;
      }
      // move the intervals from outside inside the group, next to found interval (extend)
      LayoutInterval connectingGap = null;
      int idx, addIdx;
      if (alignment == LEADING) {
        idx = index + 1; // start behind the group
        addIdx = extend.getSubIntervalCount(); // add behind the interval
      } else {
        idx = index - 1; // start before the group
        addIdx = 0; // add before the interval
      }
      while (idx >= 0 && idx < parent.getSubIntervalCount()) {
        LayoutInterval li = parent.getSubInterval(idx);
        if (li.isEmptySpace()) {
          if (connectingGap == null) { // first gap
            if (extendPos != outGroup.getCurrentSpace().positions[dimension][alignment ^ 1]) {
              // need to extend the first gap (extended interval inside group is smaller than the group)
              int neighborPos =
                  parent.getSubInterval(idx - d).getCurrentSpace().positions[dimension][alignment];
              int distance = d * (extendPos - neighborPos);
              if (distance > 0) {
                resizeInterval(li, distance);
              }
            }
            connectingGap = li;
          } else if ((idx == 0 || idx == parent.getSubIntervalCount() - 1) && commonEndingGap) { // keep the last gap out
            break;
          }
        }
        layoutModel.removeInterval(li);
        layoutModel.addInterval(li, extend, addIdx);
        if (alignment == LEADING) {
          addIdx++;
        } else {
          idx--;
        }
      }
      // check if the sequence was not whole moved into the group
      if (parent.getSubIntervalCount() == 1) { // only neighborGroup remained, eliminate the parent group
        assert outGroup == parent.getSubInterval(0);
        layoutModel.removeInterval(outGroup);
        LayoutInterval superParent = parent.getParent();
        addContent(outGroup, superParent, layoutModel.removeInterval(parent));
        break;
      }
    } while (true);
  }

  private LayoutInterval findIntervalToExtend(LayoutInterval parent, int dimension, int alignment) {
    int d = alignment == LEADING ? -1 : 1;
    int count = parent.getSubIntervalCount();
    int idx = alignment == LEADING ? count - 1 : 0;
    boolean atBorder = true;
    boolean gap = false;
    while (idx >= 0 && idx < parent.getSubIntervalCount()) {
      LayoutInterval sub = parent.getSubInterval(idx);
      if (sub.isEmptySpace()) {
        gap = true;
      } else {
        if (!atBorder
            && gap
            && sub.isParallel()
            && !LayoutInterval.isClosedGroup(sub, alignment ^ 1)) { // this open parallel sub-group might be a candidate to move inside to
          int startIndex, endIndex;
          if (alignment == LEADING) {
            startIndex = idx + 1;
            endIndex = parent.getSubIntervalCount() - 1;
          } else {
            startIndex = 0;
            endIndex = idx - 1;
          }
          LayoutInterval extend =
              prepareGroupExtension(sub, parent, startIndex, endIndex, dimension, alignment ^ 1);
          if (extend != null) {
            return extend;
          }
        }
        gap = false;
        atBorder = false;
      }
      idx += d;
    }
    return null;
  }

  private LayoutInterval prepareGroupExtension(LayoutInterval group,
      LayoutInterval parent,
      int startIndex,
      int endIndex,
      int dimension,
      int alignment) {
    boolean allOverlapping = true;
    LayoutInterval singleOverlap = null;
    List<LayoutInterval> overlapList = null;
    // looking for all intervals the given space is located next to
    Iterator it = group.getSubIntervals();
    while (it.hasNext()) {
      LayoutInterval li = (LayoutInterval) it.next();
      if (!li.isEmptySpace()) {
        if (LayoutUtils.contentOverlap(li, parent, startIndex, endIndex, dimension ^ 1)) {
          // interval overlaps orthogonally
          if (singleOverlap == null) {
            singleOverlap = li;
          } else {
            if (overlapList == null) {
              overlapList = new LinkedList<LayoutInterval>();
              overlapList.add(singleOverlap);
            }
            overlapList.add(li);
          }
        } else {
          allOverlapping = false;
        }
      }
    }
    if (allOverlapping || singleOverlap == null) {
      return null; // spans whole group or nothing
    }
    if (overlapList != null) { // overlaps multiple intervals
      LayoutInterval subGroup = new LayoutInterval(PARALLEL);
      subGroup.setGroupAlignment(alignment ^ 1);
      subGroup.setAlignment(alignment ^ 1);
      int index = -1;
      do {
        LayoutInterval li = overlapList.remove(0);
        int idx = layoutModel.removeInterval(li);
        if (index < 0) {
          index = idx;
        }
        layoutModel.addInterval(li, subGroup, -1);
        subGroup.getCurrentSpace().expand(li.getCurrentSpace());
      } while (overlapList.size() > 0);
      layoutModel.addInterval(subGroup, group, index);
      singleOverlap = subGroup;
    } else {
      LayoutInterval subParallel;
      if (singleOverlap.isSequential()) {
        subParallel =
            singleOverlap.getSubInterval(alignment == LEADING
                ? 0
                : singleOverlap.getSubIntervalCount() - 1);
        if (!subParallel.isParallel()) {
          subParallel = null;
        }
      } else if (singleOverlap.isParallel()) {
        subParallel = singleOverlap;
      } else {
        subParallel = null;
      }
      if (subParallel != null && !LayoutInterval.isClosedGroup(subParallel, alignment)) {
        LayoutInterval subOverlap =
            prepareGroupExtension(subParallel, parent, startIndex, endIndex, dimension, alignment);
        if (subOverlap != null) {
          singleOverlap = subOverlap;
        }
      }
    }
    return singleOverlap;
  }

  // [couldn't parallelizeWithParentSequence be used instead? or LayoutFeeder.separateSequence?]
  private void separateGroupContent(LayoutInterval separate,
      int outPos,
      int dimension,
      int alignment) {
    LayoutInterval group = separate.getParent();
    assert group.isParallel();
    LayoutInterval remainder = null;
    LayoutInterval remainderGroup = null;
    LayoutRegion remainderSpace = null;
    for (int i = 0; i < group.getSubIntervalCount();) {
      LayoutInterval li = group.getSubInterval(i);
      if (li != separate) {
        assert li.getAlignment() == (alignment ^ 1);
        layoutModel.removeInterval(li);
        if (remainder == null) {
          remainder = li;
        } else {
          if (remainderGroup == null) {
            remainderGroup = new LayoutInterval(PARALLEL);
            remainderGroup.setAlignment(alignment ^ 1);
            remainderGroup.setGroupAlignment(alignment ^ 1);
            layoutModel.addInterval(remainder, remainderGroup, 0);
            remainder = remainderGroup;
          }
          layoutModel.addInterval(li, remainderGroup, -1);
        }
        if (!li.isEmptySpace()) {
          if (remainderSpace == null) {
            remainderSpace = new LayoutRegion();
          }
          remainderSpace.expand(li.getCurrentSpace());
        }
      } else {
        i++;
      }
    }
    remainder.setCurrentSpace(remainderSpace);
    LayoutInterval remainderGap;
    int remainderPos = remainderSpace.positions[dimension][alignment];
    if (LayoutRegion.isValidCoordinate(outPos)) {
      int gapSize = alignment == LEADING ? remainderPos - outPos : outPos - remainderPos;
      remainderGap = new LayoutInterval(SINGLE);
      remainderGap.setSizes(NOT_EXPLICITLY_DEFINED, gapSize, Short.MAX_VALUE);
    } else { // take the existing gap next to group [this case is not used currently]
      remainderGap = LayoutInterval.getDirectNeighbor(group, alignment, false);
      if (remainderGap != null && remainderGap.isEmptySpace()) {
        layoutModel.removeInterval(remainderGap);
        // [should check for last interval in parent]
        LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(group, alignment, true);
        outPos =
            neighbor != null
                ? neighbor.getCurrentSpace().positions[dimension][alignment ^ 1]
                : group.getParent().getCurrentSpace().positions[dimension][alignment];
        int gapSize = alignment == LEADING ? remainderPos - outPos : outPos - remainderPos;
        resizeInterval(remainderGap, gapSize);
      } else {
        remainderGap = null;
      }
    }
    if (remainderGap != null) {
      LayoutInterval seq;
      if (remainder.isSequential()) {
        seq = remainder;
      } else {
        seq = new LayoutInterval(SEQUENTIAL);
        layoutModel.setIntervalAlignment(remainder, DEFAULT);
        layoutModel.addInterval(remainder, seq, 0);
      }
      layoutModel.addInterval(remainderGap, seq, alignment == LEADING ? 0 : -1);
      layoutModel.addInterval(seq, group, -1);
      group.getCurrentSpace().positions[dimension][alignment] = outPos;
    } else {
      layoutModel.addInterval(remainder, group, -1);
    }
  }

  /**
   * Makes given interval parallel with part of its parent sequence.
   */
  void parallelizeWithParentSequence(LayoutInterval interval, int endIndex, int dimension) {
    LayoutInterval parent = interval.getParent();
    assert parent.isParallel();
    LayoutInterval parParent = parent;
    while (!parParent.getParent().isSequential()) {
      parParent = parParent.getParent();
    }
    LayoutInterval parentSeq = parParent.getParent();
    int startIndex = parentSeq.indexOf(parParent);
    if (endIndex < 0) {
      endIndex = parentSeq.getSubIntervalCount() - 1;
    } else if (startIndex > endIndex) {
      int temp = startIndex;
      startIndex = endIndex;
      endIndex = temp;
    }
    layoutModel.removeInterval(interval);
    if (interval.getAlignment() == DEFAULT) {
      layoutModel.setIntervalAlignment(interval, parent.getGroupAlignment());
    }
    addParallelWithSequence(interval, parentSeq, startIndex, endIndex, dimension);
    if (parent.getSubIntervalCount() == 1) {
      addContent(
          layoutModel.removeInterval(parent, 0),
          parent.getParent(),
          layoutModel.removeInterval(parent));
    } else if (parent.getSubIntervalCount() == 0) {
      layoutModel.removeInterval(parent);
    }
  }

  void addParallelWithSequence(LayoutInterval interval,
      LayoutInterval seq,
      int startIndex,
      int endIndex,
      int dimension) {
    LayoutInterval group;
    if (startIndex > 0 || endIndex < seq.getSubIntervalCount() - 1) {
      group = new LayoutInterval(PARALLEL);
      if (interval.getAlignment() != DEFAULT) {
        group.setGroupAlignment(interval.getAlignment());
      }
      int startPos =
          LayoutUtils.getVisualPosition(seq.getSubInterval(startIndex), dimension, LEADING);
      int endPos = LayoutUtils.getVisualPosition(seq.getSubInterval(endIndex), dimension, TRAILING);
      group.getCurrentSpace().set(dimension, startPos, endPos);
      if (startIndex != endIndex) {
        LayoutInterval subSeq = new LayoutInterval(SEQUENTIAL);
        subSeq.setAlignment(seq.getAlignment());
        for (int n = endIndex - startIndex + 1; n > 0; n--) {
          layoutModel.addInterval(layoutModel.removeInterval(seq, startIndex), subSeq, -1);
        }
        layoutModel.addInterval(subSeq, group, 0);
      } else {
        layoutModel.addInterval(layoutModel.removeInterval(seq, startIndex), group, 0);
      }
      layoutModel.addInterval(group, seq, startIndex);
    } else {
      group = seq.getParent();
    }
    layoutModel.addInterval(interval, group, -1);
  }

  int optimizeGaps(LayoutInterval group, int dimension) {
    boolean anyAlignedLeading = false; // if false the group is open at leading edge
    boolean anyAlignedTrailing = false; // if false the group is open at trailing edge
    boolean anyAlignedBoth = false;
    boolean anyGapLeading = false; // if true there is some gap at the leading edge
    boolean anyGapTrailing = false; // if true there is some gap at the trailing edge
    boolean sameMinGapLeading = true; // if true all intervals are aligned with the same gap at leading edge
    boolean sameMinGapTrailing = true; // if true all intervals are aligned with the same gap at trailing edge
    int commonGapLeadingSize = Integer.MIN_VALUE;
    int commonGapTrailingSize = Integer.MIN_VALUE;
    // first analyze the group
    for (int i = 0; i < group.getSubIntervalCount(); i++) {
      LayoutInterval li = group.getSubInterval(i);
      if (li.isEmptySpace()) { // remove container supporting gap
        if (group.getSubIntervalCount() > 1) {
          layoutModel.removeInterval(group, i);
          i--;
          continue;
        }
      }
      boolean leadingAlign = false;
      boolean trailingAlign = false;
      LayoutInterval leadingGap = null;
      LayoutInterval trailingGap = null;
      boolean contentResizing = false;
      boolean noResizing = false;
      if (li.isSequential()) {
        // find out effective alignment of the sequence content without border gaps
        boolean leadGapRes = false;
        boolean trailGapRes = false;
        for (int j = 0; j < li.getSubIntervalCount(); j++) {
          LayoutInterval sub = li.getSubInterval(j);
          if (j == 0 && sub.isEmptySpace()) {
            leadingGap = sub;
            leadGapRes = LayoutInterval.wantResize(sub);
          } else if (j + 1 == li.getSubIntervalCount() && sub.isEmptySpace()) {
            trailingGap = sub;
            trailGapRes = LayoutInterval.wantResize(sub);
          } else if (!contentResizing && LayoutInterval.wantResize(sub)) {
            contentResizing = true;
          }
        }
        if (!contentResizing) {
          if (leadGapRes || trailGapRes) {
            leadingAlign = trailGapRes && !leadGapRes;
            trailingAlign = leadGapRes && !trailGapRes;
          } else {
            noResizing = true;
          }
        }
      } else if (LayoutInterval.wantResize(li)) {
        contentResizing = true;
      } else {
        noResizing = true;
      }
      if (contentResizing) {
        leadingAlign = trailingAlign = true;
      } else if (noResizing) {
        int alignment = li.getAlignment();
        leadingAlign = alignment == LEADING;
        trailingAlign = alignment == TRAILING;
      }
      if (leadingAlign) {
        anyAlignedLeading = true;
        if (trailingAlign) {
          anyAlignedBoth = true;
        }
      }
      if (trailingAlign) {
        anyAlignedTrailing = true;
      }
      if (leadingGap != null) {
        anyGapLeading = true;
        if (sameMinGapLeading) {
          int size =
              leadingAlign || leadingGap.getMinimumSize() == USE_PREFERRED_SIZE
                  ? leadingGap.getPreferredSize()
                  : leadingGap.getMinimumSize();
          if (commonGapLeadingSize != Integer.MIN_VALUE) {
            if (size != commonGapLeadingSize) {
              sameMinGapLeading = false;
            }
          } else {
            commonGapLeadingSize = size;
          }
        }
      } else {
        sameMinGapLeading = false;
      }
      if (trailingGap != null) {
        anyGapTrailing = true;
        if (sameMinGapTrailing) {
          int size =
              trailingAlign || trailingGap.getMinimumSize() == USE_PREFERRED_SIZE
                  ? trailingGap.getPreferredSize()
                  : trailingGap.getMinimumSize();
          if (commonGapTrailingSize != Integer.MIN_VALUE) {
            if (size != commonGapTrailingSize) {
              sameMinGapTrailing = false;
            }
          } else {
            commonGapTrailingSize = size;
          }
        }
      } else {
        sameMinGapTrailing = false;
      }
    }
    if (group.getSubIntervalCount() <= 1 || !anyGapLeading && !anyGapTrailing) {
      return -1;
    }
    if (!anyAlignedBoth) {
      // can't reduce common minimum gap if anything aligned to opposite egde
      if (anyAlignedTrailing) {
        sameMinGapLeading = false;
      }
      if (anyAlignedLeading) {
        sameMinGapTrailing = false;
      }
    }
    int[] groupOuterPos = group.getCurrentSpace().positions[dimension];
    assert groupOuterPos[LEADING] > Short.MIN_VALUE && groupOuterPos[TRAILING] > Short.MIN_VALUE;
    int groupInnerPosLeading =
        LayoutUtils.getOutermostComponent(group, dimension, LEADING).getCurrentSpace().positions[dimension][LEADING];
    int groupInnerPosTrailing =
        LayoutUtils.getOutermostComponent(group, dimension, TRAILING).getCurrentSpace().positions[dimension][TRAILING];
    PaddingType defaultPaddingLeading = null; // if not null, the leading padding has default preferred size
    PaddingType defaultPaddingTrailing = null; // if not null, the trailing padding has default preferred size
    boolean resizingGapLeading = false;
    boolean resizingGapTrailing = false;
    // remove gaps where needed
    for (int i = 0; i < group.getSubIntervalCount(); i++) {
      LayoutInterval li = group.getSubInterval(i);
      if (li.isSequential()) {
        if (anyGapLeading) {
          LayoutInterval gap = li.getSubInterval(0);
          if (gap.isEmptySpace()) {
            if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
              LayoutInterval neighbor =
                  LayoutInterval.getNeighbor(gap, LEADING, false, true, false);
              if (neighbor != null
                  && neighbor.isEmptySpace()
                  && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                // preferred gap with preferred gap neighbor - would not work
                layoutModel.removeInterval(gap);
                gap = null;
              }
            }
            if (gap != null && (!anyAlignedLeading || sameMinGapLeading)) {
              if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                  && isEndingDefaultGapEffective(li, dimension, LEADING)) { // default padding to be used as common gap
                defaultPaddingLeading = gap.getPaddingType();
                if (defaultPaddingLeading == null) {
                  defaultPaddingLeading = PaddingType.RELATED;
                }
              }
              if (gap.getMaximumSize() >= Short.MAX_VALUE) {
                if (li.getAlignment() == LEADING) {
                  layoutModel.setIntervalAlignment(li, TRAILING);
                }
                if (!anyAlignedLeading) {
                  resizingGapLeading = true;
                }
              }
              layoutModel.removeInterval(gap);
            }
          }
        }
        if (anyGapTrailing) {
          LayoutInterval gap = li.getSubInterval(li.getSubIntervalCount() - 1);
          if (gap.isEmptySpace()) {
            if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
              LayoutInterval neighbor =
                  LayoutInterval.getNeighbor(gap, TRAILING, false, true, false);
              if (neighbor != null
                  && neighbor.isEmptySpace()
                  && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED) {
                // preferred gap with preferred gap neighbor - would not work
                layoutModel.removeInterval(gap);
                gap = null;
              }
            }
            if (gap != null && (!anyAlignedTrailing || sameMinGapTrailing)) {
              if (gap.getPreferredSize() == NOT_EXPLICITLY_DEFINED
                  && isEndingDefaultGapEffective(li, dimension, TRAILING)) { // default padding to be used as common gap
                defaultPaddingTrailing = gap.getPaddingType();
                if (defaultPaddingTrailing == null) {
                  defaultPaddingTrailing = PaddingType.RELATED;
                }
              }
              if (gap.getMaximumSize() >= Short.MAX_VALUE) {
                if (li.getAlignment() == TRAILING) {
                  layoutModel.setIntervalAlignment(li, LEADING);
                }
                if (!anyAlignedTrailing) {
                  resizingGapTrailing = true;
                }
              }
              layoutModel.removeInterval(gap);
            }
          }
        }
        if (li.getSubIntervalCount() == 1) {
          // only one interval remained in sequence - cancel the sequence
          layoutModel.removeInterval(group, i); // removes li from group
          LayoutInterval sub = layoutModel.removeInterval(li, 0); // removes last interval from li
          layoutModel.setIntervalAlignment(sub, li.getRawAlignment());
          layoutModel.addInterval(sub, group, i);
        }
      }
    }
    LayoutInterval leadingGap = null;
    LayoutInterval trailingGap = null;
    if (anyGapLeading) {
      if (!anyAlignedLeading) { // group is open at leading edge
        int size = groupInnerPosLeading - groupOuterPos[LEADING];
        if (size > 0 || defaultPaddingLeading != null) {
          leadingGap = new LayoutInterval(SINGLE);
          if (defaultPaddingLeading != null) {
            leadingGap.setPaddingType(defaultPaddingLeading);
          } else {
            leadingGap.setPreferredSize(size);
            if (!resizingGapLeading) {
              leadingGap.setMinimumSize(USE_PREFERRED_SIZE);
            }
          }
          if (resizingGapLeading) {
            leadingGap.setMaximumSize(Short.MAX_VALUE);
          }
        }
      } else if (sameMinGapLeading) {
        leadingGap = new LayoutInterval(SINGLE);
        //                int size = commonGapLeading.getMinimumSize();
        //                if (size == USE_PREFERRED_SIZE)
        //                    size = commonGapLeading.getPreferredSize();
        //                leadingGap.setSizes(size, size, USE_PREFERRED_SIZE);
        leadingGap.setSizes(commonGapLeadingSize, commonGapLeadingSize, USE_PREFERRED_SIZE);
        if (commonGapLeadingSize == DEFAULT) {
          leadingGap.setPaddingType(defaultPaddingLeading);
        }
      }
    }
    if (anyGapTrailing) {
      if (!anyAlignedTrailing) { // group is open at trailing edge
        int size = groupOuterPos[TRAILING] - groupInnerPosTrailing;
        if (size > 0 || defaultPaddingTrailing != null) {
          trailingGap = new LayoutInterval(SINGLE);
          if (defaultPaddingTrailing != null) {
            trailingGap.setPaddingType(defaultPaddingTrailing);
          } else {
            trailingGap.setPreferredSize(size);
            if (!resizingGapTrailing) {
              trailingGap.setMinimumSize(USE_PREFERRED_SIZE);
            }
          }
          if (resizingGapTrailing) {
            trailingGap.setMaximumSize(Short.MAX_VALUE);
          }
        }
      } else if (sameMinGapTrailing) {
        trailingGap = new LayoutInterval(SINGLE);
        //                int size = commonGapTrailing.getMinimumSize();
        //                if (size == USE_PREFERRED_SIZE)
        //                    size = commonGapTrailing.getPreferredSize();
        //                trailingGap.setSizes(size, size, USE_PREFERRED_SIZE);
        trailingGap.setSizes(commonGapTrailingSize, commonGapTrailingSize, USE_PREFERRED_SIZE);
        if (commonGapTrailingSize == DEFAULT) {
          trailingGap.setPaddingType(defaultPaddingTrailing);
        }
      }
    }
    if (leadingGap != null || trailingGap != null) {
      if (leadingGap != null || !LayoutRegion.isValidCoordinate(groupOuterPos[LEADING])) {
        groupOuterPos[LEADING] = groupInnerPosLeading;
      }
      if (trailingGap != null || !LayoutRegion.isValidCoordinate(groupOuterPos[TRAILING])) {
        groupOuterPos[TRAILING] = groupInnerPosTrailing;
      }
      groupOuterPos[CENTER] = (groupInnerPosLeading + groupInnerPosTrailing) / 2;
      if (leadingGap != null) {
        group = insertGap(leadingGap, group, groupInnerPosLeading, dimension, LEADING);
      }
      if (trailingGap != null) {
        group = insertGap(trailingGap, group, groupInnerPosTrailing, dimension, TRAILING);
      }
      LayoutInterval parent = group.getParent();
      return parent != null ? parent.indexOf(group) : -1;//idx;
    }
    return -1;
  }

  private boolean isEndingDefaultGapEffective(LayoutInterval seq, int dimension, int alignment) {
    assert seq.isSequential() && (alignment == LEADING || alignment == TRAILING);
    int idx = alignment == LEADING ? 0 : seq.getSubIntervalCount() - 1;
    int d = alignment == LEADING ? 1 : -1;
    LayoutInterval gap = seq.getSubInterval(idx);
    LayoutInterval neighbor = seq.getSubInterval(idx + d);
    if (LayoutInterval.getEffectiveAlignment(neighbor, alignment) == alignment) {
      return true; // aligned
    } else {
      int prefDistance = LayoutUtils.getSizeOfDefaultGap(gap, visualMapper);
      int pos1 = neighbor.getCurrentSpace().positions[dimension][alignment];
      LayoutInterval outerNeighbor = LayoutInterval.getNeighbor(gap, alignment, true, true, false);
      int pos2 =
          outerNeighbor != null
              ? outerNeighbor.getCurrentSpace().positions[dimension][alignment ^ 1]
              : LayoutInterval.getRoot(seq).getCurrentSpace().positions[dimension][alignment];
      int currentDistance = (pos1 - pos2) * d;
      return currentDistance <= prefDistance;
    }
  }

  boolean cutStartingGap(LayoutInterval group, int size, int dimension, int alignment) {
    assert group.isGroup() && size > 0 && (alignment == LEADING || alignment == TRAILING);
    // [just very simple impl. for now - considering just one sequence...]
    LayoutInterval seq = null;
    if (group.isSequential()) {
      seq = group;
    } else if (group.getSubIntervalCount() == 1) {
      LayoutInterval li = group.getSubInterval(0);
      if (li.isSequential() && LayoutInterval.isAlignedAtBorder(li, alignment)) {
        seq = li;
      }
    }
    if (seq != null && seq.getSubIntervalCount() > 1) {
      LayoutInterval gap =
          seq.getSubInterval(alignment == LEADING ? 0 : seq.getSubIntervalCount() - 1);
      LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(gap, alignment ^ 1, true);
      if (gap != null && gap.isEmptySpace() && neighbor != null) {
        int currentSize = gap.getPreferredSize();
        if (currentSize == NOT_EXPLICITLY_DEFINED) {
          currentSize =
              LayoutRegion.distance(
                  group.getCurrentSpace(),
                  neighbor.getCurrentSpace(),
                  dimension,
                  alignment,
                  alignment) * (alignment == TRAILING ? -1 : 1);
        }
        if (currentSize >= size) {
          if (currentSize > size) {
            resizeInterval(gap, currentSize - size);
          } else {
            layoutModel.removeInterval(gap);
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Inserts a gap before or after specified interval. If in a sequence, the method takes care about
   * merging gaps if there is already some as neighbor. Expects the actual positions of the sequence
   * are up-to-date.
   * 
   * @param gap
   *          the gap to be inserted
   * @param interval
   *          the interval before or after which the gap is added
   * @param pos
   *          expected real position of the end of the interval where the gap is added (need not
   *          correspond to that stored in the interval)
   * @param dimension
   * @param alignment
   *          at which side of the interval the gap is added (LEADING or TRAILING)
   */
  LayoutInterval insertGap(LayoutInterval gap,
      LayoutInterval interval,
      int pos,
      int dimension,
      int alignment) {
    assert alignment == LEADING || alignment == TRAILING;
    assert !interval.isSequential();
    assert gap.isEmptySpace();
    LayoutInterval parent = interval.getParent();
    if (parent == null) {
      assert interval.isParallel();
      parent = interval;
      if (parent.getSubIntervalCount() > 1) {
        LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
        seq.getCurrentSpace().set(
            dimension,
            alignment == LEADING ? pos : interval.getCurrentSpace().positions[dimension][LEADING],
            alignment == LEADING ? interval.getCurrentSpace().positions[dimension][TRAILING] : pos);
        layoutModel.addInterval(seq, parent, -1);
        interval = new LayoutInterval(PARALLEL);
        interval.getCurrentSpace().set(dimension, parent.getCurrentSpace());
        layoutModel.addInterval(interval, seq, 0);
        while (parent.getSubIntervalCount() > 1) {
          layoutModel.addInterval(layoutModel.removeInterval(parent, 0), interval, -1);
        }
        parent = seq;
      } else {
        interval = parent.getSubInterval(0);
        if (interval.isSequential()) {
          parent = interval;
          int subIdx = alignment == LEADING ? 0 : parent.getSubIntervalCount() - 1;
          interval = parent.getSubInterval(subIdx);
          if (interval.isEmptySpace()) {
            subIdx += alignment == LEADING ? 1 : -1;
            LayoutInterval neighbor =
                subIdx >= 0 && subIdx < parent.getSubIntervalCount()
                    ? parent.getSubInterval(subIdx)
                    : null;
            int[] outerSpace = parent.getParent().getCurrentSpace().positions[dimension];
            int otherPos =
                neighbor != null
                    ? neighbor.getCurrentSpace().positions[dimension][alignment]
                    : outerSpace[alignment ^ 1];
            int mergedSize = (outerSpace[alignment] - otherPos) * (alignment == LEADING ? -1 : 1);
            eatGap(interval, gap, mergedSize);
            return neighbor != null ? neighbor : interval;
          }
        } else {
          LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
          seq.getCurrentSpace().set(
              dimension,
              alignment == LEADING ? pos : interval.getCurrentSpace().positions[dimension][LEADING],
              alignment == LEADING
                  ? interval.getCurrentSpace().positions[dimension][TRAILING]
                  : pos);
          layoutModel.addInterval(seq, parent, -1);
          layoutModel.removeInterval(interval);
          layoutModel.addInterval(interval, seq, -1);
          parent = seq;
        }
      }
    }
    if (parent.isSequential()) {
      // we can't use insertGapIntoSequence here because 'pos' can be special
      LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
      if (neighbor != null && neighbor.isEmptySpace()) {
        LayoutInterval next = LayoutInterval.getDirectNeighbor(neighbor, alignment, false);
        int otherPos =
            next != null
                ? next.getCurrentSpace().positions[dimension][alignment ^ 1]
                : parent.getCurrentSpace().positions[dimension][alignment];
        int mergedSize = (pos - otherPos) * (alignment == LEADING ? 1 : -1);
        eatGap(neighbor, gap, mergedSize);
      } else {
        int idx = parent.indexOf(interval) + (alignment == LEADING ? 0 : 1);
        layoutModel.addInterval(gap, parent, idx);
      }
    } else { // parallel parent
      LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
      int idx = layoutModel.removeInterval(interval);
      seq.setAlignment(interval.getAlignment());
      seq.getCurrentSpace().set(
          dimension,
          alignment == LEADING ? pos : interval.getCurrentSpace().positions[dimension][LEADING],
          alignment == LEADING ? interval.getCurrentSpace().positions[dimension][TRAILING] : pos);
      layoutModel.addInterval(seq, parent, idx);
      layoutModel.setIntervalAlignment(interval, DEFAULT);
      layoutModel.addInterval(interval, seq, 0);
      layoutModel.addInterval(gap, seq, alignment == LEADING ? 0 : 1);
    }
    return interval;
  }

  int insertGapIntoSequence(LayoutInterval gap, LayoutInterval seq, int index, int dimension) {
    assert gap.isEmptySpace();
    LayoutInterval otherGap = null;
    int alignment = DEFAULT;
    if (index >= 0 && index < seq.getSubIntervalCount()) {
      otherGap = seq.getSubInterval(index);
      if (otherGap.isEmptySpace()) {
        alignment = TRAILING;
      }
    }
    if (alignment == DEFAULT && index > 0) {
      otherGap = seq.getSubInterval(index - 1);
      if (otherGap.isEmptySpace()) {
        alignment = LEADING;
      }
    }
    if (alignment == DEFAULT) {
      layoutModel.addInterval(gap, seq, index);
      return index; // gap was added normally
    }
    int pos1, pos2;
    LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(otherGap, alignment, true);
    pos1 =
        neighbor != null
            ? neighbor.getCurrentSpace().positions[dimension][alignment ^ 1]
            : seq.getCurrentSpace().positions[dimension][alignment];
    neighbor = LayoutInterval.getDirectNeighbor(otherGap, alignment ^ 1, true);
    pos2 =
        neighbor != null
            ? neighbor.getCurrentSpace().positions[dimension][alignment]
            : seq.getCurrentSpace().positions[dimension][alignment ^ 1];
    eatGap(otherGap, gap, Math.abs(pos2 - pos1));
    return alignment == LEADING ? index - 1 : index; // gap was eaten
  }

  void eatGap(LayoutInterval main, LayoutInterval eaten, int currentMergedSize) {
    int min;
    int min1 = main.getMinimumSize();
    if (min1 == USE_PREFERRED_SIZE) {
      min1 = main.getPreferredSize();
    }
    int min2 = eaten.getMinimumSize();
    if (min2 == USE_PREFERRED_SIZE) {
      min2 = eaten.getPreferredSize();
    }
    if (min1 == 0) {
      min = min2;
    } else if (min2 == 0) {
      min = min1;
    } else if (!LayoutInterval.canResize(main) && !LayoutInterval.canResize(eaten)) {
      min = USE_PREFERRED_SIZE;
    } else if (min1 == NOT_EXPLICITLY_DEFINED || min2 == NOT_EXPLICITLY_DEFINED) {
      min = NOT_EXPLICITLY_DEFINED;
    } else {
      min = min1 + min2;
    }
    int pref;
    int pref1 = main.getPreferredSize();
    int pref2 = eaten.getPreferredSize();
    if (pref1 == 0) {
      pref = pref2;
    } else if (pref2 == 0) {
      pref = pref1;
    } else if (pref1 == NOT_EXPLICITLY_DEFINED || pref2 == NOT_EXPLICITLY_DEFINED) {
      pref = currentMergedSize;
    } else {
      pref = pref1 + pref2;
    }
    int max =
        main.getMaximumSize() >= Short.MAX_VALUE || eaten.getMaximumSize() >= Short.MAX_VALUE
            ? Short.MAX_VALUE
            : USE_PREFERRED_SIZE;
    layoutModel.setIntervalSize(main, min, pref, max);
    if (eaten.getParent() != null) {
      layoutModel.removeInterval(eaten);
    }
  }

  void mergeAdjacentGaps(Set<LayoutComponent> updatedContainers) {
    Iterator it = layoutModel.getAllComponents();
    while (it.hasNext()) {
      LayoutComponent comp = (LayoutComponent) it.next();
      if (!comp.isLayoutContainer()) {
        continue;
      }
      boolean updated = false;
      for (LayoutInterval[] roots : comp.getLayoutRoots()) {
        for (int dim = 0; dim < DIM_COUNT; dim++) {
          updated = mergeAdjacentGaps(roots[dim], dim) || updated;
        }
      }
      if (updated) {
        updatedContainers.add(comp);
      }
    }
  }

  boolean mergeAdjacentGaps(LayoutInterval root, int dimension) {
    assert root.isGroup();
    boolean updated = false;
    if (root.isSequential()) {
      for (int i = 0; i < root.getSubIntervalCount(); i++) {
        LayoutInterval interval = root.getSubInterval(i);
        if (interval.isEmptySpace() && i + 1 < root.getSubIntervalCount()) {
          LayoutInterval next = root.getSubInterval(i + 1);
          if (next.isEmptySpace()) {
            if (i + 2 < root.getSubIntervalCount()) {
              LayoutInterval nextNext = root.getSubInterval(i + 2);
              if (nextNext.isEmptySpace()) {
                i--; // The merged gap should be merged with nextNext gap
              }
            }
            updated = true;
            eatGap(interval, next, NOT_EXPLICITLY_DEFINED);
          }
        }
      }
    }
    Iterator iter = root.getSubIntervals();
    while (iter.hasNext()) {
      LayoutInterval subInterval = (LayoutInterval) iter.next();
      if (subInterval.isGroup()) {
        updated = updated || mergeAdjacentGaps(subInterval, dimension);
      }
    }
    return updated;
  }

  void suppressResizingOfSurroundingGaps(LayoutInterval interval) {
    LayoutInterval parent = interval.getParent();
    while (parent != null) {
      if (parent.isSequential()) {
        for (Iterator it = parent.getSubIntervals(); it.hasNext();) {
          LayoutInterval sub = (LayoutInterval) it.next();
          if (sub != interval && sub.isEmptySpace() && LayoutInterval.canResize(sub)) {
            int pref = sub.getPreferredSize();
            int min = sub.getMinimumSize() != pref ? USE_PREFERRED_SIZE : pref;
            int max = USE_PREFERRED_SIZE;
            layoutModel.setIntervalSize(sub, min, pref, max);
          }
        }
      } else if (!LayoutInterval.canResize(parent)) {
        break;
      }
      interval = parent;
      parent = interval.getParent();
    }
  }
}
