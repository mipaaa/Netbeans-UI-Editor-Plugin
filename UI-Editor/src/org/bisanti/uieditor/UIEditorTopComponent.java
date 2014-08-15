/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bisanti.uieditor;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import org.bisanti.util.FileUtil;
import org.bisanti.util.StringUtil;
import org.bisanti.util.Util;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.NotificationDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.bisanti.uieditor//UIEditor//EN",
autostore = false)
@TopComponent.Description(preferredID = "UIEditorTopComponent",
iconBase = "org/bisanti/uieditor/mainIcon.png", 
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
    public static final String EXT = ".ui_editor";
    
    public static final String FILE = FileUtil.USER_HOME + File.separator + ".netbeans" + EXT;  
    
    public static final Set<UIProperty> applied = new TreeSet<UIProperty>();
    
    private static final Map<Object, Object> JAVA_DEFAULTS = UIManager.getDefaults();
    
    private static final LookAndFeel DEFAULT_LAF = UIManager.getLookAndFeel();
    
    private final Map<String, String> installedLafs =
            new TreeMap<String, String>();
    
    private final String STEEL_THEME = "Steel";
    
    private final String OCEAN_THEME = "Ocean";
    
    private final ExplorerManager manager = new ExplorerManager();
    
    private final Set<UIProperty> changed = new TreeSet<UIProperty>();
    
    private JFileChooser jfc;
    
    private Object lastLafSelection;
    
    private boolean changesApplied;

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
    
    public static void loadSettings(File file)
    {
        if(file.exists())
        {
            try
            {
                UISettings settings = FileUtil.readObjects(file, UISettings.class).get(0);
                UIManager.setLookAndFeel(settings.getLafName());
                apply(settings.getProperties());
            } 
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static boolean apply(Collection<UIProperty> props)
    {
        boolean restartRequired = false;
        String currentLaf = UIManager.getLookAndFeel().getName();
        
        for(UIProperty prop: props)
        {
            Object name = prop.getName();
            Object newValue = prop.getValue();
            Object oldValue = UIManager.put(name, newValue);
            
            if(!restartRequired && !Util.equal(oldValue, newValue) && 
              (StringUtil.indexOf(false, name.toString(), "font") > -1 || StringUtil.indexOf(false, currentLaf, "nimbus") > -1))
            {
                restartRequired = true;
            }
            
            applied.add(prop);
        }
        
        repaintUI();
        
        return restartRequired;
    }
    
    private static void repaintUI()
    {
        SwingUtilities.updateComponentTreeUI(WindowManager.getDefault().getMainWindow());
        WindowManager.getDefault().getMainWindow().pack();
        WindowManager.getDefault().getMainWindow().repaint();
    }
    
    private JFileChooser getJFC()
    {
        if(this.jfc == null)
        {
            this.jfc = new JFileChooser();
            this.jfc.setMultiSelectionEnabled(false);
            this.jfc.setFileFilter(new UIFileFilter());
        }
        
        return this.jfc;
    }
    
    private Icon convertToSize(String property)
    {
        Icon icon = UIManager.getIcon(property);
        if(icon != null && icon.getIconHeight() > 16 && icon.getIconWidth() > 16)
        {
            Image image = ImageUtilities.icon2Image(icon);
            image = image.getScaledInstance(16, 16, 0);
            return ImageUtilities.image2Icon(image);
        }
        return icon;
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
        final String oldLAF = UIManager.getLookAndFeel().getName();
        
        if(!Util.equal(oldLAF, name) && oldLAF.toLowerCase().endsWith("windows"))
        {            
            DialogDescriptor msg = new DialogDescriptor(
                    "Switching from the standard Window LAF to other LAFs has known issues.\n\nIf you are trying to set a new default LAF and Exceptions appear, click the 'Save as My Default' button and restart NetBeans.\n\nDo you want to apply your new LAF?",
                    "Windows LAF Warning");
            msg.setMessageType(DialogDescriptor.WARNING_MESSAGE);
            
            if(!DialogDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(msg)))
            {
                this.lafComboBox.setSelectedItem(this.lastLafSelection);
                return;
            }            
        }
        
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
            
            this.jfc = null;
            UIManager.setLookAndFeel(name);
            repaintUI();
            this.updateLafDescription();
            
            if(this.changesApplied)
            {
                this.restartNotif();
            }
        } 
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void refreshProperties()
    {                
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
        propTreeTable = new org.openide.explorer.view.OutlineView();
        jLabel1 = new javax.swing.JLabel();
        lafComboBox = new javax.swing.JComboBox();
        themeLabel = new javax.swing.JLabel();
        themeComboBox = new javax.swing.JComboBox();
        setLafButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lafDescription = new javax.swing.JLabel();
        expandButton = new javax.swing.JButton();
        collapseButton = new javax.swing.JButton();
        applyPropsButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();

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
                        .addGap(0, 325, Short.MAX_VALUE)))
                .addContainerGap())
        );
        lafsPanelLayout.setVerticalGroup(
            lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lafsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lafsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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

        org.openide.awt.Mnemonics.setLocalizedText(applyPropsButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.applyPropsButton.text")); // NOI18N
        applyPropsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                applyPropsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(helpButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.helpButton.text")); // NOI18N
        helpButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                helpButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(saveAsButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.saveAsButton.text")); // NOI18N
        saveAsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveAsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(loadButton, org.openide.util.NbBundle.getMessage(UIEditorTopComponent.class, "UIEditorTopComponent.loadButton.text")); // NOI18N
        loadButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(loadButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(helpButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveAsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetButton))
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(expandButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(collapseButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(applyPropsButton))
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
                    .addComponent(saveButton)
                    .addComponent(helpButton)
                    .addComponent(saveAsButton)
                    .addComponent(loadButton))
                .addGap(28, 28, 28)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lafsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(expandButton)
                    .addComponent(collapseButton)
                    .addComponent(applyPropsButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lafComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_lafComboBoxItemStateChanged
    {//GEN-HEADEREND:event_lafComboBoxItemStateChanged
        switch(evt.getStateChange())
        {
            case ItemEvent.SELECTED:
                boolean visible = this.lafComboBox.getSelectedItem().toString().contains("Metal");
                this.themeComboBox.setVisible(visible);
                this.themeLabel.setVisible(visible);
                break;
            case ItemEvent.DESELECTED:
                this.lastLafSelection = evt.getItem();
                break;
        }
    }//GEN-LAST:event_lafComboBoxItemStateChanged

    private void setLafButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_setLafButtonActionPerformed
    {//GEN-HEADEREND:event_setLafButtonActionPerformed
        this.applyLaf(this.installedLafs.get(this.lafComboBox.getSelectedItem().toString()));
        this.refreshProperties();
    }//GEN-LAST:event_setLafButtonActionPerformed

    private void applyPropsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyPropsButtonActionPerformed
    {//GEN-HEADEREND:event_applyPropsButtonActionPerformed
        boolean restart = apply(this.changed);
        this.changesApplied = true;
        this.changed.clear();
        if(restart)
        {
            this.restartNotif();
        }
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
            this.save(new File(FILE));
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

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpButtonActionPerformed
    {//GEN-HEADEREND:event_helpButtonActionPerformed
        HelpAndInfo hai = HelpAndInfo.getInstance();
        hai.setLocationRelativeTo(this);
        hai.setVisible(true);
    }//GEN-LAST:event_helpButtonActionPerformed

    private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsButtonActionPerformed
    {//GEN-HEADEREND:event_saveAsButtonActionPerformed
        File saveAs = null;
        if(this.getJFC().showSaveDialog(this) == JFileChooser.APPROVE_OPTION && (saveAs = this.getJFC().getSelectedFile()) != null)
        {
            if(!saveAs.getAbsolutePath().trim().endsWith(EXT))
            {
                saveAs = new File(saveAs.getAbsolutePath().trim()+EXT);
            }
            this.save(saveAs);
        }
    }//GEN-LAST:event_saveAsButtonActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadButtonActionPerformed
    {//GEN-HEADEREND:event_loadButtonActionPerformed
        if(this.getJFC().showOpenDialog(this) == JFileChooser.APPROVE_OPTION && this.getJFC().getSelectedFile() != null)
        {
            loadSettings(this.getJFC().getSelectedFile());
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyPropsButton;
    private javax.swing.JButton collapseButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton expandButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox lafComboBox;
    private javax.swing.JLabel lafDescription;
    private javax.swing.JPanel lafsPanel;
    private javax.swing.JButton loadButton;
    private org.openide.explorer.view.OutlineView propTreeTable;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JButton saveButton;
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
    
    private void save(File file)
    {   
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
            if (file.exists())
            {
                file.delete();
            }
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
        } 
        catch (Exception ex)
        {
            DialogDescriptor.Message message = new DialogDescriptor.Message("Unable to save current settings:\n\n" + ex.getMessage());
            message.setTitle("Error During Save");
            message.setMessageType(DialogDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
        }
    }
    
    private void restartNotif()
    {
        NotificationDisplayer.getDefault().notify("UI-Editor: Restart Recommended at " + SimpleDateFormat.getTimeInstance().format(System.currentTimeMillis()), 
                    ImageUtilities.image2Icon(this.getIcon()), 
                    "One or more properties may not have been applied. Click here for more information.", 
                    new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            final String restart = "Save as Default Settings and Restart";
                            DialogDescriptor restartDialog = new DialogDescriptor(
                                    "One or more properties may not have been applied correctly. These typically include Font related properties and Nimbus " +
                                    "Look and Feel related properties.\n\nIf the applied changes do not appear to have taken effect, click the '" + restart +
                                    "' button to save your current default settings and restart NetBeans.\n\nIf you have current defaults saved, you will " +
                                    "be given the chance to save them with a different name", "UI-Editor: Restart Recommended");
                            
                            restartDialog.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
                            restartDialog.setOptions(new Object[]{restart, DialogDescriptor.CANCEL_OPTION});
                            
                            if(DialogDisplayer.getDefault().notify(restartDialog).equals(restart))
                            {
                                File defaults = new File(FILE);
                                if(defaults.exists())
                                {
                                    DialogDescriptor saveOff = new DialogDescriptor("You currently have default settings saved. Would you like to save these under a different file name so you can revert to them later?", "UI-Editor: Rename Current Settings");
                                    if(DialogDisplayer.getDefault().notify(saveOff).equals(DialogDescriptor.OK_OPTION))
                                    {
                                        File newFile = null;
                                        boolean saved = false;
                                        while(!saved)
                                        {
                                            if(getJFC().showSaveDialog(UIEditorTopComponent.this) == JFileChooser.APPROVE_OPTION && (newFile = getJFC().getSelectedFile()) != null)
                                            {
                                                try
                                                {
                                                    saved = true;
                                                    if(!newFile.getAbsolutePath().endsWith(EXT))
                                                    {
                                                        newFile = new File(newFile.getAbsolutePath().trim() + EXT);
                                                    }
                                                    if(newFile.exists())
                                                    {
                                                        newFile.delete();
                                                    }
                                                    newFile.createNewFile();
                                                    FileUtil.writeObjects(newFile, FileUtil.readObjects(defaults, UISettings.class).get(0));                                                
                                                } 
                                                catch (Exception ex)
                                                {
                                                    Exceptions.printStackTrace(ex);
                                                }
                                            }
                                            
                                            if(!saved)
                                            {
                                                String no = "No, I WANT to save my defaults!";
                                                String yes = "Yes, I DO NOT WANT to save my defaults!";
                                                DialogDescriptor confirm = new DialogDescriptor("You did not save your defaults. Are you sure you do not want to save them?", "Defaults Not Saved");
                                                confirm.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                                                confirm.setOptions(new Object[]{no, yes});
                                                if(DialogDisplayer.getDefault().notify(confirm).equals(yes))
                                                {
                                                    saved = true;
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                save(new File(FILE));
                                LifecycleManager lm = LifecycleManager.getDefault();
                                lm.saveAll();
                                lm.markForRestart();
                                lm.exit();
                            }
                        }
                    });
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
    
    @Override
    public void repaint()
    {
        super.repaint();
        Icon saveIcon = this.convertToSize("FileView.floppyDriveIcon");
        this.saveButton.setIcon(saveIcon);
        this.saveAsButton.setIcon(saveIcon);
        this.helpButton.setIcon(this.convertToSize("OptionPane.questionIcon"));
        this.deleteButton.setIcon(this.convertToSize("OptionPane.errorIcon"));
        this.resetButton.setIcon(this.convertToSize("FileView.computerIcon"));
        this.loadButton.setIcon(this.convertToSize("FileView.fileIcon"));
    }
    
    public class UIFileFilter extends FileFilter
    {
        @Override
        public boolean accept(File f)
        {
            return f.isDirectory() || f.getName().endsWith(EXT);
        }

        @Override
        public String getDescription()
        {
            return "*" + EXT;
        }
        
    }
    
}
