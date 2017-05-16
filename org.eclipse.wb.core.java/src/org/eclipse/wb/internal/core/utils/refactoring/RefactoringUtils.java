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
package org.eclipse.wb.internal.core.utils.refactoring;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import java.util.Map;

/**
 * Helper class for various refactoring utilities.
 *
 * @author scheglov_ke
 * @coverage core.util.refactoring
 */
public class RefactoringUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic refactoring utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Change} for renaming given {@link IType}.
   */
  public static Change createRenameTypeChange(IType type, String newName, IProgressMonitor pm)
      throws CoreException {
    int flags =
        org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor.JAR_MIGRATION
            | org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor.JAR_REFACTORING
            | RefactoringDescriptor.STRUCTURAL_CHANGE;
    if (!Flags.isPrivate(type.getFlags())) {
      flags |= RefactoringDescriptor.MULTI_CHANGE;
    }
    //
    org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor descriptor =
        new org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor(org.eclipse.jdt.core.refactoring.IJavaRefactorings.RENAME_TYPE);
    descriptor.setProject(type.getJavaProject().getElementName());
    descriptor.setFlags(flags);
    descriptor.setJavaElement(type);
    descriptor.setNewName(newName);
    descriptor.setUpdateQualifiedNames(false);
    descriptor.setUpdateTextualOccurrences(false);
    descriptor.setUpdateReferences(true);
    // prepare Refactoring
    RefactoringStatus refactoringStatus = new RefactoringStatus();
    Refactoring refactoring = descriptor.createRefactoring(refactoringStatus);
    // prepare Change
    CreateChangeOperation createChangeOperation =
        new CreateChangeOperation(new CheckConditionsOperation(refactoring,
            CheckConditionsOperation.ALL_CONDITIONS), RefactoringStatus.FATAL);
    createChangeOperation.run(new SubProgressMonitor(pm, 6));
    return createChangeOperation.getChange();
  }

  /**
   * @return {@link Change} for removing given {@link IType}.
   */
  public static Change createDeleteTypeChange(IType type) throws JavaModelException {
    IFile file = (IFile) type.getUnderlyingResource();
    return new DeleteFileChange(file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TextChange utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves {@link TextFileChange} for same {@link IFile}'s from given <code>sourceChange</code> to
   * <code>targetChange</code>.
   */
  public static void mergeTextChanges(Change targetChange, Change sourceChange) {
    Map<IFile, TextFileChange> sourceFileChanges = getTextFileChanges(targetChange);
    Map<IFile, TextFileChange> targetFileChanges = getTextFileChanges(sourceChange);
    // iterate over source changes
    for (Map.Entry<IFile, TextFileChange> entry : sourceFileChanges.entrySet()) {
      IFile file = entry.getKey();
      // check if there is change for same file in target change
      TextFileChange targetFileChange = targetFileChanges.get(file);
      if (targetFileChange != null) {
        TextFileChange sourceFileChange = entry.getValue();
        mergeTextChanges(targetFileChange, sourceFileChange);
      }
    }
  }

  /**
   * Merges children of given composite {@link Change} with currently existing
   * {@link TextFileChange}'s (from main refactoring).
   */
  public static void mergeTextChange(RefactoringParticipant participant, Change change) {
    if (change instanceof CompositeChange) {
      CompositeChange compositeChange = (CompositeChange) change;
      for (Change changeChild : compositeChange.getChildren()) {
        mergeTextChange(participant, changeChild);
      }
    } else if (change instanceof TextFileChange) {
      TextFileChange textFileChange = (TextFileChange) change;
      // if we have existing change for this file, merge text edits
      TextChange existingChange = participant.getTextChange(textFileChange.getFile());
      if (existingChange != null) {
        mergeTextChanges(existingChange, textFileChange);
      }
    }
  }

  /**
   * Moves <code>sourceTextChange</code> into <code>targetTextChange</code> {@link TextEdit}.
   */
  private static void mergeTextChanges(TextChange targetTextChange, TextChange sourceTextChange) {
    // do merge TextEdit
    {
      TextEdit rootEdit = sourceTextChange.getEdit();
      mergeTextEdit(targetTextChange, rootEdit);
    }
    // remove "source" change
    {
      Change parent = sourceTextChange.getParent();
      if (parent instanceof CompositeChange) {
        ((CompositeChange) parent).remove(sourceTextChange);
      }
    }
  }

  /**
   * Separate {@link TextEdit}'s are grouped using {@link MultiTextEdit}, but we can not just use
   * {@link TextChange#addEdit(TextEdit)} because sometimes "multi" edit consists of changes in
   * beginning and ending of file, so its range is full file, so "multi" edit will conflict with
   * other edits, but individual edits - not.
   */
  private static void mergeTextEdit(TextChange targetTextChange, TextEdit textEdit) {
    if (textEdit instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit = (MultiTextEdit) textEdit;
      for (TextEdit child : multiTextEdit.getChildren()) {
        mergeTextEdit(targetTextChange, child);
      }
    } else {
      ensureMultiTextEdit(targetTextChange);
      ReflectionUtils.invokeMethodEx(
          textEdit,
          "internalSetParent(org.eclipse.text.edits.TextEdit)",
          (Object) null);
      targetTextChange.addEdit(textEdit);
    }
  }

  /**
   * Ensures that given {@link TextChange} uses {@link MultiTextEdit}, wraps into it if this is not
   * so.
   */
  private static void ensureMultiTextEdit(TextChange textChange) {
    TextEdit textEdit = textChange.getEdit();
    if (!(textEdit instanceof MultiTextEdit)) {
      MultiTextEdit multiTextEdit = new MultiTextEdit();
      multiTextEdit.addChild(textEdit);
      //
      ReflectionUtils.setField(textChange, "fEdit", null);
      textChange.setEdit(multiTextEdit);
    }
  }

  /**
   * @return map of all {@link TextFileChange}-s in given composite {@link Change}.
   */
  private static Map<IFile, TextFileChange> getTextFileChanges(Change change) {
    Map<IFile, TextFileChange> textFileChanges = Maps.newHashMap();
    addTextFileChanges(textFileChanges, change);
    return textFileChanges;
  }

  /**
   * Adds entries for {@link TextFileChange}-s in given composite {@link Change}.
   */
  private static void addTextFileChanges(Map<IFile, TextFileChange> textFileChanges, Change change) {
    if (change instanceof CompositeChange) {
      CompositeChange compositeChange = (CompositeChange) change;
      for (Change child : compositeChange.getChildren()) {
        addTextFileChanges(textFileChanges, child);
      }
    } else if (change instanceof TextFileChange) {
      TextFileChange textFileChange = (TextFileChange) change;
      textFileChanges.put(textFileChange.getFile(), textFileChange);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XML change internal utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Change} for modifications in XML file done by {@link DocumentModelVisitor}.
   */
  public static Change modifyXML(IFile file,
      DocumentModelVisitor visitor,
      AbstractDocumentEditContext context) throws Exception {
    try {
      // fetch initial information
      String oldContents = context.getText();
      DocumentElement root = context.getRoot();
      // visit nodes
      root.accept(visitor);
      // prepare change
      String newContents = context.getText();
      if (!newContents.equals(oldContents)) {
        // prepare TextEdit as minimal list of replace changes
        MultiTextEdit multiTextEdit = new MultiTextEdit();
        {
          RangeDifference[] differences =
              RangeDifferencer.findDifferences(
                  new StringRangeComparator(oldContents),
                  new StringRangeComparator(newContents));
          for (RangeDifference difference : differences) {
            int rightStart = difference.rightStart();
            int rightEnd = difference.rightEnd();
            String text = newContents.substring(rightStart, rightEnd);
            int leftStart = difference.leftStart();
            int leftLength = difference.leftLength();
            multiTextEdit.addChild(new ReplaceEdit(leftStart, leftLength, text));
          }
        }
        // create text file change
        TextFileChange change = new TextFileChange("", file);
        change.setEdit(multiTextEdit);
        return change;
      }
      // no changes
      return null;
    } finally {
      context.disconnect();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StringRangeComparator
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class StringRangeComparator implements IRangeComparator {
    private final String m_string;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public StringRangeComparator(String string) {
      m_string = string;
      Assert.isTrue(!skipRangeComparison(0, 0, null));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IRangeComparator
    //
    ////////////////////////////////////////////////////////////////////////////
    public int getRangeCount() {
      return m_string.length();
    }

    public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
      StringRangeComparator otherComparator = (StringRangeComparator) other;
      return m_string.charAt(thisIndex) == otherComparator.m_string.charAt(otherIndex);
    }

    public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
      return false;
    }
  }
}
