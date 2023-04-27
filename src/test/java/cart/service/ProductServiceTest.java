package cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import cart.dao.ProductDao;
import cart.dto.ProductRequest;
import cart.dto.ProductResponse;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.transaction.annotation.Transactional;

@JdbcTest
@Transactional
@SuppressWarnings("NonAsciiCharacters")
class ProductServiceTest {

    private ProductService productService;

    @Autowired
    private ProductDao productDao;

    @BeforeEach
    void init() {
        productService = new ProductService(productDao);
    }

    @Test
    @DisplayName("저장된 결과를 반환한다.")
    void saveAndFindTest() {
        saveProduct("치킨", 10000);

        final List<ProductResponse> result = productService.findAll();
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getName()).isEqualTo("치킨"),
                () -> assertThat(result.get(0).getPrice()).isEqualTo(10000),
                () -> assertThat(result.get(0).getImgUrl()).isEqualTo("img")
        );
    }

    @Nested
    @DisplayName("삭제 테스트를 진행한다.")
    class Delete {

        @Test
        void 상품을_삭제를_성공한다() {
            // given
            final Long id = saveProduct("샐러드", 20000);

            // when
            productService.delete(id);

            // then
            final List<ProductResponse> results = productService.findAll();
            assertThat(results).isEmpty();
        }

        @Test
        void id가_없는_상품을_삭제하려면_에러를_반환한다() {
            // when, then
            final Long noneSavedId = 100L;

            assertThatThrownBy(() -> productService.delete(noneSavedId))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Test
    @DisplayName("id에 해당하는 상품을 수정한다.")
    void update() {
        // given
        final Long id = saveProduct("샐러드", 20000);
        final ProductRequest request = new ProductRequest("치킨", 10000, "changedImg");

        // when
        productService.update(id, request);

        // then
        final List<ProductResponse> results = productService.findAll();
        assertAll(
                () -> assertThat(results.get(0).getName()).isEqualTo("치킨"),
                () -> assertThat(results.get(0).getPrice()).isEqualTo(10000),
                () -> assertThat(results.get(0).getImgUrl()).isEqualTo("changedImg")
        );
    }

    private Long saveProduct(final String name, final int price) {
        final ProductRequest request = new ProductRequest(name, price, "img");
        return productService.save(request);
    }
}