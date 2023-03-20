package ru.practicum.ewm.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.mappers.UserMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Autowired
    protected UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        List<User> users = userRepository.findAllById(ids, PageRequest.of(from / size, size)).getContent();
        return users.stream().map(userMapper::mapToUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto saveUser(UserDto userDto) throws DataIntegrityViolationException {
        if (userRepository.existsNameLikeCustomQuery(userDto.getName())) {
            throw new DataIntegrityViolationException("Integrity constraint has been violated.");
        }
        final User user = userMapper.mapToUser(userDto);
        return userMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) throws NotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }
}
