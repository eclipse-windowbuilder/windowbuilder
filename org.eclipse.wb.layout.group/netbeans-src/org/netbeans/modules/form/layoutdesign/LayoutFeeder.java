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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is responsible for adding layout intervals to model based on mouse actions done by the
 * user (input provided from LayoutDragger). When an instance is created, it analyzes the original
 * positions - before the adding operation is performed (this is needed in case of resizing). Then
 * 'add' method is called to add the intervals on desired place. It is responsibility of the caller
 * to remove the intervals/components from original locations before calling 'add'. Note this class
 * does not add LayoutComponent instances to model.
 * 
 * @author Tomas Pavek
 */
class LayoutFeeder implements LayoutConstants {
  boolean imposeSize;
  boolean optimizeStructure;
  private final LayoutModel layoutModel;
  private final LayoutOperations operations;
  private final LayoutDragger dragger;
  private final IncludeDesc[] originalPositions1 = new IncludeDesc[DIM_COUNT];
  private final IncludeDesc[] originalPositions2 = new IncludeDesc[DIM_COUNT];
  private final boolean[] originalLPositionsFixed = new boolean[DIM_COUNT];
  private final boolean[] originalTPositionsFixed = new boolean[DIM_COUNT];
  private final LayoutDragger.PositionDef[] newPositions = new LayoutDragger.PositionDef[DIM_COUNT];
  private final LayoutInterval[] addingIntervals; // horizontal, vertical
  private final boolean[] becomeResizing = new boolean[DIM_COUNT];
  // working context (actual dimension)
  private int dimension;
  private LayoutInterval addingInterval;
  private LayoutRegion addingSpace;
  private boolean solveOverlap;
  private boolean originalLPosFixed;
  private boolean originalTPosFixed;
  // params used when searching for the right place (inclusion)
  private int aEdge;
  private LayoutInterval aSnappedParallel;
  private LayoutInterval aSnappedNextTo;
  private PaddingType aPaddingType;

  private static class IncludeDesc {
    LayoutInterval parent;
    int index = -1; // if adding next to
    boolean newSubGroup; // can be true if parent is sequential (parallel subgroup for part of the sequence is to be created)
    LayoutInterval neighbor; // if included in a sequence with single interval (which is not in sequence yet)
    LayoutInterval snappedParallel; // not null if aligning in parallel
    LayoutInterval snappedNextTo; // not null if snapped next to (can but need not be 'neighbor')
    PaddingType paddingType; // type of padding if snapped (next to)
    int alignment; // the edge this object defines (leading or trailing or default)
    boolean fixedPosition; // whether distance from the neighbor is definitely fixed
    int distance = Integer.MAX_VALUE;
    int ortDistance = Integer.MAX_VALUE;

    boolean snapped() {
      return snappedNextTo != null || snappedParallel != null;
    }
  }

  // -----
  LayoutFeeder(LayoutOperations operations, LayoutDragger dragger, LayoutInterval[] addingIntervals) {
    layoutModel = operations.getModel();
    this.operations = operations;
    this.dragger = dragger;
    this.addingIntervals = addingIntervals;
    for (int dim = 0; dim < DIM_COUNT; dim++) {
      dimension = dim;
      if (dragger.isResizing()) {
        LayoutInterval adding = addingIntervals[dim];
        if (dragger.isResizing(dim)) {
          IncludeDesc pos = findOutCurrentPosition(adding, dim, dragger.getResizingEdge(dim) ^ 1);
          LayoutDragger.PositionDef newPos = dragger.getPositions()[dim];
          if ((newPos == null || !newPos.snapped) && !pos.snapped()) {
            pos.alignment = LayoutInterval.getEffectiveAlignment(adding);
          }
          originalPositions1[dim] = pos;
          newPositions[dim] = newPos;
          becomeResizing[dim] = checkResizing(); // if to make the interval resizing
        } else { // this dimension has not been resized
          int alignment = DEFAULT;
          IncludeDesc pos1 = findOutCurrentPosition(adding, dim, alignment);
          originalPositions1[dim] = pos1;
          alignment = pos1.alignment;
          if (alignment == LEADING || alignment == TRAILING) {
            IncludeDesc pos2 = findOutCurrentPosition(adding, dim, alignment ^ 1);
            if (pos2.snapped()) {
              originalPositions2[dim] = pos2;
            } // don't remember second position if not snapped, one is enough
          }
        }
        originalLPositionsFixed[dim] = isFixedRelativePosition(adding, LEADING);
        originalTPositionsFixed[dim] = isFixedRelativePosition(adding, TRAILING);
      } else {
        newPositions[dim] = dragger.getPositions()[dim];
      }
    }
  }

  void add() {
    int overlapDim = getDimensionSolvingOverlap(newPositions);
    for (int dim = overlapDim, dc = 0; dc < DIM_COUNT; dim ^= 1, dc++) {
      dimension = dim;
      addingInterval = addingIntervals[dim];
      addingSpace = dragger.getMovingSpace();
      addingInterval.setCurrentSpace(addingSpace);
      solveOverlap = overlapDim == dim;
      IncludeDesc originalPos1 = originalPositions1[dim];
      IncludeDesc originalPos2 = originalPositions2[dim];
      correctNeighborInSequence(originalPos1);
      correctNeighborInSequence(originalPos2);
      if (dragger.isResizing()) {
        originalLPosFixed = originalLPositionsFixed[dim];
        originalTPosFixed = originalTPositionsFixed[dim];
        if (dragger.isResizing(dim)) {
          layoutModel.setIntervalSize(addingInterval, becomeResizing[dim]
              ? NOT_EXPLICITLY_DEFINED
              : USE_PREFERRED_SIZE, addingSpace.size(dim), becomeResizing[dim]
              ? Short.MAX_VALUE
              : USE_PREFERRED_SIZE);
        }
      }
      LayoutDragger.PositionDef newPos = newPositions[dim];
      if (newPos != null && (newPos.alignment == CENTER || newPos.alignment == BASELINE)) {
        // hack: simplified adding to a closed group
        aEdge = newPos.alignment;
        aSnappedParallel = newPos.interval;
        addSimplyAligned();
        continue;
      }
      if (dragger.isResizing()
          && (originalPos1.alignment == CENTER || originalPos1.alignment == BASELINE)) {
        aEdge = originalPos1.alignment;
        aSnappedParallel = originalPos1.snappedParallel;
        addSimplyAligned();
        continue;
      }
      // prepare task for searching the position
      IncludeDesc inclusion1 = null;
      IncludeDesc inclusion2 = null;
      List<IncludeDesc> inclusions = new LinkedList<IncludeDesc>();
      boolean preserveOriginal = false;
      // if resizing in the other dimension then renew the original position
      if (dragger.isResizing(dim ^ 1)) {
        aEdge = originalPos1.alignment;
        aSnappedParallel = originalPos1.snappedParallel;
        aSnappedNextTo = originalPos1.snappedNextTo;
        aPaddingType = originalPos1.paddingType;
      }
      // if snapped in dragger then always find the position
      else if (newPos != null) {
        aEdge = newPos.alignment;
        aSnappedParallel = !newPos.nextTo ? newPos.interval : null;
        aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
        aPaddingType = newPos.paddingType;
        // if resizing only in this dimension then preserve the original position
        preserveOriginal = dragger.isResizing(dim);
      }
      // if resizing only in this dimension and without snap then check for
      // possible growing in parallel with part of its own parent sequence
      else if (dragger.isResizing(dim)) {
        aEdge = originalPos1.alignment;
        aSnappedParallel = originalPos1.snappedParallel;
        aSnappedNextTo = originalPos1.snappedNextTo;
        aPaddingType = originalPos1.paddingType;
        preserveOriginal = true;
      }
      // otherwise plain moving without snap
      else {
        aEdge = DEFAULT;
        aSnappedParallel = aSnappedNextTo = null;
        aPaddingType = null;
      }
      LayoutInterval root = dragger.getTargetRoots()[dim];
      analyzeParallel(root, inclusions);
      // make sure an inclusion for parallel aligning is considered, choose best inclusion
      if (inclusions.isEmpty()) { // inclusion for parallel aligning not found
        assert aSnappedParallel != null;
        if (originalPos1 != null && originalPos1.alignment == aEdge) {
          inclusions.add(originalPos1);
        } else {
          addAligningInclusion(inclusions);
        }
      } else {
        IncludeDesc preferred = addAligningInclusion(inclusions); // make sure it is there...
        if (inclusions.size() > 1) {
          if (preferred == null || preserveOriginal && originalPos1.alignment == aEdge) {
            preferred = originalPos1;
          }
          mergeParallelInclusions(inclusions, preferred, preserveOriginal);
          assert inclusions.size() == 1;
        }
      }
      IncludeDesc found = inclusions.get(0);
      inclusions.clear();
      if (preserveOriginal) { // resized in this dimension only
        inclusion1 = originalPos1;
        if (found != originalPos1) {
          if (newPos != null) {
            inclusion2 = found;
          }
          LayoutInterval origParent = originalPos1.parent;
          if (found.parent == origParent
              && found.newSubGroup
              || origParent.isSequential()
              && origParent.getParent() == found.parent) {
            inclusion1.newSubGroup = true;
          }
        }
      } else {
        inclusion1 = found;
        // second search needed if resizing in the other dimension
        // and the second edge snapped in current dimension
        if (dragger.isResizing(dim ^ 1) && (newPos != null || originalPos2 != null)) {
          if (newPos != null) { // find inclusion based on position from dragger
            assert dragger.isResizing(dim);
            aEdge = newPos.alignment;
            aSnappedParallel = !newPos.nextTo ? newPos.interval : null;
            aSnappedNextTo = newPos.snapped && newPos.nextTo ? newPos.interval : null;
            aPaddingType = newPos.paddingType;
          } else { // need to renew the original position
            assert !dragger.isResizing(dim);
            aEdge = originalPos2.alignment;
            aSnappedParallel = originalPos2.snappedParallel;
            aSnappedNextTo = originalPos2.snappedNextTo;
            aPaddingType = originalPos2.paddingType;
          }
          // second round searching
          analyzeParallel(root, inclusions);
          if (inclusions.isEmpty()) { // inclusion for parallel aligning not found
            assert aSnappedParallel != null;
            if (originalPos2 != null && originalPos2.alignment == aEdge) {
              inclusions.add(originalPos2);
            } else {
              addAligningInclusion(inclusions);
            }
          } else {
            IncludeDesc preferred = addAligningInclusion(inclusions);
            if (inclusions.size() > 1) {
              if (preferred == null) {
                preferred = originalPos2 != null ? originalPos2 : originalPos1;
              }
              mergeParallelInclusions(inclusions, preferred, false);
              assert inclusions.size() == 1;
            }
          }
          inclusion2 = inclusions.get(0);
          inclusions.clear();
        }
      }
      if (!mergeSequentialInclusions(inclusion1, inclusion2)) {
        inclusion2 = null;
      }
      addInterval(inclusion1, inclusion2);
    }
  }

  private static IncludeDesc findOutCurrentPosition(LayoutInterval interval,
      int dimension,
      int alignment) {
    LayoutInterval parent = interval.getParent();
    int nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
    IncludeDesc iDesc = new IncludeDesc();
    if (parent.isSequential() && nonEmptyCount > 1) {
      if (alignment < 0) {
        alignment = LEADING;
      }
      if (nonEmptyCount == 2) { // the sequence may not survive when the interval is removed
        // (if it survives, the inclusion gets corrected by 'correctNeighborInSequence' method)
        iDesc.parent = parent.getParent();
        int index = 0;
        for (int i = parent.getSubIntervalCount() - 1; i >= 0; i--) {
          LayoutInterval li = parent.getSubInterval(i);
          if (li == interval) {
            index = i;
          } else if (!li.isEmptySpace()) {
            iDesc.neighbor = li; // next to a single interval in parallel group
            iDesc.index = index;
            break;
          }
        }
      } else { // simply goes to the sequence
        iDesc.parent = parent;
        iDesc.index = parent.indexOf(interval);
      }
    } else { // parallel parent
      if (parent.isSequential()) {
        parent = parent.getParent(); // alone in sequence, take parent
        nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
        if (alignment < 0) {
          alignment = LEADING;
        }
      } else {
        int currentAlign = interval.getAlignment();
        if (alignment < 0 || currentAlign != LEADING && currentAlign != TRAILING) {
          alignment = currentAlign;
        }
      }
      if (nonEmptyCount <= 2 && parent.getParent() != null) {
        // parallel group will not survive when the interval is removed
        LayoutInterval subGroup = parent;
        parent = parent.getParent();
        if (parent.isSequential()) {
          boolean ortOverlap = false;
          for (Iterator it = parent.getSubIntervals(); it.hasNext();) {
            LayoutInterval li = (LayoutInterval) it.next();
            if (!li.isEmptySpace()
                && !li.isParentOf(interval)
                && LayoutRegion.overlap(
                    interval.getCurrentSpace(),
                    li.getCurrentSpace(),
                    dimension ^ 1,
                    0)) { // orthogonal overlap - need to stay within the sequence
              ortOverlap = true;
              break;
            }
          }
          if (ortOverlap) { // parallel with part of the sequence
            iDesc.newSubGroup = true;
            iDesc.index = parent.indexOf(subGroup);
          } else {
            // parallel with whole sequence
            parent = parent.getParent();
          }
        }
        iDesc.parent = parent;
      } else {
        iDesc.parent = parent; // simply goes to the parallel group
      }
    }
    if (alignment == LEADING || alignment == TRAILING) {
      iDesc.fixedPosition = isFixedRelativePosition(interval, alignment);
    }
    iDesc.snappedParallel = findAlignedInterval(interval, dimension, alignment);
    // check for next to aligning
    if (iDesc.snappedParallel == null && (alignment == LEADING || alignment == TRAILING)) {
      LayoutInterval gap = LayoutInterval.getNeighbor(interval, alignment, false, true, false);
      if (gap != null && LayoutInterval.isFixedDefaultPadding(gap)) {
        LayoutInterval prev = LayoutInterval.getDirectNeighbor(gap, alignment ^ 1, true);
        if (prev == interval
            || LayoutInterval.isPlacedAtBorder(interval, prev, dimension, alignment)) {
          LayoutInterval next = LayoutInterval.getNeighbor(gap, alignment, true, true, false);
          if (next != null) {
            if (next.getParent() == gap.getParent()
                || next.getCurrentSpace().positions[dimension][alignment ^ 1] == gap.getParent().getCurrentSpace().positions[dimension][alignment]) { // the next interval is really at preferred distance
              iDesc.snappedNextTo = next;
              iDesc.paddingType = gap.getPaddingType();
            }
          } else { // likely next to the root group border
            next = LayoutInterval.getRoot(interval);
            if (LayoutInterval.isPlacedAtBorder(gap.getParent(), next, dimension, alignment)) {
              iDesc.snappedNextTo = next;
            }
          }
        }
      }
    }
    iDesc.alignment = alignment;
    return iDesc;
  }

  private static boolean isFixedRelativePosition(LayoutInterval interval, int edge) {
    assert edge == LEADING || edge == TRAILING;
    LayoutInterval parent = interval.getParent();
    if (parent == null) {
      return true;
    }
    if (parent.isSequential()) {
      LayoutInterval li = LayoutInterval.getDirectNeighbor(interval, edge, false);
      if (li != null) {
        return !LayoutInterval.wantResize(li);
      } else {
        interval = parent;
        parent = interval.getParent();
      }
    }
    if (!LayoutInterval.isAlignedAtBorder(interval, parent, edge)
        && LayoutInterval.contentWantResize(parent)) {
      return false;
    }
    return isFixedRelativePosition(parent, edge);
  }

