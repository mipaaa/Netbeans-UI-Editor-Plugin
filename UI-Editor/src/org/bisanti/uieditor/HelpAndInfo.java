/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import javax.swing.text.html.HTMLEditorKit;
import org.bisanti.util.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jason Bisanti
 */
public class HelpAndInfo extends javax.swing.JFrame
{    
    private static volatile HelpAndInfo instance;
    
    /**
     * Creates new form HelpAndInfo
     */
    private HelpAndInfo()
    {
        super("UI-Editor Help and Information");
        initComponents();
        this.howToTextPane.setEditorKit(new HTMLEditorKit());
        this.howToTextPane.setAutoscrolls(false);
        this.tipsAndTricksTextPane.setEditorKit(new HTMLEditorKit());
        
        try
        {
            StringBuilder sb = new StringBuilder();
            for(String line: FileUtil.readText("howToUse.txt"))
            {
                sb.append(line);
            }
            this.howToTextPane.setText(sb.toString());
            this.howToTextPane.setCaretPosition(0);
            sb.delete(0, sb.length());
            
            for(String line: FileUtil.readText("tipsAndTricks.txt"))
            {
                sb.append(line);
            }
            this.tipsAndTricksTextPane.setText(sb.toString());
            this.tipsAndTricksTextPane.setCaretPosition(0);
            
            
        } 
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static HelpAndInfo getInstance()
    {
        return instance == null ? instance = new HelpAndInfo() : instance;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        howToScrollPane = new javax.swing.JScrollPane();
        howToTextPane = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        tipsAndTricksScrollPane = new javax.swing.JScrollPane();
        tipsAndTricksTextPane = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();

        howToTextPane.setEditable(false);
        howToScrollPane.setViewportView(howToTextPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(howToScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(howToScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        tipsAndTricksTextPane.setEditable(false);
        tipsAndTricksScrollPane.setViewportView(tipsAndTricksTextPane);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tipsAndTricksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tipsAndTricksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 371, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 248, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane howToScrollPane;
    private javax.swing.JTextPane howToTextPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane tipsAndTricksScrollPane;
    private javax.swing.JTextPane tipsAndTricksTextPane;
    // End of variables declaration//GEN-END:variables
}