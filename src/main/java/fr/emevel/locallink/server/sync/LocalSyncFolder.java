package fr.emevel.locallink.server.sync;

import fr.emevel.locallink.network.SyncFile;
import fr.emevel.locallink.network.packets.PacketFileList;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LocalSyncFolder implements SyncFolder {

    private final File folder;
    private transient List<SyncFile> files;

    @Override
    public String getName() {
        return folder.getName();
    }

    protected void updateFiles() throws IOException {
        updateFiles(this.folder);
    }

    protected void addFile(File file) throws IOException {
        files.add(new SyncFile(file));
    }

    protected void updateFiles(File folder) throws IOException {
        files = new ArrayList<>();
        if (!folder.isDirectory() || folder.listFiles() == null) {
            return;
        }
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                addFile(file);
            }
        }
    }

    protected List<FileAction> getActions(Iterable<PacketFileList> packets) {

        List<SyncFile> distantFiles = new ArrayList<>();
        for (PacketFileList packet : packets) {
            distantFiles.addAll(packet.getFiles());
        }

        List<FileAction> actions = new ArrayList<>();
        mainloop:
        for (SyncFile file : files) {
            File localFile = new File(folder, file.getName());
            for (SyncFile clientFile : distantFiles) {
                if (file.getName().equals(clientFile.getName())) {
                    if (!file.getSha256().equals(clientFile.getSha256())) {
                        actions.add(new FileAction(localFile, FileAction.Action.UPDATE));
                    }
                    distantFiles.remove(clientFile);
                    continue mainloop;
                }
            }
            actions.add(new FileAction(localFile, FileAction.Action.ADD));
        }
        for (SyncFile clientFile : distantFiles) {
            File localFile = new File(folder, clientFile.getName());
            actions.add(new FileAction(localFile, FileAction.Action.REMOVE));
        }
        return actions;
    }

    @Override
    public List<FileAction> needUpdate(Iterable<PacketFileList> packets) throws IOException {
        updateFiles();
        return getActions(packets);
    }

}
