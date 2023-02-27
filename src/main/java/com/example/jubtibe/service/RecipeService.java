package com.example.jubtibe.service;

import com.example.jubtibe.domain.recipe.dto.RecipeRequestDto;
import com.example.jubtibe.domain.recipe.dto.RecipeResponseDto;
import com.example.jubtibe.domain.recipe.dto.RecipeSearchDto;
import com.example.jubtibe.domain.recipe.entity.Recipe;
import com.example.jubtibe.domain.user.entity.User;
import com.example.jubtibe.domain.user.entity.UserRoleEnum;
import com.example.jubtibe.dto.StatusResponseDto;
import com.example.jubtibe.entity.Timestamped;
import com.example.jubtibe.exception.CustomException;
import com.example.jubtibe.exception.ErrorCode;
import com.example.jubtibe.repository.RecipeRepository;
import com.example.jubtibe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService extends Timestamped {
    // 작성자 박성민
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public StatusResponseDto createRecipe(RecipeRequestDto requestDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_CLIENT)
        );
        recipeRepository.save(new Recipe(requestDto, user));

        return StatusResponseDto.builder()
                .statusCode(200)
                .msg("작성 완료")
                .build();
    }
    @Transactional(readOnly = true)
    public List<RecipeSearchDto> getRecipes() {
        List<Recipe> recipeList = recipeRepository.findAllByOrderByCreatedAtDesc();
        List<RecipeSearchDto> responseDtoList = new ArrayList<>();
        for(Recipe recipe : recipeList){
            responseDtoList.add(new RecipeSearchDto(recipe));
        }
        return responseDtoList;
    }

    @Transactional(readOnly = true)
    public RecipeResponseDto getRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_RECIPE)
        );
        return new RecipeResponseDto(recipe,recipe.getComments());
    }

    @Transactional
    public StatusResponseDto updateRecipe(Long id, RecipeRequestDto requestDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_CLIENT)
        );
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("레시피를 찾을 수 없습니다.")
        );

        if (user.getRole()==(UserRoleEnum.ADMIN) || recipe.getUser().getUsername().equals(username)){
            recipe.update(requestDto);
        }else return StatusResponseDto.builder()
                .statusCode(400)
                .msg("수정 권한이 없습니다.")
                .build();

        return StatusResponseDto.builder()
                .statusCode(200)
                .msg("수정 완료")
                .build();
    }

    @Transactional
    public StatusResponseDto deleteRecipe(Long id, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_CLIENT)
        );
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_RECIPE)
        );

        if (user.getRole().equals(UserRoleEnum.ADMIN) || recipe.getUser().getUsername().equals(username)){
            recipeRepository.delete(recipe);
        }else return StatusResponseDto.builder()
                .statusCode(400)
                .msg("삭제 권한이 없습니다.")
                .build();

        return StatusResponseDto.builder()
                .statusCode(200)
                .msg("삭제 완료")
                .build();
    }
}