  private static LayoutInterval findAlignedInterval(LayoutInterval interval,
      int dimension,
      int alignment) {
    LayoutInterval parent;
    LayoutInterval alignedInterval = null;
    // need to force parallel alignment in case of indented position whose
    // parent parallel group won't survive removing of the resizing component
    // (the resizing interval will target a higher group where the resulting
    // indent gap might be different)
    boolean indent = false;
    parent = interval.getParent();
    if ((alignment == LEADING || alignment == TRAILING)
        && parent.isSequential()
        && LayoutInterval.getCount(parent, -1, true) == 1) { // alone in sequence
      LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
      if (neighbor != null
          && neighbor.isEmptySpace()
          && !LayoutInterval.canResize(neighbor)
          && LayoutInterval.getCount(parent.getParent(), LayoutRegion.ALL_POINTS, true) == 2) { // otherwise only one sibling in parallel group - candidate for aligned interval
        indent = true;
      }
    }
    do {
      parent = LayoutInterval.getFirstParent(interval, PARALLEL);
      if (!indent) {
        boolean aligned =
            alignment == LEADING || alignment == TRAILING ? LayoutInterval.isAlignedAtBorder(
                interval,
                parent,
                alignment) : interval.getParent() == parent && interval.getAlignment() == alignment;
        if (!aligned) {
          return null;
        }
        if (parent.getParent() == null) {
          return parent; // aligned with root group
        }
      }
      for (Iterator it = parent.getSubIntervals(); it.hasNext();) {
        LayoutInterval sub = (LayoutInterval) it.next();
        if (!sub.isEmptySpace() && sub != interval && !sub.isParentOf(interval)) {
          if (alignment == LEADING || alignment == TRAILING) {
            LayoutInterval li = LayoutUtils.getOutermostComponent(sub, dimension, alignment);
            if (LayoutInterval.isAlignedAtBorder(li, parent, alignment)
                || LayoutInterval.isPlacedAtBorder(li, parent, dimension, alignment)) { // here we have an aligned component
              alignedInterval = li;
            } else {
              continue; // not aligned subinterval
            }
          } else {
            alignedInterval = sub;
          }
          break;
        }
      }
      if (indent) {
        return alignedInterval;
      }
      interval = parent;
    } while (alignedInterval == null);
    return parent.getSubIntervalCount() > 2 ? parent : alignedInterval;
  }

  /**
   * For inclusion derived from existing position (findOutCurrentPosition) checks if it does not
   * refer to 'neighbor' being in sequential group. In such case the inclusion is changed to refer
   * to the sequence (single neighbor is supposed to be in parallel group). This may happen if the
   * sequence survived removal of the interval even with just one component remaining (because of
   * gaps around it).
   */
  private static void correctNeighborInSequence(IncludeDesc iDesc) {
    if (iDesc != null && iDesc.neighbor != null && iDesc.neighbor.getParent().isSequential()) {
      assert iDesc.parent == iDesc.neighbor.getParent().getParent();
      iDesc.parent = iDesc.neighbor.getParent();
      iDesc.neighbor = null;
    }
  }

  // -----
  // overlap analysis
  private int getDimensionSolvingOverlap(LayoutDragger.PositionDef[] positions) {
    if (dragger.isResizing(HORIZONTAL) && !dragger.isResizing(VERTICAL)) {
      return HORIZONTAL;
    }
    if (dragger.isResizing(VERTICAL)
        && !dragger.isResizing(HORIZONTAL)
        || positions[HORIZONTAL] != null
        && positions[HORIZONTAL].snapped
        && (positions[VERTICAL] == null || !positions[VERTICAL].snapped)
        || positions[VERTICAL] != null
        && !positions[VERTICAL].nextTo
        && positions[VERTICAL].snapped
        && positions[VERTICAL].interval.getParent() == null
        && !existsComponentPlacedAtBorder(
            positions[VERTICAL].interval,
            VERTICAL,
            positions[VERTICAL].alignment)) {
      return VERTICAL;
    }
    if (positions[VERTICAL] != null
        && positions[VERTICAL].nextTo
        && positions[VERTICAL].snapped
        && positions[VERTICAL].interval.getParent() == null) {
      int alignment = positions[VERTICAL].alignment;
      int[][] overlapSides =
          overlappingGapSides(dragger.getTargetRoots()[HORIZONTAL], dragger.getMovingSpace());
      if ((alignment == LEADING || alignment == TRAILING)
          && overlapSides[VERTICAL][1 - alignment] != 0
          && overlapSides[VERTICAL][alignment] == 0) {
        return VERTICAL;
      }
    }
    if ((positions[HORIZONTAL] == null || !positions[HORIZONTAL].snapped)
        && (positions[VERTICAL] == null || !positions[VERTICAL].snapped)) {
      boolean[] overlapDim =
          overlappingGapDimensions(dragger.getTargetRoots()[HORIZONTAL], dragger.getMovingSpace());
      if (overlapDim[VERTICAL] && !overlapDim[HORIZONTAL]) {
        return VERTICAL;
      }
    }
    return HORIZONTAL;
  }

