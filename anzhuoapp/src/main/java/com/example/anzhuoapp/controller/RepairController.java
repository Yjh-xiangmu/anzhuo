package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.Repair;
import com.example.anzhuoapp.mapper.RepairMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    @Autowired
    private RepairMapper repairMapper;

    @PostMapping("/submit")
    public String submit(@RequestBody Map<String, Object> params) {
        try {
            repairMapper.submitRepair(
                    (String) params.get("username"),
                    (Integer) params.get("deviceId"),
                    (String) params.get("description"));
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "error"; }
    }

    @GetMapping("/myList")
    public List<Repair> getMyRepairs(@RequestParam String username) {
        return repairMapper.getMyRepairs(username);
    }

    @GetMapping("/allList")
    public List<Repair> getAllRepairs() { return repairMapper.getAllRepairs(); }

    @GetMapping("/pendingCount")
    public int getPendingCount() { return repairMapper.getPendingCount(); }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestBody Map<String, Object> params) {
        try {
            repairMapper.updateRepairStatus(
                    (Integer) params.get("id"), (String) params.get("status"));
            return "success";
        } catch (Exception e) { return "error"; }
    }
}