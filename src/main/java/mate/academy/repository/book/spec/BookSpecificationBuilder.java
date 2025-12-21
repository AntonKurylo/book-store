package mate.academy.repository.book.spec;

import lombok.RequiredArgsConstructor;
import mate.academy.dto.book.BookSearchParametersDto;
import mate.academy.model.Book;
import mate.academy.repository.SpecificationBuilder;
import mate.academy.repository.SpecificationProviderManager;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final SpecificationProviderManager<Book> bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> spec = Specification.where(null);
        if (Strings.isNotBlank(searchParameters.title())) {
            spec = spec.or(bookSpecificationProviderManager
                    .getSpecificationProvider("title")
                    .getSpecification(searchParameters.title().trim()));
        }
        if (Strings.isNotBlank(searchParameters.author())) {
            spec = spec.or(bookSpecificationProviderManager
                    .getSpecificationProvider("author")
                    .getSpecification(searchParameters.author().trim()));
        }
        return spec;
    }
}
