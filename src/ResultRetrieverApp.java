import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class ResultRetrieverApp {
    private JPanel panel1;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel authLabel;
    private JButton submitButton;
    private JPanel authPanel;
    private JLabel passwordLabel;
    private JLabel usernameLabel;
    private JScrollPane resultTreeScrollPanel;
    private JTree resultTree;
    private JTextArea resultTextArea;
    private JScrollPane resultScrollPanel;
    private JPanel resultTreeButtonPanel;
    private JButton refreshButton;
    private JButton reAuthButton;
    private JPanel resultTextButtonPanel;
    private JPanel resultPanel;
    private JLabel loginSuccessLabel;
    private JLabel resultTreeButtonStatusLabel;
    private final ResultRetriever retriever = new ResultRetriever();

    public final String FAILED_GET_RESULT_MSG = "Failed get results!";
    public final String FAILED_LOGIN_MSG = "Login Failed!";
    public final String SUCCESS_LOGIN_MSG = "Login Success!";

    public ResultRetrieverApp() {
        resultPanel.setVisible(false);

        submitButton.addActionListener(e -> {
            String username;
            char[] password;
            username = usernameField.getText();
            username = username.trim();
            password = passwordField.getPassword();
            StringBuilder strBuilder = new StringBuilder();
            for (char ch : password) {
                strBuilder.append(ch);
            }
            login(username, strBuilder.toString().trim());

            retrieveResults();
        });

        resultTree.addTreeSelectionListener(e -> {
            Object[] paths = e.getPath().getPath();
            String[] stringPaths = new String[4];
            if (paths.length == 4) {
                for (int i = 0; i < 4; i++) {
                    TreeNode node = (TreeNode) paths[i];
                    stringPaths[i] = node.toString();
                }
                String content = retriever.getTaskResultByPath(stringPaths[1], stringPaths[2], stringPaths[3]);
                resultTextArea.setText(content);
            }
        });

        refreshButton.addActionListener(e -> {
            if (retrieveResults()) {
                resultTreeButtonStatusLabel.setText("");
            } else {
                resultTreeButtonStatusLabel.setText(FAILED_GET_RESULT_MSG);
            }
        });

        reAuthButton.addActionListener(e -> {
            try {
                retriever.sendLoginRequest();
                retriever.automateGetResultParse();
            } catch (IOException exception) {
                resultTreeButtonStatusLabel.setText(FAILED_LOGIN_MSG);
                exception.printStackTrace();
            } catch (ParseException exception) {
                resultTreeButtonStatusLabel.setText(FAILED_GET_RESULT_MSG);
                exception.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ResultRetrieverApp");
        frame.setContentPane(new ResultRetrieverApp().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    private void login(String username, String password) {
        try {
            retriever.setLogin(username, password);
            retriever.sendLoginRequest();
            loginSuccessLabel.setText(SUCCESS_LOGIN_MSG);

            authPanel.getTopLevelAncestor().setSize(850, 400);
            Dimension minWindowSize = new Dimension(640, 480);
            authPanel.getTopLevelAncestor().setMinimumSize(minWindowSize);
            authPanel.setVisible(false);
            resultPanel.setVisible(true);

        } catch (IOException | ParseException exception) {
            loginSuccessLabel.setText(FAILED_LOGIN_MSG);
        }
    }

    private boolean retrieveResults() {
        boolean parseSuccess = retriever.automateGetResultParse();
        if (parseSuccess) {
            setResultTree();
            return true;
        } else {
            return false;
        }
    }

    private void setResultTree() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("results");
        DefaultMutableTreeNode tree = setResultRecursive(retriever.results, top);
        DefaultTreeModel treeModel = new DefaultTreeModel(tree);
        resultTree.setModel(treeModel);
    }

    private DefaultMutableTreeNode setResultRecursive(Object result, DefaultMutableTreeNode node) {
        HashMap new_result = (HashMap) result;
        for (Object key : new_result.keySet()) {
            if (!((String) key).contains("/")) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(key);
                node.add(child);
                System.out.println("add end child key " + key);
            } else {
                Object new_obj = new_result.get(key);
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(key);
                DefaultMutableTreeNode temp = setResultRecursive(new_obj, child);
                node.add(temp);
                System.out.println("add child key " + key);
            }
        }
        return node;
    }
}