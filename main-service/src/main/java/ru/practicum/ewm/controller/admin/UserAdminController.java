package ru.practicum.ewm.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;

import ru.practicum.ewm.service.user.UserService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
public class UserAdminController {
    private final UserService userService;

    @Autowired
    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> findAllUsers(@RequestParam(value = "ids", required = false) List<Long> ids,
                                      @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                      @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("Get all users: request parameters ids {}, from {}, size {} ", ids, from, size);
        return userService.getAllUsers(ids, from, size);
    }

    @PostMapping
    public ResponseEntity<UserDto> saveUser(@RequestBody @Valid UserDto userDto,
                                            BindingResult bindingResult) throws DataIntegrityViolationException {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Field: name. Error: must not be blank. Value: null", new Throwable("Incorrectly made request."));
        }
        log.info("Post user {} ", userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.saveUser(userDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable("userId") Long userId) throws NotFoundException {
        log.info("Delete user by id {} ", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
