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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.editor.UndoManager;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Test for {@link UndoManager}.
 *
 * @author scheglov_ke
 */
public class UndoManagerTest extends XwtGefTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_emptyShell() throws Exception {
		openEditor(
				"<!-- filler filler filler filler filler -->",
				"<!-- filler filler filler filler filler -->",
				"<Shell text='My Shell'/>");
		// check that fields fetched correctly
		assertNotNull(m_lastObject);
		assertSame(m_lastObject, m_contentObject);
		assertNotNull(canvas);
		assertNotNull(tree);
		assertSame(m_contentEditPart, canvas.getEditPart(m_contentObject));
		// initial size
		{
			Rectangle contentEditPartBounds = m_contentEditPart.getFigure().getBounds();
			assertEquals(450, contentEditPartBounds.width);
			assertEquals(300, contentEditPartBounds.height);
		}
		// no selection initially
		canvas.assertSelectedEmpty();
		tree.assertSelectedEmpty();
		// click Shell on canvas
		canvas.moveTo(m_lastObject, 100, 100).click();
		canvas.assertPrimarySelected(m_lastObject);
		tree.assertPrimarySelected(m_lastObject);
	}

	public void test_reparseOnDocumentChange() throws Exception {
		openEditor(
				"<!-- filler filler filler filler filler -->",
				"<!-- filler filler filler filler filler -->",
				"<Shell text='My Shell'/>");
		XmlObjectInfo initialObject = m_lastObject;
		// initially "text" value
		{
			Property property = m_lastObject.getPropertyByTitle("text");
			assertEquals("My Shell", property.getValue());
		}
		// change IDocument, so reparse should be done
		{
			IDocument document = m_lastContext.getDocument();
			int offset = document.get().indexOf("My ");
			document.replace(offset, "My".length(), "Some");
		}
		// fetch to get new object
		fetchContentFields();
		// new object and new "text"
		assertNotSame(initialObject, m_lastObject);
		{
			Property property = m_lastObject.getPropertyByTitle("text");
			assertEquals("Some Shell", property.getValue());
		}
	}

	public void test_reparse_onUndo_onRedo() throws Exception {
		openEditor("<Shell text='foo'/>");
		// initial "text"
		assertEquals("foo", m_lastObject.getPropertyByTitle("text").getValue());
		// set new "text"
		m_lastObject.getPropertyByTitle("text").setValue("bar");
		assertEquals("bar", m_lastObject.getPropertyByTitle("text").getValue());
		// do "undo"
		{
			getUndoAction().run();
			fetchContentFields();
			// still old/initial "text"
			assertEquals("foo", m_lastObject.getPropertyByTitle("text").getValue());
		}
		// do "redo"
		{
			getRedoAction().run();
			fetchContentFields();
			// again new "text"
			assertEquals("bar", m_lastObject.getPropertyByTitle("text").getValue());
		}
	}

	public void test_noReparse_afterChangeOnDesign() throws Exception {
		openEditor("<Shell text='foo'/>");
		// set new "text"
		XmlObjectInfo object = m_lastObject;
		object.getPropertyByTitle("text").setValue("bar");
		// switch to Source and back to Design
		m_designerEditor.showSource();
		m_designerEditor.showDesign();
		// reparse should not happen
		fetchContentFields();
		assertSame(object, m_lastObject);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Restore selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that we restore selection after reparse.
	 */
	public void test_restoreSelection() throws Exception {
		openEditor(
				"<!-- filler filler filler filler filler -->",
				"<!-- filler filler filler filler filler -->",
				"<Shell text='Hello!'>",
				"  <Shell.layout>",
				"    <RowLayout/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button_1' text='Button 1'/>",
				"  <Button wbp:name='button_2' text='Button 2'/>",
				"</Shell>");
		// select "button_2"
		{
			XmlObjectInfo button_2 = getObjectByName("button_2");
			tree.select(button_2);
			tree.assertPrimarySelected(button_2);
		}
		// reparse
		{
			XmlObjectInfo oldObject = m_lastObject;
			IDesignPageSite.Helper.getSite(m_lastObject).reparse();
			fetchContentFields();
			assertNotSame(oldObject, m_lastObject);
		}
		// assert that selection of "button_2" was restored
		{
			XmlObjectInfo button_2 = getObjectByName("button_2");
			tree.assertPrimarySelected(button_2);
		}
	}

	/**
	 * Following scenario should work:
	 * <ol>
	 * <li>Select "button";</li>
	 * <li>Delete "button", it disappears;</li>
	 * <li>Undo;</li>
	 * <li>"button" should be visible and selected.</li>
	 * </ol>
	 */
	public void test_restoreSelection_afterDelete_thenUndo() throws Exception {
		openEditor(
				"<!-- filler filler filler filler filler -->",
				"<!-- filler filler filler filler filler -->",
				"<Shell text='Hello!'>",
				"  <Shell.layout>",
				"    <RowLayout/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		// select "button" and delete
		{
			XmlObjectInfo button = getObjectByName("button");
			tree.select(button);
			tree.assertPrimarySelected(button);
			button.delete();
		}
		// undo
		getUndoAction().run();
		// "button" exists and selected
		fetchContentFields();
		XmlObjectInfo button = getObjectByName("button");
		tree.assertPrimarySelected(button);
		canvas.assertPrimarySelected(button);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expanded
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that when we select component, its parent become expanded.
	 */
	public void test_expandOnSelection() throws Exception {
		XmlObjectInfo shell =
				openEditor(
						"<!-- filler filler filler filler filler -->",
						"<!-- filler filler filler filler filler -->",
						"<Shell text='Hello!'>",
						"  <Shell.layout>",
						"    <RowLayout/>",
						"  </Shell.layout>",
						"  <Button wbp:name='button' text='Button'/>",
						"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		// collapse all
		{
			tree.collapseAll();
			tree.assertNotExpandedObjects(shell);
		}
		// select "button", "shell" should be expanded
		{
			canvas.select(button);
			tree.assertExpandedObjects(shell);
		}
	}

	/**
	 * When we open editor, it automatically expands components from root, if "parent" has only one
	 * "child".
	 * <p>
	 * Here we have {@link Shell} with single {@link Button}, so {@link Shell} should be expanded.
	 */
	public void test_expandOnOpen_1() throws Exception {
		XmlObjectInfo shell =
				openEditor(
						"<!-- filler filler filler filler filler -->",
						"<!-- filler filler filler filler filler -->",
						"<Shell text='Hello!'>",
						"  <Shell.layout>",
						"    <FillLayout/>",
						"  </Shell.layout>",
						"  <Button wbp:name='button' text='Button'/>",
						"</Shell>");
		// "shell" should be expanded
		tree.assertExpandedObjects(shell);
	}

	/**
	 * When we open editor, it automatically expands components from root, if "parent" has only one
	 * "child".
	 * <p>
	 * Here we have {@link Shell} with {@link Composite}, with single {@link Button}, so {@link Shell}
	 * and {@link Composite} should be expanded.
	 */
	public void test_expandOnOpen_2() throws Exception {
		XmlObjectInfo shell =
				openEditor(
						"<Shell text='Hello!'>",
						"  <Shell.layout>",
						"    <FillLayout/>",
						"  </Shell.layout>",
						"  <Composite wbp:name='composite'>",
						"    <Shell.layout>",
						"      <RowLayout/>",
						"    </Shell.layout>",
						"    <Button text='Button'/>",
						"  </Composite>",
						"</Shell>");
		XmlObjectInfo composite = getObjectByName("composite");
		// "shell" and "composite" should be expanded
		tree.assertExpandedObjects(shell, composite);
	}

	/**
	 * When we open editor, it automatically expands components from root, if "parent" has only one
	 * "child".
	 * <p>
	 * Here we have {@link Shell} with two {@link Composite}s, so only {@link Shell} should be
	 * expanded.
	 */
	public void test_expandOnOpen_3() throws Exception {
		XmlObjectInfo shell =
				openEditor(
						"<Shell text='Hello!'>",
						"  <Shell.layout>",
						"    <FillLayout/>",
						"  </Shell.layout>",
						"  <Composite wbp:name='composite_1'>",
						"    <Shell.layout>",
						"      <RowLayout/>",
						"    </Shell.layout>",
						"    <Button text='Button'/>",
						"  </Composite>",
						"  <Composite wbp:name='composite_2'>",
						"    <Shell.layout>",
						"      <RowLayout/>",
						"    </Shell.layout>",
						"    <Button text='Button'/>",
						"  </Composite>",
						"</Shell>");
		// only "shell" should be expanded
		tree.assertExpandedObjects(shell);
	}

	/**
	 * Test that after reparse expanded state it restored.
	 */
	public void test_restoreExpanded() throws Exception {
		openEditor(
				"<Shell text='Hello!'>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <Composite wbp:name='composite_1'>",
				"    <Shell.layout>",
				"      <RowLayout/>",
				"    </Shell.layout>",
				"    <Button text='Button' wbp:name='button_1'/>",
				"  </Composite>",
				"  <Composite wbp:name='composite_2'>",
				"    <Shell.layout>",
				"      <RowLayout/>",
				"    </Shell.layout>",
				"    <Button text='Button'/>",
				"  </Composite>",
				"</Shell>");
		{
			XmlObjectInfo shell = m_lastObject;
			XmlObjectInfo composite_1 = getObjectByName("composite_1");
			XmlObjectInfo composite_2 = getObjectByName("composite_2");
			XmlObjectInfo button_1 = getObjectByName("button_1");
			// initially only "shell" is expanded
			tree.assertExpandedObjects(shell);
			// expand all
			tree.select(button_1);
			waitEventLoop(0);
			tree.assertExpandedObjects(shell, composite_1);
			tree.assertNotExpandedObjects(composite_2);
		}
		// reparse
		IDesignPageSite.Helper.getSite(m_lastObject).reparse();
		fetchContentFields();
		{
			XmlObjectInfo shell = m_lastObject;
			XmlObjectInfo composite_1 = getObjectByName("composite_1");
			XmlObjectInfo composite_2 = getObjectByName("composite_2");
			// expanded state restored
			tree.assertExpandedObjects(shell, composite_1);
			tree.assertNotExpandedObjects(composite_2);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isDirty()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for "dirty" flag management.
	 */
	public void test_isDirty_undo() throws Exception {
		openEditor("<Shell text='foo'/>");
		// initially not dirty
		assertFalse(m_designerEditor.isDirty());
		// set new "text"
		m_lastObject.getPropertyByTitle("text").setValue("bar");
		assertTrue(m_designerEditor.isDirty());
		// do "undo", again not dirty
		getUndoAction().run();
		assertFalse(m_designerEditor.isDirty());
	}

	/**
	 * Test for "dirty" flag management.
	 */
	public void test_isDirty_save() throws Exception {
		openEditor("<Shell text='foo'/>");
		// initially not dirty
		assertFalse(m_designerEditor.isDirty());
		// set new "text"
		m_lastObject.getPropertyByTitle("text").setValue("bar");
		assertTrue(m_designerEditor.isDirty());
		// do save
		m_designerEditor.doSave(new NullProgressMonitor());
		assertFalse(m_designerEditor.isDirty());
	}
}
