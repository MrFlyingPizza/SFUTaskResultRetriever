import javax.swing.tree.DefaultMutableTreeNode;

public class CustomNode extends DefaultMutableTreeNode {

    String data = "";

    public CustomNode(Object userObject) {
        super.setUserObject(userObject);
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }
}
