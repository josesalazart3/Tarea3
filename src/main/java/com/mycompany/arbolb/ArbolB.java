package com.mycompany.arbolb;



import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class BTreePanel extends JPanel {
    BTree tree;
    JTextArea textArea;

    BTreePanel(int degree) {
        tree = new BTree(degree);
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JButton insertButton = new JButton("Insertar");
        JButton removeButton = new JButton("Eliminar");
        JButton searchButton = new JButton("Buscar");

        JTextField inputField = new JTextField(5);

        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText();
                if (!input.isEmpty()) {
                    int key = Integer.parseInt(input);
                    tree.insert(key);
                    updateTextArea();
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText();
                if (!input.isEmpty()) {
                    int key = Integer.parseInt(input);
                    tree.remove(key);
                    updateTextArea();
                }
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText();
                if (!input.isEmpty()) {
                    int key = Integer.parseInt(input);
                    boolean found = tree.search(key);
                    if (found)
                        textArea.append("La clave " + key + " está presente en el árbol.\n");
                    else
                        textArea.append("La clave " + key + " no está presente en el árbol.\n");
                }
            }
        });

        controlPanel.add(new JLabel("Clave: "));
        controlPanel.add(inputField);
        controlPanel.add(insertButton);
        controlPanel.add(removeButton);
        controlPanel.add(searchButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    void updateTextArea() {
        textArea.setText("");
        StringBuilder sb = new StringBuilder();
        tree.print(sb);
        textArea.append(sb.toString());
    }
}

public class ArbolB {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Árbol B");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int degree = Integer.parseInt(JOptionPane.showInputDialog("Ingrese el grado del árbol B:"));

        BTreePanel panel = new BTreePanel(degree);
        frame.getContentPane().add(panel);

        frame.setPreferredSize(new Dimension(400, 300));
        frame.pack();
        frame.setVisible(true);
    }
}

class BTreeNode {
    int[] keys;
    int degree;
    BTreeNode[] child;
    boolean leaf;
    int keyCount;

    BTreeNode(int degree, boolean leaf) {
        this.degree = degree;
        this.leaf = leaf;
        this.keys = new int[2 * degree - 1];
        this.child = new BTreeNode[2 * degree];
        this.keyCount = 0;
    }

    int findKey(int k) {
        int idx = 0;
        while (idx < this.keyCount && this.keys[idx] < k)
            idx++;
        return idx;
    }

    void insertNonFull(int k) {
        int idx = findKey(k);

        if (leaf) {
            for (int i = keyCount; i > idx; i--)
                keys[i] = keys[i - 1];
            keys[idx] = k;
            keyCount++;
        } else {
            if (child[idx].keyCount == 2 * degree - 1) {
                splitChild(idx, child[idx]);

                if (keys[idx] < k)
                    idx++;
            }
            child[idx].insertNonFull(k);
        }
    }

