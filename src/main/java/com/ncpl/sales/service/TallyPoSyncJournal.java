package com.ncpl.sales.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Durable write intents and per-PO results. Never stores passwords or sends requests. */
final class TallyPoSyncJournal {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Path path;
    private final String destination;
    private final Map<String,Map<String,String>> entries;

    TallyPoSyncJournal(String stateFile, String destination) throws IOException {
        path = Paths.get(stateFile + ".journal.json").toAbsolutePath();
        this.destination = destination;
        entries = load(path);
    }
    private static Map<String,Map<String,String>> load(Path path) throws IOException {
        if (!Files.exists(path)) return new LinkedHashMap<>();
        return JSON.readValue(Files.readAllBytes(path), new TypeReference<LinkedHashMap<String,Map<String,String>>>() {});
    }
    static List<Map<String,String>> read(String stateFile, String destination) throws IOException {
        List<Map<String,String>> result = new ArrayList<>();
        for (Map<String,String> entry : load(Paths.get(stateFile + ".journal.json").toAbsolutePath()).values())
            if (destination.equals(entry.get("destination"))) result.add(new LinkedHashMap<>(entry));
        result.sort((a,b) -> b.get("updatedAt").compareTo(a.get("updatedAt")));
        return result;
    }
    boolean isUncertain(String number) {
        Map<String,String> entry=entries.get(destination + "|" + number);
        return entry != null && ("SENDING".equals(entry.get("status")) || "UNCERTAIN".equals(entry.get("status")));
    }
    synchronized String status(String number) {
        Map<String,String> entry=entries.get(destination + "|" + number);
        return entry == null ? "" : entry.getOrDefault("status", "");
    }
    void record(String number, String status, String message) {
        Map<String,String> entry=new LinkedHashMap<>();
        entry.put("destination",destination); entry.put("poNumber",number); entry.put("status",status);
        entry.put("message",message); entry.put("updatedAt",Instant.now().toString());
        // Keep an unresolved intent sticky even if a later preparation/read fails.
        if (isUncertain(number) && !("SYNCED".equals(status) || "UNCHANGED".equals(status) || "RETRY_PENDING".equals(status)))
            entry.put("status","UNCERTAIN");
        Map<String,String> previous=entries.get(destination + "|" + number);
        if (previous!=null && entry.get("status").equals(previous.get("status"))
                && message.equals(previous.get("message"))) return;
        entries.put(destination + "|" + number,entry);
        try {
            Files.createDirectories(path.getParent());
            Path tmp=Files.createTempFile(path.getParent(),"po-journal-",".tmp");
            try {
                Files.write(tmp,JSON.writerWithDefaultPrettyPrinter().writeValueAsBytes(entries));
                try { Files.move(tmp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException e) { Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING); }
            } finally { Files.deleteIfExists(tmp); }
        } catch (IOException error) { throw new IllegalStateException("Cannot persist PO sync history; export stopped",error); }
    }
    synchronized void recordAutoReview(String number, String status, String message) {
        record(number, status, message);
    }
    static Lease acquire(String stateFile) throws IOException {
        Path lock=Paths.get(stateFile + ".lock").toAbsolutePath(); Files.createDirectories(lock.getParent());
        FileChannel channel=FileChannel.open(lock,StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        try {
            FileLock held=channel.tryLock();
            if (held == null) throw new IllegalStateException("Another ERP process is synchronizing POs.");
            return new Lease(channel,held);
        } catch (IOException | RuntimeException error) {
            channel.close();
            if (error instanceof OverlappingFileLockException)
                throw new IllegalStateException("Another ERP process is synchronizing POs.",error);
            throw error;
        }
    }
    static final class Lease implements AutoCloseable {
        private final FileChannel channel; private final FileLock lock;
        Lease(FileChannel channel,FileLock lock) { this.channel=channel;this.lock=lock; }
        public void close() throws IOException { try { lock.release(); } finally { channel.close(); } }
    }
}
