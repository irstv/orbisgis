/**
 * The GDMS library (Generic Datasource Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...).
 *
 * Gdms is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV FR CNRS 2488
 *
 * This file is part of Gdms.
 *
 * Gdms is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Gdms is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Gdms. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 *
 * or contact directly:
 * info@orbisgis.org
 */
package org.gdms.data.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.gdms.driver.ReadWriteBufferManager;

/**
 * Implementation of a file-based <code>BlockProvider</code>
 * @author Antoine Gourlay
 */
public class FileBlockProvider implements BlockProvider {

        private int groupId;
        private int blockSize = -1;
        private long lastBlockId = -1;
        private ReadWriteBufferManager buffer;
        private RandomAccessFile raf;
        private static final int VERSION_NUMBER = 1;
        public static final int DEFAULT_BLOCK_SIZE = 1024 * 4;

        public FileBlockProvider(int groupId, File file) throws IOException {
                this(groupId, file, DEFAULT_BLOCK_SIZE);
        }

        public FileBlockProvider(int groupId, File file, int blockSize) throws IOException {
                this.groupId = groupId;
                raf = new RandomAccessFile(file, "rw");
                buffer = new ReadWriteBufferManager(raf.getChannel(), blockSize + 16);
                if (raf.length() == 0) {
                        this.blockSize = blockSize;
                        writeHeader();
                } else {
                        readHeader();
                }
        }

        @Override
        public int getGroupId() {
                return groupId;
        }

        private void goToBlock(long blockId) {
                buffer.position(16 + blockId * blockSize);
        }

        @Override
        public void writeBlock(Block b) throws IOException {
                goToBlock(b.getId().getBlockId());
                buffer.put(b.getContent());
                buffer.flush();
        }

        @Override
        public Block readBlock(long blockId) throws IOException {
                goToBlock(blockId);
                BlockFullId id = new BlockFullId(groupId, blockId);
                byte[] content = new byte[blockSize];
                buffer.get(content);
                return new Block(id, content);
        }

        private void readHeader() throws IOException {
                buffer.position(0);
                int version = buffer.getInt();
                if (version != VERSION_NUMBER) {
                        throw new IOException("Badly formed file.");
                }
                blockSize = buffer.getInt();
                lastBlockId = buffer.getLong();
        }

        @Override
        public Block newBlock() {
                lastBlockId++;
                BlockFullId bi = new BlockFullId(groupId, lastBlockId);

                return new Block(bi, blockSize);
        }

        @Override
        public void close() throws IOException {
                buffer.position(8);
                buffer.putLong(lastBlockId);
                buffer.flush();
                raf.close();
        }

        @Override
        public void truncateToBlock(long blockId) throws IOException {
                if (blockId < lastBlockId) {
                        buffer.position(0);
                        raf.setLength(16 + (blockId + 1) * blockSize);
                        lastBlockId = blockId;
                }
        }

        private void writeHeader() throws IOException {
                buffer.position(0);
                buffer.putInt(VERSION_NUMBER);
                buffer.putInt(blockSize);
                buffer.putLong(lastBlockId);
        }

        /**
         * @return the numberOfBlocks
         */
        @Override
        public long getNumberOfBlocks() {
                return lastBlockId + 1;
        }
}
