package org.apache.log4j.lf5.viewer.categoryexplorer;

import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

public class CategoryExplorerTree extends JTree {
    private static final long serialVersionUID = 8066257446951323576L;
    protected CategoryExplorerModel _model;
    protected boolean _rootAlreadyExpanded;

    public CategoryExplorerTree(CategoryExplorerModel model) {
        super(model);
        this._rootAlreadyExpanded = false;
        this._model = model;
        init();
    }

    public CategoryExplorerTree() {
        this._rootAlreadyExpanded = false;
        this._model = new CategoryExplorerModel(new CategoryNode("Categories"));
        setModel(this._model);
        init();
    }

    public CategoryExplorerModel getExplorerModel() {
        return this._model;
    }

    public String getToolTipText(MouseEvent e) {
        try {
            return super.getToolTipText(e);
        } catch (Exception e2) {
            return "";
        }
    }

    protected void init() {
        putClientProperty("JTree.lineStyle", "Angled");
        CategoryNodeRenderer renderer = new CategoryNodeRenderer();
        setEditable(true);
        setCellRenderer(renderer);
        setCellEditor(new CategoryImmediateEditor(this, new CategoryNodeRenderer(), new CategoryNodeEditor(this._model)));
        setShowsRootHandles(true);
        setToolTipText("");
        ensureRootExpansion();
    }

    protected void expandRootNode() {
        if (!this._rootAlreadyExpanded) {
            this._rootAlreadyExpanded = true;
            expandPath(new TreePath(this._model.getRootCategoryNode().getPath()));
        }
    }

    protected void ensureRootExpansion() {
        this._model.addTreeModelListener(new TreeModelAdapter(this) {
            private final CategoryExplorerTree this$0;

            {
                this.this$0 = r1;
            }

            public void treeNodesInserted(TreeModelEvent e) {
                this.this$0.expandRootNode();
            }
        });
    }
}
