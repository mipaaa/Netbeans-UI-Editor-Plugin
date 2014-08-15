/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jason Bisanti
 */
public class HelpAndInfo extends javax.swing.JFrame
{    
    private static volatile HelpAndInfo instance;
    
    private final Map<JTextPane, String> textBundles = new HashMap<JTextPane, String>();
    
    /**
     * Creates new form HelpAndInfo
     */
    private HelpAndInfo()
    {
        super("UI-Editor Help and Information");
        super.setIconImage(ImageUtilities.loadImage("mainIcon.png"));
        initComponents();
        
        this.textBundles.put(this.howToTextPane, "HelpAndInfo.HowToUse");
        this.textBundles.put(this.tipsAndTricksTextPane, "HelpAndInfo.TipsAndTricks");
        this.textBundles.put(this.issuesTextPane, "HelpAndInfo.KnownIssues");
        this.textBundles.put(this.changesTextPane, "HelpAndInfo.ChangeLog");
        this.textBundles.put(this.aboutTextPane, "HelpAndInfo.About");
        
        ResourceBundle bundle = NbBundle.getBundle(this.getClass());
        for(Map.Entry<JTextPane, String> entry: this.textBundles.entrySet())
        {
            JTextPane tc = entry.getKey();
            tc.setEditorKit(new HTMLEditorKit());
            tc.setText(bundle.getString(entry.getValue()));
            tc.setCaretPosition(0);
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

        jPanel5 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        howToScrollPane = new javax.swing.JScrollPane();
        howToTextPane = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        tipsAndTricksScrollPane = new javax.swing.JScrollPane();
        tipsAndTricksTextPane = new javax.swing.JTextPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        issuesTextPane = new javax.swing.JTextPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        changesTextPane = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        aboutTextPane = new javax.swing.JTextPane();

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        howToTextPane.setEditable(false);
        howToScrollPane.setViewportView(howToTextPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(howToScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(howToScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        tipsAndTricksTextPane.setEditable(false);
        tipsAndTricksScrollPane.setViewportView(tipsAndTricksTextPane);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tipsAndTricksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tipsAndTricksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        issuesTextPane.setEditable(false);
        jScrollPane2.setViewportView(issuesTextPane);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        jScrollPane3.setViewportView(changesTextPane);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(HelpAndInfo.class, "HelpAndInfo.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        aboutTextPane.setEditable(false);
        jScrollPane1.setViewportView(aboutTextPane);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
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
    private javax.swing.JTextPane aboutTextPane;
    private javax.swing.JTextPane changesTextPane;
    private javax.swing.JScrollPane howToScrollPane;
    private javax.swing.JTextPane howToTextPane;
    private javax.swing.JTextPane issuesTextPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane tipsAndTricksScrollPane;
    private javax.swing.JTextPane tipsAndTricksTextPane;
    // End of variables declaration//GEN-END:variables
}
