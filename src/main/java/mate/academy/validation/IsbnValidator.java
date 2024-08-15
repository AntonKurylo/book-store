package mate.academy.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IsbnValidator implements ConstraintValidator<Isbn, String> {
    private static final String ISBN10_FORMAT =
            "^(?:\\d{9}[\\d|Xx])|(?:\\d{1,5}-\\d{1,7}-\\d{1,6}-[\\d|Xx])$";
    private static final String ISBN13_FORMAT =
            "^(?:\\d{12}\\d|[\\d|-]{1,5}-\\d{1,7}-\\d{1,6}-\\d)$";

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext constraintValidatorContext) {
        if (Strings.isNotBlank(isbn)) {
            return Pattern.matches(ISBN10_FORMAT, isbn) || Pattern.matches(ISBN13_FORMAT, isbn);
        }
        return false;
    }
}