  /**
   * Checks whether there is a component placed at the border of the specified interval.
   * 
   * @param interval
   *          interval to check.
   * @param dimension
   *          dimension that should be considered.
   * @param alignment
   *          alignment that should be considered.
   */
  private static boolean existsComponentPlacedAtBorder(LayoutInterval interval,
      int dimension,
      int alignment) {
    Iterator iter = interval.getSubIntervals();
    while (iter.hasNext()) {
      LayoutInterval subInterval = (LayoutInterval) iter.next();
      if (LayoutInterval.isPlacedAtBorder(interval, dimension, alignment)) {
        if (subInterval.isComponent()) {
          return true;
        } else if (subInterval.isGroup()) {
          if (existsComponentPlacedAtBorder(subInterval, dimension, alignment)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Fills the given list by components that overlap with the <code>region</code>.
   * 
   * @param overlaps
   *          list that should be filled by overlapping components.
   * @param group
   *          layout group that is scanned by this method.
   * @param region
   *          region to check.
   */
  private static void fillOverlappingComponents(List<LayoutComponent> overlaps,
      LayoutInterval group,
      LayoutRegion region) {
    Iterator iter = group.getSubIntervals();
    while (iter.hasNext()) {
      LayoutInterval subInterval = (LayoutInterval) iter.next();
      if (subInterval.isGroup()) {
        fillOverlappingComponents(overlaps, subInterval, region);
      } else if (subInterval.isComponent()) {
        LayoutComponent component = subInterval.getComponent();
        LayoutRegion compRegion = subInterval.getCurrentSpace();
        if (LayoutRegion.overlap(compRegion, region, HORIZONTAL, 0)
            && LayoutRegion.overlap(compRegion, region, VERTICAL, 0)) {
          overlaps.add(component);
        }
      }
    }
  }

  // Helper method for getDimensionSolvingOverlap() method
  private static boolean[] overlappingGapDimensions(LayoutInterval layoutRoot, LayoutRegion region) {
    boolean[] result = new boolean[2];
    int[][] overlapSides = overlappingGapSides(layoutRoot, region);
    for (int i = 0; i < DIM_COUNT; i++) {
      result[i] = overlapSides[i][0] == 1 && overlapSides[i][1] == 1;
    }
    return result;
  }

  // Helper method for getDimensionSolvingOverlap() method
  private static int[][] overlappingGapSides(LayoutInterval layoutRoot, LayoutRegion region) {
    int[][] overlapSides = new int[][]{{0, 0}, {0, 0}};
    List<LayoutComponent> overlaps = new LinkedList<LayoutComponent>();
    //        LayoutInterval layoutRoot = LayoutInterval.getRoot(positions[HORIZONTAL].interval);
    fillOverlappingComponents(overlaps, layoutRoot, region);
    Iterator<LayoutComponent> iter = overlaps.iterator();
    while (iter.hasNext()) {
      LayoutComponent component = iter.next();
      LayoutRegion compRegion = component.getLayoutInterval(HORIZONTAL).getCurrentSpace();
      for (int i = 0; i < DIM_COUNT; i++) {
        int[] edges = overlappingSides(compRegion, region, i);
        for (int j = 0; j < 2; j++) {
          if (edges[j] == 1) {
            overlapSides[i][j] = 1;
          } else if (edges[j] == -1) {
            if (overlapSides[i][j] == -1) {
              overlapSides[i][j] = 1;
            } else if (overlapSides[i][j] == 0) {
              overlapSides[i][j] = -1;
            }
          }
        }
      }
    }
    return overlapSides;
  }

  // Helper method for overlappingGapSides() method
  private static int[] overlappingSides(LayoutRegion compRegion, LayoutRegion region, int dimension) {
    int[] sides = new int[2];
    int compLeading = compRegion.positions[dimension][LEADING];
    int compTrailing = compRegion.positions[dimension][TRAILING];
    int regLeading = region.positions[dimension][LEADING];
    int regTrailing = region.positions[dimension][TRAILING];
    if (regLeading < compTrailing && compTrailing < regTrailing) {
      sides[0] = 1;
    }
    if (regLeading < compLeading && compLeading < regTrailing) {
      sides[1] = 1;
    }
    if (sides[0] == 1 && sides[1] == 1) {
      sides[0] = sides[1] = -1;
    }
    return sides;
  }

  // -----
  // the following methods work in context of adding to actual dimension
  private boolean checkResizing() {
    LayoutInterval interval = addingIntervals[dimension];
    int resizingEdge = dragger.getResizingEdge(dimension);
    int fixedEdge = resizingEdge ^ 1;
    LayoutDragger.PositionDef newPos = newPositions[dimension];
    boolean resizing = false;
    if (newPos != null && newPos.snapped && newPos.interval != null) {
      int align1, align2;
      if (newPos.interval.isParentOf(interval)) {
        LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
        if (!LayoutRegion.pointInside(
            dragger.getMovingSpace(),
            resizingEdge,
            parent.getCurrentSpace(),
            dimension)) {
          parent = newPos.interval;
        }
        align1 = LayoutInterval.getEffectiveAlignmentInParent(interval, parent, fixedEdge);
        align2 = resizingEdge;
      } else {
        LayoutInterval parent = LayoutInterval.getCommonParent(interval, newPos.interval);
        align1 = LayoutInterval.getEffectiveAlignmentInParent(interval, parent, fixedEdge);
        align2 =
            newPos.nextTo ? LayoutInterval.getEffectiveAlignmentInParent(
                newPos.interval,
                parent,
                newPos.alignment ^ 1) : resizingEdge;
      }
      if (align1 != align2
          && (align1 == LEADING || align1 == TRAILING)
          && (align2 == LEADING || align2 == TRAILING)) {
        resizing = true;
      }
    }
    // [perhaps we should consider also potential resizability of the component,
    //  not only on resizing operation - the condition should be:
    //  isComponentResizable(interval.getComponent(), dimension)  ]
    return resizing;
/*        if (pos2 != null) {
            resizing = pos1.snapped && pos2.snapped;
        }
        else if (pos1.snapped) {
            LayoutInterval parent = interval.getParent();
            if (parent == null || parent.isParallel()) {
                resizing = false;
                // cannot decide this as we can't be sure about the actual visual position of the group
                // [unless LayoutDragger provides two positions also for moving,
                //  the resizing flag is always reset when the interval is moved]
            }
            else { // in sequence
                int align = pos1.alignment ^ 1;
                LayoutInterval gap = LayoutInterval.getNeighbor(interval, align, false, true, true);
                resizing = gap != null && gap.isDefaultPadding() && !LayoutInterval.canResize(gap);
                // [not catching situation when touching the root without gap]
            }
        }
        else {
            resizing = false;
        } */
  }

  /**
   * Adds aligned with an interval to existing group, or creates new. (Now used only to a limited
   * extent for closed groups only.)
   */
  private void addSimplyAligned() {
    int alignment = aEdge;
    assert alignment == CENTER || alignment == BASELINE;
    layoutModel.setIntervalAlignment(addingInterval, alignment);
    if (addingInterval.getParent() != null) {
      // hack: resized interval in center/baseline has not been removed
      return;
    }
    if (aSnappedParallel.isParallel() && aSnappedParallel.getGroupAlignment() == alignment) {
      layoutModel.addInterval(addingInterval, aSnappedParallel, -1);
      return;
    }
    LayoutInterval parent = aSnappedParallel.getParent();
    if (parent.isParallel() && parent.getGroupAlignment() == alignment) {
      layoutModel.addInterval(addingInterval, parent, -1);
      return;
    }
    int alignIndex = layoutModel.removeInterval(aSnappedParallel);
    LayoutInterval subGroup = new LayoutInterval(PARALLEL);
    subGroup.setGroupAlignment(alignment);
    if (parent.isParallel()) {
      subGroup.setAlignment(aSnappedParallel.getAlignment());
    }
    layoutModel.setIntervalAlignment(aSnappedParallel, alignment);
    layoutModel.addInterval(aSnappedParallel, subGroup, -1);
    layoutModel.addInterval(addingInterval, subGroup, -1);
    layoutModel.addInterval(subGroup, parent, alignIndex);
  }

  void addInterval(IncludeDesc iDesc1, IncludeDesc iDesc2) {
    addToGroup(iDesc1, iDesc2, true);
    // align in parallel if required
    if (iDesc1.snappedParallel != null || iDesc2 != null && iDesc2.snappedParallel != null) {
      if (iDesc2 != null && iDesc2.snappedParallel != null) {
        alignInParallel(addingInterval, iDesc2.snappedParallel, iDesc2.alignment);
      }
      if (iDesc1.snappedParallel != null) {
        alignInParallel(addingInterval, iDesc1.snappedParallel, iDesc1.alignment);
      }
    }
    checkParallelResizing(addingInterval, iDesc1, iDesc2);
    // post processing
    LayoutInterval parent = addingInterval.getParent();
    int accAlign = DEFAULT;
    if (parent.isSequential()) {
      int tryAlign = parent.getAlignment() != TRAILING ? TRAILING : LEADING;
      if (LayoutInterval.getDirectNeighbor(addingInterval, tryAlign, true) == null) {
        accAlign = tryAlign;
      } else {
        tryAlign ^= 1;
        if (LayoutInterval.getDirectNeighbor(addingInterval, tryAlign, true) == null) {
          accAlign = tryAlign;
        }
      }
    } else {
      accAlign = addingInterval.getAlignment() ^ 1;
    }
    if (accAlign != DEFAULT) {
      accommodateOutPosition(addingInterval, accAlign); // adapt size of parent/neighbor
    }
    if (dragger.isResizing(dimension) && LayoutInterval.wantResize(addingInterval)) {
      operations.suppressResizingOfSurroundingGaps(addingInterval);
    }
    // optimize gaps (may eliminate the sequence, also removes supporting gap in container)
    operations.optimizeGaps(LayoutInterval.getFirstParent(addingInterval, PARALLEL), dimension);
    parent = addingInterval.getParent();
    if (parent.isSequential()) {// && !alignedInParallel)
      int nonEmptyCount = LayoutInterval.getCount(parent, LayoutRegion.ALL_POINTS, true);
      if (nonEmptyCount > 1 && dimension == HORIZONTAL) {
        // check whether the added interval could not be rather placed
        // in a neighbor parallel group
        operations.moveInsideSequential(parent, dimension);
      }
    }
    // avoid unnecessary parallel group nesting
    operations.mergeParallelGroups(LayoutInterval.getFirstParent(addingInterval, PARALLEL));
  }

  private void addToGroup(IncludeDesc iDesc1, IncludeDesc iDesc2, boolean definite) {
    assert iDesc2 == null
        || iDesc1.parent == iDesc2.parent
        && iDesc1.newSubGroup == iDesc2.newSubGroup
        && iDesc1.neighbor == iDesc2.neighbor;
    LayoutInterval parent = iDesc1.parent;
    LayoutInterval seq = null;
    int index = 0;
    if (parent.isSequential()) {
      if (iDesc1.newSubGroup) {
        LayoutRegion space = addingSpace;
        //                if (dimension == VERTICAL) { // count in a margin in vertical direction
        //                    // [because analyzeAdding uses it - maybe we should get rid of it completely]
        //                    space = new LayoutRegion(space);
        //                    space.reshape(VERTICAL, LEADING, -4);
        //                    space.reshape(VERTICAL, TRAILING, 4);
        //                }
        LayoutInterval subgroup = extractParallelSequence(parent, space, false, iDesc1.alignment); // dimension == VERTICAL
        if (subgroup != null) { // just for robustness - null only if something got screwed up
          seq = new LayoutInterval(SEQUENTIAL);
          parent = subgroup;
        }
      }
      if (seq == null) {
        seq = parent;
        parent = seq.getParent();
        index = iDesc1.index;
      }
    } else { // parallel parent
      LayoutInterval neighbor = iDesc1.neighbor;
      if (neighbor != null) {
        assert neighbor.getParent() == parent;
        seq = new LayoutInterval(SEQUENTIAL);
        layoutModel.addInterval(seq, parent, layoutModel.removeInterval(neighbor));
        seq.setAlignment(neighbor.getAlignment());
        layoutModel.setIntervalAlignment(neighbor, DEFAULT);
        layoutModel.addInterval(neighbor, seq, 0);
        index = iDesc1.index;
      } else {
        seq = new LayoutInterval(SEQUENTIAL);
        seq.setAlignment(iDesc1.alignment);
      }
    }
    assert iDesc1.alignment >= 0 || iDesc2 == null;
    assert iDesc2 == null || iDesc2.alignment == (iDesc1.alignment ^ 1);
    assert parent.isParallel();
    LayoutInterval[] neighbors = new LayoutInterval[2]; // LEADING, TRAILING
    LayoutInterval[] gaps = new LayoutInterval[2]; // LEADING, TRAILING
    LayoutInterval originalGap = null;
    int[] centerDst = new int[2]; // LEADING, TRAILING
    // find the neighbors for the adding interval
    int count = seq.getSubIntervalCount();
    if (index > count) {
      index = count;
    }
    for (int i = LEADING; i <= TRAILING; i++) {
      int idx1 = i == LEADING ? index - 1 : index;
      int idx2 = i == LEADING ? index - 2 : index + 1;
      if (idx1 >= 0 && idx1 < count) {
        LayoutInterval li = seq.getSubInterval(idx1);
        if (li.isEmptySpace()) {
          originalGap = li;
          if (idx2 >= 0 && idx2 < count) {
            neighbors[i] = seq.getSubInterval(idx2);
          }
        } else {
          neighbors[i] = li;
        }
      }
      if (iDesc1.alignment < 0) { // no alignment known
        centerDst[i] =
            addingSpace.positions[dimension][CENTER]
                - (neighbors[i] != null ? getPerceivedNeighborPosition(
                    neighbors[i],
                    addingSpace,
                    dimension,
                    i ^ 1) : getPerceivedParentPosition(seq, parent, addingSpace, dimension, i));
        if (i == TRAILING) {
          centerDst[i] *= -1;
        }
      }
    }
    // compute leading and trailing gaps
    int edges = 2;
    for (int i = LEADING; edges > 0; i ^= 1, edges--) {
      gaps[i] = null;
      LayoutInterval outerNeighbor =
          neighbors[i] == null ? LayoutInterval.getNeighbor(parent, i, false, true, false) : null;
      IncludeDesc iiDesc = iDesc1.alignment < 0 || iDesc1.alignment == i ? iDesc1 : iDesc2;
      if (neighbors[i] == null && iiDesc != null) { // at the start/end of the sequence
        if (iiDesc.snappedNextTo != null
            && outerNeighbor != null
            && LayoutInterval.isDefaultPadding(outerNeighbor)) { // the padding is outside of the parent already
          continue;
        }
        if (iiDesc.snappedParallel != null
            && (!seq.isParentOf(iiDesc.snappedParallel) || originalGap == null)) { // starting/ending edge aligned in parallel - does not need a gap
          continue;
        }
      }
      boolean aligned;
      if (iDesc1.alignment < 0) { // no specific alignment - decide based on distance
        aligned =
            centerDst[i] < centerDst[i ^ 1] || centerDst[i] == centerDst[i ^ 1] && i == LEADING;
      } else if (iDesc2 != null) { // both positions defined
        aligned =
            iiDesc.fixedPosition
                || i == LEADING
                && originalLPosFixed
                || i == TRAILING
                && originalTPosFixed;
      } else { // single position only (either next to or parallel)
        if (iDesc1.snappedParallel == null || !seq.isParentOf(iDesc1.snappedParallel)) {
          aligned = i == iDesc1.alignment;
        } else {
          // special case - aligning with interval in the same sequence - to subst. its position
          aligned = i == (iDesc1.alignment ^ 1);
        }
      }
      boolean minorGap = false;
      if (!aligned && neighbors[i] == null && originalGap == null) {
        IncludeDesc otherDesc = iiDesc == iDesc1 ? iDesc2 : iDesc1;
        LayoutInterval parallel = otherDesc != null ? otherDesc.snappedParallel : null;
        // make sure new sequence has appropriate explicit alignment
        if (parallel == null && seq.getSubIntervalCount() == 0 && seq.getAlignment() != (i ^ 1)) {
          layoutModel.setIntervalAlignment(seq, i ^ 1);
        }
        if (outerNeighbor != null && outerNeighbor.isEmptySpace()) {
          continue; // unaligned ending gap not needed - there's a gap outside the parent
        } else { // minor gap if it does not need to define the parent size
          minorGap =
              parallel != null
                  && parallel.getParent() != null
                  || parent.getParent() != null
                  && LayoutInterval.getCount(parent, i ^ 1, true) > 0;
        }
      }
      boolean fixedGap = aligned;
      if (!fixedGap) {
        if (minorGap || LayoutInterval.wantResize(addingInterval)) {
          fixedGap = true;
        } else if (originalGap != null && !LayoutInterval.canResize(originalGap)) {
          IncludeDesc otherDesc = iiDesc == iDesc1 ? iDesc2 : iDesc1;
          if (otherDesc == null
              || otherDesc.snappedParallel == null
              || neighbors[i] == null
              || LayoutInterval.getEffectiveAlignment(neighbors[i], i ^ 1) == (i ^ 1)) {
            fixedGap = true;
          }
        } else if (originalGap == null) {
          if (neighbors[i] != null
              && LayoutInterval.getEffectiveAlignment(neighbors[i], i ^ 1) == (i ^ 1)
              || LayoutInterval.wantResize(seq)) {
            //                         || (neighbors[i] == null && !LayoutInterval.contentWantResize(parent)))
            fixedGap = true;
          }
        }
      }
      LayoutInterval gap = new LayoutInterval(SINGLE);
      if (!minorGap) {
        if (iiDesc == null || iiDesc.snappedNextTo == null) {
          // the gap possibly needs an explicit size
          LayoutRegion space =
              iiDesc != null && iiDesc.snappedParallel != null
                  ? iiDesc.snappedParallel.getCurrentSpace()
                  : addingSpace;
          int distance =
              neighbors[i] != null ? LayoutRegion.distance(
                  neighbors[i].getCurrentSpace(),
                  space,
                  dimension,
                  i ^ 1,
                  i) : LayoutRegion.distance(parent.getCurrentSpace(), space, dimension, i, i);
          if (i == TRAILING) {
            distance *= -1;
          }
          if (distance > 0) {
            int pad =
                neighbors[i] != null
                    || LayoutInterval.getNeighbor(parent, i, false, true, false) == null
                    ? determineExpectingPadding(addingInterval, neighbors[i], seq, i)
                    : Short.MIN_VALUE; // has no neighbor, but is not related to container border
            if (distance > pad || fixedGap && distance != pad) {
              gap.setPreferredSize(distance);
              if (fixedGap) {
                gap.setMinimumSize(USE_PREFERRED_SIZE);
                gap.setMaximumSize(USE_PREFERRED_SIZE);
              }
            }
          }
        } else {
          gap.setPaddingType(iiDesc.paddingType);
        }
      }
      if (!fixedGap) {
        gap.setMaximumSize(Short.MAX_VALUE);
        // resizing gap may close the open parent group
        IncludeDesc otherDesc = iiDesc == iDesc1 ? iDesc2 : iDesc1;
        if (definite
            && neighbors[i] != null
            && parent.getParent() != null
            && (otherDesc == null || otherDesc.alignment == DEFAULT)
            && !isSignificantGroupEdge(seq, i ^ 1)) { // the aligned edge needs to be anchored out of parent (independently)
          parent = separateSequence(seq, i ^ 1);
          if (i == TRAILING) {
            edges++; // we need to revisit the LEADING gap
          }
        }
      }
      gaps[i] = gap;
    }
    if (seq.getParent() == null) { // newly created sequence
      assert seq.getSubIntervalCount() == 0;
      if (gaps[LEADING] == null && gaps[TRAILING] == null) { // after all, the sequence is not needed
        layoutModel.setIntervalAlignment(addingInterval, seq.getAlignment());
        layoutModel.addInterval(addingInterval, parent, -1);
        return;
      } else {
        layoutModel.addInterval(seq, parent, -1);
      }
    }
    // aligning in parallel with interval in the same sequence was resolved
    // by substituting its position
    if (iDesc1.snappedParallel != null && seq.isParentOf(iDesc1.snappedParallel)) {
      iDesc1.snappedParallel = null; // set to null not to try alignInParallel later
    }
    // finally add the surrounding gaps and the interval
    if (originalGap != null) {
      index = layoutModel.removeInterval(originalGap);
    } else if (neighbors[TRAILING] != null) {
      index = seq.indexOf(neighbors[TRAILING]);
    } else if (neighbors[LEADING] != null) {
      index = seq.getSubIntervalCount();
    } else {
      index = 0;
    }
    if (gaps[LEADING] != null) {
      layoutModel.addInterval(gaps[LEADING], seq, index++);
    }
    layoutModel.setIntervalAlignment(addingInterval, DEFAULT);
    layoutModel.addInterval(addingInterval, seq, index++);
    if (gaps[TRAILING] != null) {
      layoutModel.addInterval(gaps[TRAILING], seq, index);
    }
  }

  private LayoutInterval extractParallelSequence(LayoutInterval seq,
      LayoutRegion space,
      boolean close,
      int alignment) {
    int count = seq.getSubIntervalCount();
    int startIndex = 0;
    int endIndex = count - 1;
    int startPos = seq.getCurrentSpace().positions[dimension][LEADING];
    int endPos = seq.getCurrentSpace().positions[dimension][TRAILING];
    int point = alignment < 0 ? CENTER : alignment;
    for (int i = 0; i < count; i++) {
      LayoutInterval li = seq.getSubInterval(i);
      if (li.isEmptySpace()) {
        continue;
      }
      LayoutRegion subSpace = li.getCurrentSpace();
      boolean forcedParallel = !solveOverlap && LayoutUtils.contentOverlap(space, li, dimension);
      if (!forcedParallel && LayoutUtils.contentOverlap(space, li, dimension ^ 1)) { // orthogonal overlap
        // this interval makes a hard boundary
        if (getAddDirection(space, subSpace, dimension, point) == LEADING) {
          // given interval is positioned before this subinterval (trailing boundary)
          endIndex = i - 1;
          endPos = subSpace.positions[dimension][LEADING];
          break;
        } else { // given interval points behind this one (leading boundary)
          startIndex = i + 1;
          startPos = subSpace.positions[dimension][TRAILING];
        }
      } else if (close) { // go for smallest parallel part possible
        int[] detPos = space.positions[dimension];
        int[] subPos = subSpace.positions[dimension];
        if (detPos[LEADING] >= subPos[TRAILING]) {
          startIndex = i + 1;
          startPos = subPos[TRAILING];
        } else if (detPos[LEADING] >= subPos[LEADING]) {
          startIndex = i;
          startPos = subPos[LEADING];
        } else if (detPos[TRAILING] <= subPos[TRAILING]) {
          if (detPos[TRAILING] > subPos[LEADING]) {
            endIndex = i;
            endPos = subPos[TRAILING];
            break;
          } else { // detPos[TRAILING] <= subPos[LEADING]
            endIndex = i - 1;
            endPos = subPos[LEADING];
            break;
          }
        }
      }
    }
    if (startIndex > endIndex) {
      return null; // no part of the sequence can be parallel to the given space
    }
    if (startIndex == 0 && endIndex == count - 1) { // whole sequence is parallel
      return seq.getParent();
    }
    LayoutInterval group = new LayoutInterval(PARALLEL);
    //        int effAlign1 = getEffectiveAlignment(seq.getSubInterval(startIndex));
    //        int effAlign2 = getEffectiveAlignment(seq.getSubInterval(endIndex));
    //        int groupAlign = (effAlign1 == effAlign2 || effAlign2 < 0) ? effAlign1 : effAlign2;
    if (alignment != DEFAULT) {
      group.setGroupAlignment(/*groupAlign == LEADING || groupAlign == TRAILING ?
                              groupAlign :*/alignment);
    }
    if (startIndex == endIndex) {
      LayoutInterval li = layoutModel.removeInterval(seq, startIndex);
      layoutModel.addInterval(li, group, 0);
    } else {
      LayoutInterval interSeq = new LayoutInterval(SEQUENTIAL);
      group.add(interSeq, 0);
      int i = startIndex;
      while (i <= endIndex) {
        LayoutInterval li = layoutModel.removeInterval(seq, i);
        endIndex--;
        layoutModel.addInterval(li, interSeq, -1);
      }
    }
    layoutModel.addInterval(group, seq, startIndex);
    group.getCurrentSpace().set(dimension, startPos, endPos);
    return group;
  }

  private static int getPerceivedParentPosition(LayoutInterval interval,
      LayoutInterval parent,
      LayoutRegion space,
      int dimension,
      int alignment) {
    int position = Integer.MIN_VALUE;
    do {
      if (parent.isSequential()) {
        interval = parent;
        parent = interval.getParent();
      }
      LayoutInterval neighbor = null;
      while (neighbor == null && parent.getParent() != null) {
        boolean significantEdge =
            interval.getParent() != null
                ? isSignificantGroupEdge(interval, alignment)
                : LayoutInterval.isClosedGroup(parent, alignment);
        if (significantEdge) {
          break;
        }
        neighbor = LayoutInterval.getDirectNeighbor(parent, alignment, true);
        if (neighbor == null) {
          interval = parent;
          parent = interval.getParent();
          if (parent.isSequential()) {
            interval = parent;
            parent = interval.getParent();
          }
        }
      }
      if (neighbor == null) {
        position = parent.getCurrentSpace().positions[dimension][alignment];
      } else { // look for neighbor of the parent that has orthogonal overlap with the given space
        do {
          position = getPerceivedNeighborPosition(neighbor, space, dimension, alignment ^ 1);
          if (position != Integer.MIN_VALUE) {
            break;
          } else {
            // otherwise the space can "go through" this neighbor
            neighbor = LayoutInterval.getDirectNeighbor(neighbor, alignment, true);
          }
        } while (neighbor != null);
        if (neighbor == null) {
          interval = parent;
          parent = interval.getParent();
        }
      }
    } while (position == Integer.MIN_VALUE);
    return position;
  }

  private static boolean isSignificantGroupEdge(LayoutInterval interval, int alignment) {
    LayoutInterval group = interval.getParent();
    assert group.isParallel();
    if (interval.getAlignment() == alignment || LayoutInterval.wantResize(interval)) {
      return true;
    }
    if (!LayoutInterval.isClosedGroup(group, alignment)) {
      return false;
    }
    if (!LayoutInterval.isExplicitlyClosedGroup(group)) { // naturally closed group
      LayoutInterval neighborGap = LayoutInterval.getNeighbor(group, alignment, false, true, true);
      if (neighborGap != null && LayoutInterval.isDefaultPadding(neighborGap)) {
        // default padding means the group can be considered open at this edge
        return false;
      }
    }
    return true;
  }

  private static int getPerceivedNeighborPosition(LayoutInterval neighbor,
      LayoutRegion space,
      int dimension,
      int alignment) {
    assert !neighbor.isEmptySpace();
    if (neighbor.isComponent()) {
      //            || (neighbor.isParallel() && LayoutInterval.isClosedGroup(neighbor, alignment)
      //                && !LayoutRegion.overlap(neighbor.getCurrentSpace(), space, dimension, 0))
      return neighbor.getCurrentSpace().positions[dimension][alignment];
    }
    int neighborPos = Integer.MIN_VALUE;
    int n = neighbor.getSubIntervalCount();
    int i, d;
    if (neighbor.isParallel() || alignment == LEADING) {
      d = 1;
      i = 0;
    } else {
      d = -1;
      i = n - 1;
    }
    while (i >= 0 && i < n) {
      LayoutInterval sub = neighbor.getSubInterval(i);
      i += d;
      if (sub.isEmptySpace()
          || sub.isComponent()
          && !LayoutRegion.overlap(space, sub.getCurrentSpace(), dimension ^ 1, 0)) {
        continue;
      }
      int pos = getPerceivedNeighborPosition(sub, space, dimension, alignment);
      if (pos != Integer.MIN_VALUE) {
        if (neighbor.isSequential()) {
          neighborPos = pos;
          break;
        } else if (neighborPos == Integer.MIN_VALUE || pos * d < neighborPos * d) {
          neighborPos = pos;
          // continue, there can still be a closer position
        }
      }
    }
    return neighborPos;
  }

  private LayoutInterval separateSequence(LayoutInterval seq, int alignment) {
    LayoutInterval parentPar = seq.getParent();
    assert parentPar.isParallel();
    while (!parentPar.getParent().isSequential()) {
      parentPar = parentPar.getParent();
    }
    LayoutInterval parentSeq = parentPar.getParent(); // sequential
    int d = alignment == LEADING ? -1 : 1;
    int n = parentSeq.getSubIntervalCount();
    int end = parentSeq.indexOf(parentPar) + d;
    while (end >= 0 && end < n) {
      LayoutInterval sub = parentSeq.getSubInterval(end);
      if (!sub.isEmptySpace()) {
        //                LayoutRegion subSpace = sub.getCurrentSpace();
        //                if (sub.isParallel()
        //                    && subSpace.positions[dimension][alignment^1]*d > addingSpace.positions[dimension][alignment]*d
        //                    && LayoutInterval.isClosedGroup(sub, alignment))
        if (LayoutUtils.contentOverlap(addingSpace, sub, dimension ^ 1)) {
          break;
        }
      }
      end += d;
    }
    int endPos =
        end >= 0 && end < n
            ? parentSeq.getSubInterval(end).getCurrentSpace().positions[dimension][alignment ^ 1]
            : parentSeq.getParent().getCurrentSpace().positions[dimension][alignment];
    end -= d;
    operations.parallelizeWithParentSequence(seq, end, dimension);
    parentPar = seq.getParent();
    parentPar.getCurrentSpace().positions[dimension][alignment] = endPos;
    return parentPar;
  }

  /**
   * When an interval is added or resized out of current boundaries of its parent, this method tries
   * to accommodate the size increment in the parent (and its parents). It acts according to the
   * current visual position of the interval - how it exceeds the current parent border. In the
   * simplest form the method tries to shorten the nearest gap in the parent sequence.
   */
  private void accommodateOutPosition(LayoutInterval interval, /*int dimension,*/int alignment/*, boolean snapped*/) {
    if (alignment == CENTER || alignment == BASELINE) {
      return; // [but should consider these too...]
    }
    int pos = interval.getCurrentSpace().positions[dimension][alignment];
    assert pos != LayoutRegion.UNKNOWN;
    int sizeIncrement = Integer.MIN_VALUE;
    int d = alignment == LEADING ? -1 : 1;
    int[] groupPos = null;
    LayoutInterval parent = interval.getParent();
    LayoutInterval prev = null;
    do {
      if (parent.isSequential()) {
        if (sizeIncrement > 0) {
          int accommodated = accommodateSizeInSequence(interval, prev, sizeIncrement, alignment);
          sizeIncrement -= accommodated;
          if (groupPos != null) {
            groupPos[alignment] += accommodated * d;
          }
        }
        LayoutInterval neighbor = LayoutInterval.getDirectNeighbor(interval, alignment, false);
        if (neighbor != null && (!neighbor.isEmptySpace() || LayoutInterval.canResize(neighbor))) {
          // not a border interval in the sequence, can't go up
          return;
        }
        prev = interval;
      } else {
        groupPos = parent.getCurrentSpace().positions[dimension];
        if (groupPos[alignment] != LayoutRegion.UNKNOWN) {
          sizeIncrement = (pos - groupPos[alignment]) * d;
          if (sizeIncrement > 0) {
            int subPos[] = interval.getCurrentSpace().positions[dimension];
            if (!interval.getCurrentSpace().isSet(dimension)
                || subPos[alignment] * d < groupPos[alignment] * d) { // update space of subgroup according to parent (needed if subgroup is also parallel)
              subPos[alignment] = groupPos[alignment];
            }
          }
        } else {
          groupPos = null;
        }
        if (!interval.isSequential() || prev == null) {
          prev = interval;
        }
      }
      interval = parent;
      parent = interval.getParent();
    } while ((sizeIncrement > 0 || sizeIncrement == Integer.MIN_VALUE)
        && parent != null
        && (!parent.isParallel() || interval.getAlignment() != alignment));
    // can't accommodate at the aligned side [but could probably turn to other side - update 'pos', etc]
  }

  private int accommodateSizeInSequence(LayoutInterval interval,
      LayoutInterval lower,
      int sizeIncrement,
      int alignment) {
    LayoutInterval parent = interval.getParent();
    assert parent.isSequential();
    LayoutRegion space = lower.getCurrentSpace();
    int increment = sizeIncrement;
    int pos = interval.getCurrentSpace().positions[dimension][alignment];
    int outPos = parent.getParent().getCurrentSpace().positions[dimension][alignment];
    boolean parallel = false;
    int d = alignment == LEADING ? -1 : 1;
    int start = parent.indexOf(interval);
    int end = lower.isComponent() ? start : -1;
    int n = parent.getSubIntervalCount();
    for (int i = start + d; i >= 0 && i < n; i += d) {
      LayoutInterval li = parent.getSubInterval(i);
      if (end != -1) { // consider parallel expansion of the sequence out
                       // of its parent (similar to what separateSequence does)
        int endPos = Integer.MIN_VALUE;
        if (!li.isEmptySpace()) {
          if (LayoutUtils.contentOverlap(space, li, dimension ^ 1)) {
            if (end != start) {
              end = i - d;
              endPos = li.getCurrentSpace().positions[dimension][alignment ^ 1];
            } else {
              end = -1; // there was only a gap before
            }
          } else { // no orthogonal overlap, count this in
            end = i;
            if (!parallel && LayoutUtils.contentOverlap(space, li, dimension)) {
              parallel = true;
            }
          }
        }
        if ((i == 0 || i + d == n) && endPos == Integer.MIN_VALUE && end != -1) { // last gap or non-overlapping
          if (end != start && (parallel || dimension == HORIZONTAL)) {
            end = i;
            endPos = outPos;
          } else {
            end = -1; // only gap or not in parallel with anything (in vertical dimension)
          }
        }
        if (endPos != Integer.MIN_VALUE) {
          LayoutInterval toPar = lower.getParent().isSequential() ? lower.getParent() : lower;
          LayoutInterval endGap = LayoutInterval.getDirectNeighbor(lower, alignment, false);
          if (endGap == null && !LayoutInterval.isAlignedAtBorder(toPar, alignment)) {
            endGap = new LayoutInterval(SINGLE);
            if (!toPar.isSequential()) {
              toPar = new LayoutInterval(SEQUENTIAL);
              layoutModel.addInterval(toPar, lower.getParent(), layoutModel.removeInterval(lower));
              layoutModel.setIntervalAlignment(toPar, lower.getRawAlignment());
              layoutModel.setIntervalAlignment(lower, DEFAULT);
              layoutModel.addInterval(lower, toPar, 0);
            }
            layoutModel.addInterval(endGap, toPar, alignment == LEADING ? 0 : -1);
          } else {
            assert endGap == null || endGap.isEmptySpace();
          }
          operations.parallelizeWithParentSequence(toPar, end, dimension);
          if (alignment == TRAILING) {
            i -= n - parent.getSubIntervalCount();
          }
          n = parent.getSubIntervalCount(); // adjust count
          end = -1; // don't try anymore
          increment -= Math.abs(endPos - pos);
          if (increment < 0) {
            increment = 0;
          }
          continue;
        } else if (end == -1) { // no parallel expansion possible
          i = start; // restart
          continue;
        }
      }
      // otherwise look for a gap to reduce
      else if (li.isEmptySpace() && li.getPreferredSize() != NOT_EXPLICITLY_DEFINED) {
        int pad = determinePadding(interval, li.getPaddingType(), dimension, alignment);
        int currentSize = LayoutInterval.getIntervalCurrentSize(li, dimension);
        int size = currentSize - increment;
        if (size <= pad) {
          size = NOT_EXPLICITLY_DEFINED;
          increment -= currentSize - pad;
        } else {
          increment = 0;
        }
        operations.resizeInterval(li, size);
        if (LayoutInterval.wantResize(li) && LayoutInterval.wantResize(interval)) {
          // cancel gap resizing if the neighbor is also resizing
          layoutModel.setIntervalSize(
              li,
              li.getMinimumSize(),
              li.getPreferredSize(),
              USE_PREFERRED_SIZE);
        }
        break;
      } else {
        interval = li;
      }
    }
    return sizeIncrement - increment;
  }

  /**
   * This method aligns an interval (just simply added to the layout - so it is already placed
   * correctly where it should appear) in parallel with another interval.
   * 
   * @return parallel group with aligned intervals if some aligning changes happened, null if
   *         addingInterval has already been aligned or could not be aligned
   */
  private LayoutInterval alignInParallel(LayoutInterval interval,
      LayoutInterval toAlignWith,
      int alignment) {
    assert alignment == LEADING || alignment == TRAILING;
    if (toAlignWith.isParentOf(interval) // already aligned to parent
        || interval.isParentOf(toAlignWith)) // can't align with own subinterval
    { // contained intervals can't be aligned
      return null;
    } else {
      LayoutInterval commonParent = LayoutInterval.getCommonParent(interval, toAlignWith);
      if (commonParent == null || commonParent.isSequential()) {
        return null;
      }
    }
    // if not in same parallel group try to substitute interval with parent
    boolean resizing = LayoutInterval.wantResize(interval);
    LayoutInterval aligning = interval; // may be substituted with parent
    LayoutInterval parParent = LayoutInterval.getFirstParent(interval, PARALLEL);
    while (!parParent.isParentOf(toAlignWith)) {
      if (LayoutInterval.isAlignedAtBorder(aligning, parParent, alignment)) { // substitute with parent
        //                // make sure parent space is up-to-date
        //                parParent.getCurrentSpace().positions[dimension][alignment]
        //                        = aligning.getCurrentSpace().positions[dimension][alignment];
        // allow parent resizing if substituting for resizing interval
        if (resizing && !LayoutInterval.canResize(parParent)) {
          operations.enableGroupResizing(parParent);
        }
        aligning = parParent;
        parParent = LayoutInterval.getFirstParent(aligning, PARALLEL);
      } else {
        parParent = null;
      }
      if (parParent == null) {
        return null; // can't align with interval from different branch
      }
    }
    boolean resizingOp = dragger.isResizing(dimension);
    // hack: remove aligning interval temporarily not to influence next analysis
    LayoutInterval tempRemoved = aligning;
    while (tempRemoved.getParent() != parParent) {
      tempRemoved = tempRemoved.getParent();
    }
    int removedIndex = parParent.remove(tempRemoved);
    // check if we shouldn't rather align with a whole group (parent of toAlignWith)
    boolean alignWithParent = false;
    LayoutInterval alignParent;
    do {
      alignParent = LayoutInterval.getFirstParent(toAlignWith, PARALLEL);
      if (alignParent == null) {
        parParent.add(tempRemoved, removedIndex); // add back temporarily removed
        return null; // aligning with parent (the interval must be already aligned)
      }
      if (canSubstAlignWithParent(toAlignWith, dimension, alignment, resizingOp)) {
        // toAlignWith is at border so we can perhaps use the parent instead
        if (alignParent == parParent) {
          if (LayoutInterval.getNeighbor(aligning, alignment, false, true, false) == null) {
            alignWithParent = true;
          }
        } else {
          toAlignWith = alignParent;
        }
      }
    } while (toAlignWith == alignParent);
    parParent.add(tempRemoved, removedIndex); // add back temporarily removed
    if (alignParent != parParent) {
      return null; // can't align (toAlignWith is too deep)
    }
    if (aligning != interval) {
      if (!LayoutInterval.isAlignedAtBorder(toAlignWith, alignment)) {
        // may have problems with S-layout
        int dst =
            LayoutRegion.distance(
                aligning.getCurrentSpace(),
                toAlignWith.getCurrentSpace(),
                dimension,
                alignment,
                alignment) * (alignment == TRAILING ? -1 : 1);
        if (dst > 0) { // try to eliminate effect of avoiding S-layout
          // need to exclude 'interval' - remove it temporarily
          tempRemoved = interval;
          while (tempRemoved.getParent() != aligning) {
            tempRemoved = tempRemoved.getParent();
          }
          removedIndex = aligning.remove(tempRemoved);
          operations.cutStartingGap(aligning, dst, dimension, alignment);
          aligning.add(tempRemoved, removedIndex); // add back temporarily removed
        }
      }
      optimizeStructure = true;
    }
    // check congruence of effective alignment
    int effAlign1 = LayoutInterval.getEffectiveAlignment(toAlignWith, alignment);
    //        int effAlign2 = LayoutInterval.getEffectiveAlignment(aligning, alignment);
    //        if (effAlign1 == (alignment^1) /*&& effAlign2 != effAlign1*/) {
    //            LayoutInterval gap = LayoutInterval.getDirectNeighbor(aligning, alignment, false);
    //            if (gap != null && gap.isEmptySpace()) {
    //                layoutModel.setIntervalSize(gap, NOT_EXPLICITLY_DEFINED, gap.getPreferredSize(), Short.MAX_VALUE);
    //            }
    //            gap = LayoutInterval.getDirectNeighbor(aligning, alignment^1, false);
    //            if (gap != null && gap.isEmptySpace() && LayoutInterval.getDirectNeighbor(gap, alignment^1, true) == null) {
    //                layoutModel.setIntervalSize(gap, USE_PREFERRED_SIZE, gap.getPreferredSize(), USE_PREFERRED_SIZE);
    //            }
    //        }
    // separate content out of the emerging group
    List<LayoutInterval> alignedList = new ArrayList<LayoutInterval>(2);
    List<List> remainder = new ArrayList<List>(2);
    int originalCount = parParent.getSubIntervalCount();
    int extAlign1 = extract(toAlignWith, alignedList, remainder, alignment);
    extract(aligning, alignedList, remainder, alignment);
    assert !alignWithParent || remainder.isEmpty();
    // add indent if needed
    int indent =
        LayoutRegion.distance(
            toAlignWith.getCurrentSpace(),
            interval.getCurrentSpace(),
            dimension,
            alignment,
            alignment);
    if (indent != 0) {
      LayoutInterval indentGap = new LayoutInterval(SINGLE);
      indentGap.setSize(Math.abs(indent));
      // [need to use default padding for indent gap]
      LayoutInterval parent = interval.getParent();
      if (parent == null || !parent.isSequential()) {
        LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
        if (parent != null) {
          layoutModel.addInterval(seq, parent, layoutModel.removeInterval(interval));
        }
        layoutModel.setIntervalAlignment(interval, DEFAULT);
        layoutModel.addInterval(interval, seq, 0);
        parent = seq;
      }
      layoutModel.addInterval(indentGap, parent, alignment == LEADING ? 0 : -1);
      if (interval == aligning) {
        alignedList.set(alignedList.size() - 1, parent);
      }
    }
    // prepare the group where the aligned intervals will be placed
    LayoutInterval group;
    LayoutInterval commonSeq;
    if (alignWithParent || originalCount == 2 && parParent.getParent() != null) {
      // reuse the original group - avoid unnecessary nesting
      group = parParent;
      if (!remainder.isEmpty()) { // need a sequence for the remainder group
        LayoutInterval groupParent = group.getParent();
        if (groupParent.isSequential()) {
          commonSeq = groupParent;
        } else { // insert a new one
          int index = layoutModel.removeInterval(group);
          commonSeq = new LayoutInterval(SEQUENTIAL);
          commonSeq.setAlignment(group.getAlignment());
          layoutModel.addInterval(commonSeq, groupParent, index);
          //                    commonSeq.getCurrentSpace().set(dimension, groupParent.getCurrentSpace());
          layoutModel.setIntervalAlignment(group, DEFAULT);
          layoutModel.addInterval(group, commonSeq, -1);
        }
      } else {
        commonSeq = null;
      }
    } else { // need to create a new group
      group = new LayoutInterval(PARALLEL);
      group.setGroupAlignment(alignment);
      if (!remainder.isEmpty()) { // need a new sequence for the remainder group
        commonSeq = new LayoutInterval(SEQUENTIAL);
        commonSeq.add(group, 0);
        if (effAlign1 == LEADING || effAlign1 == TRAILING) {
          commonSeq.setAlignment(effAlign1);
        }
        layoutModel.addInterval(commonSeq, parParent, -1);
        //                commonSeq.getCurrentSpace().set(dimension, parParent.getCurrentSpace());
      } else {
        commonSeq = null;
        if (effAlign1 == LEADING || effAlign1 == TRAILING) {
          group.setAlignment(effAlign1);
        }
        layoutModel.addInterval(group, parParent, -1);
      }
      if (alignment == LEADING || alignment == TRAILING) {
        int alignPos = toAlignWith.getCurrentSpace().positions[dimension][alignment];
        int outerPos = parParent.getCurrentSpace().positions[dimension][alignment ^ 1];
        group.getCurrentSpace().set(
            dimension,
            alignment == LEADING ? alignPos : outerPos,
            alignment == LEADING ? outerPos : alignPos);
      }
    }
    // add the intervals and their separated neighbors to the aligned group
    LayoutInterval aligning2 = alignedList.get(1);
    if (aligning2.getParent() != group) {
      if (aligning2.getParent() != null) {
        layoutModel.removeInterval(aligning2);
      }
      layoutModel.addInterval(aligning2, group, -1);
    }
    if (!LayoutInterval.isAlignedAtBorder(aligning2, alignment)) {
      layoutModel.setIntervalAlignment(aligning2, alignment);
    }
    LayoutInterval aligning1 = alignedList.get(0);
    if (aligning1.getParent() != group) {
      if (aligning1.getParent() != null) {
        layoutModel.setIntervalAlignment(aligning1, extAlign1); //aligning1.getAlignment()); // remember explicit alignment
        layoutModel.removeInterval(aligning1);
      }
      layoutModel.addInterval(aligning1, group, -1);
    }
    if (!resizingOp
        && group.getSubIntervalCount() == 2
        && !LayoutInterval.isAlignedAtBorder(aligning1, alignment)
        && !LayoutInterval.isAlignedAtBorder(aligning2, alignment ^ 1)) {
      layoutModel.setIntervalAlignment(aligning1, alignment);
    }
    // create the remainder group next to the aligned group
    if (!remainder.isEmpty()) {
      int index = commonSeq.indexOf(group);
      if (alignment == TRAILING) {
        index++;
      }
      LayoutInterval sideGroup =
          operations.addGroupContent(remainder, commonSeq, index, dimension, alignment/*, effAlign*/);
      if (sideGroup != null) {
        int pos1 = parParent.getCurrentSpace().positions[dimension][alignment];
        int pos2 = toAlignWith.getCurrentSpace().positions[dimension][alignment];
        sideGroup.getCurrentSpace().set(
            dimension,
            alignment == LEADING ? pos1 : pos2,
            alignment == LEADING ? pos2 : pos1);
        operations.optimizeGaps(sideGroup, dimension);
        operations.mergeParallelGroups(sideGroup);
      }
    }
    return group;
  }

  private int extract(LayoutInterval interval,
      List<LayoutInterval> toAlign,
      List<List> toRemain,
      int alignment) {
    int effAlign = LayoutInterval.getEffectiveAlignment(interval, alignment);
    LayoutInterval parent = interval.getParent();
    if (parent.isSequential()) {
      int extractCount =
          operations.extract(
              interval,
              alignment,
              false,
              alignment == LEADING ? toRemain : null,
              alignment == LEADING ? null : toRemain);
      if (extractCount == 1) { // the parent won't be reused
        if (effAlign == LEADING || effAlign == TRAILING) {
          layoutModel.setIntervalAlignment(interval, effAlign);
        }
        layoutModel.removeInterval(parent);
        toAlign.add(interval);
      } else { // we'll reuse the parent sequence in the new group
        toAlign.add(parent);
      }
    } else {
      toAlign.add(interval);
    }
    return effAlign;
  }

  private void checkParallelResizing(LayoutInterval interval, IncludeDesc iDesc1, IncludeDesc iDesc2) {
    LayoutInterval parallelInt;
    LayoutInterval group = interval.getParent();
    if (group.isSequential()) {
      parallelInt = group;
      group = group.getParent();
    } else {
      parallelInt = interval;
    }
    // do nothing in root and in parallel group tied closely to root on both edges
    if (group.getParent() == null) {
      return;
    }
    // a bit of "heuristics" follows...
    int rootAlign = DEFAULT;
    if (iDesc1.snappedNextTo != null && iDesc1.snappedNextTo.getParent() == null) {
      rootAlign = iDesc1.alignment;
    }
    if (iDesc2 != null && iDesc2.snappedNextTo != null && iDesc2.snappedNextTo.getParent() == null) {
      rootAlign = rootAlign == DEFAULT ? iDesc2.alignment : LayoutRegion.ALL_POINTS;
    }
    if (rootAlign == LEADING || rootAlign == TRAILING) {
      // one edge snapped to root - check the other one for full span
      int remIdx = group.remove(parallelInt); // temporarily
      LayoutInterval neighbor = LayoutInterval.getNeighbor(group, rootAlign ^ 1, false, true, true);
      if (neighbor != null
          && neighbor.getPreferredSize() == NOT_EXPLICITLY_DEFINED
          && LayoutInterval.isAlignedAtBorder(
              neighbor.getParent(),
              LayoutInterval.getRoot(neighbor),
              rootAlign ^ 1)
          || neighbor == null
          && LayoutInterval.isAlignedAtBorder(group, LayoutInterval.getRoot(group), rootAlign ^ 1)) { // the other group edge tied closely to root
        rootAlign = LayoutRegion.ALL_POINTS;
      }
      group.add(parallelInt, remIdx);
    }
    if (rootAlign == LayoutRegion.ALL_POINTS) {
      return;
    }
    // find resizing neighbor gap of interval
    LayoutInterval neighborGap = null;
    if (interval != parallelInt) {
      assert parallelInt.isSequential();
      for (int i = LEADING; i <= TRAILING; i++) {
        LayoutInterval gap = LayoutInterval.getDirectNeighbor(interval, i, false);
        if (gap != null && gap.isEmptySpace() && LayoutInterval.canResize(gap)) {
          neighborGap = gap;
          break;
        }
      }
    }
    // interval or its neighbor gap must be resizing
    if (LayoutInterval.wantResize(interval)) {
      if (!dragger.isResizing(dimension)) {
        return;
      }
    } else if (neighborGap == null) {
      return;
    }
    if (!LayoutInterval.canResize(group)
        && (iDesc1.snappedNextTo != null && !group.isParentOf(iDesc1.snappedNextTo) || iDesc2 != null
            && iDesc2.snappedNextTo != null
            && !group.isParentOf(iDesc2.snappedNextTo))) { // snapped out of the group - it might not want to be suppressed (will check right away)
      operations.enableGroupResizing(group);
    }
    if (LayoutInterval.canResize(group) && group.getParent() != null) {
      // suppress par. group resizing if it is otherwise fixed
      boolean contentResizing = false;
      boolean samePosition = false;
      for (Iterator it = group.getSubIntervals(); it.hasNext();) {
        LayoutInterval li = (LayoutInterval) it.next();
        if (li != parallelInt) {
          if (LayoutInterval.wantResize(li)) {
            contentResizing = true;
            break;
          }
          if (!samePosition) {
            int align = li.getAlignment();
            if (align == LEADING || align == TRAILING) {
              samePosition =
                  getExpectedBorderPosition(parallelInt, dimension, align ^ 1) == getExpectedBorderPosition(
                      li,
                      dimension,
                      align ^ 1);
            }
          }
        }
      }
      if (!contentResizing && samePosition) {
        operations.suppressGroupResizing(group);
      }
    }
    if (!LayoutInterval.canResize(group)) {
      // reset explicit size of interval or gap - subordinate to fixed content
      layoutModel.changeIntervalAttribute(parallelInt, LayoutInterval.ATTRIBUTE_FILL, true);
      if (neighborGap != null) {
        layoutModel.setIntervalSize(
            neighborGap,
            NOT_EXPLICITLY_DEFINED,
            NOT_EXPLICITLY_DEFINED,
            Short.MAX_VALUE);
      } else if (interval.isComponent()) {
        java.awt.Dimension sizeLimit;
        LayoutComponent lc = interval.getComponent();
        sizeLimit =
            lc.isLayoutContainer()
                ? operations.getMapper().getComponentMinimumSize(lc.getId())
                : operations.getMapper().getComponentPreferredSize(lc.getId());
        int pref = dimension == HORIZONTAL ? sizeLimit.width : sizeLimit.height;
        if (interval.getPreferredSize() < pref) {
          layoutModel.setIntervalSize(interval, 0, 0, interval.getMaximumSize());
        } else {
          layoutModel.setIntervalSize(interval, interval.getMinimumSize() != USE_PREFERRED_SIZE
              ? interval.getMinimumSize()
              : NOT_EXPLICITLY_DEFINED, NOT_EXPLICITLY_DEFINED, interval.getMaximumSize());
        }
      }
    }
    if (interval.isComponent()
        && neighborGap == null
        && (parallelInt == interval || LayoutInterval.getCount(parallelInt, DEFAULT, true) == 1)) { // look for same sized components
      setParallelSameSize(group, parallelInt, dimension);
    }
  }

  private void setParallelSameSize(LayoutInterval group, LayoutInterval aligned, int dimension) {
    LayoutInterval alignedComp = getOneNonEmpty(aligned);
    for (Iterator it = group.getSubIntervals(); it.hasNext();) {
      LayoutInterval li = (LayoutInterval) it.next();
      if (li != aligned) {
        if (li.isParallel()) {
          setParallelSameSize(li, alignedComp, dimension);
        } else {
          LayoutInterval sub = getOneNonEmpty(li);
          if (sub != null
              && LayoutRegion.sameSpace(
                  alignedComp.getCurrentSpace(),
                  sub.getCurrentSpace(),
                  dimension) && !LayoutInterval.wantResize(li)) { // viusally aligned subinterval
            if (sub.isParallel()) {
              setParallelSameSize(sub, alignedComp, dimension);
            } else { // make this component filling the group - effectively keeping same size
              layoutModel.setIntervalAlignment(li, aligned.getAlignment());
              int min = sub.getMinimumSize();
              layoutModel.setIntervalSize(sub, min != USE_PREFERRED_SIZE
                  ? min
                  : NOT_EXPLICITLY_DEFINED, sub.getPreferredSize(), Short.MAX_VALUE);
              layoutModel.changeIntervalAttribute(sub, LayoutInterval.ATTRIBUTE_FILL, true);
            }
          }
        }
      }
    }
  }

  private static LayoutInterval getOneNonEmpty(LayoutInterval interval) {
    if (!interval.isSequential()) {
      return interval;
    }
    LayoutInterval nonEmpty = null;
    for (Iterator it = interval.getSubIntervals(); it.hasNext();) {
      LayoutInterval li = (LayoutInterval) it.next();
      if (!li.isEmptySpace()) {
        if (nonEmpty == null) {
          nonEmpty = li;
        } else {
          return null;
        }
      }
    }
    return nonEmpty;
  }

  private int getExpectedBorderPosition(LayoutInterval interval, int dimension, int alignment) {
    LayoutInterval comp = LayoutUtils.getOutermostComponent(interval, dimension, alignment);
    int pos = comp.getCurrentSpace().positions[dimension][alignment];
    LayoutInterval neighbor = LayoutInterval.getNeighbor(comp, alignment, false, true, false);
    if (neighbor != null && neighbor.isEmptySpace() && interval.isParentOf(neighbor)) {
      int diff = neighbor.getPreferredSize();
      if (diff == NOT_EXPLICITLY_DEFINED) {
        diff = LayoutUtils.getSizeOfDefaultGap(neighbor, operations.getMapper());
      }
      if (alignment == LEADING) {
        diff *= -1;
      }
      pos += diff;
    }
    return pos;
  }

  private int determinePadding(LayoutInterval interval,
      PaddingType paddingType,
      int dimension,
      int alignment) {
    LayoutInterval neighbor = LayoutInterval.getNeighbor(interval, alignment, true, true, false);
    if (paddingType == null) {
      paddingType = PaddingType.RELATED;
    }
    return dragger.findPaddings(neighbor, interval, paddingType, dimension, alignment)[0];
    // need to go through dragger as the component of 'interval' is not in model yet
  }

  /**
   * Finds padding for an interval (yet to be added) in relation to a base interval or a parent
   * interval border (base interval null)
   * 
   * @param alignment
   *          LEADING or TRAILING point of addingInt
   */
  private int determineExpectingPadding(LayoutInterval addingInt,
      LayoutInterval baseInt,
      LayoutInterval baseParent,
      int alignment) {
    if (baseInt == null) {
      baseInt = LayoutInterval.getNeighbor(baseParent, SEQUENTIAL, alignment);
    }
    return dragger.findPaddings(baseInt, addingInt, PaddingType.RELATED, dimension, alignment)[0];
  }

  // -----
  private void analyzeParallel(LayoutInterval group, List<IncludeDesc> inclusions) {
    Iterator it = group.getSubIntervals();
    while (it.hasNext()) {
      LayoutInterval sub = (LayoutInterval) it.next();
      if (sub.isEmptySpace()) {
        continue;
      }
      LayoutRegion subSpace = sub.getCurrentSpace();
      if (sub.isParallel()
          && pointInside(addingSpace, aEdge, sub, dimension)
          && shouldEnterGroup(sub)) { // group space contains significant edge
        analyzeParallel(sub, inclusions);
      } else if (sub.isSequential()) {
        // always analyze sequence - it may be valid even if there is no
        // overlap (not required in vertical dimension)
        analyzeSequential(sub, inclusions);
      } else if (orthogonalOverlap(sub)) {
        boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, 0);
        if (dimOverlap && !solveOverlap) {
          IncludeDesc origPos = originalPositions1[dimension];
          if (origPos == null
              || (aEdge ^ 1) != origPos.alignment
              || origPos.parent != sub
              && !sub.isParentOf(origPos.parent)) { // don't want to deal with the overlap here
            continue;
          }
          // otherwise overlapping enclosing space of 'sub' does not
          // matter as the other edge of adding component resides
          // inside 'sub', so this position should be in sequence
        }
        if (dimOverlap) {
          imposeSize = true;
        }
        int distance = LayoutRegion.UNKNOWN;
        if (aSnappedNextTo != null) {
          // check if aSnappedNextTo is related to this position with 'sub' as neighbor
          LayoutInterval neighbor;
          if (sub == aSnappedNextTo
              || sub.isParentOf(aSnappedNextTo)
              || aSnappedNextTo.getParent() == null
              || (neighbor = LayoutInterval.getNeighbor(sub, aEdge, true, true, false)) == aSnappedNextTo
              || neighbor != null
              && neighbor.isParentOf(aSnappedNextTo)) { // nextTo snap is relevant to this position
            distance = -1; // IncludeDesc.snappedNextTo will be set if distance == -1
          }
        }
        if (distance != -1) {
          if (!dimOverlap) { // determine distance from 'sub'
            int dstL = LayoutRegion.distance(subSpace, addingSpace, dimension, TRAILING, LEADING);
            int dstT = LayoutRegion.distance(addingSpace, subSpace, dimension, TRAILING, LEADING);
            distance = dstL >= 0 ? dstL : dstT;
          } else {
            distance = 0; // overlapping
          }
        }
        IncludeDesc iDesc = addInclusion(group, false, distance, 0, inclusions);
        if (iDesc != null) {
          iDesc.neighbor = sub;
          int point = aEdge < 0 ? CENTER : aEdge;
          iDesc.index = getAddDirection(addingSpace, subSpace, dimension, point) == LEADING ? 0 : 1;
        }
      }
    }
    if (inclusions.isEmpty()) { // no inclusion found yet
      if (group.getParent() == null
          && (aSnappedParallel == null || canAlignWith(aSnappedParallel, group, aEdge))) { // this is the last (top) valid group
        int distance = aSnappedNextTo == group ? -1 : Integer.MAX_VALUE;
        addInclusion(group, false, distance, Integer.MAX_VALUE, inclusions);
      }
    }
  }

  private void analyzeSequential(LayoutInterval group, List<IncludeDesc> inclusions) {
    boolean inSequence = false;
    boolean parallelWithSequence = false;
    int index = -1;
    int distance = Integer.MAX_VALUE;
    int ortDistance = Integer.MAX_VALUE;
    for (int i = 0, n = group.getSubIntervalCount(); i < n; i++) {
      LayoutInterval sub = group.getSubInterval(i);
      if (sub.isEmptySpace()) {
        if (index == i) {
          index++;
        }
        continue;
      }
      LayoutRegion subSpace = sub.getCurrentSpace();
      // first analyze the interval as a possible sub-group
      if (sub.isParallel()
          && pointInside(addingSpace, aEdge, sub, dimension)
          && shouldEnterGroup(sub)) { // group space contains significant edge
        int count = inclusions.size();
        analyzeParallel(sub, inclusions);
        if (inclusions.size() > count) {
          return;
        }
      }
      // second analyze the interval as a single element for "next to" placement
      boolean ortOverlap = orthogonalOverlap(sub);
      int margin = dimension == VERTICAL && !ortOverlap ? 4 : 0;
      boolean dimOverlap = LayoutRegion.overlap(addingSpace, subSpace, dimension, margin);
      // in vertical dimension always pretend orthogonal overlap if there
      // is no overlap in horizontal dimension (i.e. force inserting into sequence)
      if (ortOverlap || dimension == VERTICAL && !dimOverlap && !parallelWithSequence) {
        if (dimOverlap) { // overlaps in both dimensions
          if (!solveOverlap) { // don't want to solve the overlap in this sequence
            IncludeDesc origPos = originalPositions1[dimension];
            if (origPos == null
                || (aEdge ^ 1) != origPos.alignment
                || origPos.parent != sub
                && !sub.isParentOf(origPos.parent)) { // overlap will be solved in the other dimension
              return;
            }
            // otherwise overlapping enclosing space of 'sub' does not
            // matter as the other edge of adding component resides
            // inside 'sub', so this position should be in sequence
          }
          if (ortOverlap) {
            imposeSize = true;
          }
          inSequence = true;
          distance = ortDistance = 0;
        } else { // determine distance from the interval
          int dstL = LayoutRegion.distance(subSpace, addingSpace, dimension, TRAILING, LEADING);
          int dstT = LayoutRegion.distance(addingSpace, subSpace, dimension, TRAILING, LEADING);
          if (dstL >= 0 && dstL < distance) {
            distance = dstL;
          }
          if (dstT >= 0 && dstT < distance) {
            distance = dstT;
          }
          if (ortOverlap) {
            ortDistance = 0;
            inSequence = true;
          } else { // remember also the orthogonal distance
            dstL = LayoutRegion.distance(subSpace, addingSpace, dimension ^ 1, TRAILING, LEADING);
            dstT = LayoutRegion.distance(addingSpace, subSpace, dimension ^ 1, TRAILING, LEADING);
            if (dstL > 0 && dstL < ortDistance) {
              ortDistance = dstL;
            }
            if (dstT > 0 && dstT < ortDistance) {
              ortDistance = dstT;
            }
          }
        }
        int point = aEdge < 0 ? CENTER : aEdge;
        if (getAddDirection(addingSpace, subSpace, dimension, point) == LEADING) {
          if (aEdge != LEADING) {
            index = i;
          }
          break; // this interval is already after the adding one, no need to continue
        } else { // intervals before this one are irrelevant
          parallelWithSequence = false;
          if (aEdge == LEADING) {
            index = i + 1;
          }
        }
      } else { // no orthogonal overlap, moreover in vertical dimension located parallelly
        parallelWithSequence = true;
      }
    }
    if (inSequence || dimension == VERTICAL && !parallelWithSequence) {
      // so it make sense to add the interval to this sequence
      if (aSnappedNextTo != null
          && (group.isParentOf(aSnappedNextTo) || aSnappedNextTo.getParent() == null)) { // snapped interval is in this sequence, or it is the root group
        distance = -1; // preferred distance
      }
      IncludeDesc iDesc =
          addInclusion(group, parallelWithSequence, distance, ortDistance, inclusions);
      if (iDesc != null) {
        if (index == -1) {
          index = aEdge == LEADING ? 0 : group.getSubIntervalCount();
        }
        iDesc.index = index;
      }
    }
  }

  private static boolean pointInside(LayoutRegion space,
      int alignment,
      LayoutInterval group,
      int dimension) {
    LayoutRegion groupSpace = group.getCurrentSpace();
    if (alignment != DEFAULT) {
      return LayoutRegion.pointInside(space, alignment, groupSpace, dimension);
    }
    boolean leadingInside = LayoutRegion.pointInside(space, LEADING, groupSpace, dimension);
    boolean trailingInside = LayoutRegion.pointInside(space, TRAILING, groupSpace, dimension);
    return leadingInside
        && trailingInside
        || leadingInside
        && !LayoutInterval.isClosedGroup(group, TRAILING)
        || trailingInside
        && !LayoutInterval.isClosedGroup(group, LEADING);
/*        int point = alignment < 0 ? CENTER : alignment;
        LayoutRegion groupSpace = group.getCurrentSpace();
        if (LayoutRegion.pointInside(space, point, groupSpace, dimension))
            return true;

        if (alignment < 0){
            return (LayoutInterval.getCount(group, TRAILING, true) == 0
                    && LayoutRegion.pointInside(space, LEADING, groupSpace, dimension))
                ||
                   (LayoutInterval.getCount(group, LEADING, true) == 0
                    && LayoutRegion.pointInside(space, TRAILING, groupSpace, dimension));
        }
        return false; */
  }

  private boolean orthogonalOverlap(LayoutInterval interval) {
    boolean ortOverlap;
    if (solveOverlap
        || !LayoutUtils.isOverlapPreventedInOtherDimension(addingInterval, interval, dimension)) {
      // we are interested in the orthogonal overlap (i.e. overlap in the other dimension)
      ortOverlap = LayoutUtils.contentOverlap(addingSpace, interval, dimension ^ 1);
      if (ortOverlap
          && dragger.isResizing(dimension)
          && !dragger.isResizing(dimension ^ 1)
          && originalPositions1[dimension] != null) { // there is overlap, but in case of resizing in one dimension
                                                      // only we should not consider overlap that was not cared of
                                                      // already before the resizing started (i.e. the resizing
                                                      // interval was not in sequence with the interval in question)
        IncludeDesc original = originalPositions1[dimension];
        LayoutInterval parent = original.parent;
        if (parent.isParentOf(interval)) {
          if (parent.isParallel()
              && (original.neighbor == null || original.neighbor != interval
                  && !original.neighbor.isParentOf(interval))) {
            ortOverlap = false;
          }
        } else if (parent == interval) {
          if (parent.isParallel() && original.neighbor == null) {
            ortOverlap = false;
          }
        } else if (!interval.isParentOf(parent)) {
          parent = LayoutInterval.getCommonParent(parent, interval);
          if (parent != null && parent.isParallel()) {
            ortOverlap = false;
          }
        }
      }
    }
    // otherwise the overlap is prevented in the other dimension so we should
    // not consider it (though the actual visual appearance might look so)
    else {
      ortOverlap = false;
    }
    return ortOverlap;
  }

  private IncludeDesc addInclusion(LayoutInterval parent,
      boolean subgroup,
      int distance,
      int ortDistance,
      List<IncludeDesc> inclusions) {
    if (!inclusions.isEmpty()) {
      int index = inclusions.size() - 1;
      IncludeDesc last = inclusions.get(index);
      boolean useLast = false;
      boolean useNew = false;
      boolean ortOverlap1 = last.ortDistance == 0;
      boolean ortOverlap2 = ortDistance == 0;
      if (ortOverlap1 != ortOverlap2) {
        useLast = ortOverlap1;
        useNew = ortOverlap2;
      } else if (ortOverlap1) { // both having orthogonal overlap
        useLast = useNew = true;
      } else { // none having orthogonal overlap (could happen in vertical dimension)
        if (last.ortDistance != ortDistance) {
          useLast = last.ortDistance < ortDistance;
          useNew = ortDistance < last.ortDistance;
        } else if (last.distance != distance) {
          useLast = last.distance < distance;
          useNew = distance < last.distance;
        }
      }
      if (!useLast && !useNew) { // could not choose according to distance, so prefer deeper position
        LayoutInterval parParent = last.parent.isParallel() ? last.parent : last.parent.getParent();
        useNew = parParent.isParentOf(parent);
        useLast = !useNew;
      }
      if (!useLast) {
        inclusions.remove(index);
      }
      if (!useNew) {
        return null;
      }
    }
    IncludeDesc iDesc = new IncludeDesc();
    iDesc.parent = parent;
    iDesc.newSubGroup = subgroup;
    iDesc.alignment = aEdge;
    iDesc.snappedParallel = aSnappedParallel;
    if (distance == -1) {
      iDesc.snappedNextTo = aSnappedNextTo;
      iDesc.paddingType = aPaddingType;
      iDesc.fixedPosition = true;
    }
    iDesc.distance = distance;
    iDesc.ortDistance = ortDistance;
    inclusions.add(iDesc);
    return iDesc;
  }

  /**
   * Adds an inclusion for parallel aligning if none of found non-overlapping inclusions is
   * compatible with the required aligning. Later mergeParallelInclusions may still unify the
   * inclusions, but if not then the inclusion created here is used - because requested parallel
   * aligning needs to be preserved even if overlapping can't be avoided.
   */
  private IncludeDesc addAligningInclusion(List<IncludeDesc> inclusions) {
    if (aSnappedParallel == null) {
      return null;
    }
    boolean compatibleFound = false;
    for (Iterator it = inclusions.iterator(); it.hasNext();) {
      IncludeDesc iDesc = (IncludeDesc) it.next();
      if (canAlignWith(aSnappedParallel, iDesc.parent, aEdge)) {
        compatibleFound = true;
        break;
      }
    }
    if (!compatibleFound) {
      IncludeDesc iDesc = new IncludeDesc();
      iDesc.parent =
          aSnappedParallel.getParent() != null ? LayoutInterval.getFirstParent(
              aSnappedParallel,
              PARALLEL) : aSnappedParallel;
      iDesc.snappedParallel = aSnappedParallel;
      iDesc.alignment = aEdge;
      inclusions.add(0, iDesc);
      return iDesc;
    } else {
      return null;
    }
  }

  /**
   * @param preserveOriginal
   *          if true, original inclusion needs to be preserved, will be merged with new inclusion
   *          sequentially; if false, original inclusion is just consulted when choosing best
   *          inclusion
   */
  private void mergeParallelInclusions(List<IncludeDesc> inclusions,
      IncludeDesc original,
      boolean preserveOriginal) {
    // 1st step - find representative (best) inclusion
    IncludeDesc best = null;
    boolean bestOriginal = false;
    for (Iterator it = inclusions.iterator(); it.hasNext();) {
      IncludeDesc iDesc = (IncludeDesc) it.next();
      if (original == null || !preserveOriginal || canCombine(iDesc, original)) {
        if (best != null) {
          boolean originalCompatible =
              original != null && !preserveOriginal && iDesc.parent == original.parent;
          if (!bestOriginal && originalCompatible) {
            best = iDesc;
            bestOriginal = true;
          } else if (bestOriginal == originalCompatible) {
            LayoutInterval group1 =
                best.parent.isSequential() ? best.parent.getParent() : best.parent;
            LayoutInterval group2 =
                iDesc.parent.isSequential() ? iDesc.parent.getParent() : iDesc.parent;
            if (group1.isParentOf(group2)) {
              best = iDesc; // deeper is better
            } else if (!group2.isParentOf(group1) && iDesc.distance < best.distance) {
              best = iDesc;
            }
          }
        } else {
          best = iDesc;
          bestOriginal = original != null && !preserveOriginal && iDesc.parent == original.parent;
        }
      }
    }
    if (best == null) { // nothing compatible with original position
      assert preserveOriginal;
      inclusions.clear();
      inclusions.add(original);
      return;
    }
    LayoutInterval commonGroup = best.parent.isSequential() ? best.parent.getParent() : best.parent;
    // 2nd remove incompatible inclusions, move compatible ones to same level
    for (Iterator it = inclusions.iterator(); it.hasNext();) {
      IncludeDesc iDesc = (IncludeDesc) it.next();
      if (iDesc != best) {
        if (!compatibleInclusions(iDesc, best, dimension)) {
          it.remove();
        } else {
          LayoutInterval group =
              iDesc.parent.isSequential() ? iDesc.parent.getParent() : iDesc.parent;
          if (group.isParentOf(commonGroup)) {
            LayoutInterval neighbor = iDesc.parent.isSequential() ? iDesc.parent : iDesc.neighbor;
            layoutModel.removeInterval(neighbor);
            // [what about the alignment?]
            layoutModel.addInterval(neighbor, commonGroup, -1);
            if (group.getSubIntervalCount() == 1) {
              LayoutInterval parent = group.getParent();
              LayoutInterval last = layoutModel.removeInterval(group, 0);
              operations.addContent(last, parent, layoutModel.removeInterval(group));
              if (commonGroup == last && commonGroup.getParent() == null) {
                commonGroup = parent;
              }
              updateReplacedOriginalGroup(commonGroup, null);
            }
            if (iDesc.parent == group) {
              iDesc.parent = commonGroup;
            }
          }
        }
      }
    }
    if (best.parent.isParallel()
        && best.snappedParallel != null
        && best.ortDistance != 0
        && inclusions.size() > 1) { // forced inclusion by addAlignedInclusion
      inclusions.remove(best);
    }
    if (inclusions.size() == 1) {
      return;
    }
    // 3rd analyse inclusions requiring a subgroup (parallel with part of sequence)
    LayoutInterval subGroup = null;
    LayoutInterval nextTo = null;
    List<List> separatedLeading = new LinkedList<List>();
    List<List> separatedTrailing = new LinkedList<List>();
    for (Iterator it = inclusions.iterator(); it.hasNext();) {
      IncludeDesc iDesc = (IncludeDesc) it.next();
      if (iDesc.parent.isSequential() && iDesc.newSubGroup) {
        LayoutInterval parSeq =
            extractParallelSequence(iDesc.parent, addingSpace, false, iDesc.alignment);
        assert parSeq.isParallel(); // parallel group with part of the original sequence
        if (subGroup == null) {
          subGroup = parSeq;
        } else {
          LayoutInterval sub = layoutModel.removeInterval(parSeq, 0);
          layoutModel.addInterval(sub, subGroup, -1);
        }
        // extract surroundings of the group in the sequence
        operations.extract(parSeq, DEFAULT, true, separatedLeading, separatedTrailing);
        layoutModel.removeInterval(parSeq);
        layoutModel.removeInterval(iDesc.parent);
      }
    }
    int extractAlign = DEFAULT;
    if (subGroup != null) {
      if (separatedLeading.isEmpty()) {
        extractAlign = TRAILING;
      }
      if (separatedTrailing.isEmpty()) {
        extractAlign = LEADING;
      }
    }
    // 4th collect surroundings of adding interval
    // (the intervals will go into a side group in step 5, or into subgroup
    //  of 'subGroup' next to the adding interval if in previous step some
    //  content was separated into a parallel subgroup of a sequence)
    LayoutInterval subsubGroup = null;
    for (Iterator it = inclusions.iterator(); it.hasNext();) {
      IncludeDesc iDesc = (IncludeDesc) it.next();
      if (iDesc.parent.isParallel() || !iDesc.newSubGroup) {
        addToGroup(iDesc, null, false);
        //                mainEffectiveAlign = getEffectiveAlignment(interval);
        operations.extract(
            addingInterval,
            extractAlign,
            extractAlign == DEFAULT,
            separatedLeading,
            separatedTrailing);
        LayoutInterval parent = addingInterval.getParent();
        layoutModel.removeInterval(addingInterval);
        layoutModel.removeInterval(parent);
        if (extractAlign != DEFAULT && LayoutInterval.getCount(parent, DEFAULT, true) >= 1) {
          if (subsubGroup == null) {
            subsubGroup = new LayoutInterval(PARALLEL);
            subsubGroup.setGroupAlignment(extractAlign);
          }
          operations.addContent(parent, subsubGroup, -1);
        }
      }
      if (iDesc.snappedNextTo != null) {
        nextTo = iDesc.snappedNextTo;
      }
      if (iDesc != best) {
        it.remove();
      }
    }
    // prepare the common group for merged content
    int[] borderPos = commonGroup.getCurrentSpace().positions[dimension];
    int[] neighborPos =
        (subGroup != null ? subGroup : addingInterval).getCurrentSpace().positions[dimension];
    LayoutInterval commonSeq;
    int index;
    if (commonGroup.getSubIntervalCount() == 0 && commonGroup.getParent() != null) {
      // the common group got empty - eliminate it to avoid unncessary nesting
      LayoutInterval parent = commonGroup.getParent();
      index = layoutModel.removeInterval(commonGroup);
      if (parent.isSequential()) {
        commonSeq = parent;
        commonGroup = parent.getParent();
      } else { // parallel parent
        commonSeq = new LayoutInterval(SEQUENTIAL);
        commonSeq.setAlignment(commonGroup.getAlignment());
        layoutModel.addInterval(commonSeq, parent, index);
        commonGroup = parent;
        index = 0;
      }
    } else {
      commonSeq = new LayoutInterval(SEQUENTIAL);
      layoutModel.addInterval(commonSeq, commonGroup, -1);
      index = 0;
    }
    if (commonSeq.getSubIntervalCount() == 0) {
      commonSeq.getCurrentSpace().set(dimension, commonGroup.getCurrentSpace());
    }
    updateReplacedOriginalGroup(commonGroup, commonSeq);
    // 5th create groups of merged content around the adding component
    LayoutInterval sideGroupLeading = null;
    LayoutInterval sideGroupTrailing = null;
    if (!separatedLeading.isEmpty()) {
      int checkCount = commonSeq.getSubIntervalCount(); // remember ...
      sideGroupLeading =
          operations.addGroupContent(separatedLeading, commonSeq, index, dimension, LEADING); //, mainEffectiveAlign
      index += commonSeq.getSubIntervalCount() - checkCount;
    }
    if (!separatedTrailing.isEmpty()) {
      sideGroupTrailing =
          operations.addGroupContent(separatedTrailing, commonSeq, index, dimension, TRAILING); //, mainEffectiveAlign
    }
    if (sideGroupLeading != null) {
      int checkCount = commonSeq.getSubIntervalCount(); // remember ...
      sideGroupLeading.getCurrentSpace().set(dimension, borderPos[LEADING], neighborPos[LEADING]);
      operations.optimizeGaps(sideGroupLeading, dimension);
      index += commonSeq.getSubIntervalCount() - checkCount;
    }
    if (sideGroupTrailing != null) {
      sideGroupTrailing.getCurrentSpace().set(dimension, neighborPos[TRAILING], borderPos[TRAILING]);
      operations.optimizeGaps(sideGroupTrailing, dimension);
    }
    // 6th adjust the final inclusion
    best.parent = commonSeq;
    best.newSubGroup = false;
    best.neighbor = null;
    LayoutInterval separatingGap;
    int gapIdx = index;
    if (gapIdx == commonSeq.getSubIntervalCount()) {
      gapIdx--;
      separatingGap = commonSeq.getSubInterval(gapIdx);
    } else {
      separatingGap = commonSeq.getSubInterval(gapIdx);
      if (!separatingGap.isEmptySpace()) {
        gapIdx--;
        if (gapIdx > 0) {
          separatingGap = commonSeq.getSubInterval(gapIdx);
        }
      }
    }
    if (!separatingGap.isEmptySpace()) {
      separatingGap = null;
    } else if (subGroup == null) {
      index = gapIdx;
      // eliminate the gap if caused by addToGroup called to separate adding
      // interval's surroundings to side groups; the gap will be created
      // again when addToGroup is called definitively (for merged inclusions)
      if (index == 0 && !LayoutInterval.isAlignedAtBorder(commonSeq, LEADING)) {
        layoutModel.removeInterval(separatingGap);
        separatingGap = null;
      } else if (index == commonSeq.getSubIntervalCount() - 1
          && !LayoutInterval.isAlignedAtBorder(commonSeq, TRAILING)) {
        layoutModel.removeInterval(separatingGap);
        separatingGap = null;
      }
    }
    best.snappedNextTo = nextTo;
    if (nextTo != null) {
      best.fixedPosition = true;
    }
    // 7th resolve subgroup
    if (subGroup != null) {
      if (separatingGap != null
          && (extractAlign == DEFAULT || extractAlign == LEADING && index > gapIdx || extractAlign == TRAILING
              && index <= gapIdx)) { // subGroup goes next to a separating gap - which is likely superflous
                                     // (the extracted parallel sequence in subGroup has its own gap)
        layoutModel.removeInterval(separatingGap);
        if (index >= gapIdx && index > 0) {
          index--;
        }
      }
      int subIdx = index;
      if (subsubGroup != null && subsubGroup.getSubIntervalCount() > 0) {
        LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
        seq.setAlignment(best.alignment);
        operations.addContent(subsubGroup, seq, 0);
        layoutModel.addInterval(seq, subGroup, -1);
        // [should run optimizeGaps on subsubGroup?]
        best.parent = seq;
        index = extractAlign == LEADING ? 0 : seq.getSubIntervalCount();
      } else {
        best.newSubGroup = true;
      }
      operations.addContent(subGroup, commonSeq, subIdx);
      updateMovedOriginalNeighbor();
    }
    best.index = index;
  }

  private static boolean compatibleInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2, int dimension) {
    LayoutInterval group1 =
        iDesc1.parent.isSequential() ? iDesc1.parent.getParent() : iDesc1.parent;
    LayoutInterval group2 =
        iDesc2.parent.isSequential() ? iDesc2.parent.getParent() : iDesc2.parent;
    if (group1 == group2) {
      return true;
    }
    if (group1.isParentOf(group2)) {
      // swap so group2 is parent of group1 (iDesc1 the deeper inclusion)
      LayoutInterval temp = group1;
      group1 = group2;
      group2 = temp;
      IncludeDesc itemp = iDesc1;
      iDesc1 = iDesc2;
      iDesc2 = itemp;
    } else if (!group2.isParentOf(group1)) {
      return false;
    }
    LayoutInterval neighbor; // to be moved into the deeper group (in parallel)
    if (iDesc2.parent.isSequential()) {
      if (iDesc2.parent.isParentOf(iDesc1.parent)) {
        // in the same sequence, can't combine in parallel
        return false;
      }
      neighbor = iDesc2.parent;
    } else {
      neighbor = iDesc2.neighbor;
    }
    if (neighbor == null) {
      return false;
    }
    LayoutRegion spaceToHold = neighbor.getCurrentSpace();
    LayoutRegion spaceAvailable = group1.getCurrentSpace();
    return LayoutRegion.pointInside(spaceToHold, LEADING, spaceAvailable, dimension)
        && LayoutRegion.pointInside(spaceToHold, TRAILING, spaceAvailable, dimension);
  }

  private void updateReplacedOriginalGroup(LayoutInterval newGroup, LayoutInterval newSeq) {
    updateReplacedOriginalGroup(originalPositions1[dimension], newGroup, newSeq);
    updateReplacedOriginalGroup(originalPositions2[dimension], newGroup, newSeq);
  }

  private static void updateReplacedOriginalGroup(IncludeDesc iDesc,
      LayoutInterval newGroup,
      LayoutInterval newSeq) {
    if (iDesc != null && LayoutInterval.getRoot(newGroup) != LayoutInterval.getRoot(iDesc.parent)) {
      if (iDesc.parent.isParallel()) {
        iDesc.parent = newGroup;
      } else if (newSeq != null) {
        iDesc.parent = newSeq;
      }
    }
  }

  private void updateMovedOriginalNeighbor() {
    updateMovedOriginalNeighbor(originalPositions1[dimension]);
    updateMovedOriginalNeighbor(originalPositions2[dimension]);
  }

  private static void updateMovedOriginalNeighbor(IncludeDesc iDesc) {
    if (iDesc != null && iDesc.neighbor != null) {
      iDesc.parent = LayoutInterval.getFirstParent(iDesc.neighbor, PARALLEL);
      correctNeighborInSequence(iDesc);
    }
  }

  private boolean mergeSequentialInclusions(IncludeDesc iDesc1, IncludeDesc iDesc2) {
    if (iDesc2 == null || !canCombine(iDesc1, iDesc2)) {
      return false;
    }
    assert (iDesc1.alignment == LEADING || iDesc1.alignment == TRAILING)
        && (iDesc2.alignment == LEADING || iDesc2.alignment == TRAILING)
        && iDesc1.alignment == (iDesc2.alignment ^ 1);
    if (iDesc1.parent == iDesc2.parent) {
      return true;
    }
    LayoutInterval commonGroup;
    boolean nextTo;
    if (iDesc1.parent.isParentOf(iDesc2.parent)) {
      commonGroup = iDesc1.parent;
      nextTo =
          iDesc1.neighbor != null || iDesc2.snappedNextTo != null || iDesc2.parent.isSequential();
    } else if (iDesc2.parent.isParentOf(iDesc1.parent)) {
      commonGroup = iDesc2.parent;
      nextTo =
          iDesc2.neighbor != null || iDesc1.snappedNextTo != null || iDesc1.parent.isSequential();
    } else {
      commonGroup = LayoutInterval.getFirstParent(iDesc1.parent, SEQUENTIAL);
      nextTo = false;
    }
    if (commonGroup.isSequential() || nextTo) {
      // inclusions in common sequence or the upper inclusion has the lower as neighbor
      if (iDesc1.alignment == TRAILING) {
        IncludeDesc temp = iDesc1;
        iDesc1 = iDesc2;
        iDesc2 = temp;
      } // so iDesc1 is leading and iDesc2 trailing
      int startIndex = 0;
      LayoutInterval ext1 = null;
      boolean startGap = false;
      int endIndex = 0;
      LayoutInterval ext2 = null;
      boolean endGap = false;
      if (commonGroup.isSequential()) {
        if (commonGroup.isParentOf(iDesc1.parent)) {
          ext1 = iDesc1.parent.isSequential() ? iDesc1.parent : iDesc1.neighbor;
          if (ext1 != null) {
            while (ext1.getParent().getParent() != commonGroup) {
              ext1 = ext1.getParent();
            }
            startIndex = commonGroup.indexOf(ext1.getParent());
          } else { // nothing to extract, just find out the index
            LayoutInterval inCommon = iDesc1.parent;
            while (inCommon.getParent() != commonGroup) {
              inCommon = inCommon.getParent();
            }
            startIndex = commonGroup.indexOf(inCommon);
          }
        } else {
          startIndex = iDesc1.index;
          if (startIndex == commonGroup.getSubIntervalCount()) {
            startIndex--;
          }
          startGap = commonGroup.getSubInterval(startIndex).isEmptySpace();
        }
        if (commonGroup.isParentOf(iDesc2.parent)) {
          ext2 = iDesc2.parent.isSequential() ? iDesc2.parent : iDesc2.neighbor;
          if (ext2 != null) {
            while (ext2.getParent().getParent() != commonGroup) {
              ext2 = ext2.getParent();
            }
            endIndex = commonGroup.indexOf(ext2.getParent());
          } else {
            LayoutInterval inCommon = iDesc2.parent;
            while (inCommon.getParent() != commonGroup) {
              inCommon = inCommon.getParent();
            }
            endIndex = commonGroup.indexOf(inCommon);
          }
        } else {
          endIndex = iDesc2.index;
          if (iDesc2.snappedParallel == null || !commonGroup.isParentOf(iDesc2.snappedParallel)) {
            endGap = commonGroup.getSubInterval(--endIndex).isEmptySpace();
          }
        }
      }
      if ((endIndex > startIndex + 1 || endIndex == startIndex + 1 && !startGap && !endGap)
          && (ext1 != null && !iDesc1.newSubGroup || ext2 != null && !iDesc2.newSubGroup)) { // there is a significant part of the common sequence to be parallelized
        LayoutInterval parGroup;
        if (startIndex == 0 && endIndex == commonGroup.getSubIntervalCount() - 1) {
          // parallel with whole sequence
          parGroup = commonGroup.getParent();
        } else { // separate part of the original sequence
          parGroup = new LayoutInterval(PARALLEL);
          LayoutInterval parSeq = new LayoutInterval(SEQUENTIAL);
          layoutModel.addInterval(parSeq, parGroup, 0);
          parGroup.getCurrentSpace().set(
              dimension,
              LayoutUtils.getVisualPosition(
                  commonGroup.getSubInterval(startIndex),
                  dimension,
                  LEADING),
              LayoutUtils.getVisualPosition(
                  commonGroup.getSubInterval(endIndex),
                  dimension,
                  TRAILING));
          int i = startIndex;
          while (i <= endIndex) {
            LayoutInterval li = layoutModel.removeInterval(commonGroup, i);
            endIndex--;
            layoutModel.addInterval(li, parSeq, -1);
          }
          layoutModel.addInterval(parGroup, commonGroup, startIndex);
        }
        LayoutInterval extSeq = new LayoutInterval(SEQUENTIAL); // sequence for the extracted inclusion targets
        layoutModel.addInterval(extSeq, parGroup, -1);
        if (ext1 != null) {
          LayoutInterval parent = ext1.getParent();
          layoutModel.removeInterval(ext1);
          if (parent.getSubIntervalCount() == 1) {
            LayoutInterval last = layoutModel.removeInterval(parent, 0);
            operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent));
          }
          operations.addContent(ext1, extSeq, 0);
          if (ext2 != null) {
            LayoutInterval gap = new LayoutInterval(SINGLE);
            int size =
                LayoutRegion.distance(
                    ext1.getCurrentSpace(),
                    ext2.getCurrentSpace(),
                    dimension,
                    LEADING,
                    TRAILING);
            gap.setSize(size);
            layoutModel.addInterval(gap, extSeq, -1);
            iDesc1.index = iDesc2.index = extSeq.indexOf(gap);
          } else {
            iDesc2.index = iDesc1.index;
          }
        } else {
          iDesc1.index = iDesc2.index;
        }
        if (ext2 != null) {
          LayoutInterval parent = ext2.getParent();
          layoutModel.removeInterval(ext2);
          if (parent.getSubIntervalCount() == 1) {
            LayoutInterval last = layoutModel.removeInterval(parent, 0);
            operations.addContent(last, parent.getParent(), layoutModel.removeInterval(parent));
          }
          operations.addContent(ext2, extSeq, -1);
        }
        iDesc1.parent = iDesc2.parent = extSeq;
        iDesc1.newSubGroup = iDesc2.newSubGroup = false;
        iDesc1.neighbor = iDesc2.neighbor = null;
      } else { // end position, stay in subgroup
        if (iDesc2.parent.isParentOf(iDesc1.parent)) {
          iDesc2.parent = iDesc1.parent;
          iDesc2.index = iDesc1.index;
          iDesc2.newSubGroup = iDesc1.newSubGroup;
          iDesc2.neighbor = iDesc1.neighbor;
          if (endGap) {
            iDesc2.fixedPosition = false;
          }
        } else if (iDesc1.parent.isParentOf(iDesc2.parent)) {
          iDesc1.parent = iDesc2.parent;
          iDesc1.index = iDesc2.index;
          iDesc1.newSubGroup = iDesc2.newSubGroup;
          iDesc1.neighbor = iDesc2.neighbor;
          if (startGap) {
            iDesc1.fixedPosition = false;
          }
        }
      }
    } else { // common group is parallel - there is nothing in sequence, so nothing to extract
      assert iDesc1.parent.isParallel()
          && iDesc2.parent.isParallel()
          && (commonGroup == iDesc1.parent || commonGroup == iDesc2.parent)
          && iDesc1.neighbor == null
          && iDesc2.neighbor == null;
      if (iDesc2.snappedNextTo == null
          && iDesc2.snappedParallel == null
          || iDesc2.snappedParallel != null
          && canAlignWith(iDesc2.snappedParallel, iDesc1.parent, iDesc2.alignment)) { // iDesc2 can adapt to iDesc1
        iDesc2.parent = iDesc1.parent;
        return true;
      }
      if (iDesc2.parent == commonGroup) {
        IncludeDesc temp = iDesc1;
        iDesc1 = iDesc2;
        iDesc2 = temp;
      } // so iDesc1 is super-group and iDesc2 subgroup
      assert iDesc2.snappedNextTo == null;
      if (iDesc2.snappedParallel == iDesc2.parent) {
        iDesc2.parent = LayoutInterval.getFirstParent(iDesc2.parent, PARALLEL);
        if (iDesc2.parent == iDesc1.parent) {
          return true;
        }
      }
      if (iDesc2.snappedParallel == null
          || canAlignWith(iDesc2.snappedParallel, iDesc1.parent, iDesc2.alignment)) {
        // subgroup is either not snapped at all, or can align also in parent group
        iDesc2.parent = iDesc1.parent;
        return true;
      }
      if (LayoutInterval.isAlignedAtBorder(iDesc2.parent, iDesc1.parent, iDesc1.alignment)) {
        iDesc1.parent = iDesc2.parent;
        return true; // subgroup is aligned to parent group edge
      }
      LayoutInterval seq = iDesc2.parent.getParent();
      if (seq.isSequential() && seq.getParent() == iDesc1.parent) {
        int index = seq.indexOf(iDesc2.parent) + (iDesc1.alignment == LEADING ? -1 : 1);
        LayoutInterval gap =
            index == 0 || index == seq.getSubIntervalCount() - 1 ? seq.getSubInterval(index) : null;
        if (gap != null
            && LayoutInterval.isFixedDefaultPadding(gap)
            && iDesc1.snappedNextTo == iDesc1.parent
            && LayoutInterval.wantResize(seq)) { // subgroup is at preferred gap from parent - corresponds to parent's snappedNextTo
          iDesc1.parent = iDesc2.parent;
          iDesc1.snappedNextTo = null;
          iDesc1.snappedParallel = iDesc2.parent;
          return true;
        }
        if (gap != null && gap.isEmptySpace() && iDesc1.snappedParallel == iDesc1.parent) {
          // need to make the subgroup aligned to parent group
          int gapSize = LayoutInterval.getIntervalCurrentSize(gap, dimension);
          copyGapInsideGroup(gap, gapSize, iDesc2.parent, iDesc1.alignment);
          layoutModel.removeInterval(gap);
          iDesc1.parent = iDesc2.parent;
          return true;
        }
      }
      iDesc2.parent = iDesc1.parent; // prefer super-group otherwise
    }
    return true;
  }

  /**
   * Moves a gap next to a parallel group into the parallel group - i.e. each interval in the group
   * gets extended by the gap. Sort of opposite to LayoutOperations.optimizeGaps.
   * 
   * @param alignment
   *          which side of the group is extended
   */
  private void copyGapInsideGroup(LayoutInterval gap,
      int gapSize,
      LayoutInterval group,
      int alignment) {
    assert gap.isEmptySpace() && (alignment == LEADING || alignment == TRAILING);
    if (alignment == LEADING) {
      gapSize = -gapSize;
    }
    group.getCurrentSpace().positions[dimension][alignment] += gapSize;
    for (Iterator it = group.getSubIntervals(); it.hasNext();) {
      LayoutInterval sub = (LayoutInterval) it.next();
      LayoutInterval gapClone = LayoutInterval.cloneInterval(gap, null);
      if (sub.isSequential()) {
        sub.getCurrentSpace().positions[dimension][alignment] += gapSize;
        int index = alignment == LEADING ? 0 : sub.getSubIntervalCount();
        operations.insertGapIntoSequence(gapClone, sub, index, dimension);
      } else {
        LayoutInterval seq = new LayoutInterval(SEQUENTIAL);
        seq.getCurrentSpace().set(dimension, sub.getCurrentSpace());
        seq.getCurrentSpace().positions[dimension][alignment] += gapSize;
        seq.setAlignment(sub.getRawAlignment());
        layoutModel.addInterval(seq, group, layoutModel.removeInterval(sub));
        layoutModel.setIntervalAlignment(sub, DEFAULT);
        layoutModel.addInterval(sub, seq, 0);
        layoutModel.addInterval(gapClone, seq, alignment == LEADING ? 0 : 1);
      }
    }
  }

  private boolean shouldEnterGroup(LayoutInterval group) {
    assert group.isParallel();
    if (aSnappedParallel == null) {
      return true;
    }
    if (group == aSnappedParallel || group.isParentOf(aSnappedParallel)) {
      return true; // same tree
    }
    // Determine if within or under 'group' one might align in parallel
    // with required 'aSnappedParallel' interval. So return false if content
    // of 'group' is in an incompatible branch
    LayoutInterval interval = aSnappedParallel;
    LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
    while (parent != null) {
      if (LayoutInterval.isAlignedAtBorder(interval, parent, aEdge)) {
        if (parent.isParentOf(group) && LayoutInterval.isAlignedAtBorder(group, parent, aEdge)) {
          return true;
        }
        interval = parent;
        parent = LayoutInterval.getFirstParent(interval, PARALLEL);
      } else {
        parent = null;
      }
    }
    return false;
  }

  /**
   * @return whether being in 'group' (having it as first parallel parent) allows parallel align
   *         with 'interval'
   */
  private boolean canAlignWith(LayoutInterval interval, LayoutInterval group, int alignment) {
    if (group.isSequential()) {
      group = group.getParent();
    }
    if (interval == group) {
      return true; // can align to group border from inside
    }
    LayoutInterval parent = interval.getParent();
    if (parent == null) {
      parent = interval;
    } else if (parent.isSequential()) {
      parent = parent.getParent();
    }
    while (parent != null && parent != group && !parent.isParentOf(group)) {
      if (canSubstAlignWithParent(interval, dimension, alignment, dragger.isResizing(dimension))) {
        interval = parent;
        parent = LayoutInterval.getFirstParent(interval, PARALLEL);
      } else {
        parent = null;
      }
    }
    if (parent == null) {
      return false;
    }
    if (parent == group) {
      return true;
    }
    // otherwise parent.isParentOf(group)
    return LayoutInterval.isAlignedAtBorder(group, parent, alignment);
    // we silently assume that addingInterval will end up aligned in 'group'
  }

  private static boolean canSubstAlignWithParent(LayoutInterval interval,
      int dimension,
      int alignment,
      boolean placedAtBorderEnough) {
    LayoutInterval parent = LayoutInterval.getFirstParent(interval, PARALLEL);
    boolean aligned = LayoutInterval.isAlignedAtBorder(interval, parent, alignment);
    if (!aligned
        && LayoutInterval.getDirectNeighbor(interval, alignment, false) == null
        && LayoutInterval.isPlacedAtBorder(interval, parent, dimension, alignment)) { // not aligned, but touching parallel group border
      aligned =
          placedAtBorderEnough
              || LayoutInterval.getDirectNeighbor(parent, alignment, true) != null
              || LayoutInterval.isClosedGroup(parent, alignment);
      if (!aligned) { // check if the group can be considered "closed" at alignment edge
        boolean allTouching = true;
        for (Iterator it = parent.getSubIntervals(); it.hasNext();) {
          LayoutInterval li = (LayoutInterval) it.next();
          if (li.getAlignment() == alignment || LayoutInterval.wantResize(li)) {
            aligned = true;
            break;
          } else if (allTouching && !LayoutInterval.isPlacedAtBorder(li, dimension, alignment)) {
            allTouching = false;
          }
        }
        if (allTouching) {
          aligned = true;
        }
      }
    }
    return aligned;
  }

  private boolean canCombine(IncludeDesc iDesc1, IncludeDesc iDesc2) {
    if (iDesc1.parent == iDesc2.parent) {
      return true;
    }
    if (iDesc1.parent.isParentOf(iDesc2.parent)) {
      return isBorderInclusion(iDesc2);
    } else if (iDesc2.parent.isParentOf(iDesc1.parent)) {
      return isBorderInclusion(iDesc1);
    } else {
      LayoutInterval parParent1 =
          iDesc1.parent.isParallel() ? iDesc1.parent : iDesc1.parent.getParent();
      LayoutInterval parParent2 =
          iDesc2.parent.isParallel() ? iDesc2.parent : iDesc2.parent.getParent();
      return parParent1.getParent() == parParent2.getParent()
          && isBorderInclusion(iDesc1)
          && isBorderInclusion(iDesc2)
          && LayoutInterval.getDirectNeighbor(parParent1, iDesc1.alignment ^ 1, true) == parParent2;
    }
  }

  private boolean isBorderInclusion(IncludeDesc iDesc) {
    if (iDesc.alignment != LEADING && iDesc.alignment != TRAILING) {
      return false;
    }
    if (iDesc.parent.isSequential()) {
      int startIndex = iDesc.alignment == LEADING ? iDesc.index : 0;
      int endIndex;
      if (iDesc.alignment == LEADING) {
        endIndex = iDesc.parent.getSubIntervalCount() - 1;
      } else {
        endIndex = iDesc.index - 1;
        if (endIndex >= iDesc.parent.getSubIntervalCount()) {
          // if comming from original position the original index might be too high
          endIndex = iDesc.parent.getSubIntervalCount() - 1;
        }
      }
      return startIndex > endIndex
          || !LayoutUtils.contentOverlap(
              addingSpace,
              iDesc.parent,
              startIndex,
              endIndex,
              dimension ^ 1);
    }
    return iDesc.neighbor == null
        || iDesc.alignment == LEADING
        && iDesc.index >= 1
        || iDesc.alignment == TRAILING
        && iDesc.index == 0;
  }

  private static int getAddDirection(LayoutRegion adding,
      LayoutRegion existing,
      int dimension,
      int alignment) {
    return LayoutRegion.distance(adding, existing, dimension, alignment, CENTER) > 0
        ? LEADING
        : TRAILING;
  }
}
