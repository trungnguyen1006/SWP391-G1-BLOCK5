package com.hotelmanage.controller.blog;

import com.hotelmanage.repository.blog.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BlogController {

    private final BlogRepository blogRepository;

    @GetMapping("/blogs")
    public String listBlogs(Model model) {
        model.addAttribute("blogs", blogRepository.findAllByOrderByIdDesc());
        return "blog/list";
    }
    @GetMapping("/blogs/{id}")
    public String blogDetail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("blog", blogRepository.findById(id).orElse(null));
        return "blog/detail";
    }
}

