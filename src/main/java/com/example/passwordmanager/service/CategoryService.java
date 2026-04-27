package com.example.passwordmanager.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.passwordmanager.dto.CategoryDto;
import com.example.passwordmanager.entity.Category;
import com.example.passwordmanager.entity.User;
import com.example.passwordmanager.exception.ResourceNotFoundException;
import com.example.passwordmanager.exception.UnauthorizedAccessException;
import com.example.passwordmanager.repository.CategoryRepository;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository,
                           UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public List<CategoryDto> listForCurrentUser() {
        User user = userService.getCurrentUser();
        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createForCurrentUser(CategoryDto dto) {
        User user = userService.getCurrentUser();

        Category category = new Category();
        category.setUser(user);
        category.setName(dto.getName());
        category.setColor(dto.getColor());

        categoryRepository.save(category);
    }

    public Category findEntityById(Long id) {
        if (id == null) {
            return null;
        }

        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Unauthorized category access");
        }

        return category;
    }

    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        return dto;
    }
}
