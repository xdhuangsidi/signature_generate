package org.apache.log4j.lf5.viewer.categoryexplorer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.apache.http.HttpStatus;

public class CategoryNodeEditor extends CategoryAbstractCellEditor {
    protected CategoryExplorerModel _categoryModel;
    protected JCheckBox _checkBox = this._renderer.getCheckBox();
    protected CategoryNode _lastEditedNode;
    protected CategoryNodeEditorRenderer _renderer = new CategoryNodeEditorRenderer();
    protected JTree _tree;

    class AnonymousClass3 implements ActionListener {
        private final CategoryNodeEditor this$0;
        private final CategoryNode val$node;

        AnonymousClass3(CategoryNodeEditor categoryNodeEditor, CategoryNode categoryNode) {
            this.this$0 = categoryNodeEditor;
            this.val$node = categoryNode;
        }

        public void actionPerformed(ActionEvent e) {
            this.this$0.showPropertiesDialog(this.val$node);
        }
    }

    class AnonymousClass4 implements ActionListener {
        private final CategoryNodeEditor this$0;
        private final CategoryNode val$node;

        AnonymousClass4(CategoryNodeEditor categoryNodeEditor, CategoryNode categoryNode) {
            this.this$0 = categoryNodeEditor;
            this.val$node = categoryNode;
        }

        public void actionPerformed(ActionEvent e) {
            this.this$0._categoryModel.setDescendantSelection(this.val$node, true);
        }
    }

    class AnonymousClass5 implements ActionListener {
        private final CategoryNodeEditor this$0;
        private final CategoryNode val$node;

        AnonymousClass5(CategoryNodeEditor categoryNodeEditor, CategoryNode categoryNode) {
            this.this$0 = categoryNodeEditor;
            this.val$node = categoryNode;
        }

        public void actionPerformed(ActionEvent e) {
            this.this$0._categoryModel.setDescendantSelection(this.val$node, false);
        }
    }

    class AnonymousClass6 implements ActionListener {
        private final CategoryNodeEditor this$0;
        private final CategoryNode val$node;

        AnonymousClass6(CategoryNodeEditor categoryNodeEditor, CategoryNode categoryNode) {
            this.this$0 = categoryNodeEditor;
            this.val$node = categoryNode;
        }

        public void actionPerformed(ActionEvent e) {
            this.this$0.expandDescendants(this.val$node);
        }
    }

    class AnonymousClass7 implements ActionListener {
        private final CategoryNodeEditor this$0;
        private final CategoryNode val$node;

        AnonymousClass7(CategoryNodeEditor categoryNodeEditor, CategoryNode categoryNode) {
            this.this$0 = categoryNodeEditor;
            this.val$node = categoryNode;
        }

        public void actionPerformed(ActionEvent e) {
            this.this$0.collapseDescendants(this.val$node);
        }
    }

