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
package org.eclipse.wb.tests.designer.core.util.refactoring;

import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentEditContext;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * Tests for {@link RefactoringUtils}.
 * 
 * @author scheglov_ke
 */
public class RefactoringUtilsTest extends AbstractJavaTest {
  private static final IProgressMonitor PM = new NullProgressMonitor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // modifyXML()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for
   * {@link RefactoringUtils#modifyXML(IFile, DocumentModelVisitor, AbstractDocumentEditContext)}.
   */
  public void test_modifyXML_noChanges() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/my.xml",
            getSourceDQ(
                "<!-- filler filler filler filler filler -->",
                "<!-- filler filler filler filler filler -->",
                "<root/>"));
    Change change =
        RefactoringUtils.modifyXML(
            file,
            new DocumentModelVisitor(),
            new FileDocumentEditContext(file));
    assertSame(null, change);
  }

  /**
   * Test for
   * {@link RefactoringUtils#modifyXML(IFile, DocumentModelVisitor, AbstractDocumentEditContext)}.
   */
  public void test_modifyXML_doChanges() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/my.xml",
            getSourceDQ(
                "<!-- filler filler filler filler filler -->",
                "<!-- filler filler filler filler filler -->",
                "<root/>"));
    // prepare change
    Change change;
    {
      DocumentModelVisitor visitor = new DocumentModelVisitor() {
        @Override
        public void endVisit(DocumentElement element) {
          element.setAttribute("visited", "true");
        }
      };
      change = RefactoringUtils.modifyXML(file, visitor, new FileDocumentEditContext(file));
    }
    // no changes yet
    assertEquals(
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<root/>"),
        getFileContentSrc("test/my.xml"));
    // apply change
    change.perform(PM);
    assertEquals(
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<root visited='true'/>"),
        getFileContentSrc("test/my.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IType changes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RefactoringUtils#createDeleteTypeChange(IType)}.
   */
  public void test_createDeleteTypeChange() throws Exception {
    IType type =
        createModelType(
            "test",
            "Foo.java",
            getSource(
                "// filler filler filler filler filler",
                "package test;",
                "public class Foo {",
                "  public Foo() {",
                "  }",
                "}"));
    assertFileExists("src/test/Foo.java");
    //
    {
      Change change = RefactoringUtils.createDeleteTypeChange(type);
      assertNotNull(change);
      change.perform(PM);
    }
    assertFileNotExists("src/test/Foo.java");
  }

  /**
   * Test for {@link RefactoringUtils#createRenameTypeChange(IType, String, IProgressMonitor)}.
   */
  public void test_createRenameTypeChange() throws Exception {
    IType type =
        createModelType(
            "test",
            "Foo.java",
            getSource(
                "// filler filler filler filler filler",
                "package test;",
                "public class Foo {",
                "  public Foo() {",
                "  }",
                "}"));
    assertFileExists("src/test/Foo.java");
    //
    {
      Change change = RefactoringUtils.createRenameTypeChange(type, "Barr", PM);
      assertNotNull(change);
      change.perform(PM);
    }
    assertFileNotExists("src/test/Foo.java");
    assertFileExists("src/test/Barr.java");
    assertEquals(
        getSource(
            "// filler filler filler filler filler",
            "package test;",
            "public class Barr {",
            "  public Barr() {",
            "  }",
            "}"),
        getFileContentSrc("test/Barr.java"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // mergeTextChanges()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RefactoringUtils#mergeTextChanges(Change, Change)}.
   */
  public void test_mergeTextChanges_twoTextFileChanges() throws Exception {
    IFile file = setFileContent("Test.txt", "abc");
    // change 1
    TextFileChange fileChange_1;
    {
      fileChange_1 = new TextFileChange("Change 1", file);
      MultiTextEdit multiEdit = new MultiTextEdit();
      multiEdit.addChild(new ReplaceEdit(0, 1, "AA"));
      multiEdit.addChild(new ReplaceEdit(2, 1, "CC"));
      fileChange_1.setEdit(multiEdit);
    }
    // change 2
    TextFileChange fileChange_2;
    {
      fileChange_2 = new TextFileChange("Change 2", file);
      fileChange_2.setEdit(new ReplaceEdit(1, 1, "B"));
    }
    // prepare CompositeChange
    CompositeChange compositeChange = new CompositeChange("Composite change");
    compositeChange.add(fileChange_1);
    compositeChange.add(fileChange_2);
    // merge changes
    RefactoringUtils.mergeTextChanges(fileChange_1, fileChange_2);
    // apply
    compositeChange.perform(PM);
    assertEquals("AABCC", getFileContent(file));
  }

  /**
   * Test for {@link RefactoringUtils#mergeTextChanges(Change, Change)}.
   */
  public void test_mergeTextChanges_sourceIsCompositeChange() throws Exception {
    IFile file = setFileContent("Test.txt", "abc");
    // change 1
    CompositeChange change_1;
    {
      TextFileChange fileChange = new TextFileChange("Change 1", file);
      MultiTextEdit multiEdit = new MultiTextEdit();
      multiEdit.addChild(new ReplaceEdit(0, 1, "AA"));
      multiEdit.addChild(new ReplaceEdit(2, 1, "CC"));
      fileChange.setEdit(multiEdit);
      // use CompositeChange as "source"
      change_1 = new CompositeChange("CompositeChange 1");
      change_1.add(fileChange);
    }
    // change 2
    TextFileChange change_2;
    {
      change_2 = new TextFileChange("Change 2", file);
      change_2.setEdit(new ReplaceEdit(1, 1, "B"));
    }
    // prepare CompositeChange
    CompositeChange compositeChange = new CompositeChange("Composite change");
    compositeChange.add(change_1);
    compositeChange.add(change_2);
    // merge changes
    RefactoringUtils.mergeTextChanges(change_1, change_2);
    // apply
    compositeChange.perform(PM);
    assertEquals("AABCC", getFileContent(file));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // mergeTextChange(RefactoringParticipant)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RefactoringUtils#mergeTextChange(RefactoringParticipant, Change)}.
   */
  public void test_mergeTextChange() throws Exception {
    IFile file = setFileContent("Test.txt", "abc");
    // existing change
    TextFileChange existingChange;
    {
      existingChange = new TextFileChange("Existing change", file);
      existingChange.setEdit(new ReplaceEdit(0, 1, "AA"));
    }
    // prepare CompositeChange with "new change"
    CompositeChange newChangeComposite = new CompositeChange("CC with new");
    {
      TextFileChange newChange = new TextFileChange("New change", file);
      newChange.setEdit(new ReplaceEdit(2, 1, "C"));
      newChangeComposite.add(newChange);
    }
    assertThat(newChangeComposite.getChildren()).hasSize(1);
    // expectations
    IMocksControl mocksControl = EasyMock.createControl();
    RefactoringParticipant participant = mocksControl.createMock(RefactoringParticipant.class);
    expect(participant.getTextChange(file)).andReturn(existingChange);
    // reply
    mocksControl.replay();
    RefactoringUtils.mergeTextChange(participant, newChangeComposite);
    mocksControl.verify();
    // perform change
    existingChange.perform(PM);
    assertEquals("AAbC", getFileContent(file));
    // "new change" was removed from "composite"
    assertThat(newChangeComposite.getChildren()).hasSize(0);
  }
}
