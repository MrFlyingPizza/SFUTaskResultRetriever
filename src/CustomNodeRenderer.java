import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class CustomNodeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

        // Assuming you have a tree of Strings
        CustomNode node = (CustomNode) value;
        String nodeData = node.getData();

        // If the node is a leaf and ends with "xxx"
        if (leaf && nodeData.contains("All unit tests passed. Task succeeded.")) {
            // Paint the node in blue
            setForeground(new Color(100, 200, 110));
        } else if (((String) node.getUserObject()).endsWith(".txt")) {
            setForeground(new Color(255, 83, 83));
        }

        return this;
    }
}