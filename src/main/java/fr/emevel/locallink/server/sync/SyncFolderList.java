package fr.emevel.locallink.server.sync;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SyncFolderList implements Serializable {

    @Getter
    private List<SyncFolder> folders = new ArrayList<>();

    public SyncFolder getFolder(UUID id) {
        for (SyncFolder folder : folders) {
            if (folder.getUuid().equals(id)) {
                return folder;
            }
        }
        return null;
    }

    public void addFolder(SyncFolder folder) {
        folders.add(folder);
    }

    public void removeFolder(SyncFolder folder) {
        folders.remove(folder);
    }

}