    public CategoryNodeEditor(CategoryExplorerModel model) {
        this._categoryModel = model;
        this._checkBox.addActionListener(new ActionListener(this) {
            private final CategoryNodeEditor this$0;

            {
                this.this$0 = r1;
            }

            public void actionPerformed(ActionEvent e) {
                this.this$0._categoryModel.update(this.this$0._lastEditedNode, this.this$0._checkBox.isSelected());
                this.this$0.stopCellEditing();
            }
        });
        this._renderer.addMouseListener(new MouseAdapter(this) {
            private final CategoryNodeEditor this$0;

            {
                this.this$0 = r1;
            }

            public void mousePressed(MouseEvent e) {
                if ((e.getModifiers() & 4) != 0) {
                    this.this$0.showPopup(this.this$0._lastEditedNode, e.getX(), e.getY());
                }
                this.this$0.stopCellEditing();
            }
        });
    }

    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        this._lastEditedNode = (CategoryNode) value;
        this._tree = tree;
        return this._renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, true);
    }

    public Object getCellEditorValue() {
        return this._lastEditedNode.getUserObject();
    }

    protected JMenuItem createPropertiesMenuItem(CategoryNode node) {
        JMenuItem result = new JMenuItem("Properties");
        result.addActionListener(new AnonymousClass3(this, node));
        return result;
    }

    protected void showPropertiesDialog(CategoryNode node) {
        JOptionPane.showMessageDialog(this._tree, getDisplayedProperties(node), new StringBuffer().append("Category Properties: ").append(node.getTitle()).toString(), -1);
    }

    protected Object getDisplayedProperties(CategoryNode node) {
        ArrayList result = new ArrayList();
        result.add(new StringBuffer().append("Category: ").append(node.getTitle()).toString());
        if (node.hasFatalRecords()) {
            result.add("Contains at least one fatal LogRecord.");
        }
        if (node.hasFatalChildren()) {
            result.add("Contains descendants with a fatal LogRecord.");
        }
        result.add(new StringBuffer().append("LogRecords in this category alone: ").append(node.getNumberOfContainedRecords()).toString());
        result.add(new StringBuffer().append("LogRecords in descendant categories: ").append(node.getNumberOfRecordsFromChildren()).toString());
        result.add(new StringBuffer().append("LogRecords in this category including descendants: ").append(node.getTotalNumberOfRecords()).toString());
        return result.toArray();
    }

    protected void showPopup(CategoryNode node, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setSize(150, HttpStatus.SC_BAD_REQUEST);
        if (node.getParent() == null) {
            popup.add(createRemoveMenuItem());
            popup.addSeparator();
        }
        popup.add(createSelectDescendantsMenuItem(node));
        popup.add(createUnselectDescendantsMenuItem(node));
        popup.addSeparator();
        popup.add(createExpandMenuItem(node));
        popup.add(createCollapseMenuItem(node));
        popup.addSeparator();
        popup.add(createPropertiesMenuItem(node));
        popup.show(this._renderer, x, y);
    }

    protected JMenuItem createSelectDescendantsMenuItem(CategoryNode node) {
        JMenuItem selectDescendants = new JMenuItem("Select All Descendant Categories");
        selectDescendants.addActionListener(new AnonymousClass4(this, node));
        return selectDescendants;
    }

    protected JMenuItem createUnselectDescendantsMenuItem(CategoryNode node) {
        JMenuItem unselectDescendants = new JMenuItem("Deselect All Descendant Categories");
        unselectDescendants.addActionListener(new AnonymousClass5(this, node));
        return unselectDescendants;
    }

    protected JMenuItem createExpandMenuItem(CategoryNode node) {
        JMenuItem result = new JMenuItem("Expand All Descendant Categories");
        result.addActionListener(new AnonymousClass6(this, node));
        return result;
    }

    protected JMenuItem createCollapseMenuItem(CategoryNode node) {
        JMenuItem result = new JMenuItem("Collapse All Descendant Categories");
        result.addActionListener(new AnonymousClass7(this, node));
        return result;
    }

    protected JMenuItem createRemoveMenuItem() {
        JMenuItem result = new JMenuItem("Remove All Empty Categories");
        result.addActionListener(new ActionListener(this) {
            private final CategoryNodeEditor this$0;

            {
                this.this$0 = r1;
            }

            public void actionPerformed(ActionEvent e) {
                do {
                } while (this.this$0.removeUnusedNodes() > 0);
            }
        });
        return result;
    }

    protected void expandDescendants(CategoryNode node) {
        Enumeration descendants = node.depthFirstEnumeration();
        while (descendants.hasMoreElements()) {
            expand((CategoryNode) descendants.nextElement());
        }
    }

    protected void collapseDescendants(CategoryNode node) {
        Enumeration descendants = node.depthFirstEnumeration();
        while (descendants.hasMoreElements()) {
            collapse((CategoryNode) descendants.nextElement());
        }
    }

    protected int removeUnusedNodes() {
        int count = 0;
        Enumeration enumeration = this._categoryModel.getRootCategoryNode().depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            CategoryNode node = (CategoryNode) enumeration.nextElement();
            if (node.isLeaf() && node.getNumberOfContainedRecords() == 0 && node.getParent() != null) {
                this._categoryModel.removeNodeFromParent(node);
                count++;
            }
        }
        return count;
    }

    protected void expand(CategoryNode node) {
        this._tree.expandPath(getTreePath(node));
    }

    protected TreePath getTreePath(CategoryNode node) {
        return new TreePath(node.getPath());
    }

    protected void collapse(CategoryNode node) {
        this._tree.collapsePath(getTreePath(node));
    }
}
