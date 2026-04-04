package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Notice;
import com.example.anzhuoapp.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeMapper noticeMapper;

    @GetMapping("/list")
    public List<Notice> getList(
            @RequestParam(required = false, defaultValue = "100") int limit) {
        return noticeMapper.findAll(limit);
    }

    @PostMapping("/add")
    public String add(@RequestBody Map<String, String> params) {
        try {
            noticeMapper.insert(params.get("title"), params.get("content"));
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/delete")
    public String delete(@RequestBody Map<String, Integer> params) {
        try {
            noticeMapper.deleteById(params.get("id"));
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/update")
    public String update(@RequestBody Map<String, Object> params) {
        try {
            noticeMapper.update(
                    (Integer) params.get("id"),
                    (String) params.get("title"),
                    (String) params.get("content"));
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}