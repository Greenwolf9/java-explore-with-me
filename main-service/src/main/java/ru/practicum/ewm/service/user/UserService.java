package ru.practicum.ewm.service.user;

import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers(List<Long> ids, int from, int size);

    UserDto saveUser(UserDto userDto) throws DataIntegrityViolationException;

    void deleteUser(Long userId) throws NotFoundException;
}
