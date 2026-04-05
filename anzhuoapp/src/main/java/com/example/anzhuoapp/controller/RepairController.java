package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Repair;
import com.example.anzhuoapp.mapper.RepairMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    @Autowired
    private RepairMapper repairMapper;

    // 固定存放到 E:\Projects\anzhuo\repair_images\
    private static final String UPLOAD_DIR = "E:/Projects/anzhuo/repair_images/";

    @PostMapping("/submit")
    public String submit(@RequestBody Map<String, Object> params) {
        try {
            repairMapper.submitRepair(
                    (String) params.get("username"),
                    (Integer) params.get("deviceId"),
                    (String) params.get("description"),
                    (String) params.get("imageUrl"));
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;
            File dest = new File(UPLOAD_DIR + fileName);
            file.transferTo(dest);

            // 返回手机可访问的 URL
            return "http://192.168.10.105:8080/repair_images/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/myList")
    public List<Repair> getMyRepairs(@RequestParam String username) {
        return repairMapper.getMyRepairs(username);
    }

    @GetMapping("/allList")
    public List<Repair> getAllRepairs() {
        return repairMapper.getAllRepairs();
    }

    @GetMapping("/pendingCount")
    public int getPendingCount() {
        return repairMapper.getPendingCount();
    }

    @GetMapping("/countByUser")
    public int countByUser(@RequestParam String username) {
        return repairMapper.countByUser(username);
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestBody Map<String, Object> params) {
        try {
            repairMapper.updateRepairStatus(
                    (Integer) params.get("id"), (String) params.get("status"));
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}