package com.skillswap.platform.controller;

import com.skillswap.platform.entity.SwapRequest;
import com.skillswap.platform.entity.User;
import com.skillswap.platform.repository.SwapRequestRepository;
import com.skillswap.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/requests")
public class SwapRequestController {

    @Autowired
    private SwapRequestRepository swapRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String viewRequests(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        List<SwapRequest> incomingRequests = swapRequestRepository.findByReceiver(currentUser);
        List<SwapRequest> outgoingRequests = swapRequestRepository.findBySender(currentUser);
        
        model.addAttribute("incoming", incomingRequests);
        model.addAttribute("outgoing", outgoingRequests);
        model.addAttribute("user", currentUser);
        
        return "requests";
    }

    @PostMapping("/send")
    public String sendRequest(@RequestParam Long receiverId, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {
        if (principal == null) return "redirect:/login";
        
        try {
            User sender = userRepository.findByUsername(principal.getName()).orElseThrow();
            User receiver = userRepository.findById(receiverId).orElse(null);
            
            if (receiver == null || sender.getId().equals(receiver.getId())) {
                return "redirect:/skills/search?error=InvalidUser";
            }
            
            // Economy Logic: Check points
            if (sender.getPoints() < 10) {
                return "redirect:/skills/search?error=InsufficientCoins";
            }

            // Check if already pending
            if (swapRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, "PENDING")) {
                return "redirect:/skills/search?info=AlreadySent";
            }

            sender.setPoints(sender.getPoints() - 10);
            userRepository.save(sender);

            SwapRequest request = new SwapRequest();
            request.setSender(sender);
            request.setReceiver(receiver);
            request.setRequestedSkill(receiver.getSkillsOffered() != null ? receiver.getSkillsOffered() : "Help");
            request.setOfferedSkill(sender.getSkillsOffered() != null ? sender.getSkillsOffered() : "Help");
            request.setStatus("PENDING");
                    
            swapRequestRepository.save(request);
            
            return "redirect:/requests?success=RequestSent";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/skills/search?error=InternalServerError";
        }
    }

    @PostMapping("/{id}/accept")
    public String acceptRequest(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        SwapRequest request = swapRequestRepository.findById(id).orElse(null);
        
        if (request != null && request.getReceiver().getId().equals(currentUser.getId())) {
            request.setStatus("ACCEPTED");
            swapRequestRepository.save(request);
            
            // Economy Logic: Add points to receiver
            currentUser.setPoints(currentUser.getPoints() + 10);
            userRepository.save(currentUser);
        }
        
        return "redirect:/requests?success=Accepted";
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        SwapRequest request = swapRequestRepository.findById(id).orElse(null);
        
        if (request != null && request.getReceiver().getId().equals(currentUser.getId())) {
            request.setStatus("REJECTED");
            swapRequestRepository.save(request);

            // Refund points to sender
            User sender = request.getSender();
            sender.setPoints(sender.getPoints() + 10);
            userRepository.save(sender);
        }
        
        return "redirect:/requests?success=Rejected";
    }

    @PostMapping("/{id}/complete")
    public String completeRequest(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";
        
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        SwapRequest request = swapRequestRepository.findById(id).orElse(null);
        
        if (request != null && request.getStatus().equals("ACCEPTED") && 
            (request.getReceiver().getId().equals(currentUser.getId()) || request.getSender().getId().equals(currentUser.getId()))) {
            
            request.setStatus("COMPLETED");
            swapRequestRepository.save(request);
            
            User sender = request.getSender();
            User receiver = request.getReceiver();
            
            addBadge(sender, "Verified " + request.getRequestedSkill());
            addBadge(receiver, "Verified " + request.getOfferedSkill());
            
            userRepository.save(sender);
            userRepository.save(receiver);
        }
        
        return "redirect:/chat/" + id + "?success=Completed";
    }

    private void addBadge(User user, String newBadge) {
        String current = user.getBadge();
        if (current == null || current.trim().isEmpty()) {
            user.setBadge(newBadge);
        } else if (!current.contains(newBadge)) {
            user.setBadge(current + ", " + newBadge);
        }
    }
}