    void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.degree, y.leaf);
        z.keyCount = degree - 1;

        for (int j = 0; j < degree - 1; j++)
            z.keys[j] = y.keys[j + degree];

        if (!y.leaf) {
            for (int j = 0; j < degree; j++)
                z.child[j] = y.child[j + degree];
        }

        y.keyCount = degree - 1;

        for (int j = keyCount; j >= i + 1; j--)
            child[j + 1] = child[j];

        child[i + 1] = z;

        for (int j = keyCount - 1; j >= i; j--)
            keys[j + 1] = keys[j];

        keys[i] = y.keys[degree - 1];

        keyCount++;
    }

    void remove(int k) {
        int idx = findKey(k);

        if (idx < keyCount && keys[idx] == k) {
            if (leaf)
                removeFromLeaf(idx);
            else
                removeFromNonLeaf(idx);
        } else {
            if (leaf) {
                System.out.println("La clave " + k + " no está presente en el árbol");
                return;
            }

            boolean flag = (idx == keyCount);

            if (child[idx].keyCount < degree)
                fill(idx);

            if (flag && idx > keyCount)
                child[idx - 1].remove(k);
            else
                child[idx].remove(k);
        }
    }

    void removeFromLeaf(int idx) {
        for (int i = idx + 1; i < keyCount; i++)
            keys[i - 1] = keys[i];

        keyCount--;
    }

    void removeFromNonLeaf(int idx) {
        int k = keys[idx];

        if (child[idx].keyCount >= degree) {
            int pred = getPred(idx);
            keys[idx] = pred;
            child[idx].remove(pred);
        } else if (child[idx + 1].keyCount >= degree) {
            int succ = getSucc(idx);
            keys[idx] = succ;
            child[idx + 1].remove(succ);
        } else {
            merge(idx);
            child[idx].remove(k);
        }
    }

    int getPred(int idx) {
        BTreeNode curr = child[idx];
        while (!curr.leaf)
            curr = curr.child[curr.keyCount];

        return curr.keys[curr.keyCount - 1];
    }

    int getSucc(int idx) {
        BTreeNode curr = child[idx + 1];
        while (!curr.leaf)
            curr = curr.child[0];

        return curr.keys[0];
    }

    void fill(int idx) {
        if (idx != 0 && child[idx - 1].keyCount >= degree)
            borrowFromPrev(idx);
        else if (idx != keyCount && child[idx + 1].keyCount >= degree)
            borrowFromNext(idx);
        else {
            if (idx != keyCount)
                merge(idx);
            else
                merge(idx - 1);
        }
    }

    void borrowFromPrev(int idx) {
        BTreeNode childNode = child[idx];
        BTreeNode siblingNode = child[idx - 1];

        for (int i = childNode.keyCount - 1; i >= 0; --i)
            childNode.keys[i + 1] = childNode.keys[i];

        if (!childNode.leaf) {
            for (int i = childNode.keyCount; i >= 0; --i)
                childNode.child[i + 1] = childNode.child[i];
        }

        childNode.keys[0] = keys[idx - 1];

        if (!leaf)
            childNode.child[0] = siblingNode.child[siblingNode.keyCount];

        keys[idx - 1] = siblingNode.keys[siblingNode.keyCount - 1];

        childNode.keyCount += 1;
        siblingNode.keyCount -= 1;
    }

    void borrowFromNext(int idx) {
        BTreeNode childNode = child[idx];
        BTreeNode siblingNode = child[idx + 1];

        childNode.keys[childNode.keyCount] = keys[idx];

        if (!childNode.leaf)
            childNode.child[childNode.keyCount + 1] = siblingNode.child[0];

        keys[idx] = siblingNode.keys[0];

        for (int i = 1; i < siblingNode.keyCount; ++i)
            siblingNode.keys[i - 1] = siblingNode.keys[i];

        if (!siblingNode.leaf) {
            for (int i = 1; i <= siblingNode.keyCount; ++i)
                siblingNode.child[i - 1] = siblingNode.child[i];
        }

        childNode.keyCount += 1;
        siblingNode.keyCount -= 1;
    }

    void merge(int idx) {
        BTreeNode childNode = child[idx];
        BTreeNode siblingNode = child[idx + 1];

        childNode.keys[degree - 1] = keys[idx];

        for (int i = 0; i < siblingNode.keyCount; ++i)
            childNode.keys[i + degree] = siblingNode.keys[i];

        if (!childNode.leaf) {
            for (int i = 0; i <= siblingNode.keyCount; ++i)
                childNode.child[i + degree] = siblingNode.child[i];
        }

        for (int i = idx + 1; i < keyCount; ++i)
            keys[i - 1] = keys[i];

        for (int i = idx + 2; i <= keyCount; ++i)
            child[i - 1] = child[i];

        childNode.keyCount += siblingNode.keyCount + 1;
        keyCount--;

        siblingNode = null;
    }

    boolean search(int k) {
        int idx = findKey(k);
        if (idx < keyCount && keys[idx] == k)
            return true;
        if (leaf)
            return false;
        return child[idx].search(k);
    }

    void print(StringBuilder sb) {
        for (int i = 0; i < keyCount; i++) {
            sb.append(keys[i]).append(" ");
        }
        if (!leaf) {
            for (int i = 0; i <= keyCount; i++) {
                child[i].print(sb);
            }
        }
    }
}

class BTree {
    BTreeNode root;
    int degree;

    BTree(int degree) {
        this.degree = degree;
        root = null;
    }

    void insert(int k) {
        if (root == null) {
            root = new BTreeNode(degree, true);
            root.keys[0] = k;
            root.keyCount = 1;
        } else {
            if (root.keyCount == 2 * degree - 1) {
                BTreeNode s = new BTreeNode(degree, false);
                s.child[0] = root;
                s.splitChild(0, root);

                int i = 0;
                if (s.keys[0] < k)
                    i++;
                s.child[i].insertNonFull(k);

                root = s;
            } else
                root.insertNonFull(k);
        }
    }

    void remove(int k) {
        if (root == null) {
            System.out.println("El árbol está vacío");
            return;
        }

        root.remove(k);

        if (root.keyCount == 0) {
            if (root.leaf)
                root = null;
            else
                root = root.child[0];
        }
    }

    boolean search(int k) {
        return (root == null) ? false : root.search(k);
    }

    void print(StringBuilder sb) {
        if (root != null)
            root.print(sb);
    }
 }
