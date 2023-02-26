package fr.emevel.locallink.server.sync;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

@ToString
@AllArgsConstructor
@Getter
public class FileAction {

    public enum Action {
        ADD,
        REMOVE,
        UPDATE
    }

    private final File file;
    private final Action action;

}
