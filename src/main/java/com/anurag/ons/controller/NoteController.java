
package com.anurag.ons.controller;

import com.anurag.ons.entity.Note;
import com.anurag.ons.service.NoteService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "http://localhost:5173")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Note> getAllNotes(
            @RequestParam(name = "subject", required = false) String subject) {
        return subject != null && !subject.isBlank()
                ? service.searchNotes(subject)
                : service.getAllNotes();
    }

    @GetMapping("/my-notes")
    public List<Note> getMyNotes(@RequestParam("email") String email) {
        return service.getAllNotes().stream()
                .filter(n -> n.getUploadedBy() != null
                        && email.equalsIgnoreCase(n.getUploadedBy()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Note n = service.getNoteById(id);
        return n == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(n);
    }

    @PostMapping
    public Note addNote(@RequestBody Note note) {
        return service.addNote(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id, @RequestBody Note note) {
        Note n = service.updateNote(id, note);
        return n == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(n);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (service.getNoteById(id) == null) {
            return ResponseEntity.notFound().build();
        }

        service.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Note uploadNote(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam("branch") String branch,
            @RequestParam("semester") String semester,
            @RequestParam("uploadedBy") String uploadedBy)
            throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);

        String original = file.getOriginalFilename();

        if (original == null || original.isBlank()) {
            original = "uploaded-file";
        }

        original = Paths.get(original).getFileName().toString();

        String stored = UUID.randomUUID() + "_" + original;
        Path saved = dir.resolve(stored);

        Files.copy(
                file.getInputStream(),
                saved,
                StandardCopyOption.REPLACE_EXISTING);

        Note note = new Note();
        note.setTitle(title);
        note.setSubject(subject);
        note.setBranch(branch);
        note.setSemester(semester);
        note.setFileName(original);
        note.setFilePath(saved.toString());
        note.setStatus("PENDING");
        note.setUploadedBy(uploadedBy);

        return service.addNote(note);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Note> approveNote(@PathVariable Long id) {
        Note note = service.approveNote(id);

        return note == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(note);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Note> rejectNote(@PathVariable Long id) {
        Note note = service.rejectNote(id);

        return note == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(note);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadNote(
            @PathVariable Long id) throws IOException {

        Note note = service.getNoteById(id);

        if (note == null
                || !"APPROVED".equals(note.getStatus())
                || note.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(note.getFilePath());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + note.getFileName() + "\"")
                .body(resource);
    }
}
