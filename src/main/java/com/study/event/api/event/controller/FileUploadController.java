package com.study.event.api.event.controller;

import com.study.event.api.event.dto.request.EventUserSaveDto;
import com.study.event.api.event.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService uploadService;

    // 파일 업로드 처리 요청
    @PostMapping("/file/upload")
    public ResponseEntity<?> upload (
            @RequestPart(value = "userData") EventUserSaveDto dto,
            @RequestPart(value = "profileImage") MultipartFile uploadFile) {
        // @RequestPart - json 따로, file 따로 받을수 있게 해준다.
        // @RequestBody 는 json 만 받을 수 있다.

        log.info("userData: {}", dto);
        log.info("profileImage: {}", uploadFile.getOriginalFilename());

        // 파일을 업로드하기
        String fileUrl = "";
        try {
            fileUrl = uploadService.uploadProfileImage(uploadFile);
        } catch (IOException e) {
            log.warn("파일 업로드에 실패했습니다.");
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // SecurityConfig 에서 요청 허용 추가해야한다.
        return ResponseEntity.ok().body(fileUrl);

        // post, http://localhost:8787/file/upload, 추가전 403에러, 추가후 200 Ok

        // post, // post, http://localhost:8787/file/upload, Body, form-data
        // ... - context type, key - userData, value - {"email":"a@gmail.com", "password":"1234"}, Content-type - application/json
        // key - profileImage, File, value - 이미지파일선택, Content-type - image/png
    }
}
