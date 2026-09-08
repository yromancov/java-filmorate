package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.controller.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.mapper.UserRowMap;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMap.class}) // Добавьте сюда точное имя класса вашего маппера для User
class UserDbStorageTests {

    private final UserDbStorage userStorage;

    @Test
    public void testGetUser_ShouldReturnUserFromDataSql() {
        Optional<User> userOptional = userStorage.getUser(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Иван");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "ivan");
                    assertThat(user).hasFieldOrPropertyWithValue("email", "ivan@mail.ru");
                });
    }

    @Test
    public void testFindAll_ShouldReturnInitialUsers() {
        Collection<User> users = userStorage.findAll();

        assertThat(users)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    public void testAdd_ShouldCreateNewUser() {
        User newUser = new User();
        newUser.setEmail("new@mail.ru");
        newUser.setLogin("new_user");
        newUser.setName("Новый Пользователь");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        User savedUser = userStorage.add(newUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();

        Optional<User> userOptional = userStorage.getUser(savedUser.getId());
        assertThat(userOptional).isPresent();
    }

    @Test
    public void testUpdate_ShouldModifyExistingUser() {
        User userToUpdate = new User();
        userToUpdate.setId(1L); // Обновляем Ивана
        userToUpdate.setEmail("ivan_new@mail.ru");
        userToUpdate.setLogin("ivan_changed");
        userToUpdate.setName("Иван Обновленный");
        userToUpdate.setBirthday(LocalDate.of(1990, 5, 15));

        userStorage.update(userToUpdate);

        Optional<User> userOptional = userStorage.getUser(1L);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("email", "ivan_new@mail.ru");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "ivan_changed");
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Иван Обновленный");
                });
    }

    @Test
    public void testGetFriends_ShouldReturnMariaForIvan() {
        Collection<User> friends = userStorage.getFriends(1L);

        assertThat(friends)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("id", 2L);
    }

    @Test
    public void testAddAndDeleteFriend() {
        userStorage.addFriend(2L, 1L);
        assertThat(userStorage.getFriends(2L)).hasSize(1);

        userStorage.deleteFriend(2L, 1L);
        assertThat(userStorage.getFriends(2L)).isEmpty();
    }

    @Test
    public void testGetCommonFriends() {
        User common = new User();
        common.setEmail("common@mail.ru");
        common.setLogin("common");
        common.setName("Общий Друг");
        common.setBirthday(LocalDate.of(1995, 1, 1));
        User savedCommon = userStorage.add(common);

        userStorage.addFriend(1L, savedCommon.getId());
        userStorage.addFriend(2L, savedCommon.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(1L, 2L);

        assertThat(commonFriends)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("id", savedCommon.getId());
    }
}
