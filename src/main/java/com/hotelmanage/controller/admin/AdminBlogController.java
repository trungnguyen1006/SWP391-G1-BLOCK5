package com.hotelmanage.controller.admin;

import com.hotelmanage.entity.blog.Blog;
import com.hotelmanage.repository.blog.BlogRepository;
import com.hotelmanage.service.CloudinaryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/blogs")
@RequiredArgsConstructor
public class AdminBlogController {

    private final BlogRepository blogRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Blog> blogPage;

        if (query != null && !query.trim().isEmpty()) {
            blogPage = blogRepository.findByTitleContainingIgnoreCase(query.trim(), pageable);
        } else {
            blogPage = blogRepository.findAll(pageable);
        }

        model.addAttribute("blogs", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("q", query);
        return "admin/blog/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("blog", new Blog());
        return "admin/blog/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute BlogForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         Model model) {
        if (bindingResult.hasErrors()) {
            Blog blog = new Blog();
            blog.setTitle(form.getTitle());
            blog.setExcerpt(form.getExcerpt());
            blog.setContent(form.getContent());
            model.addAttribute("blog", blog);
            return "admin/blog/form";
        }

        Blog blog = new Blog();
        blog.setTitle(form.getTitle());
        blog.setExcerpt(form.getExcerpt());
        blog.setContent(form.getContent());

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image, "blogs");
                blog.setImageUrl(imageUrl);
            } catch (IOException e) {
                model.addAttribute("blog", blog);
                model.addAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
                return "admin/blog/form";
            }
        }

        blogRepository.save(blog);
        return "redirect:/admin/blogs?success";
    }

    @GetMapping("/{id}/view")
    public String view(@PathVariable Long id, Model model) {
        Blog blog = blogRepository.findById(id).orElse(null);
        if (blog == null) {
            return "redirect:/admin/blogs?notFound";
        }
        model.addAttribute("blog", blog);
        return "admin/blog/view";
    }

    @GetMapping("/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Blog blog = blogRepository.findById(id).orElse(null);
        if (blog == null) {
            return "redirect:/admin/blogs?notFound";
        }
        model.addAttribute("blog", blog);
        return "admin/blog/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute BlogForm form,
                         BindingResult bindingResult,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         Model model) {
        Blog blog = blogRepository.findById(id).orElse(null);
        if (blog == null) {
            return "redirect:/admin/blogs?notFound";
        }

        if (bindingResult.hasErrors()) {
            blog.setTitle(form.getTitle());
            blog.setExcerpt(form.getExcerpt());
            blog.setContent(form.getContent());
            model.addAttribute("blog", blog);
            return "admin/blog/form";
        }

        blog.setTitle(form.getTitle());
        blog.setExcerpt(form.getExcerpt());
        blog.setContent(form.getContent());

        // Upload new image if provided
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image, "blogs");
                blog.setImageUrl(imageUrl);
            } catch (IOException e) {
                model.addAttribute("blog", blog);
                model.addAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
                return "admin/blog/form";
            }
        }

        blogRepository.save(blog);
        return "redirect:/admin/blogs?updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        blogRepository.deleteById(id);
        return "redirect:/admin/blogs?deleted";
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BlogForm {
        String title;
        String excerpt;
        String content;
    }
}


