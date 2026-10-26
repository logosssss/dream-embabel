package com.zhu.embabel.extract;

import com.zhu.embabel.http.RunStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 页面提交一句话，拿回抽出的标题和正文，或明确的没抽到。
 */
@RestController
@RequestMapping({"/api/maybe-notes", "/api/maybe-notes/"})
public class MaybeNoteController {

    private final MaybeNoteService notes;

    public MaybeNoteController(MaybeNoteService notes) {
        this.notes = notes;
    }

    @PostMapping
    public ResponseEntity<MaybeNoteResponse> extract(@RequestBody MaybeNoteRequest request) {
        MaybeNoteResponse response = notes.extract(request.message());
        return RunStatus.respond(response.status(), response);
    }

    public record MaybeNoteRequest(String message) {
    }
}
