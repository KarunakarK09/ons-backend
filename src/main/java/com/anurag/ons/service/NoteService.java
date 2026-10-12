
package com.anurag.ons.service;

import com.anurag.ons.entity.Note;
import com.anurag.ons.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository repo;

    public NoteService(NoteRepository repo) {
        this.repo = repo;
    }

    public List<Note> getAllNotes() {
        return repo.findAll();
    }

    public List<Note> searchNotes(String subject) {
        return repo.searchBySubject(subject);
    }

    public Note getNoteById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Note addNote(Note note) {
        return repo.save(note);
    }

    public Note updateNote(Long id, Note note) {
        Note existing = repo.findById(id).orElse(null);

        if (existing == null) {
            return null;
        }

        existing.setTitle(note.getTitle());
        existing.setSubject(note.getSubject());
        existing.setBranch(note.getBranch());
        existing.setSemester(note.getSemester());
        existing.setFileName(note.getFileName());
        existing.setFilePath(note.getFilePath());
        existing.setStatus(note.getStatus());
        existing.setUploadedBy(note.getUploadedBy());

        return repo.save(existing);
    }

    public void deleteNote(Long id) {
        repo.deleteById(id);
    }

    public Note approveNote(Long id) {
        Note note = repo.findById(id).orElse(null);

        if (note != null) {
            note.setStatus("APPROVED");
            return repo.save(note);
        }

        return null;
    }

    public Note rejectNote(Long id) {
        Note note = repo.findById(id).orElse(null);

        if (note != null) {
            note.setStatus("REJECTED");
            return repo.save(note);
        }

        return null;
    }
}
