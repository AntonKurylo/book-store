package mate.academy.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequestDto(
        @NotBlank(message = "Name cannot be empty")
        @Size(max = 128, message = "Name must be less than 128 characters")
        String name,
        @Size(max = 256, message = "Description must be less than 256 characters")
        String description
) {
}
