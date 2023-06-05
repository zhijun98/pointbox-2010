/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Zhijun Zhang
 */
class EmsTreeFolder implements Serializable {
    private Set<EmsTreeFolder> subFolders = new HashSet<EmsTreeFolder>();
    private Set<EmsTreeItem> paletteItems = new HashSet<EmsTreeItem>();
    private String displayName;
    private String description;
    private String icon;

    public EmsTreeFolder() {
        // Allow user the flexibilty to create
    }

    public EmsTreeFolder(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public void addSubFolder(EmsTreeFolder folder) {
        subFolders.add(folder);
    }

    public void removeSubFolder(EmsTreeFolder folder) {
        subFolders.remove(folder);
    }

    public Iterator getSubFolders() {
        return subFolders.iterator();
    }

    public void addPaletteItem(EmsTreeItem item) {
        paletteItems.add(item);
    }

    public void removePaletteItem(EmsTreeItem item) {
        paletteItems.remove(item);
    }

    public Iterator getPaletteItems() {
        return paletteItems.iterator();
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public String getDisplayName() {
        return displayName;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getDescription() {
        return description;
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getIcon() {
        return icon;
    }
}
