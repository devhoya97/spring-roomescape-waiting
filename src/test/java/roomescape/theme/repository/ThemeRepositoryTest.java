package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialThemeFixture.INITIAL_THEME_COUNT;
import static roomescape.InitialThemeFixture.NOT_RESERVED_THEME;
import static roomescape.InitialThemeFixture.NOT_SAVED_THEME;
import static roomescape.InitialThemeFixture.THEME_1;
import static roomescape.InitialThemeFixture.THEME_3;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.NotFoundException;
import roomescape.theme.domain.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("테마를 저장한다.")
    void save() {
        themeRepository.save(NOT_SAVED_THEME);
        long count = themeRepository.count();

        assertThat(count).isEqualTo(INITIAL_THEME_COUNT + 1);
    }

    @Test
    @DisplayName("특정 테마 이름을 가진 테마가 저장되어 있으면 true를 반환한다.")
    void returnTrueIfExist() {
        boolean isExist = themeRepository.existsByName(THEME_1.getName());

        assertThat(isExist).isTrue();
    }

    @Test
    @DisplayName("특정 테마 이름을 가진 테마가 저장되어 있지 않으면 false를 반환한다.")
    void returnFalseIfNotExist() {
        boolean isExist = themeRepository.existsByName(NOT_SAVED_THEME.getName());

        assertThat(isExist).isFalse();
    }

    @Test
    @DisplayName("참조되어 있는 테마를 삭제하는 경우 예외가 발생한다.")
    void deleteReferencedTime() {
        assertThatThrownBy(() -> themeRepository.deleteById(THEME_1.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("모든 theme를 찾는다.")
    void findAll() {
        Iterable<Theme> found = themeRepository.findAll();

        assertThat(found).hasSize(INITIAL_THEME_COUNT);
    }

    @Test
    @DisplayName("id에 맞는 theme를 찾는다.")
    void findById() {
        Theme found = themeRepository.findById(THEME_1.getId()).get();

        assertThat(found.getName()).isEqualTo(THEME_1.getName());
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 빈 Optional 객체를 반환한다.")
    void returnEmptyIfIdNotExists() {
        Optional<Theme> theme = themeRepository.findById(-1L);

        assertThat(theme.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("id에 맞는 theme를 찾는다.")
    void getById() {
        Theme found = themeRepository.findById(THEME_1.getId()).get();

        assertThat(found.getName()).isEqualTo(THEME_1.getName());
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 예외가 발생한다.")
    void throwExceptionIfIdNotExists() {
        assertThatThrownBy(() -> themeRepository.getById(-1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 정보를 바탕으로 인기테마를 찾는다.")
    void findTrendings() {
        List<Theme> trendings = themeRepository.findTrendingThemesBetweenDates(
                LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(1),
                PageRequest.of(0, 1)
        );

        assertThat(trendings).containsExactly(THEME_3);
    }

    @Test
    @DisplayName("id에 맞는 Theme을 제거한다.")
    void delete() {
        themeRepository.deleteById(NOT_RESERVED_THEME.getId());

        assertThat(themeRepository.findById(NOT_RESERVED_THEME.getId()).isEmpty()).isTrue();
    }
}