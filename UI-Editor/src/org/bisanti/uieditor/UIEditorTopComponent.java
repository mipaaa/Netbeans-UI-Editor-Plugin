/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.bisanti.util.FileUtil;
import org.bisanti.util.StringUtil;
import org.bisanti.util.Util;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.bisanti.uieditor//UIEditor//EN",
autostore = false)
@TopComponent.Description(preferredID = "UIEditorTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.bisanti.uieditor.UIEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_UIEditorAction",
preferredID = "UIEditorTopComponent")

@SuppressWarnings({"unchecked","rawtypes"})
public final class UIEditorTopComponent extends TopComponent implements
        ExplorerManager.Provider, PropertyChangeListener
{
    public static final String FILE = FileUtil.USER_HOME + File.separator + ".netbeans.ui_editor";
    
    public static final Set<UIProperty> applied = new TreeSet<UIProperty>();
    
    private static final Map<Object, Object> JAVA_DEFAULTS = UIManager.getDefaults();
    
    private static final LookAndFeel DEFAULT_LAF = UIManager.getLookAndFeel();
    
    private final Map<String, String> installedLafs =
            new TreeMap<String, String>();
    
    private final String STEEL_THEME = "Steel";
    
    private final String OCEAN_THEME = "Ocean";
    
    private final ExplorerManager manager = new ExplorerManager();
    
    private final Set<UIProperty> changed = new TreeSet<UIProperty>();
    
    private List<Node> allNodes = new ArrayList<Node>();

    public UIEditorTopComponent()
    {
        for(LookAndFeelInfo lafi: UIManager.getInstalledLookAndFeels())
        {
            this.installedLafs.put(lafi.getName(), lafi.getClassName());
        }
        initComponents();
        
        this.updateLafDescription();
        File file = new File(FILE);
        this.deleteButton.setEnabled(file.exists());        
        this.propTreeTable.getOutline().setRootVisible(false);
        setName(NbBundle.getMessage(UIEditorTopComponent.class, "CTL_UIEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(UIEditorTopComponent.class, "HINT_UIEditorTopComponent"));
        this.lafComboBox.setSelectedItem(UIManager.getLookAndFeel().getName());
        setName(NbBundle.getMessage(UIEditorTopComponent.class, "CTL_UIEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(UIEditorTopComponent.class, "HINT_UIEditorTopComponent"));
        this.refreshProperties();
    }
    
    public static void loadSettings()
    {
        File file = new File(FILE);
        if(file.exists())
        {
            try
            {
                UISettings settings = FileUtil.readObjects(file, UISettings.class).get(0);
                UIManager.setLookAndFeel(settings.getLafName());
                apply(settings.getProperties());
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
            } 
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static void apply(Collection<UIProperty> props)
    {
        for(UIProperty prop: props)
        {
            UIManager.put(prop.getName(), prop.getValue());
            applied.add(prop);
        }
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
        WindowManager.getDefault().getMainWindow().repaint();
    }
    
    private void updateLafDescription()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        this.lafDescription.setText(laf.getID() + " (" + laf.getDescription() + ")");
        this.lafComboBox.setSelectedItem(laf.getName());
        if(laf.getName().contains("Metal"))
        {
            String theme = MetalLookAndFeel.getCurrentTheme().getName();
            if (STEEL_THEME.equals(theme))
            {
                this.themeComboBox.setSelectedItem(STEEL_THEME);
            } 
            else if (OCEAN_THEME.equals(theme))
            {
                this.themeComboBox.setSelectedItem(OCEAN_THEME);
            }
        }
    }
    
    private void applyLaf(String name)
    {
        try
        {
            String selection = this.lafComboBox.getSelectedItem().toString();
            if (selection.contains("Metal"))
            {
                String theme = this.themeComboBox.getSelectedItem().toString();
                if (STEEL_THEME.equals(theme))
                {
                    MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                }
                else if (OCEAN_THEME.equals(theme))
                {
                    MetalLookAndFeel.setCurrentTheme(new OceanTheme());
                }
            }
            UIManager.setLookAndFeel(name);
            SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
            WindowManager.getDefault().getMainWindow().repaint();
            this.updateLafDescription();
        } 
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void refreshProperties()
    {        
        this.allNodes.clear();
        
        SortedMap<String, Collection<UIProperty>> propNodes = 
                new TreeMap<String, Collection<UIProperty>>(String.CASE_INSENSITIVE_ORDER);
        
        LookAndFeel laf = UIManager.getLookAndFeel();
        for(Map.Entry<Object, Object> entry: laf.getDefaults().entrySet())
        {
            String name = entry.getKey().toString();
            
            int index = name.indexOf(".");
            Collection<UIProperty> kids;
            if(index > -1)
            {
                String parent = name.substring(0, index);
                kids = propNodes.get(parent);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    propNodes.put(parent, kids);
                }
                  
            }
            else
            {
                final String misc = "~Miscellaneous";
                kids = propNodes.get(misc);
                if(kids == null)
                {
                    kids = new ArrayList<UIProperty>();
                    propNodes.put(misc, kids);
                }              
            }
            UIProperty prop = new UIProperty(entry);
            kids.add(prop);
        }
        
        for(Collection<UIProperty> list: propNodes.values())
        {
            Collections.sort((List)list);
        }
        
        UIRootNode root = new UIRootNode(propNodes, this);
        Property[] props = root.getPropertySets()[0].getProperties();
        for(Property<Object> col: props)
        {
            this.propTreeTable.removePropertyColumn(col.getName());
            this.propTreeTable.addPropertyColumn(col.getName(), col.getDisplayName(), col.getShortDescription());
        }
        this.manager.setRootContext(root);
        
        for(Node parent: root.getChildren().getNodes(true))
        {
            this.allNodes.add(parent);
            this.allNodes.addAll(Arrays.asList(parent.getChildren().getNodes(true)));
        }
    }
    

    private Node getStartNode()
    {
        Node[] selected = this.manager.getSelectedNodes();
        if(Util.isNullOrEmpty(selected))
        {
            return this.manager.getRootContext();
        }
        else
        {
            return selected[0];
        }
    }
    
    private void findNext(Node start, String text, boolean forward)
    {        
        List<Node> toSearch;
        if(forward)
        {
            toSearch = this.allNodes;
        }
        else
        {
            toSearch = new ArrayList<Node>(this.allNodes);
            Collections.reverse(toSearch);
        }
        
        for(int i=toSearch.indexOf(start) + 1; i<toSearch.size(); i++)
        {
            if(this.contains(this.allNodes.get(i), text))
            {
                this.select(this.allNodes.get(i));
                return;
            }
        }
        
        DialogDisplayer.getDefault().notify(new Message("Text '" + text + "' not found"));
    }
    
    public boolean contains(Node n, String text)
    {
        if(n.getDisplayName().contains(text))
        {
            return true;
        }
        else if(n instanceof UINode)
        {
            UIProperty uip = ((UINode)n).getUIProperty();
            if(String.valueOf(uip.getValue()).contains(text))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private void select(Node... nodes)
    {
        try
        {
            this.manager.setSelectedNodes(nodes);
        } 
        catch (PropertyVetoException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jSeparator1 = new javax.swing.JSeparator();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        lafsPanel = new javax.swing.JPanel();
        applyPropsButton = new javax.swing.JButton();
        propTreeTable = new org.openide.explorer.view.OutlineView("Property");
        jLabel1 = new javax.swing.JLabel();
        lafComboBox = new javax.swing.JComboBox();
        themeLabel = new javax.swing.JLabel();
        themeComboBox = new javax.swing.JComboBox();
        setLafButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lafDescription = new javax.swing.JLabel();
        expandButton = new javax.swing.JButton();
        collapseButton = new javax.swing.JButton();
        searchTextField = new javax.swing.JTextField();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.deleteButton.text")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                resetButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(applyPropsButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.applyPropsButton.text")); // NOI18N
        applyPropsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                applyPropsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jLabel1.text")); // NOI18N

        lafComboBox.setModel(new javax.swing.DefaultComboBoxModel(this.installedLafs.keySet().toArray(new String[0])));
        lafComboBox.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                lafComboBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(themeLabel, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.themeLabel.text")); // NOI18N

        themeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { OCEAN_THEME, STEEL_THEME}));

        org.openide.awt.Mnemonics.setLocalizedText(setLafButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.setLafButton.text")); // NOI18N
        setLafButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                setLafButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lafDescription, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.lafDescription.text")); // NOI18N

        javax.swing.GroupLayout lafsPanelLayout = new javax.swing.GroupLayout(lafsPanel);
        lafsPanel.setLayout(lafsPanelLayout);
        lafsPanelLayout.setHorizontalGroup(
            lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propTreeTable, javax.swing.GroupLayout.DEFAULT_SIZE, 695, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lafsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(themeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setLafButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lafDescription)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 197, Short.MAX_VALUE)
                        .addComponent(applyPropsButton)))
                .addContainerGap())
        );
        lafsPanelLayout.setVerticalGroup(
            lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyPropsButton)
                    .addComponent(jLabel1)
                    .addComponent(lafComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(themeLabel)
                    .addComponent(themeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setLafButton)
                    .addComponent(jLabel2)
                    .addComponent(lafDescription))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propTreeTable, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(expandButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.expandButton.text")); // NOI18N
        expandButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                expandButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(collapseButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.collapseButton.text")); // NOI18N
        collapseButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                collapseButtonActionPerformed(evt);
            }
        });

        searchTextField.setText(org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.searchTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(prevButton, Character.toChars(8593)[0]+ " Previous");
        prevButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                prevButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(nextButton, "     "+Character.toChars(8595)[0]+"Next     ");
        nextButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                nextButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(expandButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(collapseButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(prevButton))
                            .addComponent(lafsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(5, 5, 5)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetButton)
                    .addComponent(deleteButton)
                    .addComponent(saveButton))
                .addGap(28, 28, 28)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lafsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(expandButton)
                    .addComponent(collapseButton)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prevButton)
                    .addComponent(nextButton)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lafComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_lafComboBoxItemStateChanged
    {//GEN-HEADEREND:event_lafComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            boolean visible = this.lafComboBox.getSelectedItem().toString().contains("Metal");
            this.themeComboBox.setVisible(visible);
            this.themeLabel.setVisible(visible);
        }
    }//GEN-LAST:event_lafComboBoxItemStateChanged

    private void setLafButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_setLafButtonActionPerformed
    {//GEN-HEADEREND:event_setLafButtonActionPerformed
        this.applyLaf(this.installedLafs.get(this.lafComboBox.getSelectedItem().toString()));
        this.refreshProperties();
    }//GEN-LAST:event_setLafButtonActionPerformed

    private void applyPropsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyPropsButtonActionPerformed
    {//GEN-HEADEREND:event_applyPropsButtonActionPerformed
        apply(this.changed);
        this.changed.clear();
    }//GEN-LAST:event_applyPropsButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveButtonActionPerformed
    {//GEN-HEADEREND:event_saveButtonActionPerformed
        StringBuilder sb = new StringBuilder("This will set your current UI settings as the default and erase any previously saved settings. The settings will be saved at: ");
        sb.append(FILE).append("\n\n");
        sb.append("Are you sure you want to continue?");
        DialogDescriptor confirm = new DialogDescriptor(sb.toString(), "Confirm Save");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            File file = new File(FILE);
            if(file.exists())
            {
                file.delete();
            }
            
            UISettings uis = new UISettings(UIManager.getLookAndFeel().getClass().getCanonicalName(), applied);
            Set<UIProperty> removed = new TreeSet<UIProperty>();

            if (!FileUtil.isSerializable(applied))
            {
                Iterator<UIProperty> it = applied.iterator();
                while (it.hasNext())
                {
                    UIProperty uip = it.next();
                    if (!FileUtil.isSerializable(uip))
                    {
                        removed.add(uip);
                        it.remove();
                    }
                }
            }            
            
            try
            {
                file.createNewFile();
                FileUtil.writeObjects(file, uis);
                if (!removed.isEmpty())
                {
                    DialogDescriptor.Message message = new DialogDescriptor.Message("The following properties are not Serializable and could not be saved:\n\n" + StringUtil.toString(removed, "\n"));
                    message.setTitle("Not All Properties Saved");
                    message.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(message);
                }
                this.deleteButton.setEnabled(true);
            } catch (Exception ex)
            {
                DialogDescriptor.Message message = new DialogDescriptor.Message("Unable to save current settings:\n\n" + ex.getMessage());
                message.setTitle("Error During Save");
                message.setMessageType(DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(message);
            }
            
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetButtonActionPerformed
    {//GEN-HEADEREND:event_resetButtonActionPerformed
        DialogDescriptor confirm = new DialogDescriptor("This will undo all of your changes and reset the default Look And Feel and color scheme for this platform. Are you sure you want to continue?", "Reset?");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            try
            {
                UIManager.setLookAndFeel(DEFAULT_LAF);
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                UIManager.getLookAndFeelDefaults().putAll(JAVA_DEFAULTS);
                SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
                applied.clear();
                this.refreshProperties();
                this.updateLafDescription();
            } 
            catch (UnsupportedLookAndFeelException ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_resetButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        DialogDescriptor confirm = new DialogDescriptor("This will permanently erase your default settings and they will not be applied the next time NetBeans starts. Are you sure you want to erase them?", "Erase?");
        confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        if(DialogDisplayer.getDefault().notify(confirm) == DialogDescriptor.OK_OPTION)
        {
            File file = new File(FILE);
            file.delete();
            this.deleteButton.setEnabled(false);
        }        
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void expandButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_expandButtonActionPerformed
    {//GEN-HEADEREND:event_expandButtonActionPerformed
        for(Node child: this.manager.getRootContext().getChildren().getNodes(true))
        {
            this.propTreeTable.expandNode(child);
        }
    }//GEN-LAST:event_expandButtonActionPerformed

    private void collapseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_collapseButtonActionPerformed
    {//GEN-HEADEREND:event_collapseButtonActionPerformed
        for(Node child: this.manager.getRootContext().getChildren().getNodes())
        {
            this.propTreeTable.collapseNode(child);
        }
    }//GEN-LAST:event_collapseButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextButtonActionPerformed
    {//GEN-HEADEREND:event_nextButtonActionPerformed
        this.findNext(this.getStartNode(), this.searchTextField.getText(), true);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        this.findNext(this.getStartNode(), this.searchTextField.getText(), false);
    }//GEN-LAST:event_prevButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyPropsButton;
    private javax.swing.JButton collapseButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton expandButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox lafComboBox;
    private javax.swing.JLabel lafDescription;
    private javax.swing.JPanel lafsPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private org.openide.explorer.view.OutlineView propTreeTable;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JButton setLafButton;
    private javax.swing.JComboBox themeComboBox;
    private javax.swing.JLabel themeLabel;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened()
    {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed()
    {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p)
    {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p)
    {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager()
    {
        return this.manager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if(Util.equal(UINode.VALUE_PROP, evt.getPropertyName()))
        {
            this.changed.add((UIProperty)evt.getNewValue());
            this.repaint();
        }
    }
}
