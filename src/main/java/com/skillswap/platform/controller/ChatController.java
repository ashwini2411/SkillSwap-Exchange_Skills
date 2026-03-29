package com.skillswap.platform.controller;

import com.skillswap.platform.entity.Message;
import com.skillswap.platform.entity.SwapRequest;
import com.skillswap.platform.entity.User;
import com.skillswap.platform.repository.MessageRepository;
import com.skillswap.platform.repository.SwapRequestRepository;
import com.skillswap.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwapRequestRepository swapRequestRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private com.skillswap.platform.service.AiRoadmapService aiRoadmapService;

    @GetMapping("/rooms")
    public String viewChatRooms(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<SwapRequest> activeChats = swapRequestRepository.findByUserAndStatus(currentUser, "ACCEPTED");

        model.addAttribute("chats", activeChats);
        model.addAttribute("currentUser", currentUser);
        return "chat-rooms";
    }

    @GetMapping("/{swapRequestId}")
    public String viewChatRoom(@PathVariable Long swapRequestId, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        SwapRequest request = swapRequestRepository.findById(swapRequestId).orElse(null);

        if (request == null || !request.getStatus().equals("ACCEPTED") || 
            (!request.getSender().getId().equals(currentUser.getId()) && !request.getReceiver().getId().equals(currentUser.getId()))) {
            return "redirect:/chat/rooms?error=InvalidChat";
        }

        List<Message> messages = messageRepository.findBySwapRequestOrderBySentAtAsc(request);

        model.addAttribute("messages", messages);
        model.addAttribute("chatRoom", request);
        model.addAttribute("currentUser", currentUser);
        
        return "chat-room";
    }

    @PostMapping("/{swapRequestId}/send")
    public String sendMessage(@PathVariable Long swapRequestId, @RequestParam("content") String content, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        SwapRequest request = swapRequestRepository.findById(swapRequestId).orElse(null);

        if (request != null && content != null && !content.trim().isEmpty() && request.getStatus().equals("ACCEPTED") && 
            (request.getSender().getId().equals(currentUser.getId()) || request.getReceiver().getId().equals(currentUser.getId()))) {
            
            Message message = new Message();
            message.setSwapRequest(request);
            message.setSender(currentUser);
            message.setContent(content.trim());
            
            messageRepository.save(message);
        }

        return "redirect:/chat/" + swapRequestId;
    }

    @PostMapping("/{swapRequestId}/roadmap")
    public String generateRoadmap(@PathVariable Long swapRequestId, Principal principal) {
        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        SwapRequest request = swapRequestRepository.findById(swapRequestId).orElse(null);

        if (request != null && request.getRoadmap() == null && request.getStatus().equals("ACCEPTED") && 
            (request.getSender().getId().equals(currentUser.getId()) || request.getReceiver().getId().equals(currentUser.getId()))) {
            
            String roadmapText = aiRoadmapService.generateRoadmap(request.getRequestedSkill(), request.getOfferedSkill());
            request.setRoadmap(roadmapText);
            swapRequestRepository.save(request);
            
            Message sysMsg = new Message();
            sysMsg.setSwapRequest(request);
            sysMsg.setSender(currentUser);
            sysMsg.setContent("System: A new custom syllabus has been generated! View it at the top.");
            messageRepository.save(sysMsg);
        }

        return "redirect:/chat/" + swapRequestId;
    }
}
