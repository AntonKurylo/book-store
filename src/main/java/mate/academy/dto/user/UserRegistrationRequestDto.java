package mate.academy.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mate.academy.validation.FieldMatch;

@FieldMatch(
        field = "password",
        fieldMatch = "repeatPassword",
        message = "Passwords do not match"
)
public record UserRegistrationRequestDto(
        @NotBlank(message = "Email cannot be empty")
        @Size(max = 128, message = "Email must be less than 128 characters")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
        String password,
        @NotBlank(message = "Re-password cannot be empty")
        @Size(min = 8, max = 32, message = "Re-password must be between 8 and 32 characters")
        String repeatPassword,
        @NotBlank(message = "First name cannot be empty")
        @Size(max = 64, message = "First name must be less than 64 characters")
        String firstName,
        @NotBlank(message = "Last name cannot be empty")
        @Size(max = 64, message = "Last name must be less than 64 characters")
        String lastName,
        @Size(max = 256, message = "Shipping address must be less than 256 characters")
        String shippingAddress
) {
}

