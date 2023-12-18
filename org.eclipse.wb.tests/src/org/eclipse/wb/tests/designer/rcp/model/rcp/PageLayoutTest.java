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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.SelectionToolEntryInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.AbstractPartInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.EditorAreaInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.FolderViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.IPageLayoutTopLevelInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddCreationSupport;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreateFolderInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreationSupport;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutTopBoundsSupport;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.SashLineInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.AbstractShortcutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class PageLayoutTest extends RcpModelTest {
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
	/**
	 * No reason to edit "perspective" palette.
	 */
	@Test
	public void test_canNotEditPalette() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		//
		boolean[] canEdit = {true};
		page.getBroadcast(PaletteEventListener.class).canEdit(canEdit);
		assertFalse(canEdit[0]);
	}

	/**
	 * Test for most generic {@link IPageLayout} parsing.
	 */
	@Test
	public void test_0() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    addFastViews(layout);",
						"    addViewShortcuts(layout);",
						"    addPerspectiveShortcuts(layout);",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.LEFT, 0.3f, editorArea);",
						"    layout.addView('org.eclipse.jdt.ui.TypeHierarchy', IPageLayout.BOTTOM, 0.7f, editorArea);",
						"  }",
						"  private void addFastViews(IPageLayout layout) {",
						"  }",
						"  private void addViewShortcuts(IPageLayout layout) {",
						"  }",
						"  private void addPerspectiveShortcuts(IPageLayout layout) {",
						"  }",
						"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /addFastViews(layout)/ /addViewShortcuts(layout)/ /addPerspectiveShortcuts(layout)/ /layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.LEFT, 0.3f, editorArea)/ /layout.addView('org.eclipse.jdt.ui.TypeHierarchy', IPageLayout.BOTTOM, 0.7f, editorArea)/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.LEFT, 0.3f, editorArea)/}",
				"  {void} {void} {/layout.addView('org.eclipse.jdt.ui.TypeHierarchy', IPageLayout.BOTTOM, 0.7f, editorArea)/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		// check containers
		assertEquals("(editor area)", page.getEditorArea().toString());
		assertEquals("(fast views)", page.getFastViewContainer().toString());
		assertEquals("(view shortcuts)", page.getViewShortcutContainer().toString());
		assertEquals("(perspective shortcuts)", page.getPerspectiveShortcutContainer().toString());
		// check parts
		{
			List<AbstractPartInfo> parts = page.getParts();
			Assertions.assertThat(parts).hasSize(2);
		}
	}

	/**
	 * Test for {@link PageLayoutCreationSupport}.
	 */
	@Test
	public void test_PageLayout_CreationSupport() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		PageLayoutCreationSupport creationSupport =
				(PageLayoutCreationSupport) page.getCreationSupport();
		// node
		{
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) creationSupport.getNode();
			assertEquals("layout", parameter.getName().getIdentifier());
		}
		// access
		assertEquals("parameter: layout", creationSupport.toString());
		assertFalse(creationSupport.canBeEvaluated());
		// validation
		assertFalse(creationSupport.canDelete());
		assertFalse(creationSupport.canReorder());
		assertFalse(creationSupport.canReparent());
	}

	/**
	 * Test for {@link PageLayoutTopBoundsSupport}.
	 */
	@Test
	public void test_PageLayout_TopBoundsSupport() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		PageLayoutTopBoundsSupport topBoundsSupport =
				(PageLayoutTopBoundsSupport) page.getTopBoundsSupport();
		// refresh
		page.refresh();
		assertEquals(600, page.getBounds().width);
		assertEquals(500, page.getBounds().height);
		// set size
		topBoundsSupport.setSize(800, 600);
		page.refresh();
		assertEquals(800, page.getBounds().width);
		assertEquals(600, page.getBounds().height);
	}

	/**
	 * Test for {@link EditorAreaInfo}.
	 */
	@Test
	public void test_EditorArea() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		refresh();
		// check EditorArea_Info
		EditorAreaInfo editorArea = page.getEditorArea();
		assertSame(page, editorArea.getPage());
		assertEquals(IPageLayout.ID_EDITOR_AREA, editorArea.getId());
		assertEquals("org.eclipse.ui.IPageLayout.ID_EDITOR_AREA", editorArea.getIdSource());
		assertEquals("(editor area)", editorArea.toString());
		// presentation
		{
			IObjectPresentation presentation = editorArea.getPresentation();
			assertNotNull(presentation.getIcon());
			assertEquals("(editor area)", presentation.getText());
		}
		// bounds
		{
			Rectangle bounds = editorArea.getBounds();
			Assertions.assertThat(bounds.x).isEqualTo(0);
			Assertions.assertThat(bounds.y).isEqualTo(0);
			Assertions.assertThat(bounds.width).isGreaterThan(550);
			Assertions.assertThat(bounds.height).isGreaterThan(450);
		}
	}

	/**
	 * We should render perspective a little different when "editorAreaVisible == false".
	 */
	@Test
	public void test_editorAreaVisible_false() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.setEditorAreaVisible(false);",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.ui.console.ConsoleView', IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		EditorAreaInfo editorArea = page.getEditorArea();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		{
			Rectangle editorBounds = editorArea.getBounds();
			Rectangle viewBounds = view.getBounds();
			// editor bounds
			Assertions.assertThat(editorBounds.x).isGreaterThan(550);
			Assertions.assertThat(editorBounds.y).isEqualTo(0);
			Assertions.assertThat(editorBounds.width).isEqualTo(0);
			Assertions.assertThat(editorBounds.height).isGreaterThan(450);
			// view bounds
			Assertions.assertThat(viewBounds.x).isEqualTo(0);
			Assertions.assertThat(viewBounds.y).isEqualTo(0);
			Assertions.assertThat(viewBounds.width).isGreaterThan(450);
			Assertions.assertThat(viewBounds.height).isEqualTo(editorBounds.height);
		}
	}

	/**
	 * Test for {@link IPageLayout} properties.
	 */
	@Test
	public void test_properties() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		// check properties
		{
			Property property = page.getPropertyByTitle("editorAreaVisible");
			assertNotNull(property);
			assertTrue((Boolean) property.getValue());
		}
		{
			Property property = page.getPropertyByTitle("editorReuseThreshold");
			assertNotNull(property);
		}
		{
			Property property = page.getPropertyByTitle("fixed");
			assertNotNull(property);
			assertFalse((Boolean) property.getValue());
		}
	}

	/**
	 * Test for {@link IPageLayout#addView(String, int, float, String)} and its
	 * {@link PageLayoutAddCreationSupport}.
	 */
	@Test
	public void test_PageLayout_add_CreationSupport() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.LEFT, 0.3f, editorArea);",
						"  }",
						"}");
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// access
		assertSame(page, view.getPage());
		assertEquals("\"org.eclipse.jdt.ui.PackageExplorer\"", view.getIdSource());
		// check MethodInvocation access
		{
			assertEquals("org.eclipse.jdt.ui.PackageExplorer", view.getId());
			assertEquals(IPageLayout.LEFT, view.getRelationship());
			assertEquals(0.3f, view.getRatio(), 0.001f);
			assertEquals(IPageLayout.ID_EDITOR_AREA, view.getRefId());
			assertEquals("editorArea", view.getRefIdSource());
		}
		// check presentation
		{
			IObjectPresentation presentation = view.getPresentation();
			assertNotNull(presentation.getIcon());
			assertEquals(
					"\"Package Explorer\" - org.eclipse.jdt.ui.PackageExplorer",
					presentation.getText());
		}
		// check PageLayout_add_CreationSupport
		{
			PageLayoutAddCreationSupport creationSupport =
					(PageLayoutAddCreationSupport) view.getCreationSupport();
			assertTrue(creationSupport.canReorder());
			assertTrue(creationSupport.canReparent());
			// this PageLayout_add_CreationSupport was created without source
			try {
				NodeTarget nodeTarget =
						getNodeStatementTarget(
								page,
								"createInitialLayout(org.eclipse.ui.IPageLayout)",
								false,
								1);
				creationSupport.add_getSource(nodeTarget);
				fail();
			} catch (AssertionFailedException e) {
			}
		}
	}

	/**
	 * Test for {@link SashLineInfo}'s.
	 */
	@Test
	public void test_sashLines() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_0', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.addView('view_1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.addView('view_2', IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.addView('view_3', IPageLayout.RIGHT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_0 = (PageLayoutAddViewInfo) page.getParts().get(0);
		PageLayoutAddViewInfo view_1 = (PageLayoutAddViewInfo) page.getParts().get(1);
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(2);
		PageLayoutAddViewInfo view_3 = (PageLayoutAddViewInfo) page.getParts().get(3);
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		// prepare sash lines
		SashLineInfo line_0 = view_0.getSashLine();
		SashLineInfo line_1 = view_1.getSashLine();
		SashLineInfo line_2 = view_2.getSashLine();
		SashLineInfo line_3 = view_3.getSashLine();
		// check back reference on AbstractPart_Info
		assertSame(view_0, line_0.getPart());
		assertSame(view_1, line_1.getPart());
		assertSame(view_2, line_2.getPart());
		assertSame(view_3, line_3.getPart());
		/*System.out.println(line_0);
    System.out.println(line_1);
    System.out.println(line_2);
    System.out.println(line_3);*/
		// prepare line bounds
		Rectangle bounds_0 = line_0.getBounds();
		Rectangle bounds_1 = line_1.getBounds();
		Rectangle bounds_2 = line_2.getBounds();
		Rectangle bounds_3 = line_3.getBounds();
		// check each line
		{
			SashLineInfo line = line_0;
			Assertions.assertThat(line.toString()).contains("view_0").contains("true");
			assertSame(view_0, line.getPart());
			assertEquals(PositionConstants.EAST, line.getPosition());
			assertTrue(line.isHorizontal());
			// line bounds
			Assertions.assertThat(bounds_0.x).isGreaterThan(150);
			Assertions.assertThat(bounds_0.y).isZero();
			Assertions.assertThat(bounds_0.width).isEqualTo(SashLineInfo.SASH_SIZE);
			Assertions.assertThat(bounds_0.height).isGreaterThanOrEqualTo(450);
			// part bounds
			Assertions.assertThat(line_0.getPartBounds().x).isZero();
			Assertions.assertThat(line_0.getPartBounds().y).isZero();
			Assertions.assertThat(line_0.getPartBounds().right()).isEqualTo(bounds_0.x);
			Assertions.assertThat(line_0.getPartBounds().height).isEqualTo(bounds_0.height);
			// ref bounds
			Assertions.assertThat(line_0.getRefBounds().x).isZero();
			Assertions.assertThat(line_0.getRefBounds().y).isZero();
			Assertions.assertThat(line_0.getRefBounds().width).isGreaterThan(550);
			Assertions.assertThat(line_0.getRefBounds().height).isGreaterThan(450);
		}
		{
			SashLineInfo line = line_1;
			Assertions.assertThat(line.toString()).contains("view_1").contains("false");
			assertSame(view_1, line.getPart());
			assertEquals(PositionConstants.SOUTH, line.getPosition());
			assertFalse(line.isHorizontal());
			// line bounds
			Assertions.assertThat(bounds_1.x).isEqualTo(bounds_0.x + bounds_0.width);
			Assertions.assertThat(bounds_1.y).isGreaterThan(130);
			Assertions.assertThat(bounds_1.width).isGreaterThan(350);
			Assertions.assertThat(bounds_1.height).isEqualTo(SashLineInfo.SASH_SIZE);
		}
		{
			SashLineInfo line = line_2;
			Assertions.assertThat(line.toString()).contains("view_2").contains("false");
			assertSame(view_2, line.getPart());
			assertEquals(PositionConstants.NORTH, line.getPosition());
			assertFalse(line.isHorizontal());
			// line bounds
			Assertions.assertThat(bounds_2.x).isEqualTo(bounds_1.x);
			Assertions.assertThat(bounds_2.height).isEqualTo(SashLineInfo.SASH_SIZE);
			Assertions.assertThat(bounds_2.width).isEqualTo(bounds_1.width);
		}
		{
			SashLineInfo line = line_3;
			Assertions.assertThat(line.toString()).contains("view_3").contains("true");
			assertSame(view_3, line.getPart());
			assertEquals(PositionConstants.WEST, line.getPosition());
			assertTrue(line.isHorizontal());
			// line bounds
			Assertions.assertThat(bounds_3.y).isEqualTo(bounds_1.y + bounds_1.height);
			Assertions.assertThat(bounds_3.width).isEqualTo(SashLineInfo.SASH_SIZE);
			Assertions.assertThat(bounds_3.height).isEqualTo(bounds_2.y - (bounds_1.y + bounds_1.height));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// addView()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Perspective elements don't have component class in {@link ComponentDescription}, so this caused
	 * {@link NullPointerException} during deducing settings.
	 */
	@Test
	public void test_deduceSettings() throws Exception {
		{
			GenerationSettings generationSettings =
					org.eclipse.wb.internal.rcp.ToolkitProvider.DESCRIPTION.getGenerationSettings();
			generationSettings.setDeduceSettings(true);
		}
		parsePerspective(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"    layout.addView('0', IPageLayout.LEFT, 0.3f, editorArea);",
				"    layout.addView('1', IPageLayout.LEFT, 0.3f, editorArea);",
				"    layout.addView('2', IPageLayout.LEFT, 0.3f, editorArea);",
				"    layout.addView('3', IPageLayout.LEFT, 0.3f, editorArea);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link IPageLayout#addView(String, int, float, String)}.<br> {@link IPageLayout#LEFT}
	 * relationship.
	 */
	@Test
	public void test_addView_LEFT() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.LEFT, 0.3f, editorArea);",
						"  }",
						"}");
		EditorAreaInfo editorArea = page.getEditorArea();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		{
			Rectangle editorBounds = editorArea.getBounds();
			Rectangle viewBounds = view.getBounds();
			// view bounds
			Assertions.assertThat(viewBounds.x).isEqualTo(0);
			Assertions.assertThat(viewBounds.y).isEqualTo(0);
			Assertions.assertThat(viewBounds.width).isGreaterThan(150);
			Assertions.assertThat(viewBounds.height).isGreaterThan(450);
			// editor bounds
			Assertions.assertThat(editorBounds.x).isEqualTo(viewBounds.right() + SashLineInfo.SASH_SIZE);
			Assertions.assertThat(editorBounds.y).isEqualTo(0);
			Assertions.assertThat(editorBounds.height).isEqualTo(viewBounds.height);
			// relationship for editor/view width
			assertPartsSizes(viewBounds.width, editorBounds.width, 0.3, 0.7);
		}
	}

	/**
	 * Test for {@link IPageLayout#addView(String, int, float, String)}.<br> {@link IPageLayout#RIGHT}
	 * relationship.
	 */
	@Test
	public void test_addView_RIGHT() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.RIGHT, 0.7f, editorArea);",
						"  }",
						"}");
		EditorAreaInfo editorArea = page.getEditorArea();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		{
			Rectangle editorBounds = editorArea.getBounds();
			Rectangle viewBounds = view.getBounds();
			// editor bounds
			Assertions.assertThat(editorBounds.x).isEqualTo(0);
			Assertions.assertThat(editorBounds.y).isEqualTo(0);
			Assertions.assertThat(editorBounds.width).isGreaterThan(150);
			Assertions.assertThat(editorBounds.height).isGreaterThan(450);
			// view bounds
			Assertions.assertThat(viewBounds.x).isEqualTo(editorBounds.right() + SashLineInfo.SASH_SIZE);
			Assertions.assertThat(viewBounds.y).isEqualTo(0);
			Assertions.assertThat(viewBounds.height).isEqualTo(editorBounds.height);
			// relationship for editor/view width
			assertPartsSizes(viewBounds.width, editorBounds.width, 0.3, 0.7);
		}
	}

	/**
	 * Test for {@link IPageLayout#addView(String, int, float, String)}.<br> {@link IPageLayout#TOP}
	 * relationship.
	 */
	@Test
	public void test_addView_TOP() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.TOP, 0.3f, editorArea);",
						"  }",
						"}");
		EditorAreaInfo editorArea = page.getEditorArea();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		{
			Rectangle editorBounds = editorArea.getBounds();
			Rectangle viewBounds = view.getBounds();
			// view bounds
			Assertions.assertThat(viewBounds.x).isEqualTo(0);
			Assertions.assertThat(viewBounds.y).isEqualTo(0);
			Assertions.assertThat(viewBounds.width).isGreaterThan(500);
			Assertions.assertThat(viewBounds.height).isGreaterThan(100);
			// editor bounds
			Assertions.assertThat(editorBounds.x).isEqualTo(0);
			Assertions.assertThat(editorBounds.y).isEqualTo(viewBounds.bottom() + SashLineInfo.SASH_SIZE);
			Assertions.assertThat(editorBounds.width).isEqualTo(viewBounds.width);
			// relationship for editor/view height
			assertPartsSizes(viewBounds.height, editorBounds.height, 0.3, 0.7);
		}
	}

	/**
	 * Test for {@link IPageLayout#addView(String, int, float, String)}.<br> {@link IPageLayout#BOTTOM}
	 * relationship.
	 */
	@Test
	public void test_addView_BOTTOM() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.BOTTOM, 0.7f, editorArea);",
						"  }",
						"}");
		EditorAreaInfo editorArea = page.getEditorArea();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		{
			Rectangle editorBounds = editorArea.getBounds();
			Rectangle viewBounds = view.getBounds();
			// editor bounds
			Assertions.assertThat(editorBounds.x).isEqualTo(0);
			Assertions.assertThat(editorBounds.y).isEqualTo(0);
			Assertions.assertThat(editorBounds.width).isGreaterThan(550);
			Assertions.assertThat(editorBounds.height).isGreaterThan(300);
			// view bounds
			Assertions.assertThat(viewBounds.x).isEqualTo(0);
			Assertions.assertThat(viewBounds.y).isEqualTo(editorBounds.bottom() + SashLineInfo.SASH_SIZE);
			Assertions.assertThat(viewBounds.width).isEqualTo(editorBounds.width);
			// relationship for editor/view height
			assertPartsSizes(viewBounds.height, editorBounds.height, 0.3, 0.7);
		}
	}

	/**
	 * Eclipse allows to use "view" inside of {@link IFolderLayout} as reference.
	 */
	@Test
	public void test_addView_viewInFolderAsReference() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"    layout.addView('view.2', IPageLayout.LEFT, 0.3f, 'view.1');",
						"  }",
						"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea)/ /layout.addView('view.2', IPageLayout.LEFT, 0.3f, 'view.1')/}",
				"  (editor area)",
				"  {void} {local-unique: folder} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea)/ /folder.addView('view.1')/}",
				"    {void} {void} {/folder.addView('view.1')/}",
				"  {void} {void} {/layout.addView('view.2', IPageLayout.LEFT, 0.3f, 'view.1')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		page.refresh();
		// "folder"
		{
			AbstractPartInfo folder = page.getParts().get(0);
			assertNotNull(folder.getSashLine());
		}
		// "view.2", should have "line", i.e. layout performed
		{
			AbstractPartInfo view2 = page.getParts().get(1);
			assertNotNull(view2.getSashLine());
			{
				Rectangle bounds = view2.getBounds();
				Assertions.assertThat(bounds.x).isEqualTo(0);
				Assertions.assertThat(bounds.y).isEqualTo(0);
				Assertions.assertThat(bounds.width).isGreaterThan(150);
				Assertions.assertThat(bounds.height).isGreaterThan(150);
			}
		}
	}

	/**
	 * Test for {@link IPageLayout#addView(String, int, float, String)}.<br>
	 * Invalid relationship.
	 */
	@Test
	public void test_addView_invalidRelatioship() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', -555, 0.7f, editorArea);",
						"  }",
						"}");
		// refresh()
		try {
			page.refresh();
		} catch (IllegalArgumentException e) {
			Assertions.assertThat(e.getMessage()).contains("relationship");
		}
	}

	/**
	 * Asserts that two parts sizes are distributed according given coefficients.
	 */
	private static void assertPartsSizes(int value_1, int value_2, double k_1, double k_2) {
		Assertions.assertThat(Math.abs(value_1 / k_1 - value_2 / k_2) - SashLineInfo.SASH_SIZE).isEqualTo(
				0.0,
				Offset.offset(2.0));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shortcuts
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_shortcuts_0() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addFastViews(layout);",
						"  }",
						"  private void addFastViews(IPageLayout layout) {",
						"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER)/ /addFastViews(layout)/}",
				"  (editor area)",
				"  (fast views)",
				"    {void} {empty} {/layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER)/}",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		FastViewContainerInfo container = page.getFastViewContainer();
		// OK, refresh() to check later all, including rendering
		page.refresh();
		// test FastViewContainer_Info itself
		{
			assertEquals("(fast views)", container.toString());
			assertSame(page, container.getPage());
			{
				IObjectPresentation presentation = container.getPresentation();
				assertNotNull(presentation.getIcon());
				assertEquals("(fast views)", presentation.getText());
			}
			{
				Rectangle bounds = container.getBounds();
				Assertions.assertThat(bounds.x).isEqualTo(0);
				Assertions.assertThat(bounds.y).isGreaterThan(450);
				Assertions.assertThat(bounds.width).isGreaterThan(550);
				Assertions.assertThat(bounds.height).isGreaterThan(20);
			}
		}
		// check fast views
		{
			FastViewInfo view;
			{
				List<AbstractShortcutInfo> shortcuts = container.getShortcuts();
				Assertions.assertThat(shortcuts).hasSize(1);
				view = (FastViewInfo) shortcuts.get(0);
			}
			// access
			assertEquals(IPageLayout.ID_PROJECT_EXPLORER, view.getId());
			// presentation
			{
				IObjectPresentation presentation = view.getPresentation();
				assertNotNull(presentation.getIcon());
				assertEquals(
						"\"Project Explorer\" - org.eclipse.ui.navigator.ProjectExplorer",
						presentation.getText());
			}
			// bounds
			{
				Rectangle bounds = view.getBounds();
				Assertions.assertThat(bounds.x).isGreaterThan(0);
				Assertions.assertThat(bounds.y).isGreaterThan(0);
				Assertions.assertThat(bounds.width).isGreaterThan(20);
				Assertions.assertThat(bounds.height).isGreaterThan(20);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FastView commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link FastViewContainerInfo#command_CREATE(String, FastViewInfo)}.<br>
	 * Before existing {@link FastViewInfo}.
	 */
	@Test
	public void test_fastView_CREATE_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addFastViews(layout);",
						"  }",
						"  private void addFastViews(IPageLayout layout) {",
						"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
						"  }",
						"}");
		page.refresh();
		FastViewContainerInfo container = page.getFastViewContainer();
		FastViewInfo nextItem = (FastViewInfo) container.getShortcuts().get(0);
		assertNotNull(ObjectInfoUtils.getId(nextItem));
		// do CREATE
		FastViewInfo item = container.command_CREATE("org.eclipse.jdt.ui.PackageExplorer", nextItem);
		assertNotNull(ObjectInfoUtils.getId(item));
		// check source
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addFastViews(layout);",
				"  }",
				"  private void addFastViews(IPageLayout layout) {",
				"    layout.addFastView('org.eclipse.jdt.ui.PackageExplorer');",
				"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
				"  }",
				"}");
		// check shortcut
		{
			assertSame(container, item.getParent());
			Assertions.assertThat(container.getChildren()).contains(item);
			Assertions.assertThat(item.getCreationSupport()).isInstanceOf(PageLayoutAddCreationSupport.class);
			Assertions.assertThat(item.getVariableSupport()).isInstanceOf(EmptyPureVariableSupport.class);
		}
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER)/ /addFastViews(layout)/ /layout.addFastView('org.eclipse.jdt.ui.PackageExplorer')/}",
				"  (editor area)",
				"  (fast views)",
				"    {void} {empty} {/layout.addFastView('org.eclipse.jdt.ui.PackageExplorer')/}",
				"    {void} {empty} {/layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER)/}",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
	}

	/**
	 * Test for {@link FastViewContainerInfo#command_CREATE(String, FastViewInfo)}.<br>
	 * No <code>addFastViews</code> method, create it.
	 */
	@Test
	public void test_fastView_CREATE_2() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		FastViewContainerInfo container = page.getFastViewContainer();
		// do CREATE
		FastViewInfo item = container.command_CREATE("org.eclipse.jdt.ui.PackageExplorer", null);
		// check source
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addFastViews(layout);",
				"  }",
				"  private void addFastViews(IPageLayout layout) {",
				"    layout.addFastView('org.eclipse.jdt.ui.PackageExplorer');",
				"  }",
				"}");
		// check shortcut
		{
			assertSame(container, item.getParent());
			Assertions.assertThat(container.getChildren()).contains(item);
			Assertions.assertThat(item.getCreationSupport()).isInstanceOf(PageLayoutAddCreationSupport.class);
			Assertions.assertThat(item.getVariableSupport()).isInstanceOf(EmptyPureVariableSupport.class);
		}
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addFastView('org.eclipse.jdt.ui.PackageExplorer')/}",
				"  (editor area)",
				"  (fast views)",
				"    {void} {empty} {/layout.addFastView('org.eclipse.jdt.ui.PackageExplorer')/}",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
	}

	/**
	 * Test for {@link FastViewContainerInfo#command_MOVE(FastViewInfo, FastViewInfo)}.<br>
	 * Before existing {@link FastViewInfo}.
	 */
	@Test
	public void test_fastView_MOVE_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addFastViews(layout);",
						"  }",
						"  private void addFastViews(IPageLayout layout) {",
						"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
						"    layout.addFastView(IPageLayout.ID_PROBLEM_VIEW);",
						"  }",
						"}");
		page.refresh();
		FastViewContainerInfo container = page.getFastViewContainer();
		// prepare items
		FastViewInfo item_1 = (FastViewInfo) container.getShortcuts().get(0);
		FastViewInfo item_2 = (FastViewInfo) container.getShortcuts().get(1);
		// do MOVE
		container.command_MOVE(item_2, item_1);
		// check source
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addFastViews(layout);",
				"  }",
				"  private void addFastViews(IPageLayout layout) {",
				"    layout.addFastView(IPageLayout.ID_PROBLEM_VIEW);",
				"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
				"  }",
				"}");
		// check shortcut
		{
			List<AbstractShortcutInfo> shortcuts = container.getShortcuts();
			assertSame(item_2, shortcuts.get(0));
			assertSame(item_1, shortcuts.get(1));
		}
	}

	/**
	 * Test for {@link FastViewContainerInfo#command_MOVE(FastViewInfo, FastViewInfo)}.<br>
	 * Move to last item.
	 */
	@Test
	public void test_fastView_MOVE_2() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addFastViews(layout);",
						"  }",
						"  private void addFastViews(IPageLayout layout) {",
						"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
						"    layout.addFastView(IPageLayout.ID_PROBLEM_VIEW);",
						"  }",
						"}");
		page.refresh();
		FastViewContainerInfo container = page.getFastViewContainer();
		// prepare items
		FastViewInfo item_1 = (FastViewInfo) container.getShortcuts().get(0);
		FastViewInfo item_2 = (FastViewInfo) container.getShortcuts().get(1);
		// do MOVE
		container.command_MOVE(item_1, null);
		// check source
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addFastViews(layout);",
				"  }",
				"  private void addFastViews(IPageLayout layout) {",
				"    layout.addFastView(IPageLayout.ID_PROBLEM_VIEW);",
				"    layout.addFastView(IPageLayout.ID_PROJECT_EXPLORER);",
				"  }",
				"}");
		// check shortcut
		{
			List<AbstractShortcutInfo> shortcuts = container.getShortcuts();
			assertSame(item_2, shortcuts.get(0));
			assertSame(item_1, shortcuts.get(1));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// View shortcuts commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ViewShortcutContainerInfo#command_CREATE(String, ViewShortcutInfo)}.
	 */
	@Test
	public void test_viewShortcuts_CREATE() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addViewShortcuts(layout);",
						"  }",
						"  private void addViewShortcuts(IPageLayout layout) {",
						"    layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);",
						"  }",
						"}");
		page.refresh();
		ViewShortcutContainerInfo container = page.getViewShortcutContainer();
		ViewShortcutInfo nextItem = (ViewShortcutInfo) container.getShortcuts().get(0);
		assertNotNull(ObjectInfoUtils.getId(nextItem));
		// do CREATE
		ViewShortcutInfo item =
				container.command_CREATE("org.eclipse.jdt.ui.PackageExplorer", nextItem);
		assertNotNull(ObjectInfoUtils.getId(item));
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addViewShortcuts(layout);",
				"  }",
				"  private void addViewShortcuts(IPageLayout layout) {",
				"    layout.addShowViewShortcut('org.eclipse.jdt.ui.PackageExplorer');",
				"    layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link FastViewContainerInfo#command_MOVE(FastViewInfo, FastViewInfo)}.<br>
	 * Before existing {@link FastViewInfo}.
	 */
	@Test
	public void test_viewShortcuts_MOVE() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addViewShortcuts(layout);",
						"  }",
						"  private void addViewShortcuts(IPageLayout layout) {",
						"    layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);",
						"    layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);",
						"  }",
						"}");
		page.refresh();
		ViewShortcutContainerInfo container = page.getViewShortcutContainer();
		// prepare items
		ViewShortcutInfo item_1 = (ViewShortcutInfo) container.getShortcuts().get(0);
		ViewShortcutInfo item_2 = (ViewShortcutInfo) container.getShortcuts().get(1);
		// do MOVE
		container.command_MOVE(item_2, item_1);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addViewShortcuts(layout);",
				"  }",
				"  private void addViewShortcuts(IPageLayout layout) {",
				"    layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);",
				"    layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Perspective shortcuts commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for
	 * {@link PerspectiveShortcutContainerInfo#command_CREATE(String, PerspectiveShortcutInfo)}.
	 */
	@Test
	public void test_perspectiveShortcuts_CREATE() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addPerspectiveShortcuts(layout);",
						"  }",
						"  private void addPerspectiveShortcuts(IPageLayout layout) {",
						"    layout.addPerspectiveShortcut('org.eclipse.jdt.ui.JavaPerspective');",
						"  }",
						"}");
		page.refresh();
		PerspectiveShortcutContainerInfo container = page.getPerspectiveShortcutContainer();
		PerspectiveShortcutInfo nextItem = (PerspectiveShortcutInfo) container.getShortcuts().get(0);
		assertNotNull(ObjectInfoUtils.getId(nextItem));
		// do CREATE
		PerspectiveShortcutInfo item =
				container.command_CREATE("org.eclipse.ui.resourcePerspective", nextItem);
		assertNotNull(ObjectInfoUtils.getId(item));
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addPerspectiveShortcuts(layout);",
				"  }",
				"  private void addPerspectiveShortcuts(IPageLayout layout) {",
				"    layout.addPerspectiveShortcut('org.eclipse.ui.resourcePerspective');",
				"    layout.addPerspectiveShortcut('org.eclipse.jdt.ui.JavaPerspective');",
				"  }",
				"}");
	}

	/**
	 * Test for
	 * {@link PerspectiveShortcutContainerInfo#command_MOVE(PerspectiveShortcutInfo, PerspectiveShortcutInfo)}
	 * .<br>
	 * Before existing {@link PerspectiveShortcutInfo}.
	 */
	@Test
	public void test_perspectiveShortcuts_MOVE() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    addPerspectiveShortcuts(layout);",
						"  }",
						"  private void addPerspectiveShortcuts(IPageLayout layout) {",
						"    layout.addPerspectiveShortcut('org.eclipse.ui.resourcePerspective');",
						"    layout.addPerspectiveShortcut('org.eclipse.jdt.ui.JavaPerspective');",
						"  }",
						"}");
		page.refresh();
		PerspectiveShortcutContainerInfo container = page.getPerspectiveShortcutContainer();
		// prepare items
		PerspectiveShortcutInfo item_1 = (PerspectiveShortcutInfo) container.getShortcuts().get(0);
		PerspectiveShortcutInfo item_2 = (PerspectiveShortcutInfo) container.getShortcuts().get(1);
		// do MOVE
		container.command_MOVE(item_2, item_1);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    addPerspectiveShortcuts(layout);",
				"  }",
				"  private void addPerspectiveShortcuts(IPageLayout layout) {",
				"    layout.addPerspectiveShortcut('org.eclipse.jdt.ui.JavaPerspective');",
				"    layout.addPerspectiveShortcut('org.eclipse.ui.resourcePerspective');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractPart_Info.resize()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AbstractPartInfo#resize(int, int)}.<br>
	 * Active {@link AbstractPartInfo}.
	 */
	@Test
	public void test_abstractPart_resizeActive() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// do resize
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		view.resize(+100);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view', IPageLayout.LEFT, 0.47f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link AbstractPartInfo#resize(int, int)}.<br>
	 * Active {@link AbstractPartInfo}, vertical
	 */
	@Test
	public void test_abstractPart_resizeActiveVertical() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// do resize
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		view.resize(+100);
		Object ratio = ((MethodInvocation) view.getRelatedNodes().get(0)).arguments().get(2);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view', IPageLayout.TOP, " + ratio + ", IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link AbstractPartInfo#resize(int, int)}.<br>
	 * Active {@link AbstractPartInfo}, make bigger, but not more than <code>0.95</code>
	 */
	@Test
	public void test_abstractPart_resizeActive_plusOver() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view', IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// do resize
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		view.resize(+10000);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view', IPageLayout.LEFT, 0.95f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link AbstractPartInfo#resize(int, int)}.<br>
	 * Passive {@link AbstractPartInfo}.
	 */
	@Test
	public void test_abstractPart_resizePassive() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view', IPageLayout.RIGHT, 0.7f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// do resize
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		view.resize(+100);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view', IPageLayout.RIGHT, 0.52f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link AbstractPartInfo#resize(int, int)}.<br>
	 * Passive {@link AbstractPartInfo}, make smaller.
	 */
	@Test
	public void test_abstractPart_resizePassive_minus() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view', IPageLayout.RIGHT, 0.4f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// do resize
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		view.resize(-150);
		Object ratio = ((MethodInvocation) view.getRelatedNodes().get(0)).arguments().get(2);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view', IPageLayout.RIGHT, " + ratio + ", IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link AbstractPartInfo#resize(int, int)}.<br>
	 * Passive {@link AbstractPartInfo}, make smaller, but not less than <code>0.05</code>
	 */
	@Test
	public void test_abstractPart_resizePassive_minusOver() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view', IPageLayout.RIGHT, 0.4f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		// do resize
		assertEquals(new Dimension(600, 500), page.getBounds().getSize());
		view.resize(-10000);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view', IPageLayout.RIGHT, 0.95f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Morphing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for morphing {@link PageLayoutAddViewInfo} into stand-alone/place-holder.
	 */
	@Test
	public void test_addView_morphing() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea);",
						"  }",
						"}");
		PageLayoutAddViewInfo view = (PageLayoutAddViewInfo) page.getParts().get(0);
		Property standaloneProperty = view.getPropertyByTitle("standalone");
		Property placeholderProperty = view.getPropertyByTitle("placeholder");
		// normal
		assertMorphView(view, false, false);
		// normal -> standalone
		{
			setStandalone(view, true);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addStandaloneView(IPageLayout.ID_PROJECT_EXPLORER, true, IPageLayout.LEFT, 0.3f, editorArea);",
					"  }",
					"}");
			assertMorphView(view, true, false);
		}
		// standalone -> standalone + placeholder
		{
			setPlaceholder(view, true);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addStandaloneViewPlaceholder(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea, true);",
					"  }",
					"}");
			assertMorphView(view, true, true);
		}
		// standalone + placeholder -> standalone
		{
			setPlaceholder(view, false);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addStandaloneView(IPageLayout.ID_PROJECT_EXPLORER, true, IPageLayout.LEFT, 0.3f, editorArea);",
					"  }",
					"}");
			assertMorphView(view, true, false);
		}
		// standalone -> normal
		{
			setStandalone(view, false);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea);",
					"  }",
					"}");
			assertMorphView(view, false, false);
		}
		// normal -> placeholder
		{
			setPlaceholder(view, true);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea);",
					"  }",
					"}");
			assertMorphView(view, false, true);
		}
		// placeholder -> placeholder + standalone
		{
			setStandalone(view, true);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addStandaloneViewPlaceholder(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea, true);",
					"  }",
					"}");
			assertMorphView(view, true, true);
		}
		// placeholder + standalone -> placeholder
		{
			setStandalone(view, false);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea);",
					"  }",
					"}");
			assertMorphView(view, false, true);
		}
		// placeholder -> normal
		{
			setPlaceholder(view, false);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea);",
					"  }",
					"}");
			assertMorphView(view, false, false);
		}
		// use properties
		{
			standaloneProperty.setValue(Boolean.TRUE);
			placeholderProperty.setValue(Boolean.TRUE);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    layout.addStandaloneViewPlaceholder(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.3f, editorArea, true);",
					"  }",
					"}");
			assertMorphView(view, true, true);
		}
	}

	/**
	 * Invokes {@link PageLayoutAddViewInfo#setStandalone(boolean)} and performs refresh.
	 */
	private static void setStandalone(PageLayoutAddViewInfo view, boolean makeStandalone)
			throws Exception {
		view.setStandalone(makeStandalone);
		view.getRoot().refresh();
	}

	/**
	 * Invokes {@link PageLayoutAddViewInfo#setPlaceholder(boolean)} and performs refresh.
	 */
	private static void setPlaceholder(PageLayoutAddViewInfo view, boolean makePlaceholder)
			throws Exception {
		view.setPlaceholder(makePlaceholder);
		view.getRoot().refresh();
	}

	/**
	 * Asserts that given view has same parameters as expected for {@link #test_addView_morphing()}.
	 */
	private static void assertMorphView(PageLayoutAddViewInfo view,
			boolean standalone,
			boolean placeholder) throws Exception {
		assertEquals(standalone, view.isStandalone());
		assertEquals(placeholder, view.isPlaceholder2());
		{
			Property property = view.getPropertyByTitle("standalone");
			assertTrue(property.isModified());
			assertEquals(standalone, ((Boolean) property.getValue()).booleanValue());
		}
		{
			Property property = view.getPropertyByTitle("placeholder");
			assertTrue(property.isModified());
			assertEquals(placeholder, ((Boolean) property.getValue()).booleanValue());
		}
		// arguments
		assertEquals(IPageLayout.ID_PROJECT_EXPLORER, view.getId());
		assertEquals(IPageLayout.LEFT, view.getRelationship());
		assertEquals(0.3f, view.getRatio(), 0.001);
		assertEquals(IPageLayout.ID_EDITOR_AREA, view.getRefId());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// COMMAND's for view
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.<br>
	 * Other reference already exists.
	 */
	@Test
	public void test_CREATE_view_0() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.TOP, 0.3f, layout.getEditorArea());",
						"  }",
						"}");
		page.refresh();
		// create view
		PageLayoutAddViewInfo newView =
				page.command_CREATE(
						"org.eclipse.jdt.ui.PackagesView",
						IPageLayout.LEFT,
						0.5f,
						page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.TOP, 0.3f, layout.getEditorArea());",
				"    layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.TOP, 0.3f, layout.getEditorArea())/ /layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.TOP, 0.3f, layout.getEditorArea())/}",
				"  {void} {void} {/layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(newView, page.getParts().get(1));
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.<br>
	 * No reference exist.
	 */
	@Test
	public void test_CREATE_view_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// create view
		PageLayoutAddViewInfo newView =
				page.command_CREATE(
						"org.eclipse.jdt.ui.PackagesView",
						IPageLayout.LEFT,
						0.5f,
						page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
		assertSame(newView, page.getParts().get(0));
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.<br>
	 * Reference on other view.
	 */
	@Test
	public void test_CREATE_view_2() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
						"    layout.addView('view_2', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_1 = (PageLayoutAddViewInfo) page.getParts().get(0);
		// create view
		PageLayoutAddViewInfo newView = page.command_CREATE("view_3", IPageLayout.BOTTOM, 0.4f, view_1);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
				"    layout.addView('view_3', IPageLayout.BOTTOM, 0.4f, 'view_1');",
				"    layout.addView('view_2', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
				"  }",
				"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea())/ /layout.getEditorArea()/ /layout.addView('view_2', IPageLayout.TOP, 0.3f, layout.getEditorArea())/ /layout.addView('view_3', IPageLayout.BOTTOM, 0.4f, 'view_1')/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea())/}",
				"  {void} {void} {/layout.addView('view_3', IPageLayout.BOTTOM, 0.4f, 'view_1')/}",
				"  {void} {void} {/layout.addView('view_2', IPageLayout.TOP, 0.3f, layout.getEditorArea())/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(newView, page.getParts().get(1));
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.<br>
	 * Reference on editor area, with existing reference as folder.
	 */
	@Test
	public void test_CREATE_view_3() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    {",
						"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"      folder.addView('view.1');",
						"      folder.addView('view.2');",
						"    }",
						"  }",
						"}");
		page.refresh();
		// create view
		page.command_CREATE("view_3", IPageLayout.RIGHT, 0.5f, page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    {",
				"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"      folder.addView('view.1');",
				"      folder.addView('view.2');",
				"    }",
				"    layout.addView('view_3', IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.<br>
	 * Reference on editor area, with existing reference as folder.
	 */
	@Test
	public void test_CREATE_view_4() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    {",
						"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"      folder.addView('view.2');",
						"      folder.addView('view.3');",
						"    }",
						"    layout.addView('view_4', IPageLayout.BOTTOM, 0.5f, 'view_1');",
						"  }",
						"}");
		page.refresh();
		// create view
		page.command_CREATE("view_5", IPageLayout.RIGHT, 0.5f, page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    {",
				"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"      folder.addView('view.2');",
				"      folder.addView('view.3');",
				"    }",
				"    layout.addView('view_5', IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);",
				"    layout.addView('view_4', IPageLayout.BOTTOM, 0.5f, 'view_1');",
				"  }",
				"}");
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_CREATE_view_TOP() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// create view
		page.command_CREATE(
				"org.eclipse.jdt.ui.PackagesView",
				IPageLayout.TOP,
				0.3f,
				page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_CREATE_view_BOTTOM() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// create view
		page.command_CREATE(
				"org.eclipse.jdt.ui.PackagesView",
				IPageLayout.BOTTOM,
				0.3f,
				page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_CREATE_view_LEFT() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// create view
		page.command_CREATE(
				"org.eclipse.jdt.ui.PackagesView",
				IPageLayout.LEFT,
				0.3f,
				page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_CREATE_view_RIGHT() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// create view
		page.command_CREATE(
				"org.eclipse.jdt.ui.PackagesView",
				IPageLayout.RIGHT,
				0.3f,
				page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('org.eclipse.jdt.ui.PackagesView', IPageLayout.RIGHT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link PageLayoutInfo#command_CREATE(String, int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_CREATE_view_invalidRelationship() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// create view
		try {
			int invalidRelationship = 555;
			page.command_CREATE(
					"org.eclipse.jdt.ui.PackagesView",
					invalidRelationship,
					0.3f,
					page.getEditorArea());
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test for
	 * {@link PageLayoutInfo#command_MOVE(AbstractPartInfo, int, float, IPageLayoutTopLevelInfo)} .<br>
	 * Was: relative to some view. Become: relative to other view.
	 */
	@Test
	public void test_MOVE_view_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
						"    layout.addView('view_2', IPageLayout.BOTTOM, 0.4f, 'view_1');",
						"    layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(1);
		PageLayoutAddViewInfo view_3 = (PageLayoutAddViewInfo) page.getParts().get(2);
		// move view
		page.command_MOVE(view_2, IPageLayout.RIGHT, 0.2f, view_3);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
				"    layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
				"    layout.addView('view_2', IPageLayout.RIGHT, 0.2f, 'view_3');",
				"  }",
				"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea())/ /layout.addView('view_2', IPageLayout.RIGHT, 0.2f, 'view_3')/ /layout.getEditorArea()/ /layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea())/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea())/}",
				"  {void} {void} {/layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea())/}",
				"  {void} {void} {/layout.addView('view_2', IPageLayout.RIGHT, 0.2f, 'view_3')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(view_3, page.getParts().get(1));
		assertSame(view_2, page.getParts().get(2));
	}

	/**
	 * Test for
	 * {@link PageLayoutInfo#command_MOVE(AbstractPartInfo, int, float, IPageLayoutTopLevelInfo)} .<br>
	 * Was: relative to some view. Become: relative to same view, but different relatioship.
	 */
	@Test
	public void test_MOVE_view_2() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
						"    layout.addView('view_2', IPageLayout.BOTTOM, 0.4f, 'view_1');",
						"    layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_1 = (PageLayoutAddViewInfo) page.getParts().get(0);
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(1);
		// move view
		page.command_MOVE(view_2, IPageLayout.TOP, 0.2f, view_1);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
				"    layout.addView('view_2', IPageLayout.TOP, 0.2f, 'view_1');",
				"    layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
				"  }",
				"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea())/ /layout.addView('view_2', IPageLayout.TOP, 0.2f, 'view_1')/ /layout.getEditorArea()/ /layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea())/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea())/}",
				"  {void} {void} {/layout.addView('view_2', IPageLayout.TOP, 0.2f, 'view_1')/}",
				"  {void} {void} {/layout.addView('view_3', IPageLayout.TOP, 0.3f, layout.getEditorArea())/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(view_1, page.getParts().get(0));
		assertSame(view_2, page.getParts().get(1));
	}

	/**
	 * Test for
	 * {@link PageLayoutInfo#command_MOVE(AbstractPartInfo, int, float, IPageLayoutTopLevelInfo)} .<br>
	 * Attempt to move view before itself.
	 */
	@Test
	public void test_MOVE_view_3() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
						"    layout.addView('view_2', IPageLayout.TOP, 0.3f, layout.getEditorArea());",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_1 = (PageLayoutAddViewInfo) page.getParts().get(0);
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(1);
		// move view
		page.command_MOVE(view_2, IPageLayout.BOTTOM, 0.2f, view_1);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, layout.getEditorArea());",
				"    layout.addView('view_2', IPageLayout.BOTTOM, 0.2f, 'view_1');",
				"  }",
				"}");
		assertSame(view_1, page.getParts().get(0));
		assertSame(view_2, page.getParts().get(1));
	}

	/**
	 * Test for
	 * {@link PageLayoutInfo#command_MOVE(AbstractPartInfo, int, float, IPageLayoutTopLevelInfo)} .<br>
	 */
	@Test
	public void test_MOVE_folder_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view.1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    {",
						"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"      folder.addView('view.2');",
						"      folder.addView('view.3');",
						"    }",
						"    layout.addView('view.4', IPageLayout.BOTTOM, 0.5f, 'view.1');",
						"  }",
						"}");
		page.refresh();
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addView('view.1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.addView('view.4', IPageLayout.BOTTOM, 0.5f, 'view.1')/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view.1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {local-unique: folder} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /folder.addView('view.2')/ /folder.addView('view.3')/}",
				"    {void} {void} {/folder.addView('view.2')/}",
				"    {void} {void} {/folder.addView('view.3')/}",
				"  {void} {void} {/layout.addView('view.4', IPageLayout.BOTTOM, 0.5f, 'view.1')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		// prepare "view_4"
		PageLayoutAddViewInfo view_4 = (PageLayoutAddViewInfo) page.getParts().get(2);
		assertEquals("view.4", view_4.getId());
		// move "folder"
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(1);
		page.command_MOVE(folder, IPageLayout.TOP, 0.2f, view_4);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view.1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    layout.addView('view.4', IPageLayout.BOTTOM, 0.5f, 'view.1');",
				"    {",
				"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.2f, 'view.4');",
				"      folder.addView('view.2');",
				"      folder.addView('view.3');",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addView('view.1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.createFolder('folder.1', IPageLayout.TOP, 0.2f, 'view.4')/ /layout.addView('view.4', IPageLayout.BOTTOM, 0.5f, 'view.1')/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view.1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {void} {/layout.addView('view.4', IPageLayout.BOTTOM, 0.5f, 'view.1')/}",
				"  {void} {local-unique: folder} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.2f, 'view.4')/ /folder.addView('view.2')/ /folder.addView('view.3')/}",
				"    {void} {void} {/folder.addView('view.2')/}",
				"    {void} {void} {/folder.addView('view.3')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
	}

	/**
	 * Test for
	 * {@link PageLayoutInfo#command_MOVE(FolderViewInfo, int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_MOVE_FolderView_into_topView() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    {",
						"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"      folder.addView('view');",
						"    }",
						"  }",
						"}");
		page.refresh();
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (editor area)",
				"  {void} {local-unique: folder} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /folder.addView('view')/}",
				"    {void} {void} {/folder.addView('view')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		// prepare models
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		FolderViewInfo folderView = folder.getViews().get(0);
		assertEquals("view", folderView.getId());
		// move "folderView" relative "folder", so make in top level
		PageLayoutAddViewInfo topView = page.command_MOVE(folderView, IPageLayout.TOP, 0.2f, folder);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    {",
				"      IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    }",
				"    layout.addView('view', IPageLayout.TOP, 0.2f, 'folder.1');",
				"  }",
				"}");
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.addView('view', IPageLayout.TOP, 0.2f, 'folder.1')/}",
				"  (editor area)",
				"  {void} {local-unique: folder} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {void} {/layout.addView('view', IPageLayout.TOP, 0.2f, 'folder.1')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(topView, page.getParts().get(1));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFolderLayout creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PageLayoutInfo#command_CREATE_folder(int, float, IPageLayoutTopLevelInfo)}.
	 */
	@Test
	public void test_CREATE_folder() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.createFolder('folder', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.createFolder('folder_1', IPageLayout.RIGHT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		// create folder
		page.command_CREATE_folder(IPageLayout.BOTTOM, 0.5f, page.getEditorArea());
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.createFolder('folder', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    layout.createFolder('folder_1', IPageLayout.RIGHT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    {",
				"      IFolderLayout folderLayout = layout.createFolder('folder_2', IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA);",
				"    }",
				"  }",
				"}");
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.createFolder('folder', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.createFolder('folder_1', IPageLayout.RIGHT, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.createFolder('folder_2', IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (editor area)",
				"  {void} {empty} {/layout.createFolder('folder', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {empty} {/layout.createFolder('folder_1', IPageLayout.RIGHT, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {local-unique: folderLayout} {/layout.createFolder('folder_2', IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
	}

	/**
	 * Test for {@link PageLayoutInfo#convertIntoFolder(PageLayoutAddViewInfo)}.<br>
	 * Converted view referenced editor area.
	 */
	@Test
	public void test_convertViewIntoFolder_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.addView('view_2', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_1 = (PageLayoutAddViewInfo) page.getParts().get(0);
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(1);
		PageLayoutAddViewInfo view_3 = (PageLayoutAddViewInfo) page.getParts().get(2);
		// move view
		PageLayoutCreateFolderInfo newFolder = page.convertIntoFolder(view_2);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    {",
				"      IFolderLayout folderLayout = layout.createFolder('folder', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"      folderLayout.addView('view_2');",
				"    }",
				"    layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"  }",
				"}");
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.createFolder('folder', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {local-unique: folderLayout} {/layout.createFolder('folder', IPageLayout.TOP, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /folderLayout.addView('view_2')/}",
				"    {void} {void} {/folderLayout.addView('view_2')/}",
				"  {void} {void} {/layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(view_1, page.getParts().get(0));
		assertSame(newFolder, page.getParts().get(1));
		assertSame(view_3, page.getParts().get(2));
	}

	/**
	 * Test for {@link PageLayoutInfo#convertIntoFolder(PageLayoutAddViewInfo)}.<br>
	 * Converted view referenced other view.
	 */
	@Test
	public void test_convertViewIntoFolder_2() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"    layout.addView('view_2', IPageLayout.TOP, 0.3f, 'view_1');",
						"    layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, 'view_1');",
						"  }",
						"}");
		page.refresh();
		PageLayoutAddViewInfo view_1 = (PageLayoutAddViewInfo) page.getParts().get(0);
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(1);
		// move view
		PageLayoutCreateFolderInfo newFolder = page.convertIntoFolder(view_2);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
				"    {",
				"      IFolderLayout folderLayout = layout.createFolder('folder', IPageLayout.TOP, 0.3f, 'view_1');",
				"      folderLayout.addView('view_2');",
				"    }",
				"    layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, 'view_1');",
				"  }",
				"}");
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/ /layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, 'view_1')/ /layout.createFolder('folder', IPageLayout.TOP, 0.3f, 'view_1')/}",
				"  (editor area)",
				"  {void} {void} {/layout.addView('view_1', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA)/}",
				"  {void} {local-unique: folderLayout} {/layout.createFolder('folder', IPageLayout.TOP, 0.3f, 'view_1')/ /folderLayout.addView('view_2')/}",
				"    {void} {void} {/folderLayout.addView('view_2')/}",
				"  {void} {void} {/layout.addView('view_3', IPageLayout.BOTTOM, 0.3f, 'view_1')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		assertSame(view_1, page.getParts().get(0));
		assertSame(newFolder, page.getParts().get(1));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFolderLayout, its commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link IPageLayout#createFolder(String, int, float, String)}.
	 */
	@Test
	public void test_IFolderLayout_parse() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"    folder.addView('view.2');",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{parameter: layout} {layout} {/layout.getEditorArea()/ /layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea)/}",
				"  (editor area)",
				"  {void} {local-unique: folder} {/layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea)/ /folder.addView('view.1')/ /folder.addView('view.2')/}",
				"    {void} {void} {/folder.addView('view.1')/}",
				"    {void} {void} {/folder.addView('view.2')/}",
				"  (fast views)",
				"  (view shortcuts)",
				"  (perspective shortcuts)");
		// prepare IFolderLayout
		PageLayoutCreateFolderInfo folder;
		{
			List<AbstractPartInfo> parts = page.getParts();
			Assertions.assertThat(parts).hasSize(1);
			folder = (PageLayoutCreateFolderInfo) parts.get(0);
			assertNotNull(ObjectInfoUtils.getId(folder));
		}
		// prepare view's in folder
		FolderViewInfo view_1;
		FolderViewInfo view_2;
		{
			List<FolderViewInfo> views = folder.getViews();
			Assertions.assertThat(views).hasSize(2);
			view_1 = views.get(0);
			view_2 = views.get(1);
			assertNotNull(ObjectInfoUtils.getId(view_1));
			assertNotNull(ObjectInfoUtils.getId(view_2));
		}
		// do refresh
		page.refresh();
		{
			Rectangle bounds = folder.getBounds();
			Assertions.assertThat(bounds.x).isEqualTo(0);
			Assertions.assertThat(bounds.y).isEqualTo(0);
			Assertions.assertThat(bounds.width).isGreaterThan(550);
			Assertions.assertThat(bounds.height).isGreaterThan(150);
		}
		{
			Rectangle bounds = view_1.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(50);
			Assertions.assertThat(bounds.height).isGreaterThan(20);
		}
		{
			Rectangle bounds = view_2.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(50);
			Assertions.assertThat(bounds.height).isGreaterThan(20);
			// relative to view_1
			Assertions.assertThat(view_2.getBounds().x).isGreaterThan(view_1.getBounds().right());
			Assertions.assertThat(view_2.getBounds().y).isEqualTo(view_1.getBounds().y);
		}
		// check presentation of "folder"
		{
			IObjectPresentation presentation = folder.getPresentation();
			assertNotNull(presentation.getIcon());
			assertEquals("folder.1", presentation.getText());
		}
		// check presentation of "view"
		{
			IObjectPresentation presentation = view_1.getPresentation();
			assertNotNull(presentation.getIcon());
			assertEquals("\"view.1\" - view.1", presentation.getText());
		}
	}

	/**
	 * Test for {@link IPageLayout#createFolder(String, int, float, String)}.
	 */
	@Test
	public void test_IFolderLayout_delete() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"    folder.addView('view.2');",
						"  }",
						"}");
		page.refresh();
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		// delete folder
		folder.delete();
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"  }",
				"}");
	}

	/**
	 * Test for morphing {@link PageLayoutCreateFolderInfo}.
	 */
	@Test
	public void test_IFolderLayout_morphing() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"    folder.addView('view.2');",
						"  }",
						"}");
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		// prepare "placeholder" property
		Property placeholderProperty;
		{
			placeholderProperty = folder.getPropertyByTitle("placeholder");
			assertNotNull(placeholderProperty);
			assertEquals("placeholder", placeholderProperty.getTitle());
			assertTrue(placeholderProperty.isModified());
		}
		// initially "normal"
		{
			assertFalse(folder.isPlaceholder2());
			assertEquals(false, ((Boolean) placeholderProperty.getValue()).booleanValue());
		}
		// make "placeholder"
		{
			folder.setPlaceholder(true);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    IPlaceholderFolderLayout folder = layout.createPlaceholderFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
					"    folder.addPlaceholder('view.1');",
					"    folder.addPlaceholder('view.2');",
					"  }",
					"}");
			assertTrue(folder.isPlaceholder2());
			assertEquals(true, ((Boolean) placeholderProperty.getValue()).booleanValue());
		}
		// make "normal" using property
		{
			placeholderProperty.setValue(false);
			assertEditor(
					"public class Test implements IPerspectiveFactory {",
					"  public Test() {",
					"  }",
					"  public void createInitialLayout(IPageLayout layout) {",
					"    String editorArea = layout.getEditorArea();",
					"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
					"    folder.addView('view.1');",
					"    folder.addView('view.2');",
					"  }",
					"}");
			assertFalse(folder.isPlaceholder2());
			assertEquals(false, ((Boolean) placeholderProperty.getValue()).booleanValue());
		}
	}

	/**
	 * Test for {@link PageLayoutCreateFolderInfo#command_CREATE(String, FolderViewInfo)}.
	 */
	@Test
	public void test_IFolderLayout_CREATE_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"  }",
						"}");
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		FolderViewInfo nextView = folder.getViews().get(0);
		// create new view
		FolderViewInfo newView = folder.command_CREATE("view.2", nextView);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
				"    folder.addView('view.2');",
				"    folder.addView('view.1');",
				"  }",
				"}");
		assertSame(newView, folder.getViews().get(0));
		assertSame(nextView, folder.getViews().get(1));
	}

	/**
	 * Test for {@link PageLayoutCreateFolderInfo#command_MOVE(FolderViewInfo, FolderViewInfo)}.
	 */
	@Test
	public void test_IFolderLayout_MOVE_1() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"    folder.addView('view.2');",
						"    folder.addView('view.3');",
						"  }",
						"}");
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		FolderViewInfo view_1 = folder.getViews().get(0);
		FolderViewInfo view_2 = folder.getViews().get(1);
		FolderViewInfo view_3 = folder.getViews().get(2);
		// create new view
		folder.command_MOVE(view_3, view_1);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
				"    folder.addView('view.3');",
				"    folder.addView('view.1');",
				"    folder.addView('view.2');",
				"  }",
				"}");
		assertSame(view_3, folder.getViews().get(0));
		assertSame(view_1, folder.getViews().get(1));
		assertSame(view_2, folder.getViews().get(2));
	}

	/**
	 * Test for {@link PageLayoutCreateFolderInfo#command_MOVE(PageLayoutAddViewInfo, FolderViewInfo)}
	 * .
	 */
	@Test
	public void test_IFolderLayout_MOVE_2() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"    folder.addView('view.1');",
						"    layout.addView('view.2', IPageLayout.LEFT, 0.3f, IPageLayout.ID_EDITOR_AREA);",
						"  }",
						"}");
		PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		FolderViewInfo view_1 = folder.getViews().get(0);
		PageLayoutAddViewInfo view_2 = (PageLayoutAddViewInfo) page.getParts().get(1);
		// create new view
		FolderViewInfo newView_2 = folder.command_MOVE(view_2, null);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"    IFolderLayout folder = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
				"    folder.addView('view.1');",
				"    folder.addView('view.2');",
				"  }",
				"}");
		assertSame(view_1, folder.getViews().get(0));
		assertFalse(page.getParts().contains(view_2));
		assertSame(newView_2, folder.getViews().get(1));
	}

	/**
	 * Test for {@link PageLayoutCreateFolderInfo#command_MOVE(PageLayoutAddViewInfo, FolderViewInfo)}
	 * .
	 */
	@Test
	public void test_IFolderLayout_MOVE_fromOtherFolder() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"    String editorArea = layout.getEditorArea();",
						"    {",
						"      IFolderLayout folder_1 = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
						"      folder_1.addView('view');",
						"    }",
						"    {",
						"      IFolderLayout folder_2 = layout.createFolder('folder.2', IPageLayout.RIGHT, 0.3f, editorArea);",
						"    }",
						"  }",
						"}");
		PageLayoutCreateFolderInfo folder_1 = (PageLayoutCreateFolderInfo) page.getParts().get(0);
		PageLayoutCreateFolderInfo folder_2 = (PageLayoutCreateFolderInfo) page.getParts().get(1);
		FolderViewInfo view = folder_1.getViews().get(0);
		// move "view" from "folder_1" to "folder_2"
		FolderViewInfo newView = folder_2.command_MOVE(view, null);
		assertEditor(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"    {",
				"      IFolderLayout folder_1 = layout.createFolder('folder.1', IPageLayout.TOP, 0.4f, editorArea);",
				"    }",
				"    {",
				"      IFolderLayout folder_2 = layout.createFolder('folder.2', IPageLayout.RIGHT, 0.3f, editorArea);",
				"      folder_2.addView('view');",
				"    }",
				"  }",
				"}");
		Assertions.assertThat(folder_1.getViews()).isEmpty();
		Assertions.assertThat(folder_2.getViews()).containsOnly(newView);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Palette
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for palette for {@link PageLayoutInfo}.
	 */
	@Test
	public void test_palette() throws Exception {
		PageLayoutInfo page =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		page.refresh();
		// ask for palette categories
		List<CategoryInfo> categories;
		{
			categories = new ArrayList<>();
			// add some "old" category
			CategoryInfo oldCategory = new CategoryInfo("old.ID");
			categories.add(oldCategory);
			assertTrue(categories.contains(oldCategory));
			// use broadcast to update categories
			page.getBroadcast(PaletteEventListener.class).categories(categories);
			// no "old" category
			assertFalse(categories.contains(oldCategory));
		}
		// analyze palette categories
		Assertions.assertThat(categories.size()).isGreaterThan(5);
		{
			CategoryInfo category = categories.get(0);
			assertEquals("System", category.getName());
			assertEquals(1, category.getEntries().size());
			assertInstanceOf(SelectionToolEntryInfo.class, category.getEntries().get(0));
		}
		// check for "Other" category
		{
			boolean hasOther = false;
			for (CategoryInfo category : categories) {
				if ("other".equals(category.getId())) {
					hasOther = true;
					assertEquals("Other", category.getName());
				}
			}
			assertTrue("No 'Other' category", hasOther);
		}
		// check for "standard" Eclipse categories
		{
			boolean hasGeneral = false;
			boolean hasJava = false;
			for (CategoryInfo category : categories) {
				if ("org.eclipse.ui".equals(category.getId())) {
					hasGeneral = true;
					assertEquals("General", category.getName());
					Assertions.assertThat(category.getEntries().size()).isGreaterThan(10);
				}
				if ("org.eclipse.jdt.ui.java".equals(category.getId())) {
					hasJava = true;
					assertEquals("Java", category.getName());
					Assertions.assertThat(category.getEntries().size()).isGreaterThan(5);
				}
			}
			assertTrue("No 'General' category", hasGeneral);
			assertTrue("No 'Java' category", hasJava);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Extension properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Current project is not a plugin project, so no "Extension" property.
	 */
	@Test
	public void test_extensionProperties_notPlugin() throws Exception {
		PageLayoutInfo part =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		assertNull(part.getPropertyByTitle("Extension"));
	}

	/**
	 * No "perspective" extension for this {@link IPageLayout} class, so no "Extension" property.
	 */
	@Test
	public void test_extensionProperties_noExtension() throws Exception {
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		AbstractPdeTest.createPluginXML(
				"<plugin>",
				"  <!-- ===== filler filler filler filler filler ===== -->",
				"  <!-- ===== filler filler filler filler filler ===== -->",
				"</plugin>");
		// parse
		PageLayoutInfo part =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		assertNull(part.getPropertyByTitle("Extension"));
	}

	/**
	 * Valid "perspective" extension for this {@link IPageLayout} class, so we have "Extension"
	 * property and its sub-properties.
	 */
	@Test
	public void test_extensionProperties_hasExtension() throws Exception {
		do_projectDispose();
		do_projectCreate();
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		AbstractPdeTest.createPluginXML(
				"<plugin>",
				"  <extension point='org.eclipse.ui.perspectives'>",
				"    <perspective id='id_1' name='name 1' icon='icons/false.gif' class='test.Test' fixed='true'/>",
				"  </extension>",
				"</plugin>");
		// parse
		PageLayoutInfo part =
				parsePerspective(
						"public class Test implements IPerspectiveFactory {",
						"  public Test() {",
						"  }",
						"  public void createInitialLayout(IPageLayout layout) {",
						"  }",
						"}");
		// "Extension" property
		Property extensionProperty = part.getPropertyByTitle("Extension");
		assertNotNull(extensionProperty);
		assertTrue(extensionProperty.getCategory().isSystem());
		// sub-properties
		Property[] subProperties = getSubProperties(extensionProperty);
		Assertions.assertThat(subProperties).hasSize(3);
		{
			Property nameProperty = subProperties[0];
			assertEquals("name", nameProperty.getTitle());
			assertTrue(nameProperty.isModified());
			assertEquals("name 1", nameProperty.getValue());
		}
		{
			Property iconProperty = subProperties[1];
			assertEquals("icon", iconProperty.getTitle());
			assertTrue(iconProperty.isModified());
			assertEquals("icons/false.gif", iconProperty.getValue());
		}
		{
			Property fixedProperty = subProperties[2];
			assertEquals("fixed", fixedProperty.getTitle());
			assertTrue(fixedProperty.isModified());
			assertEquals(true, fixedProperty.getValue());
		}
		// when we set value for some "Extension" sub-property, refresh() should be performed
		{
			final boolean[] refreshed = new boolean[]{false};
			part.addBroadcastListener(new ObjectEventListener() {
				@Override
				public void refreshed() throws Exception {
					refreshed[0] = true;
				}
			});
			Property nameProperty = subProperties[0];
			nameProperty.setValue("New name");
			assertTrue(refreshed[0]);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private PageLayoutInfo parsePerspective(String... lines) throws Exception {
		return parseJavaInfo(lines);
	}

	@Override
	protected String[] getTestSource_decorate(String... lines) {
		lines = CodeUtils.join(new String[]{"package test;", "import org.eclipse.ui.*;"}, lines);
		return lines;
	}
}