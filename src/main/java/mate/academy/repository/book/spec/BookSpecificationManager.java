package mate.academy.repository.book.spec;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.exception.SpecificationNotFoundException;
import mate.academy.model.Book;
import mate.academy.repository.SpecificationProvider;
import mate.academy.repository.SpecificationProviderManager;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> bookSpecificationProviders;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return bookSpecificationProviders.stream()
                .filter(provider -> provider.getKey().equals(key))
                .findFirst()
                .orElseThrow(() ->
                        new SpecificationNotFoundException(
                                "Cannot find the right specification provider for the key: "
                                        + key));
    }
}
