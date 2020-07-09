import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class CustomNodeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

        CustomNode node = (CustomNode) value;
        String nodeData = node.getData();

        if (leaf && nodeData.contains("All unit tests passed. Task succeeded.")) {
            setForeground(new Color(20, 80, 20));
        } else if (((String) node.getUserObject()).endsWith(".txt")) {
            setForeground(new Color(255, 83, 83));
        }

        return this;
    }
}