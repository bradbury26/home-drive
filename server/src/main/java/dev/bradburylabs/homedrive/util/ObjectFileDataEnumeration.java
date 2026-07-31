package dev.bradburylabs.homedrive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectFileDataEnumeration implements Enumeration<InputStream> {
    private final Iterator<File> fileIterator;

    @Override
    public boolean hasMoreElements() {
        return fileIterator.hasNext();
    }

    @Override
    public InputStream nextElement() {
        try {
            return new FileInputStream(fileIterator.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
