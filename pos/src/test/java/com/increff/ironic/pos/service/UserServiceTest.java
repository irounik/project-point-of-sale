package com.increff.ironic.pos.service;

import com.increff.ironic.pos.dao.UserDao;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.spring.AbstractUnitTest;
import com.increff.ironic.pos.testutils.AssertUtils;
import com.increff.ironic.pos.testutils.MockUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class UserServiceTest extends AbstractUnitTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAddUser() throws ApiException {
        User user = MockUtils.getMockUser();
        userService.add(user);
        User actual = userDao.selectByEmail(user.getEmail());
        AssertUtils.assertEqualUsers(user, actual);
    }

    @Test
    public void testAddAdminUser() throws ApiException {
        User user = MockUtils.getMockUser();
        user.setEmail("admin@test.com");
        userService.add(user);
        User actual = userDao.selectByEmail(user.getEmail());
        AssertUtils.assertEqualUsers(user, actual);
        Assert.assertEquals(actual.getRole(), UserRole.SUPERVISOR);
    }

    @Test
    public void testSelectByEmailForInvalidEmail() throws ApiException {
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("No user found with email");
        userService.getByEmail("invalid@email.com");
    }

    @Test
    public void testSelectByEmail() throws ApiException {
        User mockUser = MockUtils.getMockUser();
        userDao.insert(mockUser);
        User actual = userService.getByEmail(mockUser.getEmail());
        AssertUtils.assertEqualUsers(mockUser, actual);
    }

    @Test
    public void addingDuplicateUserThrowsApiException() throws ApiException {
        User original = MockUtils.getMockUser();
        userDao.insert(original);
        exceptionRule.expect(ApiException.class);
        exceptionRule.expectMessage("User with given email already exists");
        User duplicate = MockUtils.getMockUser();
        userService.add(duplicate);
    }

    @Test
    public void testGetAllUsers() {
        List<User> expectedUsers = Arrays.asList(
                new User(null, "email1@mail.com", "Pass1", UserRole.OPERATOR),
                new User(null, "email2@mail.com", "Pass2", UserRole.SUPERVISOR),
                new User(null, "email3@mail.com", "Pass2", UserRole.OPERATOR)
        );
        expectedUsers.forEach(userDao::insert);

        List<User> actualUsers = userService.getAll();
        AssertUtils.assertEqualList(expectedUsers, actualUsers, AssertUtils::assertEqualUsers);
    }

    @Test
    public void deleteUserWithValidId() throws ApiException {
        User mockUser = MockUtils.getMockUser();
        userDao.insert(mockUser);
        userService.delete(mockUser.getId());

        boolean userStillExists = userDao
                .selectAll()
                .stream()
                .anyMatch(user -> user.getId().equals(mockUser.getId()));

        Assert.assertFalse(userStillExists);
    }

    @Test
    public void deleteUserWithInvalidIdThrowsException() throws ApiException {
        exceptionRule.expect(ApiException.class);
        int invalidId = -1;
        exceptionRule.expectMessage("No user found with ID: " + invalidId);
        userService.delete(invalidId);
    }

}
